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
package com.hp.hpl.loom.manager.aggregation;

import java.util.List;

import com.hp.hpl.loom.exceptions.LogicalIdAlreadyExistsException;
import com.hp.hpl.loom.exceptions.NoSuchAggregationException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.SessionAlreadyExistsException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.Session;

public interface AggregationManagementInternal {

    //
    // Operations on Sessions
    //

    void createSession(Session session) throws NoSuchSessionException, SessionAlreadyExistsException;

    void deleteSession(Session session) throws NoSuchSessionException;

    void deleteAllSessions();

    //
    // Operations on Grounded Aggregations
    //

    /**
     * Create an empty Grounded Aggregation in specified session, with specified logicalId.
     *
     * @param session Current Session.
     * @param provider Provider that is creating the GA.
     * @param itemType ItemType of the Items contained in the GA.
     * @param logicalId Logical ID of the Grounded Aggregation.
     * @param mappedLogicalId Optionally specify a Mapped Logical ID for the GA.
     * @param name Name of the GA.
     * @param description description of the GA.
     * @param expectedSize Hint to indicate how many Items will be contained in the GA, to determine
     *        initial sizing.
     * @return The created Grounded Aggregations.
     * @throws NoSuchSessionException
     * @throws LogicalIdAlreadyExistsException
     */
    @SuppressWarnings("checkstyle:parameternumber")
    Aggregation createGroundedAggregation(Session session, Provider provider, ItemType itemType, String logicalId,
            String mappedLogicalId, String name, String description, int expectedSize)
            throws NoSuchSessionException, LogicalIdAlreadyExistsException;

    /**
     * Return true if the Grounded Aggregation with the specified Logical ID exists.
     *
     * @param session Current Session.
     * @param logicalId Logical ID of the Grounded Aggregation.
     * @return True if the Grounded Aggregation exists.
     * @throws NoSuchSessionException The specified session does not exist.
     */
    boolean groundedAggregationExists(Session session, String logicalId) throws NoSuchSessionException;


    /**
     * List all Grounded Aggregations in specified session.
     *
     * @param session Current Session.
     * @throws NoSuchSessionException The specified session does not exist.
     */
    List<Aggregation> listGroundedAggregations(Session session) throws NoSuchSessionException;

    // // Operations on Derived Aggregations //


    /**
     * Create an empty Derived Aggregation in specified session, with specified logicalId, that is
     * derived from another Aggregation.
     */

    @SuppressWarnings("checkstyle:parameternumber")
    Aggregation createDerivedAggregation(Session session, String typeId, String logicalId, Fibre.Type contains,
            String name, String description, String[] dependsOnLogicalIdArray, int expectedSize)
            throws NoSuchSessionException, LogicalIdAlreadyExistsException, NoSuchAggregationException;


    /**
     * Update Aggregation to swap to a new list of elements.
     */
    void updateDerivedAggregation(Session session, Aggregation aggregation, List<Fibre> elements)
            throws NoSuchSessionException, NoSuchAggregationException;

    /**
     * Delete all Grounded Aggregations in specified session.
     */
    void deleteAllDerivedAggregations(Session session) throws NoSuchSessionException;

    /**
     * List all Derived Aggregations in specified session.
     */
    List<Aggregation> listDerivedAggregations(Session session) throws NoSuchSessionException;

    /**
     * List all top-level Derived Aggregations in specified session.
     */
    List<Aggregation> listTopLevelDerivedAggregations(Session session) throws NoSuchSessionException;

    boolean derivedAggregationExists(Session session, String logicalId) throws NoSuchSessionException;

    //
    // Operations on Aggregations
    //

    /**
     * Delete an Aggregation in specified session, with specified logicalId.
     */
    void deleteAggregation(Session session, String logicalId) throws NoSuchSessionException;

    /**
     * Delete an Aggregation in specified session, with specified logicalId, and any children.
     * Implies deleting any Derived Aggregations, if aggregation is a GA.
     *
     * @param dynAdapterDeregistration Lets the TapManager know when an adapter has disappeared and
     *        the thread in the tapestry must be marked as deleted, so that the query manager sends
     *        and appropriate error to the client
     */
    void deleteAggregationAndChildren(Session session, String logicalId, boolean dynAdapterDeregistration)
            throws NoSuchSessionException, NoSuchAggregationException;

    /**
     * Get an Aggregation in specified session, with specified logicalId, or null if not exist.
     */
    Aggregation getAggregation(Session session, String logicalId) throws NoSuchSessionException;

    /**
     * List all Aggregations in specified session, both Grounded and Derived.
     */
    List<Aggregation> listAggregations(Session session) throws NoSuchSessionException;

    /**
     * Return true is the specified aggregation is not dirty.
     *
     * @param session the session to check in
     * @param logicalId the logical id to check for
     * @return boolean
     * @throws NoSuchSessionException If the session doesn't exist
     * @throws NoSuchAggregationException If the aggregation doesn't exist
     */
    boolean isUpToDate(Session session, String logicalId) throws NoSuchSessionException, NoSuchAggregationException;

}
