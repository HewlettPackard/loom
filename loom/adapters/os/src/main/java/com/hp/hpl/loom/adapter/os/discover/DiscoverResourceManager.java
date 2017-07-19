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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.hp.hpl.loom.adapter.os.fake.FakeInstance;
import com.hp.hpl.loom.adapter.os.fake.FakeNetwork;
import com.hp.hpl.loom.adapter.os.fake.FakeResourceManager;
import com.hp.hpl.loom.adapter.os.fake.FakeSubnet;
import com.hp.hpl.loom.adapter.os.fake.FakeVolume;

public class DiscoverResourceManager extends FakeResourceManager {

    private DiscoverOsSystem dos;
    private int workloadNbrPerType;
    private String[] workloadTypes;
    private int networkNbr;
    private int subnetPerNetworkNbr;
    private int vmPerSubnetNbr;
    private int vmWithVolumeRatio;
    private ArrayList<FakeWorkload> workloads;
    private int workloadStartIdx;

    public DiscoverResourceManager(final DiscoverOsSystem dos, final int imageNbr, final int volSizeMax,
            final int sizeSteps, final int rebootCount, final int workloadNbrPerType, final String[] workloadTypes,
            final int networkNbr, final int subnetPerNetworkNbr, final int vmPerSubnetNbr, final int vmWithVolumeRatio,
            final int workloadStartIdx) {
        super();
        this.dos = dos;
        this.imageNbr = imageNbr;
        this.volSizeMax = volSizeMax;
        this.sizeSteps = sizeSteps;
        this.rebootCount = rebootCount;
        this.workloadNbrPerType = workloadNbrPerType;
        this.workloadTypes = workloadTypes;
        this.networkNbr = networkNbr;
        this.subnetPerNetworkNbr = subnetPerNetworkNbr;
        this.vmPerSubnetNbr = vmPerSubnetNbr;
        this.vmWithVolumeRatio = vmWithVolumeRatio;
        this.workloadStartIdx = workloadStartIdx;
        workloads = new ArrayList<>();
        init();
        initWorkload();
    }

    @Override
    protected void init() {
        // create images
        for (int i = 0; i < imageNbr; ++i) {
            images.add(createImage(i));
        }
        int volIdx = 0;
        for (int i = 0; i < networkNbr; ++i) {
            FakeNetwork fn = createNetwork(i);
            networks.add(fn);
            for (int j = 0; j < subnetPerNetworkNbr; ++j) {
                FakeSubnet fs = createSubnet(subnetPerNetworkNbr * i + j);
                subnets.add(fs);
                fn.addSubnetId(fs.getItemId());
                for (int z = 0; z < vmPerSubnetNbr; ++z) {
                    int imageIdx = randGen.nextInt(imageNbr);
                    int flavourNbr = randGen.nextInt(flavours.length);
                    FakeInstance fi = createInstance(vmPerSubnetNbr * (subnetPerNetworkNbr * i + j) + z,
                            images.get(imageIdx).getItemId(), flavourNbr);
                    instances.add(fi);
                    fs.addInstanceId(fi.getItemId());
                    fi.addSubnet(fs);
                    // for each VM decide if we attach a volume or not
                    boolean needVol = randGen.nextInt(100) < vmWithVolumeRatio;
                    if (needVol) {
                        FakeVolume fv = createVolume(volIdx++);
                        fv.addAttachment(fi.getItemId());
                        // to be able to delete
                        fi.addVolume(fv);
                        volumes.add(fv);
                    }
                }
            }
        }
    }

    @Override
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    protected FakeInstance createInstance(final int idx, final String imageId, final int flavourNbr) {

        String date = new Date().toString();
        FakeInstance fi = new FakeInstance(dos.createInstanceName() /* idx */, assignUuid(), imageId, flavourNbr,
                "1.2.3.4", "0001:0002:0003:0004:0005%06", date, date, "ACTIVE",
                "16d193736a5cfdb60c697ca27ad071d6126fa13baeb670fc9d10645e");
        instanceMap.put(fi.getItemId(), fi);
        return fi;
    }

    private void initWorkload() {
        for (int i = 0; i < workloadTypes.length; ++i) {
            for (int j = workloadStartIdx; j < (workloadStartIdx + workloadNbrPerType); ++j) {
                workloads.add(createWorkload(i, j));
            }
        }
        for (int i = 0; i < instances.size(); ++i) {
            int workloadIdx = randGen.nextInt(workloads.size());
            // workloads.get(workloadIdx).addInstance(instances.get(i));
            workloads.get(workloadIdx).addInstance(instances.get(i));
        }
    }

    private FakeWorkload createWorkload(final int typeIdx, final int workloadIdx) {
        String workloadPrefix = workloadTypes[typeIdx] + "-";
        FakeWorkload fw = new FakeWorkload(workloadPrefix + workloadIdx, assignUuid());
        fw.setWorkloadType(workloadTypes[typeIdx]);
        return fw;
    }

    public Collection<FakeWorkload> getWorkloads() {
        return workloads;
    }
}
