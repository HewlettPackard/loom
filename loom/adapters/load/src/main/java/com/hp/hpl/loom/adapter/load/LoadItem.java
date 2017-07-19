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
package com.hp.hpl.loom.adapter.load;

import com.hp.hpl.loom.adapter.AdapterItem;
import com.hp.hpl.loom.model.ItemType;

public class LoadItem extends AdapterItem<LoadItemAttributes> {
    /**
     * Default constructor.
     *
     * @param itemType The item type
     */
    public LoadItem(final ItemType itemType) {
        super(null, itemType);
    }

    /**
     * Constructs a FileItem using the provided logicalId and itemType.
     *
     * @param logicalId The logical id
     * @param itemType The itemType
     */
    public LoadItem(final String logicalId, final ItemType itemType) {
        super(logicalId, itemType);
    }


    @Override
    public Object getPropertyValueForName(final String name) {
        return getCore().getAttributeValue(name);
    }

}
