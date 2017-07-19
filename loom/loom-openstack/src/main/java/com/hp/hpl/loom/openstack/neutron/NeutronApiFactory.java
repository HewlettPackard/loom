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
package com.hp.hpl.loom.openstack.neutron;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
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
import com.hp.hpl.loom.openstack.keystonev3.model.JsonEndpoint;
import com.hp.hpl.loom.openstack.neutron.impl.NeutronApiImpl;
import com.hp.hpl.loom.openstack.neutron.model.JsonVersion;
import com.hp.hpl.loom.openstack.neutron.model.JsonVersions;

/**
 * A factory to construct NeutronApi's. The intention is that this can be extended to return
 * different implementation of the API based on the supplied end points and the required versions.
 */
public final class NeutronApiFactory {
    private static final Log LOG = LogFactory.getLog(NeutronApiFactory.class);

    private NeutronApiFactory() {}

    /**
     * Get the NeutronApi based on the versions required and supplied end points.
     *
     * @param openstackApi openstackApi to lookup the tokens
     * @param jsonEndpoints End points to try and connect to
     * @param versions required versions (any will do)
     * @return the NeutronApi
     * @throws NoSupportedApiVersion thrown if no version could be found
     */
    public static NeutronApi getApi(final OpenstackApi openstackApi, final List<JsonEndpoint> jsonEndpoints,
            final String[] versions) throws NoSupportedApiVersion {

        Map<JsonEndpoint, List<JsonVersion>> jsonVersionsMap = new HashMap<>();

        getVersionsFromEndpoint(openstackApi, jsonEndpoints, jsonVersionsMap);

        JsonEndpoint neutronEndpoint = null;
        String version = null;

        Set<JsonEndpoint> keys = jsonVersionsMap.keySet();
        for (JsonEndpoint endpoint : keys) {
            List<JsonVersion> jsonVersions = jsonVersionsMap.get(endpoint);
            for (JsonVersion jsonVersion : jsonVersions) {
                for (String v : versions) {
                    if (jsonVersion.getId().equals(v.trim())) {
                        neutronEndpoint = new JsonEndpoint();
                        neutronEndpoint.setId(endpoint.getId());
                        neutronEndpoint.setInterfaceName(endpoint.getInterfaceName());
                        neutronEndpoint.setProjectId(endpoint.getProjectId());
                        neutronEndpoint.setRegion(endpoint.getRegion());
                        neutronEndpoint.setUrl(endpoint.getUrl() + "/" + jsonVersion.getId() + "/");
                        version = v;
                        break;
                    }
                }
            }
        }

        if (neutronEndpoint == null) {
            throw new NoSupportedApiVersion("Can't find a supported API version");
        }
        return new NeutronApiImpl(version, openstackApi, neutronEndpoint);
    }

    private static void getVersionsFromEndpoint(final OpenstackApi openstackApi, final List<JsonEndpoint> jsonEndpoints,
            final Map<JsonEndpoint, List<JsonVersion>> jsonVersionsMap) {
        RestTemplate restTemplate = openstackApi.getRestManager().getRestTemplate(NeutronApi.SERVICE_NAME);
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
     * @param openstackApi the openstackAPI
     * @param jsonEndpoints end points to get the version on
     * @return the set of versions available.
     */
    public static Set<String> getAvailableVersions(final OpenstackApi openstackApi,
            final List<JsonEndpoint> jsonEndpoints) {
        Set<String> versions = new HashSet<>();
        Map<JsonEndpoint, List<JsonVersion>> jsonVersionsMap = new HashMap<>();
        getVersionsFromEndpoint(openstackApi, jsonEndpoints, jsonVersionsMap);

        Collection<List<JsonVersion>> jsonVersions = jsonVersionsMap.values();
        for (List<JsonVersion> list : jsonVersions) {
            for (JsonVersion jsonVersion : list) {
                versions.add(jsonVersion.getId());
            }
        }
        return versions;
    }
}
