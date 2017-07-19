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
import java.util.Map;

import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionResult;
import com.hp.hpl.loom.model.CoreItemAttributes;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Item;

public class TestItemCollector implements ItemCollector {

    Map<String, AdapterItem> itemMap = new HashMap<>();

    @Override
    public void run() {}

    @Override
    public void close() {}

    @Override
    public boolean setScheduled() {
        return false;
    }

    @Override
    public boolean setIdle() {
        return false;
    }

    @Override
    public ActionResult doAction(final Action action, final Collection<Item> items) {
        return null;
    }

    @Override
    public ActionResult doAction(final Action action, final List<String> itemLogicalIds) {
        return null;
    }

    @Override
    public <A extends CoreItemAttributes> AdapterItem<A> getAdapterItem(final String itemLocalTypeId,
            final String logicalId) {
        // TODO Auto-generated method stub
        return itemMap.get(logicalId);
    }

    @Override
    public <A extends CoreItemAttributes> AdapterItem<A> getNewAdapterItem(final String itemLocalTypeId,
            final String logicalId) {
        return null;
    }

    @Override
    public String getLogicalId(final String itemTypeId, final String resourceId) {
        return itemTypeId + "/" + resourceId;
    }

    @Override
    public void categorizeItemAsUpdated(final String itemLocalTypeId, final Item item) {

    }

    public void registerAdapterItem(final AdapterItem item) {
        itemMap.put(item.getLogicalId(), item);
        item.setItemCollector(this);
    }

    public void deregisterAdapterItem(final AdapterItem item) {
        itemMap.remove(item.getLogicalId());
    }

    @Override
    public void setCredentials(final Credentials credentials) {}

    @Override
    public HashMap<String, HashMap<String, String>> getRelationshipsDiscoveredOnCurrentUpdateCycle() {
        HashMap<String, HashMap<String, String>> relationshipsDiscoveredOnCurrentUpdateCycle =
                new HashMap<String, HashMap<String, String>>();
        return relationshipsDiscoveredOnCurrentUpdateCycle;
    }

    @Override
    public boolean isGlobal() {
        return false;
    }

    @Override
    public ActionResult doAction(Action action, String itemTypeId) {
        return null;
    }

    @Override
    public AggregationUpdater getUpdater(String itemTypeId) {
        return null;
    }
}
