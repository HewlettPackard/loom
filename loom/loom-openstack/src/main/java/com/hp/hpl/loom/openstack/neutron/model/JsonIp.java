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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Object to model the IP.
 */
public class JsonIp {
    @JsonProperty("subnet_id")
    private String subnetId;
    @JsonProperty("ip_address")
    private String ipAddress;

    /**
     * @return the subnetId
     */
    public String getSubnetId() {
        return subnetId;
    }

    /**
     * @param subnetId the subnetId to set
     */
    public void setSubnetId(final String subnetId) {
        this.subnetId = subnetId;
    }

    /**
     * @return the ipAddress
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * @param ipAddress the ipAddress to set
     */
    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
