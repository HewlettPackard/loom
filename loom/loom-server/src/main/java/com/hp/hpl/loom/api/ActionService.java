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

import com.hp.hpl.loom.exceptions.InvalidActionSpecificationException;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionResult;

/**
 * Interface for the actions, it only have the doAction method.
 */
public interface ActionService {

    /**
     * Performs an action.
     *
     * @param action - an action to be performed
     * @param sessionId - session's cookie sent by a requester
     * @param response - http response to be sent back to the requester
     * @return returns the result of the action
     * @throws InvalidActionSpecificationException - if the action isn't valid
     * @throws NoSuchProviderException - if the requested provider does not exist
     * @throws NoSuchSessionException - if the cookie (sessionId) sent does not exist
     * @throws NoSuchItemTypeException - the itemType does not exist
     */
    ActionResult doAction(Action action, String sessionId, HttpServletResponse response)
            throws InvalidActionSpecificationException, NoSuchProviderException, NoSuchSessionException,
            NoSuchItemTypeException;

    ActionResult getActionResult(String actionResultId, String sessionId, HttpServletResponse response)
            throws InvalidActionSpecificationException, NoSuchProviderException, NoSuchSessionException,
            NoSuchItemTypeException;

}
