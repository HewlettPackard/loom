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
import com.hp.hpl.loom.adapter.AdapterItem;
import com.hp.hpl.loom.model.CoreItemAttributes;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.relationships.RelationshipUtil;


public class OsItem<A extends CoreItemAttributes> extends AdapterItem<A> {

    private String qualifiedName = null;

    protected OsItem() {
        super();
    }

    public OsItem(final String logicalId, final ItemType type) {
        super(logicalId, type);
    }

    @Override
    @JsonIgnore
    public String getQualifiedName() {
        String thisToProjectRelName = RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(getProviderType(),
                getItemType().getLocalId(), OsProjectType.TYPE_LOCAL_ID);
        String thisToRegionRelName = RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(getProviderType(),
                getItemType().getLocalId(), OsRegionType.TYPE_LOCAL_ID);
        Item project = getFirstConnectedItemWithRelationshipName(thisToProjectRelName);
        Item region = getFirstConnectedItemWithRelationshipName(thisToRegionRelName);
        String prefix = null;
        if (project != null) {
            prefix = project.getName();
        }
        if (region != null) {
            if (prefix == null) {
                prefix = region.getName();
            } else {
                prefix += "/" + region.getName();
            }
        }
        if (prefix != null) {
            qualifiedName = prefix + "/" + getName();
        }

        // if (project == null) {
        // if (region == null) {
        // qualifiedName = null;
        // } else {
        // qualifiedName = region.getName() + "/" + getName();
        // }
        // } else {
        // qualifiedName = project.getName() + "/" + region.getName() + "/" + getName();
        // }
        return qualifiedName;
    }

}
