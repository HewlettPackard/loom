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

import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.hp.hpl.loom.adapter.os.fake.FakeConfig;


public class DiscoverConfig extends FakeConfig {

    private int[] workloadNbrPerType;
    protected int[] workloadTypeNbr;
    protected int[] networkNbr;
    protected int[] subnetPerNetworkNbr;
    protected int[] vmPerSubnetNbr;
    protected int[] vmWithVolumeRatio;

    @Override
    public void loadFromProperties(final PropertiesConfiguration props) {
        super.loadFromProperties(props);
        DataConfiguration config = new DataConfiguration(props);
        workloadNbrPerType = config.getIntArray("discover.data.workloadNbrPerType");
        workloadTypeNbr = config.getIntArray("discover.data.workloadTypeNbr");
        networkNbr = config.getIntArray("discover.data.networkNbr");
        subnetPerNetworkNbr = config.getIntArray("discover.data.subnetPerNetworkNbr");
        vmPerSubnetNbr = config.getIntArray("discover.data.vmPerSubnetNbr");
        vmWithVolumeRatio = config.getIntArray("discover.data.vmWithVolumeRatio");
    }

    public int getWorkloadNbrPerType(final int index) {
        return workloadNbrPerType[index];
    }

    public int getWorkloadTypeNbr(final int index) {
        return workloadTypeNbr[index];
    }

    @Override
    public int getNetworkNbr(final int index) {
        return networkNbr[index];
    }

    @Override
    public int getSubnetPerNetworkNbr(final int index) {
        return subnetPerNetworkNbr[index];
    }

    @Override
    public int getVmPerSubnetNbr(final int index) {
        return vmPerSubnetNbr[index];
    }

    @Override
    public int getVmWithVolumeRatio(final int index) {
        return vmWithVolumeRatio[index];
    }


}
