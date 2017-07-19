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
package com.hp.hpl.loom.adapter.os.swift;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hp.hpl.loom.adapter.annotations.ConnectedTo;
import com.hp.hpl.loom.adapter.annotations.ItemTypeInfo;
import com.hp.hpl.loom.adapter.os.OsItem;
import com.hp.hpl.loom.adapter.os.OsProject;
import com.hp.hpl.loom.adapter.os.OsRegion;
import com.hp.hpl.loom.adapter.os.OsRegionType;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.relationships.RelationshipUtil;

@ItemTypeInfo(OsAccountType.TYPE_LOCAL_ID)
@ConnectedTo(toClass = OsProject.class)
@ConnectedTo(toClass = OsContainer.class)
@ConnectedTo(toClass = OsRegion.class)
public class OsAccount extends OsItem<OsAccountAttributes> {

    protected long bytesUsed;
    protected long containerCount;
    protected long objectCount;

    private OsAccount() {
        super();
    }

    public OsAccount(final String logicalId, final ItemType itemType) {
        super(logicalId, itemType);
    }

    @JsonIgnore
    public String getRegionId(final Provider provider) {
        String thisToRegionRelName = RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(provider.getProviderType(),
                getItemType().getLocalId(), OsRegionType.TYPE_LOCAL_ID);
        return this.getRelationshipsIds().get(thisToRegionRelName).iterator().next();
    }
}
