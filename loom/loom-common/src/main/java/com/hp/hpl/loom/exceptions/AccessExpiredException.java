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
package com.hp.hpl.loom.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.loom.model.ProviderList;
import com.hp.hpl.loom.model.Session;

/**
 * Exception to handle problems with expired tokens.
 *
 */
public class AccessExpiredException extends CheckedLoomException {
    private Session session;
    private ProviderList providerList;

    /**
     * Constructs an exception based on the associated session and a list of providers.
     *
     * @param session a session associated with the exception.
     * @param providerList a list of providers with expired tokens.
     * @throws JsonProcessingException if the list of providers cannot be deserialized.
     */
    public AccessExpiredException(final Session session, final ProviderList providerList)
            throws JsonProcessingException {
        super(new ObjectMapper().writeValueAsString(providerList));
        this.session = session;
        this.providerList = providerList;
    }

    /**
     * Gets session.
     *
     * @return session associated with the exception.
     */
    public Session getSession() {
        return session;
    }

    /**
     * Gets a list of providers.
     *
     * @return a list of providers with expired tokens.
     */
    public ProviderList getProviderList() {
        return providerList;
    }
}
