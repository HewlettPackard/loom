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
package com.hp.hpl.loom.openstack.common;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Helper class to create the keystone token uri's.
 */
public final class EndpointUtils {

    private EndpointUtils() {}

    /**
     * Create a keystone URI with a ?nocatalog on the end to prevent the too long tokens.
     *
     * @param authEndpoint the endpoint for keystone
     * @return the URI
     */
    public static URI keystoneTokenURINoCatalog(final String authEndpoint) {
        URI keystoneTokenURI = null;
        try {
            keystoneTokenURI = new URI(authEndpoint + "/auth/tokens?nocatalog");
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return keystoneTokenURI;
    }

    /**
     * Create a keystone URI.
     *
     * @param authEndpoint the endpoint for keystone
     * @return the URI
     */
    public static URI keystoneTokenURIWithCatalog(final String authEndpoint) {
        URI keystoneTokenURI = null;
        try {
            keystoneTokenURI = new URI(authEndpoint + "/auth/tokens");
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return keystoneTokenURI;
    }
}
