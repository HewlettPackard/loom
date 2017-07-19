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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hp.hpl.loom.model.CoreItemAttributes;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.SeparableItem;
import com.hp.hpl.loom.relationships.RelationshipUtil;

/**
 * A base AdapterItem contains the relationships, creation time, status.
 *
 * @param <A> CoreAttributes that should be associated.
 */
public class AdapterItem<A extends CoreItemAttributes> extends SeparableItem<A> implements ConnectedItem {
    private static final int HASHMAP_SIZE = 20;
    private static final double HASHSET_PERCENT = 0.75;
    protected static final String UNCHANGED = "unchanged";
    protected static final String NEW = "new";
    protected static final String UPDATED = "updated";
    protected static final String DELETED = "deleted";
    protected static final String UNSET = "unset";

    // private static final Log LOG = LogFactory.getLog(AdapterItem.class);

    @JsonIgnore
    private Map<String, Collection<String>> relationshipsIds = new HashMap<>(HASHMAP_SIZE);
    @JsonIgnore
    private Set<String> relnameSet = new HashSet<>();
    @JsonIgnore
    private String updateStatus = UNCHANGED;
    @JsonIgnore
    private ItemCollector itemCollector;
    @JsonIgnore
    private long creationEpoch = 0;

    protected AdapterItem() {
        super();
    }

    /**
     * Minimal set of parameters to pass in constructor.
     *
     * @param logicalId Logical ID of the item.
     * @param type Type of the item. The ID and local ID of the type must first have been correctly
     *        set.
     */
    public AdapterItem(final String logicalId, final ItemType type) {
        super(logicalId, type);
    }


    /**
     * Convenience constructor, with name for the item.
     *
     * @param logicalId Logical ID of the item.
     * @param type Type of the item. The ID and local ID of the type must first have been correctly
     *        set.
     * @param name Name of the item.
     */
    public AdapterItem(final String logicalId, final ItemType type, final String name) {
        super(logicalId, type, name);
    }

    /**
     * Convenience constructor, with name and description for the item.
     *
     * @param logicalId Logical ID of the item.
     * @param type Type of the item. The ID and local ID of the type must first have been correctly
     *        set.
     * @param name Name of the item.
     * @param description Description of the item.
     */
    public AdapterItem(final String logicalId, final ItemType type, final String name, final String description) {
        super(logicalId, type, name, description);
    }

    /**
     * Set the itemCollector.
     *
     * @param itemCollector the item collector
     */
    public void setItemCollector(final ItemCollector itemCollector) {
        this.itemCollector = itemCollector;
    }

    /**
     * Test the matchCreationEpoch, returns true if the epochValue is the same as the creation time.
     *
     * @param epochValue The value to set
     * @return true if hasn't changed
     */
    public boolean matchCreationEpoch(final long epochValue) {
        return this.creationEpoch == epochValue;
    }

    /**
     * Set the creationEpoch.
     *
     * @param creationEpoch time to set
     */
    public void setCreationEpoch(final long creationEpoch) {
        this.creationEpoch = creationEpoch;
    }

    /**
     * Return the map of the relationship names to ids.
     *
     * @return the map of relationship names to ids
     */
    public Map<String, Collection<String>> getRelationshipsIds() {
        return relationshipsIds;
    }

    /**
     * Set the map relationship names to ids.
     *
     * @param relationshipsIds the map of relationships.
     */
    public void setRelationshipsIds(final Map<String, Collection<String>> relationshipsIds) {
        this.relationshipsIds = relationshipsIds;
    }

    /**
     * Get the Relname set.
     *
     * @return the relname set.
     */
    public Set<String> getRelnameSet() {
        return relnameSet;
    }

    /**
     * Set the relname set.
     *
     * @param relnameSet the relname set.
     */
    public void setRelnameSet(final Set<String> relnameSet) {
        this.relnameSet = relnameSet;
    }

    /**
     * Get the update status.
     *
     * @return the update status
     */
    public String getUpdateStatus() {
        return this.updateStatus;
    }

    /**
     * Set the update status.
     *
     * @param updateStatus the update status
     */
    public void setUpdateStatus(final String updateStatus) {
        this.updateStatus = updateStatus;
    }

    /**
     * Clear the update status.
     */
    public void clearUpdateStatus() {
        setUpdateStatus(UNSET);
    }

    /**
     * Check if the updateStatus is NEW.
     *
     * @return true if the updateStatus is NEW
     */
    public boolean isUpdateStatusNew() {
        return NEW.equals(this.updateStatus);
    }

    /**
     * Check if the updateStatus is UPDATED.
     *
     * @return true if the updateStatus is UPDATED
     */
    public boolean isUpdateStatusUpdated() {
        return UPDATED.equals(this.updateStatus);
    }

    /**
     * Check if the updateStatus is DELETED.
     *
     * @return true if the updateStatus is DELETED
     */
    public boolean isUpdateStatusDeleted() {
        return DELETED.equals(this.updateStatus);
    }

    /**
     * Check if the updateStatus is UNCHANGED.
     *
     * @return true if the updateStatus is UNCHANGED
     */
    public boolean isUpdateStatusUnchanged() {
        return UNCHANGED.equals(this.updateStatus);
    }

    /**
     * Compares Item based map with id based map to create a collection of Relationship deltas
     * listing what has changed between collection cycles. care must be taken when comparing two
     * maps since entries in item based one can be created through two different routes: 1. applying
     * ADD delta generated by entries added in this id based map through setRelationship method 2.
     * applying ADD delta generated by the item at the other end of the relationship link In other
     * words, a relationship may exist even though it is not explicitly set in this id based map.
     * This method returns Deltas of type clear even when all Item "others" have been deleted. It is
     * expected that the calling code actually post process those deltas and will chekc the status
     * of those Items. If all Items "others" are deleted, this delta could be ignored
     *
     * @return Collection of relationship deltas
     */
    public Collection<ItemRelationsDelta> createRelationshipsDeltasIDs() {
        ArrayList<ItemRelationsDelta> deltaList = null;
        Map<String, Map<String, Item>> connectedRels = this.getConnectedRelationships();
        // LOG.debug("EHUD_DEBUG: rel startup: " + getLogicalId() + " : " + connectedRels.size() +
        // " / "
        // + relnameSet.size() + "/" + relationshipsIds.size());
        // check if global clear
        if (relationshipsIds.isEmpty() && !relnameSet.isEmpty()) {
            boolean clearAll = true;
            for (String relName : connectedRels.keySet()) {
                if (!relnameSet.contains(relName)) {
                    clearAll = false;
                    break;
                }
            }
            if (clearAll) {
                deltaList = new ArrayList<>();
                deltaList.add(ItemRelationsDelta.clearAllItemRelations(this));
                return deltaList;
            }
        }
        // compare Ids with Items for add
        for (String relName : relationshipsIds.keySet()) {
            ArrayList<String> connIds = null;
            Collection<String> ids = relationshipsIds.get(relName);
            Map<String, Item> relMap = connectedRels.get(relName);
            for (String lid : ids) {
                if (relMap == null || !relMap.containsKey(lid)) {
                    // new one
                    if (connIds == null) {
                        connIds = new ArrayList<>();
                    }
                    connIds.add(lid);
                }
            }
            if (connIds != null && !connIds.isEmpty()) {
                if (deltaList == null) {
                    deltaList = new ArrayList<>();
                }
                deltaList.add(ItemRelationsDelta.addItemToOtherIdsNamedRelations(this, connIds, relName));
            }
        }
        // compare Items with Ids for clear/remove
        for (String relName : relnameSet) {
            Collection<String> ids = relationshipsIds.get(relName);
            if (ids == null) {
                if (deltaList == null) {
                    deltaList = new ArrayList<>();
                }
                deltaList.add(ItemRelationsDelta.clearAllItemNamedRelations(this, relName));
                continue;
            }
            Map<String, Item> relMap = connectedRels.get(relName);
            if (relMap == null) {
                continue;
            }
            ArrayList<String> others = null;
            for (String lid : relMap.keySet()) {
                if (!ids.contains(lid)) {
                    if (others == null) {
                        others = new ArrayList<>();
                    }
                    others.add(lid);
                }
            }
            if (others != null && !others.isEmpty()) {
                if (deltaList == null) {
                    deltaList = new ArrayList<>();
                }
                deltaList.add(ItemRelationsDelta.removeItemToOtherIdsNamedRelations(this, others, relName));
            }
        }
        return deltaList;
    }

    /**
     * Clear the relationship ids list.
     */
    public void clearRelationshipsIds() {
        relationshipsIds.clear();
    }

    @SuppressWarnings("checkstyle:linelength")
    @Override
    public void setRelationship(final Provider provider, final String itemTypeLocalId,
            final String connectedResourceId) {
        String relName = RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(provider.getProviderType(),
                getItemType().getLocalId(), itemTypeLocalId);
        setRelationship(relName, itemTypeLocalId, connectedResourceId);
    }

    // /**
    // * @param itemTypeLocalId itemTypeLocalId
    // * @param connectedResourceIds the collection of connected resources
    // */
    // public void setRelationship(final String itemTypeLocalId, final Collection<String>
    // connectedResourceIds) {
    // String relName =
    // RelationshipUtil
    // .getRelationshipNameBetweenLocalTypeIds(getItemType().getLocalId(), itemTypeLocalId, "");
    // for (String connectedResourceId : connectedResourceIds) {
    // setRelationship(relName, itemTypeLocalId, connectedResourceId);
    // }
    // }

    @SuppressWarnings("checkstyle:linelength")
    @Override
    public void setRelationshipWithType(final Provider provider, final String itemTypeLocalId,
            final String connectedResourceId, final String type) {
        String otherItemTypeId = itemTypeLocalId;
        String relName = RelationshipUtil.getRelationshipNameBetweenLocalTypeIdsWithRelType(provider.getProviderType(),
                getItemType().getLocalId(), otherItemTypeId, type);
        setRelationship(relName, itemTypeLocalId, connectedResourceId);
    }

    @SuppressWarnings("checkstyle:linelength")
    private void setRelationship(final String relationshipName, final String itemTypeLocalId,
            final String connectedResourceId) {

        relnameSet.add(relationshipName);
        Collection<String> ids = relationshipsIds.get(relationshipName);
        if (ids == null) {
            ids = new HashSet<String>();
            relationshipsIds.put(relationshipName, ids);
        }
        ids.add(itemCollector.getLogicalId(itemTypeLocalId, connectedResourceId));

        // Update the getRelationshipsDiscoveredOnCurrentUpdateCycle for this cycle, to allow
        // non-existing items
        // finding relationships to them in the same updatecycle

        HashMap<String, HashMap<String, String>> relationsOnCycle =
                this.itemCollector.getRelationshipsDiscoveredOnCurrentUpdateCycle();

        String connectToId = itemCollector.getLogicalId(itemTypeLocalId, connectedResourceId);
        String connectedFromId = this.getLogicalId();

        HashMap<String, String> toLocalIdMap = relationsOnCycle.get(connectToId);

        if (toLocalIdMap == null) {
            toLocalIdMap = new HashMap<String, String>();
        }

        toLocalIdMap.put(relationshipName, connectedFromId);

        relationsOnCycle.put(connectToId, toLocalIdMap);
    }

    // @Override
    // public void setRelationship(final String itemTypeLocalId, final Collection<String>
    // connectedResourceIds,
    // final String type) {
    // String relName =
    // RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(getItemType().getLocalId(),
    // itemTypeLocalId,
    // type);
    // for (String connectedResourceId : connectedResourceIds) {
    // setRelationship(relName, itemTypeLocalId, connectedResourceId);
    // }
    // }



    /**
     * Updates the list keeping track of which relationships have been set by adding a direct entry
     * in the id based map. This update is the only way to refresh this list and must be performed
     * at the start of every collection cycle ASSUMPTION: a relationship between two ItemTypes is
     * set the same way for all items using setRelationship: either all or no relationships are set
     * for a given relName using this method.
     */
    public void updateRelnameSet() {
        Set<String> newSet = new HashSet<>((int) ((relnameSet.size()) / HASHSET_PERCENT) + 1);
        Map<String, Map<String, Item>> connectedRels = this.getConnectedRelationships();
        for (String relName : relnameSet) {
            Map<String, Item> itemMap = connectedRels.get(relName);
            if (itemMap != null && !itemMap.isEmpty()) {
                newSet.add(relName);
            }
        }
        relnameSet = newSet;
    }
}
