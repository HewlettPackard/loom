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
package com.hp.hpl.loom.adapter.keystonev3;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.hp.hpl.loom.adapter.keystonev3.rest.RestManager;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.ProviderImpl;

public class KeystoneV3Provider extends ProviderImpl {
    private static final Log LOG = LogFactory.getLog(KeystoneV3Provider.class);

    protected URI keystoneTokenURI = null;

    RestManager restManager = RestManager.getInstance();

    public KeystoneV3Provider(final String providerType, final String providerId, final String authEndpoint,
            final String providerName, final String adapterPackage) {
        super(providerType, providerId, authEndpoint, providerName, adapterPackage);
        try {
            keystoneTokenURI = new URI(authEndpoint + "/auth/tokens");
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public boolean authenticate(final Credentials creds) {
        boolean allowed = false;
        try {
            LOG.info("authenticate call: starting...");
            RestTemplate rt = restManager.getRestTemplate("keystone-auth");
            LOG.info("posting to " + keystoneTokenURI.toString());
            ResponseEntity<String> resp =
                    rt.postForEntity(keystoneTokenURI, KeystoneUtils.getUnscopedAuth(creds), String.class);
            if (resp != null) {
                LOG.info("response is not null: " + resp.getStatusCode());
                if (resp.getStatusCode() == HttpStatus.CREATED) {
                    LOG.info("response is CREATED");
                    String token = resp.getHeaders().getFirst("X-Subject-Token");
                    if (token != null) {
                        LOG.info("response contains header X-Subject-Token with value: " + token);
                        TokenManager.getInstance().setTokenHolder(creds.getUsername(), new TokenHolder(token));
                        allowed = true;
                    }
                }
            }
        } catch (HttpClientErrorException ex) {
            // LOG.error(ex);
            // LOG.error("authentication refused for user: "+ creds.getUsername(),ex);
            LOG.error("authentication refused for user: " + creds.getUsername() + " - " + ex.getMessage());
        }
        LOG.info("authenticate call: and we're done...");
        return allowed;
    }
}
