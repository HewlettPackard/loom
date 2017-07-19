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

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.adapter.os.OsInstanceType;
import com.hp.hpl.loom.adapter.os.OsItemCollector;
import com.hp.hpl.loom.adapter.os.fake.FakeAggregationUpdater;
import com.hp.hpl.loom.adapter.os.fake.FakeInstance;
import com.hp.hpl.loom.adapter.os.fake.FakeOsSystem;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;

public class DiscoverWorkloadsUpdater extends FakeAggregationUpdater<OsWorkload, OsWorkloadAttributes, FakeWorkload> {
    private static final Log LOG = LogFactory.getLog(DiscoverWorkloadsUpdater.class);

    public DiscoverWorkloadsUpdater(final Aggregation aggregation, final BaseAdapter adapter,
            final String itemTypeLocalId, final OsItemCollector oic, final FakeOsSystem fos)
            throws NoSuchItemTypeException {
        super(aggregation, adapter, itemTypeLocalId, oic, fos);
    }

    @Override
    protected Iterator<FakeWorkload> getResources(final int prjIdx, final int regIdx) {
        DiscoverResourceManager drm =
                (DiscoverResourceManager) fos.getResourceManager(projects.get(prjIdx), regions[regIdx]);
        return drm.getWorkloads().iterator();
    }

    @Override
    protected OsWorkload createEmptyItem(final String logicalId) {
        return new OsWorkload(logicalId, itemType);
    }

    @Override
    protected OsWorkloadAttributes createItemAttributes(final FakeWorkload fw) {
        OsWorkloadAttributes owa = new OsWorkloadAttributes();
        if (LOG.isTraceEnabled()) {
            LOG.trace("FAKE: project: " + currentProject.getName() + " found workload: " + fw.getItemName() + " id: "
                    + fw.getItemId());
        }

        owa.setItemName(fw.getItemName());
        owa.setItemId(fw.getItemId());
        owa.setWorkloadType(fw.getWorkloadType());
        return owa;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final OsWorkloadAttributes owa, final FakeWorkload fw) {
        return ChangeStatus.UNCHANGED;
    }

    @Override
    protected void setRelationships(final ConnectedItem osWorkload, final FakeWorkload fw) {
        ((OsWorkload) osWorkload).setRegionName(currentRegion);
        ((OsWorkload) osWorkload).setProjectName(currentProject.getCore().getItemName());
        for (FakeInstance fi : fw.getInstances()) {
            osWorkload.setRelationship(adapter.getProvider(), OsInstanceType.TYPE_LOCAL_ID, fi.getItemId());
        }
    }
}
