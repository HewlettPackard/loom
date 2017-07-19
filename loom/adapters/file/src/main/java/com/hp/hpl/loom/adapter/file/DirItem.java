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

import com.hp.hpl.loom.adapter.annotations.ConnectedTo;
import com.hp.hpl.loom.adapter.annotations.ItemTypeInfo;
import com.hp.hpl.loom.model.ItemType;

/**
 * DirItem which represents a Directory.
 *
 */
@ItemTypeInfo(Types.DIR_TYPE_LOCAL_ID)
@ConnectedTo(toClass = FileItem.class, type = "contains", typeName = "Contains")
@ConnectedTo(toClass = FileItem.class, type = "ancestor", typeName = "Ancestor")
public class DirItem extends BaseItem<DirItemAttributes> {

    /**
     * Default constructor.
     */
    public DirItem() {
        super();
    }

    /**
     * Constructs a DirItem from a logicalId and itemType.
     *
     * @param logicalId The logicalId
     * @param itemType The itemType
     */
    public DirItem(final String logicalId, final ItemType itemType) {
        super(logicalId, itemType);
    }

    @Override
    public String getQualifiedName() {
        return getCore().getDirname();
    };
}
