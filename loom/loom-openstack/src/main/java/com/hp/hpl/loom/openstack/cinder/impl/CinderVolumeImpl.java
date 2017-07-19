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

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.hp.hpl.loom.openstack.OpenstackApi;
import com.hp.hpl.loom.openstack.cinder.CinderVolume;
import com.hp.hpl.loom.openstack.cinder.model.JsonVolume;
import com.hp.hpl.loom.openstack.cinder.model.JsonVolumes;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonEndpoint;

/**
 * The cinder volume implementation.
 */
public class CinderVolumeImpl extends CinderBase<JsonVolumes, JsonVolume> implements CinderVolume {
    /**
     * Constructor that takes the end point to use and an openstackApp to lookup the RestTemplate.
     *
     * @param openstackApp the openstackApp used to access the rest templates and token holder
     * @param jsonEndpoint the end point to access
     */
    public CinderVolumeImpl(final OpenstackApi openstackApp, final JsonEndpoint jsonEndpoint) {
        super(openstackApp, jsonEndpoint);
    }

    @Override
    protected Class<JsonVolumes> getTypeClass() {
        return JsonVolumes.class;
    }

    @Override
    protected String getUriSuffix() {
        return "volume";
    }

    @Override
    public String getUri() {
        String resourcesUri = jsonEndpoint.getUrl() + "/" + getUriSuffix() + "s/detail"; // .replace("v1",
                                                                                         // getVersion())
        return resourcesUri;
    }

    @Override
    public void addToResult(final JsonVolumes result, final JsonVolumes nextResults) {
        result.getVolumes().addAll(nextResults.getVolumes());
        result.setLinks(null);
    }

    @Override
    public List<JsonVolume> getResults(final JsonVolumes result) {
        return result.getVolumes();
    }

    @Override
    public JsonVolumes createVolume(final String projectId, final JsonVolumes volumes) {
        String url = jsonEndpoint.getUrl() + "/volumes";
        RestTemplate rt = getRestTemplate(jsonEndpoint.getProjectId());
        ResponseEntity<JsonVolumes> result = rt.postForEntity(url, volumes, JsonVolumes.class);

        return result.getBody();

    }
}
