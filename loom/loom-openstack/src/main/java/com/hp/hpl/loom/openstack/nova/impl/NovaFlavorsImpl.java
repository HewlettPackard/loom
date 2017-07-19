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

import java.util.List;

import com.hp.hpl.loom.openstack.OpenstackApi;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonEndpoint;
import com.hp.hpl.loom.openstack.nova.NovaFlavors;
import com.hp.hpl.loom.openstack.nova.model.JsonFlavor;
import com.hp.hpl.loom.openstack.nova.model.JsonFlavors;

/**
 * The nova flavors API.
 */
public class NovaFlavorsImpl extends NovaBase<JsonFlavors, JsonFlavor> implements NovaFlavors {

    /**
     * Constructor that takes the end point to use and an openstackApp to lookup the RestTemplate.
     *
     * @param openstackApp the openstackApp used to access the rest templates and token holder
     * @param jsonEndpoint the end point to access
     */
    public NovaFlavorsImpl(final OpenstackApi openstackApp, final JsonEndpoint jsonEndpoint) {
        super(openstackApp, jsonEndpoint);
    }

    @Override
    protected Class<JsonFlavors> getTypeClass() {
        return JsonFlavors.class;
    }

    @Override
    protected String getUriSuffix() {
        return "flavor";
    }

    @Override
    public String getUri() {
        String resourcesUri = jsonEndpoint.getUrl() + "/" + getUriSuffix() + "s/detail";
        return resourcesUri;
    }

    @Override
    public void addToResult(final JsonFlavors result, final JsonFlavors nextResults) {
        result.getFlavors().addAll(nextResults.getFlavors());
        result.setLinks(null);
    }

    @Override
    public List<JsonFlavor> getResults(final JsonFlavors result) {
        return result.getFlavors();
    }
}
