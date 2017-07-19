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

import java.util.Date;

public class FakeResourceManager2 extends FakeResourceManager {

    protected int networkNbr;
    protected int subsPerNet;
    protected int vmPerSub;
    protected int vmWithVolRatio;
    protected int numInstances;

    private FakeOsSystem fos;

    public FakeResourceManager2(final FakeOsSystem fos, final int networkNbr, final int imageNbr, final int extraVols,
            final int volSizeMax, final int sizeSteps, final int subsPerNet, final int extraNets, final int vmPerSub,
            final int vmWithVolRatio, final int rebootCount) {
        super();
        this.networkNbr = networkNbr;
        this.imageNbr = imageNbr;
        this.extraVols = extraVols;
        this.volSizeMax = volSizeMax;
        this.sizeSteps = sizeSteps;
        this.subsPerNet = subsPerNet;
        this.extraNets = extraNets;
        this.vmPerSub = vmPerSub;
        this.vmWithVolRatio = vmWithVolRatio;
        this.rebootCount = rebootCount;
        this.fos = fos;
        init();
    }

    @Override
    protected void init() {
        // create images
        for (int i = 0; i < imageNbr; ++i) {
            images.add(createImage(i));
        }
        for (int i = 0; i < networkNbr; ++i) {
            FakeNetwork fn = createNetwork(i);
            networks.add(fn);
            for (int j = 0; j < subsPerNet; ++j) {
                FakeSubnet fs = createSubnet(subsPerNet * i + j);
                subnets.add(fs);
                fn.addSubnetId(fs.getItemId());
                for (int z = 0; z < vmPerSub; ++z) {
                    int imageIdx = randGen.nextInt(imageNbr);
                    int flavourNbr = randGen.nextInt(flavours.length);
                    FakeInstance fi = createInstance(vmPerSub * (subsPerNet * i + j) + z,
                            images.get(imageIdx).getItemId(), flavourNbr);
                    instances.add(fi);
                    fs.addInstanceId(fi.getItemId());
                    fi.addSubnet(fs);
                }
            }
        }
        // create extra networks/subnets w/o VMs
        int startIdx = networks.size();
        for (int i = startIdx; i < (startIdx + extraNets); ++i) {
            FakeNetwork fn = createNetwork(i);
            networks.add(fn);
            for (int j = 0; j < subsPerNet; ++j) {
                FakeSubnet fs = createSubnet(subsPerNet * i + j);
                subnets.add(fs);
                fn.addSubnetId(fs.getItemId());
            }
        }
        // create and attach a volume for some instances
        int vmNbr = instances.size();
        int volNbr = (vmNbr * vmWithVolRatio) / 100;
        for (int i = 0; i < volNbr; ++i) {
            FakeInstance fi = instances.get(i);
            FakeVolume fv = createVolume(i);
            fv.addAttachment(fi.getItemId());
            // to be able to delete
            fi.addVolume(fv);
            volumes.add(fv);
        }
        // create extra unattached vols
        startIdx = volumes.size();
        for (int i = 0; i < extraVols; ++i) {
            FakeVolume fv = createVolume(startIdx + i);
            volumes.add(fv);
        }
    }


    @Override
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    protected FakeInstance createInstance(final int idx, final String imageId, final int flavourNbr) {

        String date = new Date().toString();
        FakeInstance fi = new FakeInstance("vm-" + fos.getNextInstanceIndex() /* + idx */, assignUuid(), imageId,
                flavourNbr, "1.2.3.4", "0001:0002:0003:0004:0005%06", date, date, "ACTIVE",
                "16d193736a5cfdb60c697ca27ad071d6126fa13baeb670fc9d10645e");
        instanceMap.put(fi.getItemId(), fi);
        return fi;
    }

}
