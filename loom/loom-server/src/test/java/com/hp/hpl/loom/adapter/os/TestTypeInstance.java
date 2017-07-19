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

import com.hp.hpl.loom.adapter.annotations.ConnectedTo;
import com.hp.hpl.loom.adapter.annotations.ItemTypeInfo;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.SeparableItem;
import com.hp.hpl.loom.relationships.RelationshipUtil;

/**
 * Test Instance to test Connected Relationships via standard representation.
 */
@ItemTypeInfo(TestTypeInstance.TYPE_ID)
@ConnectedTo(toClass = TestTypeProject.class)
@ConnectedTo(toClass = TestTypeVolume.class)
public class TestTypeInstance extends SeparableItem<TestTypeInstanceAttributes> {

    public static final String TYPE_ID = "instance";

    private int numProjects;
    private int numVolumes;

    public TestTypeInstance() {
        super();
    }

    public TestTypeInstance(final String logicalId, final String name, final String projectId, final String deviceName,
            final ItemType type) {
        super(logicalId, type, name);
        TestTypeInstanceAttributes core = new TestTypeInstanceAttributes();
        core.setProjectId(projectId);
        core.setDeviceName(deviceName);
        this.setCore(core);
    }

    @Override
    public boolean update() {
        int oldNumProjects = numProjects;
        int oldNumVolumes = numVolumes;
        numProjects = getNumConnectedItemsWithRelationshipName(RelationshipUtil
                .getRelationshipNameBetweenLocalTypeIds(getProviderType(), TYPE_ID, TestTypeProject.TYPE_ID));
        numVolumes = getNumConnectedItemsWithRelationshipName(RelationshipUtil
                .getRelationshipNameBetweenLocalTypeIds(getProviderType(), TYPE_ID, TestTypeVolume.TYPE_ID));
        return oldNumProjects != numProjects || oldNumVolumes != numVolumes;
    }

    public int getNumProjects() {
        return numProjects;
    }

    public int getNumVolumes() {
        return numVolumes;
    }
}
