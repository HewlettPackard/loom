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

import com.hp.hpl.loom.model.Item;

/**
 * Specify Item deletion via the delta mechanism. By implication, all previously existing
 * relationships that the Item was involved in will also be deleted.
 * <p>
 * As for all delta operations, the {@link Item#update()} method of the affected items (those that
 * were previously connected to the deleted item), and those connected to the affected items, will
 * be invoked during the post-processing phase.
 * <p>
 *
 * @see UpdateDelta
 * @see UpdateResult
 * @see com.hp.hpl.loom.manager.adapter.AggregationManagement
 */
public class ItemDeletionDelta {
    private Item item;

    /**
     * Constructor for the ItemDeletionDelta from an {@link Item}.
     *
     * @param item Item to construct from
     */
    public ItemDeletionDelta(final Item item) {
        this.item = item;
    }

    /**
     * Gets the item for the ItemDeletionDelta.
     *
     * @return returns the Item
     */
    public Item getItem() {
        return item;
    }

}
