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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Object to model the Metadata.
 */
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonMetadata {
    private String description;
    private String architecture;
    @JsonProperty("auto_disk_config")
    private String autoDiskConfig;
    @JsonProperty("kernel_id")
    private String kernelId;
    @JsonProperty("ramdiskId")
    private String ramdiskId;
    private String readonly;
    @JsonProperty("attached_mode")
    private String attachedMode;

    /**
     * @return the architecture
     */
    public String getArchitecture() {
        return architecture;
    }

    /**
     * @param architecture the architecture to set
     */
    public void setArchitecture(final String architecture) {
        this.architecture = architecture;
    }

    /**
     * @return the autoDiskConfig
     */
    public String getAutoDiskConfig() {
        return autoDiskConfig;
    }

    /**
     * @param autoDiskConfig the autoDiskConfig to set
     */
    public void setAutoDiskConfig(final String autoDiskConfig) {
        this.autoDiskConfig = autoDiskConfig;
    }

    /**
     * @return the kernelId
     */
    public String getKernelId() {
        return kernelId;
    }

    /**
     * @param kernelId the kernelId to set
     */
    public void setKernelId(final String kernelId) {
        this.kernelId = kernelId;
    }

    /**
     * @return the ramdiskId
     */
    public String getRamdiskId() {
        return ramdiskId;
    }

    /**
     * @param ramdiskId the ramdiskId to set
     */
    public void setRamdiskId(final String ramdiskId) {
        this.ramdiskId = ramdiskId;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * @return the readonly
     */
    public String getReadonly() {
        return readonly;
    }

    /**
     * @param readonly the readonly to set
     */
    public void setReadonly(final String readonly) {
        this.readonly = readonly;
    }

    /**
     * @return the attachedMode
     */
    public String getAttachedMode() {
        return attachedMode;
    }

    /**
     * @param attachedMode the attachedMode to set
     */
    public void setAttachedMode(final String attachedMode) {
        this.attachedMode = attachedMode;
    }

}
