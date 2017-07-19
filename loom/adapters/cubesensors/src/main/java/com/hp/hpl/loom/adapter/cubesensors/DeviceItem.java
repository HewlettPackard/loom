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
package com.hp.hpl.loom.adapter.cubesensors;

import com.hp.hpl.loom.adapter.annotations.ItemTypeInfo;
import com.hp.hpl.loom.adapter.annotations.Sort;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.model.ItemType;

/**
 * DeviceItem represents a deviceItem.
 *
 */
@ItemTypeInfo(value = Types.DEVICE_TYPE_LOCAL_ID, sorting = {
        @Sort(operation = DefaultOperations.SORT_BY, fieldOrder = {"Size", "Path", "Filename", "Directory", "Value"}),
        @Sort(operation = DefaultOperations.GROUP_BY, fieldOrder = {"Directory"})})
public class DeviceItem extends BaseItem<DeviceItemAttributes> {

    /**
     * Default constructor.
     *
     * @param itemType The item type
     */
    public DeviceItem(final ItemType itemType) {
        super(null, itemType);
    }

    /**
     * Constructs a DeviceItem using the provided logicalId and itemType.
     *
     * @param logicalId The logical id
     * @param itemType The itemType
     */
    public DeviceItem(final String logicalId, final ItemType itemType) {
        super(logicalId, itemType);
    }
}
