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
package com.hp.hpl.loom.openstack.glance.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hp.hpl.loom.openstack.nova.model.JsonLink;

/**
 * Object to model the Image.
 */
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonImage {

    private String id;
    private List<JsonLink> links;

    @JsonProperty("container_format")
    private String containerFormat;
    @JsonProperty("disk_format")
    private String diskFormat;

    @JsonProperty("created_at")
    private String created;

    private String visibility;
    private String checksum;

    @JsonProperty("protected")
    private boolean protectedFlag;

    @JsonProperty("min_disk")
    private int minDisk;
    @JsonProperty("min_ram")
    private int minRam;
    private String name;

    private String status;
    @JsonProperty("updated_at")
    private String updated;

    @JsonProperty("size")
    private long size;


    /**
     * Default constructor.
     */
    public JsonImage() {

    }

    /**
     * Construct a JsonImage using a string.
     *
     * @param str the string
     */
    public JsonImage(final String str) {}

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
     * @return the created
     */
    public String getCreated() {
        return created;
    }

    /**
     * @param created the created to set
     */
    public void setCreated(final String created) {
        this.created = created;
    }

    /**
     * @return the minDisk
     */
    public int getMinDisk() {
        return minDisk;
    }

    /**
     * @param minDisk the minDisk to set
     */
    public void setMinDisk(final int minDisk) {
        this.minDisk = minDisk;
    }

    /**
     * @return the minRam
     */
    public int getMinRam() {
        return minRam;
    }

    /**
     * @param minRam the minRam to set
     */
    public void setMinRam(final int minRam) {
        this.minRam = minRam;
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
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(final String status) {
        this.status = status;
    }

    /**
     * @return the updated
     */
    public String getUpdated() {
        return updated;
    }

    /**
     * @param updated the updated to set
     */
    public void setUpdated(final String updated) {
        this.updated = updated;
    }

    /**
     * @return the size
     */
    public long getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(final long size) {
        this.size = size;
    }

    /**
     * @return the containerFormat
     */
    public String getContainerFormat() {
        return containerFormat;
    }

    /**
     * @param containerFormat the containerFormat to set
     */
    public void setContainerFormat(final String containerFormat) {
        this.containerFormat = containerFormat;
    }

    /**
     * @return the diskFormat
     */
    public String getDiskFormat() {
        return diskFormat;
    }

    /**
     * @param diskFormat the diskFormat to set
     */
    public void setDiskFormat(final String diskFormat) {
        this.diskFormat = diskFormat;
    }

    /**
     * @return the visibility
     */
    public String getVisibility() {
        return visibility;
    }

    /**
     * @param visibility the visibility to set
     */
    public void setVisibility(final String visibility) {
        this.visibility = visibility;
    }

    /**
     * @return the checksum
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * @param checksum the checksum to set
     */
    public void setChecksum(final String checksum) {
        this.checksum = checksum;
    }

    /**
     * @return the protectedFlag
     */
    public boolean isProtectedFlag() {
        return protectedFlag;
    }

    /**
     * @param protectedFlag the protectedFlag to set
     */
    public void setProtectedFlag(final boolean protectedFlag) {
        this.protectedFlag = protectedFlag;
    }
}
