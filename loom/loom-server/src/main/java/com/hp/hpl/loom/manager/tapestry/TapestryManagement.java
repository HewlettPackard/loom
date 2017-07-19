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
package com.hp.hpl.loom.manager.tapestry;

import com.hp.hpl.loom.exceptions.InvalidQueryInputException;
import com.hp.hpl.loom.exceptions.ItemPropertyNotFound;
import com.hp.hpl.loom.exceptions.LogicalIdAlreadyExistsException;
import com.hp.hpl.loom.exceptions.NoSuchAggregationException;
import com.hp.hpl.loom.exceptions.NoSuchQueryDefinitionException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.NoSuchTapestryDefinitionException;
import com.hp.hpl.loom.exceptions.NoSuchThreadDefinitionException;
import com.hp.hpl.loom.exceptions.OperationException;
import com.hp.hpl.loom.exceptions.RelationPropertyNotFound;
import com.hp.hpl.loom.exceptions.ThreadDeletedByDynAdapterUnload;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.tapestry.TapestryDefinition;

/**
 * All supported Tapestry management operations. A Tapestry contains a number of ThreadDefinitions
 * and is associated with a single Session. A Session may not have more than one Tapestry. Some
 * operations will in turn interact with the Query Manager for Thread pre- and post-processing.
 */
public interface TapestryManagement {
    /**
     * Retrieve the Tapestry associated with the given Session.
     *
     * @param session the key for retrieval.
     * @return The Tapestry associated with the Session, null if no Tapestry has been previously
     *         registered.
     * @throws NoSuchSessionException The given Session does not exist.
     */
    TapestryDefinition getTapestryDefinition(Session session) throws NoSuchSessionException;

    /**
     * Create/update the Tapestry for the given Session. A unique identifier is allocated to the
     * Tapestry when it is first set. An attempt to register the same Tapestry definition will
     * result in no change. An attempt to register a Tapestry with an invalid identifier will result
     * in a NoSuchTapestryDefinitionException. In addition to recording the definition, this method
     * will also trigger pre-processing of the Threads by the Query Manager.
     *
     * @param session the Session this Tapestry belongs to.
     * @param tapestry the Tapestry definition associated with the Session.
     * @return The Tapestry now associated with the Session.
     * @throws NoSuchSessionException Session does not exist.
     * @throws NoSuchTapestryDefinitionException the Tapestry is not known within the given Session.
     * @throws NoSuchAggregationException QueryManager pre-processing failed.
     * @throws NoSuchQueryDefinitionException QueryManager pre-processing failed.
     * @throws InvalidQueryInputException QueryManager pre-processing failed.
     * @throws OperationException QueryManager pre-processing failed.
     * @throws NoSuchThreadDefinitionException QueryManager pre-processing failed.
     * @throws ItemPropertyNotFound
     * @throws RelationPropertyNotFound
     * @throws UnsupportedOperationException
     * @throws ThreadDeletedByDynAdapterUnload
     * @throws IllegalArgumentException
     */
    TapestryDefinition setTapestryDefinition(Session session, TapestryDefinition tapestry)
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, NoSuchAggregationException,
            NoSuchQueryDefinitionException, InvalidQueryInputException, OperationException,
            NoSuchThreadDefinitionException, LogicalIdAlreadyExistsException, ItemPropertyNotFound,
            RelationPropertyNotFound, ThreadDeletedByDynAdapterUnload;

    /**
     * Remove any Tapestry definition associated with the given Session. This will also trigger
     * Query Manager post-processing of the contained Threads.
     *
     * @param session the Session to clear.
     * @return The old Tapestry definition or null if there was none.
     * @throws NoSuchSessionException The Session does not exist.
     */
    TapestryDefinition clearTapestryDefinition(Session session) throws NoSuchSessionException;

    /**
     * Mark threads associated to input in the Query as gone when an adapter is unregistered
     *
     * @param session Session which threads have been affected by the unregistering process
     * @param logicalId id of the inputs affected by the unregistering
     */
    void markAsGone(Session session, String logicalId);
}
