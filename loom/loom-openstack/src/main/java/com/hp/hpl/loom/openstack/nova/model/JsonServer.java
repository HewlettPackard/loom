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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Object to model the Server.
 */
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JsonServer {

    private String accessIPv4;
    private String accessIPv6;

    private String created;
    @JsonProperty("OS-EXT-SRV-ATTR:host")
    private String host;
    private String hostId;
    private String id;

    private String name;
    private int progress;
    private String status;
    @JsonProperty("tenant_id")
    private String tenantId;
    private String updated;
    @JsonProperty("user_id")
    private String userId;

    private List<JsonLink> links;
    private JsonMetadata metadata;

    @JsonProperty("key_name")
    private String keyName;
    @JsonProperty("OS-EXT-AZ:availability_zone")
    private String availabilityZone;
    @JsonProperty("OS-EXT-STS:power_state")
    private String powerState;
    @JsonProperty("config_drive")
    private String configDrive;

    @JsonProperty("OS-EXT-STS:task_state")
    private String taskState;
    @JsonProperty("OS-EXT-STS:vm_state")
    private String vmState;
    @JsonProperty("OS-EXT-SRV-ATTR:instance_name")
    private String instanceName;
    @JsonProperty("OS-EXT-SRV-ATTR:hypervisor_hostname")
    private String hypervisorHostname;

    @JsonProperty("security_groups")
    private List<JsonSecurityGroup> securityGroups;

    private List<JsonNetworkUuid> networks;

    private String imageRef;
    private String flavorRef;

    private JsonAddresses addresses;

    private JsonFlavor flavor;
    private JsonImage image;

    /**
     * @return the accessIPv4
     */
    public String getAccessIPv4() {
        return accessIPv4;
    }

    /**
     * @param accessIPv4 the accessIPv4 to set
     */
    public void setAccessIPv4(final String accessIPv4) {
        this.accessIPv4 = accessIPv4;
    }

    /**
     * @return the accessIPv6
     */
    public String getAccessIPv6() {
        return accessIPv6;
    }

    /**
     * @param accessIPv6 the accessIPv6 to set
     */
    public void setAccessIPv6(final String accessIPv6) {
        this.accessIPv6 = accessIPv6;
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
     * @return the hostId
     */
    public String getHostId() {
        return hostId;
    }

    /**
     * @param hostId the hostId to set
     */
    public void setHostId(final String hostId) {
        this.hostId = hostId;
    }

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
     * @return the tenantId
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * @param tenantId the tenantId to set
     */
    public void setTenantId(final String tenantId) {
        this.tenantId = tenantId;
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
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(final String userId) {
        this.userId = userId;
    }

    //
    // /**
    // * @return the image
    // */
    // public String getImage() {
    // return image;
    // }
    //
    // /**
    // * @param image the image to set
    // */
    // public void setImage(final String image) {
    // this.image = image;
    // }

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
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(final String host) {
        this.host = host;
    }

    /**
     * @return the keyName
     */
    public String getKeyName() {
        return keyName;
    }

    /**
     * @param keyName the keyName to set
     */
    public void setKeyName(final String keyName) {
        this.keyName = keyName;
    }

    /**
     * @return the availabilityZone
     */
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    /**
     * @param availabilityZone the availabilityZone to set
     */
    public void setAvailabilityZone(final String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    /**
     * @return the powerState
     */
    public String getPowerState() {
        return powerState;
    }

    /**
     * @param powerState the powerState to set
     */
    public void setPowerState(final String powerState) {
        this.powerState = powerState;
    }

    /**
     * @return the configDrive
     */
    public String getConfigDrive() {
        return configDrive;
    }

    /**
     * @param configDrive the configDrive to set
     */
    public void setConfigDrive(final String configDrive) {
        this.configDrive = configDrive;
    }

    /**
     * @return the taskState
     */
    public String getTaskState() {
        return taskState;
    }

    /**
     * @param taskState the taskState to set
     */
    public void setTaskState(final String taskState) {
        this.taskState = taskState;
    }

    /**
     * @return the vmState
     */
    public String getVmState() {
        return vmState;
    }

    /**
     * @param vmState the vmState to set
     */
    public void setVmState(final String vmState) {
        this.vmState = vmState;
    }

    /**
     * @return the instanceName
     */
    public String getInstanceName() {
        return instanceName;
    }

    /**
     * @param instanceName the instanceName to set
     */
    public void setInstanceName(final String instanceName) {
        this.instanceName = instanceName;
    }

    /**
     * @return the hypervisorHostname
     */
    public String getHypervisorHostname() {
        return hypervisorHostname;
    }

    /**
     * @param hypervisorHostname the hypervisorHostname to set
     */
    public void setHypervisorHostname(final String hypervisorHostname) {
        this.hypervisorHostname = hypervisorHostname;
    }

    /**
     * @return the securityGroups
     */
    public List<JsonSecurityGroup> getSecurityGroups() {
        return securityGroups;
    }

    /**
     * @param securityGroups the securityGroups to set
     */
    public void setSecurityGroups(final List<JsonSecurityGroup> securityGroups) {
        this.securityGroups = securityGroups;
    }

    /**
     * @return the flavor
     */
    public JsonFlavor getFlavor() {
        return flavor;
    }

    /**
     * @param flavor the flavor to set
     */
    public void setFlavor(final JsonFlavor flavor) {
        this.flavor = flavor;
    }

    /**
     * @return the addresses
     */
    public JsonAddresses getAddresses() {
        return addresses;
    }

    /**
     * @param addresses the addresses to set
     */
    public void setAddresses(final JsonAddresses addresses) {
        this.addresses = addresses;
    }

    /**
     * @return the image
     */
    public JsonImage getImage() {
        return image;
    }

    /**
     * @param image the image to set
     */
    public void setImage(final JsonImage image) {
        this.image = image;
    }

    /**
     * @return the imageRef
     */
    public String getImageRef() {
        return imageRef;
    }

    /**
     * @param imageRef the imageRef to set
     */
    public void setImageRef(final String imageRef) {
        this.imageRef = imageRef;
    }

    /**
     * @return the flavorRef
     */
    public String getFlavorRef() {
        return flavorRef;
    }

    /**
     * @param flavorRef the flavorRef to set
     */
    public void setFlavorRef(final String flavorRef) {
        this.flavorRef = flavorRef;
    }

    /**
     * @return the networks
     */
    public List<JsonNetworkUuid> getNetworks() {
        return networks;
    }

    /**
     * @param networks the networks to set
     */
    public void setNetworks(final List<JsonNetworkUuid> networks) {
        this.networks = networks;
    }

}
