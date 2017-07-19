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
 * Object to model the Image.
 */
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonImage {

    private String id;
    private List<JsonLink> links;

    private String created;
    private int minDisk;
    private int minRam;
    private String name;
    private int progress;
    private String status;
    private String updated;

    @JsonProperty("OS-EXT-IMG-SIZE:size")
    private long size;

    private JsonMetadata metadata;

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
     * @return the progress
     */
    public int getProgress() {
        return progress;
    }

    /**
     * @param progress the progress to set
     */
    public void setProgress(final int progress) {
        this.progress = progress;
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
     * @return the metadata
     */
    public JsonMetadata getMetadata() {
        return metadata;
    }

    /**
     * @param metadata the metadata to set
     */
    public void setMetadata(final JsonMetadata metadata) {
        this.metadata = metadata;
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
}
