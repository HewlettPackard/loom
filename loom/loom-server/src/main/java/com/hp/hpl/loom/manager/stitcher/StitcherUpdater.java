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
package com.hp.hpl.loom.manager.stitcher;

import java.util.Collection;

import com.hp.hpl.loom.model.Item;

public interface StitcherUpdater {
    /**
     * Update a set of items maintained by the Stitcher, for a specific Grounded Aggregation. The
     * set of items passed in this method are all of the same type, and from a single provider.
     * <p>
     * The method contains not only the current state of all items know to the provider (alItems),
     * but also an encoding of the differences that have occurred since these items were last
     * updated. This difference is encoded as the set of new items (newItems), items that have
     * changed in some way (updatedItems), and those that have been deleted (deletedItems).
     * <p>
     * The method performs stitching between the items passed as parameters of this method, and
     * items with other typeId or providerId that have been previously given to the stitcher. The
     * method returns return any items, in other collections with different typeIds or providerIds,
     * that have been modified as a result of the stitching process. The method should not return
     * items that have have been included in the newItems, updatedItems, or deletedItems because
     * these items are already known to be modified.
     *
     * @param typeId The ID of the type of all the items.
     * @param providerId The ID of the provider of the collection of items.
     * @param allItems The complete set of items of this type known to the provider.
     * @param newItems Items that have been created since the last update.
     * @param updatedItems Items that have been updated since the last update.
     * @param deletedItems Items that have been updated since the last update.
     * @return Any items, in other collections with different typeIds or providerIds, that have been
     *         modified as a result of the stitching process.
     */
    Collection<Item> stitchItems(String typeId, String providerId, Collection<Item> allItems, Collection<Item> newItems,
            Collection<Item> updatedItems, Collection<Item> deletedItems);

    /**
     * Notification that all items for the specific typeId and providerId have been removed. All
     * associated stitches should also be removed.
     *
     * @param typeId The Item Type ID of the items that have been removed.
     * @param providerId The provider ID of the items that have been removed.
     */
    void removeStitchedItems(String typeId, String providerId);
}
