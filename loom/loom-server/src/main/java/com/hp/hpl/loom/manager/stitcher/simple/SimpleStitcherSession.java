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
package com.hp.hpl.loom.manager.stitcher.simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.manager.stitcher.ItemEquivalence;
import com.hp.hpl.loom.manager.stitcher.StitcherUpdater;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.stitcher.extras.Verifier;

public class SimpleStitcherSession implements ItemEquivalence, StitcherUpdater {

    // CLASSES -------------------------------------------------------------------------------------

    private class ItemIndexer {

        /*
         * Index of all items known, indexed by typeId and providerId.
         * 
         * Key of first map is typeId of Items, eg "cmu-node", containing all Items for Items of
         * that type.
         * 
         * Second key is providerId of the source of items.
         */
        private Map<String, Map<String, Collection<Item>>> itemIndex = new HashMap<>();

        Collection<Item> getAllItemsByTypeId(final String typeId) {
            Map<String, Collection<Item>> itemsForTypeId = itemIndex.get(typeId);
            if (itemsForTypeId == null) {
                return Collections.emptyList(); // Immutable
            }
            Collection<Item> allItems = new HashSet<Item>();
            for (Collection<Item> coll : itemsForTypeId.values()) {
                allItems.addAll(coll);
            }
            return allItems;
        }

        Collection<Item> getItemsByTypeIdAndProviderId(final String typeId, final String providerId) {
            Map<String, Collection<Item>> itemsForTypeId = itemIndex.get(typeId);
            if (itemsForTypeId == null) {
                return Collections.emptyList(); // Immutable
            }
            Collection<Item> items = itemsForTypeId.get(providerId);
            if (items == null) {
                return Collections.emptyList(); // Immutable
            }
            return items;
        }

        void indexItemsByTypeIdAndProviderId(final String typeId, final String providerId,
                final Collection<Item> items) {
            Map<String, Collection<Item>> itemsForTypeId = itemIndex.get(typeId);
            if (itemsForTypeId == null) {
                itemsForTypeId = new HashMap<>();
                itemIndex.put(typeId, itemsForTypeId);
            }
            Collection<Item> allItemsForProvider = itemsForTypeId.get(providerId);
            if (allItemsForProvider == null) {
                allItemsForProvider = new ArrayList<Item>(items.size());
                itemsForTypeId.put(providerId, allItemsForProvider);
            }
            allItemsForProvider.addAll(items);
        }

        Collection<Item> removeAllItemsForTypeIdAndProviderId(final String typeId, final String providerId) {
            Map<String, Collection<Item>> itemsForTypeId = itemIndex.get(typeId);
            if (itemsForTypeId == null) {
                return Collections.emptyList(); // Immutable
            }
            Collection<Item> allItemsForProvider = itemsForTypeId.get(providerId);
            if (allItemsForProvider == null) {
                return Collections.emptyList(); // Immutable
            }
            itemsForTypeId.remove(providerId);
            if (itemsForTypeId.isEmpty()) {
                itemIndex.remove(typeId);
            }
            return allItemsForProvider;
        }

        Collection<Item> removeItemsByTypeIdAndProviderId(final String typeId, final String providerId,
                final Collection<Item> items) {
            Map<String, Collection<Item>> itemsForTypeId = itemIndex.get(typeId);
            if (itemsForTypeId == null) {
                return Collections.emptyList(); // Immutable
            }
            Collection<Item> allItemsForProvider = itemsForTypeId.get(providerId);
            if (allItemsForProvider == null) {
                return Collections.emptyList(); // Immutable
            }
            Collection<Item> actuallyRemoved = new ArrayList<Item>(items.size());
            for (Item item : items) {
                if (allItemsForProvider.remove(item)) {
                    actuallyRemoved.add(item);
                }
            }
            return actuallyRemoved;
        }
    }

    // CLASSES - END -------------------------------------------------------------------------------

    // VARIABLES -----------------------------------------------------------------------------------

    private static final Log LOG = LogFactory.getLog(SimpleStitcherSession.class);

    // private Session session;

    /*
     * Index of all items known to stitcher, indexed by typeId.
     * 
     * Key of first map is typeId of Items, eg "cmu-node", containing all Items for Items of that
     * type.
     * 
     * Second key is providerId of the source of items.
     */
    protected ItemIndexer itemIndex;

    /*
     * Map of LogicalId of an Item to all tacks that apply to that Item.
     */
    protected Map<String, Map<String, Item>> tacks;

    /*
     * Key of first map is typeId of "origin" of StitcherRule id, eg "cmu-node", containing a Map of
     * all indexed values for Items of that type.
     * 
     * Key of second map contains all indexable keys (typically an attribute name) for items of that
     * typeId, e.g. "name", containing a Map of all Items indexed by the value of that attribute.
     * 
     * Key of third map id the value of attribute, eg "foo.hp.com", containing the Item itself.
     */
    protected Map<String, Map<String, Map<String, Item>>> itemAttributeIndex; // TODO: Review. Not
                                                                              // yet used.
    protected SimpleStitcherRuleManagerImpl ruleManager;

    // VARIABLES - END -----------------------------------------------------------------------------

    // CONSTRUCTORS --------------------------------------------------------------------------------

    SimpleStitcherSession(final SimpleStitcherRuleManagerImpl ruleManager) {
        // this.session = session;
        this.itemIndex = new ItemIndexer();
        this.tacks = new HashMap<String, Map<String, Item>>();
        this.ruleManager = ruleManager;
    }

    // CONSTRUCTORS - END --------------------------------------------------------------------------

    // METHODS -------------------------------------------------------------------------------------

    @Override
    public Collection<Item> stitchItems(final String typeId, final String providerId, final Collection<Item> allItems,
            final Collection<Item> newItems, final Collection<Item> updatedItems, final Collection<Item> deletedItems) {

        // Verify parameters are not null. If any parameter is null, this method should stop here
        Verifier.illegalArgumentIfAnyNull(
                Arrays.asList(typeId, providerId, allItems, newItems, updatedItems, deletedItems));

        Map<String, Item> modifiedItems = new HashMap<String, Item>();
        Map<Item, Collection<Item>> newStitches = new HashMap<Item, Collection<Item>>(allItems.size());

        // Time measurement for debugging and performance tests - Start
        StopWatch watch = new StopWatch();
        watch.start();

        // Retrieve collection of microstitchers (they will be processed independently)
        Map<String, LoomMicroStitcher> microStitchersByDestTypeId = ruleManager.getMicroStitchersByTypeId(typeId);

        if (microStitchersByDestTypeId.isEmpty()) {
            // If there are no microstichers for this type ID, no stitches will be accumulated.
            // Initialize <newStitches> manually
            for (Item current : allItems) {
                newStitches.put(current, new ArrayList<Item>());
            }
        } else {
            // Stitch for each microstitcher and accumulate stitches
            for (String destTypeId : microStitchersByDestTypeId.keySet()) {
                // Load <allItems> as base elements in the microstitcher
                // Load all other items by type ID as candidate elements in the microstitcher
                LoomMicroStitcher microStitcher = microStitchersByDestTypeId.get(destTypeId);
                microStitcher.clearBaseElements(); // Resetting microstitcher (1/2)
                microStitcher.clearCandidateElements(); // Resetting microstitcher (2/2)
                microStitcher.addBaseElements(union(newItems, updatedItems));
                microStitcher.addCandidateElements(itemIndex.getAllItemsByTypeId(destTypeId));

                // Stitch!
                accumulateStitches(newStitches, microStitcher.stitch());
            }
        }

        // Collect those elements that have been modified
        modifiedItems.putAll(calculateModifiedItems(newItems, updatedItems, deletedItems, tacks, newStitches, false));

        // Remove stitches involving <deletedItems> and <updatedItems> from <tacks>
        removeInvolvedStitches(union(deletedItems, updatedItems), tacks);

        // Add new stitches to <tacks> (Step 1).
        // If the item is new, it has no associated entry in the stitches map
        // If the item is updated, its previous entry has been removed
        // In any case, a new entry needs to be created
        for (Item stitchedItem : newStitches.keySet()) {
            tacks.put(stitchedItem.getLogicalId(), new HashMap<String, Item>());
        }

        // Add new stitches to <tacks> (Step 2).
        // Now that the corresponding entries exist in the stitches map, add their stitches
        for (Item sourceItem : newStitches.keySet()) {
            Collection<Item> destItems = newStitches.get(sourceItem);

            // Add source-to-destination stitches
            for (Item destItem : destItems) {
                tacks.get(sourceItem.getLogicalId()).put(destItem.getLogicalId(), destItem);
            }

            // Add destination-to-source stitches
            for (Item destItem : destItems) {
                tacks.get(destItem.getLogicalId()).put(sourceItem.getLogicalId(), sourceItem);
            }
        }

        // Time measurement for debugging and performance tests - End
        watch.stop();
        LOG.info("Stitching for source type ID  " + typeId + " and provider ID " + providerId + ": time=" + watch);

        // Update item index: remove <deletedItems>, add <newItems>
        itemIndex.removeItemsByTypeIdAndProviderId(typeId, providerId, deletedItems);
        itemIndex.indexItemsByTypeIdAndProviderId(typeId, providerId, newItems);

        // Return modified elements
        return modifiedItems.values();
    }

    @Override
    public void removeStitchedItems(String typeId, String providerId) {

        // Retrieve removed items and delete them from index
        Collection<Item> removedItems;
        removedItems = itemIndex.removeAllItemsForTypeIdAndProviderId(typeId, providerId);

        // Remove involved stitches from <tacks>
        removeInvolvedStitches(removedItems, tacks);
    }

    @Override
    public Collection<Item> getEquivalentItems(final Item item) {

        // Given a specific item, its equivalent items can be found as stitches in <tacks>
        Map<String, Item> candidateMap = tacks.get(item.getLogicalId());
        return (candidateMap != null ? candidateMap.values() : null);
    }

    private static void accumulateStitches(Map<Item, Collection<Item>> accumulator,
            Map<Item, Collection<Item>> toBeAdded) {
        Collection<Item> collectionInAcc;

        for (Item current : toBeAdded.keySet()) {
            collectionInAcc = accumulator.get(current);
            if (collectionInAcc == null) {
                collectionInAcc = new ArrayList<Item>(); // Warning! Repeated elements?
                // collectionInAcc = new HashSet<Item>(); // This one should be better. TODO: Review
                accumulator.put(current, collectionInAcc);
            }
            collectionInAcc.addAll(toBeAdded.get(current));
        }
    }

    private static Map<String, Item> calculateModifiedItems(final Collection<Item> newItems,
            final Collection<Item> updatedItems, final Collection<Item> deletedItems,
            Map<String, Map<String, Item>> oldStitches, Map<Item, Collection<Item>> newStitches,
            boolean includeSourceItems) {

        Map<String, Item> modifiedItems;
        modifiedItems = new HashMap<String, Item>();

        // 1 - <newItems> to be considered modified
        if (includeSourceItems) {
            for (Item current : newItems) {
                modifiedItems.put(current.getLogicalId(), current);
            }
        }

        // 2 - <deletedItems> to be considered modified
        if (includeSourceItems) {
            for (Item current : deletedItems) {
                modifiedItems.put(current.getLogicalId(), current);
            }
        }

        // 3 - Items stitched from <newItems> -> modified (new stitches)
        for (Item current : newItems) {
            for (Item stitchedTo : newStitches.get(current)) {
                modifiedItems.put(stitchedTo.getLogicalId(), stitchedTo);
            }
        }

        // 4 - Items stitched from <deletedItems> -> modified (deleted stitches)
        for (Item current : deletedItems) {
            modifiedItems.putAll(oldStitches.get(current.getLogicalId()));
        }

        // 5 - For each <updatedItem> : Compare previous stitches against new ones
        for (Item current : updatedItems) {
            Map<String, Item> previouslyStitchedTo, nowStitchedTo;
            // Get previous stitches for current item (as Map)
            previouslyStitchedTo = oldStitches.get(current.getLogicalId());
            // Get newly calculated stitches for current item (as Map)
            nowStitchedTo = new HashMap<String, Item>(newStitches.size());
            newStitches.get(current).stream().forEach((Item i) -> nowStitchedTo.put(i.getLogicalId(), i));

            // If the previous stitches and the new ones (regarded as sets) are not the same
            // (symmetric difference NOT empty), there have been changes. Then:
            Collection<String> changedItems = symDifference(previouslyStitchedTo.keySet(), nowStitchedTo.keySet());
            if (!changedItems.isEmpty()) {
                // Consider this <updatedItem> as modified
                if (includeSourceItems) {
                    modifiedItems.put(current.getLogicalId(), current);
                }
                // Consider elements NOT belonging to the intersection of this two sets as modified
                for (String changedItemId : changedItems) {
                    Item changedItem = previouslyStitchedTo.get(changedItemId);
                    if (changedItem == null) {
                        changedItem = nowStitchedTo.get(changedItemId);
                    }
                    if (changedItem == null) {
                        // Unexpected situation! How to proceed? Exception? Do nothing? TODO
                    }
                    modifiedItems.put(changedItemId, changedItem);
                }
            }
        }

        return modifiedItems;
    }

    private static void removeInvolvedStitches(Collection<Item> itemsInvolved,
            Map<String, Map<String, Item>> stitches) {
        // TODO NOTE: This approach is correct if the stitches are symmetric!
        // Should be modified if they are not. In such case, all the entries in the map should be
        // checked.
        for (Item toBeRemoved : itemsInvolved) {
            String toBeRemovedId = toBeRemoved.getLogicalId();
            Map<String, Item> currentStitches = stitches.get(toBeRemovedId);
            if (currentStitches != null) {
                for (String stitchedToId : currentStitches.keySet()) {
                    stitches.get(stitchedToId).remove(toBeRemovedId);
                }
                stitches.remove(toBeRemovedId);
            }
        }
    }

    // TODO: Export this method to an external class?
    private static <T> Collection<T> union(Collection<T> setA, Collection<T> setB) {
        Set<T> tmp = new HashSet<T>(setA);
        tmp.addAll(setB);
        return tmp;
    }

    // TODO: Export this method to an external class?
    private static <T> Collection<T> intersection(Collection<T> setA, Collection<T> setB) {
        Set<T> tmp = new HashSet<T>();
        for (T x : setA)
            if (setB.contains(x))
                tmp.add(x);
        return tmp;
    }

    // TODO: Export this method to an external class?
    private static <T> Collection<T> difference(Collection<T> setA, Collection<T> setB) {
        Set<T> tmp = new HashSet<T>(setA);
        tmp.removeAll(setB);
        return tmp;
    }

    // TODO: Export this method to an external class?
    private static <T> Collection<T> symDifference(Collection<T> setA, Collection<T> setB) {
        Collection<T> tmpA;
        Collection<T> tmpB;

        tmpA = union(setA, setB);
        tmpB = intersection(setA, setB);
        return difference(tmpA, tmpB);
    }

    // METHODS - END -------------------------------------------------------------------------------

}
