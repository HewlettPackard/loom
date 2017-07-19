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
import com.hp.hpl.loom.adapter.os.OsVolume;
import com.hp.hpl.loom.adapter.os.OsVolumeAttributes;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;

public class FakeVolumesUpdater extends FakeAggregationUpdater<OsVolume, OsVolumeAttributes, FakeVolume> {
    // private static final Log LOG = LogFactory.getLog(FakeVolumesUpdater.class);

    public FakeVolumesUpdater(final Aggregation aggregation, final BaseAdapter adapter, final String itemTypeLocalId,
            final OsItemCollector oic, final FakeOsSystem fos) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemTypeLocalId, oic, fos);
    }

    @Override
    protected Iterator<FakeVolume> getResources(final int prjIdx, final int regIdx) {
        return fos.getResourceManager(projects.get(prjIdx), regions[regIdx]).getVolumes().iterator();
    }

    @Override
    protected OsVolume createEmptyItem(final String logicalId) {
        return new OsVolume(logicalId, itemType);
    }

    @Override
    protected OsVolumeAttributes createItemAttributes(final FakeVolume fv) {
        OsVolumeAttributes ova = new OsVolumeAttributes();
        ova.setItemName(fv.getItemName());
        ova.setItemId(fv.getItemId());
        ova.setSize(fv.getSize());
        ova.setCreated(fv.getCreated().toString());
        ova.setAvailabilityZone(fv.getAvailabilityZone());
        ova.setItemDescription(fv.getItemDescription());
        ova.setSnapshotId(fv.getSnapshotId());
        ova.setStatus(fv.getStatus());
        ova.setVolumeType(fv.getVolumeType());
        return ova;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final OsVolumeAttributes ova, final FakeVolume fv) {
        if (ova.getStatus() != null && !ova.getStatus().equals(fv.getStatus())) {
            return ChangeStatus.CHANGED_UPDATE;
        } else {
            return ChangeStatus.UNCHANGED;
        }

    }

    @Override
    protected void setRelationships(final ConnectedItem osVolume, final FakeVolume fv) {
        for (String id : fv.getAttachments()) {
            osVolume.setRelationship(adapter.getProvider(), OsInstanceType.TYPE_LOCAL_ID, id);
        }
        osVolume.setRelationship(adapter.getProvider(), OsRegionType.TYPE_LOCAL_ID, currentRegion);
        osVolume.setRelationship(adapter.getProvider(), OsProjectType.TYPE_LOCAL_ID,
                currentProject.getCore().getItemId());
    }
}
