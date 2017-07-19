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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.hp.hpl.loom.adapter.AggregationUpdate;
import com.hp.hpl.loom.adapter.ItemAttributeDelta;
import com.hp.hpl.loom.adapter.ItemDeletionDelta;
import com.hp.hpl.loom.adapter.ItemRelationsDelta;
import com.hp.hpl.loom.adapter.RelationsDeltaType;
import com.hp.hpl.loom.adapter.UpdateDelta;
import com.hp.hpl.loom.adapter.UpdateResult;
import com.hp.hpl.loom.exceptions.LogicalIdAlreadyExistsException;
import com.hp.hpl.loom.exceptions.NoSuchAggregationException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.SessionAlreadyExistsException;
import com.hp.hpl.loom.manager.stitcher.StitcherUpdater;
import com.hp.hpl.loom.manager.stitcher.Tacker;
import com.hp.hpl.loom.manager.tapestry.TapestryManager;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.relationships.RelationshipUtil;


/**
 * Implementation of the Aggregation Manager.
 */
@Component
public class AggregationManagerImpl implements AggregationManager {
    private static final int YEAR_1700 = 1700;

    private static final int YEAR_4000 = 4000;

    private static final int DEC = 12;

    private static final Log LOG = LogFactory.getLog(AggregationManagerImpl.class);

    @Autowired
    private TapestryManager tapestryManager;

    @Autowired
    private Tacker stitcher;

    @PostConstruct
    public void initManager() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Staring Aggregation Manager");
        }
    }

    @Value("${max.ga.size}")
    private Integer maxGaSize = Integer.MAX_VALUE;

    @Value("${max.da.size}")
    private Integer maxDaSize = Integer.MAX_VALUE;

    private Map<String, LoomModel> sessionModels = new ConcurrentHashMap<String, LoomModel>();

    private LoomModel getLoomModel(final Session session, final boolean createOnNotExist)
            throws NoSuchSessionException {
        if (session == null) {
            throw new IllegalArgumentException("session must not be null");
        }

        String sessionId = session.getId();
        LoomModel model = sessionModels.get(sessionId);
        if (model == null) {
            if (createOnNotExist) {
                model = new LoomModel(sessionId);
                sessionModels.put(sessionId, model);
            } else {
                throw new NoSuchSessionException(session);
            }
        }

        if (!model.getSessionId().equals(sessionId)) {
            throw new NoSuchSessionException("Indexed model had invalid session Id " + model.getSessionId(), session);
        }
        return model;
    }

    private LoomModel getLoomModel(final Session session) throws NoSuchSessionException {
        return getLoomModel(session, false);
    }

    private AggregationMapper getAggregationMapper(final Session session) throws NoSuchSessionException {
        return getLoomModel(session).getAggregationMapper();
    }

    // ////////////////////////////////////////////////////////////////////////////////////////
    // Operations on Sessions
    // ////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void createSession(final Session session) throws NoSuchSessionException, SessionAlreadyExistsException {
        if (session == null) {
            throw new IllegalArgumentException("session must not be null");
        }

        LOG.info("Create session " + session);
        synchronized (session) {
            getLoomModel(session, true);
        }
    }

    private void deleteSession(final String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new IllegalArgumentException("session must not be null");
        }
        LoomModel model = sessionModels.get(sessionId);
        model.deleteAllAggregations();
        sessionModels.remove(sessionId);
    }

    @Override
    public void deleteSession(final Session session) throws NoSuchSessionException {
        if (session == null) {
            throw new NoSuchSessionException(session);
        }
        LOG.info("Delete session " + session);
        String sessionId = session.getId();
        if (!sessionModels.containsKey(sessionId)) {
            throw new NoSuchSessionException(session);
        }
        synchronized (session) {
            deleteSession(sessionId);
        }
    }

    @Override
    public void deleteAllSessions() {
        LOG.info("Delete all sessions");
        List<String> sessionIds = new ArrayList<String>(sessionModels.keySet());
        for (String sessionId : sessionIds) {
            deleteSession(sessionId);
        }
    }

    // ////////////////////////////////////////////////////////////////////////////////////////
    // Operations on Grounded Aggregations
    // ////////////////////////////////////////////////////////////////////////////////////////

    @Override
    @SuppressWarnings("checkstyle:parameternumber")
    public Aggregation createGroundedAggregation(final Session session, final Provider provider,
            final ItemType itemType, final String logicalId, final String mergedLogicalId, final String name,
            final String description, final int expectedSize)
            throws NoSuchSessionException, LogicalIdAlreadyExistsException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("CreateGroundedAggregation session=" + session + " Provider=" + provider + " ItemType=" + itemType
                    + " logicalId=" + logicalId + " mergedLogicalId=" + mergedLogicalId + " name=" + name
                    + " description=" + description + " expectedSize=" + expectedSize);
        }
        validateSize(expectedSize, maxGaSize);

        LoomModel model = getLoomModel(session);

        synchronized (session) {
            if (model.aggregationExists(logicalId)) {
                throw new LogicalIdAlreadyExistsException(logicalId);
            }
            String typeId = itemType.getId();
            Aggregation aggregation =
                    new Aggregation(logicalId, typeId, name, description, expectedSize, mergedLogicalId);
            aggregation.setGrounded(true);
            aggregation.setFibreCreated(new Date());
            model.putGroundedAggregation(logicalId, aggregation, provider.getProviderId());

            if (aggregation.getMergedLogicalId() != null) {
                model.getAggregationMapper().mapGroundedAggregation(logicalId, aggregation.getMergedLogicalId());
            }
            aggregation.setPending(true);
            if (aggregation.getMergedLogicalId() != null) {
                propagatePendingToGAsInMap(session, aggregation.getMergedLogicalId(), true);
            }
            return aggregation;
        }
    }

    private class UpdateState {
        private Aggregation groundedAggregation;
        private UpdateResult updateResult;
        private Map<String, Item> postProcessedItems = new HashMap<>();
        private Map<String, Item> updatedByDeletionItems = new HashMap<>();
        // For each GA, keep a record of all Items that were updated as a result of Tacker.
        private Map<String, Item> updatedByStitching = new HashMap<>();

        UpdateState(final Aggregation groundedAggregation) {
            this.groundedAggregation = groundedAggregation;
        }

        UpdateState(final Aggregation groundedAggregation, final UpdateResult updateResult) {
            this.groundedAggregation = groundedAggregation;
            this.updateResult = updateResult;
        }

        String getLogicalId() {
            return groundedAggregation.getLogicalId();
        }

        Aggregation getGroundedAggregation() {
            return groundedAggregation;
        }

        UpdateResult getUpdateResult() {
            return updateResult;
        }

        Map<String, Item> getPostProcessedItems() {
            return postProcessedItems;
        }

        Map<String, Item> getUpdatedByDeletionItems() {
            return updatedByDeletionItems;
        }

        Map<String, Item> getUpdatedByStitching() {
            return updatedByStitching;
        }

        void postProcessed(final Item item) {
            postProcessedItems.put(item.getLogicalId(), item);
        }

        public void updatedByDeletion(final Item item) {
            updatedByDeletionItems.put(item.getLogicalId(), item);
        }

        public void updatedByStitching(final Item item) {
            updatedByStitching.put(item.getLogicalId(), item);
        }
    }

    private class UpdateContext {
        private LoomModel model;
        private StitcherUpdater stitcherUpdater;
        // True if any GA has a delta
        private boolean delta = false;
        // Map of LogicalID of GA to UpdateState for that GA
        private Map<String, UpdateState> updateStates = new ConcurrentHashMap<>();
        private Set<String> processedLogicalIds = new HashSet<>();

        private void addUpdateState(final UpdateState updateState) {
            updateStates.put(updateState.getLogicalId(), updateState);
        }

        UpdateContext(final StitcherUpdater stitcherUpdater, final LoomModel model) {
            this.stitcherUpdater = stitcherUpdater;
            this.model = model;
        }

        public LoomModel getModel() {
            return model;
        }

        public StitcherUpdater getStitcherUpdater() {
            return stitcherUpdater;
        }

        UpdateState getUpdateState(final Aggregation aggregation) {
            return updateStates.get(aggregation.getLogicalId());
        }

        UpdateState createUpdateState(final Aggregation aggregation, final UpdateResult updateResult) {
            UpdateState updateState = new UpdateState(aggregation, updateResult);
            addUpdateState(updateState);
            return updateState;
        }

        UpdateState getOrCreateUpdateState(final Item item) {
            Aggregation aggregation = item.getGroundedAggregation();
            UpdateState updateState = getUpdateState(aggregation);
            if (updateState == null) {
                updateState = new UpdateState(aggregation);
                addUpdateState(updateState);
            }
            return updateState;
        }

        Collection<UpdateState> getUpdateStates() {
            return updateStates.values();
        }


        public boolean isDelta() {
            return delta;
        }

        public void setDelta(final boolean delta) {
            this.delta = delta;
        }

        boolean isProcessed(final Item item) {
            return processedLogicalIds.contains(item.getLogicalId());
        }

        void processed(final Item item) {
            processedLogicalIds.add(item.getLogicalId());
        }
    }

    @Override
    public void updateGroundedAggregation(final Session session, final Aggregation aggregation,
            final UpdateResult updateResult) throws NoSuchSessionException, NoSuchAggregationException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("UpdateGroundedAggregation session=" + session + " Aggregation=" + aggregation.getLogicalId()
                    + " allItems=" + (updateResult.getAllItems() == null ? 0 : updateResult.getAllItems().size())
                    + " newItems=" + (updateResult.getNewItems() == null ? 0 : updateResult.getNewItems().size())
                    + " deletedItems="
                    + (updateResult.getDeletedItems() == null ? 0 : updateResult.getDeletedItems().size())
                    + " numUpdatedItems="
                    + (updateResult.getUpdatedItems() == null ? 0 : updateResult.getUpdatedItems().size()));
        }


        synchronized (session) {
            LoomModel model = getLoomModel(session);
            StitcherUpdater stitcherUpdater = null;
            if (stitcher != null) {
                stitcherUpdater = stitcher.getStitcherUpdater(session);
            }
            UpdateContext updateContext = new UpdateContext(stitcherUpdater, model);
            UpdateState updateState = updateContext.createUpdateState(aggregation, updateResult);
            if (!model.groundedAggregationExists(aggregation.getLogicalId())) {
                throw new NoSuchAggregationException(aggregation.getLogicalId());
            }

            processDeltaGroundedAggregation(session, updateContext, updateState);
            postProcessAllGroundedAggregations(session, model, updateContext);
        }
    }

    // Suppressing the warning for the session being unused as we will make use of it later.
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void processDeltaGroundedAggregation(final Session session, final UpdateContext updateContext,
            final UpdateState updateState) {

        Aggregation aggregation = updateState.getGroundedAggregation();
        if (aggregation == null) {
            throw new IllegalArgumentException("Aggregation is null");
        }
        if (!aggregation.isGrounded()) {
            throw new IllegalArgumentException("Aggregation is not Grounded " + aggregation.getLogicalId());
        }

        UpdateResult updateResult = updateState.getUpdateResult();
        if (updateResult == null) {
            throw new IllegalArgumentException("null updateResult for aggregation " + aggregation.getLogicalId());
        }

        // Set the aggregation of all new Items
        if (updateResult.getNewItems() != null) {
            for (Item item : updateResult.getNewItems()) {
                item.setGroundedAggregation(aggregation);
            }
        }

        if (!updateResult.isDeltaMode()) {
            return;
        } else {
            updateContext.setDelta(true);
        }

        UpdateDelta delta = updateResult.getUpdateDelta();
        if (delta == null) {
            return;
        }

        // Deletions
        if (delta.getDeletionDelta() != null) {
            for (ItemDeletionDelta deletionDelta : delta.getDeletionDelta()) {
                Item deleted = deletionDelta.getItem();
                // Keep record of all Items that this deleted object was connected to, so can
                // post-process and add to updated list.
                for (Item item : deleted.getAllConnectedItems()) {
                    updatedByDeletionOfRelatedItem(item, updateContext);
                }

                deleted.removeAllConnectedRelationships();
            }
        }

        // Relationships
        if (delta.getRelationsDelta() != null) {
            for (ItemRelationsDelta relationsDelta : delta.getRelationsDelta()) {
                RelationsDeltaType deltaType = relationsDelta.getType();
                Item item = relationsDelta.getItem();
                Item other = relationsDelta.getOther();
                Collection<Item> others = relationsDelta.getOthers();
                String relationsName = relationsDelta.getRelationsName();
                switch (deltaType) {
                    case Add:
                        if (other != null) {
                            addConnectedRelationshipsOnItem(item, other, relationsName);
                        }
                        if (others != null) {
                            for (Item otherItem : others) {
                                addConnectedRelationshipsOnItem(item, otherItem, relationsName);
                            }
                        }
                        break;
                    case Remove:
                        if (other != null) {
                            removeConnectedRelationshipsOnItem(item, other, relationsName);
                        }
                        if (others != null) {
                            for (Item otherItem : others) {
                                removeConnectedRelationshipsOnItem(item, otherItem, relationsName);
                            }
                        }
                        break;
                    case Clear:
                        if (relationsName == null) {
                            item.removeAllConnectedRelationships();
                        } else {
                            item.removeAllConnectedRelationshipsWithRelationshipName(relationsName);
                        }
                        break;
                    default:
                        break;
                }

            }
        }

        // Attributes
        if (delta.getAttributeDelta() != null) {
            for (ItemAttributeDelta<? extends CoreItemAttributes> attributeDelta : delta.getAttributeDelta()) {
                attributeDelta.apply();
            }
        }
    }

    private void postProcessItemsInAllGroundedAggregations(final UpdateContext updateContext) {
        if (!updateContext.isDelta()) {
            // No deltas were included, so no need to post process
            return;
        }

        for (UpdateState updateState : updateContext.getUpdateStates()) {
            UpdateResult updateResult = updateState.getUpdateResult();
            Map<String, Item> updatedByDeletionItems = updateState.getUpdatedByDeletionItems();
            if (updateResult != null) {
                if (updateResult.isDeltaMode() && updateResult.getNewItems() != null) {
                    for (Item item : updateResult.getNewItems()) {
                        postProcessItem(item, updateContext, updateState);
                    }
                }
                if (updateResult.isDeltaMode() && updateResult.getUpdatedItems() != null) {
                    for (Item item : updateResult.getUpdatedItems()) {
                        postProcessItem(item, updateContext, updateState);
                    }
                }

                // Work out the set of Items have been updated as a result of deletion of a related
                // Item, taking into account the actual deletions themselves.
                if (updateResult.getDeletedItems() != null) {
                    for (Item item : updateResult.getDeletedItems()) {
                        updatedByDeletionItems.remove(item.getLogicalId());
                    }
                }
            }

            // Post-process filtered list of Items affected by deletion.
            for (Item item : updatedByDeletionItems.values()) {
                postProcessItem(item, updateContext, updateState);
            }
        }
    }

    // Perform stitching of Items affected by update
    private void stitchItemsInAllGroundedAggregations(final UpdateContext updateContext, final Session session)
            throws NoSuchSessionException {
        StitcherUpdater stitcherUpdater = updateContext.getStitcherUpdater();
        if (stitcherUpdater == null) {
            return;
        }
        LoomModel model = updateContext.getModel();
        for (UpdateState updateState : new ArrayList<>(updateContext.getUpdateStates())) {
            UpdateResult updateResult = updateState.getUpdateResult();
            if (updateResult != null) {
                // We pass stitcher those items affected directly by an update from Adapter, not
                // those indirectly updated by deletion of a connected Item.
                Aggregation ga = updateState.getGroundedAggregation();
                String providerId = model.getGroundedAggregationProviderId(ga.getLogicalId());
                Collection<Item> itemsUpdatedByStitching = stitcherUpdater.stitchItems(ga.getTypeId(), providerId,
                        updateResult.getAllItems(), updateResult.getNewItems(), updateResult.getUpdatedItems(),
                        updateResult.getDeletedItems());
                for (Item equivalentItem : itemsUpdatedByStitching) {
                    UpdateState connectedUpdateState = updateContext.getOrCreateUpdateState(equivalentItem);
                    // TODO Do we need to index the stitched items themselves, or simply note that
                    // GA is affected
                    connectedUpdateState.updatedByStitching(equivalentItem);
                }
            }
        }
    }


    // Post-processing of Items can only occur after processing of deltas, because can only call
    // postProcessItem when all deltas, across all GAs, have been applied to create a new view of
    // the world.
    private void postProcessAllGroundedAggregations(final Session session, final LoomModel model,
            final UpdateContext updateContext) throws NoSuchSessionException {
        postProcessItemsInAllGroundedAggregations(updateContext);
        stitchItemsInAllGroundedAggregations(updateContext, session);
        updateGroundedAggregationsCountsAndDirty(session, model, updateContext);
    }

    // Update added, updated, and deleted counts and mark affected GAs as dirty
    private void updateGroundedAggregationsCountsAndDirty(final Session session, final LoomModel model,
            final UpdateContext updateContext) throws NoSuchSessionException {
        Date now = new Date();
        for (UpdateState updateState : updateContext.getUpdateStates()) {
            // Compute combined view of objects that have been updated ....
            // ... Start with Items affected by deletion of one of it's previously connected Items.
            Map<String, Item> updatedItemsMap = updateState.getUpdatedByDeletionItems();
            // ... Add effects of post-processing updates
            for (Item item : updateState.getPostProcessedItems().values()) {
                updatedItemsMap.put(item.getLogicalId(), item);
            }
            // ... Finally add updates explicitly listed by adapter
            UpdateResult updateResult = updateState.getUpdateResult();
            if (updateResult != null && updateResult.getUpdatedItems() != null) {
                for (Item item : updateResult.getUpdatedItems()) {
                    updatedItemsMap.put(item.getLogicalId(), item);
                }
            }

            if (updateResult != null && !updateResult.isIgnore() || !updatedItemsMap.isEmpty()
                    || !updateState.getUpdatedByStitching().isEmpty()) {
                // if (updateResult != null || !updatedItemsMap.isEmpty()) {
                // if ((updateResult != null && !updateResult.isIgnore()) ||
                // !updatedItemsMap.isEmpty()) {
                // If have been explicitly told by adapter that GA has changed, or if discovered
                // from post processing, or have been updated as a result of stitching.
                Aggregation aggregation = updateState.getGroundedAggregation();
                List<Item> newItems = null;
                List<Item> deletedItems = null;
                if (updateResult != null) {
                    // Replace the set of elements, and update counts
                    aggregation.setElements(new ArrayList<Fibre>(updateResult.getAllItems()));
                    newItems = updateResult.getNewItems();
                    aggregation.setCreatedCount(newItems == null ? 0 : newItems.size());

                    deletedItems = updateResult.getDeletedItems();
                    aggregation.setDeletedCount(deletedItems == null ? 0 : deletedItems.size());
                }

                // Updated items map accounts for delta processing for attribute deltas and
                // deletions, in addition to items that the adapter explicitly told us was updated.
                // Must remove new items from map before counting updated items, so can do strict
                // categorisation of new and updated.
                if (newItems != null) {
                    for (Item item : newItems) {
                        updatedItemsMap.remove(item.getLogicalId());
                    }
                }
                // Update times for updated items, if in delta mode.
                if (updateContext.isDelta()) {
                    for (Item item : updatedItemsMap.values()) {
                        item.setFibreUpdated(now);
                    }
                }
                aggregation.setUpdatedCount(updatedItemsMap.size());

                // Mark aggregation as dirty
                aggregation.setDirty(true);
                aggregation.setPending(false);
                aggregation.setFibreUpdated(now);
                if (aggregation.getMergedLogicalId() != null) {
                    propagateDirtyToGAsInMap(session, aggregation.getMergedLogicalId(), true);
                    model.getAggregationMapper().setDirty(aggregation.getMergedLogicalId(), true);
                    propagatePendingToGAsInMap(session, aggregation.getMergedLogicalId(), false);
                }
            }
        }
    }

    /*
     * Post-process an Item after the delta phase of an update to a GA has been completed. In
     * addition, post-process any connected items.
     */
    private void postProcessItem(final Item item, final UpdateContext updateContext, final UpdateState updateState) {
        if (!updateContext.isProcessed(item)) {
            if (item.update()) {
                updateState.postProcessed(item);
            }
            updateContext.processed(item);
            for (Item connectedItem : item.getAllConnectedItems()) {
                if (!updateContext.isProcessed(connectedItem)) {
                    if (connectedItem.update()) {
                        UpdateState connectedUpdateState = updateContext.getOrCreateUpdateState(connectedItem);
                        connectedUpdateState.postProcessed(connectedItem);
                    }
                    updateContext.processed(connectedItem);
                }
            }
        }
    }

    private void updatedByDeletionOfRelatedItem(final Item item, final UpdateContext updateContext) {
        UpdateState updateState = updateContext.getOrCreateUpdateState(item);
        updateState.updatedByDeletion(item);
    }

    @Override
    public void updateGroundedAggregations(final Session session,
            final Collection<AggregationUpdate> aggregationUpdates)
            throws NoSuchSessionException, NoSuchAggregationException {

        synchronized (session) {

            if (LOG.isDebugEnabled()) {
                LOG.debug("updateGroundedAggregations start");
            }

            LoomModel model = getLoomModel(session);
            UpdateContext updateContext = new UpdateContext(stitcher.getStitcherUpdater(session), model);

            for (AggregationUpdate aggregationUpdate : aggregationUpdates) {
                Aggregation aggregation = aggregationUpdate.getAggregation();
                UpdateResult updateResult = aggregationUpdate.getUpdateResult();

                if (!model.groundedAggregationExists(aggregation.getLogicalId())) {
                    throw new NoSuchAggregationException(aggregation.getLogicalId());
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("UpdateGroundedAggregations session=" + session + " Aggregation="
                            + aggregation.getLogicalId() + " allItems="
                            + (updateResult.getAllItems() == null ? 0 : updateResult.getAllItems().size())
                            + " newItems="
                            + (updateResult.getNewItems() == null ? 0 : updateResult.getNewItems().size())
                            + " deletedItems="
                            + (updateResult.getDeletedItems() == null ? 0 : updateResult.getDeletedItems().size())
                            + " numUpdatedItems="
                            + (updateResult.getUpdatedItems() == null ? 0 : updateResult.getUpdatedItems().size()));
                }

                UpdateState updateState = updateContext.createUpdateState(aggregation, updateResult);
                processDeltaGroundedAggregation(session, updateContext, updateState);

            }

            postProcessAllGroundedAggregations(session, model, updateContext);
        }
    }

    @Override
    public List<Aggregation> listGroundedAggregations(final Session session) throws NoSuchSessionException {
        LoomModel model = getLoomModel(session);
        List<Aggregation> gaList = new ArrayList<Aggregation>(model.getGroundedAggregations().values());
        return gaList;
    }

    @Override
    public boolean groundedAggregationExists(final Session session, final String logicalId)
            throws NoSuchSessionException {
        LoomModel model = getLoomModel(session);
        return model.groundedAggregationExists(logicalId);
    }

    // ////////////////////////////////////////////////////////////////////////////////////////
    // Operations on Derived Aggregations
    // ////////////////////////////////////////////////////////////////////////////////////////
    @Override
    @SuppressWarnings("checkstyle:parameternumber")
    public Aggregation createDerivedAggregation(final Session session, final String typeId, final String logicalId,
            final Fibre.Type contains, final String name, final String description,
            final String[] dependsOnLogicalIdArray, final int expectedSize)
            throws NoSuchSessionException, LogicalIdAlreadyExistsException, NoSuchAggregationException {

        LoomModel model = getLoomModel(session);
        String[] mappedSet = flattenMappedInputs(model.getAggregationMapper(), dependsOnLogicalIdArray);

        if (model.aggregationExists(logicalId)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("DA already exists " + logicalId);
            }
            throw new LogicalIdAlreadyExistsException(logicalId);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Create Empty DA: session=" + session + " contains=" + contains + " typeId=" + typeId
                    + " logicalId=" + logicalId + " dependsOnLogicalIdArray=" + mappedSet + " name=" + name
                    + " description=" + description + " expectedSize=" + expectedSize);
        }
        validateSize(expectedSize, maxDaSize);

        Aggregation aggregation = new Aggregation(logicalId, contains, typeId, name, description, expectedSize);
        return setupNewDerivedAggregation(model, aggregation, mappedSet);
    }

    private String[] flattenMappedInputs(final AggregationMapper aggregationMapper,
            final String[] dependsOnLogicalIdArray) {
        List<String> mappedList = new ArrayList<String>();
        if (dependsOnLogicalIdArray == null || dependsOnLogicalIdArray.length == 0) {
            return new String[0];
        }

        for (String element : dependsOnLogicalIdArray) {
            Set<String> map = aggregationMapper.getMap(element);
            if (map != null && map.size() != 0) {
                Set<String> mapped = aggregationMapper.getMap(element);
                for (String entry : mapped) {
                    mappedList.add(entry);
                }
            } else {
                mappedList.add(element);
            }
        }
        return mappedList.toArray(new String[mappedList.size()]);
    }

    private void validateSize(final int size, final int maxSize) {
        if (size > maxSize) {
            throw new IllegalArgumentException(
                    "Aggregation size can't above configured size: " + size + " vs " + maxSize);
        } else if (size < 0) {
            throw new IllegalArgumentException("Aggregation size can't be negative");
        }
    }

    private Aggregation setupNewDerivedAggregation(final LoomModel model, final Aggregation aggregation,
            final String[] dependsOnLogicalIdArray) throws NoSuchAggregationException {
        aggregation.setGrounded(false);
        aggregation.setIndexed(true);
        aggregation.setFibreCreated(new Date());

        // Establish the dependencies
        if (dependsOnLogicalIdArray != null) {
            boolean anyPending = false;
            for (String dependsOnLogicalId : dependsOnLogicalIdArray) {
                Aggregation dependsOnAggregation = model.getAggregation(dependsOnLogicalId);
                if (dependsOnAggregation == null || dependsOnAggregation.isDeletePerformed()) {
                    throw new NoSuchAggregationException(dependsOnLogicalId);
                }
                anyPending = anyPending || dependsOnAggregation.isPending();

                aggregation.addDependsOn(dependsOnAggregation);
            }
            aggregation.setPending(anyPending);
        }
        model.putDerivedAggregation(aggregation.getLogicalId(), aggregation);

        return aggregation;
    }

    @Override
    public void updateDerivedAggregation(final Session session, final Aggregation aggregation,
            final List<Fibre> newElements) throws NoSuchSessionException, NoSuchAggregationException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateDerivedAggregation session=" + session + " Aggregation=" + aggregation.getLogicalId()
                    + " numItems=" + newElements.size());
        }
        if (aggregation.isGrounded()) {
            throw new IllegalArgumentException("Aggregation is not Derived " + aggregation.getLogicalId());
        }
        LoomModel model = getLoomModel(session);
        if (!model.aggregationExists(aggregation.getLogicalId())) {
            throw new NoSuchAggregationException(aggregation.getLogicalId());
        }
        // Just swap the set of elements
        aggregation.setElements(newElements);

        // Assume the Aggregation is now different, otherwise why update it
        aggregation.setDirty(true);
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateDerivedAggregation end session=" + session + " Aggregation=" + aggregation.getLogicalId());
        }
    }

    @Override
    public void deleteAllDerivedAggregations(final Session session) throws NoSuchSessionException {
        LOG.info("Delete All Derived Aggregations session=" + session);
        getLoomModel(session);
        for (Aggregation aggregation : listTopLevelDerivedAggregations(session)) {
            try {
                deleteAggregationAndChildren(session, aggregation.getLogicalId(), false);
            } catch (NoSuchAggregationException e) {
                LOG.error("Received a NoSuchAggregationException when tried to delete all DAs", e);
            }
        }
    }

    @Override
    public List<Aggregation> listDerivedAggregations(final Session session) throws NoSuchSessionException {
        LoomModel model = getLoomModel(session);
        List<Aggregation> daList = new ArrayList<Aggregation>(model.getDerivedAggregations().values());
        return daList;
    }

    @Override
    public List<Aggregation> listTopLevelDerivedAggregations(final Session session) throws NoSuchSessionException {
        LoomModel model = getLoomModel(session);
        List<Aggregation> daList = new ArrayList<Aggregation>(model.getDerivedAggregations().size());
        for (Aggregation aggregation : model.getDerivedAggregations().values()) {
            if (aggregation.isTopLevel()) {
                daList.add(aggregation);
            }
        }
        return daList;
    }

    @Override
    public boolean derivedAggregationExists(final Session session, final String logicalId)
            throws NoSuchSessionException {
        LoomModel model = getLoomModel(session);
        return model.derivedAggregationExists(logicalId);
    }

    // ////////////////////////////////////////////////////////////////////////////////////////
    // Operations on Aggregations
    // ////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void deleteAggregation(final Session session, final String logicalId) throws NoSuchSessionException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Delete Aggregation session=" + session + " logicalId=" + logicalId);
        }
        LoomModel model = getLoomModel(session);

        synchronized (session) {
            Aggregation aggregation = model.getAggregation(logicalId);
            if (aggregation != null) {
                boolean deleted = aggregation.delete();
                if (deleted) {
                    model.removeAggregation(logicalId);
                }
            }
        }
    }

    private void doDeleteAggregationAndChildren(final Session session, final String logicalId,
            final boolean dynAdapterDeregistration) throws NoSuchSessionException, NoSuchAggregationException {
        LoomModel model = getLoomModel(session);
        Aggregation aggregation = model.getAggregation(logicalId);
        if (aggregation != null) {
            // For Grounded Aggregations, recursively delete any top-level aggregations that depend
            // on it and update maps
            if (aggregation.isGrounded()) {
                List<Aggregation> topLevelAggregations = aggregation.getTopLevelDependsOnMeAggregations();
                for (Aggregation topLevelAggregation : topLevelAggregations) {
                    deleteAggregationAndChildren(session, topLevelAggregation.getLogicalId(), dynAdapterDeregistration);
                }

                if (dynAdapterDeregistration
                        && model.getAggregationMapper().getMap(aggregation.getMergedLogicalId()).size() == 1) {
                    tapestryManager.markAsGone(session, aggregation.getMergedLogicalId());
                }
                model.getAggregationMapper().deleteFromMaps(aggregation);
                propagateDirtyToGAsInMap(session, aggregation.getMergedLogicalId(), true);

                // Inform the stitcher that the collection of items has gone.
                String providerId = model.getGroundedAggregationProviderId(aggregation.getLogicalId());
                StitcherUpdater stitcherUpdater = stitcher.getStitcherUpdater(session);
                if (stitcherUpdater != null) {
                    stitcherUpdater.removeStitchedItems(aggregation.getTypeId(), providerId);
                }
            }

            boolean deleted = aggregation.delete();
            if (deleted) {
                // Delete child aggregations
                if (aggregation.containsAggregations()) {
                    List<Fibre> elements = aggregation.getElements();
                    if (elements != null) {
                        for (Fibre element : elements) {
                            if (element.isAggregation()) {
                                Aggregation child = (Aggregation) element;
                                deleteAggregationAndChildren(session, child.getLogicalId(), dynAdapterDeregistration);
                            }
                        }
                    }
                }
                model.removeAggregation(logicalId);
                // mark DA as deleted
                if (dynAdapterDeregistration && !aggregation.isGrounded()) {
                    tapestryManager.markAsGone(session, logicalId);
                }
            }
        }
    }

    @Override
    public void deleteAggregationAndChildren(final Session session, final String logicalId,
            final boolean dynAdapterDeregistration) throws NoSuchSessionException, NoSuchAggregationException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Delete Aggregation and children session=" + session + " logicalId=" + logicalId);
        }

        doDeleteAggregationAndChildren(session, logicalId, dynAdapterDeregistration);
    }

    @Override
    public void deleteGroundedAggregation(final Session session, final String logicalId)
            throws NoSuchSessionException, NoSuchAggregationException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Delete GA session=" + session + " logicalId=" + logicalId);
        }
        LoomModel model = getLoomModel(session);
        if (!model.groundedAggregationExists(logicalId)) {
            throw new NoSuchAggregationException("Grounded Aggregation does not exist: " + logicalId);
        }

        synchronized (session) {
            doDeleteAggregationAndChildren(session, logicalId, false);
        }
    }

    private void propagateDirtyToGAsInMap(final Session session, final String mergedLogicalId, final boolean dirty)
            throws NoSuchSessionException {
        for (String logicalID : getAggregationMapper(session).getMap(mergedLogicalId)) {
            Aggregation agg = getAggregation(session, logicalID);
            if (agg != null) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Setting " + logicalID + " and its DAs to DIRTY.");
                }
                agg.setDirty(dirty);
            }
        }
    }

    private void propagatePendingToGAsInMap(final Session session, final String mergedLogicalId, final boolean pending)
            throws NoSuchSessionException {
        for (String logicalID : getAggregationMapper(session).getMap(mergedLogicalId)) {
            Aggregation agg = getAggregation(session, logicalID);
            if (agg != null) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Setting " + logicalID + " and its DAs to pending:" + pending);
                }
                agg.setPending(pending);
            }
        }
    }

    @Override
    public Aggregation getAggregation(final Session session, final String logicalId) throws NoSuchSessionException {
        String sessionId = session.getId();

        LoomModel model = getLoomModel(session);
        Aggregation aggregation = null;

        // Check it is a mapped aggregation
        Set<String> mapped = model.getAggregationMapper().getMap(logicalId);
        if (mapped != null && mapped.size() > 1) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Dealing with a mapped aggregation: " + mapped + " session: " + sessionId);
            }
            boolean combinationDirty = false;
            boolean combinationPending = false;
            String typeId = "";
            ArrayList<Fibre> concatenated = null;

            Date earliest = new Date(YEAR_4000, 1, DEC);
            Date oldest = new Date(YEAR_1700, 1, 1);
            Date created = null;
            Date updated = null;
            int createdCount = 0;
            int updatedCount = 0;
            int deletedCount = 0;
            for (String concatenatedId : mapped) {
                aggregation = model.getAggregation(concatenatedId);
                if (aggregation == null) {
                    // Avoid the null pointer, but this should never happen
                    LOG.fatal("Could not find aggregation " + concatenatedId + " mapped: " + logicalId + " session: "
                            + sessionId);
                    continue;
                }
                createdCount += aggregation.getCreatedCount();
                updatedCount += aggregation.getUpdatedCount();
                deletedCount += aggregation.getDeletedCount();

                Date entryCreated = aggregation.getFibreCreated();
                Date entryUpdated = aggregation.getFibreUpdated();

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Concatenating " + concatenatedId + " with size " + aggregation.getSize());
                }
                combinationDirty = combinationDirty || aggregation.isDirty();
                combinationPending = combinationPending || aggregation.isPending();

                if (entryCreated != null && entryCreated.before(earliest)) {
                    created = aggregation.getFibreCreated();
                    earliest = created;
                }

                if (entryUpdated != null && entryUpdated.after(oldest)) {
                    updated = aggregation.getFibreUpdated();
                    oldest = updated;
                }

                if (concatenated == null) {
                    // Rough estimation of size
                    concatenated = new ArrayList<>(aggregation.getSize() * mapped.size());
                }

                concatenated.addAll(aggregation.getElements());
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Concatenated size: " + concatenated.size());
                }
                typeId = aggregation.getTypeId(); // All of them are the same type for now
            }
            aggregation = new Aggregation(logicalId, typeId, "mapped_" + typeId, "mapped aggregation of type" + typeId,
                    concatenated.size());
            aggregation.setElements(concatenated);
            aggregation.setFibreCreated(created);
            aggregation.setFibreUpdated(updated);

            // Combine counts of mapped aggregations
            aggregation.setCreatedCount(createdCount);
            aggregation.setUpdatedCount(updatedCount);
            aggregation.setDeletedCount(deletedCount);

            aggregation.setPending(combinationPending);

            model.getAggregationMapper().setDirty(logicalId, combinationDirty);
        } else {
            String mappedId;
            if (mapped == null || mapped.size() == 0) {
                mappedId = logicalId;
            } else {
                // Only one entry, no need to map, just get it
                mappedId = mapped.iterator().next();
            }

            if (LOG.isTraceEnabled()) {
                LOG.trace("Dealing with a NOT mapped aggregation: " + mappedId);
            }
            aggregation = model.getAggregation(mappedId);
        }
        if (LOG.isDebugEnabled() && aggregation != null && aggregation.isGrounded()) {
            LOG.debug(
                    "Aggregation " + aggregation.getLogicalId() + " num elements=" + aggregation.getElements().size());
        }
        return aggregation;
    }

    @Override
    public List<Aggregation> listAggregations(final Session session) throws NoSuchSessionException {
        LoomModel model = getLoomModel(session);
        List<Aggregation> aList = new ArrayList<Aggregation>(model.getAggregations().values());
        return aList;
    }

    @Override
    public boolean isUpToDate(final Session session, final String logicalId) throws NoSuchSessionException {
        LoomModel model = getLoomModel(session);
        Aggregation aggregation = model.getAggregation(logicalId);
        return !aggregation.isDirty();
    }

    // /////////////////////
    // Delta operations
    // /////////////////////

    private void addConnectedRelationshipsOnItem(final Item item, final Item other, final String relationsName) {
        if (relationsName == null) {
            String relationName = RelationshipUtil.getRelationshipNameBetweenTypesWithRelType(item.getItemType(),
                    other.getItemType(), "");
            item.addConnectedRelationshipsWithName(relationName, other);
        } else {
            item.addConnectedRelationshipsWithName(relationsName, other);
        }
    }

    private void removeConnectedRelationshipsOnItem(final Item item, final Item other, final String relationsName) {
        if (relationsName == null) {
            String relationName = RelationshipUtil.getRelationshipNameBetweenTypesWithRelType(item.getItemType(),
                    other.getItemType(), "");
            item.removeConnectedRelationshipsWithNameAndType(relationName, other);
        } else {
            item.removeConnectedRelationshipsWithNameAndType(relationsName, other);
        }
    }


}
