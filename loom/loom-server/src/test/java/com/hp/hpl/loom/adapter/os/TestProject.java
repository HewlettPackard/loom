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

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hp.hpl.loom.adapter.annotations.OneToMany;
import com.hp.hpl.loom.adapter.annotations.Root;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;

@Root
public class TestProject extends Item {

    private String projectId;

    @JsonIgnore
    private Set<TestInstance> instances = new HashSet<TestInstance>();

    @JsonIgnore
    private Set<TestVolume> volumes = new HashSet<TestVolume>();

    public TestProject() {
        super();
    }

    public TestProject(final String logicalId, final String name, final String projectId, final ItemType instanceType) {
        super(logicalId, instanceType, name);
        this.projectId = projectId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(final String projectId) {
        this.projectId = projectId;
    }

    @OneToMany(value = "TestProjectToTestInstance", toClass = TestInstance.class)
    public Set<TestInstance> getInstances() {
        return instances;
    }

    public void addInstance(final TestInstance instance) {
        instances.add(instance);
    }

    @OneToMany(value = "TestProjectToTestVolume", toClass = TestVolume.class)
    public Set<TestVolume> getVolumes() {
        return volumes;
    }

    public void addVolume(final TestVolume volume) {
        volumes.add(volume);
    }
}
