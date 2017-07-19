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
package com.hp.hpl.loom.openstack.keystonev3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.loom.openstack.keystonev3.model.JsonEndpoint;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonEndpoints;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonToken;

/**
 * Hold the scoped and unscoped tokens for a given user.
 */
public class TokenHolder {

    private String unscoped;
    private Map<String, String> scopedMap = new HashMap<>();
    private TokenServiceCatalog tokenServiceCatalog = new TokenServiceCatalog();


    /**
     * Construct a new holder for given unscoped token.
     *
     * @param unscoped the unscoped token
     */
    public TokenHolder(final String unscoped) {
        super();
        this.unscoped = unscoped;
    }

    /**
     * @return the unscoped token
     */
    public String getUnscoped() {
        return unscoped;
    }

    /**
     * @param unscoped unscoped token to set
     */
    public void setUnscoped(final String unscoped) {
        this.unscoped = unscoped;
    }

    /**
     * Set the projectId, projectName, scopedToken and service catalog.
     *
     * @param projectId the project Id to store
     * @param projectName the project Name to store
     * @param scopedToken the scoped token
     * @param serviceCatalog the service catalog
     */
    public void setScopedToken(final String projectId, final String projectName, final String scopedToken,
            final JsonToken serviceCatalog) {
        scopedMap.put(projectId, scopedToken);
        tokenServiceCatalog.saveJsonToken(projectId, projectName, serviceCatalog);
    }

    /**
     * Get the scoped token for the given projectId.
     *
     * @param projectId the project id
     * @return the scoped token
     */
    public String getScopedToken(final String projectId) {
        return scopedMap.get(projectId);
    }

    /**
     * Get the JsonToken for the given project id.
     *
     * @param projectId the project Id
     * @return the jsonToken
     */
    public JsonToken getJsonTokenScoped(final String projectId) {
        return tokenServiceCatalog.getServiceCatalogProjectId(projectId);
    }

    /**
     * Get the JsonToken for a given project name.
     *
     * @param projectName the project name
     * @return the json token
     */
    public JsonToken getJsonTokenScopedByProjectName(final String projectName) {
        return tokenServiceCatalog.getServiceCatalogForProjectName(projectName);
    }

    /**
     * Get all the scoped project ids.
     *
     * @return the scoped project ids
     */
    public Collection<String> getAllScopedProjectIds() {
        return scopedMap.keySet();
    }

    /**
     * Get the region ids for a given project id, type and interface name.
     *
     * @param projectId the project id
     * @param type the interface type
     * @param name the interface name
     * @return the array of region ids.
     */
    public String[] getRegions(final String projectId, final String type, final String name) {
        List<String> regionIds = new ArrayList<>(0);

        JsonToken jsonToken = getJsonTokenScoped(projectId);
        if (jsonToken != null) {
            List<JsonEndpoints> endpoints = jsonToken.getCatalog();
            for (JsonEndpoints jsonEndpoints : endpoints) {
                if (jsonEndpoints.getType().equals(type)) {
                    List<JsonEndpoint> endpoint = jsonEndpoints.getEndpoints();
                    for (JsonEndpoint jsonEndpoint : endpoint) {
                        if (jsonEndpoint.getInterfaceName().equals(name)) {
                            regionIds.add(jsonEndpoint.getRegion());
                        }
                    }
                }
            }
        }
        String[] returnIds = new String[regionIds.size()];
        int i = 0;
        for (String regionId : regionIds) {
            returnIds[i] = regionId;
            i++;
        }

        return returnIds;
    }

    /**
     * Get the region ids for a given project name and interface name.
     *
     * @param projectName the project name
     * @param name the interface name
     * @return the array of region ids.
     */
    public Set<String> getRegions(final String projectName, final String name) {
        Set<String> regionIds = new HashSet<>(0);

        JsonToken jsonToken = getJsonTokenScopedByProjectName(projectName);
        if (jsonToken != null) {
            List<JsonEndpoints> endpoints = jsonToken.getCatalog();
            for (JsonEndpoints jsonEndpoints : endpoints) {
                List<JsonEndpoint> endpoint = jsonEndpoints.getEndpoints();
                for (JsonEndpoint jsonEndpoint : endpoint) {
                    if (jsonEndpoint.getInterfaceName().equals(name)) {
                        regionIds.add(jsonEndpoint.getRegion());
                    }
                }
            }
        }
        return regionIds;
    }

    /**
     * Class to hold the map of the service catalogs to project id and name.
     */
    class TokenServiceCatalog {
        private Map<String, JsonToken> scopedJsonTokenMap = new HashMap<>();
        private Map<String, JsonToken> scopedByProjectNameJsonTokenMap = new HashMap<>();

        public void saveJsonToken(final String projectId, final String projectName, final JsonToken serviceCatalog) {
            scopedJsonTokenMap.put(projectId, serviceCatalog);
            scopedByProjectNameJsonTokenMap.put(projectName, serviceCatalog);
        }

        public JsonToken getServiceCatalogForProjectName(final String projectName) {
            return scopedByProjectNameJsonTokenMap.get(projectName);
        }

        public JsonToken getServiceCatalogProjectId(final String projectId) {
            return scopedJsonTokenMap.get(projectId);
        }
    }

}
