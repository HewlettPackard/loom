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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.os.OsFlavour;

public class FakeResourceManager {

    private static final Log LOG = LogFactory.getLog(FakeResourceManager.class);

    protected int instanceNbr;
    protected int imageNbr;
    protected int volsPerVm;
    protected int extraVols;
    protected int volSizeMax;
    protected int sizeSteps;
    protected int subsPerVm;
    protected int extraNets;
    protected int subsPerExtraNets;
    protected int rebootCount;

    protected ArrayList<FakeImage> images;
    protected ArrayList<FakeInstance> instances;
    protected HashMap<String, FakeInstance> instanceMap;
    protected ArrayList<FakeVolume> volumes;
    protected ArrayList<FakeSubnet> subnets;
    protected ArrayList<FakeNetwork> networks;
    // fake flavours are always the same for now
    protected OsFlavour[] flavours = {new OsFlavour("2", "m1.small", 20, 2048, 1),
            new OsFlavour("3", "m1.medium", 80, 4096, 2), new OsFlavour("4", "m1.large", 100, 12288, 4)};
    protected Random randGen;
    private int randomSeed = 12;
    protected ArrayList<FakeInstance> rebootedVms;

    public FakeResourceManager() {
        images = new ArrayList<>();
        instances = new ArrayList<>();
        instanceMap = new HashMap<>();
        volumes = new ArrayList<>();
        subnets = new ArrayList<>();
        networks = new ArrayList<>();
        randGen = new Random(randomSeed);
        rebootedVms = new ArrayList<>();
    }

    public FakeResourceManager(final int instanceNbr, final int imageNbr, final int volsPerVm, final int extraVols,
            final int volSizeMax, final int sizeSteps, final int subsPerVm, final int extraNets,
            final int subsPerExtraNets, final int rebootCount) {
        this();
        this.instanceNbr = instanceNbr;
        this.imageNbr = imageNbr;
        this.volsPerVm = volsPerVm;
        this.extraVols = extraVols;
        this.volSizeMax = volSizeMax;
        this.sizeSteps = sizeSteps;
        this.subsPerVm = subsPerVm;
        this.extraNets = extraNets;
        this.subsPerExtraNets = subsPerExtraNets;
        this.rebootCount = rebootCount;
        init();
    }

    protected void init() {
        // create images
        for (int i = 0; i < imageNbr; ++i) {
            images.add(createImage(i));
        }
        // create instances and attached volumes
        for (int i = 0; i < instanceNbr; ++i) {
            int imageIdx = randGen.nextInt(imageNbr);
            int flavourNbr = randGen.nextInt(flavours.length);
            instances.add(createInstance(i, images.get(imageIdx).getItemId(), flavourNbr));
        }
        // link created volumes to another instance - ManyToMany & loop breaking
        // test
        for (int i = 0; i < volumes.size(); ++i) {
            FakeVolume vol = volumes.get(i);
            int linkVmIdx = i / volsPerVm + 1;
            if (linkVmIdx == instanceNbr) {
                linkVmIdx = 0;
            }
            FakeInstance linkVm = instances.get(linkVmIdx);
            vol.addAttachment(linkVm.getItemId());
            linkVm.addVolume(vol);
        }
        // create extra volumes
        int startIdx = volumes.size();
        for (int i = startIdx; i < (startIdx + extraVols); ++i) {
            volumes.add(createVolume(i));
        }
        // create extra Networks and subnets
        startIdx = networks.size();
        for (int i = startIdx; i < (startIdx + extraNets); ++i) {
            FakeNetwork fn = createNetwork(i);
            for (int j = 0; j < subsPerExtraNets; ++j) {
                FakeSubnet fs = createSubnet(subsPerExtraNets * i + j);
                fn.addSubnetId(fs.getItemId());
                subnets.add(fs);
            }
            networks.add(fn);
        }
    }

    protected String assignUuid() {
        return java.util.UUID.randomUUID().toString();
    }

    // private FakeResource createResource(String resBase, int idx) {
    // return new FakeResource(resBase + idx, assignUuid());
    // }
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    protected FakeInstance createInstance(final int idx, final String imageId, final int flavourNbr) {

        String date = new Date().toString();
        FakeInstance fi = new FakeInstance("vm-" + idx, assignUuid(), imageId, flavourNbr, "1.2.3.4",
                "0001:0002:0003:0004:0005%06", date, date, "ACTIVE",
                "16d193736a5cfdb60c697ca27ad071d6126fa13baeb670fc9d10645e");
        instanceMap.put(fi.getItemId(), fi);
        for (int i = 0; i < volsPerVm; ++i) {
            FakeVolume fv = createVolume(volsPerVm * idx + i);
            fv.addAttachment(fi.getItemId());
            // to be able to delete
            fi.addVolume(fv);
            volumes.add(fv);
        }
        for (int i = 0; i < subsPerVm; ++i) {
            FakeSubnet fs = createSubnet(subsPerVm * idx + i);
            FakeNetwork fn = createNetwork(subsPerVm * idx + i);
            fn.addSubnetId(fs.getItemId());
            fs.addInstanceId(fi.getItemId());
            fi.addSubnet(fs);
            subnets.add(fs);
            networks.add(fn);
        }
        // log.trace("creating instance: "+idx);
        return fi;
    }

    protected FakeImage createImage(final int idx) {
        String date = new Date().toString();
        // log.trace("creating image: "+idx);
        return new FakeImage("img-" + idx, assignUuid(), date, date, "ACTIVE", 0, 0);

    }

    protected FakeVolume createVolume(final int idx) {
        int size = (randGen.nextInt(sizeSteps) + 1) * volSizeMax;
        return new FakeVolume("vol-" + idx, assignUuid(), size, "AVAILABLE", "zone", new Date().toString(), "None",
                null, "A volume description");
    }

    protected FakeNetwork createNetwork(final int idx) {
        return new FakeNetwork("net-" + idx, assignUuid());
    }

    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    protected FakeSubnet createSubnet(final int idx) {
        return new FakeSubnet("sub-" + idx, assignUuid(), true, assignUuid(), "192.0.0.1", 4, "192.0.0.0/8");
    }

    // public methods
    public Collection<FakeInstance> getInstances() {
        ArrayList<FakeInstance> rebootClone = new ArrayList<>(rebootedVms);
        for (FakeInstance fi : rebootClone) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("ACTION-DEBUG - Decrementing rebootCount");
            }

            if (fi.decRebootCount()) {
                // reboot is done
                fi.start();
                rebootedVms.remove(fi);
            }
        }
        return instances;
    }

    public Collection<FakeImage> getImages() {
        return images;
    }

    public Collection<FakeVolume> getVolumes() {
        return volumes;
    }

    public Collection<FakeSubnet> getSubnets() {
        return subnets;
    }

    public Collection<FakeNetwork> getNetworks() {
        return networks;
    }

    public OsFlavour[] getFlavours() {
        return flavours;
    }

    public boolean doAction(final String action, final String vmId) {
        boolean retVal = false;
        FakeInstance fi = instanceMap.get(vmId);
        if (LOG.isDebugEnabled()) {
            LOG.debug("ACTION-DEBUG - received action: " + action + "for vmId: " + vmId);
        }
        if (fi != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("ACTION-DEBUG: FRM found it!");
            }

            if ("start".equals(action)) {

                if (fi.getStatus().equals("ACTIVE")) {
                    retVal = true;
                } else {
                    retVal = fi.start();
                }
            } else if ("stop".equals(action)) {
                if (fi.getStatus().equals("SHUTOFF")) {
                    retVal = true;
                } else {
                    retVal = fi.stop();
                }
            } else if (action.contains("Reboot")) {
                retVal = fi.stop();

                if (LOG.isDebugEnabled()) {
                    LOG.debug("ACTION-DEBUG - setting rebootCount to: " + rebootCount);
                }

                fi.setRebootCount(rebootCount);
                rebootedVms.add(fi);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("ACTION-DEBUG: after action, powerStatus: " + fi.getStatus() + "; action has been executed? "
                        + retVal);
            }
        }
        return retVal;
    }

    public void addInstances(final int nbr) {
        // create instances and attached volumes/subnets/networks
        int startIdx = instances.size();
        for (int i = startIdx; i < (startIdx + nbr); ++i) {
            int imageIdx = randGen.nextInt(imageNbr);
            int flavourNbr = randGen.nextInt(flavours.length);
            instances.add(createInstance(i, images.get(imageIdx).getItemId(), flavourNbr));
        }
    }

    // ONLY delet the instances not the other items created with it
    public void deleteInstances(final int nbr) {
        int delNbr;
        if (instances.size() < nbr) {
            delNbr = instances.size();
        } else {
            delNbr = nbr;
        }
        ArrayList<FakeInstance> delVms = new ArrayList<>(delNbr);
        for (int i = 0; i < delNbr; ++i) {
            delVms.add(instances.get(i));
        }


        for (FakeInstance fi : delVms) {
            for (FakeVolume vol : fi.getVolumes()) {
                vol.removeAttachment(fi.getItemId());
            }
            for (FakeSubnet sub : fi.getSubnets()) {
                sub.removeInstanceIds(fi.getItemId());
            }
            instances.remove(fi);
        }
    }
}
