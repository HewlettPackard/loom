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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.exceptions.RelationPropertyNotFound;
import com.hp.hpl.loom.manager.stitcher.ItemEquivalence;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.Item;

/**
 * Class to traverse the set of nodes in the relationship graph. The behaviour of the graph
 * traversal can be parameterised. For example, the reporting behaviour for each visited node is
 * determined by the RelationsReporter passed as an argument.
 *
 * NOTE - look at which processor is being used in {@link RelationshipCalculator} (line 61) - it
 * might be the GraphProcessorImpl instead
 *
 */
public class GraphProcessorImplIter implements GraphProcessor {
    private static final Log LOG = LogFactory.getLog(GraphProcessorImplIter.class);

    /**
     * The top item.
     */
    private Item querySourceItem = null;

    /**
     * No-arg constructor.
     */
    public GraphProcessorImplIter() {}

    @Override
    @SuppressWarnings({"checkstyle:parameternumber"})
    public boolean doProcessGraphForItem(final Item item, final Set<String> relations,
            final Map<String, Set<String>> relations2, final Map<String, List<String>> relationPaths,
            final RelationsReporter reporter, final RelationshipsModel model, final Set<String> notRelatedToTypes,
            final Set<String> disallowedTypes, final Set<String> visitedIds,
            // Equivalence parameters
            final boolean followEquivalence, final ItemEquivalence itemEquivalence, // Determine
                                                                                    // equivalence
                                                                                    // relationships
            final RelationsReporter equivalenceReporter, // Report equivalence relationships an
                                                         // equivalence jump
            final Set<String> equivalenceRelations) throws RelationPropertyNotFound {

        querySourceItem = item;

        ConnectedRelationships entityRelationships = getItemRelationships(item, model);
        disallowedTypes.clear();
        // prevents returning to the same top ItemType.
        disallowedTypes.add(item.getTypeId());
        EquivalentInfo equivalentInfo = null;
        if (followEquivalence) {
            equivalentInfo = new EquivalentInfo(itemEquivalence, equivalenceReporter, null, equivalenceRelations);
        }
        Deque<RelationshipCall> stack = new ArrayDeque<>();

        /***
         * Stop Information that will be tested by the ConnectedRelationships in order to stop the
         * graph Traversal. Initialises it for the source of the traversal.
         */
        ConditionalStopInformation stopInformation = new ConditionalStopInformation(querySourceItem);

        stack.push(new RelationshipCall(item, relations, relations2, relationPaths, new ArrayList<String>(), reporter,
                true, model, entityRelationships, null, disallowedTypes, visitedIds, notRelatedToTypes,
                // Equivalence parameters
                equivalentInfo, stopInformation));
        boolean result = false;
        while (!stack.isEmpty()) {
            RelationshipCall call = stack.pollLast();
            result = processItemInGraph(stack, call.item, call.relations, call.relations2, call.relationPaths,
                    call.relationPath, call.reporter, call.top, call.model, call.entityRelationships,
                    call.disallowedRelationship, call.disallowedTypes, call.visitedIds, call.notRelatedToTypes,
                    call.equivalentInfo, call.stopInformation);
        }
        return result;
    }

    // required as PMD think it is un-used but it is at the bottom of this class
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private ConnectedRelationships getItemRelationships(final Fibre item, final RelationshipsModel model) {
        return model.getItemRelationships(item.getClass().getName());
    }

    private void processConnectedToRelations(final Deque<RelationshipCall> stack, final String relationName,
            final ConnectedRelationships schemaRelationships, final Item item, final Set<String> itemDisallowedTypes,
            final Set<String> relations, final Map<String, Set<String>> relations2,
            final Map<String, List<String>> relationPaths, final List<String> relationPath,
            final RelationsReporter reporter, final RelationshipsModel model, final String disallowedRelationship,
            final Set<String> disallowedTypes, final Set<String> visitedIds, final Set<String> notRelatedToTypes,
            final EquivalentInfo equivalentInfo, ConditionalStopInformation stopInformation)
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
            if (schemaRelationships != null && schemaRelationships.evalutateStopCondition(stopInformation)) {
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
            // ConnectedRelationships relatedItemRelationships =
            // schemaRelationships == null ? getItemRelationships(relatedItem, model) :
            // schemaRelationships;
            ConnectedRelationships relatedItemRelationships = getItemRelationships(relatedItem, model);

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
            if (relatedItemRelationships != null && relatedItemRelationships.evalutateStopCondition(stopInformation)) {
                itemDisallowedTypes.add(relatedTypeId);
            }
            checkedDisallowed = true; // No need to check again

            stack.push(new RelationshipCall(relatedItem, relations, relations2, relationPaths, relationPath, reporter,
                    false, model, relatedItemRelationships, relationName, itemDisallowedTypes, visitedIds,
                    notRelatedToTypes, equivalentInfo, stopInformation));

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
    private List<ConnectedToRelationship> filterConnections(final Item item, final List<ConnectedToRelationship> ctrs) {
        List<ConnectedToRelationship> list = new ArrayList<>();
        list.addAll(ctrs);

        for (ConnectedToRelationship connectedToRelationship : ctrs) {
            if (!connectedToRelationship.getRelationName(item.getProviderType()).contains(item.getItemType().getId())) {
                list.remove(connectedToRelationship);
            }
        }

        return list;
    }


    @SuppressWarnings({"checkstyle:methodlength", "checkstyle:parameternumber"})
    private boolean processItemInGraph(final Deque<RelationshipCall> stack, final Item item,
            final Set<String> relations, final Map<String, Set<String>> relations2,
            final Map<String, List<String>> relationPaths, final List<String> relationPath,
            final RelationsReporter reporter, final boolean top, final RelationshipsModel model,
            final ConnectedRelationships entityRelationships, final String disallowedRelationship,
            final Set<String> disallowedTypes, final Set<String> visitedIds, final Set<String> notRelatedToTypes,
            // Equivalence parameters
            final EquivalentInfo equivalentInfo, ConditionalStopInformation stopInformation)
            throws RelationPropertyNotFound {

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

            /***
             * Updates Stop Information that will be tested by the ConnectedRelationships in order
             * to stop the graph Traversal. Gets the information from the previous Item in graph and
             * update information.
             */
            stopInformation = new ConditionalStopInformation(stopInformation, item);

            boolean hasStopConditionHappened = entityRelationships.evalutateStopCondition(stopInformation);

            /*
             * Some user defined stop conditions may require that the traversed item to be not
             * reported. In this case, this flag will be set to false. By default it is always true
             */
            boolean reportItemToReporter = stopInformation.shouldItemBeReported();

            if (reportItemToReporter) {
                if (notRelatedToTypes == null || !notRelatedToTypes.contains(item.getTypeId())) {
                    // We have not been told to ignore these relationships

                    boolean terminateEarly = reporter.report(item, disallowedRelationship, relations, relations2,
                            relationPaths, relationPath);
                    if (terminateEarly) {
                        return terminateEarly;
                    }
                }
            }
            if (hasStopConditionHappened) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Stop condition happened on: " + item.getLogicalId());
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
                /*
                 * Reports items that are in relationship between two items of the same item, if it
                 * comes from the top vertex (source vertex). This means that only items one hop
                 * away will be reported. This item will not, however, be traversed. Which means
                 * that anything. Connected to it is going to be ignored.
                 */

                String sameItemTypeRelationName = connectedToRelationship.getRelationName(item.getProviderType());
                Collection<Item> relatedItems = item.getConnectedItemsWithRelationshipName(sameItemTypeRelationName);

                if (relatedItems != null) {
                    // Reports all items that have the same ItemType.
                    for (Item relatedItem : relatedItems) {
                        reporter.report(relatedItem, sameItemTypeRelationName, relations, relations2, relationPaths,
                                relationPath);
                    }

                    /*
                     * Add to the relationship disallowed list. Note: the ItemType was already added
                     * before start traversing on the top. Thus, it wont visit the same item type
                     * through non same ItemType relationships.
                     */
                    itemDisallowedTypes.add(sameItemTypeRelationName);
                    schemaRelationNames.add(sameItemTypeRelationName);
                }
            } else if (!top && relationshipToSameItemType) {
                /*
                 * Just dont visit that relationship. Relationships to the same type should only be
                 * visited when it is directly connected to the top item. If it is not one hop away,
                 * it should not be visited.
                 */
                String sameItemTypeRelationName = connectedToRelationship.getRelationName(item.getProviderType());

                itemDisallowedTypes.add(sameItemTypeRelationName);
                schemaRelationNames.add(sameItemTypeRelationName);
            } else {
                ctrs = filterConnections(item, ctrs);


                /*
                 * connectedToRelationship2 are relationships in a ItemType Vertex that is connected
                 * to the original vertex inspected in this processItemInGraph invocation. Since
                 * same Itemtypes relationships are only allowed to explore items on hop away, they
                 * are not included in this step.
                 */

                for (ConnectedToRelationship connectedToRelationship2 : ctrs) {
                    String relationName = connectedToRelationship2.getRelationName(item.getProviderType()); // +
                                                                                                            // ":"
                                                                                                            // +
                                                                                                            // connectedToRelationship2.getType();
                    itemDisallowedTypes.add(relationName);
                    schemaRelationNames.add(relationName);
                    processConnectedToRelations(stack, relationName, schemaRelationships, item, itemDisallowedTypes,
                            relations, relations2, relationPaths, relationPath, reporter, model, disallowedRelationship,
                            disallowedTypes, visitedIds, notRelatedToTypes, equivalentInfo, stopInformation);
                }
            }
        }

        // Find the remaining relationships for the item that were not described in the schema
        Set<String> relationNames = new HashSet<String>(item.getConnectedRelationships().keySet());
        relationNames.removeAll(schemaRelationNames);

        // Visit the item's connectedRelationships that aren't captured by the schema
        for (String relationName : relationNames) {
            ConnectedRelationships itemEntityRelationships = getItemRelationships(item, model);
            if (top || !itemEntityRelationships.evalutateStopCondition(stopInformation)) {
                itemDisallowedTypes.add(relationName);
                processConnectedToRelations(stack, relationName, null, item, itemDisallowedTypes, relations, relations2,
                        relationPaths, relationPath, reporter, model, disallowedRelationship, disallowedTypes,
                        visitedIds, notRelatedToTypes, equivalentInfo, stopInformation);
            }
        }

        // Follow equivalence relationships
        if (equivalentInfo != null && equivalentInfo.itemEquivalence != null) {
            Collection<Item> equivalentToItems = equivalentInfo.itemEquivalence.getEquivalentItems(item);
            if (equivalentToItems != null) {
                for (Item equivalentTo : equivalentToItems) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Following equivalence from " + item.getLogicalId() + " to "
                                + equivalentTo.getLogicalId());
                    }
                    if (equivalentInfo.equivalentItem != null
                            && equivalentInfo.equivalentItem.getTypeId().equals(equivalentTo.getTypeId())) {
                        // Don't go back along same route via same equivalence relationship
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("Do not follow same equivalence relation "
                                    + equivalentInfo.equivalentItem.getLogicalId() + " " + item.getLogicalId());
                        }
                        continue;
                    }
                    String relType = item.getItemType().getId() + ":" + equivalentTo.getItemType().getId();
                    if (equivalentInfo.equivalenceReporter != null && top) {
                        // Report the equivalence relationship - "equivalence"
                        boolean terminateEarly = equivalentInfo.equivalenceReporter.report(equivalentTo, relType,
                                equivalentInfo.equivalenceRelations, relations2, relationPaths, relationPath);
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
                    EquivalentInfo nextEquivalentInfo = new EquivalentInfo(equivalentInfo.itemEquivalence,
                            equivalentInfo.equivalenceReporter, item, equivalentInfo.equivalenceRelations);
                    RelationshipCall call = new RelationshipCall(equivalentTo, relations, relations2, relationPaths,
                            relationPath, reporter, false, model, getItemRelationships(equivalentTo, model), relType,
                            itemDisallowedTypes, visitedIds, notRelatedToTypes,
                            // Equivalence parameters
                            nextEquivalentInfo, stopInformation);

                    stack.add(call);

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



}


/**
 * This class handles all the parameters required for the processItemInGraph call.
 *
 * It allows us to maintain a stack internally rather than recursively call the graph processing.
 *
 */
class RelationshipCall {
    public final Item item;
    public final Set<String> relations;
    public final Map<String, Set<String>> relations2;
    public final Map<String, List<String>> relationPaths;
    public final RelationsReporter reporter;

    public final boolean top;
    public final RelationshipsModel model;
    public final ConnectedRelationships entityRelationships;
    public final String disallowedRelationship;
    public final Set<String> disallowedTypes;
    public final Set<String> visitedIds;
    public final Set<String> notRelatedToTypes;
    // Equivalence parameters
    public final EquivalentInfo equivalentInfo;

    // path to this entity
    public final List<String> relationPath = new ArrayList<>();

    // All possible stop information that may be relevant when deciding to stop traversing on that
    // entity.
    public final ConditionalStopInformation stopInformation;


    public RelationshipCall(final Item item, final Set<String> relations, final Map<String, Set<String>> relations2,
            final Map<String, List<String>> relationPaths, final List<String> relationPath,
            final RelationsReporter reporter,

            final boolean top, final RelationshipsModel model, final ConnectedRelationships entityRelationships,
            final String disallowedRelationship, final Set<String> disallowedTypes, final Set<String> visitedIds,
            final Set<String> notRelatedToTypes,
            // Equivalence parameters
            final EquivalentInfo equivalentInfo, final ConditionalStopInformation stopInformation) {

        // Equivalence parameters
        // final EquivalenceDetails equivalenceDetails) {
        this.relationPaths = relationPaths;
        this.relationPath.addAll(relationPath);
        if (disallowedRelationship != null) {
            this.relationPath.add(disallowedRelationship);
        }

        this.item = item;
        this.relations = relations;
        this.relations2 = relations2;
        this.reporter = reporter;
        this.top = top;
        this.model = model;
        this.entityRelationships = entityRelationships;
        this.disallowedRelationship = disallowedRelationship;
        this.disallowedTypes = disallowedTypes;
        this.visitedIds = visitedIds;
        this.notRelatedToTypes = notRelatedToTypes;
        // this.equivalenceDetails = equivalenceDetails;
        this.equivalentInfo = equivalentInfo;
        this.stopInformation = stopInformation;
    }
}


/**
 * This class handles all the parameters required for the equivalence checking within
 * processItemInGraph call.
 *
 */
class EquivalentInfo {
    public final ItemEquivalence itemEquivalence;
    public final RelationsReporter equivalenceReporter;
    public final Item equivalentItem;
    public final Set<String> equivalenceRelations;

    public EquivalentInfo(final ItemEquivalence itemEquivalence, // Determine
                                                                 // equivalence
                                                                 // relationships
            final RelationsReporter equivalenceReporter, // Report equivalence relationships
            final Item equivalentItem, // Set to a equivalent item if we were called as a result of
                                       // an equivalence jump
            final Set<String> equivalenceRelations) {
        this.itemEquivalence = itemEquivalence;
        this.equivalenceReporter = equivalenceReporter;
        this.equivalentItem = equivalentItem;
        this.equivalenceRelations = equivalenceRelations;

    }
}
