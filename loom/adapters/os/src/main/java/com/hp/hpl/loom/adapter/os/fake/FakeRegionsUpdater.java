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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.adapter.os.OsItemCollector;
import com.hp.hpl.loom.adapter.os.OsProject;
import com.hp.hpl.loom.adapter.os.OsProjectType;
import com.hp.hpl.loom.adapter.os.OsRegion;
import com.hp.hpl.loom.adapter.os.OsRegionAttributes;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;
import com.hp.hpl.loom.model.Item;


public class FakeRegionsUpdater extends FakeAggregationUpdater<OsRegion, OsRegionAttributes, FakeRegion> {

    private static final Log LOG = LogFactory.getLog(FakeRegionsUpdater.class);

    protected Map<String, List<OsProject>> projMap;

    public FakeRegionsUpdater(final Aggregation aggregation, final BaseAdapter adapter, final String itemTypeLocalId,
            final OsItemCollector oic, final FakeOsSystem fos) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemTypeLocalId, oic, fos);
    }

    @Override
    protected Iterator<FakeRegion> getResourceIterator() {
        Iterator<FakeRegion> iter = super.getResourceIterator();
        // create region index to set relationships properly
        projMap = new HashMap<>();
        for (Item it : projects) {
            String[] regs = getConfiguredRegions(it.getName());
            for (String reg : regs) {
                List<OsProject> projList = projMap.get(reg);
                if (projList == null) {
                    projList = new ArrayList<>();
                    projMap.put(reg, projList);
                }
                projList.add((OsProject) it);
            }
        }
        return iter;
    }

    @Override
    protected Iterator<FakeRegion> getResources(final int prjIdx, final int regIdx) {
        ArrayList<FakeRegion> regs = new ArrayList<>();
        regs.add(new FakeRegion(regions[regIdx], regions[regIdx]));
        return regs.iterator();
    }

    @Override
    protected OsRegion createEmptyItem(final String logicalId) {
        return new OsRegion(logicalId, itemType);
    }

    @Override
    protected OsRegionAttributes createItemAttributes(final FakeRegion fr) {
        OsRegionAttributes ora = new OsRegionAttributes();
        ora.setItemName(fr.getItemName());
        ora.setItemId(fr.getItemName());
        ora.setProviderId(oic.getProvider().getProviderId());
        return ora;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final OsRegionAttributes ora, final FakeRegion fr) {
        return ChangeStatus.UNCHANGED;
    }

    @Override
    protected void setRelationships(final ConnectedItem osRegion, final FakeRegion fr) {
        for (OsProject osProj : projMap.get(osRegion.getName())) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("region name: " + osRegion.getName() + " with logicalId: " + osRegion.getLogicalId()
                        + " is linked to project: " + osProj.getCore().getItemId());
            }
            osRegion.setRelationship(adapter.getProvider(), OsProjectType.TYPE_LOCAL_ID, osProj.getCore().getItemId());
        }
    }

}
