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

import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LogFactory.class})
public class AdapterWatcherThreadTest {
    /**
     * Confirms that it doesn't crash if you
     */
    @Test
    public void testBadlyConfigured() {
        mockStatic(LogFactory.class);
        Log logger = mock(Log.class);
        when(LogFactory.getLog(any(Class.class))).thenReturn(logger);
        when(logger.isWarnEnabled()).thenReturn(true);

        AdapterWatcherThread thread = new AdapterWatcherThread(null);
        thread.run(); // kick of the run method - this would normally be done by the Thread

        // just confirm it doesn't crash - just writes out a error (not checking the text as that
        // is subject to change)
        Mockito.verify(logger).error(Matchers.anyString());
    }
}
