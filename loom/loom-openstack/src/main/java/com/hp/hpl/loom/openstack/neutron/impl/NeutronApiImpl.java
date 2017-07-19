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
package com.hp.hpl.loom.openstack.neutron.impl;

import com.hp.hpl.loom.openstack.OpenstackApi;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonEndpoint;
import com.hp.hpl.loom.openstack.neutron.NeutronApi;
import com.hp.hpl.loom.openstack.neutron.NeutronNetworks;
import com.hp.hpl.loom.openstack.neutron.NeutronPorts;
import com.hp.hpl.loom.openstack.neutron.NeutronSubnets;

/**
 * The Neutron API implementation, it constructs the sub implementations.
 */
public class NeutronApiImpl implements NeutronApi {
    private NeutronNetworks neutronNetwork;
    private NeutronSubnets neutronSubnets;
    private NeutronPorts neutronPorts;
    private String version;

    /**
     * An implementation of the NeutronApi.
     *
     * @param version the version this API is supporting
     * @param openstackService the openstackApi for looking up tokens
     * @param jsonEndpoint the end point to access
     */
    public NeutronApiImpl(final String version, final OpenstackApi openstackService, final JsonEndpoint jsonEndpoint) {
        neutronNetwork = new NeutronNetworksImpl(openstackService, jsonEndpoint);
        neutronSubnets = new NeutronSubnetsImpl(openstackService, jsonEndpoint);
        neutronPorts = new NeutronPortsImpl(openstackService, jsonEndpoint);
        this.version = version;
    }

    @Override
    public NeutronNetworks getNeutronNetwork() {
        return neutronNetwork;
    }

    @Override
    public NeutronSubnets getNeutronSubnets() {
        return neutronSubnets;
    }

    @Override
    public NeutronPorts getNeutronPorts() {
        return neutronPorts;
    }

    @Override
    public String getVersion() {
        return version;
    }
}
