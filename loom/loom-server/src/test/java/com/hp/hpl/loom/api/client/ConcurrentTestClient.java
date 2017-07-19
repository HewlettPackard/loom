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
package com.hp.hpl.loom.api.client;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.os.BaseOsAdapter;
import com.hp.hpl.loom.adapter.os.fake.FakeConfig;
import com.hp.hpl.loom.api.service.TestDataConfig;
import com.hp.hpl.loom.api.service.utils.BasicQueryOperations;
import com.hp.hpl.loom.exceptions.NoSuchThreadDefinitionException;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.QueryResult;
import com.hp.hpl.loom.model.QueryResultElement;
import com.hp.hpl.loom.tapestry.PatternDefinition;
import com.hp.hpl.loom.tapestry.PatternDefinitionList;
import com.hp.hpl.loom.tapestry.TapestryDefinition;
import com.hp.hpl.loom.tapestry.ThreadDefinition;

public class ConcurrentTestClient implements Callable<List<AssertionResult>> {
    private static final Log LOG = LogFactory.getLog(ConcurrentTestClient.class);

    private int index;
    private LoomClient client;
    private long duration;
    private int pause;
    private String providerType;
    private String providerId;
    private TestDataConfig testDataConfig;
    private int adapterIdx;

    private Random rand = new Random();
    private List<AssertionResult> assertionResults;

    private PatternDefinitionList patterns;
    private Predicate<ArrayList<QueryResultElement>> equalsNumInstances;
    private Predicate<ArrayList<QueryResultElement>> equalsNumVolumes;
    private Predicate<ArrayList<QueryResultElement>> equalsNumImages;
    private Predicate<ArrayList<QueryResultElement>> equalsNumNetworks;
    private Predicate<ArrayList<QueryResultElement>> equalsNumSubnets;

    public ConcurrentTestClient(final int index, final LoomClient client, final long duration,
            final TestDataConfig testDataConfig, final int adapterIdx) {
        this.index = index;
        this.client = client;
        this.testDataConfig = testDataConfig;
        this.adapterIdx = adapterIdx;
        providerType = testDataConfig.getProviderType(adapterIdx);
        providerId = testDataConfig.getProviderId(adapterIdx);
        this.duration = duration;
        int min = 100;
        int max = 500;
        pause = rand.nextInt(max - min + 1) + min;
        assertionResults = new ArrayList<AssertionResult>();
        Credentials credentials = new Credentials(testDataConfig.getUsername(), testDataConfig.getPassword());
        patterns = client.loginProvider(providerType, providerId, credentials);

        equalsNumInstances = (final ArrayList<QueryResultElement> list) -> (list.size() == testDataConfig
                .getExpectedInstanceNbr(FakeConfig.PRIVATE_INDEX));
        equalsNumVolumes = (final ArrayList<QueryResultElement> list) -> (list.size() == testDataConfig
                .getExpectedVolumeNbr(FakeConfig.PRIVATE_INDEX));
        equalsNumImages = (final ArrayList<QueryResultElement> list) -> (list.size() == testDataConfig
                .getExpectedImageNbr(FakeConfig.PRIVATE_INDEX));
        equalsNumNetworks = (final ArrayList<QueryResultElement> list) -> (list.size() == testDataConfig
                .getExpectedNetworkNbr(FakeConfig.PRIVATE_INDEX));
        equalsNumSubnets = (final ArrayList<QueryResultElement> list) -> (list.size() == testDataConfig
                .getExpectedSubnetNbr(FakeConfig.PRIVATE_INDEX));
    }

    @Override
    public synchronized List<AssertionResult> call() {
        long start = System.currentTimeMillis();
        try {
            for (int count = 0; System.currentTimeMillis() - start < duration; count++) {
                performOperations();
                LOG.debug("Client " + index + " performed operation " + count);
                try {
                    Thread.sleep(pause);
                } catch (InterruptedException e) {
                    LOG.debug("Thread in ConcurrentTestClient was interrupted: " + e.getStackTrace());
                }
            }
        } catch (Exception e) {
            LOG.error("Concurrent error in client " + index, e);
            assertionResults
                    .add(new AssertionResult("Unhandled exception in client " + index + " " + e.toString(), true));
        } finally {
            client.logoutProvider(providerType, providerId);
        }
        return assertionResults;
    }

    private void performOperations() throws Exception {
        PatternDefinition pattern = null;
        boolean threadsExists = false;

        if (patterns.getPatterns().size() > 0) {
            for (Iterator<PatternDefinition> patternIt = patterns.getPatterns().iterator(); patternIt.hasNext();) {
                pattern = patternIt.next();
                if (pattern.getId().endsWith(BaseOsAdapter.ALL_FIVE_PATTERN)) {
                    threadsExists = true;
                    break;
                }
            }

            if (!threadsExists) {
                assertionResults.add(new AssertionResult("Pattern - does not exist", true));
            } else {
                int expectedInstanceNumber = testDataConfig.getExpectedInstanceNbr(adapterIdx);
                int expectedVolumeNumber = testDataConfig.getExpectedVolumeNbr(adapterIdx);
                int expectedImageNumber = testDataConfig.getExpectedImageNbr(adapterIdx);
                int expectedNetworkNumber = testDataConfig.getExpectedNetworkNbr(adapterIdx);
                int expectedSubnetNumber = testDataConfig.getExpectedSubnetNbr(adapterIdx);

                TapestryDefinition tapestryDefinition = new TapestryDefinition();
                List<ThreadDefinition> threads = pattern.getThreads();

                if (threads == null) {
                    assertionResults.add(new AssertionResult("Thread is null", true));
                    throw new Exception("Thread is null");
                }

                if (threads.size() != 5) {
                    assertionResults.add(
                            new AssertionResult("The number of threads is " + threads.size() + ", expected 5", true));
                    throw new Exception("The number of threads is " + threads.size() + ", expected 5");
                }

                tapestryDefinition.setThreads(threads);
                String tapestryId = client.createTapestryDefinition(tapestryDefinition).getId();
                // String tapestryId =
                // verifyAndGetJsonId(client.createTapestryDefinition(tapestryDefinition));

                QueryResult qr = null;

                for (ThreadDefinition thread : threads) {
                    String threadId = thread.getId();
                    String itemType = thread.getItemType();

                    if (!(itemType.equals("os-instance") || itemType.equals("os-volume") || itemType.equals("os-image")
                            || itemType.equals("os-network") || itemType.equals("os-subnet"))) {
                        assertionResults.add(new AssertionResult("ItemType " + itemType + " is not recognised", true));
                        throw new Exception("ItemType " + itemType + " is not recognised");
                    }

                    if (itemType.equals("os-instance")) {
                        qr = BasicQueryOperations.getThreadWithWait(client, tapestryId, threadId, equalsNumInstances);
                        if (qr.getElements().size() != expectedInstanceNumber) {
                            assertionResults.add(new AssertionResult("There are " + qr.getElements().size()
                                    + " instances, expected " + expectedInstanceNumber, true));
                            throw new Exception("There are " + qr.getElements().size() + " instances, expected "
                                    + expectedInstanceNumber);
                        }
                    } else if (itemType.equals("os-volume")) {
                        qr = BasicQueryOperations.getThreadWithWait(client, tapestryId, threadId, equalsNumVolumes);
                        if (qr.getElements().size() != expectedVolumeNumber) {
                            assertionResults.add(new AssertionResult("There are " + qr.getElements().size()
                                    + " volumes, expected " + expectedVolumeNumber, true));
                            throw new Exception("There are " + qr.getElements().size() + " volumes, expected "
                                    + expectedVolumeNumber);
                        }
                    } else if (itemType.equals("os-image")) {
                        qr = BasicQueryOperations.getThreadWithWait(client, tapestryId, threadId, equalsNumImages);
                        if (qr.getElements().size() != expectedImageNumber) {
                            assertionResults.add(new AssertionResult(
                                    "There are " + qr.getElements().size() + " images, expected " + expectedImageNumber,
                                    true));
                            throw new Exception("There are " + qr.getElements().size() + " images, expected "
                                    + expectedImageNumber);
                        }
                    } else if (itemType.equals("os-network")) {
                        qr = BasicQueryOperations.getThreadWithWait(client, tapestryId, threadId, equalsNumNetworks);
                        if (qr.getElements().size() != expectedNetworkNumber) {
                            assertionResults.add(new AssertionResult("There are " + qr.getElements().size()
                                    + " networks, expected " + expectedNetworkNumber, true));
                            throw new Exception("There are " + qr.getElements().size() + " networks, expected "
                                    + expectedNetworkNumber);
                        }
                    } else if (itemType.equals("os-subnet")) {
                        qr = BasicQueryOperations.getThreadWithWait(client, tapestryId, threadId, equalsNumSubnets);
                        if (qr.getElements().size() != expectedSubnetNumber) {
                            assertionResults.add(new AssertionResult("There are " + qr.getElements().size()
                                    + " subnets, expected " + expectedSubnetNumber, true));
                            throw new Exception("There are " + qr.getElements().size() + " subnets, expected "
                                    + expectedSubnetNumber);
                        }
                    }
                    if (qr.getElements().get(0).getEntity().isAggregation()) {
                        assertionResults.add(new AssertionResult("Fibre should not be a da", true));
                        throw new Exception("Fibre should not be a da");
                    }
                }

                TapestryDefinition retrievedTapestryDefinition = client.getTapestry(tapestryId);
                compareThreads(threads, retrievedTapestryDefinition);

                for (ThreadDefinition thread : threads) {
                    thread.setId(UUID.randomUUID().toString());
                }

                tapestryDefinition.setThreads(threads);
                tapestryDefinition.setId(tapestryId);
                client.updateTapestryDefinition(tapestryId, tapestryDefinition);
                retrievedTapestryDefinition = client.getTapestry(tapestryId);

                compareThreads(threads, retrievedTapestryDefinition);
            }
        }
    }

    private void compareThreads(final List<ThreadDefinition> threads,
            final TapestryDefinition retrievedTapestryDefinition) throws Exception {
        for (ThreadDefinition thread : threads) {
            try {
                if (!thread.equals(retrievedTapestryDefinition.getThreadDefinition(thread.getId()))) {
                    assertionResults.add(new AssertionResult("Thread " + thread.getId() + " failed to update", true));
                    throw new Exception("Thread " + thread.getId() + " failed to update");
                } else {
                    LOG.info("Thread " + thread.getId() + " was updated successfully");
                }
            } catch (NoSuchThreadDefinitionException e) {
                assertionResults.add(new AssertionResult("Thread " + thread.getId() + " failed to update", true));
                throw new Exception("Thread " + thread.getId() + " failed to update");
            }
        }
    }

    private String verifyAndGetJsonId(String jsonId) {
        assertTrue(jsonId.contains("{"));
        assertTrue(jsonId.contains("}"));
        assertTrue(jsonId.contains("\"id\":"));
        assertNotNull("id was null", jsonId);
        jsonId = jsonId.replace("{", "");
        jsonId = jsonId.replace("}", "");
        jsonId = jsonId.replace("\"", "");
        String tokens[] = jsonId.split("\\s+");
        return tokens[tokens.length - 1];
    }
}
