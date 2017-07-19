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

import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;

public class FakeConfig {

    public static final int PRIVATE_INDEX = 0;
    public static final int PUBLIC_INDEX = 1;

    private int index;

    // common values
    private int[] projectNbr;
    // alll regions
    private int[] regionNbr;
    // per region per project
    private int[] imageNbr;
    private int[] extraVols;
    private int[] volSizeMax;
    private int[] sizeSteps;
    private int[] extraNets;
    private int[] rebootCount;

    // discover like data generation attrs
    private int[] networkNbr;
    private int[] subnetPerNetworkNbr;
    private int[] vmPerSubnetNbr;
    private int[] vmWithVolumeRatio;

    // old data generation attrs
    private int[] instanceNbr;
    private int[] volsPerVm;
    private int[] subsPerVm;
    private int[] subsPerExtraNets;

    // quotas
    private int[] instanceQuota;
    private int[] coreQuota;
    private int[] volumeQuota;
    private int[] gigabyteQuota;
    private int[] ramQuota;
    private int quotaRepeatNbr;

    public void loadFromProperties(final PropertiesConfiguration props) {
        DataConfiguration config = new DataConfiguration(props);

        projectNbr = config.getIntArray("fake.data.projectNbr");

        // all regions
        regionNbr = config.getIntArray("fake.data.regionNbr");
        // per region per project
        imageNbr = config.getIntArray("fake.data.imageNbr");
        extraVols = config.getIntArray("fake.data.extraVols");
        volSizeMax = config.getIntArray("fake.data.volSizeMax");
        sizeSteps = config.getIntArray("fake.data.sizeSteps");
        extraNets = config.getIntArray("fake.data.extraNets");
        rebootCount = config.getIntArray("fake.data.rebootCount");


        // discover like data generation attrs
        networkNbr = config.getIntArray("fake.data.networkNbr");
        subnetPerNetworkNbr = config.getIntArray("fake.data.subnetPerNetworkNbr");
        vmPerSubnetNbr = config.getIntArray("fake.data.vmPerSubnetNbr");
        vmWithVolumeRatio = config.getIntArray("fake.data.vmWithVolumeRatio");


        // old data generation attrs
        instanceNbr = config.getIntArray("fake.data.instanceNbr");
        volsPerVm = config.getIntArray("fake.data.volsPerVm");
        subsPerVm = config.getIntArray("fake.data.subsPerVm");
        subsPerExtraNets = config.getIntArray("fake.data.subsPerExtraNet");


        instanceQuota = config.getIntArray("fake.quota.instances");
        coreQuota = config.getIntArray("fake.quota.cores");
        volumeQuota = config.getIntArray("fake.quota.volumes");
        gigabyteQuota = config.getIntArray("fake.quota.gigabytes");
        ramQuota = config.getIntArray("fake.quota.ram");

        quotaRepeatNbr = config.getInt("fake.quota.repeatNbr");

        index = config.getInt("fake.index", 0);
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return the projectNbr
     */
    public int getProjectNbr(final int index) {
        return projectNbr[index];
    }

    /**
     * @return the regionNbr
     */
    public int getRegionNbr(final int index) {
        return regionNbr[index];
    }

    /**
     * @return the imageNbr
     */
    public int getImageNbr(final int index) {
        return imageNbr[index];
    }

    /**
     * @return the extraVols
     */
    public int getExtraVols(final int index) {
        return extraVols[index];
    }

    /**
     * @return the volSizeMax
     */
    public int getVolSizeMax(final int index) {
        return volSizeMax[index];
    }

    /**
     * @return the sizeSteps
     */
    public int getSizeSteps(final int index) {
        return sizeSteps[index];
    }

    /**
     * @return the extraNets
     */
    public int getExtraNets(final int index) {
        return extraNets[index];
    }

    /**
     * @return the rebootCount
     */
    public int getRebootCount(final int index) {
        return rebootCount[index];
    }

    /**
     * @return the networkNbr
     */
    public int getNetworkNbr(final int index) {
        return networkNbr[index];
    }

    /**
     * @return the subnetPerNetworkNbr
     */
    public int getSubnetPerNetworkNbr(final int index) {
        return subnetPerNetworkNbr[index];
    }

    /**
     * @return the vmPerSubnetNbr
     */
    public int getVmPerSubnetNbr(final int index) {
        return vmPerSubnetNbr[index];
    }

    /**
     * @return the vmWithVolumeRatio
     */
    public int getVmWithVolumeRatio(final int index) {
        return vmWithVolumeRatio[index];
    }

    /**
     * @return the instanceNbr
     */
    public int getInstanceNbr(final int index) {
        return instanceNbr[index];
    }

    /**
     * @return the volsPerVm
     */
    public int getVolsPerVm(final int index) {
        return volsPerVm[index];
    }

    /**
     * @return the subsPerVm
     */
    public int getSubsPerVm(final int index) {
        return subsPerVm[index];
    }

    /**
     * @return the subsPerExtraNets
     */
    public int getSubsPerExtraNets(final int index) {
        return subsPerExtraNets[index];
    }

    /**
     * @return the instanceQuota
     */
    public int getInstanceQuota(final int index) {
        return instanceQuota[index];
    }

    /**
     * @return the coreQuota
     */
    public int getCoreQuota(final int index) {
        return coreQuota[index];
    }

    /**
     * @return the volumeQuota
     */
    public int getVolumeQuota(final int index) {
        return volumeQuota[index];
    }

    /**
     * @return the gigabyteQuota
     */
    public int getGigabyteQuota(final int index) {
        return gigabyteQuota[index];
    }

    /**
     * @return the ramQuota
     */
    public int getRamQuota(final int index) {
        return ramQuota[index];
    }

    /**
     * @return the quotaRepeatNbr
     */
    public int getQuotaRepeatNbr() {
        return quotaRepeatNbr;
    }


}
