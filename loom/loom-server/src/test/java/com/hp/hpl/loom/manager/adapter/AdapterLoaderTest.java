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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.hp.hpl.loom.adapter.Adapter;
import com.hp.hpl.loom.exceptions.DuplicateAdapterException;
import com.hp.hpl.loom.exceptions.DuplicateItemTypeException;
import com.hp.hpl.loom.exceptions.DuplicatePatternException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NullItemTypeIdException;
import com.hp.hpl.loom.exceptions.NullPatternIdException;
import com.hp.hpl.loom.model.AdapterStatus;

@RunWith(MockitoJUnitRunner.class)
public class AdapterLoaderTest {
    private static final Log LOG = LogFactory.getLog(AdapterLoaderTest.class);

    private String monitorDir = "tmp-adapters";

    @Before
    public void clearAndCreate() {
        File file = new File(monitorDir);
        if (!file.mkdir()) {
            // delete any files in there
            File[] files = file.listFiles();
            if (files != null) {
                for (File file2 : files) {
                    file2.delete();
                }
            }
        }
    }

    @After
    public void deleteTmp() {
        File monitor = new File(monitorDir);
        delete(monitor);
        monitor.deleteOnExit();
    }

    /**
     * Deletes the file or if it a directory calls itself to delete the dir/file within it
     *
     * @param dir
     */
    private void delete(final File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                delete(file);
            }
        } else {
            dir.deleteOnExit();
        }
    }


    /**
     * Check the constructor behaves if it get a: - Null path to watch - A missing path to watch - A
     * file rather than a directory
     */
    @Test
    public void testConstructor() {
        // none will throw an error
        AdapterLoader adapter1 = new AdapterLoader(null);
        adapter1.loadExisting();
        AdapterLoader adapter2 = new AdapterLoader("./asdasd/asdasd/");
        adapter2.loadExisting();
        AdapterLoader adapter3 = new AdapterLoader("./src");
        adapter3.loadExisting();
    }

    /**
     * Creates a loader and confirm that the loading existing finds configs in the configured path
     *
     * @throws DuplicateAdapterException
     * @throws UnsupportedOperationException
     * @throws NullPatternIdException
     * @throws DuplicatePatternException
     * @throws NullItemTypeIdException
     * @throws DuplicateItemTypeException
     * @throws NoSuchProviderException
     */
    @Test
    public void testLoadExisting()
            throws DuplicateAdapterException, NoSuchProviderException, DuplicateItemTypeException,
            NullItemTypeIdException, DuplicatePatternException, NullPatternIdException, UnsupportedOperationException {
        AdapterManager adapterManager = mock(AdapterManager.class);
        AdapterLoader adapterLoader = new AdapterLoader("src/test/resources/adapterConfigs");
        adapterLoader.adapterManager = adapterManager;
        adapterLoader.loadExisting();

        // verify that adapterManager had two adapters registered
        verify(adapterManager).registerAdapter(adapterLoader.getAdapter("fakeAdapterPrivate.properties"));
        verify(adapterManager).registerAdapter(adapterLoader.getAdapter("fakeAdapterPublic.properties"));
    }

    /**
     * Checks that the getPathToWatch handles the null / empty case
     */
    @Test
    public void testGetPathToWatchNull() {
        AdapterLoader adapter = new AdapterLoader(null);
        Assert.assertNull(adapter.getPathToWatch());
    }

    /**
     * Checks that the getPathToWatch handles the success
     */
    @Test
    public void testGetPathToWatch() {
        AdapterManager adapterManager = mock(AdapterManager.class);
        AdapterLoader adapterLoader = new AdapterLoader("src/test/resources/adapterConfigs");
        adapterLoader.adapterManager = adapterManager;
        adapterLoader.loadExisting();

        Assert.assertEquals(adapterLoader.getPathToWatch(), Paths.get("src/test/resources/adapterConfigs"));
    }

    /**
     * Configure two adapters and confirm we can look them up using there property file - this is
     * used for the adapter lookup in testing
     */
    @Test
    public void testGetAdapter() {
        AdapterManager adapterManager = mock(AdapterManager.class);
        AdapterLoader adapterLoader = new AdapterLoader("src/test/resources/adapterConfigs");
        adapterLoader.adapterManager = adapterManager;
        adapterLoader.loadExisting();

        // verify that adapterManager had two adapters registered
        Assert.assertNotNull(adapterLoader.getAdapter("fakeAdapterPrivate.properties"));
        Assert.assertNotNull(adapterLoader.getAdapter("fakeAdapterPublic.properties"));
    }

    /**
     * Test we handle the different watch events correctly: CREATE/DELETE/MODIFY
     *
     * @throws IOException
     * @throws DuplicateAdapterException
     * @throws NoSuchProviderException
     * @throws UnsupportedOperationException
     * @throws NullPatternIdException
     * @throws DuplicatePatternException
     * @throws NullItemTypeIdException
     * @throws DuplicateItemTypeException
     */
    @Test
    public void testProcessWatchEvent()
            throws IOException, DuplicateAdapterException, NoSuchProviderException, DuplicateItemTypeException,
            NullItemTypeIdException, DuplicatePatternException, NullPatternIdException, UnsupportedOperationException {
        Path newdir = Paths.get(monitorDir);

        AdapterManager adapterManager = mock(AdapterManager.class);
        AdapterLoader adapterLoader = new AdapterLoader(newdir.toAbsolutePath().toString());
        adapterLoader.adapterManager = adapterManager;

        // nothing should have been loaded as we didn't call "loadExisting"
        Assert.assertNull(adapterLoader.getAdapter("fakeAdapterPrivate.properties"));
        Assert.assertNull(adapterLoader.getAdapter("fakeAdapterPublic.properties"));

        // copy the config into the monitored DIR
        Path source = Paths.get("src/test/resources/adapterConfigs/fakeAdapterPrivate.properties");
        Files.copy(source, newdir.resolve(source.getFileName()));

        WatchEvent<Path> event = new WatchEvent<Path>() {

            @Override
            public java.nio.file.WatchEvent.Kind<Path> kind() {
                return StandardWatchEventKinds.ENTRY_MODIFY;
            }

            @Override
            public int count() {
                return 1;
            }

            @Override
            public Path context() {
                return Paths.get("fakeAdapterPrivate.properties");
            }

        };
        adapterLoader.processWatchEvent(event);

        // verify that adapterManager had two adapters registered
        Adapter adapter = adapterLoader.getAdapter("fakeAdapterPrivate.properties");
        Assert.assertNotNull(adapter);
        verify(adapterManager).registerAdapter(adapter);


        // now try a modify
        event = new WatchEvent<Path>() {

            @Override
            public java.nio.file.WatchEvent.Kind<Path> kind() {
                return StandardWatchEventKinds.ENTRY_MODIFY;
            }

            @Override
            public int count() {
                return 1;
            }

            @Override
            public Path context() {
                return Paths.get("fakeAdapterPrivate.properties");
            }

        };
        adapterLoader.processWatchEvent(event);
        // verify that adapterManager had two adapters registered
        Adapter adapter2 = adapterLoader.getAdapter("fakeAdapterPrivate.properties");
        Assert.assertNotNull(adapter2);
        verify(adapterManager).deregisterAdapter(adapter, new HashSet<>(0));
        verify(adapterManager).registerAdapter(adapter2);


        // now try a delete event
        event = new WatchEvent<Path>() {

            @Override
            public java.nio.file.WatchEvent.Kind<Path> kind() {
                return StandardWatchEventKinds.ENTRY_DELETE;
            }

            @Override
            public int count() {
                return 1;
            }

            @Override
            public Path context() {
                return Paths.get("fakeAdapterPrivate.properties");
            }

        };

        adapterLoader.processWatchEvent(event);
        Assert.assertNull(adapterLoader.getAdapter("fakeAdapterPrivate.properties"));
        verify(adapterManager).deregisterAdapter(adapter2, new HashSet<>(0));

    }


    /**
     * Test we handle the loading of a properties file where the service needs to be loaded in via
     * the jarfile
     *
     * Currently ignored as it depends on the release target of the maven lifecycle.
     *
     * @throws IOException
     * @throws DuplicateAdapterException
     * @throws NoSuchProviderException
     * @throws UnsupportedOperationException
     * @throws NullPatternIdException
     * @throws DuplicatePatternException
     * @throws NullItemTypeIdException
     * @throws DuplicateItemTypeException
     */
    @Test
    @Ignore
    public void testLoadSuccessFileNotInClasspath()
            throws IOException, DuplicateAdapterException, NoSuchProviderException, DuplicateItemTypeException,
            NullItemTypeIdException, DuplicatePatternException, NullPatternIdException, UnsupportedOperationException {

        Path newdir = Paths.get(monitorDir);

        AdapterManager adapterManager = mock(AdapterManager.class);
        AdapterLoader adapterLoader = new AdapterLoader(newdir.toAbsolutePath().toString());
        adapterLoader.adapterManager = adapterManager;

        // copy the config into the monitored DIR
        Path source = Paths.get("../adapters/file/file.properties");
        Files.copy(source, newdir.resolve("file.properties"));
        Path sourceJar = Paths.get("../adapters/file/target/loomAdapterFile.jar");
        File dir = newdir.resolve("loomAdapterFile").toFile();
        dir.mkdir();
        Files.copy(sourceJar, newdir.resolve("loomAdapterFile/loomAdapterFile.jar"),
                StandardCopyOption.REPLACE_EXISTING);


        WatchEvent<Path> event = new WatchEvent<Path>() {

            @Override
            public java.nio.file.WatchEvent.Kind<Path> kind() {
                return StandardWatchEventKinds.ENTRY_MODIFY;
            }

            @Override
            public int count() {
                return 1;
            }

            @Override
            public Path context() {
                return Paths.get("file.properties");
            }

        };
        adapterLoader.processWatchEvent(event);

        // verify that adapterManager had two adapters registered
        Adapter adapter = adapterLoader.getAdapter("file.properties");
        Assert.assertNotNull(adapter);
        verify(adapterManager).registerAdapter(adapter);

        // try and deregister
        event = new WatchEvent<Path>() {

            @Override
            public java.nio.file.WatchEvent.Kind<Path> kind() {
                return StandardWatchEventKinds.ENTRY_DELETE;
            }

            @Override
            public int count() {
                return 1;
            }

            @Override
            public Path context() {
                return Paths.get("file.properties");
            }

        };
        adapterLoader.processWatchEvent(event);

        verify(adapterManager).deregisterAdapter(adapter, new HashSet<>(0));

    }

    /**
     * Test what happens when the jarfile link is missing/empty
     *
     * @throws DuplicateAdapterException
     * @throws IOException
     */
    @Test
    public void testLoadMissingFileNotInClasspath() throws DuplicateAdapterException, IOException {
        Path newdir = Paths.get(monitorDir);

        AdapterManager adapterManager = mock(AdapterManager.class);
        AdapterLoader adapterLoader = new AdapterLoader(newdir.toAbsolutePath().toString());
        adapterLoader.adapterManager = adapterManager;

        // copy the config into the monitored DIR
        Path source = Paths.get("src/test/resources/adapterConfigs/noAdapter.properties-example");
        Files.copy(source, newdir.resolve("noAdapter.properties"));

        WatchEvent<Path> event = new WatchEvent<Path>() {

            @Override
            public java.nio.file.WatchEvent.Kind<Path> kind() {
                return StandardWatchEventKinds.ENTRY_MODIFY;
            }

            @Override
            public int count() {
                return 1;
            }

            @Override
            public Path context() {
                return Paths.get("noAdapter.properties");
            }

        };
        adapterLoader.processWatchEvent(event);

        // verify that no adapters are registered
        Adapter adapter = adapterLoader.getAdapter("noAdapter.properties");
        Assert.assertNull(adapter);
    }


    /**
     * Test what happens if an event comes in for a file without .properties
     *
     * @throws IOException
     */
    @Test
    public void testLoadNonProperties() throws IOException {
        Path newdir = Paths.get(monitorDir);

        AdapterManager adapterManager = mock(AdapterManager.class);
        AdapterLoader adapterLoader = new AdapterLoader(newdir.toAbsolutePath().toString());
        adapterLoader.adapterManager = adapterManager;

        // copy the config into the monitored DIR
        Path source = Paths.get("src/test/resources/adapterConfigs/noAdapter.properties-example");
        Files.copy(source, newdir.resolve("noAdapter.notProperties"));

        WatchEvent<Path> event = new WatchEvent<Path>() {

            @Override
            public java.nio.file.WatchEvent.Kind<Path> kind() {
                return StandardWatchEventKinds.ENTRY_MODIFY;
            }

            @Override
            public int count() {
                return 1;
            }

            @Override
            public Path context() {
                return Paths.get("noAdapter.properties");
            }

        };
        adapterLoader.processWatchEvent(event);

        event = new WatchEvent<Path>() {

            @Override
            public java.nio.file.WatchEvent.Kind<Path> kind() {
                return StandardWatchEventKinds.ENTRY_DELETE;
            }

            @Override
            public int count() {
                return 1;
            }

            @Override
            public Path context() {
                return Paths.get("noAdapter.properties");
            }

        };
        adapterLoader.processWatchEvent(event);
    }

    /**
     * Test what happens if an event comes in deleting a properties file for an adapter that wasn't
     * loaded <<<<<<< HEAD
     *
     * =======
     *
     * >>>>>>> bfbe35aaaf0e3eee16a4cb4121998f22a0e7cd5f
     *
     * @throws IOException
     */
    @Test
    public void testDeleteNonLoadedProperties() throws IOException {
        Path newdir = Paths.get(monitorDir);

        AdapterManager adapterManager = mock(AdapterManager.class);
        AdapterLoader adapterLoader = new AdapterLoader(newdir.toAbsolutePath().toString());
        adapterLoader.adapterManager = adapterManager;

        // copy the config into the monitored DIR
        Path source = Paths.get("src/test/resources/adapterConfigs/fakeAdapterPrivate.properties");
        Files.copy(source, newdir.resolve(source.getFileName()));

        WatchEvent<Path> event = new WatchEvent<Path>() {

            @Override
            public java.nio.file.WatchEvent.Kind<Path> kind() {
                return StandardWatchEventKinds.ENTRY_DELETE;
            }

            @Override
            public int count() {
                return 1;
            }

            @Override
            public Path context() {
                return Paths.get("fakeAdapterPrivate.properties");
            }

        };
        adapterLoader.processWatchEvent(event);
    }

    /**
     * Creates a loader and confirm that the loading existing finds configs in the configured path
     *
     * Checks the adapter status call returns two results and the details are correct
     *
     * @throws DuplicateAdapterException
     * @throws UnsupportedOperationException
     * @throws NullPatternIdException
     * @throws DuplicatePatternException
     * @throws NullItemTypeIdException
     * @throws DuplicateItemTypeException
     * @throws NoSuchProviderException
     */
    @Test
    public void testAdapterStatus()
            throws DuplicateAdapterException, NoSuchProviderException, DuplicateItemTypeException,
            NullItemTypeIdException, DuplicatePatternException, NullPatternIdException, UnsupportedOperationException {
        AdapterManager adapterManager = mock(AdapterManager.class);
        AdapterLoader adapterLoader = new AdapterLoader("src/test/resources/adapterConfigs");
        adapterLoader.adapterManager = adapterManager;
        adapterLoader.loadExisting();

        // verify that adapterManager had correct adapters registered
        verify(adapterManager).registerAdapter(adapterLoader.getAdapter("deltaAdapterPrivate.properties"));
        verify(adapterManager).registerAdapter(adapterLoader.getAdapter("fakeAdapterPrivate.properties"));
        verify(adapterManager).registerAdapter(adapterLoader.getAdapter("fakeAdapterPublic.properties"));

        List<AdapterStatus> statuses = adapterLoader.getAdapterDetails();
        Assert.assertEquals("Incorrect number of adapters", 3, statuses.size());
        Map<String, AdapterStatus> statusesMap = new HashMap<>();
        for (AdapterStatus status : statuses) {
            statusesMap.put(status.getId(), status);
        }
        AdapterStatus statusDeltaPrivate = statusesMap.get("delta/os");
        AdapterStatus statusPrivate = statusesMap.get("private/os");
        AdapterStatus statusPublic = statusesMap.get("public/os");
        Assert.assertEquals("class com.hp.hpl.loom.adapter.os.deltas.DeltaAdapter", statusDeltaPrivate.getClassName());
        Assert.assertEquals("Private", statusDeltaPrivate.getName());
        Assert.assertEquals("class com.hp.hpl.loom.adapter.os.fake.FakeAdapter", statusPublic.getClassName());
        Assert.assertEquals("Public", statusPublic.getName());
        Assert.assertEquals("class com.hp.hpl.loom.adapter.os.fake.FakeAdapter", statusPrivate.getClassName());
        Assert.assertEquals("Private", statusPrivate.getName());
    }

    /**
     * Tests that the adapter status call works whilst the adapters are being reloaded.
     */
    @Test
    public void testAdapterStatusDuringReload() {
        AdapterManager adapterManager = mock(AdapterManager.class);
        AdapterLoader adapterLoader = new AdapterLoader("src/test/resources/adapterConfigs");
        adapterLoader.adapterManager = adapterManager;
        adapterLoader.loadExisting();

        // verify that adapterManager had two adapters registered
        Assert.assertNotNull(adapterLoader.getAdapter("fakeAdapterPrivate.properties"));
        Assert.assertNotNull(adapterLoader.getAdapter("fakeAdapterPublic.properties"));


        Thread t = new Thread(new Runnable() {
            int cycle = 0;
            boolean finished = false;

            @Override
            public void run() {
                while (!finished) {
                    if (cycle == 100) {
                        finished = true;
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    try {
                        adapterLoader.removeAdapter("fakeAdapterPrivate.properties",
                                adapterLoader.getAdapter("fakeAdapterPrivate.properties"));
                        adapterLoader.removeAdapter("fakeAdapterPublic.properties",
                                adapterLoader.getAdapter("fakeAdapterPublic.properties"));
                        adapterLoader.loadExisting();
                    } catch (Exception ex) {
                        throw ex;
                    }
                    cycle++;
                }

            }
        });

        t.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        for (int i = 0; i < 100; i++) {
            adapterLoader.getAdapterDetails();
            adapterLoader.getStatusEvents();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            t.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
