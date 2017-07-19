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
package com.hp.hpl.loom.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.SeparableItem;
import com.hp.hpl.loom.relationships.RelationshipUtil;

/**
 * Abstract updater this provides a some of the updater logic.
 *
 * @param <T> AdapterItem type to use
 * @param <A> CoreItemAttributes to based on
 * @param <R> Resource to use
 */
public abstract class AggregationUpdater<T extends AdapterItem<A>, A extends CoreItemAttributes, R> {
    private static final Log LOG = LogFactory.getLog(AggregationUpdater.class);

    protected Aggregation aggregation;
    protected BaseAdapter adapter;
    protected ItemType itemType;
    protected String logicalIdBase;
    protected HashMap<String, T> oldItemMap;
    protected HashMap<String, T> newItemMap;
    protected HashMap<String, String> newRelationshipsMap;
    private boolean alreadyCalled = false;
    private AdapterUpdateResult upRes;
    protected ItemCollector itemCollector;
    protected Map<String, ItemAttributeDelta<A>> attrDeltaMap;
    protected long collectionEpoch;
    protected Date currentDate;

    // data collection times
    private TimingBag updateTimingBag;
    private TimingBag collectTimingBag;
    private TimingBag wrapupTimingBag;

    /**
     * Constructor for the aggregation updater takes an aggregation, adapter, itemTypeLocalId and
     * itemCollector. It maintains this aggregation is up to date.
     *
     * @param aggregation the aggregation this updater updates
     * @param adapter the adapter linked to this updater
     * @param itemTypeLocalId The itemTypeLocalId
     * @param itemCollector The itemCollector linked to this updater
     * @throws NoSuchItemTypeException throw if the itemType doesn't exist
     */
    public AggregationUpdater(final Aggregation aggregation, final BaseAdapter adapter, final String itemTypeLocalId,
            final ItemCollector itemCollector) throws NoSuchItemTypeException {
        this.aggregation = aggregation;
        this.adapter = adapter;
        this.itemCollector = itemCollector;
        init(itemTypeLocalId);
    }

    /**
     * Constructor for the aggregation updater takes an aggregation, adapter, itemTypeLocalId and
     * itemCollector. It maintains this aggregation is up to date.
     *
     * @param aggregation the aggregation this updater updates
     * @param adapter the adapter linked to this updater
     * @param itemCollector The itemCollector linked to this updater
     * @throws NoSuchItemTypeException thrown if the itemType doesn't exist
     * @throws NoSuchProviderException thrown if the adapter is not known
     */
    public AggregationUpdater(final Aggregation aggregation, final BaseAdapter adapter,
            final ItemCollector itemCollector) throws NoSuchItemTypeException, NoSuchProviderException {
        this.aggregation = aggregation;
        this.adapter = adapter;
        this.itemCollector = itemCollector;
        String localTypeId = adapter.getItemTypeLocalIdFromLogicalId(aggregation.getLogicalId());
        init(localTypeId);
    }

    private void init(final String itemTypeLocalId) throws NoSuchItemTypeException {
        this.itemType = adapter.getItemType(itemTypeLocalId);
        logicalIdBase = adapter.getAggregationLogicalId(itemType.getLocalId());
        oldItemMap = new HashMap<>();
        newItemMap = new HashMap<>();
        newRelationshipsMap = new HashMap<>();
        attrDeltaMap = new HashMap<>();
        collectTimingBag = new TimingBag(aggregation.getLogicalId() + " - COLLECT");
        updateTimingBag = new TimingBag(aggregation.getLogicalId() + " - UPDATE_CREATION");
        wrapupTimingBag = new TimingBag(aggregation.getLogicalId() + " - WRAPUP");
    }

    /**
     * Gets the old items.
     *
     * @return the map of the old items
     */
    public HashMap<String, T> getOldItems() {
        return oldItemMap;
    }

    /**
     * Get the aggregation this update relates to.
     *
     * @return the aggregation
     */
    public Aggregation getAggregation() {
        return aggregation;
    }


    /**
     * Gets the logical id for a given resource id (this makes use of the adapter and calls
     * adapter.getItemLogicalId).
     *
     * @param resourceId ResourceId to lookup for
     * @return Logical id
     */
    public String getLogicalId(final String resourceId) {
        return adapter.getItemLogicalId(aggregation, resourceId);
    }

    /**
     * Returns a list of the items for this aggregation updater.
     *
     * @return the items
     */
    public ArrayList<T> getItems() {
        return new ArrayList<>(newItemMap.values());
    }

    /**
     * Returns the old item for a given logical id.
     *
     * @param logicalId the logical id to lookup
     * @return The old item
     */
    public T getOldItem(final String logicalId) {
        return oldItemMap.get(logicalId);
    }

    /**
     * Returns the item for a given item id.
     *
     * @param itemId the item id to lookup
     * @return The item
     */
    public T getItem(final String itemId) {
        return newItemMap.get(getLogicalId(itemId));
    }

    /**
     * Returns the item for a given logical id.
     *
     * @param logicalId the logical id to lookup
     * @return The item
     */
    public T getItemFromLogicalId(final String logicalId) {
        return newItemMap.get(logicalId);
    }

    /**
     * Get the latest core items for a given itemId.
     *
     * @param itemId Item id to lookup based on.
     * @return CoreAttributes for the given itemId
     */
    public A getLatestCoreItemAttributes(final String itemId) {
        String logicalId = getLogicalId(itemId);
        ItemAttributeDelta<A> iad = attrDeltaMap.get(logicalId);
        if (iad == null) {
            T item = newItemMap.get(logicalId);
            if (item != null) {
                return item.getCore();
            }
        } else {
            return iad.getNewItemAttributes();
        }
        return null;
    }

    /**
     * Update the items.
     *
     * @param cEpoch The time of the update
     * @return the updated results
     */
    public AdapterUpdateResult updateItems(final long cEpoch) {
        StopWatch watch = new StopWatch();
        watch.start();
        // init new collection dat structure
        this.collectionEpoch = cEpoch;
        this.currentDate = new Date();
        upRes = new AdapterUpdateResult(oldItemMap.size());
        newItemMap = new HashMap<>(oldItemMap.size());
        Iterator<R> iter = null;
        List<R> items = null;

        List<AdapterItem<A>> existingItems = null;

        String adapterKey = adapter.adapterConfig.getProviderType() + "-" + adapter.adapterConfig.getProviderId();

        if (itemCollector.isGlobal()) {
            items = new ArrayList<>();
            iter = getResourceIterator();
        } else {
            ItemCollector collector = adapter.getGlobal();
            if (collector == null) {
                iter = getResourceIterator();
            } else {
                existingItems = new ArrayList<>();
                HashMap<String, T> data = collector.getUpdater(this.itemType.getLocalId()).getOldItems();
                for (AdapterItem<A> adapterItem : data.values()) {
                    if (this.includeInResult(adapterItem)) {
                        existingItems.add(adapterItem);
                    }
                }
            }
        }

        if (existingItems == null) {
            while (iter.hasNext()) {
                R resource = iter.next();
                processResource(resource);
            }
        } else {
            Iterator<AdapterItem<A>> iter2 = existingItems.iterator();
            while (iter2.hasNext()) {
                AdapterItem<A> item = iter2.next();
                processItem(item);
            }
        }

        // find out deletion
        for (Map.Entry<String, T> oldEntry : oldItemMap.entrySet()) {
            String logicalId = oldEntry.getKey();
            T oldItem = oldEntry.getValue();
            if (!newItemMap.containsKey(logicalId)) {
                oldItem.setUpdateStatus(AdapterItem.DELETED);
                upRes.addToDeleted(oldItem);
            }
        }
        int relationDeltaSize = 0;
        upRes.setUpdateEstimate(upRes.getUpdatedItems().size() + relationDeltaSize);
        watch.stop();
        collectTimingBag.recordTime(watch.getTime());

        return upRes;
    }

    @SuppressWarnings("checkstyle:linelength")
    private void processItem(AdapterItem<A> item) {

        String logicalId = item.getLogicalId();
        T adapterItem = newItemMap.get(logicalId);
        if (adapterItem != null) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("found a duplicate Item: " + logicalId);
            }
            return;
        }
        adapterItem = oldItemMap.get(logicalId);
        if (adapterItem == null) {
            // new Item
            adapterItem = createFullItemFromItem(logicalId, item);
            if (adapterItem != null) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("updateFirst - putting full Item & new: " + logicalId);
                }
                adapterItem.setFibreCreated(currentDate);
                upRes.addToNew(adapterItem);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("NOT able to create an AdapterItem (Null ItemAttributes?): " + logicalId);
                }
            }
        } else {
            // LOG.debug("EHUD_DEBUG: repeat Item: " + logicalId);
            // we have already seen it
            // clear updated tag from previous collection cycle
            adapterItem.clearUpdateStatus();
            // update relationship name list
            adapterItem.updateRelnameSet();

            if (!adapterItem.getUpdateStatus().equals(item.getUpdateStatus())) {
                adapterItem.setUpdateStatus(AdapterItem.UPDATED);
            }
        }
        if (adapterItem != null) {
            newItemMap.put(logicalId, adapterItem);
            adapterItem.clearRelationshipsIds();
        }

        Map<String, Map<String, Item>> relTypeToItemsMap = item.getConnectedRelationships();
        Set<String> relKeys = relTypeToItemsMap.keySet();
        for (String key : relKeys) {
            Map<String, Item> itemsMap = relTypeToItemsMap.get(key);
            Collection<Item> itemsValues = itemsMap.values();
            for (Item i : itemsValues) {
                if (i instanceof SeparableItem) {
                    if (includeInRelationships(i)) {
                        adapterItem.setRelationship(this.adapter.provider,
                                ((SeparableItem) i).getItemType().getLocalId(),
                                ((SeparableItem) i).getCore().getItemId());
                    }
                }
            }
        }


        // now calculate rel delta
        if (adapterItem != null) {
            upRes.addToAll(adapterItem);
            if (adapterItem.isUpdateStatusUpdated()) {
                upRes.addToUpdated(adapterItem);
            }
        }
    }

    private void processResource(R resource) {

        String logicalId = getLogicalId(getItemId(resource));
        T adapterItem = newItemMap.get(logicalId);
        if (adapterItem != null) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("found a duplicate Item: " + logicalId);
            }
            return;
        }
        adapterItem = oldItemMap.get(logicalId);
        if (adapterItem == null) {
            // new Item
            adapterItem = createFullItem(logicalId, resource);
            if (adapterItem != null) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("updateFirst - putting full Item & new: " + logicalId);
                }
                adapterItem.setFibreCreated(currentDate);
                upRes.addToNew(adapterItem);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("NOT able to create an AdapterItem (Null ItemAttributes?): " + logicalId);
                }
            }
        } else {
            // LOG.debug("EHUD_DEBUG: repeat Item: " + logicalId);
            // we have already seen it
            // clear updated tag from previous collection cycle
            adapterItem.clearUpdateStatus();
            // update relationship name list
            adapterItem.updateRelnameSet();
            CoreItemAttributes.ChangeStatus changeStatus =
                    compareItemAttributesToResource(adapterItem.getCore(), resource);
            if (!changeStatus.equals(CoreItemAttributes.ChangeStatus.UNCHANGED)) {
                LOG.debug("Item changed since last collection cycle: " + logicalId);
                // change! a new ItemAttributes needs to be created
                attrDeltaMap.put(logicalId, new ItemAttributeDelta<A>(adapterItem, createItemAttributes(resource)));
                if (changeStatus.equals(CoreItemAttributes.ChangeStatus.CHANGED_UPDATE)) {
                    adapterItem.setUpdateStatus(AdapterItem.UPDATED);
                }
            } else {
                // LOG.debug("EHUD_DEBUG: Same same: " + logicalId);
                // no attr change detected - no extra action required
                adapterItem.setUpdateStatus(AdapterItem.UNCHANGED);
            }
        }
        if (adapterItem != null) {
            newItemMap.put(logicalId, adapterItem);
            adapterItem.clearRelationshipsIds();
        }
        // only have to worry about setting Ids
        // no check for null adapterItem since we have cases where relationships are set that
        // don't use
        // the adapterItem directly (see PortUpdater in OpenStack adapter)
        setRelationships(adapterItem, resource);

        // now calculate rel delta
        if (adapterItem != null) {
            upRes.addToAll(adapterItem);
            if (adapterItem.isUpdateStatusUpdated()) {
                upRes.addToUpdated(adapterItem);
            }
        }
    }

    /**
     * This goes through each Item in newItemMap and calculate relationship delta or goes for a full
     * view.
     *
     * @param deltaEnabled flag indicating if the deltas are enabled
     * @return the updated AdapterUpdateResults
     */
    public AdapterUpdateResult updateRelationships(final boolean deltaEnabled) {
        StopWatch watch = new StopWatch();
        watch.start();
        if (deltaEnabled && alreadyCalled) {
            UpdateDelta upDelta = upRes.getOrCreateUpdateDelta();
            if (LOG.isDebugEnabled()) {
                LOG.debug("using DELTA MODE " + itemType.getLocalId());
            }
            // create deleted deltas
            // UpdateDelta upDelta = upRes.getOrCreateUpdateDelta();
            for (Item oldItem : upRes.getDeletedItems()) {
                upDelta.addItemDeletionDelta(new ItemDeletionDelta(oldItem));
            }
            // add attribute deltas to upRes
            for (ItemAttributeDelta<A> iad : attrDeltaMap.values()) {
                upDelta.addItemAttributeDelta(iad);
            }
            // create relationships deltas
            for (Item item : upRes.getAllItems()) {
                Collection<ItemRelationsDelta> irds = ((AdapterItem) item).createRelationshipsDeltasIDs();
                if (irds != null && !irds.isEmpty()) {
                    upDelta.addItemRelationsDeltas(irds);
                    // if (adapterItem.isUpdateStatusUnchanged()) {
                    // adapterItem.setUpdateStatus(AdapterItem.UPDATED);
                    // }
                }
            }

        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("using FULL MODE " + itemType.getLocalId());
            }
            // Must now provide a full view; too many changes?
            // upRes.clearAllItems();
            ArrayList<Item> freshAll = new ArrayList<>();
            upRes.clearUpdatedItems();
            for (Item item : upRes.getAllItems()) {
                // for (String logicalId : newItemMap.keySet()) {
                T adapterItem = getNewAdapterItem(item.getLogicalId());
                // now time to fill-in the empty connected Relationship structure
                Map<String, Collection<String>> relationshipsIds = adapterItem.getRelationshipsIds();
                for (String relName : relationshipsIds.keySet()) {
                    String otherLocalTypeId = RelationshipUtil.getOtherLocalTypeId(relName, itemType.getId());
                    for (String lid : relationshipsIds.get(relName)) {
                        AdapterItem<?> otherItem = itemCollector.getNewAdapterItem(otherLocalTypeId, lid);
                        // LOG.debug("EHUD_DEBUG: FUll Mode: addConnectedRel: " + logicalId +
                        // " otherItem: "
                        // + otherItem.getLogicalId());
                        if (otherItem != null) {
                            adapterItem.addConnectedRelationshipsWithName(relName, otherItem);
                        }
                    }
                }
                adapterItem.clearRelationshipsIds();
                // upRes.addToAll(adapterItem);
                freshAll.add(adapterItem);
                if (adapterItem.isUpdateStatusUpdated()) {
                    upRes.addToUpdated(adapterItem);
                }
            }
            // now replace all with freshAll
            upRes.setAllItems(freshAll);
        }
        watch.stop();
        updateTimingBag.recordTime(watch.getTime());
        return upRes;
    }

    /**
     * Adds an item to the updated list.
     *
     * @param item item to add to the updated list
     */
    public void categorizeItemAsUpdated(final Item item) {
        upRes.addToUpdated(item);
    }

    private boolean processRelDelta(final ItemRelationsDelta ird) {
        RelationsDeltaType deltaType = ird.getType();
        String relName = ird.getRelationsName();
        String otherLocalTypeId;
        if (deltaType.equals(RelationsDeltaType.Clear)) {
            Collection<String> relNames;
            if (relName == null) {
                relNames = ird.getItem().getConnectedRelationships().keySet();
            } else {
                relNames = Arrays.asList(relName);
            }
            boolean nonDeletedFound = false;
            for (String relationName : relNames) {
                // otherLocalTypeId = RelationshipUtil.getOtherLocalTypeId(relName,
                // itemType.getLocalId());
                otherLocalTypeId = RelationshipUtil.getOtherLocalTypeId(relationName, itemType.getId());
                Map<String, Item> relMap = ird.getItem().getConnectedRelationships().get(relationName);
                if (relMap != null) {
                    for (Item item : relMap.values()) {
                        AdapterItem<?> adapterItem =
                                itemCollector.getAdapterItem(otherLocalTypeId, item.getLogicalId());
                        if (adapterItem != null) {
                            if (adapterItem.isUpdateStatusUnchanged()) {
                                adapterItem.setUpdateStatus(AdapterItem.UPDATED);
                                itemCollector.categorizeItemAsUpdated(otherLocalTypeId, adapterItem);
                            }
                            // once set to true, stays true
                            nonDeletedFound |= !adapterItem.isUpdateStatusDeleted();
                        }
                    }
                }
            }
            // ignore the relDelta if all Item "others" are deleted
            return nonDeletedFound;
        } else if (deltaType.equals(RelationsDeltaType.Add)) {
            otherLocalTypeId = RelationshipUtil.getOtherLocalTypeId(relName, itemType.getId());
            ArrayList<Item> others = new ArrayList<>();
            for (String lid : ird.getOtherIds()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("querying lid: " + lid + " for otherLocalTypeId: " + otherLocalTypeId);
                }
                AdapterItem<?> adapterItem = itemCollector.getAdapterItem(otherLocalTypeId, lid);
                if (adapterItem != null) {
                    others.add(adapterItem);
                    if (adapterItem.isUpdateStatusUnchanged()) {
                        adapterItem.setUpdateStatus(AdapterItem.UPDATED);
                        itemCollector.categorizeItemAsUpdated(otherLocalTypeId, adapterItem);
                    }
                } else {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Relationships quote a logicalId that cannot be found as an Item: " + lid + " in ");
                        if (itemType != null) {
                            LOG.warn("Relationships quote a logicalId that cannot be found as an Item: in "
                                    + this.itemType.getLocalId());
                        }
                    }
                }
            }
            ird.setOthers(others);
            ird.setOtherIds(null);
            return !others.isEmpty();
        } else if (deltaType.equals(RelationsDeltaType.Remove)) {
            otherLocalTypeId = RelationshipUtil.getOtherLocalTypeId(relName, itemType.getId());
            ArrayList<Item> others = new ArrayList<>();
            for (String lid : ird.getOtherIds()) {
                AdapterItem<?> adapterItem = itemCollector.getAdapterItem(otherLocalTypeId, lid);
                if (adapterItem != null) {
                    if (adapterItem.isUpdateStatusDeleted()) {
                        continue;
                    }
                    others.add(adapterItem);
                    if (adapterItem.isUpdateStatusUnchanged()) {
                        adapterItem.setUpdateStatus(AdapterItem.UPDATED);
                        itemCollector.categorizeItemAsUpdated(otherLocalTypeId, adapterItem);
                    }
                } else {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("2. Relationships quote a logicalId that cannot be found as an Item: " + lid);
                    }
                }
            }
            ird.setOthers(others);
            ird.setOtherIds(null);
            return !others.isEmpty();
        } else {
            LOG.error("Unknown ItemRelationDelta type: " + deltaType);
            return false;
        }
    }

    /**
     * Performs a wrap up update with delta either enabled or not. It returns the
     * {@link UpdateResult}
     *
     * @param deltaEnabled set if the deltas are enabled
     * @return UpdateResults for the wrap up update
     */
    public UpdateResult wrapupUpdate(final boolean deltaEnabled) {
        StopWatch watch = new StopWatch();
        watch.start();
        if (alreadyCalled) {
            UpdateDelta upDelta = upRes.getUpdateDelta();
            if (upDelta != null) {
                List<ItemRelationsDelta> newRelDeltas = new ArrayList<>();
                for (ItemRelationsDelta ird : upDelta.getRelationsDelta()) {
                    if (processRelDelta(ird)) {
                        newRelDeltas.add(ird);
                        AdapterItem adapterItem = (AdapterItem) ird.getItem();
                        if (adapterItem.isUpdateStatusUnchanged()) {
                            adapterItem.setUpdateStatus(AdapterItem.UPDATED);
                            upRes.addToUpdated(adapterItem);
                        }
                    }
                }
                upDelta.setRelationsDelta(newRelDeltas);
            }
        }
        if (!deltaEnabled || !alreadyCalled) {
            for (T adapterItem : newItemMap.values()) {
                adapterItem.setGroundedAggregation(aggregation);
                if (adapterItem.update() || adapterItem.isUpdateStatusUpdated()) {
                    adapterItem.setFibreUpdated(currentDate);
                }

            }
            upRes.setUpdateDelta(null);
            upRes.setDeltaMode(false);
        } else {
            upRes.setDeltaMode(true);
        }

        if (!(newItemMap.isEmpty() && oldItemMap.isEmpty())) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("SETTING oldItemMap & clearing newItemMap");
            }
            oldItemMap = newItemMap;
            // newItemMap = new HashMap<>(oldItemMap.size());
        }
        attrDeltaMap.clear();
        if (!alreadyCalled) {
            upRes.forceUpdate();
            alreadyCalled = true;
        }
        watch.stop();
        wrapupTimingBag.recordTime(watch.getTime());
        return upRes;
    }

    /**
     * Gets a new AdapterItem for a given key, if it already exists in the newItemMap this is
     * returned instead.
     *
     * @param logicalId id to lookup
     * @return gets a newAdapterItem for the given logicalId
     */
    public T getNewAdapterItem(final String logicalId) {
        T adapterItem = newItemMap.get(logicalId);
        if (adapterItem != null && !adapterItem.matchCreationEpoch(collectionEpoch)) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("item epochs don't match - cloning adapterItem " + logicalId);
            }
            // not a NEW item
            // detect a relationship change
            // boolean relationshipChange = adapterItem.compareRelations();
            // not created this time round through attributeUpdate so clone
            adapterItem = cloneAdapterItem(adapterItem);
            newItemMap.put(logicalId, adapterItem);
            ItemAttributeDelta<A> iad = attrDeltaMap.get(logicalId);
            if (iad != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("setting new CoreItemAttribute while cloning adapterItem " + logicalId + " "
                            + iad.getNewItemAttributes());
                }
                adapterItem.setCore(iad.getNewItemAttributes());
            }
        }
        return adapterItem;
    }

    private T cloneAdapterItem(final T srcItem) {
        T newItem = createAndInitItem(srcItem.getLogicalId());
        // all next 6 should be set within a single setItemAttributes() type method
        newItem.setUuid(srcItem.getUuid());
        newItem.setFibreCreated(srcItem.getFibreCreated());
        newItem.setFibreUpdated(srcItem.getFibreUpdated());
        newItem.setAlertLevel(srcItem.getAlertLevel());
        newItem.setAlertDescription(srcItem.getAlertDescription());
        newItem.setFullyQualifiedName(srcItem.getFullyQualifiedName());
        newItem.setUpdateStatus(srcItem.getUpdateStatus());
        newItem.setCore(srcItem.getCore());
        newItem.setRelationshipsIds(srcItem.getRelationshipsIds());
        newItem.setRelnameSet(srcItem.getRelnameSet());
        return newItem;
    }

    /**
     * Logs the display item map.
     */
    public void displayItemMap() {
        StringBuffer stBuf = new StringBuffer();
        for (String key : newItemMap.keySet()) {
            stBuf.append("id: " + key + " itemName: " + newItemMap.get(key).getName() + "\n");
        }
        LOG.info("itemMap: \n" + stBuf.toString());
    }


    /**
     * Gets the collect timing bag.
     *
     * @return the timing bag
     */
    public TimingBag getCollectTimingBag() {
        return collectTimingBag;
    }

    /**
     * Gets the update timing bag.
     *
     * @return the timing bag
     */
    public TimingBag getUpdateTimingBag() {
        return updateTimingBag;
    }

    /**
     * Gets the wrapup timing bag.
     *
     * @return the timing bag
     */
    public TimingBag getWrapupTimingBag() {
        return wrapupTimingBag;
    }

    private T createAndInitItem(final String logicalId) {
        T newItem = createEmptyItem(logicalId);
        newItem.setCreationEpoch(collectionEpoch);
        newItem.setItemCollector(itemCollector);
        newItem.getProviderIds().add(adapter.getProvider().getProviderTypeAndId());
        return newItem;
    }

    protected T createFullItem(final String logicalId, final R resource) {
        A itemAttr = createItemAttributes(resource);
        if (itemAttr == null) {
            return null;
        }
        T newItem = createAndInitItem(logicalId);
        newItem.setUpdateStatus(AdapterItem.NEW);
        newItem.setCore(itemAttr);
        return newItem;
    }

    protected T createFullItemFromItem(final String logicalId, final AdapterItem<A> item) {
        T newItem = createAndInitItem(logicalId);
        newItem.setUpdateStatus(AdapterItem.NEW);
        newItem.setCore(item.getCore());
        return newItem;
    }


    protected String getItemQualifiedName(final T item) {
        return item.getQualifiedName();
    }

    // abstract methods to be implemented by specific adapters
    /**
     * Should return an Iterator covering all resources of the given type exposed by the Provider.
     *
     * @return
     */
    protected abstract Iterator<R> getResourceIterator();

    /**
     * Should return an Iterator based on the data passed to it (filtered by the current user if
     * required).
     *
     * @param data The global snapshot of the data
     * @return the Iterator<R> of results.
     */
    protected Iterator<R> getUserResourceIterator(Collection<R> data) {
        return getResourceIterator();
    }

    /**
     * Loom relies on each resource to be uniquely identifiable within the adapter's namespace using
     * this id. This id must not chnage within the lifetime of the resource.
     *
     * @param resource
     * @return the id linked, at the adapter's discretion to the given resource
     */
    protected abstract String getItemId(R resource);

    /**
     * This method returns an Item which should only be set with its logicalId and its ItemType.
     *
     * @param logicalId
     * @return an instance of the relevant Item
     */
    protected abstract T createEmptyItem(String logicalId);

    /**
     * This method must create a CoreItemAttributes object and populate its fields with values based
     * on the fields of the resource object. This method is called whenever a new or updated view of
     * the resource (i.e. first time round or whenever a chnage is detected) is required.
     *
     * @param resource
     * @return CoreItemAttributes instance with attributes set to values found in the resource
     *         object
     */
    protected abstract A createItemAttributes(R resource);

    /**
     * Called to detect if the resource has changed since the last data collection cycle. Not all
     * attributes need to be compared. The adapter writer is free to decide which attribute changes
     * constitute a meaningful change that Loom should be aware of.
     *
     * @param itemAttr the CoreItemAttributes object holding the attribute values collected in the
     *        previous cycle
     * @param resource the resource holding the latest values
     * @return UNCHANGED if no change is detected, CHANGED_IGNORE if some attributes have changed
     *         but are not impacting any queries (i.e. grounded aggregations should not be marked
     *         dirty) or CHANGED_UPDATE if attributes that may be used in queries have changed
     *         values.
     */
    protected abstract CoreItemAttributes.ChangeStatus compareItemAttributesToResource(A itemAttr, R resource);

    // /**
    // * Called to detect if the resource has changed since the last data collection cycle. Not all
    // * attributes need to be compared. The adapter writer is free to decide which attribute
    // changes
    // * constitute a meaningful change that Loom should be aware of.
    // *
    // * @param itemAttr the CoreItemAttributes object holding the attribute values collected in the
    // * previous cycle
    // * @param resource the resource holding the latest values
    // * @return UNCHANGED if no change is detected, CHANGED_IGNORE if some attributes have changed
    // * but are not impacting any queries (i.e. grounded aggregations should not be marked
    // * dirty) or CHANGED_UPDATE if attributes that may be used in queries have changed
    // * values.
    // */
    // protected CoreItemAttributes.ChangeStatus compareItemAttributesToResourceNEW(final A
    // itemAttr, final R resource) {
    // boolean changed = compareItemAttributesToResource(itemAttr, resource);
    // if (changed) {
    // return CoreItemAttributes.ChangeStatus.CHANGED_UPDATE;
    // } else {
    // return CoreItemAttributes.ChangeStatus.UNCHANGED;
    // }
    // }

    /**
     * Method called on every collection cycle; should simply set any detected relationships. Any
     * delta between those newly set relationships and with previous relationships detected in the
     * previous cycle are created automatically by AdapterItem and AggregationUpdater. This method
     * is called in step 1 of the collection cycle (within updateItems()) when any changes to
     * recently collected Items are kept inside delta objects that will be applied later. For
     * instance, calling item.getCore() from within that method will return the value before any
     * delta being applied and may reflect an pout of date value.
     *
     * @param connectedItem on which to set relationships, i.e. the item matching the quoted
     *        resource. ConnectedItem is an interface restricting the methods available at stage of
     *        processing and is implemented by T.
     * @param resource may hold links to other resources which can be used to set relationships
     */
    protected abstract void setRelationships(ConnectedItem connectedItem, R resource);

    /**
     * @return the newRelationshipsMap
     */
    public HashMap<String, String> getNewRelationshipsMap() {
        return newRelationshipsMap;
    }

    /**
     * Should we include the item in the relationships.
     *
     * @param item Item to check
     * @return true if the item should be in the relationships
     */
    public boolean includeInRelationships(Item item) {
        return true;
    }

    /**
     * Should we include the item in the result.
     *
     * @param item Item to check
     * @return true if the result should be included
     */
    public boolean includeInResult(Item item) {
        return true;
    }

}
