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
package com.hp.hpl.loom.openstack.swift;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.openstack.NoSupportedApiVersion;
import com.hp.hpl.loom.openstack.OpenstackApi;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonEndpoint;
import com.hp.hpl.loom.openstack.swift.impl.SwiftApiImpl;

/**
 * A factory to construct SwiftApi's. The intention is that this can be extended to return different
 * implementation of the API based on the supplied end points and the required versions.
 */
public final class SwiftApiFactory {
    private static final Log LOG = LogFactory.getLog(SwiftApiFactory.class);

    private SwiftApiFactory() {}

    /**
     * Creates a swift API from the list of endpoints provided and version.
     *
     * Example end point URLs seen: - public
     * https://region-a.geo-1.objects.hpcloudsvc.com/v1/19952939207044 - public
     * https://region-b.geo-1.objects.hpcloudsvc.com/v1/19952939207044
     *
     * @param openstackApi the openstackApp used to access the rest templates and token holder
     * @param jsonEndpoints the end points to try accessing
     * @param versions the versions the caller is happy having
     * @return a swiftApi
     * @throws NoSupportedApiVersion if no version can be found that it supports
     */
    public static SwiftApi getApi(final OpenstackApi openstackApi, final List<JsonEndpoint> jsonEndpoints,
            final String[] versions) throws NoSupportedApiVersion {
        JsonEndpoint swiftEndpoint = null;
        String version = null;
        for (JsonEndpoint jsonEndpoint : jsonEndpoints) {
            URL url;
            try {
                url = new URL(jsonEndpoint.getUrl());
                for (String v : versions) {
                    if (url.getPath().toLowerCase().startsWith("/" + v.trim())) {
                        swiftEndpoint = jsonEndpoint;
                        version = v.trim();
                        break;
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        if (swiftEndpoint == null) {
            StringBuffer sb = new StringBuffer();
            for (String v : versions) {
                sb.append(" " + v);
            }
            if (LOG.isInfoEnabled()) {
                LOG.info("Couldn't find a suitable endpoint for swift");
                for (JsonEndpoint jsonEndpoint : jsonEndpoints) {
                    LOG.info(" > " + jsonEndpoint.getUrl());
                }
                for (JsonEndpoint jsonEndpoint : jsonEndpoints) {
                    LOG.info("Found this endpoint: " + jsonEndpoint.getUrl() + " doesn't match: " + sb.toString());
                }
            }

            throw new NoSupportedApiVersion("Couldn't find endpoint for swift using version: " + sb.toString());
        }

        return new SwiftApiImpl(version, openstackApi, swiftEndpoint);
    }

    /**
     * Returns the Set of available versions based on the supplied list of jsonEndpoints.
     *
     * @param jsonEndpoints the end points to parse for supported versions.
     * @return the set of versions.
     */
    public static Set<String> getAvailableVersions(final List<JsonEndpoint> jsonEndpoints) {
        Set<String> versions = new HashSet<>();
        for (JsonEndpoint jsonEndpoint : jsonEndpoints) {
            URL url;
            try {
                url = new URL(jsonEndpoint.getUrl());
                String[] elements = url.getPath().split("/");
                if (elements.length >= 2) {
                    versions.add(elements[1]);
                }
            } catch (MalformedURLException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Problem parsing url: " + jsonEndpoint.getUrl(), e);
                }
            }
        }
        return versions;
    }
}
