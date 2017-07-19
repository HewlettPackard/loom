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

import com.hp.hpl.loom.model.Item;

/**
 * Data Structure used to encode the updates to be applied to a Grounded Aggregation. In both Full
 * and Delta Modes, the following data structures contain lists of the Items that are affected by
 * the update.
 * <p>
 * <ul>
 * <li>allItems - The complete list of Items contained in the Grounded Aggregation.
 * <li>newItems - The sub-set of allItems that are new.
 * <li>updatedItems - The sub-set of allItems that have been updated, or will be updated by the
 * application of an ItemRelationsDelta or ItemAttributeDelta.
 * <li>deletedItems - The set of Items that have been deleted since the last update, and no longer
 * appear in allItems.
 * </ul>
 * <p>
 * If a Grounded Aggregation is to be updated in Full Mode, then the above attributes are all that
 * are required. The Adapter should construct a fully connected set of objects in allItems that
 * optionally connect to other GAs, and pass the items in this data structure.
 * <p>
 * To use the Delta Mode, a non-null {@link UpdateDelta} object should either be passed in the
 * constructor, or set via {@link #setUpdateDelta(UpdateDelta)}. Delta Mode can be forced by calling
 * {@link #setDeltaMode(boolean)}. If Delta Mode is active, any delta instructions specified in the
 * {@link UpdateDelta} will first be applied to the set of objects specified in allItems to apply
 * changes to attributes and connected relationships. If Delta Mode is active, the "post-processing"
 * phase of the call to update all Grounded Aggregations will be triggered after all delta update
 * instructions have been applied to all aggregations.
 * <p>
 * In Delta Mode the updatedItems list must also contain the list of items that will be affected by
 * the application of any ItemRelationsDelta or ItemAttributeDelta delta operations. In the case of
 * ItemRelationsDelta operations, the operation itself may actually be included in another
 * UpdateResult to be applied on a different Grounded Aggregation. For example, if the connected
 * relationships of item <i>a</i> in GA <i>A</i> are to be cleared by an ItemRelationsDelta
 * associated with GA <i>A</i>, then not only must <i>a</i> appear in the updatedItems list of the
 * UpdateResult for GA <i>A</i>, but also any other items <i>b</i> (in GA <i>B</i>) that <i>a</i>
 * was previously connected to must also appear in the updatedItems list of the UpdateResult for GA
 * <i>B</i>, even though no delta operation is actually specified for GA <i>B</i>. Note that, by
 * contrast, there is no requirement to include affected items <i>b</i> in the updatedItems list of
 * the UpdateResult for GA <i>B</i> for the case of an item deletion delta (ItemDeletionDelta) for
 * <i>a</i>.
 * <p>
 *
 * @see UpdateDelta
 */
public class UpdateResult {

    private ArrayList<Item> allItems;
    private ArrayList<Item> newItems;
    private ArrayList<Item> updatedItems;
    private ArrayList<Item> deletedItems;

    //
    // Delta attributes
    private UpdateDelta updateDelta;
    private boolean deltaMode = false;

    private boolean ignore = true;

    /**
     * Construct an empty UpdateResult, with Delta Mode set to false.
     *
     * @param expectedSize Initial capacity of the empty lists created for items.
     */
    public UpdateResult(final int expectedSize) {
        allItems = new ArrayList<Item>(expectedSize);
        newItems = new ArrayList<Item>(expectedSize);
        updatedItems = new ArrayList<Item>(expectedSize);
        deletedItems = new ArrayList<Item>(expectedSize);
    }

    /**
     * Construct an UpdateResult, with the specified set of items, and Delta Mode set to false.
     *
     * @param allItems - The complete list of Items contained in the Grounded Aggregation.
     * @param newItems - The sub-set of allItems that are new.
     * @param updatedItems - The sub-set of allItems that have been updated.
     * @param deletedItems - The set of Items that have been deleted since the last update, and no
     *        longer appear in allItems.
     */
    public UpdateResult(final ArrayList<Item> allItems, final ArrayList<Item> newItems,
            final ArrayList<Item> updatedItems, final ArrayList<Item> deletedItems) {
        this.allItems = allItems;
        this.newItems = newItems;
        this.updatedItems = updatedItems;
        this.deletedItems = deletedItems;
        if (!newItems.isEmpty() || !updatedItems.isEmpty() || !deletedItems.isEmpty()) {
            ignore = false;
        }
    }

    /**
     * Construct an UpdateResult, with the specified set of items, a list of delta instructions to
     * be applied to allItems, and Delta Mode set to true.
     *
     * @param allItems - The complete list of Items contained in the Grounded Aggregation.
     * @param newItems - The sub-set of allItems that are new.
     * @param updatedItems - The sub-set of allItems that have been updated.
     * @param deletedItems - The set of Items that have been deleted since the last update, and no
     *        longer appear in allItems.
     * @param updateDelta List of delta instructions to be applied to allItems.
     */
    public UpdateResult(final ArrayList<Item> allItems, final ArrayList<Item> newItems,
            final ArrayList<Item> updatedItems, final ArrayList<Item> deletedItems, final UpdateDelta updateDelta) {
        this(allItems, newItems, updatedItems, deletedItems);
        this.updateDelta = updateDelta;
        deltaMode = true;
    }

    /**
     * Gets allItems.
     *
     * @return a list of all the items
     */
    public ArrayList<Item> getAllItems() {
        return allItems;
    }

    /**
     * Clears the allItems array.
     */
    public void clearAllItems() {
        allItems.clear();
    }

    /**
     * Sets the allItems list.
     *
     * @param allItems list of items to set
     */
    public void setAllItems(final ArrayList<Item> allItems) {
        this.allItems = allItems;
    }

    /**
     * Gets the new Items.
     *
     * @return returns the new items.
     */
    public ArrayList<Item> getNewItems() {
        return newItems;
    }

    /**
     * Gets the updated items.
     *
     * @return returns the updated items.
     */
    public ArrayList<Item> getUpdatedItems() {
        return updatedItems;
    }

    /**
     * Clears the updated items.
     */
    public void clearUpdatedItems() {
        updatedItems.clear();
    }

    /**
     * Returns all the deletedItems.
     *
     * @return the deletedItems.
     */
    public ArrayList<Item> getDeletedItems() {
        return deletedItems;
    }

    /**
     * Add an item to the allItems list.
     *
     * @param item item to add
     * @return flag indicating if it was added (as specified by {@link java.util.Collection#add}
     */
    public boolean addToAll(final Item item) {
        return allItems.add(item);
    }

    /**
     * Add an item to the newItems list.
     *
     * @param item item to add
     * @return flag indicating if it was added (as specified by {@link java.util.Collection#add}
     */
    public boolean addToNew(final Item item) {
        return newItems.add(item);
    }

    /**
     * Add an item to the updatedItems list.
     *
     * @param item item to add
     * @return flag indicating if it was added (as specified by {@link java.util.Collection#add}
     */
    public boolean addToUpdated(final Item item) {
        return updatedItems.add(item);
    }

    /**
     * Add an item to the deletedItems list.
     *
     * @param item item to add
     * @return flag indicating if it was added (as specified by {@link java.util.Collection#add}
     */
    public boolean addToDeleted(final Item item) {
        return deletedItems.add(item);
    }

    /**
     * Returns a string based on the allItems list.
     *
     * @return String based on the contents of the allItems list
     */
    public String getAllAsString() {
        StringBuffer stBuf = new StringBuffer();
        for (Item item : allItems) {
            stBuf.append("id: " + item.getLogicalId() + "\n");
        }
        return stBuf.toString();
    }

    /**
     * Gets a string representing a summary of this result.
     *
     * @return String representing the update results.
     */
    public String getSummary() {
        StringBuffer stBuf = new StringBuffer();
        stBuf.append("all: [" + allItems.size() + "] new: [" + newItems.size() + "] updated: [" + updatedItems.size()
                + "] deleted: [" + deletedItems.size() + "]\n");
        if (updateDelta != null) {
            stBuf.append("delta: " + updateDelta.getSummary());
        }
        return stBuf.toString();
    }

    /**
     * Return The List of delta instructions to be applied to allItems, or null if not set.
     *
     * @return The List of delta instructions to be applied to allItems.
     */
    public UpdateDelta getUpdateDelta() {
        return updateDelta;
    }

    /**
     * Set the UpdateDelta of the update - the List of delta instructions to be applied to allItems.
     * If updateDelta is not null, then the Delta Mode of the update is set to true.
     *
     * @param updateDelta UpdateDelta of the update.
     */
    public void setUpdateDelta(final UpdateDelta updateDelta) {
        this.updateDelta = updateDelta;
        deltaMode = updateDelta != null;
    }

    /**
     * Gets or creates a {@link UpdateDelta}.
     *
     * @return the updateDelta
     */
    public UpdateDelta getOrCreateUpdateDelta() {
        if (updateDelta == null) {
            updateDelta = new UpdateDelta();
        }
        return updateDelta;
    }

    /**
     * Return true if the update is in Delta Mode.
     *
     * @return true if the update is in Delta Mode.
     */
    public boolean isDeltaMode() {
        return deltaMode;
    }

    /**
     * Explicitly set the Delta Mode of the update.
     *
     * @param deltaMode Delta Mode of the update.
     */
    public void setDeltaMode(final boolean deltaMode) {
        this.deltaMode = deltaMode;
    }

    /**
     * @return true only if updateResult carries only changes that does not dirty the related
     *         GroundedAggregation.
     */
    public boolean isIgnore() {
        return ignore;
    }

    /**
     * Explicitly set the ignore boolean of the update.
     *
     * @param ignore ignore value for the update.
     */
    public void setIgnore(final boolean ignore) {
        this.ignore = ignore;
    }
}
