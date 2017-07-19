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

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.adapter.os.OsAggregationUpdater;
import com.hp.hpl.loom.adapter.os.OsNetwork;
import com.hp.hpl.loom.adapter.os.OsNetworkAttributes;
import com.hp.hpl.loom.adapter.os.OsProjectType;
import com.hp.hpl.loom.adapter.os.OsRegionType;
import com.hp.hpl.loom.adapter.os.OsSubnetType;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;
import com.hp.hpl.loom.openstack.NoSupportedApiVersion;
import com.hp.hpl.loom.openstack.neutron.model.JsonNetwork;

public class RealNetworksUpdater extends OsAggregationUpdater<OsNetwork, OsNetworkAttributes, JsonNetwork> {
    private static final Log LOG = LogFactory.getLog(RealNetworksUpdater.class);

    private RealItemCollector ric;

    public RealNetworksUpdater(final Aggregation aggregation, final BaseAdapter adapter, final String itemTypeLocalId,
            final RealItemCollector ric) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemTypeLocalId, ric);
        this.ric = ric;
    }

    @Override
    protected String getItemId(final JsonNetwork resource) {
        return resource.getId();
    }

    @Override
    protected Iterator<? extends JsonNetwork> getResources(final int prjIdx, final int regIdx) {
        String regionId = regions[regIdx];
        String projectId = projects.get(prjIdx).getCore().getItemId();

        String networkVersion = adapter.getAdapterConfig().getPropertiesConfiguration()
                .getString(Constants.NETWORK + Constants.VERSION_SUFFIX);

        String[] networkVersions = null;
        if (networkVersion != null) {
            networkVersions = networkVersion.split(",");
        }
        try {
            return ric.getOpenstackApi().getNeutronApi(networkVersions, projectId, regionId).getNeutronNetwork()
                    .getIterator();
        } catch (NoSupportedApiVersion e) {
            throw new RuntimeException("Problem accessing networks for version: " + networkVersion + " projectId: "
                    + projectId + " regionId: " + regionId);
        }
    }

    @Override
    protected String[] getConfiguredRegions(final String projectName) {
        String projectId = getProjectId(projectName);
        return ric.getOpenstackApi().getTokenManager().getTokenHolder().getRegions(projectId, Constants.NETWORK,
                Constants.PUBLIC);
    }

    @Override
    protected OsNetwork createEmptyItem(final String logicalId) {
        return new OsNetwork(logicalId, itemType);
    }

    @Override
    protected OsNetworkAttributes createItemAttributes(final JsonNetwork network) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("REAL: project: " + currentProject.getName() + " found network: " + network.getName() + " id: "
                    + network.getId());
        }

        // return without populating
        if (network.isShared() == false && !network.getTenantId().equals(currentProject.getCore().getItemId())) {
            LOG.info("Dropping network: project: " + currentProject.getName() + " found network: " + network.getName()
                    + " id: " + network.getId() + " vs " + currentProject.getCore().getItemId() + " shared? "
                    + network.isShared());
            return null;
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("REAL: project: " + currentProject.getName() + " proceed with network: " + network.getName()
                    + " id: " + network.getId());
        }
        OsNetworkAttributes ona = new OsNetworkAttributes();
        ona.setItemName(network.getName());
        ona.setItemId(network.getId());
        ona.setShared(network.isShared());
        ona.setStatus(network.getStatus().toString());
        ona.setAdminStateUp(network.isAdminStateUp());
        return ona;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final OsNetworkAttributes ona, final JsonNetwork network) {
        if (ona.getStatus() != null && !ona.getStatus().toString().equals(network.getStatus().toString())) {
            return ChangeStatus.CHANGED_UPDATE;
        } else {
            return ChangeStatus.UNCHANGED;
        }
    }

    @Override
    protected void setRelationships(final ConnectedItem osNetwork, final JsonNetwork network) {
        if (osNetwork == null) {
            return;
        }
        // imageId triggers a relationship
        for (String id : network.getSubnets()) {
            osNetwork.setRelationship(adapter.getProvider(), OsSubnetType.TYPE_LOCAL_ID, id);
        }
        osNetwork.setRelationship(adapter.getProvider(), OsRegionType.TYPE_LOCAL_ID, currentRegion);
        if (network.isShared() == false || network.getTenantId().equals(currentProject.getCore().getItemId())) {
            osNetwork.setRelationship(adapter.getProvider(), OsProjectType.TYPE_LOCAL_ID,
                    currentProject.getCore().getItemId());
        }
    }

}
