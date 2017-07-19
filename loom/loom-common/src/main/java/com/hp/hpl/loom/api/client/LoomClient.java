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
package com.hp.hpl.loom.api.client;

import java.util.List;

import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionResult;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.ItemTypeList;
import com.hp.hpl.loom.model.OperationList;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.ProviderList;
import com.hp.hpl.loom.model.QueryResult;
import com.hp.hpl.loom.model.QueryResultElement;
import com.hp.hpl.loom.model.QueryResultList;
import com.hp.hpl.loom.model.Status;
import com.hp.hpl.loom.tapestry.PatternDefinition;
import com.hp.hpl.loom.tapestry.PatternDefinitionList;
import com.hp.hpl.loom.tapestry.TapestryDefinition;
import com.hp.hpl.loom.tapestry.TapestryDefinitionList;
import com.hp.hpl.loom.tapestry.ThreadDefinition;
import com.hp.hpl.loom.tapestry.ThreadDefinitionList;

/**
 * Java client interface to Loom.
 */
public interface LoomClient {

    /**
     * The loom session cookie.
     */
    String SESSION_COOKIE = "loom";

    // patterns

    /**
     * Get the {@link PatternDefinitionList} for this session.
     *
     * @return the pattern definition list
     */
    PatternDefinitionList getPatterns();

    /**
     * Get the {@link PatternDefinition} for the given pattern id.
     *
     * @param patternId patternId to lookup
     * @return PatternDefinition for a given id
     */
    PatternDefinition getPattern(String patternId);

    // tapestry

    /**
     * Create a tapestry definition from the provided tapestry definition.
     *
     * @param tapestryDefinition the tapestry definition to create
     * @return the tapestry definition created
     */
    TapestryDefinition createTapestryDefinition(TapestryDefinition tapestryDefinition);

    /**
     * Get the tapestry definition from a given tapestry id.
     *
     * @param tapId the tapestry definition
     * @return the tapestry definition
     */
    TapestryDefinition getTapestry(String tapId);

    /**
     * Update the tapestry definition based on the tapestryDefinition and tapestryId.
     *
     * @param tapestryId the tapestryId the tapestry definition id
     * @param tapestryDefinition the tapestry definition
     */
    void updateTapestryDefinition(String tapestryId, TapestryDefinition tapestryDefinition);

    /**
     * Get the tapestry definition list.
     *
     * @return the tapestry definition list
     */
    TapestryDefinitionList getTapestryDefinitions();

    /**
     * Delete the tapestry definition corresponding to a tapestryId.
     *
     * @param tapestryId the tapestryId the tapestry id to delete
     */
    void deleteTapestryDefinition(String tapestryId);

    /**
     * Delete the tapestry definitions.
     */
    void deleteTapestryDefinitions();

    // threads

    /**
     * Update the thread definition using the id's and definition provided.
     *
     * @param tapestryId the tapestryId tapestry id to update
     * @param threadId the threadId thread id to update
     * @param threadDefinition threadDefinition to update to
     */
    void updateThreadDefinition(String tapestryId, String threadId, ThreadDefinition threadDefinition);

    /**
     * Get a thread definition for a given tapestryId and threadId.
     *
     * @param tapestryId the tapestryId tapestry id to lookup
     * @param threadId the threadId thread id to lookup
     * @return thread definition corresponding to the tapestry and thread id
     */
    ThreadDefinition getThreadDefinition(String tapestryId, String threadId);

    /**
     * Get a the {@link ThreadDefinitionList} for a given tapestryId.
     *
     * @param tapestryId the tapestryId tapestry id to lookup
     * @return ThreadDefinitionList corresponding to the tapestryId
     */
    ThreadDefinitionList getThreadDefinitions(String tapestryId);

    /**
     * Delete a given thread definition based on the tapestryId and threadId.
     *
     * @param tapestryId the tapestryId tapestry id to delete
     * @param threadId the threadId thread id to delete
     */
    void deleteThreadDefinition(String tapestryId, String threadId);

    /**
     * Delete a given thread definition based on the tapestryId.
     *
     * @param tapestryId the tapestryId tapestry id to delete
     */
    void deleteThreadDefinitions(String tapestryId);

    /**
     * Create a threadDefinition using the tapestryId and {@link ThreadDefinition}.
     *
     * @param tapestryId the tapestryId tapestryId to link the threadDefinition to
     * @param threadDefinition the threadDefinition to create
     */
    void createThreadDefinition(String tapestryId, ThreadDefinition threadDefinition);

    /**
     * Get the Aggregations.
     *
     * @param tapestryId the tapestryId
     * @param threadIds the threadIds
     * @return the query results list
     */
    QueryResultList getAggregations(String tapestryId, List<String> threadIds);

    /**
     * Login.
     *
     * @param providerType providerType to use
     * @param providerId the providerId
     * @param credentials the credentials
     * @return the patternDefinitionList
     */
    PatternDefinitionList loginProvider(String providerType, String providerId, Credentials credentials);

    /**
     * Logout of the provider.
     *
     * @param providerType providerType to use
     * @param providerId the providerId
     */
    void logoutProvider(String providerType, String providerId);

    /**
     * Logout of all providers.
     */
    void logoutAllProviders();

    /**
     * Get the providerList for all providers.
     *
     * @return the list of providers.
     */
    ProviderList getProviders();

    /**
     * Get the providers for a given providerType.
     *
     * @param providerType providerType to use
     * @return the provider list
     */
    ProviderList getProviders(String providerType);

    /**
     * Get the provider for a given type and id.
     *
     * @param providerType providerType to use
     * @param providerId the providerId
     * @return the provider
     */
    Provider getProvider(String providerType, String providerId);

    /**
     * Get the aggregation for a tapestryId and threadId.
     *
     * @param tapestryId the tapestryId
     * @param threadId the threadId
     * @return the query results
     */
    QueryResult getAggregation(String tapestryId, String threadId);

    /**
     * Get the item for a given logicalId.
     *
     * @param logicalId the logicalId
     * @return the QueryResultElement
     */
    QueryResultElement getItem(String logicalId);

    /**
     * Submit an action.
     *
     * @param action the action to execute
     * @return the action result
     */
    ActionResult executeAction(Action action);

    /**
     * Check the action results status.
     *
     * @param actionResultId the id of the action result to check
     * @return the action result
     */
    ActionResult actionResultStatus(String actionResultId);

    /**
     * Get the item types for a given providerType and id.
     *
     * @param providerType providerType to use
     * @param providerId the providerId
     * @return item types list
     */
    ItemTypeList getItemTypes(String providerType, String providerId);

    /**
     * Get the item types for a given providerType.
     *
     * @param providerType providerType to use
     * @return item types list
     */
    ItemTypeList getItemTypes(String providerType);

    /**
     * Get the item types.
     *
     * @return item types list
     */
    ItemTypeList getItemTypes();

    /**
     * Get the sessionId.
     *
     * @return sessionId
     */
    String getSessionId();

    /**
     * Gets the status information.
     *
     * @return the status info
     */
    Status getStatus();

    /**
     * Gets the operations list.
     *
     * @param providerType providerType to use
     * @param declaredBy who declared the operation
     * @return the operation list
     */
    OperationList getOperations(String providerType, String declaredBy);

    /**
     * Login to a provider with a bad operation.
     *
     * @param providerType providerType to use
     * @param providerId the providerId
     * @param credentials the credentials
     * @param op operation to try
     * @return the patternDefinitionList
     */
    PatternDefinitionList loginProviderBadOp(String providerType, String providerId, Credentials credentials,
            String op);

    /**
     * Get the list of providers.
     *
     * @param noSession true if noSession exception is required
     * @return the provider list
     */
    ProviderList getProviders(boolean noSession);

    /**
     * Get the patterns list.
     *
     * @param noSession true if noSession exception is required
     * @return the pattern defintion list
     */
    PatternDefinitionList getPatterns(boolean noSession);

    /**
     * Get the patterns.
     *
     * @param patternId the patternId
     * @param noSession true if noSession exception is required
     * @return the patternDefinition
     */
    PatternDefinition getPattern(String patternId, boolean noSession);

    /**
     * Get the tapestryDefinition.
     *
     * @param tapestryDefinition the tapestryDefinition
     * @param noSession true if noSession exception is required
     * @return the tapestryDefinition
     */
    TapestryDefinition createTapestryDefinition(TapestryDefinition tapestryDefinition, boolean noSession);

    /**
     * Submit the action.
     *
     * @param action the action
     * @param noSession true if noSession exception is required
     * @return the actionResult
     */
    ActionResult executeAction(Action action, boolean noSession);

    /**
     * Check the action results status.
     *
     * @param actionResultId the id of the action result to check
     * @param noSession true if noSession exception is required
     * @return the action result
     */
    ActionResult actionResultStatus(String actionResultId, boolean noSession);

    /**
     * Get the tapestry.
     *
     * @param tapestryId the tapestry id
     * @param noSession true if noSession exception is required
     * @return the tapestryDefinition
     */
    TapestryDefinition getTapestry(String tapestryId, boolean noSession);

    /**
     * Update the tapestry definition.
     *
     * @param tapestryId the tapestryId
     * @param tapestryDefinition the tapestryDefinition
     * @param noSession true if noSession exception is required
     */
    void updateTapestryDefinition(String tapestryId, TapestryDefinition tapestryDefinition, boolean noSession);

    /**
     * Get the aggregation.
     *
     * @param tapestryId the tapestryId
     * @param threadId the threadId
     * @param noSession true if noSession exception is required
     * @return the queryResult
     */
    QueryResult getAggregation(String tapestryId, String threadId, boolean noSession);

    /**
     * Get the aggregations.
     *
     * @param tapestryId the tapestryId
     * @param threadIds the threadIds
     * @param noSession true if noSession exception is required
     * @return the queryResultList
     */
    QueryResultList getAggregations(String tapestryId, List<String> threadIds, boolean noSession);

    /**
     * Get the tapestry definitions list.
     *
     * @param noSession true if noSession exception is required
     * @return the tapestry definition list
     */
    TapestryDefinitionList getTapestryDefinitions(boolean noSession);

    /**
     * Delete the tapestry definition.
     *
     * @param tapestryId the tapestryId
     * @param noSession true if noSession exception is required
     */
    void deleteTapestryDefinition(String tapestryId, boolean noSession);

    /**
     * Delete the tapestry definitions.
     *
     * @param noSession true if noSession exception is required
     */
    void deleteTapestryDefinitions(boolean noSession);

    /**
     * Create the threadDefintion.
     *
     * @param tapestryId the tapestryId
     * @param threadDefinition the threadDefinition
     * @param noSession true if noSession exception is required
     */
    void createThreadDefinition(String tapestryId, ThreadDefinition threadDefinition, boolean noSession);

    /**
     * Update the threadDefinition.
     *
     * @param tapestryId the tapestryId
     * @param threadId the threadId
     * @param threadDefinition the threadDefinition
     * @param noSession true if noSession exception is required
     */
    void updateThreadDefinition(String tapestryId, String threadId, ThreadDefinition threadDefinition,
            boolean noSession);

    /**
     * Get the threadDefinition.
     *
     * @param tapestryId the tapestryId
     * @param threadId the threadId
     * @param noSession true if noSession exception is required
     * @return the threadDefinition
     */
    ThreadDefinition getThreadDefinition(String tapestryId, String threadId, boolean noSession);

    /**
     * Delete the threadDefinition.
     *
     * @param tapestryId the tapestryId
     * @param threadId the threadId
     * @param noSession true if noSession exception is required
     */
    void deleteThreadDefinition(String tapestryId, String threadId, boolean noSession);

    /**
     * Get the thread definitions.
     *
     * @param tapestryId the tapestryId
     * @param noSession true if noSession exception is required
     * @return the threadDefinitions list
     */
    ThreadDefinitionList getThreadDefinitions(String tapestryId, boolean noSession);

    /**
     * Delete the threadDefinitions.
     *
     * @param tapestryId the tapestryId
     * @param noSession true if noSession exception is required
     */
    void deleteThreadDefinitions(String tapestryId, boolean noSession);

    /**
     * Login to a provider.
     *
     * @param providerType providerType to use
     * @param providerId the providerId
     * @param credentials the credentials
     * @param noSession true if noSession exception is required
     * @return the pattern definition list
     */
    PatternDefinitionList loginProvider(String providerType, String providerId, Credentials credentials,
            boolean noSession);

    /**
     * Logout of a provider.
     *
     * @param providerType providerType to use
     * @param providerId the providerId
     * @param noSession true if noSession exception is required
     */
    void logoutProvider(String providerType, String providerId, boolean noSession);

    /**
     * Logout of all providers.
     *
     * @param noSession true if noSession exception is required
     */
    void logoutAllProviders(boolean noSession);

    /**
     * Get the items for a given logical id.
     *
     * @param logicalId the logicalId
     * @param noSession true if noSession exception is required
     * @return the query result element
     */
    QueryResultElement getItem(String logicalId, boolean noSession);

    /**
     * Get the providers for a given provider type.
     *
     * @param providerType providerType to use
     * @param noSession true if noSession exception is required
     * @return the provider list
     */
    ProviderList getProviders(String providerType, boolean noSession);

    /**
     * Get the provider.
     *
     * @param providerType providerType to use
     * @param providerId the providerId
     * @param noSession true if noSession exception is required
     * @return the provider
     */
    Provider getProvider(String providerType, String providerId, boolean noSession);

    /**
     * Get the itemTypes.
     *
     * @param providerType providerType to use
     * @param providerId the providerId
     * @param noSession true if noSession exception is required
     * @return the itemTypeList
     */
    ItemTypeList getItemTypes(String providerType, String providerId, boolean noSession);

    /**
     * Get the item types for a given providerType with or without a session.
     *
     * @param providerType providerType to use provider type to get item types for
     * @param noSession true if noSession exception is required
     * @return ItemType list
     */
    ItemTypeList getItemTypes(String providerType, boolean noSession);

    /**
     * Get the operations for the given providerType, declaredBy and sessionflag.
     *
     * @param providerType providerType to use
     * @param declaredBy who declared the operation
     * @param noSession true if noSession exception is required
     * @return list of operations
     */
    OperationList getOperations(String providerType, String declaredBy, boolean noSession);

}
