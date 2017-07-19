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
package com.hp.hpl.loom.relationships;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.RelationPropertyNotFound;
import com.hp.hpl.loom.manager.stitcher.ItemEquivalence;
import com.hp.hpl.loom.manager.stitcher.Tacker;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.QueryResult;
import com.hp.hpl.loom.model.QueryResultElement;
import com.hp.hpl.loom.model.Session;

/**
 * Calculate relationships, using a RelationshipsModel, between entities and aggregations of
 * entities. The RelationshipsModel is constructed by analysing annotations on Items in the model.
 */
@Component
public class RelationshipCalculator {
    private static final int DEFAULT_DISALLOWED_SIZE = 40;

    private static final Log LOG = LogFactory.getLog(RelationshipCalculator.class);

    private static final ExecutorService THREAD_POOL =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @Autowired
    private Tacker stitcher;

    @Value("${include.timing}")
    private boolean timing = false;

    // If total number of items exceeds this, worth looking for already visited items.
    @Value("${relation.algorithm.useVisitedIdsThreshold}")
    private int useVisitedIdsThreshold;

    // If total number of items exceeds this, use threads for calculateRelationshipsMultiple.
    @Value("${relation.algorithm.useMultipleThreadsThreshold}")
    private int useMultipleThreadsThreshold;

    private GraphProcessor graphProcessor = new GraphProcessorImplIter();

    // ////////////////////////////////////////////////////////////////////////////////
    // Specific methods for relationship calculation.
    // ////////////////////////////////////////////////////////////////////////////////

    // PMD thinks this method isn't used but it is, for now disabling check
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void reportRelationToSelf(final Set<String> relations, final Map<String, Set<String>> relationsTypeMap,
            final Map<String, List<String>> relationPaths, List<String> path, final Fibre item,
            final String ownTopLevelAggregation, final boolean fromFirstLevelAggregation) {
        for (Aggregation aggregation : item.getMemberOf()) {

            for (Aggregation topLevelAggregation : aggregation.calcTopLevelAggregations()) {
                if (topLevelAggregation.getLogicalId().equals(ownTopLevelAggregation)) {
                    continue; // Do not include relationship to own top-level DA.
                }
                if (aggregation.containsAggregations() || !aggregation.isTopLevel()) {
                    addRelationToAggregation(null, relations, relationsTypeMap, aggregation, relationPaths, path);
                } else if (fromFirstLevelAggregation) {
                    // This is tricky. Reference the ID of item itself, so the corresponding
                    // relationship in other DA matches.
                    // The client relies on these symmetric relations.
                    relations.add(item.getLogicalId());

                    // Set<String> rel = relations2.get(relType);
                    // if (rel == null) {
                    // rel = new HashSet<>();
                    // relations2.put(relType, rel);
                    // }
                    // rel.add(item.getLogicalId());

                }
            }
        }
    }

    /**
     * Calculate relationships by processing relationship graph with relationships calculation
     * reporter.
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private void calculateRelationshipsForItem(final Item item, final Set<String> relations,
            final Map<String, Set<String>> relationsTypeMap, final Map<String, List<String>> relationPaths,
            final boolean fromFirstLevelAggregation, final RelationshipsModelImpl model,
            final Set<String> notRelatedToTypes, final String ownTopLevelAggregation, final Set<String> disallowedTypes,
            final Set<String> visitedIds, final boolean followEquivalence, final ItemEquivalence equivalence,
            final Set<String> equivalenceRelations) throws RelationPropertyNotFound {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Calc relations for item " + item.getLogicalId());
        }
        // Include self so pick up on ancestors and descendants as user drills down

        reportRelationToSelf(relations, relationsTypeMap, relationPaths, new ArrayList<String>(), item,
                ownTopLevelAggregation, fromFirstLevelAggregation);
        graphProcessor.doProcessGraphForItem(item, relations, relationsTypeMap, relationPaths,
                this::reportRelationToItem, model, notRelatedToTypes, disallowedTypes, visitedIds, followEquivalence,
                equivalence, this::reportEquivalenceRelationToItem, equivalenceRelations);
    }

    private void calculateRelationshipsForItems(final List<Item> items, final RelationshipsModelImpl model,
            final Set<String> notRelatedToTypes, final String ownTopLevelAggregation, final Set<String> relations,
            final Map<String, Set<String>> relationsTypeMap, final Map<String, List<String>> relationPaths,
            final Set<String> disallowedTypes, final Set<String> visitedIds, final boolean followEquivalence,
            final ItemEquivalence equivalence, final Set<String> equivalenceRelations) throws RelationPropertyNotFound {
        for (Item item : items) {
            calculateRelationshipsForItem(item, relations, relationsTypeMap, relationPaths, true, model,
                    notRelatedToTypes, ownTopLevelAggregation, disallowedTypes, visitedIds, followEquivalence,
                    equivalence, equivalenceRelations);
        }
    }

    private void calculateRelationshipsForQueryResultElement(final RelationshipsModelImpl model,
            final Set<String> notRelatedToTypes, final String ownTopLevelAggregation,
            final QueryResultElement resultElement, final Set<String> disallowedTypes, final Set<String> visitedIds,
            final boolean followEquivalence, final ItemEquivalence equivalence) throws RelationPropertyNotFound {
        Set<String> relations = resultElement.getRelations();
        Map<String, Set<String>> relationsTypeMap = resultElement.getRelationTypes();
        Map<String, List<String>> relationPaths = resultElement.getRelationPaths();

        Set<String> equivalenceRelations = resultElement.getEquivalenceRelations();
        Fibre entity = resultElement.getEntity();

        calculateRelationshipsForFibre(model, notRelatedToTypes, ownTopLevelAggregation, entity, relations,
                relationsTypeMap, relationPaths, disallowedTypes, visitedIds, followEquivalence, equivalence,
                equivalenceRelations);


        // dirty fix to see if this helps with relationship calc
        removeNonDirectRelationships(relationsTypeMap, entity);
    }

    private void removeNonDirectRelationships(Map<String, Set<String>> relationsTypeMap, Fibre entity) {
        Set<String> remove = new HashSet<>();
        Set<String> keys = relationsTypeMap.keySet();
        for (String key : keys) {
            String[] keySplit = key.split(":");
            if (keySplit.length > 1) {
                // if (entity.getItemType() != null) {
                String typeId = entity.getTypeId();

                if (!keySplit[0].equals(typeId) && !keySplit[1].equals(typeId)) {
                    remove.add(key);
                }
                // } else {
                // String[] idSplit = entity.getTypeId().split("-");
                // String id = idSplit[1];
                //
                // if (!keySplit[0].equals(id) && !keySplit[1].equals(id)) {
                // remove.add(key);
                // }
                // }
            }
        }
        relationsTypeMap.keySet().removeAll(remove);
    }

    private void calculateRelationshipsForFibre(final RelationshipsModelImpl model, final Set<String> notRelatedToTypes,
            final String ownTopLevelAggregation, final Fibre entity, final Set<String> relations,
            final Map<String, Set<String>> relationsTypeMap, final Map<String, List<String>> relationPaths,
            final Set<String> disallowedTypes, final Set<String> visitedIds, final boolean followEquivalence,
            final ItemEquivalence equivalence, final Set<String> equivalenceRelations) throws RelationPropertyNotFound {
        relations.clear();
        relationsTypeMap.clear();
        relationPaths.clear();
        equivalenceRelations.clear();
        if (visitedIds != null) {
            visitedIds.clear();
        }
        if (entity.isAggregation()) {
            Aggregation aggregation = (Aggregation) entity;
            // But must calculate relations using contained entities
            calculateRelationshipsForItems(aggregation.getContainedItems(), model, notRelatedToTypes,
                    ownTopLevelAggregation, relations, relationsTypeMap, relationPaths, disallowedTypes, visitedIds,
                    followEquivalence, equivalence, equivalenceRelations);

        } else {
            Item item = (Item) entity;
            calculateRelationshipsForItem(item, relations, relationsTypeMap, relationPaths, false, model,
                    notRelatedToTypes, ownTopLevelAggregation, disallowedTypes, visitedIds, followEquivalence,
                    equivalence, equivalenceRelations);

        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("Calc relations for result element " + entity.getLogicalId() + " agg=" + entity.isAggregation()
                    + " type=" + entity.getTypeId() + " visited=" + (visitedIds != null ? visitedIds.size() : 0));
        }
    }

    /**
     * Calculate relationships for QueryResult data structure, containing relationships for all
     * contained QueryResultElements.
     */
    private void calculateRelationships(final QueryResult result, final RelationshipsModelImpl model,
            final Set<String> notRelatedToTypes, final boolean useVisitedIds, final boolean followEquivalence,
            final ItemEquivalence equivalence, final int expectedNodes) throws RelationPropertyNotFound {
        StopWatch watch = null;
        if (LOG.isDebugEnabled()) {
            watch = new StopWatch();
            watch.start();
        }
        // Start at the root of the results set
        final String ownTopLevelAggregation = result.getLogicalId();
        // A single object re-used between relationships calculation for every item.
        Set<String> disallowedTypes = new HashSet<>(DEFAULT_DISALLOWED_SIZE);
        Set<String> visitedIds = useVisitedIds ? new HashSet<>(2 * expectedNodes) : null;
        for (QueryResultElement resultElement : result.getElements()) {
            calculateRelationshipsForQueryResultElement(model, notRelatedToTypes, ownTopLevelAggregation, resultElement,
                    disallowedTypes, visitedIds, followEquivalence, equivalence);
        }
        if (LOG.isDebugEnabled()) {
            watch.stop();
            int total = result.totalRelations();
            if (total > 0) {
                LOG.debug("Calculated relations=" + total + " " + result.getLogicalId() + " "
                        + result.getItemType().getId() + " relations time=" + watch);
            }
        }
    }

    /**
     * Calculate relationships for multiple QueryResult data structures, containing relationships
     * for all contained QueryResultElements.
     *
     * @throws RelationPropertyNotFound
     */
    public void calculateRelationshipsMultiple(final Session session, final List<QueryResult> queryResults,
            final List<Aggregation> groundedAggregations, final boolean followEquivalence)
            throws RelationPropertyNotFound, NoSuchSessionException {
        List<Set<String>> notRelatedToTypesList = new ArrayList<>(queryResults.size());
        for (int count = 0; count < queryResults.size(); count++) {
            notRelatedToTypesList.add(null);
        }
        calculateRelationshipsMultiple(session, queryResults, notRelatedToTypesList, groundedAggregations,
                followEquivalence);
    }

    private int computeTotalItems(final List<Aggregation> groundedAggregations) {
        int items = 0;
        for (Aggregation ga : groundedAggregations) {
            items += ga.getSize();
        }
        return items;
    }

    public GraphProcessor getGraphProcessor() {
        return graphProcessor;
    }

    /**
     * Calculate relationships for multiple QueryResult data structures, containing relationships
     * for all contained QueryResultElements.
     */
    private void calculateRelationshipsMultiple(final Session session, final List<QueryResult> queryResults,
            final List<Set<String>> notRelatedToTypesList, final List<Aggregation> groundedAggregations,
            final boolean followEquivalence) throws RelationPropertyNotFound, NoSuchSessionException {
        // Decide on calculation strategy. a) whether to use multiple threads. b) whether to
        // remember visited logicalIds to attempt to eliminate already visited Items
        final int totalItems = computeTotalItems(groundedAggregations);
        final boolean useVisitedIds = totalItems > useVisitedIdsThreshold ? true : false;
        final boolean useMultipleThreads = totalItems > useMultipleThreadsThreshold ? true : false;

        doCalculateRelationshipsMultiple(session, queryResults, notRelatedToTypesList, groundedAggregations,
                followEquivalence, useVisitedIds, useMultipleThreads);
    }

    void doCalculateRelationshipsMultiple(final Session session, final List<QueryResult> queryResults,
            final List<Set<String>> notRelatedToTypesList, final List<Aggregation> groundedAggregations,
            final boolean followEquivalence, final boolean useVisitedIds, final boolean useMultipleThreads)
            throws RelationPropertyNotFound, NoSuchSessionException {
        final int totalItems = computeTotalItems(groundedAggregations);
        if (LOG.isDebugEnabled()) {
            LOG.debug("UseVisitedIds=" + useVisitedIds + " useMultipleThreads=" + useMultipleThreads + " totalItems="
                    + totalItems);
        }

        ItemEquivalence equivalence = stitcher.getItemEquivalence(session);
        RelationshipsModelImpl model = new RelationshipsModelImpl();
        model.calculateClassRelationships(groundedAggregations);

        StopWatch watch = new StopWatch();
        watch.start();

        if (useMultipleThreads) {
            int numQueryResults = queryResults.size();
            List<Future<Boolean>> resultSet = new ArrayList<>(numQueryResults);
            for (int index = 0; index < numQueryResults; index++) {
                QueryResult queryResult = queryResults.get(index);
                Set<String> notRelatedToTypes = notRelatedToTypesList.get(index);
                Future<Boolean> future = THREAD_POOL.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call()
                            throws InvocationTargetException, IllegalAccessException, RelationPropertyNotFound {
                        StopWatch w1 = new StopWatch();
                        w1.start();
                        calculateRelationships(queryResult, model, notRelatedToTypes, useVisitedIds, followEquivalence,
                                equivalence, totalItems);
                        w1.stop();
                        queryResult.addTiming("relCalcTime", w1.getTime());
                        return true;
                    }
                });
                resultSet.add(future);
            }

            boolean interrupted = false;
            for (Future<Boolean> future : resultSet) {
                try {
                    Boolean success = future.get();
                    if (success) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Relationship calc success");
                        }
                    } else {
                        LOG.error("Relationship calculation failed ");
                    }
                } catch (InterruptedException e) {
                    interrupted = true;
                } catch (ExecutionException e) {
                    // Should never happen
                    LOG.error("Relationship calculation interrupted", e);
                }
            }
            if (interrupted) {
                LOG.error("Relationship calculation interrupted");
                Thread.currentThread().interrupt();
            }
        } else {
            int numQueryResults = queryResults.size();
            for (int index = 0; index < numQueryResults; index++) {
                StopWatch w1 = null;
                if (timing) {
                    w1 = new StopWatch();
                    w1.start();
                }
                QueryResult queryResult = queryResults.get(index);
                Set<String> notRelatedToTypes = notRelatedToTypesList.get(index);
                calculateRelationships(queryResult, model, notRelatedToTypes, useVisitedIds, followEquivalence,
                        equivalence, totalItems);
                if (timing) {
                    w1.stop();
                    queryResult.addTiming("relCalcTime", w1.getTime());
                }
            }
        }

        watch.stop();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Calculated multiple relations time=" + watch);
        }
    }


    //
    // RelationsReporter for the relationships calculation - conforms to the required signature.
    //

    protected boolean reportRelationToItem(final Item item, String relType, final Set<String> relations,
            final Map<String, Set<String>> relationsTypeMap, final Map<String, List<String>> relationPaths,
            List<String> relationPath) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Report relations for " + item.getLogicalId());
        }
        for (Aggregation aggregation : item.getMemberOf()) {
            if (aggregation.containsAggregations() || !aggregation.isTopLevel()) {
                addRelationToAggregation(relType, relations, relationsTypeMap, aggregation, relationPaths,
                        relationPath);

            } else {
                // This is a non-aggregated DA containing items, so reference ID of item itself
                if (LOG.isTraceEnabled() && relations.contains(item.getLogicalId())) {
                    LOG.trace("Ooops already added item " + item.getLogicalId());
                }

                addToRelations(item.getLogicalId(), relType, relations, relationsTypeMap);
                relationPaths.put(item.getLogicalId(), relationPath);
            }
        }

        return false;
    }

    protected boolean reportEquivalenceRelationToItem(final Item item, String relType, final Set<String> relations,
            final Map<String, Set<String>> relationsTypeMap, final Map<String, List<String>> relationPaths,
            List<String> relationPath) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Report equivalence relations for " + item.getLogicalId());
        }
        for (Aggregation aggregation : item.getMemberOf()) {
            if (aggregation.containsAggregations() || !aggregation.isTopLevel()) {
                addRelationToAggregation(relType, relations, relationsTypeMap, aggregation, relationPaths,
                        relationPath);
            } else {
                // This is a non-aggregated DA containing items, so reference ID of item itself
                if (LOG.isTraceEnabled() && relations.contains(item.getLogicalId())) {
                    LOG.trace("Ooops already added equivalent item " + item.getLogicalId());
                }

                relationPaths.put(item.getLogicalId(), relationPath);
                addToRelations(item.getLogicalId(), relType, relations, relationsTypeMap);
            }
        }
        return false;
    }

    private void addToRelations(final String logicalId, String relType, final Set<String> relations,
            final Map<String, Set<String>> relationsTypeMap) {
        relations.add(logicalId);
        if (relType != null) {
            Set<String> rel = relationsTypeMap.get(relType);
            if (rel == null) {
                rel = new HashSet<>();
                relationsTypeMap.put(relType, rel);
            }
            rel.add(logicalId);
        }
    }

    /*
     * Add a relation to the specified Aggregation. The provided aggregation is assumed to be a
     * member of the memberOf set for a LoomEntity. We can guarantee that the parent of the
     * collection is not null.
     */
    private void addRelationToAggregation(final String relType, final Set<String> relations,
            final Map<String, Set<String>> relationsTypeMap, final Aggregation aggregation,
            final Map<String, List<String>> relationPaths, List<String> path) {
        // Follow links up the tree until we find the 1st level collection, below the root
        // collection of the thread,
        // because it is these aggregations that get listed in the top-level collection representing
        // a thread.
        for (Aggregation firstLevel : aggregation.calcFirstLevelAggregations()) {
            addToRelations(firstLevel.getLogicalId(), relType, relations, relationsTypeMap);
            relationPaths.put(firstLevel.getLogicalId(), path);
        }
    }
}
