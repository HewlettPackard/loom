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
package com.hp.hpl.loom.adapter.os.fake;

import java.util.ArrayList;

import com.hp.hpl.loom.adapter.os.OsSubnetAttributes;

public class FakeSubnet extends OsSubnetAttributes {

    private ArrayList<String> instanceIds;

    public FakeSubnet(final String name, final String id, final boolean enableDhcp, final String networkId,
            final String gatewayIp, final int ipVersion, final String cidr) {
        setItemName(name);
        setItemId(id);
        setEnableDhcp(enableDhcp);
        setNetworkId(networkId);
        setGatewayIp(gatewayIp);
        setIpVersion(ipVersion);
        setCidr(cidr);
        instanceIds = new ArrayList<>();
    }

    public ArrayList<String> getInstanceIds() {
        return instanceIds;
    }

    public void addInstanceId(final String id) {
        instanceIds.add(id);
    }

    public void removeInstanceIds(final String id) {
        instanceIds.remove(id);
    }

}
