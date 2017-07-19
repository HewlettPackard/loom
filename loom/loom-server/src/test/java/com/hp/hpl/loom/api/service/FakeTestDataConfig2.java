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

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.os.fake.FakeConfig;

public class FakeTestDataConfig2 extends BaseTestDataConfig implements TestDataConfig {
    private static final Log LOG = LogFactory.getLog(FakeTestDataConfig2.class);

    private int[] projectNbr, regionNbr;
    private int[] expectedInstanceNbr, expectedVolumeNbr, expectedImageNbr, expectedNetworkNbr, expectedSubnetNbr;
    private int[] networkNbr, imageNbr, vmPerSub, extraVols, volSizeMax, sizeSteps, subsPerNet, extraNets,
            vmWithVolRatio;
    private String privateAdapterProviderType, privateAdapterProviderId, publicAdapterProviderType,
            publicAdapterProviderId;

    public FakeTestDataConfig2() {
        super();

        try {
            DataConfiguration privateAdapterProperties = new DataConfiguration(
                    new PropertiesConfiguration("src/test/resources/adapterConfigs/fakeAdapterPrivate.properties"));
            DataConfiguration publicAdapterProperties = new DataConfiguration(
                    new PropertiesConfiguration("src/test/resources/adapterConfigs/fakeAdapterPublic.properties"));

            // Load fake OS data parameters
            projectNbr = privateAdapterProperties.getIntArray("fake.data.projectNbr");
            regionNbr = privateAdapterProperties.getIntArray("fake.data.regionNbr");
            networkNbr = privateAdapterProperties.getIntArray("fake.data.networkNbr");
            imageNbr = privateAdapterProperties.getIntArray("fake.data.imageNbr");
            vmPerSub = privateAdapterProperties.getIntArray("fake.data.vmPerSubnetNbr");
            extraVols = privateAdapterProperties.getIntArray("fake.data.extraVols");
            volSizeMax = privateAdapterProperties.getIntArray("fake.data.volSizeMax");
            sizeSteps = privateAdapterProperties.getIntArray("fake.data.sizeSteps");
            subsPerNet = privateAdapterProperties.getIntArray("fake.data.subnetPerNetworkNbr");
            extraNets = privateAdapterProperties.getIntArray("fake.data.extraNets");
            vmWithVolRatio = privateAdapterProperties.getIntArray("fake.data.vmWithVolumeRatio");
            privateAdapterProviderType = privateAdapterProperties.getString("providerType");
            privateAdapterProviderId = privateAdapterProperties.getString("providerId");
            publicAdapterProviderType = publicAdapterProperties.getString("providerType");
            publicAdapterProviderId = publicAdapterProperties.getString("providerId");
        } catch (ConfigurationException e) {
            throw new RuntimeException("Can't manually load fake adapter properties");
        }

        if (projectNbr.length != regionNbr.length) {
            String msg = "Config error: length of regionNbr config not same as projectNbr: " + regionNbr.length + " != "
                    + projectNbr.length;
            LOG.error(msg);
            throw new RuntimeException(msg);
        }

        // networks
        if ((regionNbr.length != networkNbr.length) || (regionNbr.length != extraNets.length)) {
            String msg = "Config error: length of regionNbr config not same as networkNbr or extraNets: "
                    + regionNbr.length + " != " + networkNbr.length + " or != " + extraNets.length;
            LOG.error(msg);
            throw new RuntimeException(msg);
        }
        expectedNetworkNbr = new int[regionNbr.length];
        for (int count = 0; count < regionNbr.length; count++) {
            expectedNetworkNbr[count] = projectNbr[count] * regionNbr[count] * (networkNbr[count] + extraNets[count]);
        }

        // subnets
        if (regionNbr.length != subsPerNet.length) {
            String msg = "Config error: length of regionNbr config not same as subsPerNet: " + regionNbr.length + " != "
                    + subsPerNet.length;
            LOG.error(msg);
            throw new RuntimeException(msg);
        }
        expectedSubnetNbr = new int[regionNbr.length];
        for (int count = 0; count < regionNbr.length; count++) {
            expectedSubnetNbr[count] = expectedNetworkNbr[count] * subsPerNet[count];
        }

        // Calculate expected number of instances for each type of provider
        if (regionNbr.length != networkNbr.length) {
            String msg = "Config error: length of regionNbr config not same as networkNbr: " + regionNbr.length + " != "
                    + networkNbr.length;
            LOG.error(msg);
            throw new RuntimeException(msg);
        }
        expectedInstanceNbr = new int[regionNbr.length];
        for (int count = 0; count < regionNbr.length; count++) {
            expectedInstanceNbr[count] =
                    projectNbr[count] * regionNbr[count] * networkNbr[count] * subsPerNet[count] * vmPerSub[count];
        }

        // volumes
        if ((regionNbr.length != vmWithVolRatio.length) || (regionNbr.length != extraVols.length)) {
            String msg = "Config error: length of regionNbr config not same as vmWithVolRatio or extraVols: "
                    + regionNbr.length + " != " + vmWithVolRatio.length + " or != " + extraVols.length;
            LOG.error(msg);
            throw new RuntimeException(msg);
        }
        expectedVolumeNbr = new int[regionNbr.length];
        for (int count = 0; count < regionNbr.length; count++) {
            expectedVolumeNbr[count] = ((expectedInstanceNbr[count] * vmWithVolRatio[count]) / 100)
                    + extraVols[count] * projectNbr[count] * regionNbr[count];
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

    }

    @Override
    public String getProviderType(final int adapterIdx) {
        if (FakeConfig.PRIVATE_INDEX == adapterIdx) {
            return privateAdapterProviderType;
        } else if (FakeConfig.PUBLIC_INDEX == adapterIdx) {
            return publicAdapterProviderType;
        } else {
            return null;
        }
    }

    @Override
    public String getProviderId(final int adapterIdx) {
        if (FakeConfig.PRIVATE_INDEX == adapterIdx) {
            return privateAdapterProviderId;
        } else if (FakeConfig.PUBLIC_INDEX == adapterIdx) {
            return publicAdapterProviderId;
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
