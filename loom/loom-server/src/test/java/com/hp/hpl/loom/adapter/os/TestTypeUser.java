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
@ItemTypeInfo(TestTypeUser.TYPE_ID)
@ConnectedTo(toClass = TestTypeProject.class, type = "admin")
@ConnectedTo(toClass = TestTypeProject.class, type = "access")
@ConnectedTo(toClass = TestTypeProject.class, type = "user")
public class TestTypeUser extends SeparableItem<TestTypeUserAttributes> {

    public static final String TYPE_ID = "user";

    private int numAdminProjects;
    private int numAccessProjects;
    private String name;

    public TestTypeUser() {
        super();
    }

    public TestTypeUser(final String logicalId, final String name, final ItemType type) {
        super(logicalId, type, name);
        this.name = name;
        TestTypeUserAttributes core = new TestTypeUserAttributes();
        this.setCore(core);
    }

    @Override
    public boolean update() {
        int oldNumAdminProjects = numAdminProjects;
        int oldNumAccessProjects = numAccessProjects;
        numAdminProjects = getNumConnectedItemsWithRelationshipName(
                RelationshipUtil.getRelationshipNameBetweenLocalTypeIdsWithRelType(getProviderType(), TYPE_ID,
                        TestTypeProject.TYPE_ID, "admin"));
        numAccessProjects = getNumConnectedItemsWithRelationshipName(
                RelationshipUtil.getRelationshipNameBetweenLocalTypeIdsWithRelType(getProviderType(), TYPE_ID,
                        TestTypeProject.TYPE_ID, "access"));
        return oldNumAdminProjects != numAdminProjects || oldNumAccessProjects != numAccessProjects;
    }

    public int getNumAdminProjects() {
        return numAdminProjects;
    }

    public int getNumAccessProjects() {
        return numAccessProjects;
    }
}
