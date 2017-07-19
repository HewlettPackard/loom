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
package com.hp.hpl.loom.openstack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.client.RestTemplate;

import com.hp.hpl.loom.openstack.cinder.CinderApi;
import com.hp.hpl.loom.openstack.cinder.CinderApiFactory;
import com.hp.hpl.loom.openstack.common.RestManager;
import com.hp.hpl.loom.openstack.glance.GlanceApi;
import com.hp.hpl.loom.openstack.glance.GlanceApiFactory;
import com.hp.hpl.loom.openstack.keystonev3.KeystoneApi;
import com.hp.hpl.loom.openstack.keystonev3.TokenManager;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonEndpoint;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonEndpoints;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonToken;
import com.hp.hpl.loom.openstack.neutron.NeutronApi;
import com.hp.hpl.loom.openstack.neutron.NeutronApiFactory;
import com.hp.hpl.loom.openstack.nova.NovaApi;
import com.hp.hpl.loom.openstack.nova.NovaApiFactory;
import com.hp.hpl.loom.openstack.swift.SwiftApi;
import com.hp.hpl.loom.openstack.swift.SwiftApiFactory;

/**
 * OpenstackApi interface.
 */
public class OpenstackApi {
    private static final Log LOG = LogFactory.getLog(OpenstackApi.class);
    private RestManager restManager = null;
    private TokenManager tokenManager = null;

    private Map<String, NeutronApi> neutronApiCache = new HashMap<>();
    private Map<String, CinderApi> cinderApiCache = new HashMap<>();
    private Map<String, NovaApi> novaApiCache = new HashMap<>();
    private Map<String, SwiftApi> swiftApiCache = new HashMap<>();
    private Map<String, GlanceApi> glanceApiCache = new HashMap<>();

    // keystone
    private KeystoneApi keystoneApi = new KeystoneApi(this);

    private String baseUrl;
    private String username = null;

    /**
     * Create a new openstack api for a given baseUrl and username.
     *
     * @param baseUrl the keystone api
     * @param username the username to connect as
     */
    public OpenstackApi(final String baseUrl, final String username) {
        this.baseUrl = baseUrl;
        this.username = username;
        tokenManager = new TokenManager(username);
        restManager = new RestManager();
    }

    /**
     * Get a RestTemplate with an unscoped token (via the interceptor).
     *
     * @return the rest template
     */
    public RestTemplate getRestTemplateWithUnscopedToken() {
        RestTemplate rt = restManager.getRestTemplate("keystone-data");
        rt.setInterceptors(Collections.singletonList(new UnscopedClientHttpRequestInterceptor(tokenManager)));
        return rt;
    }

    /**
     * Get a RestTemplate with an scoped token (via the interceptor).
     *
     * @param projectId the project id
     * @return the rest template
     */
    public RestTemplate getRestTemplateWithScopedToken(final String projectId) {
        RestTemplate rt = restManager.getRestTemplate("scoped");
        rt.setInterceptors(Collections.singletonList(new ScopedClientHttpRequestInterceptor(projectId, tokenManager)));
        return rt;
    }

    /**
     * Get a RestTemplate with an UnscopedAndSubjectToken token (via the interceptor).
     *
     * @return the rest template
     */
    public RestTemplate getRestTemplateWithUnscopedAndSubjectToken() {
        RestTemplate rt = restManager.getRestTemplate("keystone-data");
        rt.setInterceptors(Collections.singletonList(new UnscopedAndSubjectClientHttpRequestInterceptor(tokenManager)));
        return rt;
    }

    /**
     * Get all the public end points for a given regionId, projectId and sericeName.
     *
     * @param regionId the regionId
     * @param projectId the projectId
     * @param serviceName the serviceName
     * @return the list of end points.
     */
    public List<JsonEndpoint> getPublicEndPoints(final String regionId, final String projectId,
            final String serviceName) {
        List<JsonEndpoint> results = new ArrayList<>();

        JsonToken jsonToken = this.getTokenManager().getTokenHolder().getJsonTokenScoped(projectId);
        List<JsonEndpoints> endpoints = jsonToken.getCatalog();
        for (JsonEndpoints jsonEndpoints : endpoints) {
            if (jsonEndpoints.getType().equals(serviceName)) {
                List<JsonEndpoint> endpoint = jsonEndpoints.getEndpoints();
                for (JsonEndpoint jsonEndpoint : endpoint) {
                    if (regionId.endsWith(jsonEndpoint.getRegion())
                            && jsonEndpoint.getInterfaceName().equals("public")) {
                        results.add(jsonEndpoint);
                        jsonEndpoint.setProjectId(projectId);
                    }
                }
            }
        }
        return results;
    }

    /**
     * Get the keystone uri base.
     *
     * @return the keystone uri
     */
    public String getKeystoneUriBase() {
        return baseUrl;
    }

    /**
     * Authenticate with a given password.
     *
     * @param password the password
     * @return true if successful
     */
    public boolean authenticate(final String password) {
        return keystoneApi.getKeystoneProject().authenticate(baseUrl, username, password);
    }

    /**
     * @return Get the RestManager
     */
    public RestManager getRestManager() {
        return restManager;
    }

    /**
     * Set the proxy.
     *
     * @param proxyHost the proxyHost
     * @param proxyPort the proxyPort
     */
    public void setProxy(final String proxyHost, final int proxyPort) {
        if (LOG.isInfoEnabled()) {
            LOG.info("setting proxy to: " + proxyHost + "/ " + proxyPort);
        }
        restManager.setProxyValues(proxyHost, proxyPort);
    }

    /**
     * @return the tokenManager
     */
    public TokenManager getTokenManager() {
        return tokenManager;
    }

    /**
     * @return the keystoneApi
     */
    public KeystoneApi getKeystoneApi() {
        return keystoneApi;
    }

    /**
     * Get the CinderApi for a given array of versions, projectId, regionId.
     *
     * @param versions Array of acceptable versions
     * @param projectId project id to lookup for
     * @param regionId region id to lookup for
     * @return the CinderApi
     * @throws NoSupportedApiVersion thrown if no version can be found
     */
    public CinderApi getCinderApi(final String[] versions, final String projectId, final String regionId)
            throws NoSupportedApiVersion {
        String key = projectId + regionId;
        CinderApi cinderApi = cinderApiCache.get(key);
        if (cinderApi == null) {
            List<JsonEndpoint> jsonEndpoints = getPublicEndPoints(regionId, projectId, CinderApi.SERVICE_NAME);
            cinderApi = CinderApiFactory.getApi(this, jsonEndpoints, versions);
            cinderApiCache.put(key, cinderApi);
        }
        return cinderApi;
    }

    /**
     * Get the NovaApi for a given array of versions, projectId, regionId.
     *
     * @param versions Array of acceptable versions
     * @param projectId project id to lookup for
     * @param regionId region id to lookup for
     * @return the NovaApi
     * @throws NoSupportedApiVersion thrown if no version can be found
     */
    public NovaApi getNovaApi(final String[] versions, final String projectId, final String regionId)
            throws NoSupportedApiVersion {
        String key = projectId + regionId;
        NovaApi novaApi = novaApiCache.get(key);
        if (novaApi == null) {
            List<JsonEndpoint> jsonEndpoints = getPublicEndPoints(regionId, projectId, NovaApi.SERVICE_NAME);
            novaApi = NovaApiFactory.getApi(this, jsonEndpoints, versions);
            novaApiCache.put(key, novaApi);
        }
        return novaApi;
    }

    /**
     * Get the NeutronApi for a given array of versions, projectId, regionId.
     *
     * @param versions Array of acceptable versions
     * @param projectId project id to lookup for
     * @param regionId region id to lookup for
     * @return the NeutronApi
     * @throws NoSupportedApiVersion thrown if no version can be found
     */
    public NeutronApi getNeutronApi(final String[] versions, final String projectId, final String regionId)
            throws NoSupportedApiVersion {
        String key = projectId + regionId;
        NeutronApi neutronApi = neutronApiCache.get(key);
        if (neutronApi == null) {
            List<JsonEndpoint> jsonEndpoints = getPublicEndPoints(regionId, projectId, NeutronApi.SERVICE_NAME);
            neutronApi = NeutronApiFactory.getApi(this, jsonEndpoints, versions);
            neutronApiCache.put(key, neutronApi);
        }
        return neutronApi;
    }

    /**
     * Get the SwiftApi for a given array of versions, projectId, regionId.
     *
     * @param versions Array of acceptable versions
     * @param projectId project id to lookup for
     * @param regionId region id to lookup for
     * @return the SwiftApi
     * @throws NoSupportedApiVersion thrown if no version can be found
     */
    public SwiftApi getSwiftApi(final String[] versions, final String projectId, final String regionId)
            throws NoSupportedApiVersion {
        String key = projectId + regionId;
        SwiftApi swiftApi = swiftApiCache.get(key);
        if (swiftApi == null) {
            List<JsonEndpoint> jsonEndpoints = getPublicEndPoints(regionId, projectId, SwiftApi.SERVICE_NAME);
            swiftApi = SwiftApiFactory.getApi(this, jsonEndpoints, versions);
            swiftApiCache.put(key, swiftApi);
        }
        return swiftApi;
    }

    /**
     * Get the GlanceApi for a given array of versions, projectId, regionId.
     *
     * @param versions Array of acceptable versions
     * @param projectId project id to lookup for
     * @param regionId region id to lookup for
     * @return the GlanceApi
     * @throws NoSupportedApiVersion thrown if no version can be found
     */
    public GlanceApi getGlanceApi(final String[] versions, final String projectId, final String regionId)
            throws NoSupportedApiVersion {
        String key = projectId + regionId;
        GlanceApi glanceApi = glanceApiCache.get(key);
        if (glanceApi == null) {
            List<JsonEndpoint> jsonEndpoints = getPublicEndPoints(regionId, projectId, GlanceApi.SERVICE_NAME);
            glanceApi = GlanceApiFactory.getApi(this, jsonEndpoints, versions);
            glanceApiCache.put(key, glanceApi);
        }
        return glanceApi;
    }


    /**
     * Get the available versions for a given projectId, regionId and serviceName.
     *
     * @param projectId the projectId to lookup
     * @param regionId the regionId to lookup
     * @param serviceName the serviceName
     * @return the Set of the available versions
     */
    public Set<String> getAvailableVersions(final String projectId, final String regionId, final String serviceName) {
        Set<String> versions = new HashSet<>();
        if (SwiftApi.SERVICE_NAME.equals(serviceName)) {
            List<JsonEndpoint> jsonEndpoints = getPublicEndPoints(regionId, projectId, SwiftApi.SERVICE_NAME);
            versions = SwiftApiFactory.getAvailableVersions(jsonEndpoints);
        } else if (NeutronApi.SERVICE_NAME.equals(serviceName)) {
            List<JsonEndpoint> jsonEndpoints = getPublicEndPoints(regionId, projectId, NeutronApi.SERVICE_NAME);
            versions = NeutronApiFactory.getAvailableVersions(this, jsonEndpoints);
        } else if (NovaApi.SERVICE_NAME.equals(serviceName)) {
            List<JsonEndpoint> jsonEndpoints = getPublicEndPoints(regionId, projectId, NovaApi.SERVICE_NAME);
            versions = NovaApiFactory.getAvailableVersions(jsonEndpoints);
        } else if (CinderApi.SERVICE_NAME.equals(serviceName)) {
            List<JsonEndpoint> jsonEndpoints = getPublicEndPoints(regionId, projectId, CinderApi.SERVICE_NAME);
            versions = CinderApiFactory.getAvailableVersions(jsonEndpoints);
        }
        return versions;
    }

}
