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
package com.hp.hpl.loom.openstack.nova.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Object to model the Flavor.
 */
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonFlavor {

    private String id;
    private List<JsonLink> links;

    private int disk;
    private String name;
    private int ram;
    private int vcpus;

    @JsonProperty("os-flavor-access:is_public")
    private boolean isPublic;


    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * @return the links
     */
    public List<JsonLink> getLinks() {
        return links;
    }

    /**
     * @param links the links to set
     */
    public void setLinks(final List<JsonLink> links) {
        this.links = links;
    }

    /**
     * @return the disk
     */
    public int getDisk() {
        return disk;
    }

    /**
     * @param disk the disk to set
     */
    public void setDisk(final int disk) {
        this.disk = disk;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the ram
     */
    public int getRam() {
        return ram;
    }

    /**
     * @param ram the ram to set
     */
    public void setRam(final int ram) {
        this.ram = ram;
    }

    /**
     * @return the vcpus
     */
    public int getVcpus() {
        return vcpus;
    }

    /**
     * @param vcpus the vcpus to set
     */
    public void setVcpus(final int vcpus) {
        this.vcpus = vcpus;
    }

    /**
     * @return the isPublic
     */
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * @param flag the isPublic to set
     */
    public void setPublic(final boolean flag) {
        isPublic = flag;
    }


}
