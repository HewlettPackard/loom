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
package com.hp.hpl.loom.openstack.nova.impl;

import com.hp.hpl.loom.openstack.OpenstackApi;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonEndpoint;
import com.hp.hpl.loom.openstack.nova.NovaApi;
import com.hp.hpl.loom.openstack.nova.NovaFlavors;
import com.hp.hpl.loom.openstack.nova.NovaImages;
import com.hp.hpl.loom.openstack.nova.NovaQuotas;
import com.hp.hpl.loom.openstack.nova.NovaServers;

/**
 * The nova API impl.
 */
public class NovaApiImpl implements NovaApi {
    private NovaImages novaImages;
    private NovaServers novaServers;
    private NovaFlavors novaFlavors;
    private NovaQuotas novaQuotas;
    private String version;

    /**
     * An implementation of the NovaApi.
     *
     * @param version the version this API is supporting
     * @param openstackService the openstackApi for looking up tokens
     * @param jsonEndpoint the end point to access
     */
    public NovaApiImpl(final String version, final OpenstackApi openstackService, final JsonEndpoint jsonEndpoint) {
        novaImages = new NovaImagesImpl(openstackService, jsonEndpoint);
        novaServers = new NovaServersImpl(openstackService, jsonEndpoint);
        novaFlavors = new NovaFlavorsImpl(openstackService, jsonEndpoint);
        novaQuotas = new NovaQuotasImpl(openstackService, jsonEndpoint);
        this.version = version;
    }

    @Override
    public NovaImages getNovaImages() {
        return novaImages;
    }

    @Override
    public NovaServers getNovaServers() {
        return novaServers;
    }

    @Override
    public NovaFlavors getNovaFlavors() {
        return novaFlavors;
    }

    @Override
    public NovaQuotas getNovaQuotas() {
        return novaQuotas;
    }

    @Override
    public String getVersion() {
        return version;
    }
}
