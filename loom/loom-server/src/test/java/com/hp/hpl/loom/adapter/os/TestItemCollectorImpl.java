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
package com.hp.hpl.loom.adapter.os;

import java.util.Collection;
import java.util.List;

import com.hp.hpl.loom.adapter.AdapterItem;
import com.hp.hpl.loom.adapter.AggregationUpdater;
import com.hp.hpl.loom.adapter.BaseItemCollector;
import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionResult;
import com.hp.hpl.loom.model.CoreItemAttributes;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.Session;

public class TestItemCollectorImpl extends BaseItemCollector {

    public TestItemCollectorImpl(final Session session) {
        super(session);
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
    protected void collectItems() {

    }

    @Override
    public <A extends CoreItemAttributes> AdapterItem<A> getAdapterItem(final String itemLocalTypeId,
            final String logicalId) {
        return null;
    }

    @Override
    public <A extends CoreItemAttributes> AdapterItem<A> getNewAdapterItem(final String itemLocalTypeId,
            final String logicalId) {
        return null;
    }

    @Override
    public String getLogicalId(final String itemTypeId, final String resourceId) {
        return null;
    }

    @Override
    public void categorizeItemAsUpdated(final String itemTypeLocalId, final Item item) {

    }

    @Override
    public boolean isGlobal() {
        return false;
    }

    @Override
    public ActionResult doAction(Action action, String itemTypeId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AggregationUpdater getUpdater(String itemTypeId) {
        // TODO Auto-generated method stub
        return null;
    }
}
