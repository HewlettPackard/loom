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
import com.hp.hpl.loom.exceptions.NoSuchUserException;
import com.hp.hpl.loom.exceptions.SessionAlreadyExistsException;
import com.hp.hpl.loom.exceptions.UserAlreadyConnectedException;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Status;
import com.hp.hpl.loom.tapestry.PatternDefinitionList;

/**
 * An interface for accessing services offered by Loom.
 */
public interface LoomService {

    // LOGIN/LOGOUT

    /**
     * Login/logout from a single provider and returns a list of pattern definitions.
     *
     * @param providerType - provider's type
     * @param providerId - provider ID
     * @param operation - operation of an action i.e. login or logout
     * @param creds - credentials used for logging in
     * @param sessionId - session's cookie sent by a requester
     * @param response - http response to be sent back to the requester
     * @return a list of pattern definitions offered by a given provider
     * @throws NoSuchProviderException - if the requested provider does not exist
     * @throws SessionAlreadyExistsException - if the session already exists in the sessionManager
     * @throws NoSuchSessionException - if the cookie (sessionId) sent does not exist
     * @throws UserAlreadyConnectedException - if the user is already logged in on this session
     * @throws NoSuchUserException - if the user does not exist
     */
    PatternDefinitionList logProvider(String providerType, String providerId, String operation, Credentials creds,
            String sessionId, HttpServletResponse response) throws NoSuchProviderException,
            SessionAlreadyExistsException, NoSuchSessionException, UserAlreadyConnectedException, NoSuchUserException;

    /**
     * Logs out of all providers.
     *
     * @param operation - operation of an action, in this case, only logout is valid
     * @param sessionId - session's cookie sent by a requester
     * @param response - http response to be sent back to the requester
     * @throws NoSuchSessionException - if the cookie (sessionId) sent does not exist
     * @throws NoSuchUserException - if the user does not exist
     * @throws NoSuchProviderException - if the requested provider does not exist
     *
     */
    void logoutAllProviders(String operation, String sessionId, HttpServletResponse response)
            throws NoSuchSessionException, NoSuchUserException, NoSuchProviderException;

    /**
     * Gets the status of LoomService.
     *
     * @param sessionId - session's cookie sent by a requester
     * @param response - http response to be sent back to the requester
     * @return the version as defined in the "Implementation-Build" of the loom.war
     */
    Status getStatus(String sessionId, HttpServletResponse response);
}
