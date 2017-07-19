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
package com.hp.hpl.loom.adapter.os.real;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.adapter.os.OsAggregationUpdater;
import com.hp.hpl.loom.adapter.os.OsProjectType;
import com.hp.hpl.loom.adapter.os.OsRegionType;
import com.hp.hpl.loom.adapter.os.OsSubnet;
import com.hp.hpl.loom.adapter.os.OsSubnetAttributes;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;
import com.hp.hpl.loom.model.SeparableItem;
import com.hp.hpl.loom.openstack.NoSupportedApiVersion;
import com.hp.hpl.loom.openstack.neutron.model.JsonSubnet;

public class RealSubnetsUpdater extends OsAggregationUpdater<OsSubnet, OsSubnetAttributes, JsonSubnet> {
    private static final Log LOG = LogFactory.getLog(RealSubnetsUpdater.class);

    private RealItemCollector ric;

    public RealSubnetsUpdater(final Aggregation aggregation, final BaseAdapter adapter, final String itemTypeLocalId,
            final RealItemCollector ric) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemTypeLocalId, ric);
        this.ric = ric;
    }

    @Override
    protected String getItemId(final JsonSubnet resource) {
        return resource.getId();
    }

    @Override
    protected Iterator<? extends JsonSubnet> getResources(final int prjIdx, final int regIdx) {
        String regionId = regions[regIdx];
        String projectId = projects.get(prjIdx).getCore().getItemId();
        String networkVersion = adapter.getAdapterConfig().getPropertiesConfiguration()
                .getString(Constants.NETWORK + Constants.VERSION_SUFFIX);

        String[] networkVersions = {};
        if (networkVersion != null) {
            networkVersions = networkVersion.split(",");
        }

        try {
            return ric.getOpenstackApi().getNeutronApi(networkVersions, projectId, regionId).getNeutronSubnets()
                    .getJsonResources().getSubnets().iterator();
        } catch (NoSupportedApiVersion e) {
            throw new RuntimeException("Problem accessing network subnet for version: " + networkVersion
                    + " projectId: " + projectId + " regionId: " + regionId);
        }
    }

    @Override
    protected String[] getConfiguredRegions(final String projectName) {
        String projectId = getProjectId(projectName);
        return ric.getOpenstackApi().getTokenManager().getTokenHolder().getRegions(projectId, Constants.NETWORK,
                Constants.PUBLIC);
    }

    @Override
    protected OsSubnet createEmptyItem(final String logicalId) {
        return new OsSubnet(logicalId, itemType);
    }

    @Override
    protected OsSubnetAttributes createItemAttributes(final JsonSubnet subnet) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("REAL: project: " + currentProject.getName() + " found subnet: " + subnet.getName() + " id: "
                    + subnet.getId());
        }

        // return without populating
        if (!subnet.getTenantId().equals(currentProject.getCore().getItemId())) {
            boolean subnetInNetwork = false;
            ArrayList<? extends SeparableItem> networks = ric.getNetworks();
            for (SeparableItem separableItem : networks) {
                if (separableItem.getCore().getItemId().equals(subnet.getNetworkId())) {
                    subnetInNetwork = true;
                }
            }

            if (!subnetInNetwork) {
                return null;
            }
        }

        OsSubnetAttributes osa = new OsSubnetAttributes();
        osa.setItemName(subnet.getName());
        osa.setItemId(subnet.getId());
        osa.setNetworkId(subnet.getNetworkId());
        osa.setEnableDhcp(subnet.isEnableDhcp());
        osa.setGatewayIp(subnet.getGatewayIp());
        osa.setCidr(subnet.getCidr());
        osa.setIpVersion(subnet.getIpVersion());
        return osa;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final OsSubnetAttributes osa, final JsonSubnet subnet) {
        return ChangeStatus.UNCHANGED;
    }

    @Override
    protected void setRelationships(final ConnectedItem osSubnet, final JsonSubnet subnet) {
        if (osSubnet == null) {
            return;
        }
        osSubnet.setRelationship(adapter.getProvider(), OsRegionType.TYPE_LOCAL_ID, currentRegion);
        osSubnet.setRelationship(adapter.getProvider(), OsProjectType.TYPE_LOCAL_ID,
                currentProject.getCore().getItemId());
    }

}
