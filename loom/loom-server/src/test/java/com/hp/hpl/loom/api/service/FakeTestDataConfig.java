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
package com.hp.hpl.loom.api.service;

import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.os.fake.FakeConfig;

public class FakeTestDataConfig extends BaseTestDataConfig implements TestDataConfig {
    private static final Log LOG = LogFactory.getLog(FakeTestDataConfig.class);

    private int[] projectNbr, regionNbr, instanceNbr;
    private int[] expectedInstanceNbr, expectedVolumeNbr, expectedImageNbr, expectedNetworkNbr, expectedSubnetNbr;
    private int[] imageNbr, volsPerVm, extraVols, volSizeMax, sizeSteps, subsPerVm, extraNets, subsPerExtraNet;
    private String fakeAdapterProviderType, fakeAdapterProviderId, fakeAdapter2ProviderType, fakeAdapter2ProviderId;

    public FakeTestDataConfig() {
        super();
        DataConfiguration properties = new DataConfiguration(prop);
        // Load fake OS data parameters
        projectNbr = properties.getIntArray("fake.data.projectNbr");
        regionNbr = properties.getIntArray("fake.data.regionNbr");
        instanceNbr = properties.getIntArray("fake.data.instanceNbr");
        imageNbr = properties.getIntArray("fake.data.imageNbr");
        volsPerVm = properties.getIntArray("fake.data.volsPerVm");
        extraVols = properties.getIntArray("fake.data.extraVols");
        volSizeMax = properties.getIntArray("fake.data.volSizeMax");
        sizeSteps = properties.getIntArray("fake.data.sizeSteps");
        subsPerVm = properties.getIntArray("fake.data.subsPerVm");
        extraNets = properties.getIntArray("fake.data.extraNets");
        subsPerExtraNet = properties.getIntArray("fake.data.subsPerExtraNet");
        fakeAdapterProviderType = properties.getString("fake.adapter.provider.type");
        fakeAdapterProviderId = properties.getString("fake.adapter.provider.id");
        fakeAdapter2ProviderType = properties.getString("fake.adapter2.provider.type");
        fakeAdapter2ProviderId = properties.getString("fake.adapter2.provider.id");

        if (projectNbr.length != regionNbr.length) {
            String msg = "Config error: length of regionNbr config not same as projectNbr: " + regionNbr.length + " != "
                    + projectNbr.length;
            LOG.error(msg);
            throw new RuntimeException(msg);
        }

        // Calculate expected number of instances for each type of provider
        if (regionNbr.length != instanceNbr.length) {
            String msg = "Config error: length of regionNbr config not same as instanceNbr: " + regionNbr.length
                    + " != " + instanceNbr.length;
            LOG.error(msg);
            throw new RuntimeException(msg);
        }
        expectedInstanceNbr = new int[regionNbr.length];
        for (int count = 0; count < regionNbr.length; count++) {
            expectedInstanceNbr[count] = projectNbr[count] * regionNbr[count] * instanceNbr[count];
        }

        // volumes
        if ((regionNbr.length != volsPerVm.length) || (regionNbr.length != extraVols.length)) {
            String msg = "Config error: length of regionNbr config not same as volsPerVm or extraVols: "
                    + regionNbr.length + " != " + volsPerVm.length + " or != " + extraVols.length;
            LOG.error(msg);
            throw new RuntimeException(msg);
        }
        expectedVolumeNbr = new int[regionNbr.length];
        for (int count = 0; count < regionNbr.length; count++) {
            expectedVolumeNbr[count] =
                    (expectedInstanceNbr[count] * volsPerVm[count]) + (regionNbr[count] * extraVols[count]);
        }

        // images
        if (regionNbr.length != imageNbr.length) {
            String msg = "Config error: length of regionNbr config not same as imageNbr: " + regionNbr.length + " != "
                    + imageNbr.length;
            LOG.error(msg);
            throw new RuntimeException(msg);
        }
        expectedImageNbr = new int[regionNbr.length];
        for (int count = 0; count < regionNbr.length; count++) {
            expectedImageNbr[count] = projectNbr[count] * regionNbr[count] * imageNbr[count];
        }

        // networks
        if ((regionNbr.length != subsPerVm.length) || (regionNbr.length != extraNets.length)) {
            String msg = "Config error: length of regionNbr config not same as subsPerVm or extraNets: "
                    + regionNbr.length + " != " + subsPerVm.length + " or != " + extraNets.length;
            LOG.error(msg);
            throw new RuntimeException(msg);
        }
        expectedNetworkNbr = new int[regionNbr.length];
        for (int count = 0; count < regionNbr.length; count++) {
            expectedNetworkNbr[count] = (expectedInstanceNbr[count] * subsPerVm[count])
                    + (projectNbr[count] * regionNbr[count] * extraNets[count]);
        }

        // subnets
        if (regionNbr.length != subsPerExtraNet.length) {
            String msg = "Config error: length of regionNbr config not same as subsPerExtraNet: " + regionNbr.length
                    + " != " + subsPerExtraNet.length;
            LOG.error(msg);
            throw new RuntimeException(msg);
        }
        expectedSubnetNbr = new int[regionNbr.length];
        for (int count = 0; count < regionNbr.length; count++) {
            expectedSubnetNbr[count] =
                    ((extraNets[count] * subsPerExtraNet[count]) * projectNbr[count] * regionNbr[count])
                            + (expectedInstanceNbr[count] * subsPerVm[count]);
        }
    }

    @Override
    public String getProviderType(final int adapterIdx) {
        if (FakeConfig.PRIVATE_INDEX == adapterIdx) {
            return fakeAdapterProviderType;
        } else if (FakeConfig.PUBLIC_INDEX == adapterIdx) {
            return fakeAdapter2ProviderType;
        } else {
            return null;
        }
    }

    @Override
    public String getProviderId(final int adapterIdx) {
        if (FakeConfig.PRIVATE_INDEX == adapterIdx) {
            return fakeAdapterProviderId;
        } else if (FakeConfig.PUBLIC_INDEX == adapterIdx) {
            return fakeAdapter2ProviderId;
        } else {
            return null;
        }
    }

    @Override
    public int getExpectedInstanceNbr(final int adapterIdx) {
        return expectedInstanceNbr[adapterIdx];
    }

    @Override
    public int getExpectedVolumeNbr(final int adapterIdx) {
        return expectedVolumeNbr[adapterIdx];
    }

    @Override
    public int getExpectedImageNbr(final int adapterIdx) {
        return expectedImageNbr[adapterIdx];
    }

    @Override
    public int getExpectedNetworkNbr(final int adapterIdx) {
        return expectedNetworkNbr[adapterIdx];
    }

    @Override
    public int getExpectedSubnetNbr(final int adapterIdx) {
        return expectedSubnetNbr[adapterIdx];
    }

}
