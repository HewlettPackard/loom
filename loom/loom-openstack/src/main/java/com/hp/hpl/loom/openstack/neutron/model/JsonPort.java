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
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Object to model the Port.
 */
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonPort {
    private String status;
    @JsonProperty("binding:host_id")
    private String hostId;
    private String name;
    @JsonProperty("admin_state_up")
    private boolean adminStateUp;
    @JsonProperty("network_id")
    private String networkId;
    @JsonProperty("tenant_id")
    private String tenantId;


    @JsonProperty("device_owner")
    private String deviceOwner;

    @JsonProperty("mac_address")
    private String macAddress;

    @JsonProperty("id")
    private String id;

    @JsonProperty("device_id")
    private String deviceId;

    @JsonProperty("binding:vnic_type")
    private String vnicType;

    @JsonProperty("binding:vif_type")
    private String vifType;



    @JsonProperty("security_groups")
    private List<String> securityGroups;

    @JsonProperty("fixed_ips")
    private Set<JsonIp> fixedIps;

    @JsonProperty("binding:vif_details")
    private Map<String, String> vifDetails;

    @JsonProperty("extra_dhcp_opts")
    private List<String> extraDhcpOpts;

    @JsonProperty("allowed_address_pairs")
    private List<String> allowedAddressPairs;


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
     * @return the networkId
     */
    public String getNetworkId() {
        return networkId;
    }

    /**
     * @param networkId the networkId to set
     */
    public void setNetworkId(final String networkId) {
        this.networkId = networkId;
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
     * @return the vifType
     */
    public String getVifType() {
        return vifType;
    }

    /**
     * @param vifType the vifType to set
     */
    public void setVifType(final String vifType) {
        this.vifType = vifType;
    }

    /**
     * @return the deviceOwner
     */
    public String getDeviceOwner() {
        return deviceOwner;
    }

    /**
     * @param deviceOwner the deviceOwner to set
     */
    public void setDeviceOwner(final String deviceOwner) {
        this.deviceOwner = deviceOwner;
    }

    /**
     * @return the macAddress
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * @param macAddress the macAddress to set
     */
    public void setMacAddress(final String macAddress) {
        this.macAddress = macAddress;
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
     * @return the deviceId
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * @param deviceId the deviceId to set
     */
    public void setDeviceId(final String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * @return the vnicType
     */
    public String getVnicType() {
        return vnicType;
    }

    /**
     * @param vnicType the vnicType to set
     */
    public void setVnicType(final String vnicType) {
        this.vnicType = vnicType;
    }

    /**
     * @return the securityGroups
     */
    public List<String> getSecurityGroups() {
        return securityGroups;
    }

    /**
     * @param securityGroups the securityGroups to set
     */
    public void setSecurityGroups(final List<String> securityGroups) {
        this.securityGroups = securityGroups;
    }

    /**
     * @return the fixedIps
     */
    public Set<JsonIp> getFixedIps() {
        return fixedIps;
    }

    /**
     * @param fixedIps the fixedIps to set
     */
    public void setFixedIps(final Set<JsonIp> fixedIps) {
        this.fixedIps = fixedIps;
    }

    /**
     * @return the vifDetails
     */
    public Map<String, String> getVifDetails() {
        return vifDetails;
    }

    /**
     * @param vifDetails the vifDetails to set
     */
    public void setVifDetails(final Map<String, String> vifDetails) {
        this.vifDetails = vifDetails;
    }


    /**
     * @return the allowedAddressPairs
     */
    public List<String> getAllowedAddressPairs() {
        return allowedAddressPairs;
    }

    /**
     * @param allowedAddressPairs the allowedAddressPairs to set
     */
    public void setAllowedAddressPairs(final List<String> allowedAddressPairs) {
        this.allowedAddressPairs = allowedAddressPairs;
    }

    /**
     * @return the extraDhcpOpts
     */
    public List<String> getExtraDhcpOpts() {
        return extraDhcpOpts;
    }

    /**
     * @param extraDhcpOpts the extraDhcpOpts to set
     */
    public void setExtraDhcpOpts(final List<String> extraDhcpOpts) {
        this.extraDhcpOpts = extraDhcpOpts;
    }

}
