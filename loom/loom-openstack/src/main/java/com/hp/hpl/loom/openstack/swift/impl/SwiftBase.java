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
package com.hp.hpl.loom.openstack.swift.impl;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.hp.hpl.loom.adapter.AuthenticationFailureException;
import com.hp.hpl.loom.openstack.BaseApi;
import com.hp.hpl.loom.openstack.OpenstackApi;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonEndpoint;

/**
 * Base class for the swift API.
 *
 * @param <R> the type that this API returns.
 */
public abstract class SwiftBase<R> extends BaseApi<R> {
    private static final Log LOG = LogFactory.getLog(SwiftBase.class);
    protected JsonEndpoint jsonEndpoint;

    /**
     * @param openstackApp the openstackApp used to access the rest templates and token holder
     * @param jsonEndpoint the end point to access
     */
    public SwiftBase(final OpenstackApi openstackApp, final JsonEndpoint jsonEndpoint) {
        super(openstackApp);
        this.jsonEndpoint = jsonEndpoint;
    }

    protected RestTemplate getRestTemplate(final String projectId) {
        return openstackApp.getRestTemplateWithScopedToken(projectId);
    }

    /**
     * Gets the resource for this type.
     *
     * @return the R resource for this type.
     */
    public R getJsonResources() {
        R result = null;
        RestTemplate rt = getRestTemplate(jsonEndpoint.getProjectId());
        String resourcesUri = getUri();
        try {
            URI keystoneURI = new URI(resourcesUri);
            if (LOG.isDebugEnabled()) {
                LOG.debug("GET to " + keystoneURI.toString());
            }
            result = getResourcesFromGet(rt, keystoneURI);
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
        return result;
    }

    /**
     * Gets the URI (as a string) for this API.
     *
     * @return the URI (as a string) for this API
     */
    public abstract String getUri();
}
