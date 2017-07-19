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

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * Wrapper for the Set of ItemTypes.
 */
@JsonAutoDetect
public class ItemTypeList {
    private Set<ItemType> itemTypes;

    /**
     * No-arg constructor for JSON serialisation.
     */
    public ItemTypeList() {
        itemTypes = new HashSet<>();
    }

    /**
     * @param itemTypes Set of itemTypes
     */
    public ItemTypeList(final Set<ItemType> itemTypes) {
        this.itemTypes = itemTypes;
    }

    /**
     * Returns the Set of ItemTypes.
     *
     * @return the itemTypes
     */
    public Set<ItemType> getItemTypes() {
        return itemTypes;
    }

    /**
     * @param itemTypes the Set of ItemTypes.
     */
    public void setItemTypes(final Set<ItemType> itemTypes) {
        this.itemTypes = itemTypes;
    }
}
