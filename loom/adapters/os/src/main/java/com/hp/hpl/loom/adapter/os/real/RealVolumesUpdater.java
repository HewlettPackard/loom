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
import com.hp.hpl.loom.adapter.os.OsInstanceType;
import com.hp.hpl.loom.adapter.os.OsProjectType;
import com.hp.hpl.loom.adapter.os.OsRegionType;
import com.hp.hpl.loom.adapter.os.OsVolume;
import com.hp.hpl.loom.adapter.os.OsVolumeAttributes;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;
import com.hp.hpl.loom.openstack.NoSupportedApiVersion;
import com.hp.hpl.loom.openstack.cinder.model.JsonAttachment;
import com.hp.hpl.loom.openstack.cinder.model.JsonVolume;

public class RealVolumesUpdater extends OsAggregationUpdater<OsVolume, OsVolumeAttributes, JsonVolume> {
    private static final Log LOG = LogFactory.getLog(RealVolumesUpdater.class);

    private RealItemCollector ric;

    // private HashMap<String, String> volumeTypes;

    public RealVolumesUpdater(final Aggregation aggregation, final BaseAdapter adapter, final String itemTypeLocalId,
            final RealItemCollector ric) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemTypeLocalId, ric);
        this.ric = ric;
        // volumeTypes = new HashMap<>();
    }

    @Override
    protected String getItemId(final JsonVolume resource) {
        return resource.getId();
    }

    @Override
    protected Iterator<? extends JsonVolume> getResources(final int prjIdx, final int regIdx) {
        String regionId = regions[regIdx];
        String projectId = projects.get(prjIdx).getCore().getItemId();
        String volumeVersion = adapter.getAdapterConfig().getPropertiesConfiguration()
                .getString(Constants.VOLUME + Constants.VERSION_SUFFIX);
        String[] volumeVersions = {};
        if (volumeVersion != null) {
            volumeVersions = volumeVersion.split(",");
        }


        try {
            return ric.getOpenstackApi().getCinderApi(volumeVersions, projectId, regionId).getCinderVolume()
                    .getIterator();
        } catch (NoSupportedApiVersion e) {
            throw new RuntimeException("Problem accessing cinder volume for version: " + volumeVersion + " projectId: "
                    + projectId + " regionId: " + regionId);
        }
    }

    @Override
    protected String[] getConfiguredRegions(final String projectName) {
        String projectId = getProjectId(projectName);
        return ric.getOpenstackApi().getTokenManager().getTokenHolder().getRegions(projectId, Constants.VOLUME,
                Constants.PUBLIC);
    }

    @Override
    protected OsVolume createEmptyItem(final String logicalId) {
        return new OsVolume(logicalId, itemType);
    }

    @Override
    protected OsVolumeAttributes createItemAttributes(final JsonVolume volume) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("REAL: found volume: " + volume.getDisplayName() + " id: " + volume.getId());
            LOG.trace("REAL: size: " + volume.getSize());
        }
        OsVolumeAttributes ova = new OsVolumeAttributes();
        ova.setItemName(volume.getDisplayName());
        ova.setItemId(volume.getId());
        ova.setSize(volume.getSize());
        ova.setCreated(volume.getCreatedAt().toString());
        ova.setAvailabilityZone(volume.getAvailabilityZone());
        if (volume.getDisplayName() == null || volume.getDisplayName().equals("")) {
            ova.setItemName(volume.getId());
        }
        if (volume.getDisplayDescription() == null || volume.getDisplayDescription().equals("")) {
            ova.setItemDescription(volume.getId());
        } else {
            ova.setItemDescription(volume.getDisplayDescription());
        }
        ova.setSnapshotId(volume.getSnapshotId());
        ova.setStatus(volume.getStatus().toString());
        ova.setVolumeType(volume.getVolumeType());
        return ova;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final OsVolumeAttributes ova, final JsonVolume volume) {
        if (ova.getStatus() != null && !ova.getStatus().toString().equals(volume.getStatus().toString())) {
            return ChangeStatus.CHANGED_UPDATE;
        } else {
            return ChangeStatus.UNCHANGED;
        }
    }

    @Override
    protected void setRelationships(final ConnectedItem osVolume, final JsonVolume volume) {
        for (JsonAttachment va : volume.getAttachments()) {
            osVolume.setRelationship(adapter.getProvider(), OsInstanceType.TYPE_LOCAL_ID, va.getServerId());
        }
        osVolume.setRelationship(adapter.getProvider(), OsRegionType.TYPE_LOCAL_ID, currentRegion);
        osVolume.setRelationship(adapter.getProvider(), OsProjectType.TYPE_LOCAL_ID,
                currentProject.getCore().getItemId());
    }
}
