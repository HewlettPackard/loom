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
import com.hp.hpl.loom.adapter.os.OsProjectAttributes;
import com.hp.hpl.loom.adapter.os.OsQuota;
import com.hp.hpl.loom.adapter.os.ProjectsUpdater;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NullQuotaInformationException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;
import com.hp.hpl.loom.openstack.NoSupportedApiVersion;
import com.hp.hpl.loom.openstack.cinder.impl.CinderVolumeQuotaImpl;
import com.hp.hpl.loom.openstack.cinder.model.JsonVolumeQuota;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonProject;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonProjects;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonUser;
import com.hp.hpl.loom.openstack.nova.model.JsonQuota;

// import org.jclouds.openstack.keystone.v2_0.KeystoneApi;

public class RealProjectsUpdater extends ProjectsUpdater<JsonProject> {
    private static final Log LOG = LogFactory.getLog(RealProjectsUpdater.class);

    private RealItemCollector ric;

    public RealProjectsUpdater(final Aggregation aggregation, final BaseAdapter adapter, final String itemTypeLocalId,
            final RealItemCollector ric) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemTypeLocalId, ric);
        this.ric = ric;
    }

    @Override
    protected String getItemId(final JsonProject resource) {
        return resource.getId();
    }

    @Override
    protected Iterator<? extends JsonProject> getResources() {
        JsonUser jsonUser = ric.getOpenstackApi().getTokenManager().getJsonUser();
        JsonProjects jsonProjects =
                ric.getOpenstackApi().getKeystoneApi().getKeystoneProject().getUsersProjects(jsonUser.getId());

        for (JsonProject jsonProject : jsonProjects.getProjects()) {
            if (jsonProject.isEnabled()) {
                ric.getOpenstackApi().getKeystoneApi().getKeystoneProject().getServiceCatalog(jsonProject.getId(),
                        jsonProject.getName());
            }
        }

        return jsonProjects.getProjects().iterator();
        // Optional<? extends TenantApi> optApi = keystoneApi.getTenantApi();
        // return optApi.get().list().concat().iterator();
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final OsProjectAttributes opa, final JsonProject project) {
        return ChangeStatus.UNCHANGED;
    }

    @Override
    protected void setRelationships(final ConnectedItem osProject, final JsonProject project) {}

    @Override
    protected OsProjectAttributes createItemAttributes(final JsonProject project) {

        if (LOG.isDebugEnabled()) {
            LOG.trace("Populating new project Item: " + project.getName());
        }
        OsProjectAttributes opa = new OsProjectAttributes();
        opa.setItemId(project.getId());
        opa.setItemName(project.getName());

        String description = project.getDescription();

        if (description == null || description.equals("")) {
            description = "Not set";
        }

        opa.setItemDescription(description);
        opa.setProviderId(ric.getProvider().getProviderId());

        try {
            OsQuota quota = getQuota(opa);
            opa.setInstancesQuota(quota.getInstances());
            opa.setCoresQuota(quota.getCores());
            opa.setFloatingIpsQuota(quota.getFloatingIps());
            opa.setGigabytesQuota(quota.getGigabytes());
            opa.setInjectedFileContentBytes(quota.getInjectedFileContentBytes());
            opa.setInjectedFilesQuota(quota.getInjectedFiles());
            opa.setRamQuota(quota.getRam());
            opa.setSecurityGroupRulesQuota(quota.getSecurityGroupRules());
            opa.setSecurityGroupsQuota(quota.getSecurityGroups());
            opa.setSnapshotsQuota(quota.getSnapshots());
            opa.setVolumesQuota(quota.getVolumes());
        } catch (NullQuotaInformationException e) {
            LOG.warn(e.getMessage());
        }


        return opa;
    }

    private OsQuota getQuota(final OsProjectAttributes opa) throws NullQuotaInformationException {
        int cores = 0;
        int instances = 0;
        int injectedFiles = 0;
        int injectedFileContentBytes = 0;
        int volumes = 0;
        int snapshots = 0;
        int gigabytes = 0;
        int ram = 0;
        int securityGroups = 0;
        int securityGroupRules = 0;
        int floatingIps = 0;

        String computeVersion = adapter.getAdapterConfig().getPropertiesConfiguration()
                .getString(Constants.COMPUTE + Constants.VERSION_SUFFIX);
        String volumeVersion = adapter.getAdapterConfig().getPropertiesConfiguration()
                .getString(Constants.VOLUME + Constants.VERSION_SUFFIX);
        String[] volumeVersions = {};
        if (volumeVersion != null) {
            volumeVersions = volumeVersion.split(",");
        }
        String[] computeVersions = {};
        if (computeVersion != null) {
            computeVersions = computeVersion.split(",");
        }

        // nova quotas

        String[] regionIds = ric.getOpenstackApi().getTokenManager().getTokenHolder().getRegions(opa.getItemName(),
                Constants.COMPUTE, Constants.PUBLIC);
        for (String regionId : regionIds) {
            Iterator<JsonQuota> jsonQuotas;
            try {
                jsonQuotas = ric.getOpenstackApi().getNovaApi(computeVersions, opa.getItemName(), regionId)
                        .getNovaQuotas().getIterator();
            } catch (NoSupportedApiVersion e) {
                throw new RuntimeException("Problem accessing compute quotas for version: " + computeVersion
                        + " projectId: " + opa.getItemName() + " regionId: " + regionId);
            }
            JsonQuota jsonQuota = null;
            while (jsonQuotas.hasNext()) {
                jsonQuota = jsonQuotas.next();
            }
            if (jsonQuota != null) {
                try {
                    cores += jsonQuota.getCores();
                    instances += jsonQuota.getInstances();
                    injectedFiles += jsonQuota.getInjectedFiles();
                    injectedFileContentBytes += jsonQuota.getInjectedFileContentBytes();

                    ram += jsonQuota.getRam();
                    securityGroups += jsonQuota.getSecurityGroups();
                    securityGroupRules += jsonQuota.getSecurityGroupRules();
                    floatingIps += jsonQuota.getFloatingIps();
                } catch (NullPointerException e) {
                    throw new NullQuotaInformationException("Some of the quotas information is not available");
                }
            }
        }

        // cinder quotas
        // String[] cinderZones = ric.getCinderZones(opa.getItemName());
        //
        String[] cinderZones = ric.getOpenstackApi().getTokenManager().getTokenHolder().getRegions(opa.getItemName(),
                Constants.VOLUME, Constants.PUBLIC);

        for (String cinderZone : cinderZones) {
            CinderVolumeQuotaImpl quotaApi;
            try {
                quotaApi = ric.getOpenstackApi().getCinderApi(volumeVersions, opa.getItemName(), cinderZone)
                        .getCinderVolumeQuota();
            } catch (NoSupportedApiVersion e1) {
                throw new RuntimeException("Problem accessing volume quota for version: " + volumeVersion
                        + " projectId: " + opa.getItemName() + " regionId: " + cinderZone);
            }
            Iterator<JsonVolumeQuota> jsonVolumeQuotas = quotaApi.getIterator();
            while (jsonVolumeQuotas.hasNext()) {
                JsonVolumeQuota volQuota = jsonVolumeQuotas.next();

                try {
                    snapshots += volQuota.getSnapshots();
                    gigabytes += volQuota.getGigabytes();
                    volumes += volQuota.getVolumes();
                } catch (NullPointerException e) {
                    throw new NullQuotaInformationException("Some of the quotas information is not available");
                }
            }

        }


        return new OsQuota(opa.getItemId(), cores, instances, injectedFiles, injectedFileContentBytes, volumes,
                snapshots, gigabytes, ram, securityGroups, securityGroupRules, floatingIps);
    }
}
