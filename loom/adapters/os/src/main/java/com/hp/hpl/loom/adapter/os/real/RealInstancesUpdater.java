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

import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.adapter.os.OsAggregationUpdater;
import com.hp.hpl.loom.adapter.os.OsFlavour;
import com.hp.hpl.loom.adapter.os.OsImageType;
import com.hp.hpl.loom.adapter.os.OsInstance;
import com.hp.hpl.loom.adapter.os.OsInstanceAttributes;
import com.hp.hpl.loom.adapter.os.OsProjectType;
import com.hp.hpl.loom.adapter.os.OsRegionType;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;
import com.hp.hpl.loom.openstack.NoSupportedApiVersion;
import com.hp.hpl.loom.openstack.nova.NovaApi;
import com.hp.hpl.loom.openstack.nova.model.JsonFlavor;
import com.hp.hpl.loom.openstack.nova.model.JsonImage;
import com.hp.hpl.loom.openstack.nova.model.JsonServer;

public class RealInstancesUpdater extends OsAggregationUpdater<OsInstance, OsInstanceAttributes, JsonServer> {
    private static final Log LOG = LogFactory.getLog(RealInstancesUpdater.class);

    private RealItemCollector ric;
    private HashMap<String, OsFlavour> flavours;

    public RealInstancesUpdater(final Aggregation aggregation, final BaseAdapter adapter, final String itemTypeLocalId,
            final RealItemCollector ric) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemTypeLocalId, ric);
        this.ric = ric;
        flavours = new HashMap<>(4);
    }

    @Override
    protected String getItemId(final JsonServer resource) {
        return resource.getId();
    }

    @Override
    protected Iterator<? extends JsonServer> getResources(final int prjIdx, final int regIdx) {
        String regionId = regions[regIdx];
        String projectId = projects.get(prjIdx).getCore().getItemId();
        String computeVersion = adapter.getAdapterConfig().getPropertiesConfiguration()
                .getString(Constants.COMPUTE + Constants.VERSION_SUFFIX);

        String[] computeVersions = {};
        if (computeVersion != null) {
            computeVersions = computeVersion.split(",");
        }

        NovaApi novaApi;
        try {
            novaApi = ric.getOpenstackApi().getNovaApi(computeVersions, projectId, regionId);
        } catch (NoSupportedApiVersion e) {
            throw new RuntimeException("Problem accessing compute servers for version: " + computeVersion
                    + " projectId: " + projectId + " regionId: " + regionId);
        }
        getFlavours(novaApi);
        return novaApi.getNovaServers().getIterator();
    }

    @Override
    protected String[] getConfiguredRegions(final String projectName) {
        String projectId = getProjectId(projectName);
        return ric.getOpenstackApi().getTokenManager().getTokenHolder().getRegions(projectId, Constants.COMPUTE,
                Constants.PUBLIC);
    }

    private void getFlavours(final NovaApi novaApi) {
        flavours = new HashMap<>();
        Iterator<JsonFlavor> iterator = novaApi.getNovaFlavors().getIterator();
        if (iterator != null) {
            while (iterator.hasNext()) {
                JsonFlavor flavor = iterator.next();
                OsFlavour osFlavour = new OsFlavour(flavor.getId(), flavor.getName(), flavor.getDisk(), flavor.getRam(),
                        flavor.getVcpus());
                flavours.put(flavor.getId(), osFlavour);
            }
        }
    }

    @Override
    protected OsInstance createEmptyItem(final String logicalId) {
        return new OsInstance(logicalId, itemType);
    }

    @Override
    protected OsInstanceAttributes createItemAttributes(final JsonServer server) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("REAL: found server: " + server.getName() + " id: " + server.getId());
            LOG.trace("REAL: imageId: " + server.getImage().getId() + " status: " + server.getStatus());
        }

        OsInstanceAttributes oia = new OsInstanceAttributes();
        oia.setItemName(server.getName());
        oia.setItemId(server.getId());
        oia.setOsFlavour(flavours.get(server.getFlavor().getId()));

        oia.setStatus(server.getStatus());
        oia.setAccessIPv4(server.getAccessIPv4());
        oia.setAccessIPv6(server.getAccessIPv6());
        oia.setCreated(server.getCreated().toString());
        oia.setUpdated(server.getUpdated().toString());
        oia.setHostId(server.getHostId());

        return oia;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final OsInstanceAttributes oia, final JsonServer server) {
        if (oia.getStatus() != null && !oia.getStatus().toString().equals(server.getStatus().toString())) {
            return ChangeStatus.CHANGED_UPDATE;
        } else {
            return ChangeStatus.UNCHANGED;
        }
    }

    @Override
    protected void setRelationships(final ConnectedItem osInstance, final JsonServer server) {
        // imageId triggers a relationship

        JsonImage serverImage = server.getImage();
        // TODO - server Image was all null's
        if (serverImage != null && serverImage.getId() != null) {
            osInstance.setRelationship(adapter.getProvider(), OsImageType.TYPE_LOCAL_ID, serverImage.getId());
        }
        osInstance.setRelationship(adapter.getProvider(), OsRegionType.TYPE_LOCAL_ID, currentRegion);
        osInstance.setRelationship(adapter.getProvider(), OsProjectType.TYPE_LOCAL_ID,
                currentProject.getCore().getItemId());
    }
}
