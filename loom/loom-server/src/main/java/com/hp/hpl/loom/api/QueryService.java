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

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hp.hpl.loom.exceptions.AccessExpiredException;
import com.hp.hpl.loom.exceptions.InvalidQueryInputException;
import com.hp.hpl.loom.exceptions.InvalidQueryParametersException;
import com.hp.hpl.loom.exceptions.ItemPropertyNotFound;
import com.hp.hpl.loom.exceptions.LogicalIdAlreadyExistsException;
import com.hp.hpl.loom.exceptions.NoSuchAggregationException;
import com.hp.hpl.loom.exceptions.NoSuchItemException;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NoSuchQueryDefinitionException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.NoSuchTapestryDefinitionException;
import com.hp.hpl.loom.exceptions.NoSuchThreadDefinitionException;
import com.hp.hpl.loom.exceptions.OperationException;
import com.hp.hpl.loom.exceptions.RelationPropertyNotFound;
import com.hp.hpl.loom.exceptions.ThreadDeletedByDynAdapterUnload;
import com.hp.hpl.loom.model.QueryResult;
import com.hp.hpl.loom.model.QueryResultElement;
import com.hp.hpl.loom.model.QueryResultList;

public interface QueryService {
    // Missing "throws"


    /**
     * Queries a thread for a given threadId and a given tapestryId.
     *
     * @param tapestryId - an ID of the tapestry definition of interest
     * @param threadId - an ID of a thread definition in a given tapestry definition
     * @param sessionId - session's cookie sent by a requester
     * @param response - http response to be sent back to the requester
     * @return a QueryResult object containing elements of the request and relevant information
     * @return
     * @throws NoSuchSessionException
     * @throws NoSuchThreadDefinitionException
     * @throws NoSuchTapestryDefinitionException
     * @throws NoSuchQueryDefinitionException
     * @throws NoSuchAggregationException
     * @throws LogicalIdAlreadyExistsException
     * @throws InvalidQueryInputException
     * @throws UnsupportedOperationException
     * @throws OperationException
     * @throws ItemPropertyNotFound
     * @throws RelationPropertyNotFound
     * @throws IllegalArgumentException
     * @throws ThreadDeletedByDynAdapterUnload
     * @throws InvalidQueryParametersException
     * @throws NoSuchItemTypeException
     * @throws JsonProcessingException
     * @throws AccessExpiredException
     * @throws NoSuchProviderException
     */
    QueryResult getQueryResult(String tapestryId, String threadId, String sessionId, HttpServletResponse response)
            throws NoSuchSessionException, NoSuchThreadDefinitionException, NoSuchTapestryDefinitionException,
            NoSuchQueryDefinitionException, NoSuchAggregationException, LogicalIdAlreadyExistsException,
            InvalidQueryInputException, OperationException, ItemPropertyNotFound, RelationPropertyNotFound,
            ThreadDeletedByDynAdapterUnload, NoSuchItemTypeException, InvalidQueryParametersException,
            AccessExpiredException, JsonProcessingException, NoSuchProviderException;


    /**
     * Gets a specific item.
     *
     * @param logicalId - a logical ID of an item
     * @param sessionId - session's cookie sent by a requester
     * @param response - http response to be sent back to the requester
     * @return
     * @throws NoSuchProviderException - if the requested provider does not exist
     * @throws NoSuchSessionException - if the cookie (sessionId) sent does not exist
     * @throws NoSuchItemTypeException - the itemType does not exist
     * @throws NoSuchItemException - the item requested does not exist
     */
    QueryResultElement getItem(String logicalId, String sessionId, HttpServletResponse response)
            throws NoSuchProviderException, NoSuchSessionException, NoSuchItemTypeException, NoSuchItemException;


    /**
     * Queries a set of thread for given threadIds and a given tapestryId.
     *
     * @param tapestryId - an ID of the tapestry definition of interest
     * @param threadIds - a list of thread IDs in a given tapestry definition
     * @param sessionId - session's cookie sent by a requester
     * @param response - http response to be sent back to the requester
     * @return a QueryResultList object containing a list of QueryResult objects
     * @throws NoSuchSessionException
     * @throws NoSuchThreadDefinitionException
     * @throws NoSuchTapestryDefinitionException
     * @throws NoSuchQueryDefinitionException
     * @throws NoSuchAggregationException
     * @throws LogicalIdAlreadyExistsException
     * @throws InvalidQueryInputException
     * @throws OperationException
     * @throws ItemPropertyNotFound
     * @throws RelationPropertyNotFound
     * @throws ThreadDeletedByDynAdapterUnload
     * @throws JsonProcessingException
     * @throws AccessExpiredException
     * @throws NoSuchProviderException
     */
    QueryResultList getQueryResults(String tapestryId, List<String> threadIds, String sessionId,
            HttpServletResponse response)
            throws NoSuchSessionException, NoSuchThreadDefinitionException, NoSuchTapestryDefinitionException,
            NoSuchQueryDefinitionException, NoSuchAggregationException, LogicalIdAlreadyExistsException,
            InvalidQueryInputException, OperationException, ItemPropertyNotFound, RelationPropertyNotFound,
            ThreadDeletedByDynAdapterUnload, AccessExpiredException, JsonProcessingException, NoSuchProviderException;

}
