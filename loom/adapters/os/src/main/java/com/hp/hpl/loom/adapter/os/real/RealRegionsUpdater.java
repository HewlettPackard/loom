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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.adapter.os.OsAggregationUpdater;
import com.hp.hpl.loom.adapter.os.OsProject;
import com.hp.hpl.loom.adapter.os.OsProjectType;
import com.hp.hpl.loom.adapter.os.OsRegion;
import com.hp.hpl.loom.adapter.os.OsRegionAttributes;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;
import com.hp.hpl.loom.model.Item;

public class RealRegionsUpdater extends OsAggregationUpdater<OsRegion, OsRegionAttributes, String> {

    protected RealItemCollector ric;
    protected Map<String, List<OsProject>> projMap;

    public RealRegionsUpdater(final Aggregation aggregation, final BaseAdapter adapter, final String itemTypeLocalId,
            final RealItemCollector ric) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemTypeLocalId, ric);
        this.ric = ric;
    }

    @Override
    protected String getItemId(final String resource) {
        return resource;
    }

    @Override
    protected Iterator<String> getResourceIterator() {
        Iterator<String> iter = super.getResourceIterator();
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
    protected Iterator<String> getResources(final int prjIdx, final int regIdx) {
        String projectName = projects.get(prjIdx).getName();
        return ric.getOpenstackApi().getTokenManager().getTokenHolder().getRegions(projectName, Constants.PUBLIC)
                .iterator();
    }

    @Override
    protected String[] getConfiguredRegions(final String projectName) {
        Set<String> regions =
                ric.getOpenstackApi().getTokenManager().getTokenHolder().getRegions(projectName, Constants.PUBLIC);
        String[] r = new String[regions.size()];
        int i = 0;
        for (String reg : regions) {
            r[i] = reg;
            i++;
        }
        return r;
    }

    @Override
    protected OsRegion createEmptyItem(final String logicalId) {
        return new OsRegion(logicalId, itemType);
    }

    @Override
    protected OsRegionAttributes createItemAttributes(final String name) {
        OsRegionAttributes ora = new OsRegionAttributes();
        ora.setItemName(name);
        ora.setItemId(name);
        ora.setProviderId(ric.getProvider().getProviderId());
        return ora;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final OsRegionAttributes ora, final String fr) {
        return ChangeStatus.UNCHANGED;
    }

    @Override
    protected void setRelationships(final ConnectedItem osRegion, final String name) {
        // relationship
        // this is too narrow - one shot at setting rels so must link this region to all related
        // projs now
        // region.addConnectedRelationships(currentProject);
        for (OsProject osProj : projMap.get(osRegion.getName())) {
            osRegion.setRelationship(adapter.getProvider(), OsProjectType.TYPE_LOCAL_ID, osProj.getCore().getItemId());
        }
    }
}
