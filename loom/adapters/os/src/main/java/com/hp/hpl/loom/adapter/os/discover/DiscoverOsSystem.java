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
package com.hp.hpl.loom.adapter.os.discover;

import java.util.Arrays;
import java.util.List;

import com.hp.hpl.loom.adapter.os.fake.FakeConfig;
import com.hp.hpl.loom.adapter.os.fake.FakeOsSystem;
import com.hp.hpl.loom.adapter.os.fake.FakeProject;
import com.hp.hpl.loom.adapter.os.fake.FakeResourceManager;

public class DiscoverOsSystem extends FakeOsSystem {

    protected int workloadNbrPerType;
    protected int workloadTypeNbr;
    protected int networkNbr;
    protected int subnetPerNetworkNbr;
    protected int vmPerSubnetNbr;
    protected int vmWithVolumeRatio;

    private String providerName;

    private String[] projectNames = {"Production", "Staging", "Dev-Test", "Dev", "Tools"};
    private String[] projectDescriptions = {"Live production service", "Staging for production", "Dev-Test environment",
            "Developer environment", "Tools support"};

    protected List<String> regionNames = Arrays.asList("AMS", "EMEA", "APJ");
    protected List<String> workloadTypeNames = Arrays.asList("EntSrch", "CDN", "Vertica", "IDOL", "Hadoop", "VDI");

    protected String[] workloadTypes;

    public DiscoverOsSystem(final FakeConfig fc, final int index, final String providerName) {
        super(fc, index);
        this.providerName = providerName;
        workloadNbrPerType = ((DiscoverConfig) fc).getWorkloadNbrPerType(index);
        workloadTypeNbr = ((DiscoverConfig) fc).getWorkloadTypeNbr(index);
        networkNbr = ((DiscoverConfig) fc).getNetworkNbr(index);
        subnetPerNetworkNbr = ((DiscoverConfig) fc).getSubnetPerNetworkNbr(index);
        vmPerSubnetNbr = ((DiscoverConfig) fc).getVmPerSubnetNbr(index);
        vmWithVolumeRatio = ((DiscoverConfig) fc).getVmWithVolumeRatio(index);
        workloadTypes = new String[workloadTypeNbr];
        for (int i = 0; i < workloadTypeNbr; ++i) {
            workloadTypes[i] = workloadTypeNames.get(i);
        }
    }

    @Override
    protected void setAllRegions() {
        if (regions.length == 0) {
            regions = new String[regionNbr];
            for (int i = 0; i < regionNbr; ++i) {
                regions[i] = regionNames.get(i) + " (" + providerName + ")";
            }
        }
    }

    String createInstanceName() {
        // Private Instances follow a simplified naming pattern that matches the one expected by the
        // slimos adapter, but other indexes include the index id to make sure all Instances
        // have unique names.
        String instanceName =
                adapterIdx == 0 ? "vm-" + getNextInstanceIndex() : "vm-" + adapterIdx + "-" + getNextInstanceIndex();
        return instanceName;
    }

    /*
     * protected void setProjects() { OsProjectType prjType = new OsProjectType(); for (int i = 0; i
     * < projectNbr; ++i) { OsProject osprj = new OsProject("project-" + i, projectNames[i %
     * projectNames.length] + " (" + providerName + ")", "project-" + i, prjType,
     * projectDescriptions[i]); projects.add(osprj); } }
     */

    @Override
    protected void setProjects() {
        for (int i = 0; i < projectNbr; ++i) {
            FakeProject osprj = new FakeProject(projectNames[i % projectNames.length] + " (" + providerName + ")",
                    "project-" + i, projectDescriptions[i]);
            projects.add(osprj);
        }
    }

    @Override
    protected FakeResourceManager createResourceManager(final FakeOsSystem fos, final int regionIdx) {
        int workloadStartIdx = workloadNbrPerType * regionIdx;
        return new DiscoverResourceManager(this, imageNbr, volSizeMax, sizeSteps, rebootCount, workloadNbrPerType,
                workloadTypes, networkNbr, subnetPerNetworkNbr, vmPerSubnetNbr, vmWithVolumeRatio, workloadStartIdx);
    }

    // only for testing
    @Override
    public int getTotalInstanceNbr() {
        return networkNbr * subnetPerNetworkNbr * vmPerSubnetNbr * regionNbr * projectNbr;
    }
}
