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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.hp.hpl.loom.adapter.AuthenticationFailureException;
import com.hp.hpl.loom.openstack.OpenstackApi;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonEndpoint;
import com.hp.hpl.loom.openstack.swift.SwiftContainer;
import com.hp.hpl.loom.openstack.swift.model.JsonContainer;

/**
 * The swift object API implementation. It will recursively load all the contents of the container.
 */
public class SwiftContainersImpl extends SwiftPagingBase<JsonContainer[], JsonContainer> implements SwiftContainer {
    private static final Log LOG = LogFactory.getLog(SwiftContainersImpl.class);

    /**
     * @param openstackApp the openstackApp used to access the rest templates and token holder
     * @param jsonEndpoint the end point to access
     */
    public SwiftContainersImpl(final OpenstackApi openstackApp, final JsonEndpoint jsonEndpoint) {
        super(openstackApp, jsonEndpoint);
    }

    @Override
    public Iterator<? extends JsonContainer> getIterator() {
        final String containerUri = getUri();
        return this.getIteratorUsingUri(containerUri);
    }

    @Override
    protected void processHeaders(final URI uri, final HttpHeaders headers) {
        List<String> items = headers.get("X-Container-Object-Count");
        if (items != null) {
            int maxSize = Integer.parseInt(items.get(0));
            maxSizes.put(uri.toString(), maxSize);
        } else {
            maxSizes.put(uri.toString(), 0);
        }
    }


    @Override
    public JsonContainer[] getJsonResources() {
        JsonContainer[] result = null;
        RestTemplate rt = getRestTemplate(jsonEndpoint.getProjectId());
        String resourcesUri = getUri();
        try {
            URI swiftUrl = new URI(resourcesUri);
            if (LOG.isDebugEnabled()) {
                LOG.debug("GET to " + swiftUrl.toString());
            }
            JsonContainer[] data = getResourcesFromGet(rt, swiftUrl);
            // extra linkedlist is required as asList is unmodifiable
            List<JsonContainer> extra = new LinkedList<>(Arrays.asList(data));
            if (extra.size() != 0) {
                // check for more
                List<JsonContainer> more = getMore(rt, resourcesUri, data[data.length - 1]);
                extra.addAll(more);
            }
            result = new JsonContainer[extra.size()];
            result = extra.toArray(result);
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
        return result;
    }

    private List<JsonContainer> getMore(final RestTemplate rt, final String url, final JsonContainer last)
            throws URISyntaxException, UnsupportedEncodingException {
        String params = url.contains("?") ? "&" : "?";
        String newUrl = url + params + "marker=" + URLEncoder.encode(last.getName(), "UTF-8");
        URI keystoneURI = new URI(newUrl);
        JsonContainer[] data = getResourcesFromGet(rt, keystoneURI);
        List<JsonContainer> list = new ArrayList<>();
        list.addAll(Arrays.asList(data));
        if (data.length != 0) {
            // check for more
            List<JsonContainer> extra = getMore(rt, url, data[data.length - 1]);
            list.addAll(extra);
        }
        return list;
    }


    @Override
    public String getUri() {
        return jsonEndpoint.getUrl() + "?format=json";
    }

    @Override
    public JsonContainer[] createContainer(final String containerName) {
        // JsonEndpoint jsonEndpoint = openstackApp.getPublicEndPoints(region, projectId, this);
        String url = jsonEndpoint.getUrl() + "/" + containerName;
        RestTemplate rt = getRestTemplate(jsonEndpoint.getProjectId());
        rt.put(url, null);
        return getJsonResources();
    }

    /**
     * Adds an object to this container.
     *
     * @param jsonContainer Container to add to
     * @param item String item to add
     */
    public void addObject(final JsonContainer jsonContainer, final String item) {
        // JsonEndpoint jsonEndpoint = openstackApp.getPublicEndPoints(region, projectId, this);
        String url = jsonEndpoint.getUrl() + "/" + jsonContainer.getName() + "/" + item;
        RestTemplate rt = getRestTemplate(jsonEndpoint.getProjectId());
        rt.put(url, item);
    }


    @Override
    protected Class<JsonContainer[]> getTypeClass() {
        return JsonContainer[].class;
    }


    @Override
    protected String getUriSuffix() {
        return null;
    }
}
