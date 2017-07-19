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
package com.hp.hpl.loom.manager.adapter;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.loom.exceptions.AdapterConfigException;


public class AdapterConfigTest {
    /**
     * Tests that it handles the path not existing
     */
    @Test
    public void testFileNotFound() {
        try {
            new AdapterConfig("thisisnthere.properties");
            Assert.fail("Expected an IllegalArgumentException");
        } catch (AdapterConfigException ex) {
            Assert.assertEquals("Missing properties file at path: thisisnthere.properties", ex.getMessage());
        }
    }

    /**
     * Tests that it handles an empty PropertiesConfiguration
     */
    @Test
    public void testEmptyProperties() {
        try {
            new AdapterConfig(new PropertiesConfiguration());
            Assert.fail("Expected an IllegalArgumentException");
        } catch (AdapterConfigException ex) {
            Assert.assertEquals("providerType is missing in file null", ex.getMessage());
        }
    }

    @Test
    public void testSuccessProperties() {
        PropertiesConfiguration props = loadProperties();
        try {
            new AdapterConfig(props);

        } catch (AdapterConfigException ex) {
            Assert.fail("Expected it to pass");
        }
    }

    /**
     * Tests that it handles an PropertiesConfiguration missing each item and throws the correct
     * error
     */
    @Test
    public void testMissingProperties() {
        checkPropertyException(AdapterConfig.ADAPTER_CLASS);
        checkPropertyException(AdapterConfig.AUTH_ENDPOINT);
        checkPropertyException(AdapterConfig.PROVIDER_ID);
        checkPropertyException(AdapterConfig.PROVIDER_NAME);
        checkPropertyException(AdapterConfig.PROVIDER_TYPE);
        checkPropertyException(AdapterConfig.COLLECT_THREADS);
        checkPropertyException(AdapterConfig.SCHEDULING_INTERVAL);
    }

    /**
     * Helper to check for the correct exception and message
     *
     * @param key
     */
    private void checkPropertyException(final String key) {
        PropertiesConfiguration props = loadProperties();
        try {
            props.setProperty(key, null);
            new AdapterConfig(props);
            Assert.fail("Expected an IllegalArgumentException");
        } catch (AdapterConfigException ex) {
            Assert.assertEquals(key + " is missing in file filePath", ex.getMessage());
        }
    }

    /**
     * Test loading from a file vs load the same file into the PropertiesConfiguration the config
     *
     * @throws ConfigurationException
     */
    @Test
    public void testLoadingViaFileVsProperties() throws ConfigurationException, AdapterConfigException {
        AdapterConfig config = new AdapterConfig("./src/test/resources/test.properties");
        AdapterConfig config2 = new AdapterConfig(new PropertiesConfiguration("./src/test/resources/test.properties"));

        Assert.assertEquals(config.getAdapterClass(), config2.getAdapterClass());
        Assert.assertEquals(config.getAdapterDir(), config2.getAdapterDir());
        Assert.assertEquals(config.getAuthEndpoint(), config2.getAuthEndpoint());
        Assert.assertEquals(config.getProviderId(), config2.getProviderId());
        Assert.assertEquals(config.getProviderName(), config2.getProviderName());
        Assert.assertEquals(config.getProviderType(), config2.getProviderType());
        Assert.assertEquals(config.getCollectThreads(), config2.getCollectThreads());
        Assert.assertEquals(config.getSchedulingInterval(), config2.getSchedulingInterval());
    }

    /**
     * Test loading from a file vs load the same file into the PropertiesConfiguration the config
     */
    @Test
    public void testInvalidIntFormat() {
        PropertiesConfiguration props = loadProperties();
        try {
            props.setProperty(AdapterConfig.COLLECT_THREADS, "TEST"); // anything other than an int
            new AdapterConfig(props);
            Assert.fail("Expected an IllegalArgumentException");
        } catch (AdapterConfigException ex) {
            Assert.assertEquals(AdapterConfig.COLLECT_THREADS + " is not valid in file filePath", ex.getMessage());
        }

        props = loadProperties();
        try {
            props.setProperty(AdapterConfig.SCHEDULING_INTERVAL, "TEST"); // anything other than an
                                                                          // int
            new AdapterConfig(props);
            Assert.fail("Expected an IllegalArgumentException");
        } catch (AdapterConfigException ex) {
            Assert.assertEquals(AdapterConfig.SCHEDULING_INTERVAL + " is not valid in file filePath", ex.getMessage());
        }

    }

    /**
     * Test all the getters
     */
    @Test
    public void testAllTheGetters() {
        PropertiesConfiguration props = loadProperties();
        try {
            AdapterConfig config = new AdapterConfig(props);
            Assert.assertEquals("adapterClass", config.getAdapterClass());
            Assert.assertEquals("adapterDir", config.getAdapterDir());
            Assert.assertEquals("authEndpoint", config.getAuthEndpoint());
            Assert.assertEquals(1, config.getCollectThreads());
            Assert.assertEquals("providerId", config.getProviderId());
            Assert.assertEquals("providerName", config.getProviderName());
            Assert.assertEquals("providerType", config.getProviderType());
            Assert.assertEquals(2, config.getSchedulingInterval());


        } catch (AdapterConfigException ex) {
            Assert.fail("Expected it to pass");
        }
    }

    /**
     * Creates a properties config with all the properties set
     *
     * @return
     */
    private PropertiesConfiguration loadProperties() {
        PropertiesConfiguration properties = new PropertiesConfiguration();

        properties.addProperty(AdapterConfig.ADAPTER_CLASS, "adapterClass");
        properties.addProperty(AdapterConfig.ADAPTER_DIR, "adapterDir");
        properties.addProperty(AdapterConfig.AUTH_ENDPOINT, "authEndpoint");
        properties.addProperty(AdapterConfig.COLLECT_THREADS, "1");
        properties.addProperty(AdapterConfig.PROVIDER_ID, "providerId");
        properties.addProperty(AdapterConfig.PROVIDER_NAME, "providerName");
        properties.addProperty(AdapterConfig.PROVIDER_TYPE, "providerType");
        properties.addProperty(AdapterConfig.SCHEDULING_INTERVAL, "2");

        properties.addProperty(AdapterConfig.FILE_PATH, "filePath");
        return properties;
    }

}
