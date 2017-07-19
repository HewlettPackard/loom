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
package com.hp.hpl.loom.adapter.os;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hp.hpl.loom.adapter.annotations.ConnectedTo;
import com.hp.hpl.loom.adapter.annotations.ItemTypeInfo;
import com.hp.hpl.loom.adapter.annotations.Root;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;

@Root
@ItemTypeInfo(OsRegionType.TYPE_LOCAL_ID)
@ConnectedTo(toClass = OsSubnet.class)
@ConnectedTo(toClass = OsProject.class)
@ConnectedTo(toClass = OsInstance.class)
@ConnectedTo(toClass = OsNetwork.class)
@ConnectedTo(toClass = OsImage.class)
@ConnectedTo(toClass = OsVolume.class)
public class OsRegion extends OsItem<OsRegionAttributes> {

    private OsRegion() {
        super();
    }

    public OsRegion(final String logicalId, final ItemType imageType) {
        super(logicalId, imageType);
    }

    @JsonIgnore
    @Override
    public String getQualifiedName() {
        return "/" + getCore().getItemName();
    }

}
