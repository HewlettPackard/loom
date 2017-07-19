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
package com.hp.hpl.loom.manager.query;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.Provider;

/**
 * Classes implementing this interface handle the set of lambdas used by the query manager as
 * processing units of its pipeline.
 *
 * It offers basic CRUD operations to manage the lambdas.
 *
 * Operations are shared for all users at the moment. There is a set of default operations (@see
 * com.hp.hpl.loom.manager.query.DefaultOperations) that are declared at boot time anc cannot be
 * deleted
 *
 */
public interface OperationManager {

    /**
     * Register a new operation in Loom. The specified opId will automatically be preceded by
     * {@code<providerType>}{@link com.hp.hpl.loom.model.Provider#PROV_SEPARATOR}{@code <providerId>}
     * {@link com.hp.hpl.loom.model.Provider#PROV_SEPARATOR} so that the final ID would be:
     * {@code <providerType>}{@link com.hp.hpl.loom.model.Provider#PROV_SEPARATOR}
     * {@code<providerId>} {@link com.hp.hpl.loom.model.Provider#PROV_SEPARATOR}{@code<opId>}
     *
     * @param opId Id of the new operation
     * @param op Operation itself
     * @param prov Provider for whom the operation has been registered
     */
    void registerOperation4Provider(String opId, QuadFunctionMeta op, Provider prov);

    /**
     * Same as {@link #registerOperation4Provider} above. To be used by Loom itself to declare
     * default operations. Some user management is needed to prevent
     *
     * @param opId Id of the new operation
     * @param op Operation itself
     * @param loomRegisterCode A code used by Loom itself to register DefaultOperations and tests
     */
    void registerOperation(String opId, QuadFunctionMeta op, UUID loomRegisterCode);

    /**
     * Update already existent operation (except for default Loom operations).
     *
     * @param opId Id of the operation to be updated
     * @param op New version of the operation
     */
    void updateOperation(String opId, QuadFunctionMeta op);

    /**
     * Delete an existent operation.
     *
     * @param opId Id of the operation to be deleted
     * @param uuid A code used by Loom itself to register DefaultOperations and tests
     */
    void deleteOperation(String opId, final UUID uuid);

    /**
     * Function to be applied to the input data during a phase of the Query operation pipeline.
     *
     * @param opId Id of the op to be applied
     * @return Function to be invoked by the Query Executor to convert from input to output
     */
    @SuppressWarnings("checkstyle:linelength")
    QuadFunction<PipeLink<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, PipeLink<Fibre>> getFunction(
            String opId);

    /**
     * Retrieve the specification of an operation given its opId.
     *
     * @param opId operationId to lookup
     * @return Query operation associated to the specified ID
     */
    QuadFunctionMeta getOperation(String opId);

    /**
     * Deletes all operations given a provider.
     *
     * @param prov Provider which operations are to be deleted
     */
    void deleteOperationsForProvider(Provider prov);

    /**
     * Deletes all operations given some provider values.
     *
     * @param providerTypeAndId Type and id of the provider to be deleted
     */
    void deleteOperations(String providerTypeAndId);

    /**
     * Get list of all operation in Loom. Some implementations may filter operations
     *
     * @param uuid Loom uuid for privileged access
     * @return list of Loom operations
     */
    List<String> listOperations(UUID uuid);

    /**
     * Get list of all operation in Loom. Some implementations may filter operations per provider
     *
     * @param prov provider
     * @return list of Loom operations
     */
    List<String> listOperations(Provider prov);

    /**
     * Get list of all operation in Loom. Some implementations may filter operations per provider
     *
     * @param providerTypeAndId Concatenation of type and id used to register the operation
     * @return list of Loom operations
     */
    List<String> listOperations(String providerTypeAndId);

    /**
     * Get list of all operation in Loom. Some implementations may filter operations per provider
     *
     * @return list of Loom operations
     */
    List<String> getDefault();

    /**
     * Map of opIds and Functions available in Loom. Some implementations may filter the Entries in
     * the map based on provider
     *
     * @param uuid Loom uuid for privileged access and tests
     * @return Map of opIds to Function
     */
    @SuppressWarnings("checkstyle:linelength")
    Map<String, QuadFunction<PipeLink<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, PipeLink<Fibre>>> getAllFunctions(
            UUID uuid);

    /**
     * Map of opIds and Functions available in Loom. Some implementations may filter the Entries in
     * the map based on provider
     *
     * @param provider Provider which functions are to be obtained
     * @return Map of opIds to Function for the specified provider
     */
    @SuppressWarnings("checkstyle:linelength")
    Map<String, QuadFunction<PipeLink<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, PipeLink<Fibre>>> getAllFunctions(
            Provider provider);



}
