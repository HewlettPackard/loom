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
package com.hp.hpl.loom.manager.stitcher.test;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.manager.stitcher.ItemEquivalence;
import com.hp.hpl.loom.manager.stitcher.StitcherUpdater;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.stitcher.IndexableStitcherRule;
import com.hp.hpl.loom.stitcher.StitcherRule;

/**
 * Initial implementation of the Stitcher interfaces for Item equivalence and Stitcher updating.
 */
class TestStitcherSession implements ItemEquivalence, StitcherUpdater {

    private static final Log LOG = LogFactory.getLog(TestStitcherSession.class);

    private TestStitcherRuleManager ruleManager;
    private boolean allowStitching;
    private boolean allowIndexing;

    TestStitcherSession(final TestStitcherRuleManager ruleManager, final boolean allowStitching,
            final boolean allowIndexing) {
        this.ruleManager = ruleManager;
        this.allowStitching = allowStitching;
        this.allowIndexing = allowIndexing;
    }

    /*
     * Keeps track of all stitches between items.
     */
    private class ItemTacker {
        /*
         * Map of LogicalId of an Item to all tacks that apply to that Item.
         */
        private Map<String, Map<String, Item>> tacksIndex = new HashMap<>();

        /*
         * Return all items that have been stitched to the specified item.
         */
        Collection<Item> getTackedItems(final Item from) {
            Map<String, Item> tacksForFromItem = tacksIndex.get(from.getLogicalId());
            if (tacksForFromItem == null) {
                return null;
            }
            return tacksForFromItem.values();
        }

        /*
         * Create a stitch between the two specified items.
         */
        void tackItems(final Item left, final Item right) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("tackItems " + left + " to " + right);
            }
            tackItem(left, right);
            tackItem(right, left);
        }

        private void tackItem(final Item from, final Item to) {
            Map<String, Item> tacksForFromItem = tacksIndex.get(from.getLogicalId());
            if (tacksForFromItem == null) {
                tacksForFromItem = new HashMap<>();
                tacksIndex.put(from.getLogicalId(), tacksForFromItem);
            }
            tacksForFromItem.put(to.getLogicalId(), to);
        }

        /**
         * Completely remove all tacks from the specified Item, returning all items that were
         * previously stitched to it in a map.
         *
         * @param item Item to be unstitched.
         * @return A map containing any removed Items, keyed by logicalID of the Items.
         */
        Map<String, Item> removeItem(final Item item) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Remove all stitches for item " + item.getLogicalId());
            }
            String itemLogicalId = item.getLogicalId();
            Map<String, Item> tacksForItem = tacksIndex.get(itemLogicalId);
            if (tacksForItem == null) {
                return null;
            }
            Map<String, Item> itemsRemovedFromThisItem = new HashMap<>();
            for (Item otherItem : tacksForItem.values()) {
                String otherLogicalId = otherItem.getLogicalId();
                itemsRemovedFromThisItem.put(otherLogicalId, otherItem);
                Map<String, Item> tacksForOtherItem = tacksIndex.get(otherLogicalId);
                tacksForOtherItem.remove(itemLogicalId);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Remove stitch from " + itemLogicalId + " to " + otherLogicalId);
                }
                if (tacksForOtherItem.isEmpty()) {
                    tacksIndex.remove(otherLogicalId);
                }
            }
            tacksIndex.remove(itemLogicalId);
            return itemsRemovedFromThisItem;
        }

        /**
         * Remove tacks from the specified Item to Items that have the specified type, returning all
         * items that were previously stitched to it in a map.
         *
         * @param item Item to be unstitched.
         * @param otherTypeId Type of items to be unstitched.
         * @return A map containing any removed Items, keyed by logicalID of the Items.
         */
        Map<String, Item> removeTacksForTypeId(final Item item, final String otherTypeId) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Clear all stitches for item " + item.getLogicalId() + " of type " + otherTypeId);
            }
            String itemLogicalId = item.getLogicalId();
            Map<String, Item> tacksForItem = tacksIndex.get(itemLogicalId);
            if (tacksForItem == null) {
                return null;
            }
            Map<String, Item> itemsRemovedFromThisItem = new HashMap<>();
            // Remove other side of tacks for items matching the specified type
            for (Item otherItem : tacksForItem.values()) {
                if (!otherItem.getTypeId().equals(otherTypeId)) {
                    continue;
                }
                String otherLogicalId = otherItem.getLogicalId();
                itemsRemovedFromThisItem.put(otherLogicalId, otherItem);
                Map<String, Item> tacksForOtherItem = tacksIndex.get(otherLogicalId);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Remove matching stitch from " + itemLogicalId + " to other " + otherLogicalId);
                }
                tacksForOtherItem.remove(itemLogicalId);
                if (tacksForOtherItem.isEmpty()) {
                    tacksIndex.remove(otherLogicalId);
                }
            }
            // Now remove logical id's on this items for other items that matched the specified type
            for (String otherLogicalId : itemsRemovedFromThisItem.keySet()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Remove matching stitch from " + itemLogicalId + " to other " + otherLogicalId);
                }
                tacksForItem.remove(otherLogicalId);
            }
            if (tacksForItem.isEmpty()) {
                tacksIndex.remove(itemLogicalId);
            }
            return itemsRemovedFromThisItem;
        }
    }

    /*
     * Index of all collections of Items known to stitcher, indexed by typeId and providerId.
     */
    private class ItemIndexer {
        /*
         * Key of first map is typeId of Items, eg "cmu-node", containing all Items for Items of
         * that type.
         * 
         * Second key is providerId of the source of items.
         */
        private Map<String, Map<String, Collection<Item>>> itemIndex = new HashMap<>();

        /*
         * Return all collections of items, for all providers, for the specified typeId.
         */
        Collection<Collection<Item>> getAllItemsByTypeId(final String typeId) {
            Map<String, Collection<Item>> itemsForTypeId = itemIndex.get(typeId);
            if (itemsForTypeId == null) {
                return null;
            }
            return itemsForTypeId.values();
        }

        /*
         * Index a collection of Items for the specified typeId and providerId.
         */
        void indexItemsByTypeIdAndProviderId(final String typeId, final String providerId,
                final Collection<Item> items) {
            Map<String, Collection<Item>> itemsForTypeId = itemIndex.get(typeId);
            if (itemsForTypeId == null) {
                itemsForTypeId = new HashMap<>();
                itemIndex.put(typeId, itemsForTypeId);
            }
            itemsForTypeId.put(providerId, items);
        }

        /*
         * Remove the collection of items for the specified typeId and providerId. Note that this
         * method also removes all tacks involving the removed items.
         */
        void removeAllItemsForTypeIdAndProviderId(final String typeId, final String providerId) {
            Map<String, Collection<Item>> itemsForTypeId = itemIndex.get(typeId);
            if (itemsForTypeId == null) {
                return;
            }
            Collection<Item> allItemsForProvider = itemsForTypeId.get(providerId);
            if (allItemsForProvider == null) {
                return;
            }
            // Remove the tacks as well
            for (Item item : allItemsForProvider) {
                itemTacker.removeItem(item);
            }
            itemsForTypeId.remove(providerId);
            if (itemsForTypeId.isEmpty()) {
                itemsForTypeId.remove(typeId);
            }
        }
    }

    /*
     * Index of values of Items for a specific attribute key. Both the value and the attribute key
     * are specified by indexable stitching rules, and also index of Item logical ID to indexed
     * value for the Item.
     */
    private class ValueIndex {
        /*
         * The fist key is the value of an indexed attribute, returned by an indexable stitching
         * rule, eg "foo.hp.com", containing the logical ID of the Item.
         *
         * The second key is the logicalId of Items that share that value, with the value of the map
         * being the Item itself.
         */
        Map<String, Map<String, Item>> indexedValues = new HashMap<>();

        /*
         * Map of logicalId of an Item to the indexed value of the Item.
         */
        Map<String, String> currentValues = new HashMap<>();

        private void deleteItemUsingValue(final String value, final Item item) {
            // Remove the entry for Item from the indexedValues based on old value
            Map<String, Item> itemsMap = indexedValues.get(value);
            itemsMap.remove(item.getLogicalId());
            if (itemsMap.isEmpty()) {
                indexedValues.remove(value);
            }
        }

        /*
         * Index an Item with the specified value. Adding an item will not only index the Item by
         * the current supplied value, but will also if necessary update the indexed values based on
         * the old indexed value.
         */
        void addOrReplaceItem(final String value, final Item item) {
            String logicalId = item.getLogicalId();
            String oldValue = currentValues.get(logicalId);
            if (oldValue != null && !value.equals(oldValue)) {
                deleteItemUsingValue(oldValue, item);
            }
            // Re-index the Item
            currentValues.put(logicalId, value);
            Map<String, Item> itemsMap = indexedValues.get(value);
            if (itemsMap == null) {
                itemsMap = new HashMap<>();
                indexedValues.put(value, itemsMap);
            }
            itemsMap.put(logicalId, item);
        }

        /*
         * Delete an item from the index.
         */
        void deleteItem(final Item item) {
            String logicalId = item.getLogicalId();
            String oldValue = currentValues.remove(logicalId);
            deleteItemUsingValue(oldValue, item);
        }

        /*
         * Return a collection of all items that share the specified value for the attribute key.
         * This collection forms the candidate set for matching of stitches.
         */
        Collection<Item> getItemsWithValue(final String value) {
            Map<String, Item> itemsMap = indexedValues.get(value);
            if (itemsMap == null) {
                return null;
            } else {
                return itemsMap.values();
            }
        }
    }

    /*
     * Maintains an index of item attributes for indexable stitching rules. A ValueIndex is
     * maintained on a per {typeId, providerId, attributeKey}, where the attributeKey is specified
     * by an indexable stitching rule. It is the ValueIndex that contains the index of values.
     */
    private class ItemAttributeIndexer {
        /*
         * Key of first map is typeId of "origin" of StitcherRule id, eg "cmu-node", containing a
         * Map of all indexed values for Items of that type.
         *
         * Second key is providerId of the source of items.
         *
         * Third key is a concatenation of the ruleId and the index key returned by a rule, e.g.
         * "rule#name", and points to a ValueIndex for that key.
         */
        private Map<String, Map<String, Map<String, ValueIndex>>> itemAttributeIndex = new HashMap<>();

        /*
         * Return (create if necessary) the ValueIndex for the specified {typeId, providerId,
         * attributeKey}.
         */
        ValueIndex getOrCreateValueIndex(final String typeId, final String providerId, final String ruleId,
                final String key) {
            String attributeKey = ruleId + "#" + key;
            Map<String, Map<String, ValueIndex>> providerIdIndex = itemAttributeIndex.get(typeId);
            if (providerIdIndex == null) {
                providerIdIndex = new HashMap<>();
                itemAttributeIndex.put(typeId, providerIdIndex);
            }
            Map<String, ValueIndex> attributeKeyIndex = providerIdIndex.get(providerId);
            if (attributeKeyIndex == null) {
                attributeKeyIndex = new HashMap<>();
                providerIdIndex.put(providerId, attributeKeyIndex);
            }
            ValueIndex valueIndex = attributeKeyIndex.get(attributeKey);
            if (valueIndex == null) {
                valueIndex = new ValueIndex();
                attributeKeyIndex.put(attributeKey, valueIndex);
            }
            return valueIndex;
        }

        /*
         * Return a collection of all items of a specified typeId that share the same indexed value
         * for the specified attributeKey and attributeValue. The returned collection will be drawn
         * from all providers of the specified typeId.
         */
        Collection<Item> getItemsByAttributeValue(final String typeId, final String ruleId, final String key,
                final String attributeValue) {
            String attributeKey = ruleId + "#" + key;
            Map<String, Map<String, ValueIndex>> providerIdIndex = itemAttributeIndex.get(typeId);
            if (providerIdIndex == null) {
                return null;
            }
            List<Item> items = new ArrayList<>();
            // Find Index for every provider
            for (Map<String, ValueIndex> providerValueIndices : providerIdIndex.values()) {
                ValueIndex valueIndex = providerValueIndices.get(attributeKey);
                if (valueIndex != null) {
                    Collection<Item> providerItems = valueIndex.getItemsWithValue(attributeValue);
                    if (providerItems != null) {
                        items.addAll(providerItems);
                    }
                }
            }
            return items;
        }

        /*
         * Remove all indexed values for the specified typeId and providerId.
         */
        void removeAllItemsForTypeIdAndProviderId(final String typeId, final String providerId) {
            Map<String, Map<String, ValueIndex>> providerIdIndex = itemAttributeIndex.get(typeId);
            if (providerIdIndex == null) { // Nothing to do
                return;
            }
            Map<String, ValueIndex> attributeKeyIndex = providerIdIndex.remove(providerId);
            if (attributeKeyIndex == null) { // No providers remaining for this type
                itemAttributeIndex.remove(typeId);
            }
        }
    }

    private class ItemStitchDelta {
        private String logicalId;
        private Map<String, Item> oldStitches;
        private Set<String> newStitches = new HashSet<>();

        void addNewStitchToItem(final Item otherItem) {
            newStitches.add(otherItem.getLogicalId());
        }

        void removeNewFromOld() {
            for (String logicalId : newStitches) {
                oldStitches.remove(logicalId);
            }
        }

        String getLogicalId() {
            return logicalId;
        }

        Map<String, Item> getOldStitches() {
            return oldStitches;
        }

        ItemStitchDelta(final String logicalId, final Map<String, Item> oldStitches) {
            this.logicalId = logicalId;
            this.oldStitches = oldStitches;
        }

        boolean oldStitchesContains(final String otherLogicalId) {
            return oldStitches.containsKey(otherLogicalId);
        }
    }


    private ItemTacker itemTacker = new ItemTacker();
    private ItemIndexer itemIndexer = new ItemIndexer();
    private ItemAttributeIndexer itemAttributeIndexer = new ItemAttributeIndexer();


    // /////////////////////////////////////////////////////
    // ItemEquivalence interface
    // /////////////////////////////////////////////////////

    @Override
    public Collection<Item> getEquivalentItems(final Item item) {
        if (!allowStitching) {
            return null;
        }

        return itemTacker.getTackedItems(item);
    }

    // /////////////////////////////////////////////////////
    // TackUpdater interface
    // /////////////////////////////////////////////////////

    private int nullSize(final Collection collection) {
        return collection == null ? 0 : collection.size();
    }

    @Override
    public Collection<Item> stitchItems(final String thisTypeId, final String providerId,
            final Collection<Item> allItems, final Collection<Item> newItems, final Collection<Item> updatedItems,
            final Collection<Item> deletedItems) {

        if (!allowStitching) {
            return null;
        }

        // Verify parameters
        if (StringUtils.isEmpty(thisTypeId)) {
            throw new IllegalArgumentException("Unexpected null or empty TypeId");
        }
        if (StringUtils.isEmpty(providerId)) {
            throw new IllegalArgumentException("Unexpected null or empty ProviderId");
        }
        if (allItems == null) {
            throw new IllegalArgumentException("Unexpected null collection for allItems");
        }
        // if (newItems == null) {
        // throw new IllegalArgumentException("Unexpected null collection for newItems");
        // }
        // if (updatedItems == null) {
        // throw new IllegalArgumentException("Unexpected null collection for updatedItems");
        // }
        // if (deletedItems == null) {
        // throw new IllegalArgumentException("Unexpected null collection for deletedItems");
        // }

        if (LOG.isDebugEnabled()) {
            LOG.debug("stitchItems for provider " + providerId + " type " + thisTypeId + " allItems=" + allItems.size()
                    + " newItems=" + nullSize(newItems) + " updatedItems=" + nullSize(updatedItems) + " deletedItems="
                    + nullSize(deletedItems));
        }

        Map<String, Item> tackedItems = new HashMap<>();

        //
        // Index the items.
        itemIndexer.indexItemsByTypeIdAndProviderId(thisTypeId, providerId, allItems);

        Map<String, StitcherRule> tackRulesMap = ruleManager.getReleventSourceRules(thisTypeId);
        if (tackRulesMap.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("stitchItems for provider " + providerId + " type " + thisTypeId + " no rules apply");
            }
            return tackedItems.values();
        }

        StopWatch watch = new StopWatch();
        watch.start();

        // For updated items, remove all tacks to other types before start re-stitching. Otherwise
        // multiple rules that apply to the same {thisTypeId, otherTypeId} tuple will both attempt
        // to remove existing stitches and therefore interfere.
        Map<String, Map<String, ItemStitchDelta>> oldStitchesByType = new HashMap<>();
        for (Map.Entry<String, StitcherRule> ruleEntry : tackRulesMap.entrySet()) {
            String ruleId = ruleEntry.getKey();
            StitcherRule rule = ruleEntry.getValue();
            String otherTypeId = rule.otherTypeId();
            Map<String, ItemStitchDelta> oldStitchesById = oldStitchesByType.get(otherTypeId);
            if (oldStitchesById == null) {
                oldStitchesById = new HashMap<>();
                oldStitchesByType.put(otherTypeId, oldStitchesById);
                for (Item thisItem : updatedItems) {
                    Map<String, Item> oldStitches = itemTacker.removeTacksForTypeId(thisItem, otherTypeId);
                    if (oldStitches != null) {
                        oldStitchesById.put(thisItem.getLogicalId(),
                                new ItemStitchDelta(thisItem.getLogicalId(), oldStitches));
                    }
                }
            }
        }


        for (Map.Entry<String, StitcherRule> ruleEntry : tackRulesMap.entrySet()) {
            String ruleId = ruleEntry.getKey();
            StitcherRule rule = ruleEntry.getValue();

            if (LOG.isDebugEnabled()) {
                LOG.debug("stitchItems for rule " + ruleInfo(rule) + " provider " + providerId + " type " + thisTypeId
                        + " allItems=" + allItems.size());
            }

            String otherTypeId = rule.otherTypeId();
            Map<String, ItemStitchDelta> oldStitchesById = oldStitchesByType.get(otherTypeId);

            if (allowIndexing && rule.isIndexable() && rule instanceof IndexableStitcherRule) {
                IndexableStitcherRule indexableRule = (IndexableStitcherRule) rule;
                //
                // Index items and find items using index of attribute values
                String thisIndexKey = indexableRule.indexKey();
                if (StringUtils.isEmpty(thisIndexKey)) {
                    LOG.warn("Indexable Stitcher Rule returned an empty indexKey " + ruleInfo(rule));
                    continue;
                }
                String otherIndexKey = indexableRule.otherIndexKey();
                if (StringUtils.isEmpty(otherIndexKey)) {
                    LOG.warn("Indexable Stitcher Rule returned an empty otherIndexKey " + ruleInfo(rule));
                    continue;
                }

                // Get the valueIndex for source items
                ValueIndex valueIndex =
                        itemAttributeIndexer.getOrCreateValueIndex(thisTypeId, providerId, ruleId, thisIndexKey);

                // Add tacks for new items
                if (newItems != null) {
                    for (Item thisItem : newItems) {
                        // Index item for this rule
                        String thisIndexValue = indexItemValue(indexableRule, valueIndex, thisItem);
                        if (thisIndexValue == null) {
                            LOG.warn("Indexable Stitcher Rule returned a null indexValue " + ruleInfo(rule));
                            continue;
                        }

                        // Find candidate Items from other collection
                        String otherIndexValue = indexableRule.otherIndexValue(thisItem);
                        if (otherIndexValue == null) {
                            LOG.warn("Indexable Stitcher Rule returned a null otherIndexValue " + ruleInfo(rule));
                            continue;
                        }
                        Collection<Item> otherItems = itemAttributeIndexer.getItemsByAttributeValue(otherTypeId, ruleId,
                                otherIndexKey, otherIndexValue);
                        if (otherItems != null) {
                            addTacks(rule, thisItem, otherItems, tackedItems);
                        }
                    }
                }

                // Refresh tacks for updated items
                for (Item thisItem : updatedItems) {
                    // Index item for this rule
                    String thisIndexValue = indexItemValue(indexableRule, valueIndex, thisItem);
                    if (thisIndexValue == null) {
                        LOG.warn("Indexable Stitcher Rule returned a null indexValue " + ruleInfo(rule));
                        continue;
                    }

                    // Which items were affected by removal of the tack
                    ItemStitchDelta itemStitchDelta = oldStitchesById.get(thisItem.getLogicalId());

                    // Find candidate Items from other collection
                    String otherIndexValue = indexableRule.otherIndexValue(thisItem);
                    if (otherIndexValue == null) {
                        LOG.warn("Indexable Stitcher Rule returned a null otherIndexValue " + ruleInfo(rule));
                        continue;
                    }
                    Collection<Item> otherItems = itemAttributeIndexer.getItemsByAttributeValue(otherTypeId, ruleId,
                            otherIndexKey, otherIndexValue);
                    if (otherItems != null) {
                        refreshTacks(rule, thisItem, otherItems, tackedItems, itemStitchDelta);
                    }
                }

                // Remove index for deleted items
                for (Item deletedItem : deletedItems) {
                    valueIndex.deleteItem(deletedItem);
                }
            } else {
                //
                // Need to do pair-wise comparison of Items
                Collection<Collection<Item>> otherItemCollections = itemIndexer.getAllItemsByTypeId(otherTypeId);
                if (otherItemCollections == null) {
                    // Have not received an update for items of this type yet
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Have not yet received an update for items of type " + otherTypeId);
                    }
                    continue;
                }

                if (newItems != null) {
                    // Add tacks for new items
                    for (Item thisItem : newItems) {
                        for (Collection<Item> otherItems : otherItemCollections) {
                            addTacks(rule, thisItem, otherItems, tackedItems);
                        }
                    }
                }

                if (updatedItems != null) {
                    // Refresh tacks for updated items
                    for (Item thisItem : updatedItems) {
                        // Which items were affected by removal of the tack
                        ItemStitchDelta itemStitchDelta = oldStitchesById.get(thisItem.getLogicalId());
                        for (Collection<Item> otherItems : otherItemCollections) {
                            refreshTacks(rule, thisItem, otherItems, tackedItems, itemStitchDelta);
                        }
                    }
                }
            }
        }

        // Were there any stitch removals remaining (for updated items) to be taken account of
        for (Map<String, ItemStitchDelta> oldStitchesById : oldStitchesByType.values()) {
            for (ItemStitchDelta itemStitchDelta : oldStitchesById.values()) {
                itemStitchDelta.removeNewFromOld();
                Map<String, Item> removedStitches = itemStitchDelta.getOldStitches();
                if (!removedStitches.isEmpty()) {
                    for (Item itemRemoved : removedStitches.values()) {
                        tackedItems.put(itemRemoved.getLogicalId(), itemRemoved);
                    }
                }
            }
        }


        if (deletedItems != null) {
            // Remove tacks for deleted items
            deleteTacks(deletedItems, tackedItems);
        }

        watch.stop();
        LOG.info("Stitching for type " + thisTypeId + " time=" + watch);

        return tackedItems.values();
    }


    private String indexItemValue(final IndexableStitcherRule indexableRule, final ValueIndex valueIndex,
            final Item thisItem) {
        // Index item for this rule
        String thisIndexValue = indexableRule.indexValue(thisItem);
        if (thisIndexValue != null) {
            valueIndex.addOrReplaceItem(thisIndexValue, thisItem);
        }
        return thisIndexValue;
    }

    private void addTacks(final StitcherRule rule, final Item thisItem, final Collection<Item> otherItems,
            final Map<String, Item> tackedItems) {
        for (Item otherItem : otherItems) {
            if (rule.matches(thisItem, otherItem)) {
                // If a match was found, tack the items
                itemTacker.tackItems(thisItem, otherItem);
                tackedItems.put(otherItem.getLogicalId(), otherItem);
            }
        }
    }

    private void refreshTacks(final StitcherRule rule, final Item thisItem, final Collection<Item> otherItems,
            final Map<String, Item> tackedItems, final ItemStitchDelta itemStitchDelta) {
        for (Item otherItem : otherItems) {
            if (rule.matches(thisItem, otherItem)) {
                // If a match was found, tack the items
                itemTacker.tackItems(thisItem, otherItem);
                boolean stitchedSameItem = false; // Are we re-stitching an existing stitch
                if (itemStitchDelta != null) {
                    itemStitchDelta.addNewStitchToItem(otherItem);
                    boolean hasOldStitch = itemStitchDelta.oldStitchesContains(otherItem.getLogicalId());
                    if (hasOldStitch) {
                        stitchedSameItem = true;
                    }
                }
                if (!stitchedSameItem) {
                    tackedItems.put(otherItem.getLogicalId(), otherItem);
                }
            }
        }
    }

    private void deleteTacks(final Collection<Item> deletedItems, final Map<String, Item> tackedItems) {
        for (Item deletedItem : deletedItems) {
            Map<String, Item> oldStitches = itemTacker.removeItem(deletedItem);
            if (oldStitches != null) {
                for (Item itemRemoved : oldStitches.values()) {
                    tackedItems.put(itemRemoved.getLogicalId(), itemRemoved);
                }
            }
        }
    }

    private String ruleInfo(final StitcherRule rule) {
        return "source type " + rule.sourceTypeId() + " other type " + rule.otherTypeId();
    }


    @Override
    public void removeStitchedItems(final String typeId, final String providerId) {

        if (!allowStitching) {
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Remove all stitched items typeId=" + typeId + " providerId=" + providerId);
        }

        if (ruleManager.rulesExistForItemTypeId(typeId)) {
            StopWatch watch = new StopWatch();
            watch.start();
            // There may be stitches for this item type, which are removed by this call to
            // itemIndexer.
            itemIndexer.removeAllItemsForTypeIdAndProviderId(typeId, providerId);
            itemAttributeIndexer.removeAllItemsForTypeIdAndProviderId(typeId, providerId);
            watch.stop();
            LOG.info("removeStitchedItems " + typeId + " " + providerId + " time=" + watch);
        }
    }
}
