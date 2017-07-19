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
 * Thrown when attempt to register a duplicate ItemType.
 */
public class DuplicateItemTypeException extends CheckedLoomException {
    private String itemTypeId;

    /**
     * @param itemTypeId the itemTypeId
     */
    public DuplicateItemTypeException(final String itemTypeId) {
        super("ItemType# " + itemTypeId + " already exists");
        this.itemTypeId = itemTypeId;
    }

    /**
     * @param itemTypeId the itemTypeId
     * @param cause the cause
     */
    public DuplicateItemTypeException(final String itemTypeId, final Throwable cause) {
        super("ItemType# " + itemTypeId + " already exists", cause);
        this.itemTypeId = itemTypeId;
    }

    /**
     * The itemTypeId.
     *
     * @return the itemTypeId
     */
    public String getItemTypeId() {
        return itemTypeId;
    }
}
