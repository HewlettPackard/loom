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
package com.hp.hpl.loom.model;

import com.hp.hpl.loom.adapter.annotations.LoomAttribute;
import com.hp.hpl.loom.manager.query.DefaultOperations;



/**
 * Base class to hold additional Item attributes for a {@link SeparableItem}. Note that if
 * {@link #getItemName()} or {@link #getItemDescription()} return a non-null value, then the value
 * will replace the name and description of the {@link SeparableItem} to which it becomes attached.
 * An Adapter must set the <i>id</i> attribute of a {@link SeparableItem} to a unique string value
 * for that item.
 */
public class CoreItemAttributes {

    /**
     * Reflects the nature of the changes between two data collections.
     */
    public static enum ChangeStatus {
        /**
         * no change.
         */
        UNCHANGED,
        /**
         * attributes have changed but none are significant from the point of view of queries and
         * cache invalidation. Applies to metrics and fast changing attributes.
         */
        CHANGED_IGNORE,
        /**
         * at least one attribute has changed that may impact existing queries. The grounded
         * aggregation should be marked dirty and related cached derived aggregations should be
         * recalculated.
         */
        CHANGED_UPDATE
    };

    @LoomAttribute(key = "Item Name", supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
    private String itemName;

    @LoomAttribute(key = "Item Description",
            supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
    private String itemDescription;

    @LoomAttribute(key = "Item Id", supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
    private String itemId;

    /**
     * No-arg constructor for JSON serialisation.
     */
    public CoreItemAttributes() {}

    /**
     * Convenience constructor.
     *
     * @param itemName Name of the Item.
     * @param itemId Unique ID of the Item.
     */
    public CoreItemAttributes(final String itemName, final String itemId) {
        this.itemName = itemName;
        this.itemId = itemId;
    }

    /**
     * Convenience constructor.
     *
     * @param itemName Name of the Item.
     * @param itemId Unique ID of the Item.
     * @param itemDescription Description of the Item.
     */
    public CoreItemAttributes(final String itemName, final String itemId, final String itemDescription) {
        this.itemName = itemName;
        this.itemId = itemId;
        this.itemDescription = itemDescription;
    }

    /**
     * Getter for name.
     *
     * @return Name of the Item.
     */
    public final String getItemName() {
        return itemName;
    }

    /**
     * Setter for name.
     *
     * @param name Name of the Item.
     */
    public final void setItemName(final String name) {
        itemName = name;
    }

    /**
     * Getter for Item ID.
     *
     * @return ID of the Item.
     */
    public final String getItemId() {
        return itemId;
    }

    /**
     * Setter for Item ID.
     *
     * @param itemId ID of the Item.
     */
    public final void setItemId(final String itemId) {
        this.itemId = itemId;
    }

    /**
     * Getter for description.
     *
     * @return Description of the Item.
     */
    public final String getItemDescription() {
        return itemDescription;
    }

    /**
     * Setter for description.
     *
     * @param description Description of the Item.
     */
    public final void setItemDescription(final String description) {
        itemDescription = description;
    }
}
