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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.annotations.ManyToMany;
import com.hp.hpl.loom.adapter.annotations.ManyToOne;
import com.hp.hpl.loom.adapter.annotations.OneToMany;
import com.hp.hpl.loom.exceptions.RelationPropertyNotFound;
import com.hp.hpl.loom.manager.stitcher.ItemEquivalence;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.Item;

/**
 * Class to traverse the set of nodes in the relationship graph. The behaviour of the graph
 * traversal can be parameterised. For example, the reporting behaviour for each visited node is
 * determined by the RelationsReporter passed as an argument.
 * 
 * NOTE - look at which processor is being used in RelationshipCalculator (line 61) - it might be
 * the GraphProcessorImplIter instead
 * 
 */
@Deprecated
public class GraphProcessorImpl implements GraphProcessor {
    private static final Log LOG = LogFactory.getLog(GraphProcessorImpl.class);

    /**
     * No-arg constructor.
     */
    public GraphProcessorImpl() {}

    // required as PMD think it is un-used but it is at the bottom of this class
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private ConnectedRelationships getItemRelationships(final Fibre item, final RelationshipsModel model) {
        return model.getItemRelationships(item.getClass().getName());
    }

    private void processConnectedToRelations(final String relationName,
            final ConnectedRelationships schemaRelationships, final Item item, final Set<String> itemDisallowedTypes,
            final Set<String> relations, final Map<String, Set<String>> relations2, final RelationsReporter reporter,
            final RelationshipsModel model, final String disallowedRelationship, final Set<String> disallowedTypes,
            final Set<String> visitedIds, final Set<String> notRelatedToTypes, final boolean followEquivalence,
            final ItemEquivalence itemEquivalence, final Set<String> equivalenceRelations)
            throws RelationPropertyNotFound {

        if (disallowedRelationship != null && disallowedRelationship.equals(relationName)) {
            // Don't go back along same route via same relationship
            if (LOG.isTraceEnabled()) {
                LOG.trace("Do not follow same relation " + relationName + " " + item.getLogicalId());
            }
            return;
        }

        Collection<Item> relatedItems = item.getConnectedItemsWithRelationshipName(relationName);
        if (relatedItems == null || relatedItems.isEmpty()) {
            // if the item type is root than make sure we don't revisit (even if there aren't any
            // items)
            if (schemaRelationships != null && schemaRelationships.isRoot()) {
                // we have to build the item type from the start of the item logical id and the
                // ItemTypeInfo - eg: foreman (adapter name) + plus "-" and host (from schema
                // definition)
                if (item != null && item.getLogicalId().indexOf("/") != -1) {
                    disallowedTypes.add(item.getLogicalId().substring(0, item.getLogicalId().indexOf("/")) + "-"
                            + schemaRelationships.getItemTypeInfo().value());
                } else {
                    disallowedTypes.add(schemaRelationships.getItemTypeInfo().value());
                }
            }
            return;
        }

        boolean disallowed = false;
        boolean checkedDisallowed = false;
        String relatedTypeId = null;
        for (Item relatedItem : relatedItems) {
            // Either we
            ConnectedRelationships relatedItemRelationships =
                    schemaRelationships == null ? getItemRelationships(relatedItem, model) : schemaRelationships;

            relatedTypeId = relatedItem.getTypeId();
            if (!checkedDisallowed
                    && (disallowedTypes.contains(relationName) || disallowedTypes.contains(relatedTypeId))) {
                // Don't go back into types have already visited
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Item " + item.getLogicalId() + " type " + item.getTypeId() + " skipped type "
                            + relatedTypeId);
                }
                disallowed = true;
                break;
            }

            // if the item is root than make sure we don't revisit
            if (relatedItemRelationships.isRoot()) {
                itemDisallowedTypes.add(relatedTypeId);
            }
            checkedDisallowed = true; // No need to check again
            processItemInGraph(relatedItem, relations, relations2, reporter, false, model, relatedItemRelationships,
                    relationName, itemDisallowedTypes, visitedIds, notRelatedToTypes,
                    // Equivalence parameters
                    followEquivalence, itemEquivalence, null, null, equivalenceRelations);
        }
        if (!disallowed) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("ConnectedTo add disallowed type " + item.getLogicalId() + " type " + relatedTypeId);
            }
            itemDisallowedTypes.add(relationName);
        }
    }

    /**
     * Returns a list of connections that only directly relate to this item
     * 
     * @param item
     * @param ctrs
     * @return
     */
    private List<ConnectedToRelationship> filterConnections(Item item, List<ConnectedToRelationship> ctrs) {
        List<ConnectedToRelationship> list = new ArrayList<>();
        list.addAll(ctrs);

        for (ConnectedToRelationship connectedToRelationship : ctrs) {
            if (!connectedToRelationship.getRelationName(item.getProviderType())
                    .contains(item.getItemType().getLocalId())) {
                list.remove(connectedToRelationship);
            }
        }

        return list;
    }


    @SuppressWarnings({"checkstyle:methodlength", "checkstyle:parameternumber"})
    private boolean processItemInGraph(final Item item, final Set<String> relations,
            final Map<String, Set<String>> relations2, final RelationsReporter reporter, final boolean top,
            final RelationshipsModel model, final ConnectedRelationships entityRelationships,
            final String disallowedRelationship, final Set<String> disallowedTypes, final Set<String> visitedIds,
            final Set<String> notRelatedToTypes,
            // Equivalence parameters
            final boolean followEquivalence, final ItemEquivalence itemEquivalence, // Determine
                                                                                    // equivalence
                                                                                    // relationships
            final RelationsReporter equivalenceReporter, // Report equivalence relationships
            final Item equivalentItem, // Set to a equivalent item if we were called as a result of
                                       // an equivalence jump
            final Set<String> equivalenceRelations) throws RelationPropertyNotFound {

        if (visitedIds != null) {
            if (visitedIds.contains(item.getLogicalId())) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Already visited " + item.getLogicalId());
                }
                return false;
            }
            visitedIds.add(item.getLogicalId());
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("Add relations for " + item.getLogicalId());
        }

        if (entityRelationships == null) {
            // nothing to be done here
            LOG.error("EntityRelationships was null for item " + item.getLogicalId() + " " + item.getClass().getName());
            return false;
        }

        if (!top) {
            if (notRelatedToTypes == null || !notRelatedToTypes.contains(item.getTypeId())) {
                // We have not been told to ignore these relationships
                boolean terminateEarly =
                        reporter.report(item, disallowedRelationship, relations, relations2, null, null);
                if (terminateEarly) {
                    return terminateEarly;
                }
            }
            if (entityRelationships.isRoot()) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Do not follow root " + item.getLogicalId());
                }
                return false;
            }
        }

        String itemTypeId = item.getTypeId();

        Set<String> itemDisallowedTypes = new HashSet<String>(disallowedTypes);
        itemDisallowedTypes.add(itemTypeId);
        // Follow the ConnectedTo relationships that are described in the schema
        List<String> schemaRelationNames = new ArrayList<>();

        for (ConnectedToRelationship connectedToRelationship : entityRelationships.getConnectedTos()) {
            ConnectedRelationships schemaRelationships = connectedToRelationship.getToRelationships();

            List<ConnectedToRelationship> ctrs = schemaRelationships.getConnectedTos();

            // Check if this relationship is is connecting two items of the same ItemType.
            boolean relationshipToSameItemType = connectedToRelationship.getRelationName(item.getProviderType())
                    .contains(item.getItemType().getLocalId() + ":" + item.getItemType().getLocalId()) ? true : false;

            if (top && relationshipToSameItemType) {
                // Reports items that are in relationship between two items of the same item, if it
                // comes from the top vertex (source vertex).
                // This means that only items one hop away will be reported. This item will not,
                // however, be traversed. Which means that anything
                // Connected to it is going to be ignored.

                String sameItemTypeRelationName = connectedToRelationship.getRelationName(item.getProviderType());
                Collection<Item> relatedItems = item.getConnectedItemsWithRelationshipName(sameItemTypeRelationName);

                if (relatedItems != null) {
                    // Reports all items that have the same ItemType.
                    for (Item relatedItem : relatedItems) {
                        reporter.report(relatedItem, sameItemTypeRelationName, relations, relations2, null, null);
                    }

                    // Add to the relationship disallowed list. Note: the ItemType was already added
                    // before start traversing on the top.
                    // Thus, it wont visit the same item type through non same ItemType
                    // relationships.
                    itemDisallowedTypes.add(sameItemTypeRelationName);
                    schemaRelationNames.add(sameItemTypeRelationName);
                }
            } else if (!top && relationshipToSameItemType) {
                // Just dont visit that relationship. Relationships to the same type should only be
                // visited when it is directly connected to the top item.
                // If it is not one hop away, it should not be visited.
                String sameItemTypeRelationName = connectedToRelationship.getRelationName(item.getProviderType());

                itemDisallowedTypes.add(sameItemTypeRelationName);
                schemaRelationNames.add(sameItemTypeRelationName);
            } else {
                ctrs = filterConnections(item, ctrs);


                // connectedToRelationship2 are relationships in a ItemType Vertex that is connected
                // to the original vertex inspected in
                // this processItemInGraph invocation. Since same Itemtypes relationships are only
                // allowed to explore items on hop away,
                // they are not included in this step.

                for (ConnectedToRelationship connectedToRelationship2 : ctrs) {
                    String relationName = connectedToRelationship2.getRelationName(item.getProviderType()); // +
                                                                                                            // ":"
                                                                                                            // +
                                                                                                            // connectedToRelationship2.getType();
                    itemDisallowedTypes.add(relationName);
                    schemaRelationNames.add(relationName);
                    processConnectedToRelations(relationName, schemaRelationships, item, itemDisallowedTypes, relations,
                            relations2, reporter, model, disallowedRelationship, disallowedTypes, visitedIds,
                            notRelatedToTypes, followEquivalence, itemEquivalence, equivalenceRelations);
                }
            }
        }

        // Find the remaining relationships for the item that were not described in the schema
        Set<String> relationNames = new HashSet<String>(item.getConnectedRelationships().keySet());
        relationNames.removeAll(schemaRelationNames);

        // Visit the item's connectedRelationships that aren't captured by the schema
        for (String relationName : relationNames) {
            itemDisallowedTypes.add(relationName);
            processConnectedToRelations(relationName, null, item, itemDisallowedTypes, relations, relations2, reporter,
                    model, disallowedRelationship, disallowedTypes, visitedIds, notRelatedToTypes, followEquivalence,
                    itemEquivalence, equivalenceRelations);
        }

        // Follow equivalence relationships
        if (followEquivalence && itemEquivalence != null) {
            Collection<Item> equivalentToItems = itemEquivalence.getEquivalentItems(item);
            if (equivalentToItems != null) {
                for (Item equivalentTo : equivalentToItems) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Following equivalence from " + item.getLogicalId() + " to "
                                + equivalentTo.getLogicalId());
                    }
                    if (equivalentItem != null && equivalentItem.getTypeId().equals(equivalentTo.getTypeId())) {
                        // Don't go back along same route via same equivalence relationship
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("Do not follow same equivalence relation " + equivalentItem.getLogicalId() + " "
                                    + item.getLogicalId());
                        }
                        continue;
                    }

                    if (equivalenceReporter != null) {
                        // Report the equivalence relationship
                        boolean terminateEarly = equivalenceReporter.report(equivalentTo, "equivalence",
                                equivalenceRelations, relations2, null, null);
                        if (terminateEarly) {
                            return terminateEarly;
                        }
                    }

                    boolean disallowed = false;
                    String relatedTypeId = equivalentTo.getTypeId();
                    if (disallowedTypes.contains(relatedTypeId)) {
                        // Don't go back into types have already visited
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("Item " + item.getLogicalId() + " type " + item.getTypeId()
                                    + " skipped equivalent type " + relatedTypeId);
                        }
                        disallowed = true;
                        break;
                    }

                    processItemInGraph(equivalentTo, relations, relations2, reporter, false, model,
                            getItemRelationships(equivalentTo, model), null, itemDisallowedTypes, visitedIds,
                            notRelatedToTypes,
                            // Equivalence parameters
                            followEquivalence, itemEquivalence, null, item, equivalenceRelations);

                    if (!disallowed) {
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("ConnectedTo add disallowed type " + item.getLogicalId() + " type "
                                    + relatedTypeId);
                        }
                        itemDisallowedTypes.add(relatedTypeId);
                    }
                }
            }
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("Exit add relations for " + item.getLogicalId());
        }

        return false;
    }


    @SuppressWarnings({"checkstyle:parameternumber"})
    public boolean doProcessGraphForItem(final Item item, final Set<String> relations,
            final Map<String, Set<String>> relations2, final Map<String, List<String>> rp,
            final RelationsReporter reporter, final RelationshipsModel model, final Set<String> notRelatedToTypes,
            final Set<String> disallowedTypes, final Set<String> visitedIds,
            // Equivalence parameters
            final boolean followEquivalence, final ItemEquivalence itemEquivalence, // Determine
                                                                                    // equivalence
                                                                                    // relationships
            final RelationsReporter equivalenceReporter, // Report equivalence relationships an
                                                         // equivalence jump
            final Set<String> equivalenceRelations) throws RelationPropertyNotFound {

        ConnectedRelationships entityRelationships = getItemRelationships(item, model);
        disallowedTypes.clear();

        // prevents returning to the same top ItemType.
        disallowedTypes.add(item.getTypeId());
        return processItemInGraph(item, relations, relations2, reporter, true, model, entityRelationships, null,
                disallowedTypes, visitedIds, notRelatedToTypes,
                // Equivalence parameters
                followEquivalence, itemEquivalence, equivalenceReporter, null, equivalenceRelations);
    }

}
