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

/**
 * Interface exposed by a stitching function to return a collection of items that are equivalent to
 * another item.
 */
public interface ItemEquivalence {
    /**
     * Return the collection of items that are equivalent to the specified item.
     *
     * @param item The item to be compared for equivalence.
     * @return The collection of equivalent items.
     */
    Collection<Item> getEquivalentItems(Item item);
}
