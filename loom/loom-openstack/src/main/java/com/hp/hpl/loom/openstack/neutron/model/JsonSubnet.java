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
 * Object to model the Subnet.
 */
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JsonSubnet {
    private String name;
    @JsonProperty("enable_dhcp")
    private boolean enableDhcp;
    @JsonProperty("network_id")
    private String networkId;
    @JsonProperty("tenant_id")
    private String tenantId;
    @JsonProperty("dns_nameservers")
    private List<String> dnsNameservers;
    @JsonProperty("gateway_ip")
    private String gatewayIp;

    @JsonProperty("allocation_pools")
    private List<JsonPools> allocationPools;

    // "ipv6_ra_mode": null,
    // "host_routes": [],

    @JsonProperty("ip_version")
    private int ipVersion;

    @JsonProperty("ipv6_address_mode")
    private String ipv6AddressMode;
    @JsonProperty("cidr")
    private String cidr;
    @JsonProperty("id")
    private String id;

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
     * @return the enableDhcp
     */
    public boolean isEnableDhcp() {
        return enableDhcp;
    }

    /**
     * @param enableDhcp the enableDhcp to set
     */
    public void setEnableDhcp(final boolean enableDhcp) {
        this.enableDhcp = enableDhcp;
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
     * @return the dnsNameservers
     */
    public List<String> getDnsNameservers() {
        return dnsNameservers;
    }

    /**
     * @param dnsNameservers the dnsNameservers to set
     */
    public void setDnsNameservers(final List<String> dnsNameservers) {
        this.dnsNameservers = dnsNameservers;
    }

    /**
     * @return the gatewayIp
     */
    public String getGatewayIp() {
        return gatewayIp;
    }

    /**
     * @param gatewayIp the gatewayIp to set
     */
    public void setGatewayIp(final String gatewayIp) {
        this.gatewayIp = gatewayIp;
    }

    /**
     * @return the ipVersion
     */
    public int getIpVersion() {
        return ipVersion;
    }

    /**
     * @param ipVersion the ipVersion to set
     */
    public void setIpVersion(final int ipVersion) {
        this.ipVersion = ipVersion;
    }

    /**
     * @return the ipv6AddressMode
     */
    public String getIpv6AddressMode() {
        return ipv6AddressMode;
    }

    /**
     * @param ipv6AddressMode the ipv6AddressMode to set
     */
    public void setIpv6AddressMode(final String ipv6AddressMode) {
        this.ipv6AddressMode = ipv6AddressMode;
    }

    /**
     * @return the cidr
     */
    public String getCidr() {
        return cidr;
    }

    /**
     * @param cidr the cidr to set
     */
    public void setCidr(final String cidr) {
        this.cidr = cidr;
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
     * @return the allocationPools
     */
    public List<JsonPools> getAllocationPools() {
        return allocationPools;
    }

    /**
     * @param allocationPools the allocationPools to set
     */
    public void setAllocationPools(final List<JsonPools> allocationPools) {
        this.allocationPools = allocationPools;
    }
}
