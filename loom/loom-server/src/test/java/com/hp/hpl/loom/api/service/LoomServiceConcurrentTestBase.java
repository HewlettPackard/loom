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

import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.loom.adapter.os.fake.FakeConfig;
import com.hp.hpl.loom.api.client.AssertionResult;
import com.hp.hpl.loom.api.client.ConcurrentTestClient;
import com.hp.hpl.loom.api.client.LoomClient;

public abstract class LoomServiceConcurrentTestBase extends IntegrationTestBase {
    private static final Log LOG = LogFactory.getLog(LoomServiceConcurrentTestBase.class);

    private int noOfClients = 16;
    private long duration = 10000;

    @Before
    public void setUp() throws Exception {
        super.setUp(noOfClients);
    }

    @Test
    public void testConcurrentCalls() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(noOfClients);
        List<Future<List<AssertionResult>>> allAssertionResults =
                Collections.synchronizedList(new ArrayList<Future<List<AssertionResult>>>());
        int count = 0;
        for (LoomClient c : clients) {
            count++;
            Callable<List<AssertionResult>> worker =
                    new ConcurrentTestClient(count, c, duration, testDataConfig, FakeConfig.PRIVATE_INDEX);
            Future<List<AssertionResult>> submit = executor.submit(worker);
            allAssertionResults.add(submit);
        }

        for (Future<List<AssertionResult>> assertionResults : allAssertionResults) {
            try {
                List<AssertionResult> results = assertionResults.get();
                for (AssertionResult result : results) {
                    assertFalse(result.getMessage(), result.getFail());
                }
            } catch (InterruptedException e) {
                LOG.debug("InterruptedException " + e.getStackTrace().toString());
            } catch (ExecutionException e) {
                LOG.debug("ExecutionException " + e.getStackTrace().toString());
            }
        }
        executor.shutdown();
    }
}
