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
package com.hp.hpl.loom.openstack.cinder.impl;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.loom.openstack.OpenstackApi;
import com.hp.hpl.loom.openstack.cinder.CinderVolumeType;
import com.hp.hpl.loom.openstack.cinder.model.JsonVolumeTypes;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonEndpoint;

/**
 * The cinder volume type implementation.
 */
public class CinderVolumeTypeImpl extends CinderBase<JsonVolumeTypes, String> implements CinderVolumeType {
    /**
     * Constructor that takes the end point to use and an openstackApp to lookup the RestTemplate.
     *
     * @param openstackApp the openstackApp used to access the rest templates and token holder
     * @param jsonEndpoint the end point to access
     */
    public CinderVolumeTypeImpl(final OpenstackApi openstackApp, final JsonEndpoint jsonEndpoint) {
        super(openstackApp, jsonEndpoint);
    }

    @Override
    protected Class<JsonVolumeTypes> getTypeClass() {
        return JsonVolumeTypes.class;
    }

    @Override
    protected String getUriSuffix() {
        return "type";
    }

    @Override
    public String getUri() {
        String resourcesUri = jsonEndpoint.getUrl() + "/" + getUriSuffix() + "s";
        return resourcesUri;
    }

    @Override
    public void addToResult(final JsonVolumeTypes result, final JsonVolumeTypes nextResults) {
        // nothing to do here
    }

    @Override
    public List<String> getResults(final JsonVolumeTypes result) {
        List<String> list = new ArrayList<>();
        if (result.getName() != null) {
            list.add(result.getName());
        }
        return list;
    }
}
