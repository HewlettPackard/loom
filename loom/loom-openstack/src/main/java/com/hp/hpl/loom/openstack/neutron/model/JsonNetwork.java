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
package com.hp.hpl.loom.openstack.neutron.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Object to model the Network.
 */
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JsonNetwork {
    private String status;
    private List<String> subnets;
    private String name;
    @JsonProperty("provider:physical_network")
    private String physicalNetwork;
    @JsonProperty("admin_state_up")
    private boolean adminStateUp;
    @JsonProperty("tenant_id")
    private String tenantId;
    @JsonProperty("provider:network_type")
    private String networkType;
    @JsonProperty("router:external")
    private String external;
    private boolean shared;
    private String id;
    @JsonProperty("provider:segmentation_id")
    private String segmentationId;

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
     * @return the subnets
     */
    public List<String> getSubnets() {
        return subnets;
    }

    /**
     * @param subnets the subnets to set
     */
    public void setSubnets(final List<String> subnets) {
        this.subnets = subnets;
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
     * @return the physicalNetwork
     */
    public String getPhysicalNetwork() {
        return physicalNetwork;
    }

    /**
     * @param physicalNetwork the physicalNetwork to set
     */
    public void setPhysicalNetwork(final String physicalNetwork) {
        this.physicalNetwork = physicalNetwork;
    }

    /**
     * @return the adminStateUp
     */
    public boolean isAdminStateUp() {
        return adminStateUp;
    }

    /**
     * @param adminStateUp the adminStateUp to set
     */
    public void setAdminStateUp(final boolean adminStateUp) {
        this.adminStateUp = adminStateUp;
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
     * @return the networkType
     */
    public String getNetworkType() {
        return networkType;
    }

    /**
     * @param networkType the networkType to set
     */
    public void setNetworkType(final String networkType) {
        this.networkType = networkType;
    }

    /**
     * @return the external
     */
    public String getExternal() {
        return external;
    }

    /**
     * @param external the external to set
     */
    public void setExternal(final String external) {
        this.external = external;
    }

    /**
     * @return the shared
     */
    public boolean isShared() {
        return shared;
    }

    /**
     * @param shared the shared to set
     */
    public void setShared(final boolean shared) {
        this.shared = shared;
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
     * @return the segmentationId
     */
    public String getSegmentationId() {
        return segmentationId;
    }

    /**
     * @param segmentationId the segmentationId to set
     */
    public void setSegmentationId(final String segmentationId) {
        this.segmentationId = segmentationId;
    }

}
