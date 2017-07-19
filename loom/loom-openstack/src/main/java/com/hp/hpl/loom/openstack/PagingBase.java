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
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.hp.hpl.loom.adapter.AuthenticationFailureException;
import com.hp.hpl.loom.openstack.common.JsonPaging;
import com.hp.hpl.loom.openstack.common.Pagination;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonEndpoint;

/**
 * Extension on the BaseApi that handles the pagination.
 *
 * @param <R> the type it returns
 */
public abstract class PagingBase<R extends Pagination, T> extends BaseApi<R> {
    private static final Log LOG = LogFactory.getLog(PagingBase.class);
    private String limit = "";
    protected JsonEndpoint jsonEndpoint;

    /**
     * Constructor that takes the openstackApi reference. This is required so that sub classes can
     * lookup the rest templates.
     *
     * @param openstackApp the openstackApi shared by all the API's
     * @param jsonEndpoint the endpoint to connect to
     */
    public PagingBase(final OpenstackApi openstackApp, final JsonEndpoint jsonEndpoint) {
        super(openstackApp);
        this.jsonEndpoint = jsonEndpoint;
    }

    protected RestTemplate getRestTemplate(final String projectId) {
        return openstackApp.getRestTemplateWithScopedToken(projectId);
    }

    /**
     * @return the url to access
     */
    public abstract String getUri();

    protected void processNext(final R result, final RestTemplate rt) {
        String nextUrl = getNext(result);
        if (nextUrl != null) {
            R nextResults = getJsonResources(rt, nextUrl);
            addToResult(result, nextResults);
        }
    }

    public Iterator<T> getIterator() {
        Iterator<T> iterator = new Iterator<T>() {
            List<T> data = null;
            int counter = -1;
            URI resourcesURI;
            R result = null;

            @Override
            public boolean hasNext() {
                RestTemplate rt = getRestTemplate(jsonEndpoint.getProjectId());
                if (data == null) {
                    // perform initial lookup
                    String resourcesUri = getUri();
                    try {
                        URI keystoneURI = new URI(resourcesUri);
                        if (LOG.isInfoEnabled()) {
                            LOG.info("GET to " + keystoneURI.toString());
                        }
                        result = getResourcesFromGet(rt, keystoneURI);
                        List<T> list = getResults(result);
                        if (data == null) {
                            data = list;
                        } else {
                            data.addAll(list);
                        }
                    } catch (HttpStatusCodeException ex) {
                        LOG.error("unable to obtain resources for: " + resourcesUri + " - " + ex.getMessage());
                        // check if it's a 401
                        if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                            throw new AuthenticationFailureException(ex);
                        }
                    } catch (URISyntaxException ex) {
                        LOG.error("unable to build resourcesUri: " + resourcesUri + " " + ex.getMessage());
                    }
                }
                if ((counter + 1) < data.size()) {
                    return true;
                } else {
                    if (data.size() == 0) {
                        return false;
                    } else {
                        // lookup more or return false
                        String nextUrl = getNext(result);
                        if (nextUrl != null) {
                            R nextResults = getJsonResources(rt, nextUrl);
                            addToResult(result, nextResults);
                        }
                        if ((counter + 1) < data.size()) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
            }


            @Override
            public T next() {
                if (this.hasNext()) {
                    counter++;
                    return data.get(counter);
                } else {
                    return null;
                }

            }

        };
        return iterator;
    }


    // /**
    // * Get the resources.
    // *
    // * @return the R
    // */
    // public R getJsonResources() {
    // R result = null;
    // RestTemplate rt = getRestTemplate(jsonEndpoint.getProjectId());
    // String resourcesUri = getUri();
    // resourcesUri += limit;
    // try {
    // URI keystoneURI = new URI(resourcesUri);
    // if (LOG.isInfoEnabled()) {
    // LOG.info("GET to " + keystoneURI.toString());
    // }
    // result = getResourcesFromGet(rt, keystoneURI);
    // processNext(result, rt);
    // } catch (HttpStatusCodeException ex) {
    // LOG.error("unable to obtain resources for: " + resourcesUri + " - " + ex.getMessage());
    // // check if it's a 401
    // if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
    // throw new AuthenticationFailureException(ex);
    // }
    // } catch (URISyntaxException ex) {
    // LOG.error("unable to build resourcesUri: " + resourcesUri + " " + ex.getMessage());
    // }
    // return result;
    // }

    private R getJsonResources(final RestTemplate rt, final String nextUrl) {
        R result = null;
        try {
            URI keystoneURI = new URI(nextUrl);
            result = getResourcesFromGet(rt, keystoneURI);
            processNext(result, rt);

        } catch (URISyntaxException ex) {
            LOG.error("unable to build resourcesUri: " + nextUrl);
        } catch (HttpStatusCodeException ex) {
            LOG.error("unable to obtain resources for: " + nextUrl + " - " + ex.getMessage());
            // check if it's a 401
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new AuthenticationFailureException(ex);
            } else {
                throw ex;
            }
        }
        return result;
    }


    private String getNext(final R result) {
        String href = null;
        List<JsonPaging> links = result.getLinks();
        if (links != null) {
            for (JsonPaging jsonPaging : links) {
                if (jsonPaging.getRel().equals("next")) {
                    href = jsonPaging.getHref();
                }
            }
        }
        return href;
    }

    /**
     * Add the results to the other results.
     *
     * @param result the existing results
     * @param nextResults the next set of results
     */
    public abstract void addToResult(R result, R nextResults);

    public abstract List<T> getResults(R result);


}
