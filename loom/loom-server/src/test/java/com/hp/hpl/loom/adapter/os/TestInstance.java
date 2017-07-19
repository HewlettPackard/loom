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
import com.hp.hpl.loom.adapter.annotations.ManyToOne;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;

public class TestInstance extends Item {

    private String projectId;
    private String flavour;
    private ItemType type;

    @JsonIgnore
    private TestVolume volume;

    @JsonIgnore
    private TestProject project;

    public TestInstance() {
        super();
    }

    public TestInstance(final String logicalId, final String name, final String projectId, final String flavour,
            final ItemType instanceType) {
        super(logicalId, instanceType, name);
        this.projectId = projectId;
        this.flavour = flavour;
        type = instanceType;
    }


    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(final String projectId) {
        this.projectId = projectId;
    }

    public void setFlavour(final String flavour) {
        this.flavour = flavour;
    }

    public String getFlavour() {
        return flavour;
    }

    @JsonIgnore
    public ItemType getType() {
        return type;
    }

    @ManyToOne("TestVolumeToTestInstance")
    public TestVolume getVolume() {
        return volume;
    }

    public void setVolume(final TestVolume volume) {
        this.volume = volume;
    }

    @ManyToOne("TestProjectToTestInstance")
    public TestProject getProject() {
        return project;
    }

    public void setProject(final TestProject project) {
        this.project = project;
    }
}
