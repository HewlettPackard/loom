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
import java.util.Set;

import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.adapter.os.OsAggregationUpdater;
import com.hp.hpl.loom.adapter.os.OsInstance;
import com.hp.hpl.loom.adapter.os.OsItem;
import com.hp.hpl.loom.adapter.os.OsPortType;
import com.hp.hpl.loom.adapter.os.OsSubnetType;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;
import com.hp.hpl.loom.openstack.NoSupportedApiVersion;
import com.hp.hpl.loom.openstack.neutron.model.JsonIp;
import com.hp.hpl.loom.openstack.neutron.model.JsonPort;

public class RealPortsUpdater extends OsAggregationUpdater<OsItem<CoreItemAttributes>, CoreItemAttributes, JsonPort> {

    private RealItemCollector ric;

    public RealPortsUpdater(final Aggregation aggregation, final BaseAdapter adapter, final RealItemCollector ric)
            throws NoSuchItemTypeException {
        super(aggregation, adapter, OsPortType.TYPE_LOCAL_ID, ric);
        this.ric = ric;
    }

    @Override
    protected String getItemId(final JsonPort resource) {
        return resource.getId();
    }

    @Override
    protected Iterator<? extends JsonPort> getResources(final int prjIdx, final int regIdx) {
        String regionId = regions[regIdx];
        String projectId = projects.get(prjIdx).getCore().getItemId();
        String networkVersion = adapter.getAdapterConfig().getPropertiesConfiguration()
                .getString(Constants.NETWORK + Constants.VERSION_SUFFIX);
        String[] networkVersions = {};
        if (networkVersion != null) {
            networkVersions = networkVersion.split(",");
        }

        try {
            return ric.getOpenstackApi().getNeutronApi(networkVersions, projectId, regionId).getNeutronPorts()
                    .getJsonResources().getPorts().iterator();
        } catch (NoSupportedApiVersion e) {
            throw new RuntimeException("Problem accessing network ports for version: " + networkVersion + " projectId: "
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
    protected OsItem createEmptyItem(final String logicalId) {
        return null;
    }

    @Override
    protected CoreItemAttributes createItemAttributes(final JsonPort port) {
        return null;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final CoreItemAttributes ia, final JsonPort port) {
        return ChangeStatus.UNCHANGED;
    }

    @Override
    protected void setRelationships(final ConnectedItem osItem, final JsonPort port) {
        if (port.getDeviceOwner().startsWith("compute:")) {
            // log.info("REAL: setting realtionships for server: "+port.getDeviceId());
            OsInstance instance = ric.getInstance(port.getDeviceId());
            if (instance != null) {
                Set<JsonIp> ips = port.getFixedIps();
                for (JsonIp ip : ips) {
                    instance.setRelationship(adapter.getProvider(), OsSubnetType.TYPE_LOCAL_ID, ip.getSubnetId());
                }
            }
        }
    }
}
