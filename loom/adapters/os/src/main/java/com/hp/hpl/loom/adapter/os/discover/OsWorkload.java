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
package com.hp.hpl.loom.adapter.os.discover;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hp.hpl.loom.adapter.annotations.ConnectedTo;
import com.hp.hpl.loom.adapter.annotations.ItemTypeInfo;
import com.hp.hpl.loom.adapter.os.OsInstance;
import com.hp.hpl.loom.adapter.os.OsItem;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;

@ItemTypeInfo(OsWorkloadType.TYPE_LOCAL_ID)
@ConnectedTo(toClass = OsInstance.class)
public class OsWorkload extends OsItem<OsWorkloadAttributes> {

    @JsonIgnore
    private String projectName;
    @JsonIgnore
    private String regionName;

    private OsWorkload() {
        super();
    }

    public OsWorkload(final String logicalId, final ItemType workloadType) {
        super(logicalId, workloadType);
    }

    public void setRegionName(final String regionName) {
        this.regionName = regionName;
    }

    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    @JsonIgnore
    @Override
    public String getQualifiedName() {
        String qn = null;
        if (regionName != null && projectName != null) {
            qn = projectName + "/" + regionName;
        }
        /*
         * if (testInstance != null) { qn =
         * testInstance.getFirstConnectedItemWithRelationshipName(RelationshipUtil
         * .getRelationshipNameBetweenLocalTypeIds(OsInstanceType.TYPE_LOCAL_ID,
         * OsProjectType.TYPE_LOCAL_ID)) + "/" +
         * testInstance.getFirstConnectedItemWithRelationshipName(RelationshipUtil
         * .getRelationshipNameBetweenLocalTypeIds(OsInstanceType.TYPE_LOCAL_ID,
         * OsRegionType.TYPE_LOCAL_ID)) + "/" + getName(); }
         */
        return qn;
    }
}
