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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.exceptions.InvalidActionSpecificationException;
import com.hp.hpl.loom.exceptions.LogicalIdAlreadyExistsException;
import com.hp.hpl.loom.exceptions.NoSuchAggregationException;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.manager.adapter.AdapterManager;
import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionResult;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.Session;

/**
 * Abstract class that implements the BaseItemCollector with some default implementation.
 */
public abstract class AggregationUpdaterBasedItemCollector extends BaseItemCollector {
    private static final int GA_EXPECTED_SIZE = 60;

    private static final int TIME_DISPLAY_DEFAULT = 5;

    private static final Log LOG = LogFactory.getLog(AggregationUpdaterBasedItemCollector.class);

    protected static final int DFLT_NEW_THRESHOLD = 10000;
    protected static final int DFLT_UPDATE_THRESHOLD = 10000;
    protected static final int DFLT_DELETION_THRESHOLD = 10000;
    protected static final int DFLT_CHANGES_THRESHOLD = 10000;

    protected BaseAdapter adapter;
    protected AdapterManager adapterManager;
    protected Map<String, AggregationUpdater<? extends AdapterItem<?>, ? extends CoreItemAttributes, ?>> updaterMap;
    protected Map<String, Aggregation> aggregationMap;
    protected Map<String, Aggregation> unregisteredAggregationMap;
    protected int runIdx = 0;
    protected int timeDisplayInterval = TIME_DISPLAY_DEFAULT;
    protected TimingBag collectTimingBag;
    protected TimingBag updateCreationTimingBag;
    protected Map<String, AdapterUpdateResult> updateResultMap;
    protected Map<String, Collection<Item>> testCollectionMap;

    /**
     * Constructor for the {@link AggregationUpdaterBasedItemCollector}.
     *
     * @param session Session to collect for
     * @param adapter Adapter to collect for
     * @param adapterManager AdapterManger to work with
     */
    public AggregationUpdaterBasedItemCollector(final Session session, final BaseAdapter adapter,
            final AdapterManager adapterManager) {
        super(session);
        this.adapter = adapter;
        setProvider(adapter.getProvider());
        this.adapterManager = adapterManager;
        updaterMap = new HashMap<>();
        aggregationMap = new HashMap<>();
        unregisteredAggregationMap = new HashMap<>();
        updateResultMap = new HashMap<>();
        testCollectionMap = new HashMap<>();
        collectTimingBag = new TimingBag("all collected types");
        updateCreationTimingBag = new TimingBag("all updated types");
        createAggregations();
    }

    @Override
    public AggregationUpdater getUpdater(final String itemTypeId) {
        return updaterMap.get(itemTypeId);
    }

    protected Aggregation createRegisteredAggregation(final String itemTypeId) throws NoSuchSessionException,
            LogicalIdAlreadyExistsException, NoSuchItemTypeException, NoSuchProviderException {
        String aggName = assignAggregationName(itemTypeId);
        return adapterManager.createGroundedAggregation(session, provider, adapter.getItemType(itemTypeId), null, true,
                aggName, "List of " + aggName, GA_EXPECTED_SIZE);
    }

    protected Aggregation createUnregisteredAggregation(final String itemTypeId) throws NoSuchSessionException,
            LogicalIdAlreadyExistsException, NoSuchItemTypeException, NoSuchProviderException {
        String aggName = assignAggregationName(itemTypeId);

        if (LOG.isDebugEnabled()) {
            LOG.debug("creating an unregistered aggregation for itemType: " + itemTypeId);
        }

        Aggregation agg = new Aggregation(adapter.getAggregationLogicalId(itemTypeId), itemTypeId, aggName, "", 0);

        return agg;
    }

    protected String assignAggregationName(final String itemTypeId) {
        return itemTypeId + "s";
    }

    protected void createAggregations() {
        try {
            for (String itemTypeId : getUpdateItemTypeIdList()) {
                Aggregation agg = createRegisteredAggregation(itemTypeId);
                aggregationMap.put(itemTypeId, agg);
            }
            for (String itemTypeId : getCollectionItemTypeIdList()) {
                if (aggregationMap.get(itemTypeId) == null) {
                    Aggregation agg = createUnregisteredAggregation(itemTypeId);
                    unregisteredAggregationMap.put(itemTypeId, agg);
                }
            }
        } catch (NoSuchItemTypeException nsie) {
            LOG.error("itemtypeId is invalid - abort data update", nsie);
        } catch (NoSuchSessionException nsse) {
            LOG.error("AggregationManager did not recognise the session - abort data update", nsse);
        } catch (LogicalIdAlreadyExistsException lae) {
            LOG.error("AggregationManager already had Aggregation with the logicalId - abort data update", lae);
        } catch (NoSuchProviderException nspe) {
            LOG.error("AdapterManager does not recognise this provider - abort data update: " + provider, nspe);
        }
    }

    /**
     * Returns the provider for this collector.
     *
     * @return the provider.
     */
    public Provider getProvider() {
        return provider;
    }

    @Override
    public void close() {
        super.close();
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleting groundedAggregation for session: " + session.getId());
        }
        try {
            // remove only registered aggregations
            for (Aggregation agg : aggregationMap.values()) {
                adapterManager.deleteGroundedAggregation(session, agg.getLogicalId());
            }
        } catch (NoSuchSessionException e) {
            LOG.error("AggregationManager did not recognise the session - abort aggregation deletion", e);
        } catch (NoSuchAggregationException nsae) {
            LOG.error("AggregationManager did not recognise the aggregation - abort aggregation deletion", nsae);
        }
    }

    protected Aggregation getAggregation(final String itemTypeId) {
        Aggregation agg = aggregationMap.get(itemTypeId);
        if (agg == null) {
            agg = unregisteredAggregationMap.get(itemTypeId);
        }
        return agg;
    }

    protected void createUpdater(final String itemTypeId) throws NoSuchProviderException, NoSuchItemTypeException {
        if (updaterMap.get(itemTypeId) == null) {
            updaterMap.put(itemTypeId, getAggregationUpdater(getAggregation(itemTypeId)));
        }
    }

    @Override
    protected void collectItems() {
        int thisRunIdx = runIdx++;

        if (LOG.isDebugEnabled()) {
            LOG.debug("starting Collection task for session: " + session.getId() + " provider: "
                    + provider.getProviderId() + "runIdx: " + thisRunIdx);
        }

        try {
            // create updaters
            for (String itemTypeId : getCollectionItemTypeIdList()) {
                createUpdater(itemTypeId);
            }

            // let the subclass to specific processing
            preUpdateItemHook();

            // Creates a new relationshipMap, so items can find relationships inside the same update
            // cycle.
            relationshipsDiscoveredOnCurrentUpdateCycle = new HashMap<String, HashMap<String, String>>();

            // attributes check and update
            for (String itemTypeId : getCollectionItemTypeIdList()) {
                AggregationUpdater<? extends AdapterItem<?>, ? extends CoreItemAttributes, ?> updater =
                        updaterMap.get(itemTypeId);
                if (updater != null) {
                    AdapterUpdateResult upRes = updater.updateItems(thisRunIdx);
                    updateResultMap.put(itemTypeId, upRes);
                }
            }

            // roll up the times and record
            long allTime = 0;
            for (AggregationUpdater<? extends AdapterItem<?>, ? extends CoreItemAttributes, ?> aggUp : updaterMap
                    .values()) {
                allTime += aggUp.getCollectTimingBag().getLatest();
            }
            collectTimingBag.recordTime(allTime);

            // let the subclass to specific processing
            postUpdateItemHook();

            boolean deltaEnable = !tooManyDeltas();

            // data update
            for (String itemTypeId : getUpdateItemTypeIdList()) {
                AggregationUpdater<? extends AdapterItem<?>, ? extends CoreItemAttributes, ?> updater =
                        updaterMap.get(itemTypeId);
                if (updater != null) {
                    UpdateResult upRes = updater.updateRelationships(deltaEnable);
                    testCollectionMap.put(itemTypeId, upRes.getAllItems());
                }
            }

            // roll up the times and record
            allTime = 0;
            for (AggregationUpdater<? extends AdapterItem<?>, ? extends CoreItemAttributes, ?> aggUp : updaterMap
                    .values()) {
                allTime += aggUp.getUpdateTimingBag().getLatest();
            }
            updateCreationTimingBag.recordTime(allTime);

            // / let the subclass to specific processing
            postUpdateRelationshipsHook();

            // now wrapup the update
            for (String itemTypeId : getUpdateItemTypeIdList()) {
                AggregationUpdater<? extends AdapterItem<?>, ? extends CoreItemAttributes, ?> updater =
                        updaterMap.get(itemTypeId);
                if (updater != null) {
                    updater.wrapupUpdate(deltaEnable);
                }
            }

            // DEBUG DISPLAY OF INFO
            for (String itemTypeId : getUpdateItemTypeIdList()) {
                UpdateResult upRes = updateResultMap.get(itemTypeId);
                if (upRes != null && LOG.isDebugEnabled()) {
                    LOG.debug("itemTypeId: " + itemTypeId + " -> " + upRes.getSummary());
                }
            }

            // notification
            if (deltaEnable) {
                ArrayList<AggregationUpdate> aggUp = new ArrayList<>();
                for (String itemTypeId : getUpdateItemTypeIdList()) {
                    AdapterUpdateResult upRes = updateResultMap.get(itemTypeId);

                    if (upRes != null && upRes.hasAnyChange()) {
                        aggUp.add(new AggregationUpdate(getAggregation(itemTypeId), upRes));
                    }
                }
                if (!aggUp.isEmpty()) {
                    adapterManager.updateGroundedAggregations(session, aggUp);
                }
            } else {
                boolean anyChange = false;
                for (String itemTypeId : getUpdateItemTypeIdList()) {
                    AdapterUpdateResult upRes = updateResultMap.get(itemTypeId);
                    if (upRes != null && upRes.hasAnyChange()) {
                        anyChange = true;
                    }
                }
                if (anyChange) {
                    ArrayList<AggregationUpdate> aggUp = new ArrayList<>();
                    for (String itemTypeId : getUpdateItemTypeIdList()) {
                        aggUp.add(new AggregationUpdate(getAggregation(itemTypeId), updateResultMap.get(itemTypeId)));
                    }
                    adapterManager.updateGroundedAggregations(session, aggUp);
                }
            }

            if (runIdx % timeDisplayInterval == 1) {
                if (LOG.isTraceEnabled()) {
                    for (AggregationUpdater<? extends AdapterItem<?>, ? extends CoreItemAttributes, ?> aggUp : updaterMap
                            .values()) {
                        LOG.trace(aggUp.getCollectTimingBag().displayTimes());
                        LOG.trace(aggUp.getUpdateTimingBag().displayTimes());
                    }
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug(collectTimingBag.displayTimes());
                    LOG.debug(updateCreationTimingBag.displayTimes());
                }
            }

        } catch (NoSuchSessionException e) {
            LOG.error("AggregationManager did not recognise the session - abort data update", e);
        } catch (NoSuchAggregationException nsae) {
            LOG.error("AggregationManager did not recognise the aggregation - abort data update", nsae);
        } catch (NoSuchItemTypeException nsite) {
            LOG.error("AggregationManager did not recognise the aggregation - abort data update", nsite);
        } catch (NoSuchProviderException nspe) {
            LOG.warn("Could not find an aggregationUpdater for a given provider, possible cause: adapter is shutdown"
                    + "while thread is scheduled", nspe);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("exiting Collection task for session: " + session.getId() + " provider: "
                    + provider.getProviderId() + "runIdx: " + thisRunIdx);
        }
    }

    protected boolean tooManyDeltas() {
        int totalNew = 0;
        int totalUpdate = 0;
        int totalDelete = 0;
        for (AdapterUpdateResult upRes : updateResultMap.values()) {
            totalNew += upRes.getNewItems().size();
            totalUpdate += upRes.getUpdateEstimate();
            totalDelete += upRes.getDeletedItems().size();
        }
        LOG.debug("Delta count: " + totalNew + " / " + totalUpdate + " / " + totalDelete);
        return tooManyNew(totalNew) || tooManyUpdate(totalUpdate) || tooManyDeletion(totalDelete)
                || tooManyChanges(totalNew + totalUpdate + totalDelete);
    }

    protected boolean tooManyNew(final int newValue) {
        return newValue > DFLT_NEW_THRESHOLD;
    }

    protected boolean tooManyUpdate(final int updateValue) {
        return updateValue > DFLT_UPDATE_THRESHOLD;
    }

    protected boolean tooManyDeletion(final int deletionValue) {
        return deletionValue > DFLT_DELETION_THRESHOLD;
    }

    protected boolean tooManyChanges(final int changesValue) {
        return changesValue > DFLT_CHANGES_THRESHOLD;
    }

    private Item getReferencedItem(final String itemLogicalId) throws NoSuchProviderException {
        String itemTypeLocalId = adapter.getItemTypeLocalIdFromLogicalId(itemLogicalId);
        return updaterMap.get(itemTypeLocalId).getOldItem(itemLogicalId);
    }

    @Override
    public AdapterItem<?> getAdapterItem(final String itemTypeLocalId, final String itemLogicalId) {
        return updaterMap.get(itemTypeLocalId).getItemFromLogicalId(itemLogicalId);
    }

    @SuppressWarnings("checkstyle:linelength")
    @Override
    public AdapterItem<?> getNewAdapterItem(final String itemTypeLocalId, final String itemLogicalId) {
        return updaterMap.get(itemTypeLocalId.substring(itemTypeLocalId.indexOf("-") + 1, itemTypeLocalId.length()))
                .getNewAdapterItem(itemLogicalId);
    }

    @Override
    public String getLogicalId(final String itemTypeLocalId, final String resourceId) {
        return updaterMap.get(itemTypeLocalId).getLogicalId(resourceId);
    }

    @Override
    public void categorizeItemAsUpdated(final String itemTypeLocalId, final Item item) {
        updaterMap.get(itemTypeLocalId).categorizeItemAsUpdated(item);
    }

    @Override
    public ActionResult doAction(final Action action, final String itemTypeId) {
        try {
            return doAction(action, itemTypeId, null);
        } catch (InvalidActionSpecificationException iase) {
            LOG.error("could not perform action " + action, iase);
            return new ActionResult(ActionResult.Status.aborted);
        }
    }

    @Override
    public ActionResult doAction(final Action action, final Collection<Item> items) {
        String lid = items.iterator().next().getLogicalId();
        try {
            String itemTypeId = adapter.getItemTypeLocalIdFromLogicalId(lid);
            return doAction(action, itemTypeId, items);
        } catch (InvalidActionSpecificationException iase) {
            LOG.error("could not perform action " + action, iase);
            return new ActionResult(ActionResult.Status.aborted);
        } catch (NoSuchProviderException nspe) {
            LOG.error("could not perform action " + action + " itemType not recognised from logicalId: " + lid, nspe);
            return new ActionResult(ActionResult.Status.aborted);
        }
    }

    @Override
    public ActionResult doAction(final Action action, final List<String> itemLogicalIds) {
        try {
            String itemTypeId = adapter.getItemTypeLocalIdFromLogicalId(itemLogicalIds.get(0));

            // map item logicalIds to actual Items
            List<Item> items = new ArrayList<>(itemLogicalIds.size());
            for (String itlid : itemLogicalIds) {
                items.add(getReferencedItem(itlid));
            }
            return doAction(action, itemTypeId, items);
        } catch (InvalidActionSpecificationException iase) {
            LOG.error("could not perform action " + action, iase);
            return new ActionResult(ActionResult.Status.aborted);
        } catch (NoSuchProviderException nspe) {
            LOG.error("could not perform action " + action + " itemType not recognised from logicalId: "
                    + itemLogicalIds.get(0), nspe);
            return new ActionResult(ActionResult.Status.aborted);
        }
    }

    protected void validateAction(final String itemTypeId, final Action action)
            throws InvalidActionSpecificationException {
        if (itemTypeId == null) {
            throw new InvalidActionSpecificationException("null typeId for action " + action);
        }
        ItemType itemType = adapter.getItemType(itemTypeId);
        Map<String, Action> itemActions = itemType.getItemActions();
        Map<String, Action> aggregationActions = itemType.getAggregationActions();
        if ((itemActions == null || itemActions.isEmpty())
                && (aggregationActions == null || aggregationActions.isEmpty())) {
            throw new InvalidActionSpecificationException("No actions found for typeId " + itemTypeId);
        }
        Action allowedAction = itemActions.get(action.getId());
        if (allowedAction == null) {
            allowedAction = aggregationActions.get(action.getId());
            if (allowedAction == null) {
                throw new InvalidActionSpecificationException("action not allowed by itemType " + itemTypeId, action);
            }
        }
    }

    // hooks
    protected void preUpdateItemHook() {}

    protected void postUpdateItemHook() {}

    protected void postUpdateRelationshipsHook() {}


    // helper method
    protected boolean aggregationMatchesItemType(final Aggregation agg, final String itemTypeLocalId)
            throws NoSuchProviderException {
        String localTypeId = adapter.getItemTypeLocalIdFromLogicalId(agg.getLogicalId());
        return localTypeId.equals(itemTypeLocalId);
    }

    // abstract methods
    // factory method returning an AggregationUpdater per GroundedAggregation i.e. per ItemType
    protected abstract AggregationUpdater<? extends AdapterItem<?>, ? extends CoreItemAttributes, ?> getAggregationUpdater(
            Aggregation aggregation) throws NoSuchProviderException, NoSuchItemTypeException;

    // The returned list is used to call AggregationUpdaters updateRelationships() and
    // wrapupUpdate() as well to
    // create and update groundedAggregations
    protected abstract Collection<String> getUpdateItemTypeIdList();

    // The returned list is used to call AggregationUpdaters updateItem(), the stage where
    // attributes are collected
    // and compared and when if based relationships are set on AdapterItems.
    protected abstract Collection<String> getCollectionItemTypeIdList();

    protected abstract ActionResult doAction(Action action, String itemTypeId, Collection<Item> items)
            throws InvalidActionSpecificationException;

    /**
     * temporarily here for testing.
     *
     * @param itemTypeId item id
     * @return the collection of items.
     */
    public Collection<Item> getNewItems(final String itemTypeId) {
        return testCollectionMap.get(itemTypeId);
    }

    @Override
    public boolean isGlobal() {
        return false;
    }
}
