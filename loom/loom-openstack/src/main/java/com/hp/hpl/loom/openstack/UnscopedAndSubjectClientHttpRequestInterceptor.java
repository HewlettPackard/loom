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

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.hp.hpl.loom.openstack.keystonev3.TokenManager;

/**
 * Interceptor for the client call that adds in the X-Auth-Token and X-Subject-Token unscoped token
 * to the call.
 */
public class UnscopedAndSubjectClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {
    private TokenManager tokenManager;

    /**
     * Constructor that takes the tokenManager to look the X-Auth-Token and X-Subject-Tokenup from.
     *
     * @param tokenManager the tokenManager to look up the X-Auth-Token and X-Subject-Token from.
     */
    public UnscopedAndSubjectClientHttpRequestInterceptor(final TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
            final ClientHttpRequestExecution execution) throws IOException {
        HttpHeaders headers = request.getHeaders();
        headers.add("X-Auth-Token", tokenManager.getTokenHolder().getUnscoped());
        headers.add("X-Subject-Token", tokenManager.getTokenHolder().getUnscoped());
        return execution.execute(request, body);
    }
}
