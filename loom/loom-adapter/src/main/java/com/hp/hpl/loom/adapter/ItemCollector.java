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
package com.hp.hpl.loom.adapter;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionResult;
import com.hp.hpl.loom.model.CoreItemAttributes;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Item;

/**
 * ItemCollector interface that defines the collection interface.
 *
 */
public interface ItemCollector extends Runnable {

    /**
     * Open state.
     */
    String OPEN = "open";
    /**
     * Scheduled state.
     */
    String SCHEDULED = "scheduled";
    /**
     * Busy state.
     */
    String BUSY = "busy";
    /**
     * Idle state.
     */
    String IDLE = "idle";
    /**
     * Closed state.
     */
    String CLOSED = "closed";
    /**
     * Paused state.
     */
    String PAUSED = "paused";

    /**
     * Called when the collector should close.
     */
    void close();

    /**
     * Called to notify the collector it has been scheduled.
     *
     * @return if it was successful
     */
    boolean setScheduled();

    /**
     * Called to set the collector into an idle state.
     *
     * @return if it was successful
     */
    boolean setIdle();

    /**
     * Sets the credentials of the user.
     *
     * @param credentials the user creditionals
     */
    void setCredentials(Credentials credentials);

    /**
     * Is this collector a global one.
     *
     * @return the global flag
     */
    boolean isGlobal();

    /**
     * Returns an aggregationUpdate for a given itemTypeId.
     *
     * @param itemTypeId itemTypeId to find hte aggregationUpdater for
     * @return the aggregationUpdater
     */
    AggregationUpdater getUpdater(String itemTypeId);

    /**
     * Called to perform an action on an itemTypeId.
     *
     * @param action The action to perform
     * @param itemTypeId The itemTypeId to perform an action on
     * @return actionResult for the action
     */
    ActionResult doAction(final Action action, final String itemTypeId);

    /**
     * Called to perform an action.
     *
     * @param action The action to perform
     * @param items The items to perform the action on
     * @return actionResult for the action
     */
    ActionResult doAction(Action action, Collection<Item> items);

    /**
     * Called to perform an action.
     *
     * @param action The action to perform
     * @param itemLogicalIds the itemLogicalIds to perform the action on
     * @return actionResult for the action
     */
    ActionResult doAction(Action action, List<String> itemLogicalIds);

    /**
     * Get the items from the adapter for the given parameters.
     *
     * @param itemLocalTypeId itemLocalTypeId to lookup with
     * @param logicalId the logicalId to lookup with
     * @param <A> AdapterItem type to return
     * @return coreItemAttributes to return for the given parameters.
     */
    <A extends CoreItemAttributes> AdapterItem<A> getAdapterItem(String itemLocalTypeId, String logicalId);

    /**
     * Get the news items from the adapter for the given parameters.
     *
     * @param itemLocalTypeId itemLocalTypeId to lookup with
     * @param logicalId the logicalId to lookup with
     * @param <A> AdapterItem type to return
     * @return coreItemAttributes to return for the given parameters.
     */
    <A extends CoreItemAttributes> AdapterItem<A> getNewAdapterItem(String itemLocalTypeId, String logicalId);

    /**
     * Sets a categorizeItem as updated for the given parameters.
     *
     * @param itemLocalTypeId itemLocalTypeId to lookup with
     * @param item the logicalId to lookup with
     */
    void categorizeItemAsUpdated(String itemLocalTypeId, Item item);

    /**
     * Get the logicalId for the give parameters.
     *
     * @param itemTypeId itemTypeId to build the logicalId from
     * @param resourceId resourceId to build the logicalId from
     * @return The logicalId derived from the itemTypeId and resourceId
     */
    String getLogicalId(String itemTypeId, String resourceId);

    /**
     * These method was added in order to allow new entities to find previously declared
     * relationships with them in the same update cycle. E.g.: If On the Docker adapter, if a host
     * is created and declares a relationship to a non-existing container, the container - when it
     * gets created - should be able to find who has a relationship with it.
     *
     * @return the relationshipsDiscoveredOnCurrentUpdateCycle
     */
    HashMap<String, HashMap<String, String>> getRelationshipsDiscoveredOnCurrentUpdateCycle();
}
