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
package com.hp.hpl.loom.exceptions;

/**
 * Throw whne the item type can't found.
 */
public class NoSuchItemTypeException extends CheckedLoomException {
    private String itemTypeId;

    /**
     * @param itemTypeId itemTypeId that wasn't found
     */
    public NoSuchItemTypeException(final String itemTypeId) {
        super("ItemType " + itemTypeId + " does not exist");
        this.itemTypeId = itemTypeId;
    }

    /**
     * @param itemTypeId itemTypeId that wasn't found
     * @param cause the cause
     */
    public NoSuchItemTypeException(final String itemTypeId, final Throwable cause) {
        super("ItemType " + itemTypeId + " does not exist", cause);
        this.itemTypeId = itemTypeId;
    }

    /**
     * Get the itemTypeId.
     *
     * @return the itemTypeId
     */
    public String getItemTypeId() {
        return itemTypeId;
    }
}
