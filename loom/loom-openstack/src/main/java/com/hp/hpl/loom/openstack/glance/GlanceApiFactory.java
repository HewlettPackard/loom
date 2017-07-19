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
package com.hp.hpl.loom.openstack.glance;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.hp.hpl.loom.openstack.NoSupportedApiVersion;
import com.hp.hpl.loom.openstack.OpenstackApi;
import com.hp.hpl.loom.openstack.glance.impl.GlanceApiImpl;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonEndpoint;
import com.hp.hpl.loom.openstack.neutron.model.JsonVersion;
import com.hp.hpl.loom.openstack.neutron.model.JsonVersions;

/**
 * A factory to construct GlanceApi's. The intention is that this can be extended to return
 * different implementation of the API based on the supplied end points and the required versions.
 */
public final class GlanceApiFactory {
    private static final Log LOG = LogFactory.getLog(GlanceApiFactory.class);

    private GlanceApiFactory() {}

    /**
     * Get the GlanceApi based on the versions required and supplied end points.
     *
     * @param openstackApi openstackApi to lookup the tokens
     * @param jsonEndpoints End points to try and connect to
     * @param versions required versions (any will do)
     * @return the CinderApi
     * @throws NoSupportedApiVersion thrown if no version could be found
     */
    public static GlanceApi getApi(final OpenstackApi openstackApi, final List<JsonEndpoint> jsonEndpoints,
            final String[] versions) throws NoSupportedApiVersion {

        Map<JsonEndpoint, List<JsonVersion>> jsonVersionsMap = new HashMap<>();

        getVersionsFromEndpoint(openstackApi, jsonEndpoints, jsonVersionsMap);

        JsonEndpoint glanceEndpoint = null;
        String version = null;

        Set<JsonEndpoint> keys = jsonVersionsMap.keySet();
        for (JsonEndpoint endpoint : keys) {
            List<JsonVersion> jsonVersions = jsonVersionsMap.get(endpoint);
            for (JsonVersion jsonVersion : jsonVersions) {
                for (String v : versions) {
                    if (jsonVersion.getId().equals(v.trim())) {
                        glanceEndpoint = new JsonEndpoint();
                        glanceEndpoint.setId(endpoint.getId());
                        glanceEndpoint.setInterfaceName(endpoint.getInterfaceName());
                        glanceEndpoint.setProjectId(endpoint.getProjectId());
                        glanceEndpoint.setRegion(endpoint.getRegion());
                        glanceEndpoint.setUrl(endpoint.getUrl() + "/" + jsonVersion.getId() + "/");
                        version = v;
                        break;
                    }
                }
            }
        }

        if (glanceEndpoint == null) {
            throw new NoSupportedApiVersion("Can't find a supported API version");
        }
        return new GlanceApiImpl(version, openstackApi, glanceEndpoint);
    }

    private static void getVersionsFromEndpoint(final OpenstackApi openstackApi, final List<JsonEndpoint> jsonEndpoints,
            final Map<JsonEndpoint, List<JsonVersion>> jsonVersionsMap) {
        RestTemplate restTemplate = openstackApi.getRestManager().getRestTemplate(GlanceApi.SERVICE_NAME);
        for (JsonEndpoint jsonEndpoint : jsonEndpoints) {
            ResponseEntity<JsonVersions> resp;
            try {
                resp = restTemplate.getForEntity(new URI(jsonEndpoint.getUrl()), JsonVersions.class);
                JsonVersions details = resp.getBody();
                jsonVersionsMap.put(jsonEndpoint, details.getVersions());
            } catch (RestClientException e) {
                throw e;
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Get the set of available versions based on the end points supplied.
     *
     * @param jsonEndpoints end points to get the version on
     * @return the set of versions available.
     */
    public static Set<String> getAvailableVersions(final List<JsonEndpoint> jsonEndpoints) {
        Set<String> versions = new HashSet<>();
        for (JsonEndpoint jsonEndpoint : jsonEndpoints) {
            URL url;
            try {
                url = new URL(jsonEndpoint.getUrl());
                String[] elements = url.getPath().split("/");
                if (elements.length >= 2) {
                    versions.add(elements[1]);
                }
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return versions;
    }
}
