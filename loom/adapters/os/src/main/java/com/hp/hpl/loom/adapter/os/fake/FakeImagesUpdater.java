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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.adapter.os.OsImage;
import com.hp.hpl.loom.adapter.os.OsImageAttributes;
import com.hp.hpl.loom.adapter.os.OsItemCollector;
import com.hp.hpl.loom.adapter.os.OsProjectType;
import com.hp.hpl.loom.adapter.os.OsRegionType;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;

public class FakeImagesUpdater extends FakeAggregationUpdater<OsImage, OsImageAttributes, FakeImage> {
    private static final Log LOG = LogFactory.getLog(FakeImagesUpdater.class);

    // private int idx = 0;

    public FakeImagesUpdater(final Aggregation aggregation, final BaseAdapter adapter, final String itemTypeLocalId,
            final OsItemCollector oic, final FakeOsSystem fos) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemTypeLocalId, oic, fos);
    }

    @Override
    protected Iterator<FakeImage> getResources(final int prjIdx, final int regIdx) {
        // if (prjIdx + regIdx == 0) {
        // idx++;
        // }
        return fos.getResourceManager(projects.get(prjIdx), regions[regIdx]).getImages().iterator();
    }

    @Override
    protected OsImage createEmptyItem(final String logicalId) {
        return new OsImage(logicalId, itemType);
    }

    @Override
    protected OsImageAttributes createItemAttributes(final FakeImage fi) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("FAKE: project: " + currentProject.getName() + " found image: " + fi.getItemName() + " id: "
                    + fi.getItemId());
        }
        OsImageAttributes oia = new OsImageAttributes();
        oia.setItemName(fi.getItemName());
        oia.setItemId(fi.getItemId());
        oia.setCreated(fi.getCreated());
        oia.setUpdated(fi.getUpdated());
        oia.setStatus(fi.getStatus());
        oia.setMinDisk(fi.getMinDisk());
        oia.setMinRam(fi.getMinRam());
        // oia.setIdx(idx);
        return oia;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final OsImageAttributes oia, final FakeImage fi) {
        return ChangeStatus.UNCHANGED;
    }

    // @Override
    // protected boolean compareItemAttributesToResource(final OsImageAttributes oia, final
    // FakeImage fi) {
    // return oia.getStatus() != null && !oia.getStatus().equals(fi.getStatus());
    // }

    @Override
    protected void setRelationships(final ConnectedItem osImage, final FakeImage fi) {
        osImage.setRelationship(adapter.getProvider(), OsRegionType.TYPE_LOCAL_ID, currentRegion);
        osImage.setRelationship(adapter.getProvider(), OsProjectType.TYPE_LOCAL_ID,
                currentProject.getCore().getItemId());
    }
}
