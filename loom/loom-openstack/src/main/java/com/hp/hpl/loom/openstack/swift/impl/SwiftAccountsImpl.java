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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.hp.hpl.loom.adapter.AuthenticationFailureException;
import com.hp.hpl.loom.openstack.OpenstackApi;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonEndpoint;
import com.hp.hpl.loom.openstack.swift.SwiftAccounts;
import com.hp.hpl.loom.openstack.swift.model.JsonAccount;
import com.hp.hpl.loom.openstack.swift.model.JsonContainer;

/**
 * The swift accounts implementation. It reads the account information by making a HEAD call to the
 * get containers call.
 */
public class SwiftAccountsImpl extends SwiftBase<JsonAccount> implements SwiftAccounts {
    private static final Log LOG = LogFactory.getLog(SwiftAccountsImpl.class);

    /**
     * Constructor that takes the end point to use and an openstackApp to lookup the RestTemplate.
     *
     * @param openstackApp the openstackApp used to access the rest templates and token holder
     * @param jsonEndpoint the end point to access
     */
    public SwiftAccountsImpl(final OpenstackApi openstackApp, final JsonEndpoint jsonEndpoint) {
        super(openstackApp, jsonEndpoint);
    }

    @Override
    public String getUri() {
        return jsonEndpoint.getUrl();
    }

    @Override
    public JsonAccount getJsonResources() {
        JsonAccount jsonAccount = null;
        RestTemplate rt = getRestTemplate(jsonEndpoint.getProjectId());
        String resourcesUri = getUri();
        try {
            URI swiftURI = new URI(resourcesUri);
            HttpHeaders headers = getResourcesFromHead(rt, swiftURI);
            jsonAccount = buildFromHeaders(headers);
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
        return jsonAccount;
    }

    private JsonAccount buildFromHeaders(final HttpHeaders headers) throws URISyntaxException {
        JsonAccount jsonAccount = new JsonAccount();

        List<String> containerCount = headers.get("X-Account-Container-Count");
        List<String> objectCount = headers.get("X-Account-Object-Count");
        List<String> accountBytesUsed = headers.get("X-Account-Bytes-Used");

        if (containerCount != null && containerCount.size() > 0) {
            jsonAccount.setContainerCount(Integer.parseInt(containerCount.get(0)));
        }
        if (objectCount != null && objectCount.size() > 0) {
            jsonAccount.setObjectCount(Integer.parseInt(objectCount.get(0)));
        }
        if (accountBytesUsed != null && accountBytesUsed.size() > 0) {
            jsonAccount.setBytesUsed(Integer.parseInt(accountBytesUsed.get(0)));
        }
        return jsonAccount;
    }

    protected HttpHeaders getResourcesFromHead(final RestTemplate rt, final URI targetURI) throws URISyntaxException {
        HttpHeaders resp = rt.headForHeaders(targetURI);

        if (resp != null) {
            return resp;
        } else {
            throw new URISyntaxException(targetURI.toString(),
                    "UpdaterHttpException(\"unable to collect projects - HTTP response was null\");");
        }
    }


    @Override
    protected Class<JsonAccount> getTypeClass() {
        return JsonAccount.class;
    }


    @Override
    protected String getUriSuffix() {
        return null;
    }

    @Override
    public JsonAccount getJsonResourcesWithContainer() {
        JsonAccount jsonAccount = null;
        RestTemplate rt = getRestTemplate(jsonEndpoint.getProjectId());


        String resourcesUri = getUri();
        try {
            URI swiftUrl = new URI(resourcesUri);
            if (LOG.isDebugEnabled()) {
                LOG.debug("GET to " + swiftUrl.toString());
            }
            ResponseEntity<JsonContainer[]> resp = getResourcesFromGetContainer(rt, swiftUrl);

            jsonAccount = this.buildFromHeaders(resp.getHeaders());

            // extra linkedlist is required as asList is unmodifiable
            JsonContainer[] data = resp.getBody();
            List<JsonContainer> extra = new LinkedList<>(Arrays.asList(data));
            if (extra.size() != 0) {
                // check for more
                List<JsonContainer> more = getMore(jsonAccount, rt, resourcesUri, data);
                extra.addAll(more);
            }
            JsonContainer[] result = new JsonContainer[extra.size()];
            result = extra.toArray(result);
            jsonAccount.setContainers(result);
        } catch (URISyntaxException | UnsupportedEncodingException ex) {
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
        return jsonAccount;
    }

    private List<JsonContainer> getMore(final JsonAccount jsonAccount, final RestTemplate rt, final String url,
            final JsonContainer[] currentData) throws URISyntaxException, UnsupportedEncodingException {
        JsonContainer last = currentData[currentData.length - 1];
        List<JsonContainer> list = new ArrayList<>();
        if (currentData.length != jsonAccount.getContainerCount()) {
            String params = url.contains("?") ? "&" : "?";
            String newUrl = url + params + "marker=" + URLEncoder.encode(last.getName(), "UTF-8");
            URI keystoneURI = new URI(newUrl);
            JsonContainer[] data = getResourcesFromGetContainer(rt, keystoneURI).getBody();
            list.addAll(Arrays.asList(data));
            if (data.length != 0) {
                // check for more
                List<JsonContainer> extra = getMore(jsonAccount, rt, url, data);
                list.addAll(extra);
            }
        }
        return list;
    }

    protected ResponseEntity<JsonContainer[]> getResourcesFromGetContainer(final RestTemplate rt, final URI targetURI) {
        ResponseEntity<JsonContainer[]> resp = rt.getForEntity(targetURI, JsonContainer[].class);

        if (resp != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("response is not null: " + resp.getStatusCode());
            }
            if (resp.getStatusCode() == HttpStatus.OK) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("response is OK");
                }

                return resp;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    // private List<JsonContainer> getMore(final RestTemplate rt, final String url, final
    // JsonContainer last)
    // throws URISyntaxException, UnsupportedEncodingException {
    // String params = url.contains("?") ? "&" : "?";
    // String newUrl = url + params + "marker=" + URLEncoder.encode(last.getName(), "UTF-8");
    // URI keystoneURI = new URI(newUrl);
    // JsonContainer[] data = getResourcesFromGet(rt, keystoneURI);
    // List<JsonContainer> list = new ArrayList<>();
    // list.addAll(Arrays.asList(data));
    // if (data.length != 0) {
    // // check for more
    // List<JsonContainer> extra = getMore(rt, url, data[data.length - 1]);
    // list.addAll(extra);
    // }
    // return list;
    // }
}
