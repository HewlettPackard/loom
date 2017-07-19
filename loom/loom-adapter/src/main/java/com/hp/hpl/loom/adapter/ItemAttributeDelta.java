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

import com.hp.hpl.loom.model.CoreItemAttributes;
import com.hp.hpl.loom.model.SeparableItem;

/**
 * Specify changes to Item attributes via the delta mechanism. The referenced item must be included
 * in the updatedItems list of the corresponding {@link UpdateResult} for the Grounded Aggregation.
 * <p>
 * As for all delta operations, the {@link com.hp.hpl.loom.model.Item#update()} method of the
 * affected items, and those connected to the affected items, will be invoked during the
 * post-processing phase.
 * <p>
 *
 * @see UpdateDelta
 * @see UpdateResult
 * @see com.hp.hpl.loom.manager.adapter.AggregationManagement
 *
 * @param <A> The CoreItemAttribute subclass that this delta represents.
 */
public class ItemAttributeDelta<A extends CoreItemAttributes> {
    private SeparableItem<A> item;
    private A newItemAttributes;

    /**
     * Constructor.
     *
     * @param item The item to be updated.
     * @param newItemAttributes The new set of attributes for the item.
     */
    public ItemAttributeDelta(final SeparableItem<A> item, final A newItemAttributes) {
        this.item = item;
        this.newItemAttributes = newItemAttributes;
    }

    /**
     * Return the item to be updated.
     *
     * @return The item.
     */
    public SeparableItem<A> getItem() {
        return item;
    }

    /**
     * Return the new set of attributes for the item.
     *
     * @return The new set of attributes for the item.
     */
    public A getNewItemAttributes() {
        return newItemAttributes;
    }

    /**
     * Apply the change.
     */
    public void apply() {
        item.setCore(newItemAttributes);
    }
}
