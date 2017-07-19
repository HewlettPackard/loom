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
package com.hp.hpl.loom.adapter.os.deltas;

import com.hp.hpl.loom.adapter.os.fake.FakeConfig;
import com.hp.hpl.loom.adapter.os.fake.FakeOsSystem;
import com.hp.hpl.loom.adapter.os.fake.FakeProject;
import com.hp.hpl.loom.adapter.os.fake.FakeResourceManager;

public class DeltaOsSystem extends FakeOsSystem {

    protected int networkNbr;
    protected int subnetPerNetworkNbr;
    protected int vmPerSubnetNbr;
    protected int vmWithVolumeRatio;

    // private String providerName;

    protected String[] workloadTypes;

    public DeltaOsSystem(final FakeConfig fc, final int index) {
        super(fc, index);
        // this.providerName = providerName;
        networkNbr = fc.getNetworkNbr(index);
        subnetPerNetworkNbr = fc.getSubnetPerNetworkNbr(index);
        vmPerSubnetNbr = fc.getVmPerSubnetNbr(index);
        vmWithVolumeRatio = fc.getVmWithVolumeRatio(index);
    }


    @Override
    protected void setAllRegions() {
        if (regions.length == 0) {
            regions = new String[regionNbr];
            for (int i = 0; i < regionNbr; ++i) {
                regions[i] = "region-" + i;
            }
        }
    }

    @Override
    protected void setProjects() {
        for (int i = 0; i < projectNbr; ++i) {
            FakeProject osprj = new FakeProject("project-" + i, "prj_" + i, "fake project number " + i);
            projects.add(osprj);
        }
    }


    @Override
    protected FakeResourceManager createResourceManager(final FakeOsSystem fos, final int regionIdx) {
        return new DeltaResourceManager(imageNbr, volSizeMax, sizeSteps, rebootCount);
    }

    public DeltaResourceManager getFirstResourceManager(final String prjName, final String regName) {
        return (DeltaResourceManager) managerMap.get(getManagerKey(prjName, regName));
    }

    // only for testing
    @Override
    public int getTotalInstanceNbr() {
        return networkNbr * subnetPerNetworkNbr * vmPerSubnetNbr * regionNbr * projectNbr;
    }
}
