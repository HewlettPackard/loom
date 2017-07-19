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
package com.hp.hpl.loom.api;

import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.ProviderList;

/**
 * Interface for looking up providers.
 */
public interface ProviderService {

    /**
     * Gets a list of all providers.
     *
     * @param sessionId - session's cookie sent by a requester
     * @param response - http response to be sent back to the requester
     * @return a list of providers connected to Loom
     */
    ProviderList getProviders(String sessionId, HttpServletResponse response);

    /**
     * Gets a list of all providers of a specific type.
     *
     * @param providerType - the provider type requested for a list of providers
     * @param sessionId - session's cookie sent by a requester
     * @param response - http response to be sent back to the requester
     * @return a list of providers of a specific type connected to Loom
     */
    ProviderList getProviders(String providerType, String sessionId, HttpServletResponse response);

    /**
     * Gets a provider.
     *
     * @param providerType - the provider type requested of a requested provider
     * @param providerId - the id of a requested provider
     * @param sessionId - session's cookie sent by a requester
     * @param response - http response to be sent back to the requester
     * @return a list of providers of a specific type connected to Loom
     * @throws NoSuchProviderException - thrown if the provider isn't found
     */
    Provider getProvider(String providerType, String providerId, String sessionId, HttpServletResponse response)
            throws NoSuchProviderException;

    /**
     * Returns the graph relationships for this provider.
     *
     * @param sessionId - session's cookie sent by a requester
     * @param response - http response to be sent back to the requester
     * @throws NoSuchProviderException, NoSuchSessionException
     * @throws NoSuchSessionException
     */
    String getRelationships(String sessionId, HttpServletResponse response)
            throws NoSuchProviderException, NoSuchSessionException;

}
