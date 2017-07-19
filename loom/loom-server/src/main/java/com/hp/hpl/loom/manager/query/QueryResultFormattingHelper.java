/*******************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development LP Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package com.hp.hpl.loom.manager.query;


import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.exceptions.ItemPropertyNotFound;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.RelationPropertyNotFound;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.QueryResult;
import com.hp.hpl.loom.model.QueryResultElement;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.relationships.RelationshipCalculator;
import com.hp.hpl.loom.tapestry.Operation;

/**
 * Helper class that provides the {@link QueryResult} formating. It ensures standard naming, count
 * setting for all {@link QueryResult}.
 *
 */
public final class QueryResultFormattingHelper {
    private static final Log LOG = LogFactory.getLog(QueryResultFormattingHelper.class);

    private QueryResultFormattingHelper() {}

    /**
     * Determines the name for Derived {@link Aggregation} based on the provided parameters.
     *
     * @param derivedLogicalId Logical id to use for the name
     * @param isLast flag indicates if this is the last result
     * @param isBraid flag indicates if this is a braid
     * @param opsDone list of operations done
     * @param key a key
     * @param i counter?
     * @param usedGroupedIds The user group ids
     * @return String of the name
     */
    public static String getNameForDa(final String derivedLogicalId, final boolean isLast, final boolean isBraid,
            final List<Operation> opsDone, final String key, final int i, final Map<String, Integer> usedGroupedIds) {
        String groupedLogicalId;
        if (isLast) {
            if (isBraid) {
                groupedLogicalId = derivedLogicalId + "/" + i; // indices
            } else {
                groupedLogicalId = derivedLogicalId + "/id/" + indexIfAlreadyUsed(key, usedGroupedIds); // group
                                                                                                        // name
            }
        } else {
            String opChain = opsDone.stream().map(Operation::getOperator).collect(joining("/"));
            if (isBraid) {
                groupedLogicalId = derivedLogicalId + "/" + opChain + "/" + i; // indices
            } else {
                // group name
                groupedLogicalId = derivedLogicalId + "/" + opChain + "/id/" + indexIfAlreadyUsed(key, usedGroupedIds);
            }
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("Logical ID of the DAs is " + groupedLogicalId);
        }
        return groupedLogicalId;
    }

    private static String indexIfAlreadyUsed(final String key, final Map<String, Integer> usedGroupedIds) {
        if (usedGroupedIds.containsKey(key)) {
            Integer previous = usedGroupedIds.get(key);
            Integer current = previous + 1;
            usedGroupedIds.put(key, current);
            return key + "[" + current + "]";
        } else {
            usedGroupedIds.put(key, 0);
            return key;
        }
    }

    /**
     * Sets the attributes on the provided aggregation.
     *
     * @param aggregated whether it is aggregated (Braid/Group)
     * @param effectiveBraid whether it is a braided
     * @param isGrouped is it grouped
     * @param derived the aggregation to setup
     * @param aggSize the new size
     * @param aggIndex the largest value in the index
     */
    public static void setAttributes(final boolean effectiveBraid, final boolean isGrouped, final Aggregation derived,
            final int aggSize, int aggIndex, final String opTag) {
        // if (aggregated) {
        // if (effectiveBraid) {
        // tag = BRAID;
        // } else {
        // tag = GROUPED;
        // }
        // } else {
        // if (effectiveBraid) {
        // tag = BRAID;
        // } else {
        // if (isGrouped) {
        // tag = GROUPED;
        // }
        // }
        // }
        derived.setTags(opTag);

        if (aggSize != 0) {
            derived.setMinIndex(aggIndex);
            aggIndex += aggSize - 1;
            derived.setMaxIndex(aggIndex);
        }

        if (derived.isAggregation()) {
            if (derived.getTags().equals(DefaultOperations.BRAID.toString())) {
                derived.setName("[" + ((Aggregation) derived).getMinIndex() + ","
                        + ((Aggregation) derived).getMaxIndex() + "]");
            } else {
                int indx = derived.getLogicalId().lastIndexOf("/id/");
                derived.setName(derived.getLogicalId().substring(indx + 4, derived.getLogicalId().length()));
            }
        }
    }


    /**
     * Returns the {@link Fibre.Type} for the providerd list of entities. It defauls to {@link Item}
     * but if the first entity is an {@link Aggregation} it returns Fibre.Type.Aggregation.
     *
     * @param entities items to examine to determine type
     * @return The fibre type - either Item or Aggregation
     */
    public static Fibre.Type getLoomEntityType(final List<Fibre> entities) {
        Fibre.Type contains = Fibre.Type.Item;

        Optional<Fibre> first = entities.stream().findFirst();
        if (first.isPresent() && first.get().isAggregation()) {
            contains = Fibre.Type.Aggregation;
        }

        return contains;
    }

    /**
     * Converts a {@link Aggregation} into a {@link QueryResult}, it will set the counts by compare
     * it to the previous {@link QueryResult} and DA time.
     *
     * @param previousDaTime time of the previous DA used to determine the changes
     * @param previousQueryResult the previous QueryResult to comapare with (they can be null)
     * @param da DerivedAggregation to build the QueryResult from
     * @param itemType itemType to set on the QueryResult
     * @return the newly created {@link QueryResult}
     */
    public static QueryResult convertToQueryResult(final long previousDaTime, final QueryResult previousQueryResult,
            final Aggregation da, final ItemType itemType) {

        if (da == null) {
            // if (da == null || da.getSize() == 0) {
            LOG.warn("Pipeline resulted in empty DA");
            return new QueryResult();
        }
        String aggTag = da.getTags();
        if (LOG.isTraceEnabled()) {
            LOG.trace("Converting DA " + da.getLogicalId() + " with size " + da.getSize() + " to QueryResult with tag: "
                    + aggTag);
        }

        determineCounts(previousDaTime, previousQueryResult, da);


        ArrayList<QueryResultElement> results = new ArrayList<QueryResultElement>(da.getSize());
        QueryResultElement element;
        for (Fibre entity : da.getElements()) {
            element = new QueryResultElement(entity);
            results.add(element);
        }

        QueryResult queryResult = new QueryResult(da.getLogicalId());
        queryResult.setElements(results);
        queryResult.setName(da.getLogicalId());
        if (da.isPending()) {
            queryResult.setStatus(QueryResult.Statuses.PENDING);
        } else {
            queryResult.setStatus(QueryResult.Statuses.READY);
        }

        if (da.getAlertCount() != null && da.getAlertCount() > 0) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Setting alerts " + da.getLogicalId());
            }
            queryResult.setAlertCount(da.getAlertCount());
            queryResult.setHighestAlertDescription(da.getHighestAlertDescription());
            queryResult.setHighestAlertLevel(da.getHighestAlertLevel());
        }
        queryResult.setItemType(itemType);
        return queryResult;
    }

    /**
     * Determines the changes between the previous QueryResults and the new aggregation. It makes
     * use of the previous da's created time to determine the differences.
     *
     * @param previousDaTime The previous time the DA was updated (used to check new results against
     * @param previousQueryResult The previous query results
     * @param da The DA we are determining counts for
     */
    private static void determineCounts(final long previousDaTime, final QueryResult previousQueryResult,
            final Aggregation da) {
        if (previousQueryResult != null) {
            Map<String, String> previousLogicalIds = previousQueryResult.getContainedItemsLogicalIds();
            Map<String, Aggregation> aggregagtionMap = new HashMap<>();

            for (Fibre entity : da.getElements()) {
                if (entity.isAggregation()) {
                    Aggregation agg = (Aggregation) entity;
                    aggregagtionMap.put(agg.getLogicalId(), agg);
                    List<Item> latestItems = agg.getContainedItems();
                    // determine the differences between the items
                    int createdCount = 0;
                    int updatedCount = 0;
                    for (Item newItem : latestItems) {
                        previousLogicalIds.remove(newItem.getLogicalId());
                        // get the updated time (defaults to created
                        long createdTime = newItem.getFibreCreated().getTime();
                        // get the updated time (defaults to created
                        long updatedTime =
                                newItem.getFibreUpdated() != null ? newItem.getFibreUpdated().getTime() : createdTime;

                        if (createdTime > previousDaTime) {
                            createdCount++;
                        } else if (updatedTime > previousDaTime) {
                            updatedCount++;
                        }
                    }
                    // the totals for this aggregation (the delete is outside of this loop in case a
                    // missing item has appears in another aggregation
                    agg.setCreatedCount(createdCount);
                    agg.setUpdatedCount(updatedCount);
                }
            }

            for (String aggIds : previousLogicalIds.values()) {
                Aggregation agg = aggregagtionMap.get(aggIds);
                if (agg != null) {
                    agg.setDeletedCount(agg.getDeletedCount() + 1);
                }
            }
        }
    }

    /**
     * Populates the relationships for multiple queryResults.
     *
     * @param session The current session.
     * @param qrMap Map of QueryResults to Strings
     * @param gas list of grounded aggregations to calculate from param followEquivalence True if
     *        the relationship calculation should follow equivalence relationships.
     * @param relationshipCalculator relationship calculator to use for setting the relationships
     * @return the qrMap with the relationships setup
     * @throws NoSuchSessionException thrown if no sessions
     * @throws ItemPropertyNotFound thrown if the ItemProperty not found
     * @throws RelationPropertyNotFound thrown if there isn't a relationship
     */
    public static Map<String, QueryResult> populateWithRelationshipsMultiple(final Session session,
            final Map<String, QueryResult> qrMap, final List<Aggregation> gas, final boolean followEquivalence,
            final RelationshipCalculator relationshipCalculator)
            throws NoSuchSessionException, ItemPropertyNotFound, RelationPropertyNotFound {
        try {
            relationshipCalculator.calculateRelationshipsMultiple(session, new ArrayList<QueryResult>(qrMap.values()),
                    gas, followEquivalence);
        } catch (RelationPropertyNotFound e) {
            throw new RelationPropertyNotFound("");
        }
        return qrMap;
    }


    /**
     * Calculates the given aggregation alert level. It does this looping over the elements getting
     * their alert levels and aggregating together.
     *
     * @param da Aggregation to calculate the alerts for
     */
    public static void calculateAggregateAlerts(final Aggregation da) {
        // Long alertCount=aggregatedEntities.stream().mapToLong(le ->
        // ((Aggregation)le).getAlertCount()== null ? 0 : ((Aggregation)le).getAlertCount()
        // ).reduce(0, (x,y) -> x+y );
        List<Fibre> aggregatedEntities = da.getElements();
        if (LOG.isTraceEnabled()) {
            LOG.trace("Aggregating Alerts for " + da.getLogicalId() + " containing " + aggregatedEntities.size()
                    + " entities.");
        }

        if (aggregatedEntities == null || aggregatedEntities.size() == 0) {
            return;
        }

        boolean isItemList = aggregatedEntities.stream().findFirst().get().isItem();
        Long alertCount = 0L;
        String highestAlertDescription = "";
        Integer highestAlertLevel = 0;
        Set<String> providerIds = new HashSet<>();

        Aggregation agg;
        Item item;
        for (Fibre ent : aggregatedEntities) {
            if (isItemList) {
                item = (Item) ent;
                if (item.getAlertLevel() > 0) {
                    alertCount++;
                    if (item.getAlertLevel() > highestAlertLevel) {
                        highestAlertLevel = item.getAlertLevel();
                        highestAlertDescription = item.getAlertDescription();
                    }
                }
                providerIds.addAll(item.getProviderIds());
            } else {
                agg = (Aggregation) ent;
                alertCount += agg.getAlertCount() == null ? 0 : agg.getAlertCount();
                if (agg.getHighestAlertLevel() != null && agg.getHighestAlertLevel() > highestAlertLevel) {
                    highestAlertLevel = agg.getHighestAlertLevel();
                    highestAlertDescription = agg.getHighestAlertDescription();
                }
                providerIds.addAll(agg.getProviderIds());
            }
        }
        // if(LOG.isTraceEnabled())LOG.trace("Alert Count: "+alertCount+" for "+da.getLogicalId());
        // if(LOG.isTraceEnabled())LOG.trace("Top level Alert: "+highestAlertLevel);
        // if(LOG.isTraceEnabled())LOG.trace("Top Alert description: "+highestAlertDescription);

        // if(alertCount>0){
        for (Aggregation parent : da.getDependsOnAggregations()) {
            parent.setAlertCount(alertCount);
            parent.setHighestAlertLevel(highestAlertLevel);
            parent.setHighestAlertDescription(highestAlertDescription);
            parent.setProviderIds(providerIds);
        }
        da.setAlertCount(alertCount);
        da.setHighestAlertLevel(highestAlertLevel);
        da.setHighestAlertDescription(highestAlertDescription);
        da.setProviderIds(providerIds);
    }
}
