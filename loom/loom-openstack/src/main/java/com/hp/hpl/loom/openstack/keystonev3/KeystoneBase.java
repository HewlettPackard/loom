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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.hp.hpl.loom.openstack.BaseApi;
import com.hp.hpl.loom.openstack.OpenstackApi;
import com.hp.hpl.loom.openstack.common.EndpointUtils;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonAuth;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonToken;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonTokens;

/**
 * Base class for the keystone API.
 *
 * @param <R> object this API returns
 */
public abstract class KeystoneBase<R> extends BaseApi<R> {
    private static final int MAX_HEADER_SIZE = 9000;
    private static final Log LOG = LogFactory.getLog(KeystoneBase.class);

    /**
     * @param openstackApp the openstackApp used to access the rest templates and token holder
     */
    public KeystoneBase(final OpenstackApi openstackApp) {
        super(openstackApp);
    }

    /**
     * Gets the service catalog for a given project id and stores it with the project name.
     *
     * @param projectId project id to get catalog for
     * @param projectName project name to get catalog for
     */
    public void getServiceCatalog(final String projectId, final String projectName) {
        try {
            URI keystoneTokenURI = EndpointUtils.keystoneTokenURIWithCatalog(openstackApp.getKeystoneUriBase());

            RestTemplate rt = openstackApp.getRestManager().getRestTemplate("keystone-auth");
            JsonAuth auth = KeystoneUtils.getScopedAuth(openstackApp.getTokenManager().getTokenHolder().getUnscoped(),
                    projectId);

            ResponseEntity<JsonTokens> resp = rt.postForEntity(keystoneTokenURI, auth, JsonTokens.class);
            String token = null;
            JsonToken jsonToken = null;
            if (resp != null) {
                if (resp.getStatusCode() == HttpStatus.CREATED) {
                    token = resp.getHeaders().getFirst("X-Subject-Token");
                    jsonToken = resp.getBody().getToken();
                }
            }

            if (token != null && token.length() > MAX_HEADER_SIZE) {
                keystoneTokenURI = EndpointUtils.keystoneTokenURINoCatalog(openstackApp.getKeystoneUriBase());

                resp = rt.postForEntity(keystoneTokenURI, auth, JsonTokens.class);
                if (resp != null) {
                    if (resp.getStatusCode() == HttpStatus.CREATED) {
                        token = resp.getHeaders().getFirst("X-Subject-Token");
                    }
                }
            }
            if (token != null && jsonToken != null) {
                openstackApp.getTokenManager().getTokenHolder().setScopedToken(projectId, projectName, token,
                        jsonToken);
            }
        } catch (HttpClientErrorException ex) {
            LOG.error("authentication refused for user: " + projectId + " - " + ex.getMessage());
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("authenticate call: and we're done...");
        }
    }

    @Override
    protected abstract Class<R> getTypeClass();
}
