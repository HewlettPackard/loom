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

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Abstract api base class - contains the abstract methods all the API's support.
 *
 * @param <R> resource this API returns
 */
public abstract class BaseApi<R> {
    private static final Log LOG = LogFactory.getLog(BaseApi.class);
    protected OpenstackApi openstackApp;

    /**
     * Constructor that takes the openstackApi reference. This is required so that sub classes can
     * lookup the rest templates.
     *
     * @param openstackApp the openstackApi shared by all the API's
     */
    public BaseApi(final OpenstackApi openstackApp) {
        this.openstackApp = openstackApp;
    }

    /**
     * Get the resources for a given URL. It can throw a number of RuntimeExceptions (connection not
     * found etc - which are all wrapped in a RestClientException).
     *
     * @param rt the RestTemplate to use
     * @param targetURI the url to access
     * @return the returns object or null if not found
     */
    public R getResourcesFromGet(final RestTemplate rt, final URI targetURI) {
        ResponseEntity<R> resp = rt.getForEntity(targetURI, getTypeClass());

        if (resp != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("response is not null: " + resp.getStatusCode());
            }
            if (resp.getStatusCode() == HttpStatus.OK) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("response is OK");
                }
                this.processHeaders(targetURI, resp.getHeaders());
                return resp.getBody();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }


    protected abstract Class<R> getTypeClass();

    protected abstract String getUriSuffix();

    protected void processHeaders(final URI targetURI, final HttpHeaders headers) {}

    // /**
    // * @return returns the ServiceName for that this API access.
    // */
    // public abstract String getServiceName();
}
