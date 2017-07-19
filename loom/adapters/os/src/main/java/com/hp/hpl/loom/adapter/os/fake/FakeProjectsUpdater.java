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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.adapter.os.OsItemCollector;
import com.hp.hpl.loom.adapter.os.OsProjectAttributes;
import com.hp.hpl.loom.adapter.os.OsQuota;
import com.hp.hpl.loom.adapter.os.ProjectsUpdater;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;

// import com.hp.hpl.loom.model.OpenstackProject;

public class FakeProjectsUpdater extends ProjectsUpdater<FakeProject> {
    private static final Log LOG = LogFactory.getLog(FakeProjectsUpdater.class);

    private FakeOsSystem fos;
    private OsItemCollector oic;
    protected List<FakeProject> projList;

    public FakeProjectsUpdater(final Aggregation aggregation, final BaseAdapter adapter, final String itemTypeLocalId,
            final OsItemCollector oic, final FakeOsSystem fos) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemTypeLocalId, oic);
        this.fos = fos;
        this.oic = oic;
        projList = fos.getProjects();

        if (LOG.isInfoEnabled()) {
            LOG.info("FAKE LIST OF PROJECTS: " + projList);
        }

    }

    @Override
    protected String getItemId(final FakeProject resource) {
        return resource.getItemId();
    }

    @Override
    protected Iterator<FakeProject> getResources() {
        return projList.iterator();
    }

    @Override
    protected OsProjectAttributes createItemAttributes(final FakeProject fp) {

        if (LOG.isDebugEnabled()) {
            LOG.trace("Populating new project Item: " + fp.getItemName());
        }
        OsProjectAttributes opa = new OsProjectAttributes();
        opa.setItemId(fp.getItemId());
        opa.setItemName(fp.getItemName());
        opa.setItemDescription(fp.getItemDescription());
        opa.setProviderId(oic.getProvider().getProviderId());

        OsQuota fq = fos.getOsQuota(fp.getItemId());

        if (fq != null) {
            opa.setInstancesQuota(fq.getInstances());
            opa.setCoresQuota(fq.getCores());
            opa.setFloatingIpsQuota(fq.getFloatingIps());
            opa.setGigabytesQuota(fq.getGigabytes());
            opa.setInjectedFileContentBytes(fq.getInjectedFileContentBytes());
            opa.setInjectedFilesQuota(fq.getInjectedFiles());
            opa.setRamQuota(fq.getRam());
            opa.setSecurityGroupRulesQuota(fq.getSecurityGroupRules());
            opa.setSecurityGroupsQuota(fq.getSecurityGroups());
            opa.setSnapshotsQuota(fq.getSnapshots());
            opa.setVolumesQuota(fq.getVolumes());
        }

        return opa;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final OsProjectAttributes opa, final FakeProject fp) {
        return ChangeStatus.UNCHANGED;
    }

    @Override
    protected void setRelationships(final ConnectedItem osProject, final FakeProject fp) {}
}
