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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import com.hp.hpl.loom.exceptions.AdapterConfigException;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.manager.adapter.AdapterConfig;
import com.hp.hpl.loom.manager.adapter.AdapterManager;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.manager.query.QueryOperation;
import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionResult;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.stitcher.StitcherRulePair;
import com.hp.hpl.loom.tapestry.Meta;
import com.hp.hpl.loom.tapestry.Operation;
import com.hp.hpl.loom.tapestry.PatternDefinition;
import com.hp.hpl.loom.tapestry.QueryDefinition;
import com.hp.hpl.loom.tapestry.ThreadDefinition;

/**
 * This class implements most of the Adapter interface greatly reducing the overhead of producing
 * new adapters.
 *
 */
public abstract class BaseAdapter implements Adapter, Runnable {
    private static final Log LOG = LogFactory.getLog(BaseAdapter.class);

    private ItemCollector global = null;

    protected Provider provider;

    protected AdapterManager adapterManager;

    protected AdapterConfig adapterConfig;

    private Map<Session, ItemCollector> collectorMap;
    private ExecutorService collectorExec;
    protected int collectThreads;
    private ExecutorService schedExec;
    private int schedulingInterval;
    private boolean open = false;


    private void setup() {
        collectThreads = adapterConfig.getCollectThreads();
        schedulingInterval = adapterConfig.getSchedulingInterval();

        provider = createProvider(adapterConfig.getProviderType(), adapterConfig.getProviderId(),
                adapterConfig.getAuthEndpoint(), adapterConfig.getProviderName());
        collectorMap = new HashMap<>();
        schedExec = Executors.newFixedThreadPool(1,
                new CustomizableThreadFactory(provider.getProviderTypeAndId() + "#sched-"));
        // itemTypeMap = new HashMap<>();
        // patternMap = new HashMap<>();
    }

    @Override
    public Set<Session> getSessions() {
        if (collectorMap != null) {
            return collectorMap.keySet();
        } else {
            return new HashSet<Session>();
        }
    }

    @Override
    public void setAdapterManager(final AdapterManager aManager, final PropertiesConfiguration properties)
            throws AdapterConfigException {
        adapterManager = aManager;
        adapterConfig = new AdapterConfig(properties);
    }

    @Override
    public void onLoad() {
        this.setup();

        // creating the collectorExec object based on property set by Spring
        collectorExec = Executors.newFixedThreadPool(collectThreads,
                new CustomizableThreadFactory(provider.getProviderTypeAndId() + "#collect-"));
        // starting polling thread
        open = true;
        // this uses schedulingInterval that should have been set automatically
        schedExec.execute(this);
    }

    @Override
    @PreDestroy
    public void onUnload() {
        // close down thread
        synchronized (this) {
            open = false;
            notifyAll();
        }
    }

    /**
     * Gets the itemType for a given localId.
     *
     * @param localIds the local id to use in the lookup
     * @return The list of {@link ItemType}
     */
    public List<ItemType> getItemTypesFromLocalIds(final List<String> localIds) {
        List<ItemType> itList = new ArrayList<>(localIds.size());
        for (String localId : localIds) {
            itList.add(getItemType(localId));
        }
        return itList;
    }

    /**
     * Gets the {@link ItemType} for a given localId.
     *
     * @param localId localId to lookup with
     * @return the ItemType
     */
    public ItemType getItemType(final String localId) {
        return adapterManager.getItemTypeLocalId(this.getProvider(), localId);
    }

    /**
     * Gets the logicalId for a given aggregation and itemId.
     *
     * @param aggregation aggregation to build from
     * @param itemId itemId to build from
     * @return logicalId of the aggregation and itemId
     */
    public String getItemLogicalId(final Aggregation aggregation, final String itemId) {
        return LoomUtils.getItemLogicalId(aggregation, itemId);
    }

    /**
     * Gets the aggregation for a given logicalId.
     *
     * @param itemTypeLocalId the logical id to lookup with
     * @return the corresponding aggregation
     * @throws NoSuchItemTypeException throw if an itemType isn't found
     */
    public String getAggregationLogicalId(final String itemTypeLocalId) throws NoSuchItemTypeException {
        if (itemTypeLocalId == null) {
            throw new IllegalArgumentException("cannot derive an aggregationLogicalId if itemTypeLocalId is null");
        }
        if (adapterManager.getItemTypeLocalId(this.getProvider(), itemTypeLocalId) == null) {
            throw new NoSuchItemTypeException(itemTypeLocalId);
        }
        return LoomUtils.getAggregationLogicalIdFromItemType(provider, itemTypeLocalId, null);
    }

    /**
     * Gets the local id for an itemType based on the logical id.
     *
     * @param logicalId logical id to lookup with
     * @return ItemType local id
     * @throws NoSuchProviderException throw if the provider doesn't exist
     */
    public String getItemTypeLocalIdFromLogicalId(final String logicalId) throws NoSuchProviderException {
        return adapterManager.getItemTypeLocalIdFromLogicalId(provider, logicalId);
    }

    protected String getAggregationLogicalIdForPattern(final String itemTypeLocalId) {
        return LoomUtils.getMergedLogicalIdFromItemType(provider, itemTypeLocalId, null);
    }

    protected String createHumanReadableThreadName(final ItemType type) {
        String localId = type.getLocalId();

        return localId.substring(0, 1).toUpperCase() + localId.substring(1) + "s";
    }


    /**
     * Create a pattern definition for a single input thread.
     *
     * @param id pattern id
     * @param itemTypes item types
     * @param description description
     * @param maxFibres max fibres
     *
     * @param defaultPattern is this a default pattern
     * @return the pattern definition
     */
    public PatternDefinition createPatternDefinitionWithSingleInputPerThread(final String id,
            final List<ItemType> itemTypes, final String description, final List<Integer> maxFibres,
            final boolean defaultPattern) {
        return createPatternDefinitionWithSingleInputPerThread(id, itemTypes, description, maxFibres, defaultPattern,
                new ArrayList<>());
    }

    /**
     * Create a pattern definition for a single input thread.
     *
     * @param id pattern id
     * @param itemTypes item types
     * @param description description
     * @param maxFibres max fibres
     * @param defaultPattern is this a default pattern
     * @param metrics names of the default metrics to be loaded when pattern is first loaded
     * @return the pattern definition
     */
    public PatternDefinition createPatternDefinitionWithSingleInputPerThread(final String id,
            final List<ItemType> itemTypes, final String description, final List<Integer> maxFibres,
            final boolean defaultPattern, final List<String> metrics) {

        if (maxFibres != null && itemTypes.size() != maxFibres.size()) {
            throw new IllegalArgumentException(
                    "itemTypes (" + itemTypes.size() + ") & maxFibres (" + maxFibres.size() + " sizes should match");
        }

        List<Integer> fibreList;
        if (maxFibres == null) {
            fibreList = Collections.nCopies(itemTypes.size(), 0);
        } else {
            fibreList = maxFibres;
        }

        List<ThreadDefinition> threadDefs = new ArrayList<>(itemTypes.size());
        Map<String, ItemType> itMap = new HashMap<>(itemTypes.size());
        int threadIdx = 0;
        for (ItemType it : itemTypes) {
            List<String> ins = new ArrayList<String>(1);
            ins.add(getAggregationLogicalIdForPattern(it.getLocalId()));
            QueryDefinition query;
            if (fibreList.get(threadIdx) == 0) {
                query = new QueryDefinition(ins);
            } else {
                Map<String, Object> braidParams = new HashMap<String, Object>(1);
                braidParams.put(QueryOperation.MAX_FIBRES, fibreList.get(threadIdx));
                Operation braidOperation = new Operation(DefaultOperations.BRAID.toString(), braidParams);
                List<Operation> braidPipe = new ArrayList<>(1);
                braidPipe.add(braidOperation);
                query = new QueryDefinition(braidPipe, ins);
            }
            ThreadDefinition threadDefinition = new ThreadDefinition(id + "-" + Integer.toString(threadIdx++),
                    it.getId(), query, createHumanReadableThreadName(it));
            threadDefs.add(threadDefinition);
            itMap.put(it.getId(), it);
        }

        PatternDefinition pd = new PatternDefinition(id, threadDefs, provider.getProviderType(), new Meta(itMap),
                description, metrics);
        pd.setDefaultPattern(defaultPattern);

        return pd;
    }

    @Override
    public Provider getProvider() {
        return provider;
    }

    @Override
    public void run() {

        if (LOG.isInfoEnabled()) {
            LOG.info(this.getProvider().getProviderName() + "/" + this.getProvider().getProviderId()
                    + ": Adapter polling thread is starting");

        }
        while (open) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Adapter scheduling a new run of ItemCollectors");
            }
            ArrayList<ItemCollector> ics;
            synchronized (collectorMap) {
                ics = new ArrayList<>(collectorMap.values());
            }
            for (ItemCollector ic : ics) {
                if (ic.setScheduled()) {
                    collectorExec.execute(ic);
                }
            }
            synchronized (this) {
                try {
                    wait(schedulingInterval);
                } catch (InterruptedException ie) {
                    LOG.warn("adapter scheduling thread interrupted - closing down?");
                }
            }
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("Adapter polling thread is exiting");
        }
    }

    @Override
    public void userConnected(final Session session, final Credentials creds) {
        ItemCollector ic = collectorMap.get(session);
        if (ic == null) {
            ic = this.getNewItemCollectorInstanceStoreGlobal(session, creds);
            synchronized (collectorMap) {
                collectorMap.put(session, ic);
            }
        }
        LOG.info("Setting credentials");
        ic.setCredentials(creds);
        LOG.info("Setting credentials set");
        ic.setIdle();
        if (ic.setScheduled()) {
            collectorExec.execute(ic);
        }
    }

    @Override
    public void userDisconnected(final Session session, final Credentials creds) throws NoSuchSessionException {
        ItemCollector ic;
        synchronized (collectorMap) {
            ic = collectorMap.remove(session);
        }
        if (ic == null) {
            throw new NoSuchSessionException("Can't locate session", session);
        }
        ic.close();
    }

    @Override
    public ActionResult doAction(final Session session, final Action action, final Collection<Item> items)
            throws NoSuchSessionException {
        ItemCollector ic = collectorMap.get(session);
        if (ic == null) {
            throw new NoSuchSessionException(session);
        }
        return ic.doAction(action, items);
    }

    @SuppressWarnings("checkstyle:linelength")
    @Override
    public ActionResult doAction(final Session session, final Action action, final String itemTypeId)
            throws NoSuchSessionException {
        ItemCollector ic = collectorMap.get(session);
        if (ic == null) {
            throw new NoSuchSessionException(session);
        }
        return ic.doAction(action, itemTypeId);
    }

    @Override
    public ActionResult doAction(final Session session, final Action action, final List<String> logicalIds)
            throws NoSuchSessionException {
        ItemCollector ic = collectorMap.get(session);
        if (ic == null) {
            throw new NoSuchSessionException(session);
        }
        return ic.doAction(action, logicalIds);
    }

    @Override
    public Collection<StitcherRulePair<?, ?>> getStitchingRules() {
        return new ArrayList<StitcherRulePair<?, ?>>();
    }

    // abstract methods
    @Override
    public abstract Collection<ItemType> getItemTypes();

    @Override
    public abstract Collection<PatternDefinition> getPatternDefinitions();

    protected ItemCollector getNewItemCollectorInstanceStoreGlobal(Session session, Credentials creds) {
        ItemCollector collector = getNewItemCollectorInstance(session, creds);
        collector.setCredentials(creds);
        if (collector.isGlobal()) {
            global = collector;
        }
        return collector;
    }

    /**
     * Get a new ItemCollector for a given session and creds.
     *
     * @param session session to base the item collector on
     * @param creds creds to use
     * @return Item Collector
     */
    protected abstract ItemCollector getNewItemCollectorInstance(Session session, Credentials creds);

    /**
     * Return the global ItemCollector.
     *
     * @return the global itemCollector
     */
    public ItemCollector getGlobal() {
        return global;
    }

    /**
     * Created the provider.
     *
     * @param providerType providerType
     * @param providerId providerId
     * @param authEndpoint authEndpoint
     * @param providerName providerName
     * @return The provider
     */
    protected abstract Provider createProvider(String providerType, String providerId, String authEndpoint,
            String providerName);

    /**
     * only here for testing.
     *
     * @param session session to lookup for
     * @return ItemCollector for the provided session
     */
    public ItemCollector getItemCollector(final Session session) {
        return collectorMap.get(session);
    }

    /**
     * Returns the AdapterConfig.
     *
     * @return the adapter config
     */
    public AdapterConfig getAdapterConfig() {
        return adapterConfig;
    }

}
