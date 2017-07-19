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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.loom.adapter.os.fake.FakeImage;
import com.hp.hpl.loom.adapter.os.fake.FakeInstance;
import com.hp.hpl.loom.adapter.os.fake.FakeResourceManager;
import com.hp.hpl.loom.adapter.os.fake.FakeVolume;

public class DeltaResourceManager extends FakeResourceManager {


    // private int networkNbr;
    // private int subnetPerNetworkNbr;
    // private int vmPerSubnetNbr;
    // private int vmWithVolumeRatio;

    private Map<Integer, TestMapHolder> dataMap = new HashMap<>();

    private int tickCount = 0;
    private int firstNbr = 5;
    private TestMapHolder tmh;
    private TestMapHolder tmh2;
    private TestMapHolder tmh3;
    private TestMapHolder tmh4;
    private TestMapHolder tmh5;
    private TestMapHolder tmh6;

    public DeltaResourceManager(final int imageNbr, final int volSizeMax, final int sizeSteps, final int rebootCount) {
        super();
        this.imageNbr = imageNbr;
        this.volSizeMax = volSizeMax;
        this.sizeSteps = sizeSteps;
        this.rebootCount = rebootCount;
        // this.networkNbr = networkNbr;
        // this.subnetPerNetworkNbr = subnetPerNetworkNbr;
        // this.vmPerSubnetNbr = vmPerSubnetNbr;
        // this.vmWithVolumeRatio = vmWithVolumeRatio;
        init();
    }

    // private boolean isDeltaMode(final int i) {
    // // return false;
    // // return (i % 2 == 0);
    // return true;
    // }

    @Override
    protected void init() {
        for (int i = 0; i < firstNbr; ++i) {
            tmh = new TestMapHolder();
            tmh.setAggCount(7);
            tmh.setProject(1, 1, 0);
            tmh.setRegion(1, 1, 0);
            tmh.setSubnet(0, 0);
            tmh.setNetwork(0, 0);
            images.add(createImage(i));
            int imageIdx = 0;
            int flavourNbr = 1;
            instances.add(createInstance(i, images.get(imageIdx).getItemId(), flavourNbr));
            volumes.add(createVolume(i));
            tmh.setImage(firstNbr, firstNbr, 0, 0);
            tmh.setInstance(firstNbr, firstNbr, 0, 0, 0, 0);
            tmh.setVolume(firstNbr, firstNbr, 0, 0, 0, 0);
            dataMap.put(1, tmh);
        }
    }

    @Override
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    protected FakeInstance createInstance(final int idx, final String imageId, final int flavourNbr) {

        String date = new Date().toString();
        FakeInstance fi = new FakeInstance("vm-" + idx, assignUuid(), imageId, flavourNbr, "1.2.3.4",
                "0001:0002:0003:0004:0005%06", date, date, "ACTIVE",
                "16d193736a5cfdb60c697ca27ad071d6126fa13baeb670fc9d10645e");
        instanceMap.put(fi.getItemId(), fi);
        return fi;
    }

    @Override
    public Collection<FakeInstance> getInstances() {
        super.getInstances();
        return instances;
    }

    @Override
    public Collection<FakeImage> getImages() {
        // IMAGES ARE COLLECTED FIRST
        processNewTick();
        return images;
    }

    @Override
    public Collection<FakeVolume> getVolumes() {
        return volumes;
    }

    private void processNewTick() {
        if (tickCount == 2) {
            // add 4 more instances
            for (int i = firstNbr; i < firstNbr + 4; ++i) {
                int imageIdx = 0;
                int flavourNbr = 1;
                instances.add(createInstance(i, images.get(imageIdx).getItemId(), flavourNbr));
            }
            tmh2 = tmh.copy();
            // if (isDeltaMode(tickCount)) {
            tmh2.setAggCount(4);
            // }
            tmh2.setProject(1, 0, 1);
            tmh2.setRegion(1, 0, 1);
            tmh2.setInstance(firstNbr + 4, 4, 0, 0, 0, 12);
            tmh2.setImage(5, 0, 1, 0);
            tmh2.setVolume(firstNbr, 0, 0, 0, 0, 0);
            dataMap.put(2, tmh2);
        }
        if (tickCount == 3) {
            // add 4 more volumes
            for (int i = firstNbr; i < firstNbr + 4; ++i) {
                volumes.add(createVolume(i));
            }
            // link all volumes 1 by 1 to an instance
            for (int i = 0; i < firstNbr + 4; ++i) {
                FakeVolume vol = volumes.get(i);
                FakeInstance linkVm = instances.get(i);
                vol.addAttachment(linkVm.getItemId());
                linkVm.addVolume(vol);
            }
            tmh3 = tmh2.copy();
            // reldelta: pervol 1 reg, 1proj ->8
            // plus 1 for each linked instanc: 9 -> total=17
            tmh3.setVolume(firstNbr + 4, 4, 5, 0, 0, 17);
            tmh3.setInstance(firstNbr + 4, 0, 9, 0, 0, 0);
            tmh3.setImage(5, 0, 0, 0);
            // if (isDeltaMode(tickCount)) {
            tmh3.setAggCount(4);
            // }
            dataMap.put(3, tmh3);
        }
        if (tickCount == 4) {
            // delete 5 volumes
            for (int i = 0; i < 5; ++i) {
                FakeVolume vol = volumes.remove(i);
                for (String id : vol.getAttachments()) {
                    FakeInstance fi = instanceMap.get(id);
                    fi.removeVolume(vol);
                }
            }
            tmh4 = tmh3.copy();
            // when deleted no relations deltas are issued
            tmh4.setVolume(4, 0, 0, 5, 5, 0);
            tmh4.setProject(1, 0, 0);
            tmh4.setRegion(1, 0, 0);
            tmh4.setInstance(firstNbr + 4, 0, 0, 0, 0, 0);
            // if (isDeltaMode(tickCount)) {
            tmh4.setAggCount(1);
            // }
            dataMap.put(4, tmh4);
        }
        if (tickCount == 5) {
            // remove remaining relationships
            for (int i = 0; i < 4; ++i) {
                FakeVolume vol = volumes.get(i);
                ArrayList<String> vmIds = new ArrayList<>(vol.getAttachments());
                for (String id : vmIds) {
                    vol.removeAttachment(id);
                    FakeInstance fi = instanceMap.get(id);
                    fi.removeVolume(vol);
                }
            }
            tmh5 = tmh4.copy();
            // 4 clear relDeltas, one for each volume
            tmh5.setVolume(4, 0, 4, 0, 0, 4);
            tmh5.setInstance(firstNbr + 4, 0, 4, 0, 0, 0);
            // if (isDeltaMode(tickCount)) {
            tmh5.setAggCount(2);
            // }
            dataMap.put(5, tmh5);
        }
        if (tickCount == 6) {
            // remove all instances
            deleteInstances(9);
            tmh6 = tmh5.copy();
            tmh6.setVolume(4, 0, 0, 0, 0, 0);
            tmh6.setInstance(0, 0, 0, 9, 9, 0);
            // if (isDeltaMode(tickCount)) {
            tmh6.setAggCount(1);
            // }
            dataMap.put(6, tmh6);
        }
        ++tickCount;
    }

    public Map<Integer, TestMapHolder> getDataMap() {
        return dataMap;
    }
}
