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
package com.hp.hpl.loom.adapter.os;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.AggregationUpdater;
import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes;
import com.hp.hpl.loom.model.SeparableItem;

public abstract class OsAggregationUpdater<T extends OsItem<A>, A extends CoreItemAttributes, R>
        extends AggregationUpdater<T, A, R> implements Iterator<R> {
    private static final Log LOG = LogFactory.getLog(OsAggregationUpdater.class);

    protected int regIdx = 0;
    protected int regMax = 0;
    protected int prjIdx = 0;
    protected int prjMax = 0;
    protected Iterator<? extends R> currentIterator;
    protected Iterator<? extends R> nextIterator;
    protected ArrayList<? extends SeparableItem> projects;
    protected String[] regions;
    protected R currentResource;
    protected String currentRegion;
    protected OsProject currentProject;
    protected OsItemCollector oic;

    public OsAggregationUpdater(final Aggregation aggregation, final BaseAdapter adapter, final String itemTypeLocalId,
            final OsItemCollector oic) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemTypeLocalId, oic);
        this.oic = oic;
    }

    protected String assignUuid() {
        return java.util.UUID.randomUUID().toString();
    }

    protected String getProjectId(final String projectName) {
        String projectId = null;
        for (SeparableItem<A> project : projects) {
            if (project.getName().equals(projectName)) {
                projectId = project.getCore().getItemId();
            }
        }
        return projectId;
    }

    @Override
    protected Iterator<R> getResourceIterator() {
        regIdx = 0;
        regMax = 0;
        projects = oic.getProjects();

        prjMax = projects.size();
        if (prjMax == 0) {
            return new ArrayList<R>(0).iterator();
        }
        prjIdx = -1;
        if (LOG.isTraceEnabled()) {
            LOG.trace("XXX: regIdx/regMax prjIdx/prjMax :" + regIdx + "/" + regMax + " " + prjIdx + "/" + prjMax);
        }
        currentIterator = getNewIterator();
        // regions has been set in the getNewIterator call
        if (currentIterator != null) {
            currentRegion = regions[regIdx - 1];
            currentProject = (OsProject) projects.get(prjIdx);
        }
        return this;
    }

    @Override
    public boolean hasNext() {
        boolean retBool = false;
        if (currentIterator != null && currentIterator.hasNext()) {
            retBool = true;
        } else {
            // loop until an iterator has data or no more
            while (true) {
                nextIterator = getNewIterator();
                if (nextIterator != null) {
                    if (nextIterator.hasNext()) {
                        retBool = true;
                        break;
                    }
                } else {
                    retBool = false;
                    break;
                }
            }
        }
        if (!retBool) {
            currentResource = null;
        }
        return retBool;
    }

    @Override
    public R next() {
        if (currentIterator != null && currentIterator.hasNext()) {
            currentResource = currentIterator.next();
            return currentResource;
        } else {
            if (nextIterator == null) {
                nextIterator = getNewIterator();
            }
            if (nextIterator != null && nextIterator.hasNext()) {
                currentIterator = nextIterator;
                currentRegion = regions[regIdx - 1];
                currentProject = (OsProject) projects.get(prjIdx);
                nextIterator = null;
                currentResource = currentIterator.next();
                return currentResource;
            } else {
                currentResource = null;
            }
            throw new NoSuchElementException();
        }
    }

    @Override
    public void remove() {
        // not needed so not implemented
    }

    protected Iterator<? extends R> getNewIterator() {
        if (regIdx < regMax) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("XXX 2: regIdx/regMax prjIdx/prjMax :" + regIdx + "/" + regMax + " " + prjIdx + "/" + prjMax);
            }
            return getResources(prjIdx, regIdx++);
        } else {
            if (LOG.isTraceEnabled()) {
                LOG.trace("XXX 3: regIdx/regMax prjIdx/prjMax :" + regIdx + "/" + regMax + " " + prjIdx + "/" + prjMax);
            }
            ++prjIdx;
            if (prjIdx < prjMax) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("XXX 4: regIdx/regMax prjIdx/prjMax :" + regIdx + "/" + regMax + " " + prjIdx + "/"
                            + prjMax);
                }
                regions = getConfiguredRegions(projects.get(prjIdx).getName());
                if (regions.length > 0) {
                    regIdx = 0;
                    regMax = regions.length;
                    Iterator<? extends R> iter = getResources(prjIdx, regIdx++);
                    return iter;
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    // additional abstract methods
    protected abstract Iterator<? extends R> getResources(int prjId, int regId);

    protected abstract String[] getConfiguredRegions(String projectName);

}
