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
package com.hp.hpl.loom.adapter.os.discover;

import java.util.Collection;

import com.hp.hpl.loom.adapter.AggregationUpdater;
import com.hp.hpl.loom.adapter.os.fake.FakeConfig;
import com.hp.hpl.loom.adapter.os.fake.FakeItemCollector;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.manager.adapter.AdapterManager;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Session;


public class DiscoverItemCollector extends FakeItemCollector {

    public DiscoverItemCollector(final Session session, final DiscoverAdapter adapter,
            final AdapterManager adapterManager, final Credentials creds, final String authEndpoint,
            final FakeConfig fc, final int index) {
        super(session, adapter, adapterManager, creds, authEndpoint, fc);
        // setup data generation
        fos = new DiscoverOsSystem(fc, index, adapter.getProvider().getProviderName());
        fos.init();
    }

    @Override
    protected Collection<String> createUpdateItemTypeIdList() {
        Collection<String> superList = super.createUpdateItemTypeIdList();
        superList.add(OsWorkloadType.TYPE_LOCAL_ID);
        return superList;
    }

    @Override
    protected Collection<String> createCollectionItemTypeIdList() {
        Collection<String> superList = super.createCollectionItemTypeIdList();
        superList.add(OsWorkloadType.TYPE_LOCAL_ID);
        return superList;
    }

    @Override
    protected AggregationUpdater<?, ?, ?> getAggregationUpdater(final Aggregation agg)
            throws NoSuchProviderException, NoSuchItemTypeException {
        AggregationUpdater<?, ?, ?> aggUp = null;
        try {
            if (aggregationMatchesItemType(agg, OsWorkloadType.TYPE_LOCAL_ID)) {
                // workload
                aggUp = new DiscoverWorkloadsUpdater(agg, adapter, OsWorkloadType.TYPE_LOCAL_ID, this, fos);
            } else {
                aggUp = super.getAggregationUpdater(agg);
            }
        } catch (RuntimeException ex) {
            throw new NoSuchProviderException("adapter has gone");
        }
        return aggUp;
    }

}
