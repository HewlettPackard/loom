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
import com.hp.hpl.loom.adapter.os.OsRegion;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.relationships.RelationshipUtil;

@ItemTypeInfo(OsContainerType.TYPE_LOCAL_ID)
@ConnectedTo(toClass = OsAccount.class)
@ConnectedTo(toClass = OsRegion.class)
@ConnectedTo(toClass = OsObject.class)
public class OsContainer extends OsItem<OsContainerAttributes> {

    private OsContainer() {
        super();
    }

    public OsContainer(final String logicalId, final ItemType itemType) {
        super(logicalId, itemType);
    }

    @Override
    @JsonIgnore
    public String getQualifiedName() {
        return getName();
    }

    @JsonIgnore
    public String getOsAccountId(final Provider provider) {
        String thisToAccountRelName = RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                provider.getProviderType(), getItemType().getLocalId(), OsAccountType.TYPE_LOCAL_ID);
        return this.getRelationshipsIds().get(thisToAccountRelName).iterator().next();
    }
}
