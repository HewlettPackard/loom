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
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;


public class TestMtoMInstance extends Item {

    private String projectId;
    private String deviceName;
    private ItemType type;

    @JsonIgnore
    private Set<TestMtoMVolume> volumes = new HashSet<TestMtoMVolume>();

    @JsonIgnore
    private Set<TestMtoMProject> projects = new HashSet<TestMtoMProject>();

    public TestMtoMInstance() {
        super();
    }

    public TestMtoMInstance(final String logicalId, final String name, final String projectId, final String deviceName,
            final ItemType instanceType) {
        super(logicalId, instanceType, name);
        this.projectId = projectId;
        this.deviceName = deviceName;
        type = instanceType;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(final String projectId) {
        this.projectId = projectId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(final String deviceName) {
        this.deviceName = deviceName;
    }

    @JsonIgnore
    public ItemType getType() {
        return type;
    }

    @ManyToMany(value = "TestMtoMVolumeToTestMtoMInstance", toClass = TestMtoMVolume.class)
    public Set<TestMtoMVolume> getVolumes() {
        return volumes;
    }

    public void addVolume(final TestMtoMVolume volume) {
        volumes.add(volume);
    }

    @ManyToMany(value = "TestMtoMProjectToTestMtoMInstance", toClass = TestMtoMProject.class)
    public Set<TestMtoMProject> getProjects() {
        return projects;
    }

    public void addProject(final TestMtoMProject project) {
        projects.add(project);
    }
}
