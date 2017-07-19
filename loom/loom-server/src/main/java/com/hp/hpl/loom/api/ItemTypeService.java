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
import com.hp.hpl.loom.model.ItemTypeList;

/**
 * Interface for looking up {@link com.hp.hpl.loom.model.ItemType}s.
 */
public interface ItemTypeService {

    /**
     * Gets all item types.
     *
     * @param providerType - a given provider type
     * @param providerId - a given provider ID
     * @param sessionId - session's cookie sent by a requester
     * @param response - http response to be sent back to the requester
     * @return the list of ItemTypes
     * @throws NoSuchProviderException thrown if the provider isn't found
     */
    ItemTypeList getItemTypes(String providerType, String providerId, String sessionId, HttpServletResponse response)
            throws NoSuchProviderException;

    /**
     * Gets all item types for a given provider type.
     *
     * @param providerType - a given provider type
     * @param sessionId - session's cookie sent by a requester
     * @param response - http response to be sent back to the requester
     * @return a list of item types
     */
    ItemTypeList getItemTypes(String providerType, String sessionId, HttpServletResponse response);

    /**
     * Gets all item types for all provide types.
     *
     * @param sessionId - session's cookie sent by a requester
     * @param current - include only the current itemTypes
     * @param response - http response to be sent back to the requester
     * @return a list of item types
     */
    ItemTypeList getItemTypes(String sessionId, boolean current, HttpServletResponse response)
            throws NoSuchSessionException;
}
