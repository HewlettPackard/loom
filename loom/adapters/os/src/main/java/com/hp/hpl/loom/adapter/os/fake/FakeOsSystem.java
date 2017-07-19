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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.AdapterItem;
import com.hp.hpl.loom.adapter.os.OsQuota;
import com.hp.hpl.loom.model.Item;

/**
 *
 * this is a fake data generator instantiated per session(user)
 *
 */
public class FakeOsSystem {
    private static final Log LOG = LogFactory.getLog(FakeOsSystem.class);

    protected String[] regions = {};

    private FakeConfig fc;

    // projects
    protected int projectNbr;
    // per project
    protected int regionNbr;
    // per region per project
    protected int instanceNbr;
    protected int imageNbr;
    // ONLY ONE VOL PER VM FOR NOW - RELATIONSHIP CONSTRAINT
    protected int volsPerVm;
    protected int extraVols;
    protected int volSizeMax;
    protected int sizeSteps;
    protected int subsPerVm;
    protected int extraNets;
    protected int subsPerExtraNets; // private ArrayList<FakeResource> projects;
    protected int rebootCount;
    protected int networkNbr;
    protected int subsPerNet;
    protected int vmPerSub;
    protected int vmWithVolRatio;

    protected ArrayList<FakeProject> projects;

    private HashMap<String, String[]> regionMap;
    protected HashMap<String, FakeResourceManager> managerMap;
    private HashMap<String, OsQuota> quotaMap;
    protected int adapterIdx;
    private int instanceIndex = 0;

    /*
     * Central allocation of indices to instances.
     */
    protected int getNextInstanceIndex() {
        return instanceIndex++;
    }

    public FakeOsSystem(final FakeConfig fc, final int index) {
        projects = new ArrayList<>();
        // init Maps
        regionMap = new HashMap<String, String[]>();
        managerMap = new HashMap<String, FakeResourceManager>();
        quotaMap = new HashMap<>();
        this.fc = fc;
        adapterIdx = index;
        projectNbr = fc.getProjectNbr(index);
        regionNbr = fc.getRegionNbr(index);
        instanceNbr = fc.getInstanceNbr(index);
        imageNbr = fc.getImageNbr(index);
        volsPerVm = fc.getVolsPerVm(index);
        extraVols = fc.getExtraVols(index);
        volSizeMax = fc.getVolSizeMax(index);
        sizeSteps = fc.getSizeSteps(index);
        subsPerVm = fc.getSubsPerVm(index);
        extraNets = fc.getExtraNets(index);
        rebootCount = fc.getRebootCount(index);
        subsPerExtraNets = fc.getSubsPerExtraNets(index);
        rebootCount = fc.getRebootCount(index);
        networkNbr = fc.getNetworkNbr(index);
        subsPerNet = fc.getSubnetPerNetworkNbr(index);
        vmPerSub = fc.getVmPerSubnetNbr(index);
        vmWithVolRatio = fc.getVmWithVolumeRatio(index);
    }

    protected void setAllRegions() {
        if (regions.length == 0) {
            regions = new String[regionNbr];
            for (int i = 0; i < regionNbr; ++i) {
                regions[i] = "region-" + i;
            }
        }
    }

    protected void setProjects() {
        for (int i = 0; i < projectNbr; ++i) {
            FakeProject osprj = new FakeProject("project-" + i, "prj_" + i, "fake project number " + i);
            projects.add(osprj);
        }
    }

    public void init() {
        setProjects();
        setAllRegions();
        int projIdx = 0;
        for (FakeProject project : projects) {
            setQuota(project.getItemId(), projIdx++, adapterIdx);
            // projects can be configured to be in different regions
            // for now, all projects have resources in all defined regions
            String[] selectedRegions = selectRegions(project);
            String projectName = project.getItemName();
            regionMap.put(projectName, selectedRegions);
            for (int i = 0; i < selectedRegions.length; ++i) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Adding manager for" + "project " + projectName + " and region " + selectedRegions[i]);
                }
                managerMap.put(getManagerKey(project.getItemName(), selectedRegions[i]),
                        createResourceManager(this, i));
            }
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("regionMap: " + regionMap);
            LOG.info("managerMap " + managerMap);
        }

    }

    protected void setQuota(final String projectId, final int projIdx, final int adapterIdx) {
        int repeatNbr = fc.getQuotaRepeatNbr();
        int loopIdx = projIdx % repeatNbr;
        int quotaIdx = adapterIdx * repeatNbr + loopIdx;
        OsQuota osQuota = new OsQuota(projectId, fc.getCoreQuota(quotaIdx), fc.getInstanceQuota(quotaIdx), 0, 0,
                fc.getVolumeQuota(quotaIdx), 0, fc.getGigabyteQuota(quotaIdx), fc.getRamQuota(quotaIdx), 0, 0, 0);
        quotaMap.put(projectId, osQuota);
    }

    @SuppressWarnings("PMD.UnusedFormalParameter")
    private String[] selectRegions(final FakeProject project) {
        // for now, all projects have resources in all defined regions
        return regions;
    }

    protected String getManagerKey(final String projectName, final String region) {
        return projectName + "-" + region;
    }

    protected FakeResourceManager createResourceManager(final FakeOsSystem fos, final int regionIdx) {
        // return new FakeResourceManager(instanceNbr, imageNbr, volsPerVm, extraVols, volSizeMax,
        // sizeSteps, subsPerVm,
        // extraNets, subsPerExtraNets, rebootCount);
        return new FakeResourceManager2(fos, networkNbr, imageNbr, extraVols, volSizeMax, sizeSteps, subsPerNet,
                extraNets, vmPerSub, vmWithVolRatio, rebootCount);
    }

    // public methods
    public String[] getConfiguredRegions(final String projectName) {
        return regionMap.get(projectName);
    }

    public FakeResourceManager getResourceManager(final Item project, final String region) {
        return managerMap.get(getManagerKey(((AdapterItem) project).getCore().getItemName(), region));
    }

    public OsQuota getOsQuota(final String projectId) {
        return quotaMap.get(projectId);
    }

    // only for testing
    public int getTotalInstanceNbr() {
        FakeResourceManager frm = managerMap.values().iterator().next();
        return frm.getInstances().size() * regionNbr * projectNbr;
    }

    public ArrayList<FakeProject> getProjects() {
        return projects;
    }
}
