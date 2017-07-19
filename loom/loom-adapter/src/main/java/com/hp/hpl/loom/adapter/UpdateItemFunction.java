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
 * Allow a recalculation of attributes of an Item.
 */
@FunctionalInterface
public interface UpdateItemFunction {
    /**
     * Update an item and return true if the attributes of an Item were actually changed.
     *
     * @param item The item to be updated.
     * @return True if the Item was actually changed.
     */
    boolean update(Item item);
}
