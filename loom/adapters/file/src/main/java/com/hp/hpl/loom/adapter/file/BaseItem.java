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
package com.hp.hpl.loom.adapter.file;

import com.hp.hpl.loom.adapter.AdapterItem;
import com.hp.hpl.loom.model.CoreItemAttributes;
import com.hp.hpl.loom.model.ItemType;

/**
 * BaseItem for the FileAdapter extended by the FileItem and DirItem.
 *
 * @param <A>
 */
public class BaseItem<A extends CoreItemAttributes> extends AdapterItem<A> {
    protected BaseItem() {
        super();
    }

    /**
     * Constructs a BaseItem.
     *
     * @param logicalId the logical Id
     * @param type the ItemType
     */
    public BaseItem(final String logicalId, final ItemType type) {
        super(logicalId, type);
    }
}
