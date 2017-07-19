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
import com.hp.hpl.loom.adapter.os.OsFlavour;
import com.hp.hpl.loom.adapter.os.OsImageType;
import com.hp.hpl.loom.adapter.os.OsInstance;
import com.hp.hpl.loom.adapter.os.OsInstanceAttributes;
import com.hp.hpl.loom.adapter.os.OsItemCollector;
import com.hp.hpl.loom.adapter.os.OsProjectType;
import com.hp.hpl.loom.adapter.os.OsRegionType;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;
import com.hp.hpl.loom.relationships.RelationshipUtil;


public class FakeInstancesUpdater extends FakeAggregationUpdater<OsInstance, OsInstanceAttributes, FakeInstance> {
    public static final int POWER_OFF_ALERT_LEVEL = 6;

    private static final Log LOG = LogFactory.getLog(FakeInstancesUpdater.class);

    private OsFlavour[] flavours;

    public FakeInstancesUpdater(final Aggregation aggregation, final BaseAdapter adapter, final String itemTypeLocalId,
            final OsItemCollector oic, final FakeOsSystem fos) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemTypeLocalId, oic, fos);
    }

    @Override
    protected Iterator<FakeInstance> getResources(final int prjIdx, final int regIdx) {
        flavours = fos.getResourceManager(projects.get(prjIdx), regions[regIdx]).getFlavours();
        return fos.getResourceManager(projects.get(prjIdx), regions[regIdx]).getInstances().iterator();
    }

    @Override
    protected OsInstance createEmptyItem(final String logicalId) {
        return new OsInstance(logicalId, itemType);
    }

    @Override
    protected OsInstanceAttributes createItemAttributes(final FakeInstance fi) {
        OsInstanceAttributes oia = new OsInstanceAttributes();
        oia.setItemName(fi.getItemName());
        oia.setItemId(fi.getItemId());
        oia.setOsFlavour(flavours[fi.getFlavourNbr()]);
        oia.setStatus(fi.getStatus());

        if (fi.getStatus().equals("SHUTOFF") && LOG.isDebugEnabled()) {
            LOG.debug("ACTION-DEBUG: FIU: saw a SHUTOFF for id: " + fi.getItemId());
        }

        oia.setAccessIPv4(fi.getAccessIPv4());
        oia.setAccessIPv6(fi.getAccessIPv6());
        oia.setCreated(fi.getCreated());
        oia.setUpdated(fi.getUpdated());
        oia.setHostId(fi.getHostId());

        return oia;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final OsInstanceAttributes oia, final FakeInstance fi) {
        if (oia.getStatus() != null && !oia.getStatus().equals(fi.getStatus())) {
            return ChangeStatus.CHANGED_UPDATE;
        } else {
            return ChangeStatus.UNCHANGED;
        }
    }

    @Override
    protected void setRelationships(final ConnectedItem osInstance, final FakeInstance fi) {
        // imageId triggers a relationship
        osInstance.setRelationship(adapter.getProvider(), OsImageType.TYPE_LOCAL_ID, fi.getImageId());
        osInstance.setRelationship(adapter.getProvider(), OsRegionType.TYPE_LOCAL_ID, currentRegion);
        osInstance.setRelationship(adapter.getProvider(), OsProjectType.TYPE_LOCAL_ID,
                currentProject.getCore().getItemId());
    }

}
