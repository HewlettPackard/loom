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
package com.hp.hpl.loom.adapter.os.fake;

import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.os.OsAggregationUpdater;
import com.hp.hpl.loom.adapter.os.OsItem;
import com.hp.hpl.loom.adapter.os.OsItemCollector;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes;

public abstract class FakeAggregationUpdater<T extends OsItem<A>, A extends CoreItemAttributes, R extends A>
        extends OsAggregationUpdater<T, A, R> {
    protected FakeOsSystem fos;

    public FakeAggregationUpdater(final Aggregation aggregation, final BaseAdapter adapter,
            final String itemTypeLocalId, final OsItemCollector oic, final FakeOsSystem fos)
            throws NoSuchItemTypeException {
        super(aggregation, adapter, itemTypeLocalId, oic);
        this.fos = fos;
    }

    @Override
    protected String getItemId(final R resource) {
        return resource.getItemId();
    }

    @Override
    protected String[] getConfiguredRegions(final String projectName) {
        return fos.getConfiguredRegions(projectName);
    }

}
