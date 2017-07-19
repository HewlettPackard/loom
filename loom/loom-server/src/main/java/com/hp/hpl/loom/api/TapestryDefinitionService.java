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

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;

import com.hp.hpl.loom.api.util.SessionManager;
import com.hp.hpl.loom.exceptions.InvalidQueryInputException;
import com.hp.hpl.loom.exceptions.InvalidQueryParametersException;
import com.hp.hpl.loom.exceptions.ItemPropertyNotFound;
import com.hp.hpl.loom.exceptions.LogicalIdAlreadyExistsException;
import com.hp.hpl.loom.exceptions.NoSuchAggregationException;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchQueryDefinitionException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.NoSuchTapestryDefinitionException;
import com.hp.hpl.loom.exceptions.NoSuchThreadDefinitionException;
import com.hp.hpl.loom.exceptions.OperationException;
import com.hp.hpl.loom.exceptions.RelationPropertyNotFound;
import com.hp.hpl.loom.exceptions.ThreadDefinitionAlreadyExistsException;
import com.hp.hpl.loom.exceptions.ThreadDeletedByDynAdapterUnload;
import com.hp.hpl.loom.model.RelationshipTypeSet;
import com.hp.hpl.loom.tapestry.TapestryDefinition;
import com.hp.hpl.loom.tapestry.TapestryDefinitionList;
import com.hp.hpl.loom.tapestry.ThreadDefinition;
import com.hp.hpl.loom.tapestry.ThreadDefinitionList;

/**
 * Interface for the TapestryDefinitionService.
 */
public interface TapestryDefinitionService {

    /**
     * Creates a tapestry.
     *
     * @param tapestryDefinition - a tapestry definition to be created
     * @param sessionId - session's cookie sent by a requester
     * @param response - http response to be sent back to the requester
     * @return a tapestry definition's ID assigned by Loom
     * @throws InterruptedException thrown if interrupted
     * @throws NoSuchSessionException thrown if session isn't found
     * @throws NoSuchTapestryDefinitionException thrown if tapestry isn't found
     * @throws NoSuchAggregationException thrown if the aggregation isn't found
     * @throws NoSuchQueryDefinitionException thrown if the query isn't found
     * @throws LogicalIdAlreadyExistsException thrown if the logical id already exists
     * @throws OperationException thrown if there is an operation exception
     * @throws NoSuchThreadDefinitionException thrown if the thread isn't found
     * @throws InvalidQueryInputException thrown if the query is invalid
     * @throws InvalidQueryParametersException thrown if the query params are invalid
     * @throws NoSuchItemTypeException thrown if the itemtype isn't found
     * @throws ItemPropertyNotFound thrown if the item property isn't found
     * @throws RelationPropertyNotFound thrown if the relation property isn't found
     * @throws ThreadDeletedByDynAdapterUnload thrown if the thread was deleted by the dynamic
     *         adapter unload
     */
    TapestryDefinition createTapestryDefinition(TapestryDefinition tapestryDefinition, String sessionId,
            HttpServletResponse response) throws InterruptedException, NoSuchSessionException,
            NoSuchTapestryDefinitionException, NoSuchAggregationException, NoSuchQueryDefinitionException,
            LogicalIdAlreadyExistsException, OperationException, NoSuchThreadDefinitionException,
            InvalidQueryInputException, InvalidQueryParametersException, NoSuchItemTypeException, ItemPropertyNotFound,
            RelationPropertyNotFound, ThreadDeletedByDynAdapterUnload;

    // Missing "throws"

    /**
     * Updates a tapestry for a given tapestryId.
     *
     * @param tapestryId - a tapestry id to be update
     * @param tapestryDefinition - a tapestry definition to be update
     * @param sessionId - session's cookie sent by a requester
     * @param response - http response to be sent back to the requester
     * @throws InterruptedException thrown if interrupted
     * @throws NoSuchSessionException thrown if session isn't found
     * @throws NoSuchTapestryDefinitionException thrown if tapestry isn't found
     * @throws NoSuchAggregationException thrown if the aggregation isn't found
     * @throws NoSuchQueryDefinitionException thrown if the query isn't found
     * @throws LogicalIdAlreadyExistsException thrown if the logical id already exists
     * @throws OperationException thrown if there is an operation exception
     * @throws NoSuchThreadDefinitionException thrown if the thread isn't found
     * @throws InvalidQueryInputException thrown if the query is invalid
     * @throws InvalidQueryParametersException thrown if the query params are invalid
     * @throws NoSuchItemTypeException thrown if the itemtype isn't found
     * @throws ItemPropertyNotFound thrown if the item property isn't found
     * @throws RelationPropertyNotFound thrown if the relation property isn't found
     * @throws ThreadDeletedByDynAdapterUnload thrown if the thread was deleted by the dynamic
     *         adapter unload
     */
    void updateTapestryDefinition(String tapestryId, TapestryDefinition tapestryDefinition, String sessionId,
            HttpServletResponse response) throws InterruptedException, NoSuchSessionException,
            NoSuchTapestryDefinitionException, NoSuchAggregationException, NoSuchQueryDefinitionException,
            LogicalIdAlreadyExistsException, OperationException, NoSuchThreadDefinitionException,
            InvalidQueryInputException, InvalidQueryParametersException, NoSuchItemTypeException, ItemPropertyNotFound,
            RelationPropertyNotFound, ThreadDeletedByDynAdapterUnload;

    /**
     * Gets a tapestry.
     *
     * @param tapestryId - an ID of a tapestry definition being requested
     * @param sessionId - session's cookie sent by a requester
     * @param response - http response to be sent back to the requester
     * @return a tapestry definition requested
     * @throws NoSuchSessionException - if the cookie (sessionId) sent does not exist
     * @throws NoSuchTapestryDefinitionException - if no tapestry definition matches the ID provided
     */
    TapestryDefinition getTapestryDefinition(String tapestryId, String sessionId, HttpServletResponse response)
            throws NoSuchSessionException, NoSuchTapestryDefinitionException;

    /**
     * Gets all tapestries.
     *
     * @param sessionId - session's cookie sent by a requester
     * @param response - http response to be sent back to the requester
     * @return a list of tapestries for the current user
     * @throws NoSuchSessionException - if the cookie (sessionId) sent does not exist
     */
    TapestryDefinitionList getTapestryDefinitions(String sessionId, HttpServletResponse response)
            throws NoSuchSessionException;

    /**
     * Deletes a tapestry.
     *
     * @param tapestryId - an ID of a tapestry definition being requested for deletion
     * @param sessionId - session's cookie sent by a requester
     * @param response - http response to be sent back to the requester
     * @throws NoSuchSessionException - if the cookie (sessionId) sent does not exist
     * @throws NoSuchTapestryDefinitionException - if no tapestry definition matches the ID provided
     */
    void deleteTapestryDefinition(String tapestryId, String sessionId, HttpServletResponse response)
            throws NoSuchSessionException, NoSuchTapestryDefinitionException;

    /**
     * Deletes all tapestries associated with the current user.
     *
     * @param sessionId - session's cookie sent by a requester
     * @param response - http response to be sent back to the requester
     * @throws NoSuchSessionException - if the cookie (sessionId) sent does not exist
     */
    void deleteTapestryDefinitions(String sessionId, HttpServletResponse response) throws NoSuchSessionException;

    /**
     * Updates a thread.
     *
     * @param tapestryId - an ID of a tapestry definition
     * @param threadId - an ID of a thread definition being updated
     * @param threadDefinition - new thread definition
     * @param sessionId - session's cookie sent by a requester
     * @param response - http response to be sent back to the requester
     * @throws NoSuchSessionException - if the cookie (sessionId) sent does not exist
     * @throws NoSuchTapestryDefinitionException - if no tapestry definition matches the ID provided
     * @throws NoSuchAggregationException thrown if the aggregation isn't found
     * @throws NoSuchQueryDefinitionException thrown if the query isn't found
     * @throws LogicalIdAlreadyExistsException thrown if the logical id already exists
     * @throws OperationException thrown if there is an operation exception
     * @throws NoSuchThreadDefinitionException thrown if the thread isn't found
     * @throws InvalidQueryInputException thrown if the query is invalid
     * @throws InvalidQueryParametersException thrown if the query params are invalid
     * @throws NoSuchItemTypeException thrown if the itemtype isn't found
     * @throws ItemPropertyNotFound thrown if the item property isn't found
     * @throws RelationPropertyNotFound thrown if the relation property isn't found
     * @throws ThreadDeletedByDynAdapterUnload thrown if the thread was deleted by the dynamic
     *         adapter unload
     */
    void updateThreadDefinition(String tapestryId, String threadId, ThreadDefinition threadDefinition, String sessionId,
            HttpServletResponse response) throws NoSuchSessionException, NoSuchTapestryDefinitionException,
            NoSuchThreadDefinitionException, NoSuchItemTypeException, InvalidQueryInputException,
            InvalidQueryParametersException, NoSuchAggregationException, NoSuchQueryDefinitionException,
            OperationException, LogicalIdAlreadyExistsException, ItemPropertyNotFound, RelationPropertyNotFound,
            ThreadDeletedByDynAdapterUnload;

    /**
     * Creates a thread.
     *
     * @param tapestryId - an ID of a tapestry definition
     * @param threadDefinition - new thread definition
     * @param sessionId - session's cookie sent by a requester
     * @param response - http response to be sent back to the requester
     * @throws NoSuchSessionException - if the cookie (sessionId) sent does not exist
     * @throws NoSuchTapestryDefinitionException - if no tapestry definition matches the ID provided
     * @throws NoSuchAggregationException thrown if the aggregation isn't found
     * @throws NoSuchQueryDefinitionException thrown if the query isn't found
     * @throws LogicalIdAlreadyExistsException thrown if the logical id already exists
     * @throws OperationException thrown if there is an operation exception
     * @throws NoSuchThreadDefinitionException thrown if the thread isn't found
     * @throws InvalidQueryInputException thrown if the query is invalid
     * @throws InvalidQueryParametersException thrown if the query params are invalid
     * @throws NoSuchItemTypeException thrown if the itemtype isn't found
     * @throws ItemPropertyNotFound thrown if the item property isn't found
     * @throws RelationPropertyNotFound thrown if the relation property isn't found
     * @throws ThreadDefinitionAlreadyExistsException - if the thread definition already exists
     * @throws ThreadDeletedByDynAdapterUnload - thrown if the thread was deleted by the dynamic
     *         adapter unload
     */
    void createThreadDefinition(String tapestryId, ThreadDefinition threadDefinition, String sessionId,
            HttpServletResponse response) throws NoSuchSessionException, NoSuchTapestryDefinitionException,
            NoSuchItemTypeException, InvalidQueryInputException, InvalidQueryParametersException,
            NoSuchAggregationException, NoSuchQueryDefinitionException, OperationException,
            LogicalIdAlreadyExistsException, ItemPropertyNotFound, RelationPropertyNotFound,
            ThreadDefinitionAlreadyExistsException, NoSuchThreadDefinitionException, ThreadDeletedByDynAdapterUnload;

    /**
     * Gets a thread.
     *
     * @param tapestryId - an ID of a tapestry definition
     * @param threadId - an ID of a thread definition being updated
     * @param sessionId - session's cookie sent by a requester
     * @param response - http response to be sent back to the requester
     * @return the thread definition
     * @throws NoSuchSessionException - if the cookie (sessionId) sent does not exist
     * @throws NoSuchTapestryDefinitionException - if no tapestry definition matches the ID provided
     * @throws NoSuchThreadDefinitionException thrown if the thread isn't found
     * @throws InvalidQueryInputException thrown if the query is invalid
     * @throws InvalidQueryParametersException thrown if the query params are invalid
     * @throws NoSuchItemTypeException thrown if the itemtype isn't found
     */
    ThreadDefinition getThreadDefinition(String tapestryId, String threadId, String sessionId,
            HttpServletResponse response)
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, NoSuchThreadDefinitionException,
            NoSuchItemTypeException, InvalidQueryInputException, InvalidQueryParametersException;

    /**
     * Deletes a thread.
     *
     * @param tapestryId - an ID of a tapestry definition
     * @param threadId - an ID of a thread definition being updated
     * @param sessionId - session's cookie sent by a requester
     * @param response - http response to be sent back to the requester
     * @throws NoSuchSessionException - if the cookie (sessionId) sent does not exist
     * @throws NoSuchTapestryDefinitionException - if no tapestry definition matches the ID provided
     * @throws NoSuchAggregationException thrown if the aggregation isn't found
     * @throws NoSuchQueryDefinitionException thrown if the query isn't found
     * @throws LogicalIdAlreadyExistsException thrown if the logical id already exists
     * @throws OperationException thrown if there is an operation exception
     * @throws NoSuchThreadDefinitionException thrown if the thread isn't found
     * @throws InvalidQueryInputException thrown if the query is invalid
     * @throws InvalidQueryParametersException thrown if the query params are invalid
     * @throws NoSuchItemTypeException thrown if the itemtype isn't found
     * @throws ItemPropertyNotFound thrown if the item property isn't found
     * @throws RelationPropertyNotFound thrown if the relation property isn't found
     * @throws ThreadDeletedByDynAdapterUnload - thrown if the thread was deleted by the dynamic
     *         adapter unload
     */
    void deleteThreadDefinition(String tapestryId, String threadId, String sessionId, HttpServletResponse response)
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, NoSuchThreadDefinitionException,
            NoSuchAggregationException, NoSuchQueryDefinitionException, OperationException, InvalidQueryInputException,
            LogicalIdAlreadyExistsException, InvalidQueryParametersException, ItemPropertyNotFound,
            RelationPropertyNotFound, ThreadDeletedByDynAdapterUnload, NoSuchItemTypeException;

    /**
     * Gets all threads.
     *
     * @param tapestryId - an ID of a tapestry definition
     * @param sessionId - session's cookie sent by a requester
     * @param response - http response to be sent back to the requester
     * @return the thread definition list
     * @throws NoSuchSessionException - if the cookie (sessionId) sent does not exist
     * @throws NoSuchTapestryDefinitionException - if no tapestry definition matches the ID provided
     * @throws InvalidQueryInputException thrown if the query is invalid
     * @throws InvalidQueryParametersException thrown if the query params are invalid
     * @throws NoSuchItemTypeException thrown if the itemtype isn't found
     */
    ThreadDefinitionList getThreadDefinitions(String tapestryId, String sessionId, HttpServletResponse response)
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, NoSuchItemTypeException,
            InvalidQueryInputException, InvalidQueryParametersException;

    /**
     * Deletes all threads for a given tapestry.
     *
     * @param tapestryId - an ID of a tapestry definition
     * @param sessionId - session's cookie sent by a requester
     * @param response - http response to be sent back to the requester
     * @throws NoSuchSessionException - if the cookie (sessionId) sent does not exist
     * @throws NoSuchTapestryDefinitionException - if no tapestry definition matches the ID provided
     * @throws NoSuchAggregationException thrown if the aggregation isn't found
     * @throws NoSuchQueryDefinitionException thrown if the query isn't found
     * @throws LogicalIdAlreadyExistsException thrown if the logical id already exists
     * @throws OperationException thrown if there is an operation exception
     * @throws NoSuchThreadDefinitionException thrown if the thread isn't found
     * @throws InvalidQueryInputException thrown if the query is invalid
     * @throws InvalidQueryParametersException thrown if the query params are invalid
     * @throws NoSuchItemTypeException thrown if the itemtype isn't found
     * @throws ItemPropertyNotFound thrown if the item property isn't found
     * @throws RelationPropertyNotFound thrown if the relation property isn't found
     * @throws ThreadDeletedByDynAdapterUnload - thrown if the thread was deleted by the dynamic
     *         adapter unload
     */
    void deleteThreadDefinitions(String tapestryId, String sessionId, HttpServletResponse response)
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, NoSuchAggregationException,
            NoSuchQueryDefinitionException, OperationException, NoSuchThreadDefinitionException,
            InvalidQueryInputException, LogicalIdAlreadyExistsException, InvalidQueryParametersException,
            ItemPropertyNotFound, RelationPropertyNotFound, ThreadDeletedByDynAdapterUnload, NoSuchItemTypeException;


    RelationshipTypeSet getThreadRelations(@PathVariable final String tapestryId,
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response)
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, NoSuchThreadDefinitionException,
            NoSuchItemTypeException, InvalidQueryInputException, InvalidQueryParametersException;

}
