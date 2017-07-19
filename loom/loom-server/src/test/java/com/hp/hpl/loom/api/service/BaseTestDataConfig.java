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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.api.ApiConfig;

public abstract class BaseTestDataConfig implements TestDataConfig {

    private static final String DEFAULT_TEST_DEPLOYMENT_PROPERTIES_FILE = "deployment-test.properties";
    private static final Log LOG = LogFactory.getLog(BaseTestDataConfig.class);

    protected PropertiesConfiguration prop;

    private boolean soak;
    private int soakTimeMins;
    private String aggregatorUri, username, password;
    private int testFibres, clientFibres;

    public BaseTestDataConfig() {
        prop = loadProperties();
        soak = prop.getBoolean("test.soak");
        soakTimeMins = prop.getInt("test.soakTimeMins");
        testFibres = prop.getInt("fake.data.braid.test");
        clientFibres = prop.getInt("fake.data.braid.client");
        aggregatorUri = prop.getString("aggregator.uri");
        username = prop.getString("test.username");
        password = prop.getString("test.password");
    }

    private PropertiesConfiguration loadProperties() {
        PropertiesConfiguration prop = new PropertiesConfiguration();
        String deploymentProperties = System.getProperty(ApiConfig.DEPLOYMENT_PROPERTIES_PROPERTY);
        try {
            if (deploymentProperties == null) {
                deploymentProperties = DEFAULT_TEST_DEPLOYMENT_PROPERTIES_FILE;
                LOG.info("Using default properties file '" + deploymentProperties + "'");
            } else {
                LOG.info("Using override properties file '" + deploymentProperties + "'");
            }

            LOG.info("Looking for deployment properties file on classpath '" + deploymentProperties + "'");

            InputStream in = ApiConfig.class.getResourceAsStream(deploymentProperties);

            if (in == null) {
                LOG.warn("Failed to locate properties file on classpath, looking in working directory: "
                        + System.getProperty("user.dir"));
                in = new FileInputStream("./" + deploymentProperties);
                LOG.info("Found '" + deploymentProperties + "' in working directory");
            } else {
                LOG.info("Found '" + deploymentProperties + "' on the classpath");
            }

            prop.load(in);
            return prop;
        } catch (ConfigurationException | IOException e) {
            LOG.error("Failed to load properties file '" + deploymentProperties + "'", e);
            throw new RuntimeException("Failed to initialise from properties file " + deploymentProperties);
        }
    }

    protected int[] parseIntArray(final String arrayOfInts) {
        String[] intStringArray = arrayOfInts.split(",");
        int[] intArray = new int[intStringArray.length];
        for (int count = 0; count < intStringArray.length; count++) {
            intArray[count] = Integer.parseInt(intStringArray[count]);
        }
        return intArray;
    }

    @Override
    public boolean getSoak() {
        return soak;
    }

    @Override
    public int getSoakTimeMins() {
        return soakTimeMins;
    }

    @Override
    public int getBraidTest() {
        return testFibres;
    }

    @Override
    public int getBraidClient() {
        return clientFibres;
    }

    @Override
    public String getAggregatorUri() {
        return aggregatorUri;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }
}
