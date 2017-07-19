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
import com.hp.hpl.loom.adapter.annotations.ManyToMany;
import com.hp.hpl.loom.adapter.annotations.Root;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;

@Root
public class TestMtoMProject extends Item {

    private String projectId;

    @JsonIgnore
    private Set<TestMtoMInstance> instances = new HashSet<TestMtoMInstance>();

    @JsonIgnore
    private Set<TestMtoMVolume> volumes = new HashSet<TestMtoMVolume>();

    public TestMtoMProject() {
        super();
    }

    public TestMtoMProject(final String logicalId, final String name, final String projectId,
            final ItemType instanceType) {
        super(logicalId, instanceType, name);
        this.projectId = projectId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(final String projectId) {
        this.projectId = projectId;
    }

    @ManyToMany(value = "TestMtoMProjectToTestMtoMInstance", toClass = TestMtoMInstance.class)
    public Set<TestMtoMInstance> getInstances() {
        return instances;
    }

    public void addInstance(final TestMtoMInstance instance) {
        instances.add(instance);
    }

    @ManyToMany(value = "TestMtoMProjectToTestMtoMVolume", toClass = TestMtoMVolume.class)
    public Set<TestMtoMVolume> getVolumes() {
        return volumes;
    }

    public void addVolume(final TestMtoMVolume volume) {
        volumes.add(volume);
    }
}
