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
package com.hp.hpl.loom.openstack.cinder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.openstack.NoSupportedApiVersion;
import com.hp.hpl.loom.openstack.OpenstackApi;
import com.hp.hpl.loom.openstack.cinder.impl.CinderApiImpl;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonEndpoint;

/**
 * A factory to construct CinderApi's. The intention is that this can be extended to return
 * different implementation of the API based on the supplied end points and the required versions.
 */
public final class CinderApiFactory {
    private static final Log LOG = LogFactory.getLog(CinderApiFactory.class);

    private CinderApiFactory() {}

    /**
     * Get the CinderApi based on the versions required and supplied end points.
     *
     * @param openstackApi openstackApi to lookup the tokens
     * @param jsonEndpoints End points to try and connect to
     * @param versions required versions (any will do)
     * @return the CinderApi
     * @throws NoSupportedApiVersion thrown if no version could be found
     */
    public static CinderApi getApi(final OpenstackApi openstackApi, final List<JsonEndpoint> jsonEndpoints,
            final String[] versions) throws NoSupportedApiVersion {
        JsonEndpoint cinderEndpoint = null;
        String version = null;
        for (JsonEndpoint jsonEndpoint : jsonEndpoints) {
            URL url;
            try {
                url = new URL(jsonEndpoint.getUrl());
                for (String v : versions) {
                    if (url.getPath().toLowerCase().startsWith("/" + v.trim())) {
                        cinderEndpoint = jsonEndpoint;
                        version = v;
                        break;
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        if (cinderEndpoint == null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Couldn't find a suitable endpoint for cinder");
                for (JsonEndpoint jsonEndpoint : jsonEndpoints) {
                    LOG.info(" > " + jsonEndpoint.getUrl());
                }
            }
            StringBuffer sb = new StringBuffer();
            for (String v : versions) {
                sb.append(" " + v);
            }
            for (JsonEndpoint jsonEndpoint : jsonEndpoints) {
                LOG.info("Found this endpoint: " + jsonEndpoint.getUrl() + " doesn't match: " + sb.toString());
            }
            throw new NoSupportedApiVersion("Couldn't find endpoint for cinder using version: " + sb.toString());
        }

        return new CinderApiImpl(version, openstackApi, cinderEndpoint);
    }

    /**
     * Get the set of available versions based on the end points supplied.
     *
     * @param jsonEndpoints end points to get the version on
     * @return the set of versions available.
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return versions;
    }
}
