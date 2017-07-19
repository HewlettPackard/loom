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
package com.hp.hpl.loom.adapter.hpcloud.item;

import com.hp.hpl.loom.adapter.annotations.ConnectedTo;
import com.hp.hpl.loom.adapter.annotations.ItemTypeInfo;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.ChefOrgType;
import com.hp.hpl.loom.model.ItemType;

@ItemTypeInfo(ChefOrgType.TYPE_LOCAL_ID)
@ConnectedTo(toClass = ChefClientItem.class)
@ConnectedTo(toClass = ChefEnvironmentItem.class)
@ConnectedTo(toClass = ChefNodeItem.class)
@ConnectedTo(toClass = ChefUploadedCookbookItem.class)
@ConnectedTo(toClass = ChefUserItem.class)
public class ChefOrgItem extends HpCloudItem<ChefOrgItemAttributes> {

    public ChefOrgItem(final String logicalId, final ItemType type) {
        super(logicalId, type);
        // TODO Auto-generated constructor stub
    }

    public ChefOrgItem(final String logicalId, final ItemType type, final String name) {
        super(logicalId, type, name);
        // TODO Auto-generated constructor stub
    }

    public ChefOrgItem(final String logicalId, final ItemType type, final String name, final String description) {
        super(logicalId, type, name, description);
        // TODO Auto-generated constructor stub
    }

    // public ChefOrgItem(final String logicalId, final ItemType type, final String name, final
    // String description,
    // final String id) {
    // super(logicalId, type, name, description, id);
    // // TODO Auto-generated constructor stub
    // }

    // @Override
    // public String getQualifiedName() {
    // return get_id();
    // }

    // Saul, TODO: Review whether this method should be implemented or not
    // @JsonIgnore
    // @Override
    // public boolean isDifferentFrom(final Item oldItem) {
    // return true;
    // }

}
