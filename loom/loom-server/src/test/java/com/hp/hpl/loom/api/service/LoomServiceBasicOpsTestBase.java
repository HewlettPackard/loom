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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import com.hp.hpl.loom.api.client.LoomClientException;
import com.hp.hpl.loom.api.service.utils.TapestryHandling;
import com.hp.hpl.loom.exceptions.NoSuchThreadDefinitionException;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.ItemTypeList;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.ProviderList;
import com.hp.hpl.loom.model.QueryResult;
import com.hp.hpl.loom.model.QueryResultList;
import com.hp.hpl.loom.model.Status;
import com.hp.hpl.loom.tapestry.PatternDefinition;
import com.hp.hpl.loom.tapestry.PatternDefinitionList;
import com.hp.hpl.loom.tapestry.QueryDefinition;
import com.hp.hpl.loom.tapestry.TapestryDefinition;
import com.hp.hpl.loom.tapestry.TapestryDefinitionList;
import com.hp.hpl.loom.tapestry.ThreadDefinition;
import com.hp.hpl.loom.tapestry.ThreadDefinitionList;

public abstract class LoomServiceBasicOpsTestBase extends IntegrationTestBase {
    private static final Log LOG = LogFactory.getLog(LoomServiceBasicOpsTestBase.class);

    @Value("${api.rate.limit}")
    private Integer maxReqsPerSecond;

    @Before
    public void setUp() throws Exception {
        super.setUp(1);
        PatternDefinitionList patternDefinitionList = loginProvider();
        assertNotNull("Null response in getting PatternDefinitionList", patternDefinitionList);
        // Thread.sleep(1850); // leave adapter room to create the model
        List<PatternDefinition> patterns = patternDefinitionList.getPatterns();
        assertNotNull("Null response in getting patterns", patterns);
    }

    @After
    public void cleanUp() {
        try {
            loginProvider(); // we have to relogin to sthe logout works!
            logoutProvider();
        } catch (LoomClientException ex) {
            // we are have already logged out this errors.
        }
        LOG.info("Logged out");
    }

    @Test
    public void testLoginProviderWrongly() {
        LOG.info("testLoginProviderWrongly start");
        Credentials credentials = new Credentials("NotWorking", "NotWorking");
        try {
            client.loginProvider("os", "private", credentials);
            fail("Expecting an error");
        } catch (LoomClientException ex) {
            assertEquals(ex.getStatusCode(), HttpStatus.UNAUTHORIZED.value());
        }
        LOG.info("testLoginProviderWrongly end");
    }

    @Test
    public void testLogoutAllProviders() {
        LOG.info("testLogoutAllProviders start");
        Credentials credentials = new Credentials("test", "test");
        try {
            client.loginProvider("os", "private", credentials);
            client.getPatterns();
            client.logoutAllProviders();
            client.getPatterns();
            fail("Expecting an error");
        } catch (LoomClientException ex) {
            assertEquals(ex.getStatusCode(), HttpStatus.UNAUTHORIZED.value());
        }
        LOG.info("testLogoutAllProviders end");
    }

    @Test
    public void testGetPatterns() throws InterruptedException {
        LOG.info("testGetPatterns start");
        PatternDefinitionList patternDefinitionList = client.getPatterns();
        assertNotNull("PatternDefinitionList was null", patternDefinitionList);
        LOG.info("Returned list " + patternDefinitionList);

        LOG.info("testGetPatterns end");
    }

    @Test
    public void testGetPattern() throws InterruptedException {
        LOG.info("testGetPattern start");
        PatternDefinitionList patternDefinitionList = client.getPatterns();
        List<PatternDefinition> patternDefinitions = patternDefinitionList.getPatterns();
        if (patternDefinitions.size() > 0) {
            String patternId = patternDefinitions.iterator().next().getId();
            PatternDefinition patternDefinition = client.getPattern(patternId);
            assertNotNull("PaternDefinition was null", patternDefinition);
            LOG.info("Returned pattern " + patternDefinition);
        } else {
            LOG.info("There is no pattern");
        }
        LOG.info("testGetPattern end");
    }

    @Test
    public void testCreateTapestryDefinition() throws InterruptedException {
        LOG.info("testCreateTapestryDefinition start");
        TapestryHandling.createTapestryDefinition(client);
        LOG.info("testCreateTapestryDefinition end");
    }

    @Test
    public void testGetProviders() throws InterruptedException {
        LOG.info("testGetProviders start");
        ProviderList providerList = client.getProviders();
        assertNotNull("ProviderList was null", providerList);
        LOG.info("Returned list " + providerList);
        LOG.info("testGetProviders end");
    }

    @Test
    public void testGetProvidersOfType() throws InterruptedException {
        LOG.info("testGetProvidersOfType start");
        ProviderList providerList = client.getProviders("os");
        assertNotNull("ProviderList was null", providerList);
        LOG.info("Returned list " + providerList);
        LOG.info("testGetProvidersOfType end");
    }

    @Test
    public void testGetProvider() throws InterruptedException {
        LOG.info("testGetProvider start");
        Provider provider = client.getProvider("os", "private");
        assertNotNull("Provider was null", provider);
        LOG.info("Returned provider " + provider);
        LOG.info("testGetProvider end");
    }

    @Test
    public void testGetAggregation() throws InterruptedException {
        LOG.info("testGetAggregation start");
        QueryResult result = getAggregation();
        assertNotNull("Query was null", result);
        LOG.info("testGetAggregation end");
    }

    @Test
    public void testGetAggregations() throws InterruptedException {
        LOG.info("testGetAggregations start");
        QueryResultList result = getAggregations();
        assertNotNull("QueryResultList was null", result);
        assertTrue("There must not be any query results in the list, there are " + result.getQueryResults().size(),
                result.getQueryResults().size() == 0);
        LOG.info("testGetAggregations end");
    }

    private QueryResult getAggregation() {
        PatternDefinitionList patternDefinitionList = client.getPatterns();
        List<PatternDefinition> patternDefinitions = patternDefinitionList.getPatterns();

        if (patternDefinitions.size() > 0) {
            PatternDefinition patternDefinition = patternDefinitions.iterator().next();
            List<ThreadDefinition> threads = patternDefinition.getThreads();
            if (threads.size() > 0) {
                String tapestryId = TapestryHandling.createTapestryDefinition(client);
                ThreadDefinition thread = threads.iterator().next();
                String threadId = thread.getId();
                return client.getAggregation(tapestryId, threadId);
            }
        }
        return new QueryResult();
    }

    private QueryResultList getAggregations() {
        String tapestryId = TapestryHandling.createTapestryDefinition(client);
        return client.getAggregations(tapestryId, new ArrayList<String>(0));
    }

    @Test
    public void testDoubleLogin() {
        PatternDefinitionList patterns = loginProvider();
        assertNotNull("A list of patterns is expected", patterns);
    }

    @Test
    public void testGetTapestryDefinition() throws InterruptedException {
        LOG.info("testGetTapestryDefinition start");
        String tapestryId = TapestryHandling.createTapestryDefinition(client);
        TapestryDefinition tapestryDefinition = client.getTapestry(tapestryId);
        assertNotNull("TapestryDefinition with id " + tapestryId + " does not exist", tapestryDefinition);

        LOG.info("testGetTapestryDefinition end");
    }

    @Test
    public void testUpdateTapestryDefinition() throws InterruptedException {
        LOG.info("testUpdateTapestryDefinition start");
        String tapestryId = TapestryHandling.createTapestryDefinition(client);
        TapestryDefinition tapestryDefinition = client.getTapestry(tapestryId);

        String updatedInput = "";

        List<ThreadDefinition> threads = tapestryDefinition.getThreads();
        if (threads.size() > 0) {
            ThreadDefinition thread = threads.get(0);
            QueryDefinition queryDefinition = thread.getQuery();
            List<String> inputs = queryDefinition.getInputs();
            if (inputs.size() > 0) {
                String firstInput = queryDefinition.getInputs().get(0);
                LOG.info("originalInput: " + firstInput);
                updatedInput = firstInput;
                inputs.set(0, updatedInput);
                queryDefinition.setInputs(inputs);
            }
        }
        LOG.debug("Tapestry ID: " + tapestryId);
        client.updateTapestryDefinition(tapestryId, tapestryDefinition);
        tapestryDefinition = client.getTapestry(tapestryId);
        assertNotNull("Updated TapestryDefinition with id " + tapestryId + " does not exist", tapestryDefinition);

        threads = tapestryDefinition.getThreads();
        if (threads.size() > 0) {
            ThreadDefinition thread = threads.iterator().next();
            QueryDefinition queryDefinition = thread.getQuery();
            ArrayList<String> inputs = (ArrayList<String>) queryDefinition.getInputs();
            String retrievedInput = inputs.get(0);
            LOG.info("updatedInput: " + retrievedInput);
            assertTrue(retrievedInput.equals(updatedInput));
        }
        LOG.info("testUpdateTapestryDefinition end");
    }

    @Test
    public void testGetTapestryDefinitions() throws InterruptedException {
        // LOOM-1291
        LOG.info("testGetTapestryDefinitions start");
        TapestryHandling.createTapestryDefinition(client);
        TapestryDefinitionList tapestryDefinitionList = client.getTapestryDefinitions();
        assertTrue("TapestryDefinitionList should have size of 1", tapestryDefinitionList.getTapestries().size() == 1);
        LOG.info("testGetTapestryDefinitions end");
    }

    @Test
    public void testDeleteTapestryDefinition() throws InterruptedException {
        LOG.info("testDeleteTapestryDefinition start");
        String tapestryId = TapestryHandling.createTapestryDefinition(client);
        TapestryDefinitionList tapestryDefinitionList = client.getTapestryDefinitions();
        assertTrue("TapestryDefinitionList should have size of 1", tapestryDefinitionList.getTapestries().size() == 1);
        client.deleteTapestryDefinition(tapestryId);
        tapestryDefinitionList = client.getTapestryDefinitions();
        assertTrue(
                "TapestryDefinitionList should have size of 0, found " + tapestryDefinitionList.getTapestries().size(),
                tapestryDefinitionList.getTapestries().size() == 0);
        LOG.info("testDeleteTapestryDefinition end");
    }

    @Test
    public void testDeleteTapestryDefinitions() throws InterruptedException {
        LOG.info("testDeleteTapestryDefinitions start");
        // LOOM-1291 - try to add more tapestries here.
        TapestryHandling.createTapestryDefinition(client);
        TapestryDefinitionList tapestryDefinitionList = client.getTapestryDefinitions();
        assertTrue("TapestryDefinitionList should have size of 1", tapestryDefinitionList.getTapestries().size() == 1);
        client.deleteTapestryDefinitions();
        tapestryDefinitionList = client.getTapestryDefinitions();
        assertTrue(
                "TapestryDefinitionList should have size of 0, found " + tapestryDefinitionList.getTapestries().size(),
                tapestryDefinitionList.getTapestries().size() == 0);
        LOG.info("testDeleteTapestryDefinitions end");
    }

    @Test
    public void testCreateThreadDefinition() throws InterruptedException {
        LOG.info("testCreateThreadDefinition start");
        TapestryDefinition tapestryDefinition = TapestryHandling.createFullTapestryFromPatterns(client, 45);
        String tapestryId = tapestryDefinition.getId();
        List<ThreadDefinition> threads = tapestryDefinition.getThreads();
        if (threads.size() > 0) {
            String threadId = tapestryDefinition.getThreads().get(0).getId();
            ThreadDefinition threadDefinition = client.getThreadDefinition(tapestryId, threadId);
            ThreadDefinition newThreadDefinition =
                    new ThreadDefinition("testThreadId", threadDefinition.getItemType(), threadDefinition.getQuery());
            client.createThreadDefinition(tapestryId, newThreadDefinition);
            try {
                assertNotNull("The thread is not created correctly"
                        + client.getTapestry(tapestryId).getThreadDefinition("testThreadId"));
            } catch (NoSuchThreadDefinitionException | LoomClientException e) {
                assertTrue("This should not happen!", false);
            }
        }
        LOG.info("testCreateThreadDefinition end");
    }

    @Test
    public void testUpdateThreadDefinition() throws InterruptedException {
        LOG.info("testUpdateThreadDefinition start");
        TapestryDefinition tapestryDefinition = TapestryHandling.createFullTapestryFromPatterns(client, 45);
        String tapestryId = tapestryDefinition.getId();
        List<ThreadDefinition> threads = tapestryDefinition.getThreads();
        if (threads.size() > 0) {
            String threadId = tapestryDefinition.getThreads().get(0).getId();
            ThreadDefinition threadDefinition = client.getThreadDefinition(tapestryId, threadId);
            threadDefinition.setName("newName");
            client.updateThreadDefinition(tapestryId, threadId, threadDefinition);
            try {
                String threadName = client.getTapestry(tapestryId).getThreadDefinition(threadId).getName();
                assertTrue("The thread's name is not updated to: 'newName' but remain: " + threadName,
                        threadName.equals("newName"));
            } catch (NoSuchThreadDefinitionException | LoomClientException e) {
                assertTrue("This should not happen!", false);
            }
        }
        LOG.info("testUpdateThreadDefinition end");
    }

    @Test
    public void testGetThreadDefinition() throws InterruptedException {
        LOG.info("testGetThreadDefinition start");
        TapestryDefinition tapestryDefinition = TapestryHandling.createFullTapestryFromPatterns(client, 45);
        String tapestryId = tapestryDefinition.getId();
        if (tapestryDefinition.getThreads().size() > 0) {
            String threadId = tapestryDefinition.getThreads().get(0).getId();
            ThreadDefinition threadDefinition = client.getThreadDefinition(tapestryId, threadId);
            assertNotNull("Thread definition: " + threadId + " must not be null", threadDefinition);
        }
        LOG.info("testGetThreadDefinition end");
    }

    @Test
    public void testDeleteThreadDefinition() throws InterruptedException {
        LOG.info("testDeleteThreadDefinition start");
        TapestryDefinition tapestryDefinition = TapestryHandling.createFullTapestryFromPatterns(client, 45);
        String tapestryId = tapestryDefinition.getId();
        int noOfOriginalThreads = tapestryDefinition.getThreads().size();
        if (tapestryDefinition.getThreads().size() > 0) {
            String threadId = tapestryDefinition.getThreads().get(0).getId();
            client.deleteThreadDefinition(tapestryId, threadId);
            int noOfThreads = client.getTapestry(tapestryId).getThreads().size();
            assertTrue("Number of threads is: " + noOfThreads + ", expected " + (noOfOriginalThreads - 1),
                    noOfThreads == noOfOriginalThreads - 1);
        }
        LOG.info("testDeleteThreadDefinition end");
    }

    @Test
    public void testGetThreadDefinitions() throws InterruptedException {
        LOG.info("testGetThreadDefinitionList start");
        TapestryDefinition tapestryDefinition = TapestryHandling.createFullTapestryFromPatterns(client, 45);
        String tapestryId = tapestryDefinition.getId();
        ThreadDefinitionList threadDefinitionList = client.getThreadDefinitions(tapestryId);
        assertNotNull("Thread definition list must not be null", threadDefinitionList);
        LOG.info("testGetThreadDefinitionList end");
    }

    @Test
    public void testDeleteThreadDefinitions() throws InterruptedException {
        LOG.info("testDeleteThreadDefinitions start");
        TapestryDefinition tapestryDefinition = TapestryHandling.createFullTapestryFromPatterns(client, 45);
        String tapestryId = tapestryDefinition.getId();
        client.deleteThreadDefinitions(tapestryId);
        int noOfThreads = client.getTapestry(tapestryId).getThreads().size();
        assertTrue("Tapestry should have no thread, found " + noOfThreads, noOfThreads == 0);
        LOG.info("testDeleteThreadDefinitions end");
    }

    @Test
    public void testGetItemTypes() throws InterruptedException {
        LOG.info("testGetItemTypes start");
        ProviderList providerList = client.getProviders();
        if (providerList.getProviders().size() > 0) {
            Provider provider = providerList.getProviders().get(0);
            ItemTypeList itemTypeList = client.getItemTypes(provider.getProviderType());
            assertNotNull("ItemTypeList must not be null", itemTypeList);
            itemTypeList = client.getItemTypes(provider.getProviderType(), provider.getProviderId());
            assertNotNull("ItemTypeList must not be null", itemTypeList);
            itemTypeList = client.getItemTypes();
            assertNotNull("ItemTypeList must not be null", itemTypeList);
        }
        LOG.info("testGetItemTypes end");
    }

    @Test
    public void testGetStatus() throws InterruptedException {
        LOG.info("testGetStatus start");
        Status status = client.getStatus();
        assertNotNull("Status was null", status);
        assertNotNull("Status.build was null", status.getBuild());
        assertNotNull("Status.version was null", status.getVersion());
        LOG.info("Returned status " + status.getVersion() + " " + status.getBuild());
        LOG.info("testGetStatus end");
    }

    @Test
    public void testGetOperations() throws InterruptedException {
        LOG.info("testGetOperations start");
        ProviderList providerList = client.getProviders();
        if (providerList.getProviders().size() > 0) {
            Provider provider = providerList.getProviders().get(0);
            assertNotNull("getOperations must return an OperationList, returned null",
                    client.getOperations(provider.getProviderType(), null));
            assertNotNull("getOperations must return an OperationList, returned null",
                    client.getOperations("any", null));
            assertNotNull("getOperations must return an OperationList, returned null",
                    client.getOperations(null, provider.getProviderType()));
            assertNotNull("getOperations must return an OperationList, returned null",
                    client.getOperations(null, "loom"));
        }
        LOG.info("testGetOperations end");
    }

    @Test(expected = LoomClientException.class)
    public void testGetOperationsWithBadRequest() throws InterruptedException {
        LOG.info("testGetOperationsWithBadRequest start");
        client.getOperations("any", "loom");
        LOG.info("testGetOperationsWithBadRequest end");
    }

    // @Test(expected = ApiThrottlingException.class)
    // public void testRateLimiting() throws InterruptedException, ExecutionException {
    // ExecutorService threadPool = Executors.newFixedThreadPool(300);
    // CompletionService<String> pool = new ExecutorCompletionService<String>(threadPool);
    //
    // for (int i = 0; i < 300; i++) {
    // pool.submit(new GetStatusTask());
    // }
    //
    //
    // threadPool.shutdown();
    // }
    //
    // private final class GetStatusTask implements Callable<String> {
    // public String call() {
    // client.getStatus();
    // return "Run";
    // }
    // }
}
