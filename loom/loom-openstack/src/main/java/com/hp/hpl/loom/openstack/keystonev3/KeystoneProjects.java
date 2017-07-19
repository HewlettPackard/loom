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


import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.hp.hpl.loom.adapter.AuthenticationFailureException;
import com.hp.hpl.loom.openstack.OpenstackApi;
import com.hp.hpl.loom.openstack.common.EndpointUtils;
import com.hp.hpl.loom.openstack.common.RestUtils;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonAuth;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonProjects;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonTokens;

/**
 * The keystone projects implementation.
 */
public class KeystoneProjects extends KeystoneBase<JsonProjects> {
    private static final Log LOG = LogFactory.getLog(KeystoneProjects.class);

    /**
     * An implementation of the Keystone Projects API.
     *
     * @param openstackApp the openstackApi for looking up tokens
     */
    public KeystoneProjects(final OpenstackApi openstackApp) {
        super(openstackApp);
    }

    @Override
    protected String getUriSuffix() {
        return "project";
    }

    @Override
    protected Class<JsonProjects> getTypeClass() {
        return JsonProjects.class;
    }

    /**
     * Authenticate for the given baseUrl, username and password.
     *
     * @param baseUrl keystone url to try authenticating against
     * @param username username
     * @param password password
     * @return true if it was successful
     */
    public boolean authenticate(final String baseUrl, final String username, final String password) {
        boolean allowed = false;
        try {
            URI keystoneTokenURI = EndpointUtils.keystoneTokenURINoCatalog(baseUrl);
            RestTemplate rt = openstackApp.getRestManager().getRestTemplate("keystone-auth");
            JsonAuth auth = KeystoneUtils.getUnscopedAuth(username, password);
            LOG.info("AUTH " + keystoneTokenURI);
            LOG.info("AUTH " + RestUtils.toJson(auth));
            ResponseEntity<JsonTokens> resp = rt.postForEntity(keystoneTokenURI, auth, JsonTokens.class);
            if (resp != null) {
                if (resp.getStatusCode() == HttpStatus.CREATED) {
                    String token = resp.getHeaders().getFirst("X-Subject-Token");
                    LOG.info("AUTH token1 " + token);

                    if (token != null) {
                        TokenHolder tokenHolder = new TokenHolder(token);
                        openstackApp.getTokenManager().setTokenHolder(tokenHolder);
                        openstackApp.getTokenManager().setJsonUser(resp.getBody().getToken().getUser());
                        LOG.info("AUTH token2 " + resp.getBody().getToken());
                        allowed = true;
                    }
                }
            }
        } catch (HttpClientErrorException ex) {
            LOG.error("authentication refused for user: " + username + " - " + ex.getMessage());
        }
        return allowed;
    }

    /**
     * Log this user out (based on the token in the TokenHolder).
     *
     * @param baseUrl keystone url to logout with
     * @return true if it was successful.
     */
    public boolean logout(final String baseUrl) {
        boolean success = false;
        try {
            URI keystoneTokenURI = EndpointUtils.keystoneTokenURINoCatalog(baseUrl);
            RestTemplate rt = openstackApp.getRestTemplateWithUnscopedAndSubjectToken();
            rt.delete(keystoneTokenURI);
            success = true;
        } catch (RestClientException ex) {
            LOG.error("Revoke failed!!! " + ex.getMessage());
            success = false;
        }
        return success;
    }

    /**
     * Get the user's projects for a given userId.
     *
     * @param userId the userId to lookup with
     * @return the projects for that user
     */
    public JsonProjects getUsersProjects(final String userId) {

        RestTemplate rt = openstackApp.getRestTemplateWithUnscopedToken();
        String resourcesUri = openstackApp.getKeystoneUriBase() + "/users/" + userId + "/projects";
        try {
            URI keystoneURI = new URI(resourcesUri);
            return getResourcesFromGet(rt, keystoneURI);
        } catch (URISyntaxException ex) {
            LOG.error("unable to build resourcesUri: " + resourcesUri);
        } catch (HttpStatusCodeException ex) {
            LOG.error("unable to obtain resources for: " + resourcesUri + " - " + ex.getMessage());
            // check if it's a 401
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new AuthenticationFailureException(ex);
            } else {
                throw ex;
            }
        }
        return new JsonProjects();
    }
}
