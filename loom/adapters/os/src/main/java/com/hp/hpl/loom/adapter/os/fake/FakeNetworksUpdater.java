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

import java.util.Iterator;

import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.adapter.os.OsItemCollector;
import com.hp.hpl.loom.adapter.os.OsNetwork;
import com.hp.hpl.loom.adapter.os.OsNetworkAttributes;
import com.hp.hpl.loom.adapter.os.OsProjectType;
import com.hp.hpl.loom.adapter.os.OsRegionType;
import com.hp.hpl.loom.adapter.os.OsSubnetType;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;

public class FakeNetworksUpdater extends FakeAggregationUpdater<OsNetwork, OsNetworkAttributes, FakeNetwork> {
    // private static final Log LOG = LogFactory.getLog(FakeNetworksUpdater.class);

    public FakeNetworksUpdater(final Aggregation aggregation, final BaseAdapter adapter, final String itemTypeLocalId,
            final OsItemCollector oic, final FakeOsSystem fos) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemTypeLocalId, oic, fos);
    }

    @Override
    protected Iterator<FakeNetwork> getResources(final int prjIdx, final int regIdx) {
        return fos.getResourceManager(projects.get(prjIdx), regions[regIdx]).getNetworks().iterator();
    }

    @Override
    protected OsNetwork createEmptyItem(final String logicalId) {
        return new OsNetwork(logicalId, itemType);
    }

    @Override
    protected OsNetworkAttributes createItemAttributes(final FakeNetwork fn) {
        OsNetworkAttributes ona = new OsNetworkAttributes();
        ona.setItemName(fn.getItemName());
        ona.setItemId(fn.getItemId());
        ona.setShared(fn.isShared());
        ona.setStatus(fn.getStatus());
        ona.setAdminStateUp(fn.isAdminStateUp());
        return ona;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final OsNetworkAttributes ona, final FakeNetwork fn) {
        if (ona.getStatus() != null && !ona.getStatus().equals(fn.getStatus())) {
            return ChangeStatus.CHANGED_UPDATE;
        } else {
            return ChangeStatus.UNCHANGED;
        }
    }

    @Override
    protected void setRelationships(final ConnectedItem osNetwork, final FakeNetwork fn) {
        // imageId triggers a relationship
        for (String id : fn.getSubnetIds()) {
            osNetwork.setRelationship(adapter.getProvider(), OsSubnetType.TYPE_LOCAL_ID, id);
        }
        osNetwork.setRelationship(adapter.getProvider(), OsRegionType.TYPE_LOCAL_ID, currentRegion);
        osNetwork.setRelationship(adapter.getProvider(), OsProjectType.TYPE_LOCAL_ID,
                currentProject.getCore().getItemId());
    }
}
