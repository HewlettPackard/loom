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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.hp.hpl.loom.adapter.os.fake.FakeConfig;
import com.hp.hpl.loom.api.client.LoomClient;
import com.hp.hpl.loom.api.client.LoomMvcClient;
import com.hp.hpl.loom.api.client.RestClient;
import com.hp.hpl.loom.manager.adapter.AdapterLoader;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.QueryResultElement;
import com.hp.hpl.loom.tapestry.PatternDefinitionList;

/**
 * Base class for integration testing of both local and remote Loom Server.
 */

public abstract class IntegrationTestBase {
    private static final Log LOG = LogFactory.getLog(IntegrationTestBase.class);

    public static final long WAIT_TIME = 2 * 1000;
    public static final int MAX_ATTEMPTS = 20;

    public static final Predicate<ArrayList<QueryResultElement>> notEquals0 =
            (final ArrayList<QueryResultElement> list) -> (list.size() != 0);
    public static final Predicate<ArrayList<QueryResultElement>> greaterThan0 =
            (final ArrayList<QueryResultElement> list) -> (list.size() > 0);

    private static boolean waited = false;

    protected Predicate<ArrayList<QueryResultElement>> equalsTestNumOfFibres;
    protected Predicate<ArrayList<QueryResultElement>> equalsStandardNumOfFibres;
    protected Predicate<ArrayList<QueryResultElement>> equalsNumImagesPrivateProvider;
    protected Predicate<ArrayList<QueryResultElement>> equalsNumSubnetsPrivateProvider;
    protected Predicate<ArrayList<QueryResultElement>> equalsNumInstancesPrivateProvider;
    protected Predicate<ArrayList<QueryResultElement>> equalsNumInstancesPublicProvider;
    protected Predicate<ArrayList<QueryResultElement>> equalsNumInstancesPrivateAndPublicProvider;

    protected int numElemsFirstGroupByFlavour = 2;

    // protected int[] projectNbr,regionNbr,instanceNbr,expectedInstanceNbr;
    // protected int[]
    // imageNbr,volsPerVm,extraVols,volSizeMax,sizeSteps,subsPerVm,extraNets,subsPerExtraNet;
    protected int testFibres, standardFibres;

    private boolean isMvc = true;

    protected List<LoomClient> clients;
    protected LoomClient client;

    // Create this after construction so we can pick up specific config parameters from the test
    // adapter(s).
    protected TestDataConfig testDataConfig = new FakeTestDataConfig2();

    @Autowired
    private AdapterLoader adapterLoader;

    // ///////////////////////////////////////////////////////////////////////
    // Values set from properties
    // ///////////////////////////////////////////////////////////////////////

    /** true if should run soak tests. */
    private boolean soak = false;
    private int soakTimeMins = 1;

    abstract void setupTestAndClient(int noOfClients);

    protected boolean isMvc() {
        return isMvc;
    }

    protected void setUp(final int noOfClients) throws Exception {
        setupTestAndClient(noOfClients);
        soak = testDataConfig.getSoak();
        soakTimeMins = testDataConfig.getSoakTimeMins();

        equalsNumImagesPrivateProvider = (final ArrayList<QueryResultElement> list) -> (list.size() == testDataConfig
                .getExpectedImageNbr(FakeConfig.PRIVATE_INDEX));

        equalsNumSubnetsPrivateProvider = (final ArrayList<QueryResultElement> list) -> (list.size() == testDataConfig
                .getExpectedSubnetNbr(FakeConfig.PRIVATE_INDEX));

        equalsNumInstancesPrivateProvider = (final ArrayList<QueryResultElement> list) -> (list.size() == testDataConfig
                .getExpectedInstanceNbr(FakeConfig.PRIVATE_INDEX));

        equalsNumInstancesPublicProvider = (final ArrayList<QueryResultElement> list) -> (list.size() == testDataConfig
                .getExpectedInstanceNbr(FakeConfig.PUBLIC_INDEX));

        equalsNumInstancesPrivateAndPublicProvider = (final ArrayList<QueryResultElement> list) -> (list
                .size() == testDataConfig.getExpectedInstanceNbr(FakeConfig.PRIVATE_INDEX)
                        + testDataConfig.getExpectedInstanceNbr(FakeConfig.PUBLIC_INDEX));

        testFibres = testDataConfig.getBraidTest();
        standardFibres = testDataConfig.getBraidClient();
        equalsTestNumOfFibres = (final ArrayList<QueryResultElement> list) -> (list.size() == testFibres);
        equalsStandardNumOfFibres = (final ArrayList<QueryResultElement> list) -> (list.size() == standardFibres);
    }

    protected boolean isSoakEnabled() {
        return soak;
    }

    protected int getSoakTimeMins() {
        return soakTimeMins;
    }

    /**
     * Use this in setupTestAndClient to connect to a remote REST server.
     */
    protected void setupRemoteClient(final int noOfClients) {
        LOG.info("setupRemoteClient enter");
        isMvc = false;

        // try {
        // Connect to Loom
        String restUri = testDataConfig.getAggregatorUri();

        clients = new ArrayList<LoomClient>(noOfClients);
        for (int i = 0; i < noOfClients; i++) {
            RestClient restClient = new RestClient();
            restClient.setBaseURL(restUri);
            clients.add(restClient);
            if (i == 0) {
                client = restClient;
            }
        }
        // } catch (IOException e) {
        // LOG.error("Failed to load properties file '" + deploymentProperties + "'", e);
        // throw new RuntimeException("Failed to initialise from properties file " +
        // deploymentProperties);
        // }
        LOG.info("setupRemoteClient exit");
    }

    private void waitForStartup() {
        if (!waited) {
            waited = true;
            // Give the adapters time to retrieve the data
            try {
                java.lang.Thread.sleep(WAIT_TIME);
            } catch (InterruptedException e) {
            }
        }
    }

    protected void setupMvcClient(final WebApplicationContext ctx, final int noOfClients) {
        LOG.info("setupMvcClient start");
        isMvc = true;
        // loadProperties();
        clients = new ArrayList<LoomClient>(noOfClients);
        for (int i = 0; i < noOfClients; i++) {
            MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
            clients.add(new LoomMvcClient(mockMvc));
            if (i == 0) {
                client = clients.get(0);
            }
            waitForStartup();
        }

        LOG.info("setupMvcClient end");
    }

    protected PatternDefinitionList loginProvider() {
        Credentials credentials = new Credentials(testDataConfig.getUsername(), testDataConfig.getPassword());
        return client.loginProvider("os", "private", credentials);
    }

    protected void logoutProvider() {
        client.logoutProvider("os", "private");
    }

}
