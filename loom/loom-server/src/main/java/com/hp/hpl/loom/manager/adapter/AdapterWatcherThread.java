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

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An adapter watcher thread that implements Runnable. It is registered via spring and injected with
 * the adapterLoader instance. It sits in a loop monitoring the file system for changes using the
 * watchService.
 *
 */
public class AdapterWatcherThread implements Runnable {
    // the sleep time for the thread
    private static final int THREAD_SLEEP = 5000;
    private static final Log LOG = LogFactory.getLog(AdapterWatcherThread.class);
    // AdapterLoader to pass events back to
    private AdapterLoader adapterLoader;

    /**
     * Constructor used by Spring to create the thread.
     *
     * @param adapterLoader class to pass the events back to
     */
    public AdapterWatcherThread(final AdapterLoader adapterLoader) {
        this.adapterLoader = adapterLoader;
    }

    /**
     * Implementation of the run interface. It sits in loop checking with the WatchService if any
     * files have changed
     */
    @Override
    @SuppressWarnings({"checkstyle:emptyblock", "checkstyle:linelength"})
    public void run() {
        try {
            // if we haven't been correctly configured then finish with a log message
            if (adapterLoader == null) {
                LOG.error(
                        "AdapterWatcherThread hasn't been correctly configured - it needs a AdapterLoader within the constructor");
                return;
            }

            try {
                WatchService watcher = FileSystems.getDefault().newWatchService();
                WatchKey key = adapterLoader.getPathToWatch().register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

                do {
                    try {
                        // added a sleep to make the watcher "group" up the modify events, otherwise
                        // in windows when you edit the config and save you get two events back to
                        // back (not tried on unix).
                        Thread.sleep(THREAD_SLEEP);
                    } catch (InterruptedException e) {
                        // don't do anything
                    }

                    List<Object> contexts = new ArrayList<>();


                    // if we have a CREATE and a MODIFY for the same file than filter to just the
                    // CREATE
                    for (final WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                            contexts.add(event.context());
                        }

                        if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY
                                && !contexts.contains(event.context())) {
                            adapterLoader.processWatchEvent(event);
                        } else {
                            adapterLoader.processWatchEvent(event);
                        }
                    }
                } while (key.reset());
            } catch (IOException ex) {
                LOG.error("IOException within the AdapterWatcherThread", ex);
            }
        } catch (Throwable ex) {
            LOG.error("A runtime exception within the AdapterWatcherThread", ex);
        }
    }
}
