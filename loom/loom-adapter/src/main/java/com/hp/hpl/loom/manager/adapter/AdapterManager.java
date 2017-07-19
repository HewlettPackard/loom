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
package com.hp.hpl.loom.manager.adapter;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.hp.hpl.loom.adapter.Adapter;
import com.hp.hpl.loom.exceptions.DuplicateAdapterException;
import com.hp.hpl.loom.exceptions.DuplicateItemTypeException;
import com.hp.hpl.loom.exceptions.DuplicatePatternException;
import com.hp.hpl.loom.exceptions.LogicalIdAlreadyExistsException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.NullItemTypeIdException;
import com.hp.hpl.loom.exceptions.NullPatternIdException;
import com.hp.hpl.loom.exceptions.UserAlreadyConnectedException;
import com.hp.hpl.loom.manager.itemtype.ItemTypeManagement;
import com.hp.hpl.loom.manager.tapestry.PatternManagement;
import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionResult;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.Session;

/**
 * Abstract class that covers the AdapterManager functions - it needs to implement the interfaces
 * from {@link AggregationManagement}, {@link PatternManagement} and {@link ItemTypeManagement}.
 *
 */
public abstract class AdapterManager implements AggregationManagement, PatternManagement, ItemTypeManagement {

    /**
     * Returns a sorted List of providers know by this AdapterManager.
     *
     * They are ordered by the provider name.
     *
     * @return a sorted List of providers
     */
    public abstract List<Provider> getProviders();

    /**
     * Returns all the providers of the given type.
     *
     * They are ordered by the provider name.
     *
     * @param providerType The type to filter the providers on.
     * @return a sorted List of providers.
     */
    public abstract List<Provider> getProviders(final String providerType);

    /**
     * Returns a provider based on the providerType and provider Id - throws an exception if none
     * are found.
     *
     * @param providerType ProviderType to filter on
     * @param providerId ProviderId to match on
     * @return Provider
     * @throws NoSuchProviderException Thrown if no provider can be found
     */
    public abstract Provider getProvider(final String providerType, final String providerId)
            throws NoSuchProviderException;

    /**
     * Return a provider for a given itemLogicalId.
     *
     * @param itemLogicalId LogicalId to search for providers under
     * @return The provider
     * @throws NoSuchProviderException Thrown if no provider can be found
     */
    public abstract Provider getProvider(final String itemLogicalId) throws NoSuchProviderException;

    /**
     * Attempt to connect a user to a given provider using the provided Credentials.
     *
     * @param session The users session
     * @param provider The provider to connect to
     * @param creds The user credentials
     * @throws NoSuchProviderException Thrown if no provider can be found
     * @throws NoSuchSessionException Thrown if the session isn't valid
     * @throws UserAlreadyConnectedException Thrown if they are already connected
     */
    public abstract void userConnected(final Session session, final Provider provider, final Credentials creds)
            throws NoSuchProviderException, NoSuchSessionException, UserAlreadyConnectedException;

    /**
     * Disconnect the user.
     *
     * @param session The users session
     * @param provider The provider to disconnect to
     * @param creds The user credentials
     * @throws NoSuchProviderException Thrown if no provider can be found
     * @throws NoSuchSessionException Thrown if the session isn't valid
     */
    public abstract void userDisconnected(final Session session, final Provider provider, final Credentials creds)
            throws NoSuchProviderException, NoSuchSessionException;

    /**
     * Perform an action on a given set of items.
     *
     * @param session The users session
     * @param provider The provider to perform an action on
     * @param action The action to perform
     * @param items The items to perform the action on
     * @return The action results
     * @throws NoSuchProviderException Thrown if no provider can be found
     * @throws NoSuchSessionException Thrown if the session isn't valid
     */
    public abstract ActionResult doAction(final Session session, final Provider provider, final Action action,
            final Collection<Item> items) throws NoSuchProviderException, NoSuchSessionException;

    // /**
    // * Perform an action on a given set of itemLogicalIds.
    // *
    // * @param session The users session
    // * @param provider The provider to connect to
    // * @param action The action to perform
    // * @param itemLogicalIds The logical id's to perform the action on
    // * @return The action results
    // * @throws NoSuchProviderException Thrown if no provider can be found
    // * @throws NoSuchSessionException Thrown if the session isn't valid
    // */
    // public abstract ActionResult doAction(final Session session, final Provider provider, final
    // Action action,
    // final List<String> itemLogicalIds) throws NoSuchProviderException, NoSuchSessionException;

    /**
     * Perform an action on a given set of itemTypeId.
     *
     * @param session The users session
     * @param provider The provider to connect to
     * @param action The action to perform
     * @param itemTypeId The itemTypeId
     * @return The action results
     * @throws NoSuchProviderException Thrown if no provider can be found
     * @throws NoSuchSessionException Thrown if the session isn't valid
     */
    public abstract ActionResult doAction(final Session session, final Provider provider, final Action action,
            final String itemTypeId) throws NoSuchProviderException, NoSuchSessionException;

    /**
     * Gets the action results for a given actionResultId.
     *
     * @param session the session
     * @param actionResultId the action result id
     * @return the ActionResult
     * @throws NoSuchSessionException throw if no session is found
     */
    public abstract ActionResult getActionResult(final Session session, final UUID actionResultId)
            throws NoSuchSessionException;

    /**
     * Register an adapter with this adapterManager.
     *
     * @param adapter The adapter to register
     * @throws DuplicateAdapterException Thrown if this adapter is already registered
     * @throws NoSuchProviderException Thrown if the provider can't be found
     * @throws DuplicateItemTypeException Thrown if the adapter attempts to register a duplicate
     *         ItemType
     * @throws NullItemTypeIdException Thrown if the adapter attempts to register a null ItemTypeId
     * @throws DuplicatePatternException Thrown if the adapter attempts to register a duplicate
     *         pattern
     * @throws NullPatternIdException Thrown if the adapter attempts to register a pattern with a
     *         null id
     */
    public abstract void registerAdapter(final Adapter adapter)
            throws DuplicateAdapterException, NoSuchProviderException, DuplicateItemTypeException,
            NullItemTypeIdException, DuplicatePatternException, NullPatternIdException;

    /**
     * Deregister an adapter.
     *
     * @param adapter The adapter to register
     * @param sessions The sessions to kill off as well (deletes the aggregations)
     * @throws NoSuchProviderException Thrown if no provider can be found
     */
    public abstract void deregisterAdapter(final Adapter adapter, final Set<Session> sessions)
            throws NoSuchProviderException;

    /**
     * Create a grounded aggregation based on the ItemType, localId, name and description.
     *
     * @param session The users session
     * @param provider The provider to connect to
     * @param itemType The itemtype for the grounded aggregation
     * @param localId The localId for the grounded aggregation
     * @param mapped Whether the aggregation should map the logical ids
     * @param name Name to describe the aggregation
     * @param description Description for the aggregation
     * @param expectedSize Expected size for sizing
     * @return The grounded aggregation
     * @throws NoSuchSessionException Thrown if the session isn't valid
     * @throws LogicalIdAlreadyExistsException Thrown if the logicalId already exists
     * @throws NoSuchProviderException Thrown if no provider can be found
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public abstract Aggregation createGroundedAggregation(final Session session, final Provider provider,
            final ItemType itemType, final String localId, final boolean mapped, final String name,
            final String description, final int expectedSize)
            throws NoSuchSessionException, LogicalIdAlreadyExistsException, NoSuchProviderException;

    /**
     * Get the aggregation for a given itemLogicalId.
     *
     * @param session The users session
     * @param itemLogicalId The itemLogicalId
     * @return The aggregation
     * @throws NoSuchSessionException Thrown if the session isn't valid
     */
    public abstract Aggregation getAggregationForItem(final Session session, final String itemLogicalId)
            throws NoSuchSessionException;

    /**
     * Get the ItemTypeLocalId from the logicalId.
     *
     * @param provider The provider to connect to
     * @param logicalId LogicalId to lookup for
     * @return ItemTypelLocalId
     * @throws NoSuchProviderException Thrown if no provider can be found
     */
    public abstract String getItemTypeLocalIdFromLogicalId(final Provider provider, final String logicalId)
            throws NoSuchProviderException;

    /**
     * Returns a ItemType based on the provider and itemLocalId.
     *
     * @param provider The provider to connect to
     * @param itemLocalId ItemLocalId to lookup against
     * @return ItemType from the itemLocalId
     */
    public abstract ItemType getItemTypeLocalId(final Provider provider, final String itemLocalId);

}
