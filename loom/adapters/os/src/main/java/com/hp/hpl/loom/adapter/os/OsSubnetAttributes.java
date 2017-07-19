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

import com.hp.hpl.loom.model.CoreItemAttributes;

public class OsSubnetAttributes extends CoreItemAttributes {

    private boolean enableDhcp;
    private String networkId;
    private String gatewayIp;
    private int ipVersion;
    private String cidr;

    public OsSubnetAttributes() {
        super();
    }

    public OsSubnetAttributes(final boolean enableDhcp, final String networkId, final String gatewayIp,
            final int ipVersion, final String cidr) {
        super();
        this.enableDhcp = enableDhcp;
        this.networkId = networkId;
        this.gatewayIp = gatewayIp;
        this.ipVersion = ipVersion;
        this.cidr = cidr;
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


}
