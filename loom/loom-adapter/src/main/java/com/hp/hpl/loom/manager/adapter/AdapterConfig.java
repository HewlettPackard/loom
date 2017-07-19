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

import java.io.File;
import java.util.NoSuchElementException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.exceptions.AdapterConfigException;

/**
 * Class that handles the loading of the adapter config. It attempts to verify the minimal config
 * has been provided.
 *
 */
public class AdapterConfig {
    private static final Log LOG = LogFactory.getLog(AdapterConfig.class);


    private String providerType;
    private String providerId;
    private String providerName;
    private String authEndpoint;
    private int collectThreads;
    private int schedulingInterval;
    private String adapterClass;
    private String adapterDir;

    private String path;

    // set of constants keys that map to the properties object
    /**
     * The provider type key.
     */
    public static final String PROVIDER_TYPE = "providerType";
    /**
     * The provider id key.
     */
    public static final String PROVIDER_ID = "providerId";
    /**
     * The provider name key.
     */
    public static final String PROVIDER_NAME = "providerName";
    /**
     * The auth endpoint key.
     */
    public static final String AUTH_ENDPOINT = "authEndpoint";
    /**
     * The collecting threads key.
     */
    public static final String COLLECT_THREADS = "collectThreads";
    /**
     * The scheduling interval key.
     */
    public static final String SCHEDULING_INTERVAL = "schedulingInterval";
    /**
     * The adapter class key.
     */
    public static final String ADAPTER_CLASS = "adapterClass";
    /**
     * The adapter directory key.
     */
    public static final String ADAPTER_DIR = "adapterDir";
    /**
     * The file path key.
     */
    public static final String FILE_PATH = "path";

    private PropertiesConfiguration properties;

    /**
     * Construct this config from a file path Note throws a runtime exception
     * IllegalArgumentException if config can't be loaded.
     *
     * @param path path to a adapter config file (a property file)
     * @throws AdapterConfigException thrown if there is a problem with the adapterConfig
     */
    public AdapterConfig(final String path) throws AdapterConfigException {
        this.loadFromPath(path);
    }

    /**
     * Construct a config from a existing PropertiesConfiguration (useful for testing). Note throws
     * a runtime exception IllegalArgumentException if config can't be loaded.
     *
     * @param properties The properties file to try and load
     * @throws AdapterConfigException thrown if there is a problem with the adapterConfig
     */
    public AdapterConfig(final PropertiesConfiguration properties) throws AdapterConfigException {
        this.loadFromProperties(properties);
    }

    /**
     * The PropertiesConfiguration that is backing this adapterConfig, it allows for the loading of
     * addition properties in a guarded way.
     *
     * @return A PropertiesConfiguration which contains the current config (if it wasn't created
     *         from one then one is created)
     */
    public PropertiesConfiguration getPropertiesConfiguration() {
        // if it wasn't loaded via a properties file, create one here and set the variables
        if (properties == null) {
            properties = new PropertiesConfiguration();

            properties.addProperty(PROVIDER_TYPE, providerType);
            properties.addProperty(PROVIDER_ID, providerId);
            properties.addProperty(PROVIDER_NAME, providerName);
            properties.addProperty(AUTH_ENDPOINT, authEndpoint);
            properties.addProperty(COLLECT_THREADS, collectThreads);
            properties.addProperty(SCHEDULING_INTERVAL, schedulingInterval);
            properties.addProperty(ADAPTER_CLASS, adapterClass);
            properties.addProperty(FILE_PATH, path);
            if (adapterDir != null) {
                properties.addProperty(ADAPTER_DIR, adapterDir);
            }
        }
        return properties;
    }

    /**
     * Attempts to load a config from a given file path Note throws a runtime exception
     * IllegalArgumentException if config can't be loaded.
     *
     * @param path Location to load the config from
     */
    private void loadFromPath(final String filePath) throws AdapterConfigException {
        path = filePath;
        File loc = new File(path);
        if (loc.exists()) {
            try {
                PropertiesConfiguration props = new PropertiesConfiguration(loc);
                loadFromProperties(props);
            } catch (ConfigurationException e) {
                throw new AdapterConfigException("ConfigurationException file at path: " + path, e);
            }
        } else {
            throw new AdapterConfigException("Missing properties file at path: " + path);
        }
    }

    /**
     * Loads the properties from a properties object - it confirms they exist otherwise it throws an
     * AdapterConfigException.
     *
     * @param properties The properties to parse
     * @throws AdapterConfigException Thrown if properties are missing or incorrect (Strings that
     *         can't become int's)
     */
    private void loadFromProperties(final PropertiesConfiguration props) throws AdapterConfigException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Trying to load properties");
            LOG.debug(props.toString());
        }
        // store all the properties for later
        properties = props;

        providerType = properties.getString(PROVIDER_TYPE);
        providerId = properties.getString(PROVIDER_ID);
        providerName = properties.getString(PROVIDER_NAME);
        authEndpoint = properties.getString(AUTH_ENDPOINT);
        adapterClass = properties.getString(ADAPTER_CLASS);
        adapterDir = properties.getString(ADAPTER_DIR);

        if (properties.getString(FILE_PATH) != null) {
            path = properties.getString(FILE_PATH);
        }

        if (StringUtils.isEmpty(providerType)) {
            throw new AdapterConfigException(PROVIDER_TYPE + " is missing in file " + path);
        }
        if (StringUtils.isEmpty(providerId)) {
            throw new AdapterConfigException(PROVIDER_ID + " is missing in file " + path);
        }
        if (StringUtils.isEmpty(providerName)) {
            throw new AdapterConfigException(PROVIDER_NAME + " is missing in file " + path);
        }
        if (StringUtils.isEmpty(authEndpoint)) {
            throw new AdapterConfigException(AUTH_ENDPOINT + " is missing in file " + path);
        }
        if (StringUtils.isEmpty(adapterClass)) {
            throw new AdapterConfigException(ADAPTER_CLASS + " is missing in file " + path);
        }

        try {
            collectThreads = properties.getInt(COLLECT_THREADS);
        } catch (NoSuchElementException ex) {
            throw new AdapterConfigException(COLLECT_THREADS + " is missing in file " + path, ex);
        } catch (ConversionException ex) {
            throw new AdapterConfigException(COLLECT_THREADS + " is not valid in file " + path, ex);
        }
        try {
            schedulingInterval = properties.getInt(SCHEDULING_INTERVAL);
        } catch (NoSuchElementException ex) {
            throw new AdapterConfigException(SCHEDULING_INTERVAL + " is missing in file " + path, ex);
        } catch (ConversionException ex) {
            throw new AdapterConfigException(SCHEDULING_INTERVAL + " is not valid in file " + path, ex);
        }
    }

    /**
     * @return the providerType
     */
    public String getProviderType() {
        return providerType;
    }

    /**
     * @return the providerId
     */
    public String getProviderId() {
        return providerId;
    }

    /**
     * @return the providerName
     */
    public String getProviderName() {
        return providerName;
    }

    /**
     * @return the authEndpoint
     */
    public String getAuthEndpoint() {
        return authEndpoint;
    }

    /**
     * @return the collectThreads
     */
    public int getCollectThreads() {
        return collectThreads;
    }

    /**
     * @return the schedulingInterval
     */
    public int getSchedulingInterval() {
        return schedulingInterval;
    }

    /**
     * @return the adapterClass
     */
    public String getAdapterClass() {
        return adapterClass;
    }

    /**
     * @return the adapterDir
     */
    public String getAdapterDir() {
        return adapterDir;
    }

    @Override
    public String toString() {
        StringBuilder stb = new StringBuilder();
        stb.append(PROVIDER_TYPE + " - > " + providerType + "\n");
        stb.append(PROVIDER_ID + " - > " + providerId + "\n");
        stb.append(PROVIDER_NAME + " - > " + providerName + "\n");
        stb.append(AUTH_ENDPOINT + " - > " + authEndpoint + "\n");
        stb.append(COLLECT_THREADS + " - > " + collectThreads + "\n");
        stb.append(SCHEDULING_INTERVAL + " - > " + schedulingInterval + "\n");
        stb.append(ADAPTER_CLASS + " - > " + adapterClass + "\n");
        stb.append(FILE_PATH + " - > " + path + "\n");
        if (adapterDir != null) {
            stb.append(ADAPTER_DIR + " - > " + adapterDir + "\n");
        }
        return stb.toString();
    }
}
