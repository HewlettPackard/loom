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
 * Thrown when a ItemProperty is not found.
 */
public class ItemPropertyNotFound extends CheckedLoomException {

    /**
     * @param property the property
     */
    public ItemPropertyNotFound(final String property) {
        super("Item " + property + " does not exist");
    }

    /**
     * @param itemId the itemId
     * @param cause the cause
     */
    public ItemPropertyNotFound(final String itemId, final Throwable cause) {
        super("Item " + itemId + " does not exist", cause);
    }
}
