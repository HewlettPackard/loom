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
package com.hp.hpl.loom.adapter.cubesensors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hp.hpl.loom.adapter.AggregationUpdater;
import com.hp.hpl.loom.adapter.AggregationUpdaterBasedItemCollector;
import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.exceptions.InvalidActionSpecificationException;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.manager.adapter.AdapterManager;
import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionResult;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.Session;

/**
 * Providers a updater for the CubeSensorsAdapter.
 *
 */
public class DeviceCollector extends AggregationUpdaterBasedItemCollector {
    /**
     * Constructor it takes a client session, adapter and adapter Manager to register back with.
     *
     * @param session - Client session
     * @param adapter - base adapter (the file adapter)
     * @param adapterManager adapterManager to register ourselves with
     */
    public DeviceCollector(final Session session, final BaseAdapter adapter, final AdapterManager adapterManager) {
        super(session, adapter, adapterManager);
    }

    @Override
    protected AggregationUpdater<?, ?, ?> getAggregationUpdater(final Aggregation aggregation)
            throws NoSuchProviderException, NoSuchItemTypeException {

        if (aggregationMatchesItemType(aggregation, Types.DEVICE_TYPE_LOCAL_ID)) {
            return new DeviceUpdater(aggregation, adapter, this);
        }

        throw new NoSuchItemTypeException(aggregation.getTypeId());
    }

    @Override
    protected Collection<String> getUpdateItemTypeIdList() {
        List<String> list = new ArrayList<String>();
        list.add(Types.DEVICE_TYPE_LOCAL_ID);
        return list;
    }

    @Override
    protected Collection<String> getCollectionItemTypeIdList() {
        List<String> list = new ArrayList<String>();
        list.add(Types.DEVICE_TYPE_LOCAL_ID);
        return list;
    }

    @Override
    protected ActionResult doAction(final Action action, final String itemTypeId, final Collection<Item> items)
            throws InvalidActionSpecificationException {

        return new ActionResult(ActionResult.Status.completed);

    }

}
