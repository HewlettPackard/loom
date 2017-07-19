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

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.hp.hpl.loom.openstack.OpenstackApi;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonEndpoint;
import com.hp.hpl.loom.openstack.neutron.NeutronSubnets;
import com.hp.hpl.loom.openstack.neutron.model.JsonSubnet;
import com.hp.hpl.loom.openstack.neutron.model.JsonSubnets;

/**
 * The neutron subnets implementation.
 */
public class NeutronSubnetsImpl extends NeutronBase<JsonSubnets, JsonSubnet> implements NeutronSubnets {
    /**
     * Constructor that takes the end point to use and an openstackApp to lookup the RestTemplate.
     *
     * @param openstackApp the openstackApp used to access the rest templates and token holder
     * @param jsonEndpoint the end point to access
     */
    public NeutronSubnetsImpl(final OpenstackApi openstackApp, final JsonEndpoint jsonEndpoint) {
        super(openstackApp, jsonEndpoint);
    }

    @Override
    protected Class<JsonSubnets> getTypeClass() {
        return JsonSubnets.class;
    }

    @Override
    protected String getUriSuffix() {
        return "subnet";
    }

    @Override
    public String getUri() {
        String resourcesUri = jsonEndpoint.getUrl() + getUriSuffix() + "s";
        return resourcesUri;
    }

    @Override
    public void addToResult(final JsonSubnets result, final JsonSubnets nextResults) {
        result.getSubnets().addAll(nextResults.getSubnets());
        result.setLinks(null);
    }

    @Override
    public List<JsonSubnet> getResults(final JsonSubnets result) {
        return result.getSubnets();
    }

    @Override
    public JsonSubnets createSubnets(final JsonSubnets subnets) {
        String url = getUri();
        RestTemplate rt = getRestTemplate(jsonEndpoint.getProjectId());
        ResponseEntity<JsonSubnets> result = rt.postForEntity(url, subnets, JsonSubnets.class);

        return result.getBody();
    }
}
