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
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hp.hpl.loom.adapter.Adapter;
import com.hp.hpl.loom.exceptions.AdapterConfigException;
import com.hp.hpl.loom.exceptions.DuplicateAdapterException;
import com.hp.hpl.loom.exceptions.DuplicateItemTypeException;
import com.hp.hpl.loom.exceptions.DuplicatePatternException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NullItemTypeIdException;
import com.hp.hpl.loom.exceptions.NullPatternIdException;
import com.hp.hpl.loom.model.AdapterStatus;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.StatusEvent;
import com.hp.hpl.loom.model.StatusTypeEnum;

@Component
/**
 * This class monitors a file path and attempts to load adapters based on the properties files
 * within that path. If it finds the adapter is already loaded into the classpath than it makes use
 * of this otherwise it attempts to load the service based on the JarFile location within the
 * properties.
 *
 *
 */
public class AdapterLoader {

    private static final Log LOG = LogFactory.getLog(AdapterLoader.class);

    // The path this AdapterLoader is monitoring
    private Path pathToWatch;

    @Autowired
    protected AdapterManager adapterManager;

    // maintain a list of properties files to adapters so that we can lookup the correct handle for
    // the modify/delete
    // watch event and also the testing adapter lookup
    private Map<String, Adapter> propertiesToAdapter = new HashMap<>();
    private Map<String, Long> propertiesToTimestamp = new HashMap<>();
    private Map<Provider, Adapter> providerToAdapter = new HashMap<>();

    // holds a map of status events related to given adapter
    private Map<String, List<StatusEvent>> propertiesToStatusEvents = new HashMap<>();

    // constant for the properties file ending
    private static final String PROPERTY_FILE_ENDING = ".properties";

    /**
     * Constructs a new AdapterLoader that monitors the pathToWatch for adapter properties files
     *
     * @param pathToWatch Path to watch for properties files
     */
    public AdapterLoader(final String pathToWatch) {

        try {
            this.pathToWatch = Paths.get(pathToWatch);
        } catch (InvalidPathException | NullPointerException ex) {
            LOG.error("Path " + pathToWatch + " could not be watched");
        }
    }

    /**
     * After the construction of the class it checks to see what existing properties files exist.
     * The watcher API only provides notifications for changes to the file system.
     */
    @PostConstruct
    protected void loadExisting() {
        if (LOG.isInfoEnabled()) {
            LOG.info("Loading existing adapters");
        }
        if (this.getPathToWatch() != null) {
            // get list of the suitable files - filter based on the properties file ending
            File[] files = this.getPathToWatch().toFile().listFiles(new FileFilter() {
                @Override
                public boolean accept(final File pathname) {
                    if (pathname.getAbsolutePath().endsWith(PROPERTY_FILE_ENDING)) {
                        return true;
                    }
                    return false;
                }
            });

            // if we have files then try to load them
            if (files != null && files.length != 0) {
                for (File file : files) {
                    this.load(file);
                }
            } else {
                if (LOG.isWarnEnabled()) {
                    String message = "No adapters found to load at start: path = "
                            + this.getPathToWatch().toAbsolutePath().toString();
                    LOG.warn(message);
                }
            }
        }
    }

    /**
     * Returns the path to watch. Used locally and by the AdapterWatcherThread to determine which
     * path to monitor
     *
     * @return Path to watch
     */
    protected Path getPathToWatch() {
        return pathToWatch;
    }

    /**
     * Gets the Adapter load based on the adapter property file. This is useful for loading a given
     * adapter when you know the property file that configured it (within testing).
     *
     * @param adapterPropertyFile Property file that loaded the adapter
     * @return Adapter corresponding to the property file or null if non found
     */
    public Adapter getAdapter(final String adapterPropertyFile) {
        synchronized (propertiesToAdapter) {
            return propertiesToAdapter.get(adapterPropertyFile);
        }
    }

    /**
     * Gets the Adapter based on a given provider
     * 
     * @param provider the provider to look up from
     * @return Adapter corresponding to the provider
     */
    public Adapter getAdapter(final Provider provider) {
        synchronized (providerToAdapter) {
            return providerToAdapter.get(provider);
        }
    }

    /**
     * Handles a watchevent, it currently only handles the DELETE and MODIFY - create isn't handled
     * as when you create a new file you get two events - one of the create and one for the modify,
     * therefore it can be safely handled in the modify only.
     *
     * If the delete corresponds to a adapter loaded - it deregisterWithAdapterManager it with the
     * adatperManager If the modify is new / modified it passes it to the load method that handles
     * re-registering existing adapters
     *
     * @param event
     */
    public void processWatchEvent(final WatchEvent<?> event) {
        synchronized (propertiesToAdapter) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Logging watch event:" + event.kind() + ": " + event.context());
            }
            // check it is ended with .properties
            if (isPropertyFile(event)) {
                String path = ((Path) event.context()).toString();
                Adapter adapter = propertiesToAdapter.get(path);
                // if we have already seen this then deregister it
                if (adapter != null) {
                    removeAdapter(path, adapter);
                }

                if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE
                        || event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                    File file = new File(getPathToWatch().toString(), ((Path) event.context()).toString());
                    load(file);
                }
            }
        }
    }

    /**
     * Register the adapter and add to the local list to maintain what has been loaded, when and
     * from where
     *
     * @param key
     * @param adapter
     * @throws DuplicateAdapterException
     * @throws UnsupportedOperationException
     * @throws NullPatternIdException
     * @throws DuplicatePatternException
     */
    private void addAdapter(final String key, final Adapter adapter)
            throws DuplicateAdapterException, NoSuchProviderException, DuplicateItemTypeException,
            NullItemTypeIdException, DuplicatePatternException, NullPatternIdException {
        adapter.onLoad();
        adapterManager.registerAdapter(adapter);
        if (LOG.isInfoEnabled()) {
            LOG.info("Adapter registered -> " + adapter.getProvider().getProviderName() + " "
                    + adapter.getProvider().getProviderId());
        }
        recordAdapterDetails(key, adapter);
    }

    /**
     * Remove an adapter from the local list to maintain what has been loaded, when and from where
     *
     * @param key
     * @param adapter
     */
    protected void removeAdapter(final String key, final Adapter adapter) {
        adapter.onUnload();

        if (adapterManager != null) {
            try {
                adapterManager.deregisterAdapter(adapter, adapter.getSessions());
            } catch (NoSuchProviderException nspe) {
                LOG.error("provider cannot deregister because it's already been done?!");
            }
        }
        removeAdapterDetails(key);
    }

    /**
     * Adds the adapter to the local maps, keying in based on the value of key (the property file
     * name)
     *
     * @param key - Key to store the adapter under
     * @param adapter - The adapter to store
     */
    private void recordAdapterDetails(final String key, final Adapter adapter) {
        synchronized (propertiesToAdapter) {
            propertiesToAdapter.put(key, adapter);
            propertiesToTimestamp.put(key, System.currentTimeMillis());
            if (propertiesToStatusEvents.get(key) == null) {
                propertiesToStatusEvents.put(key, new ArrayList<StatusEvent>());
            }
            providerToAdapter.put(adapter.getProvider(), adapter);
        }
    }

    /**
     * Removes the adapter details from the local maps
     *
     * @param key The key to remove
     */
    private void removeAdapterDetails(final String key) {
        synchronized (propertiesToAdapter) {
            Adapter adapter = propertiesToAdapter.get(key);
            if (adapter != null) {
                providerToAdapter.put(adapter.getProvider(), adapter);
            }
            propertiesToAdapter.remove(key);
            propertiesToTimestamp.remove(key);
            propertiesToStatusEvents.remove(key);
        }
    }

    /**
     * Records a status event based on a property file name (the key)
     *
     * @param key - property file name
     * @param statusEvent The status event which contains the event type, message, and timestamp
     */
    public void recordStatusEvent(final String key, final StatusEvent statusEvent) {
        List<StatusEvent> events = propertiesToStatusEvents.get(key);
        if (propertiesToStatusEvents.get(key) == null) {
            events = new ArrayList<StatusEvent>();
            propertiesToStatusEvents.put(key, events);
        }
        events.add(statusEvent);
    }

    /**
     * Checks if the WatchEvent is for a properties file
     *
     * @param event The event to check the path of
     * @return True if the event corresponds to a property file
     */
    private boolean isPropertyFile(final WatchEvent<?> event) {
        boolean isPropertyFile = false;
        final Path eventPath = (Path) event.context();
        if (LOG.isDebugEnabled()) {
            LOG.debug("eventPath.toString() " + eventPath.toString());
        }
        // check it is ended with .properties
        if (eventPath.toString().endsWith(PROPERTY_FILE_ENDING)) {
            isPropertyFile = true;
        }
        return isPropertyFile;
    }

    /**
     * Load the adapter based on the properties file
     *
     * @param properties The properties file to attempt to load
     */
    private void load(final File properties) {
        try {
            if (LOG.isInfoEnabled()) {
                LOG.info("Loading file " + properties.getAbsolutePath());
            }
            AdapterConfig adapterConfig = new AdapterConfig(properties.getAbsolutePath());

            Adapter adapter = loadAdapter(adapterConfig);
            if (adapter != null) {
                adapter.setAdapterManager(adapterManager, adapterConfig.getPropertiesConfiguration());

                // we have successfully found add adapter details, register and add to local cache
                addAdapter(properties.getName(), adapter);
            } else {
                // no adapter found of this properties file
                StatusEvent statusEvent = new StatusEvent("No adapter found for this properties file",
                        StatusTypeEnum.WARNING, System.currentTimeMillis());
                recordStatusEvent(properties.getName(), statusEvent);
            }
        } catch (AdapterConfigException | MalformedURLException | DuplicateAdapterException | NoSuchProviderException
                | DuplicateItemTypeException | NullItemTypeIdException | DuplicatePatternException
                | NullPatternIdException | UnsupportedOperationException | IllegalArgumentException ex) {
            StatusEvent statusEvent =
                    new StatusEvent(ex.getMessage(), StatusTypeEnum.ERROR, System.currentTimeMillis());
            recordStatusEvent(properties.getName(), statusEvent);
            LOG.error("Problem loading file", ex);
        }
    }

    /**
     * Loads an adapter based on the AdapterConfig (a processed version of the properties file) It
     * checks if the service is available for the classpth, if not it attempts to load file the
     * jarfile reference in the properties (if present)
     *
     * @param adapterConfig AdapterConfig file to try and load from
     * @return The adapter fetched based on the config file
     * @throws MalformedURLException
     */
    private Adapter loadAdapter(final AdapterConfig adapterConfig) throws MalformedURLException {
        // check if we know of the adapter class already if not then check we have the corresponding
        // adapter jar
        ServiceLoader<Adapter> serviceLoader = ServiceLoader.load(Adapter.class);
        Adapter foundAdapter = lookup(serviceLoader, adapterConfig);
        // if not found try the class path
        if (foundAdapter == null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Config: " + adapterConfig.toString());
            }

            if (adapterConfig.getAdapterDir() != null) {
                List<URL> urls = new ArrayList<URL>();
                String[] paths = null;
                if (adapterConfig.getAdapterDir().contains(";")) {
                    paths = adapterConfig.getAdapterDir().split(";");
                } else {
                    paths = new String[] {adapterConfig.getAdapterDir()};
                }
                for (String path : paths) {
                    File jarDir = new File(this.getPathToWatch().toString(), path);
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Didn't find adapter in classpath -> " + adapterConfig.getAdapterClass()
                                + " looking in " + jarDir.getAbsolutePath());
                    }

                    if (jarDir.exists() && jarDir.isDirectory()) {
                        // add then as well
                        File[] files = jarDir.listFiles();
                        for (File file : files) {
                            if (file.isFile()) {
                                urls.add(file.toURI().toURL());
                            }
                        }
                    } else {
                        LOG.error("Adapter dir is missing or not a directory: " + jarDir);
                    }
                }

                for (URL found : urls) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Adding file to thread classpath:" + found);
                    }
                    try {
                        addPath(found);
                    } catch (Exception e) {
                        LOG.error("Problem adding URL to classloader", e);
                    }
                }

                serviceLoader = ServiceLoader.load(Adapter.class);
                foundAdapter = lookup(serviceLoader, adapterConfig);
            } else {
                LOG.warn("No adapter class found for:" + adapterConfig.getAdapterClass() + " "
                        + adapterConfig.getProviderName());
            }
        } else {
            if (LOG.isInfoEnabled()) {
                LOG.info("Found adapter in classpath -> " + adapterConfig.getAdapterClass());
            }
        }

        return foundAdapter;
    }

    /**
     * Adds the given url to the classloader of the current thread. It throws Exception as there
     * isn't anything you can do with the more specific exceptions (five in total)
     *
     * @param url
     * @throws Exception
     */
    public static void addPath(final URL url) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<?> urlClass = URLClassLoader.class;
        Method method = urlClass.getDeclaredMethod("addURL", new Class[] {URL.class});
        method.setAccessible(true);
        method.invoke(classLoader, new Object[] {url});
    }


    /**
     * Returns the adapter status information back to the status page - it contains the build,
     * version, name, class name and loaded time
     *
     * @return a list of adatperStatus objects containing the status info
     */
    public List<AdapterStatus> getAdapterDetails() {
        List<AdapterStatus> adapterStatuses = new ArrayList<AdapterStatus>();
        synchronized (propertiesToAdapter) {
            for (String key : propertiesToAdapter.keySet()) {
                Adapter adapter = propertiesToAdapter.get(key);
                Long loadedTime = propertiesToTimestamp.get(key);

                AdapterStatus adapterStatus = new AdapterStatus();
                adapterStatus.setProviderType(adapter.getProvider().getProviderType());
                adapterStatus.setProviderId(adapter.getProvider().getProviderId());
                adapterStatus.setBuild(adapter.getClass().getPackage().getImplementationTitle());
                adapterStatus.setVersion(adapter.getClass().getPackage().getImplementationVersion());
                adapterStatus.setName(adapter.getProvider().getProviderName());
                adapterStatus.setClassName(adapter.getClass().toString());
                adapterStatus.setLoadedTime(loadedTime);
                adapterStatus.setStatusEvents(propertiesToStatusEvents.get(key));
                adapterStatus.setPropertiesName(key);

                adapterStatuses.add(adapterStatus);
            }
        }

        return adapterStatuses;
    }

    /**
     * Returns the status events for properties files that failed to load adapters.
     *
     * @return
     */
    public Map<String, List<StatusEvent>> getStatusEvents() {
        Map<String, List<StatusEvent>> results = new HashMap<>();
        synchronized (propertiesToAdapter) {
            // now add in the status event for the adapters that haven't loaded
            Set<String> statusEventsWithoutAdapters = propertiesToStatusEvents.keySet();
            statusEventsWithoutAdapters.removeAll(propertiesToAdapter.keySet());
            for (String key : statusEventsWithoutAdapters) {
                results.put(key, propertiesToStatusEvents.get(key));
            }
        }

        return results;
    }

    /**
     * Looks up the adapter using the serviceLoader and adapterConfig. It iterates over all the
     * Adapters found and checks the class to see if it matches the adapterConfig required
     *
     * @param serviceLoader ServiceLoader to get the adapters from
     * @param adapterConfig AdapterConfig to attempt to load
     * @return Adapter found or null
     */
    private Adapter lookup(final ServiceLoader<Adapter> serviceLoader, final AdapterConfig adapterConfig) {
        Adapter foundAdapter = null;
        Iterator<Adapter> apit = serviceLoader.iterator();

        while (apit.hasNext()) {
            Adapter adapter = apit.next();
            if (LOG.isInfoEnabled()) {
                LOG.info("Found service " + adapter);
            }

            String clazz = adapter.getClass().getCanonicalName();
            if (clazz.equals(adapterConfig.getAdapterClass())) {
                foundAdapter = adapter;
                break;
            }
        }


        if (foundAdapter == null) {
            LOG.warn("No adapters found, please ensure you have included a manifest file "
                    + "'com.hp.hpl.loom.adapter.Adapter' under META-INF\\services with your "
                    + "adapter implemenation class defined within");
        }

        return foundAdapter;
    }
}
