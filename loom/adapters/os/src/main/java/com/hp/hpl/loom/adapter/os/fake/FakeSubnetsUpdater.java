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
import com.hp.hpl.loom.adapter.os.OsInstanceType;
import com.hp.hpl.loom.adapter.os.OsItemCollector;
// import org.apache.commons.logging.Log;
// import org.apache.commons.logging.LogFactory;
import com.hp.hpl.loom.adapter.os.OsProjectType;
import com.hp.hpl.loom.adapter.os.OsRegionType;
import com.hp.hpl.loom.adapter.os.OsSubnet;
import com.hp.hpl.loom.adapter.os.OsSubnetAttributes;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;


public class FakeSubnetsUpdater extends FakeAggregationUpdater<OsSubnet, OsSubnetAttributes, FakeSubnet> {
    // private static final Log LOG = LogFactory.getLog(FakeSubnetsUpdater.class);

    public FakeSubnetsUpdater(final Aggregation aggregation, final BaseAdapter adapter, final String itemTypeLocalId,
            final OsItemCollector oic, final FakeOsSystem fos) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemTypeLocalId, oic, fos);
    }

    @Override
    protected Iterator<FakeSubnet> getResources(final int prjIdx, final int regIdx) {
        return fos.getResourceManager(projects.get(prjIdx), regions[regIdx]).getSubnets().iterator();
    }

    @Override
    protected OsSubnet createEmptyItem(final String logicalId) {
        return new OsSubnet(logicalId, itemType);
    }

    @Override
    protected OsSubnetAttributes createItemAttributes(final FakeSubnet fs) {
        OsSubnetAttributes osa = new OsSubnetAttributes();
        osa.setItemName(fs.getItemName());
        osa.setItemId(fs.getItemId());
        osa.setNetworkId(fs.getNetworkId());
        osa.setEnableDhcp(fs.isEnableDhcp());
        osa.setGatewayIp(fs.getGatewayIp());
        osa.setCidr(fs.getCidr());
        osa.setIpVersion(fs.getIpVersion());
        return osa;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final OsSubnetAttributes osa, final FakeSubnet fs) {
        return ChangeStatus.UNCHANGED;
    }

    @Override
    protected void setRelationships(final ConnectedItem osSubnet, final FakeSubnet fs) {
        for (String id : fs.getInstanceIds()) {
            osSubnet.setRelationship(adapter.getProvider(), OsInstanceType.TYPE_LOCAL_ID, id);
        }
        osSubnet.setRelationship(adapter.getProvider(), OsRegionType.TYPE_LOCAL_ID, currentRegion);
        osSubnet.setRelationship(adapter.getProvider(), OsProjectType.TYPE_LOCAL_ID,
                currentProject.getCore().getItemId());
    }
}
