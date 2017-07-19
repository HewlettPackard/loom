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
import com.hp.hpl.loom.adapter.hpcloud.itemtype.VmType;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;

@ItemTypeInfo(VmType.TYPE_LOCAL_ID)
@ConnectedTo(toClass = HostItem.class)
public class VmItem extends HpCloudItem<VmItemAttributes> {

    public VmItem(final String logicalId, final ItemType type) {
        super(logicalId, type);
        this.setName(logicalId);
    }

    public VmItem(final String logicalId, final ItemType type, final String name) {
        super(logicalId, type, name);
        // TODO Auto-generated constructor stub
    }

    public VmItem(final String logicalId, final ItemType type, final String name, final String description) {
        super(logicalId, type, name, description);
        // TODO Auto-generated constructor stub
    }

    // public VmItem(final String logicalId, final ItemType type, final String name, final String
    // description,
    // final String id) {
    // super(logicalId, type, name, description, id);
    // // TODO Auto-generated constructor stub
    // }

    /*
     * @JsonIgnore
     * 
     * @Override public boolean isDifferentFrom(final Item oldItem) { VmItem fi = (VmItem) oldItem;
     * 
     * if (super.isDifferentFrom(oldItem)) { return true; }
     * 
     * // attr checks if ((getName() != null) && (!getName().equals(fi.getName()))) { return true; }
     * return false; }
     */

    @Override
    public String getQualifiedName() {
        return this.getName();
    }
}
