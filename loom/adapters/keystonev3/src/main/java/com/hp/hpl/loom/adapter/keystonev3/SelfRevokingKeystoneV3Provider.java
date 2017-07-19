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

import java.io.IOException;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.hp.hpl.loom.model.Credentials;

public class SelfRevokingKeystoneV3Provider extends KeystoneV3Provider
        implements Runnable, ClientHttpRequestInterceptor {
    private static final int THREAD_SLEEP = 15000;

    private static final Log LOG = LogFactory.getLog(SelfRevokingKeystoneV3Provider.class);

    private String currentToken;

    public SelfRevokingKeystoneV3Provider(final String providerType, final String providerId, final String authEndpoint,
            final String providerName, final String adapterPackage) {
        super(providerType, providerId, authEndpoint, providerName, adapterPackage);
    }

    @Override
    public boolean authenticate(final Credentials creds) {
        boolean ok = super.authenticate(creds);
        if (ok) {
            Thread newThread = new Thread(this);
            newThread.setName("TokenRevoking-" + this.getProviderTypeAndId());
            newThread.start();
        }
        return ok;
    }

    @Override
    @SuppressWarnings("checkstyle:emptyblock")
    public void run() {
        LOG.warn("****** SelfRevokingProvider Thread going to sleep for 15 secs");
        try {
            Thread.sleep(THREAD_SLEEP);
        } catch (InterruptedException ie) {
        }
        LOG.warn("****** SelfRevokingProvider finished sleeping, revoking all tokens");
        try {
            RestTemplate rt = restManager.getRestTemplate("keystone-revoke");
            rt.setInterceptors(Collections.singletonList(this));
            LOG.warn("****** deleting " + keystoneTokenURI.toString());
            for (TokenHolder tokenHolder : TokenManager.getInstance().getAllTokenHolders()) {
                currentToken = tokenHolder.getUnscoped();
                LOG.warn("****** deleting token " + currentToken + "[" + keystoneTokenURI.toString() + "]");
                rt.delete(keystoneTokenURI);
            }
        } catch (HttpClientErrorException ex) {
            LOG.error("****** token revocation refused for token " + currentToken);
        }
        LOG.warn("****** all tokens have been revoked - SelfRevokingProvider Thread is exiting...");
    }

    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
            final ClientHttpRequestExecution execution) throws IOException {
        HttpHeaders headers = request.getHeaders();
        headers.add("X-Subject-Token", currentToken);
        headers.add("X-Auth-Token", currentToken);
        return execution.execute(request, body);
    }
}
