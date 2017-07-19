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
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;

import com.hp.hpl.loom.openstack.OpenstackApi;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonEndpoint;
import com.hp.hpl.loom.openstack.swift.SwiftObjects;
import com.hp.hpl.loom.openstack.swift.model.JsonObject;


/**
 * The swift object API implementation. It will recursively load the contents of the container.
 */
public class SwiftObjectsImpl extends SwiftPagingBase<JsonObject[], JsonObject> implements SwiftObjects {
    private static final Log LOG = LogFactory.getLog(SwiftObjectsImpl.class);

    /**
     * @param openstackApp the openstackApp used to access the rest templates and token holder
     * @param jsonEndpoint the end point to access
     */
    public SwiftObjectsImpl(final OpenstackApi openstackApp, final JsonEndpoint jsonEndpoint) {
        super(openstackApp, jsonEndpoint);
    }

    @Override
    public Iterator<? extends JsonObject> getIterator(final String containerId) {
        final String objectUri = getUri(containerId);
        return this.getIteratorUsingUri(objectUri);
    }

    @Override
    protected void processHeaders(final URI uri, final HttpHeaders headers) {
        int maxSize = Integer.parseInt(headers.get("X-Container-Object-Count").get(0));
        maxSizes.put(uri.toString(), maxSize);
    }

    /**
     * Constructs a string of URL based on the end point and the containerId.
     *
     * @param containerId the container id to include in the URL.
     * @return the string of URL
     */
    public String getUri(final String containerId) {
        String url = jsonEndpoint.getUrl();
        String resourcesUri = url + "/" + containerId + "?format=json";
        return resourcesUri;
    }


    @Override
    protected Class<JsonObject[]> getTypeClass() {
        return JsonObject[].class;
    }


    @Override
    protected String getUriSuffix() {
        return null;
    }

    @Override
    public String getUri() {
        return null;
    }

}
