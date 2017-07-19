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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.manager.adapter.AdapterManager;
import com.hp.hpl.loom.manager.aggregation.AggregationManager;
import com.hp.hpl.loom.manager.stitcher.ItemEquivalence;
import com.hp.hpl.loom.manager.stitcher.Tacker;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.relationships.GraphProcessor;
import com.hp.hpl.loom.relationships.RelationshipCalculator;
import com.hp.hpl.loom.relationships.RelationshipsModelImpl;


public class OperationContextImpl implements OperationContext {
    private AdapterManager adapterManager;
    private AggregationManager aggregationManager;
    private RelationshipCalculator relationshipCalculator;
    private Tacker stitcher;
    private Session session;
    private ItemType type;

    public OperationContextImpl(final AdapterManager adapterManager, final AggregationManager aggregationManager,
            final RelationshipCalculator relationshipCalculator, final Tacker stitcher, final Session session,
            final ItemType type) {
        this.adapterManager = adapterManager;
        this.aggregationManager = aggregationManager;
        this.relationshipCalculator = relationshipCalculator;
        this.stitcher = stitcher;
        this.session = session;
        this.type = type;
    }

    @Override
    public ItemEquivalence getItemEquivalence(final Map<OperationErrorCode, String> errors) {
        ItemEquivalence itemEquivalence = null;
        try {
            itemEquivalence = stitcher.getItemEquivalence(session);
        } catch (NoSuchSessionException e) {
            errors.put(OperationErrorCode.NoSuchSession, e.toString());
        }
        return itemEquivalence;
    }

    @Override
    public RelationshipsModelImpl getRelationshipsModel(final Map<OperationErrorCode, String> errors) {
        RelationshipsModelImpl model = new RelationshipsModelImpl();
        try {
            List<Aggregation> gas = aggregationManager.listGroundedAggregations(session);
            model.calculateClassRelationships(gas);
        } catch (NoSuchSessionException e) {
            errors.put(OperationErrorCode.NoSuchSession, e.toString());
        }
        return model;
    }

    public Collection<Item> getItemsWithLogicalId(String logicalId, final Map<OperationErrorCode, String> errors) {
        Aggregation agg = null;
        try {
            agg = aggregationManager.getAggregation(session, logicalId);
        } catch (NoSuchSessionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (agg != null) {
            return agg.getContainedItems();
        } else {
            errors.put(OperationErrorCode.NoSuchFibre, "Could not find aggregation for id " + logicalId);
            return null;
        }
    }

    @Override
    public Item getItemWithLogicalId(final String logicalId, final Map<OperationErrorCode, String> errors) {
        Item resultingItem = null;
        try {

            Aggregation containing = adapterManager.getAggregationForItem(session, logicalId);
            if (containing == null) {
                errors.put(OperationErrorCode.NoSuchFibre, "Could not find aggregation for item " + logicalId);
                return null;
            }

            if (!containing.containsAggregations()) {
                for (Fibre item : containing.getElements()) {
                    if (item.getLogicalId().equals(logicalId)) {
                        resultingItem = (Item) item;
                        break;
                    }
                }
            }
        } catch (NoSuchSessionException e) {
            errors.put(OperationErrorCode.NoSuchSession, e.toString());
        }
        if (resultingItem == null) {
            errors.put(OperationErrorCode.NoSuchFibre, "Could not find item " + logicalId);
        }
        return resultingItem;
    }

    @Override
    public GraphProcessor getGraphProcessor() {
        return relationshipCalculator.getGraphProcessor();
    }

    @Override
    public ItemType getType() {
        return type;
    }

    @Override
    public Collection<Item> getEquivalentItems(Item item) throws NoSuchSessionException {
        ItemEquivalence itemEq = stitcher.getItemEquivalence(session);
        if (itemEq == null) {
            return new ArrayList<Item>(0);
        } else {
            return itemEq.getEquivalentItems(item);
        }
    }
}
