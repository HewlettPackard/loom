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
import com.hp.hpl.loom.adapter.annotations.Root;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.SeparableItem;
import com.hp.hpl.loom.relationships.RelationshipUtil;

/**
 * Test Project to test Connected Relationships via standard representation.
 */

@ItemTypeInfo(TestTypeProject.TYPE_ID)
@Root
@ConnectedTo(toClass = TestTypeVolume.class)
@ConnectedTo(toClass = TestTypeInstance.class)
@ConnectedTo(toClass = TestTypeUser.class, type = "admin")
@ConnectedTo(toClass = TestTypeUser.class, type = "access")
@ConnectedTo(toClass = TestTypeUser.class, type = "user")
public class TestTypeProject extends SeparableItem<TestTypeProjectAttributes> {

    public static final String TYPE_ID = "project";

    private int numInstances;
    private int numVolumes;

    public TestTypeProject() {
        super();
    }

    public TestTypeProject(final String logicalId, final String name, final String projectId, final int pid,
            final ItemType type) {
        super(logicalId, type, name);
        TestTypeProjectAttributes core = new TestTypeProjectAttributes();
        core.setProjectId(projectId);
        core.setPid(pid);
        this.setCore(core);
    }

    @Override
    public boolean update() {
        int oldNumInstances = numInstances;
        int oldNumVolumes = numVolumes;

        numInstances = getNumConnectedItemsWithRelationshipName(RelationshipUtil
                .getRelationshipNameBetweenLocalTypeIds(getProviderType(), TYPE_ID, TestTypeInstance.TYPE_ID));
        numVolumes = getNumConnectedItemsWithRelationshipName(RelationshipUtil
                .getRelationshipNameBetweenLocalTypeIds(getProviderType(), TYPE_ID, TestTypeVolume.TYPE_ID));
        return oldNumInstances != numInstances || oldNumVolumes != numVolumes;
    }

    public int getNumInstances() {
        return numInstances;
    }

    public int getNumVolumes() {
        return numVolumes;
    }
}
