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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.hp.hpl.loom.exceptions.AdapterConfigException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.UserAlreadyConnectedException;
import com.hp.hpl.loom.manager.adapter.AdapterConfig;
import com.hp.hpl.loom.manager.adapter.AdapterManager;
import com.hp.hpl.loom.manager.query.OperationContext;
import com.hp.hpl.loom.manager.query.OperationErrorCode;
import com.hp.hpl.loom.manager.query.PipeLink;
import com.hp.hpl.loom.manager.query.QuadFunction;
import com.hp.hpl.loom.manager.query.QuadFunctionMeta;
import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionResult;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.stitcher.StitcherRulePair;
import com.hp.hpl.loom.tapestry.PatternDefinition;

/**
 * Interface for adapters.
 *
 */
public interface Adapter {

    /**
     * Gets the provider for this adapter.
     *
     * @return Provider instance associated with this adapter. Existing code assumes a 1-1 mmapping
     *         between adapter and provider
     */
    Provider getProvider();

    /**
     * AdapterManager is the only Loom entity that the adapter needs to know about. All Loom calls
     * from adapters go through it. This is set by the adapterLoader when the adapter is loaded.
     *
     * The Adapter properties are detected by the AdapterLoader and set at loading time.
     *
     * @param adapterManager AdapterManager to set on this adapter
     * @param adapterConfig AdapterConfig to configure this adapter
     * @throws AdapterConfigException thrown if the adapter config is invalid
     */
    void setAdapterManager(AdapterManager adapterManager, PropertiesConfiguration adapterConfig)
            throws AdapterConfigException;

    /**
     * Method called before any model definitions (ItemTypes, Operations, etc.) are added so this
     * should be used by the adapter to set itself up and create any resources it requires.
     */
    void onLoad();

    /**
     * Called by the AdapterLoader when the adapter is torn down. This method should destroy all
     * resources created in method beforeRegisterAdapterManager()
     */
    void onUnload();

    /**
     * Called during adapter registration to optionally return a collection of Stitcher rules to
     * define equivalence relationships between Items that are created by different Adapters.
     *
     * @return Return any {@link StitcherRulePair} created by adapter, or an empty collection.
     * @see StitcherRulePair
     */
    Collection<StitcherRulePair<?, ?>> getStitchingRules();

    /**
     * Called during adapter registration.
     *
     * @return all ItemTypes explicitly created by adapter
     */
    Collection<ItemType> getItemTypes();

    /**
     * If some ItemTypes are created or modified implicitly by using annotations on Items, those
     * items should be returned here.
     *
     * @return all Item classes containing annotations defining or modifying ItemTypes
     */
    Collection<Class> getAnnotatedItemsClasses();

    /**
     * Called at adapter registration time.
     *
     * @return all PatternDefinitions suggested by adapter
     */
    Collection<PatternDefinition> getPatternDefinitions();

    /**
     * method called whenever a user logs in with Loom. This should trigger the process of
     * collecting data for this particular user credentials within the context of the given session.
     *
     * @param session The client' session
     * @param creds The user credentials
     * @throws UserAlreadyConnectedException if this method has already been called with the same
     *         Session object
     */
    void userConnected(Session session, Credentials creds) throws UserAlreadyConnectedException;

    /**
     * method called whenever a user logs out of Loom. This should stop data collection for this
     * user and destroy all resources used by that process.
     *
     * @param session The client' session
     * @param creds The user credentials
     * @throws NoSuchSessionException if Session has not been quoted before in a userConnected call
     */
    void userDisconnected(Session session, Credentials creds) throws NoSuchSessionException;

    /**
     * performs the given action on all Items contained in the collection. The returned ActionResult
     * indicates the success of scheduling the action after validation instead of the success of the
     * action itself. This is a limitation of the current release where Loom calls this method
     * synchronously and therefore relies on adapters to return quickly to avoid holding the session
     * lock for too long.
     *
     * @param session The client' session
     * @param action Action to perform
     * @param items Items to perform action on
     * @return The result of the action
     * @throws NoSuchSessionException Thrown if the isn't a session
     */
    ActionResult doAction(Session session, Action action, Collection<Item> items) throws NoSuchSessionException;

    /**
     * performs the given action on all Items referenced by their logicalIds in the List. The
     * returned ActionResult indicates the success of scheduling the action after validation instead
     * of the success of the action itself. This is a limitation of the current release where Loom
     * calls this method synchronously and therefore relies on adapters to return quickly to avoid
     * holding the session lock for too long.
     *
     * @param session The client' session
     * @param action Action to perform
     * @param itemLogicalIds ItemLogicalIds to perform action on
     * @return The result of the action
     * @throws NoSuchSessionException Thrown if the isn't a session
     */
    ActionResult doAction(Session session, Action action, List<String> itemLogicalIds) throws NoSuchSessionException;

    /**
     * Performs the given action on this itemType. It allows for thread level actions rather than
     * acting on items/aggregations.
     *
     * @param session The client' session
     * @param action Action to perform
     * @param itemTypeId The itemTypeId
     * @return The result of the action
     * @throws NoSuchSessionException Thrown if the isn't a session
     */
    ActionResult doAction(final Session session, final Action action, final String itemTypeId)
            throws NoSuchSessionException;

    /**
     * method called at registration time by the adapterLoader.
     *
     * @param map the key is the case insensitive operation name; the value is the QueryOperation
     *        containing the FunctionalInterface to execute whenever a Query quotes the operation
     *        name.
     * @return the map of operation strings to query operation
     */
    @SuppressWarnings("checkstyle:linelength")
    Map<String, QuadFunctionMeta> registerQueryOperations(
            Map<String, QuadFunction<PipeLink<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, PipeLink<Fibre>>> map);

    /**
     * Gets the sessions for this adapter.
     *
     * @return the set of adapters
     */
    Set<Session> getSessions();

    /**
     * Gets the adapter config.
     *
     * @return the config
     */
    AdapterConfig getAdapterConfig();
}
