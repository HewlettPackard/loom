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
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.loom.adapter.os.OsInstanceType;
import com.hp.hpl.loom.api.service.utils.ActionHandling;
import com.hp.hpl.loom.api.service.utils.BasicQueryOperations;
import com.hp.hpl.loom.api.service.utils.DrillDownThreads;
import com.hp.hpl.loom.api.service.utils.QueryFormatting;
import com.hp.hpl.loom.api.service.utils.TapestryHandling;
import com.hp.hpl.loom.api.service.utils.ThreadNavigation;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.manager.query.QueryOperation;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.ProviderImpl;
import com.hp.hpl.loom.tapestry.PatternDefinitionList;
import com.hp.hpl.loom.tapestry.QueryDefinition;
import com.hp.hpl.loom.tapestry.TapestryDefinition;
import com.hp.hpl.loom.tapestry.ThreadDefinition;


/**
 * Example base class for Loom service integration tests.
 */
public abstract class LoomServiceSoakTestBase extends IntegrationTestBase {
    private static final Log LOG = LogFactory.getLog(LoomServiceSoakTestBase.class);
    private Provider provider = new ProviderImpl("os", "providerId", "authEndpoint", "providerName", "adapterPackage");

    @Before
    public void setUp() throws Exception {
        super.setUp(1);
        PatternDefinitionList patternDefinitionList = loginProvider();
        assertNotNull("Null response in getting PatternDefinitionList", patternDefinitionList);
    }

    @After
    public void cleanUp() {
        logoutProvider();
        LOG.info("Logged out");
    }

    private void setRndInstancesOp(final TapestryDefinition tapestryDefinition, final String op, final int maxFibres) {
        // Find instances thread
        ThreadDefinition instancesThread = TapestryHandling
                .findThreadDefinitionWithItemType(tapestryDefinition.getThreads(), OsInstanceType.TYPE_LOCAL_ID);
        // And modify with a random op parameter
        OsInstanceType oit = new OsInstanceType(provider);
        oit.setId(oit.getLocalId());
        QueryDefinition query = instancesThread.getQuery();
        if (op.equals(DefaultOperations.SORT_BY.toString())) {
            Map<String, Object> params =
                    ThreadNavigation.populateRandomProperty(DefaultOperations.SORT_BY.toString(), oit);
            if (Math.random() > 0.5) {
                params.put(QueryOperation.ORDER, QueryOperation.DSC_ORDER);
            } else {
                params.put(QueryOperation.ORDER, QueryOperation.ASC_ORDER);
            }
            query = QueryFormatting.createSortByBraidQuery(query.getInputs(), params, maxFibres);
        }
        if (op.equals(DefaultOperations.GROUP_BY.toString())) {
            Map<String, Object> groupParams =
                    ThreadNavigation.populateRandomProperty(DefaultOperations.GROUP_BY.toString(), oit);
            query = QueryFormatting.createGroupByBraidQuery(groupParams, maxFibres, query.getInputs());
        }
        instancesThread.setQuery(query);
    }

    /**
     * Do an initial drill down to reach Instance items, then soak test of query with no updates to
     * the tapestry, but induce changes to the system.
     *
     * @throws InterruptedException
     */
    private void doSoakTest(final String testName, final boolean drillDown, final boolean reboot,
            final boolean removeDrillDown, final boolean randomDrillDown, final boolean randomFibres, final String op)
            throws InterruptedException {
        // Create a tapestry containing all of the threads
        int maxFibres = 45;
        if (randomFibres) {
            maxFibres = 15 + (int) (Math.random() * ((100 - 15) + 1)); // anywhere in the range
                                                                       // [15,100]
        }

        TapestryDefinition tapestryDefinition = TapestryHandling.createFullTapestryFromPatterns(client, maxFibres);
        // Add extra threads to drill down to Instance items
        int soakTime = getSoakTimeMins() * 60000;
        long startTime = Calendar.getInstance().getTimeInMillis();
        DrillDownThreads drillDownThreads = null;
        for (int count = 0; (Calendar.getInstance().getTimeInMillis() - startTime) < soakTime; count++) {
            if (count % 5 == 0) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(testName + count + " iteration");
                }
            }

            if (op != null) {
                // TODO Allow random op selection to work with rest of test parameterisation
                assertFalse("drillDown not supported with random op", drillDown);
                assertFalse("reboot not supported with random op", reboot);
                assertFalse("removeDrillDown not supported with random op", removeDrillDown);
                assertFalse("randomDrillDown not supported with random op", randomDrillDown);
                assertFalse("randomFibres not supported with random op", randomFibres);

                // Randomize the op on the query for instances
                setRndInstancesOp(tapestryDefinition, op, maxFibres);
                TapestryHandling.clientUpdateTapestryFromTapestryDefinition(client, tapestryDefinition);
            }

            if (drillDown && drillDownThreads == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(testName + count + " drill down");
                }
                drillDownThreads = ThreadNavigation.drillDownOnTypeId(client, tapestryDefinition,
                        OsInstanceType.TYPE_LOCAL_ID, maxFibres, randomDrillDown);
            }

            if (drillDown && reboot) {
                // Sometimes cause some dynamic changes
                if (count % 3 == 0) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(testName + count + " reboot instance");
                    }
                    ActionHandling.pickAnInstanceAndReboot(client, tapestryDefinition.getId(),
                            drillDownThreads.getItemThreadId());
                }
            }

            BasicQueryOperations.queryAllThreads(client, tapestryDefinition);

            if (drillDown && removeDrillDown) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(testName + count + " remove from tapestry " + drillDownThreads.getNewThreadIds());
                }
                TapestryHandling.removeThreadsFromTapestry(client, tapestryDefinition,
                        drillDownThreads.getNewThreadIds());
                drillDownThreads = null;
                BasicQueryOperations.queryAllThreads(client, tapestryDefinition);
            }

            if (!isSoakEnabled()) {
                break;
            }
        }
    }

    /**
     * Soak test of query with no updates to the tapestry, and static system.
     *
     * @throws InterruptedException
     */
    @Test
    public void testSoakStaticQueryStaticSystem() throws InterruptedException {
        LOG.info("testSoakStaticQueryStaticSystem start");
        doSoakTest("SoakStaticQueryStaticSystem", false, false, false, false, false, null);
        LOG.info("testSoakStaticQueryStaticSystem end");
    }

    @Test
    public void testSoakStaticQueryDynamicSystem() throws InterruptedException {
        LOG.info("testSoakStaticQueryDynamicSystem start");
        doSoakTest("SoakStaticQueryDynamicSystem", true, true, false, false, false, null);
        LOG.info("testSoakStaticQueryDynamicSystem end");
    }

    @Test
    public void testSoakDynamicQueryDynamicSystem() throws InterruptedException {
        LOG.info("testSoakDynamicQueryDynamicSystem start");
        doSoakTest("SoakDynamicQueryDynamicSystem", true, true, true, false, false, null);
        LOG.info("testSoakStaticQueryDynamicSystem end");
    }

    @Test
    public void testRandomSoakStaticQueryStaticSystem() throws InterruptedException {
        LOG.info("testRandomSoakStaticQueryStaticSystem start");
        doSoakTest("RandomSoakStaticQueryStaticSystem", false, false, false, true, false, null);
        LOG.info("testRandomSoakStaticQueryStaticSystem end");
    }

    @Test
    public void testRandomSoakStaticQueryDynamicSystem() throws InterruptedException {
        LOG.info("testRandomSoakStaticQueryDynamicSystem start");
        doSoakTest("RandomSoakStaticQueryDynamicSystem", true, true, false, true, false, null);
        LOG.info("testRandomSoakStaticQueryDynamicSystem end");
    }

    @Test
    public void testRandomSoakDynamicQueryDynamicSystem() throws InterruptedException {
        LOG.info("testRandomSoakDynamicQueryDynamicSystem start");
        doSoakTest("RandomSoakDynamicQueryDynamicSystem", true, true, true, true, false, null);
        LOG.info("testRandomSoakStaticQueryDynamicSystem end");
    }

    @Test
    public void testSoakStaticQueryStaticSystemRndFibre() throws InterruptedException {
        LOG.info("testSoakStaticQueryStaticSystemRndFibre start");
        doSoakTest("SoakStaticQueryStaticSystemRndFibre", false, false, false, false, true, null);
        LOG.info("testSoakStaticQueryStaticSystemRndFibre end");
    }

    @Test
    public void testSoakStaticQueryDynamicSystemRndFibre() throws InterruptedException {
        LOG.info("testSoakStaticQueryDynamicSystemRndFibre start");
        doSoakTest("SoakStaticQueryDynamicSystemRndFibre", true, true, false, false, true, null);
        LOG.info("testSoakStaticQueryDynamicSystemRndFibre end");
    }

    @Test
    public void testSoakDynamicQueryDynamicSystemRndFibre() throws InterruptedException {
        LOG.info("testSoakDynamicQueryDynamicSystemRndFibre start");
        doSoakTest("SoakDynamicQueryDynamicSystemRndFibre", true, true, true, false, true, null);
        LOG.info("testSoakStaticQueryDynamicSystemRndFibre end");
    }

    @Test
    public void testRandomSoakStaticQueryStaticSystemRndFibre() throws InterruptedException {
        LOG.info("testRandomSoakStaticQueryStaticSystemRndFibre start");
        doSoakTest("RandomSoakStaticQueryStaticSystemRndFibre", false, false, false, true, true, null);
        LOG.info("testRandomSoakStaticQueryStaticSystemRndFibre end");
    }

    @Test
    public void testRandomSoakStaticQueryDynamicSystemRndFibre() throws InterruptedException {
        LOG.info("testRandomSoakStaticQueryDynamicSystemRndFibre start");
        doSoakTest("RandomSoakStaticQueryDynamicSystemRndFibre", true, true, false, true, true, null);
        LOG.info("testRandomSoakStaticQueryDynamicSystemRndFibre end");
    }

    @Test
    public void testRandomSoakDynamicQueryDynamicSystemRndFibre() throws InterruptedException {
        LOG.info("testRandomSoakDynamicQueryDynamicSystemRndFibre start");
        doSoakTest("RandomSoakDynamicQueryDynamicSystemRndFibre", true, true, true, true, true, null);
        LOG.info("testRandomSoakStaticQueryDynamicSystemRndFibre end");
    }


    @Test
    public void testRandomSoakGroupDynamicQueryStaticSystem() throws InterruptedException {
        LOG.info("testRandomSoakGroupDynamicQueryStaticSystem start");
        String op = DefaultOperations.GROUP_BY.toString();
        doSoakTest("testRandomSoakGroupDynamicQueryStaticSystem", false, false, false, false, false, op);
        LOG.info("testRandomSoakGroupDynamicQueryStaticSystem end");
    }

    /**
     * Exercise group and sort by at different drill down levels based on random attribute selection
     */
    private void doRandomNavigationTest(final String testName, final boolean drillDown, final boolean reboot,
            final boolean removeDrillDown, final List<String> opPipeline) throws InterruptedException {
        // Create a tapestry containing all of the threads

        int min = 15;
        int max = 100;
        int maxFibres = min + (int) (Math.random() * ((max - min) + 1));
        LOG.info("testing " + testName + " with " + maxFibres);
        TapestryDefinition tapestryDefinition = TapestryHandling.createFullTapestryFromPatterns(client, maxFibres);
        // Add extra threads to drill down to Instance items
        int soakTime = getSoakTimeMins() * 60000;
        long startTime = Calendar.getInstance().getTimeInMillis();

        DrillDownThreads drillDownThreads = null;
        for (int count = 0; (Calendar.getInstance().getTimeInMillis() - startTime) < soakTime; count++) {
            if (count % 5 == 0) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(testName + count + " iteration");
                }
            }

            if (drillDown && opPipeline.size() != 0 && drillDownThreads == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(testName + count + " executing op pipeline");
                }
                for (String opName : opPipeline) {
                    OsInstanceType oit = new OsInstanceType(provider);
                    oit.setId(oit.getLocalId());
                    if (drillDownThreads == null) {
                        drillDownThreads = ThreadNavigation.execOperationWithRandomParamsOnTypeId(client,
                                tapestryDefinition, opName, oit, maxFibres, true, null);
                    } else {
                        drillDownThreads = ThreadNavigation.execOperationWithRandomParamsOnTypeId(client,
                                tapestryDefinition, opName, oit, maxFibres, true, drillDownThreads);
                    }
                }
            }

            if (drillDown && drillDownThreads == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(testName + count + " drill down");
                }
                drillDownThreads = ThreadNavigation.drillDownOnTypeId(client, tapestryDefinition,
                        OsInstanceType.TYPE_LOCAL_ID, maxFibres, true);
            }

            if (drillDown && reboot) {
                // Sometimes cause some dynamic changes
                if (count % 3 == 0) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(testName + count + " reboot instance");
                    }
                    ActionHandling.pickAnInstanceAndReboot(client, tapestryDefinition.getId(),
                            drillDownThreads.getItemThreadId());
                }
            }

            BasicQueryOperations.queryAllThreads(client, tapestryDefinition);

            if (drillDown && removeDrillDown) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(testName + count + " remove from tapestry " + drillDownThreads.getNewThreadIds());
                }
                TapestryHandling.removeThreadsFromTapestry(client, tapestryDefinition,
                        drillDownThreads.getNewThreadIds());
                drillDownThreads = null;
                BasicQueryOperations.queryAllThreads(client, tapestryDefinition);
            }

            if (!isSoakEnabled()) {
                break;
            }
        }
    }



    @Test
    public void testSortNavigationStaticQueryStaticSystem() throws InterruptedException {
        LOG.info("testSortNavigationStaticQueryStaticSystem start");
        List<String> ops = new ArrayList<>(1);
        ops.add(DefaultOperations.SORT_BY.toString());
        doRandomNavigationTest("testSortNavigationStaticQueryStaticSystem", false, false, false, ops);
        LOG.info("testSortNavigationStaticQueryStaticSystem stop");
    }

    @Test
    public void testGroupNavigationStaticQueryStaticSystem() throws InterruptedException {
        LOG.info("testGroupNavigationStaticQueryStaticSystem start");
        List<String> ops = new ArrayList<>(1);
        ops.add(DefaultOperations.GROUP_BY.toString());
        doRandomNavigationTest("testGroupNavigationStaticQueryStaticSystem", false, false, false, ops);
        LOG.info("testGroupNavigationStaticQueryStaticSystem stop");
    }

    @Test
    public void testGroupNavigationDynamicQueryStaticSystem() throws InterruptedException {
        LOG.info("testGroupNavigationDynamicQueryStaticSystem start");
        List<String> ops = new ArrayList<>(1);
        ops.add(DefaultOperations.GROUP_BY.toString());
        doRandomNavigationTest("testGroupNavigationDynamicQueryStaticSystem", true, false, true, ops);
        LOG.info("testGroupNavigationDynamicQueryStaticSystem stop");
    }

    @Test
    public void testGroupSortNavigationStaticQueryStaticSystem() throws InterruptedException {
        LOG.info("testGroupSortNavigationStaticQueryStaticSystem start");
        List<String> ops = new ArrayList<>(1);
        ops.add(DefaultOperations.GROUP_BY.toString());
        ops.add(DefaultOperations.SORT_BY.toString());
        doRandomNavigationTest("testGroupSortNavigationStaticQueryStaticSystem", false, false, false, ops);
        LOG.info("testGroupSortNavigationStaticQueryStaticSystem stop");
    }

    @Test
    public void testSortGroupNavigationStaticQueryStaticSystem() throws InterruptedException {
        LOG.info("testRandomGroupSortNavigationStaticQueryStaticSystem start");
        List<String> ops = new ArrayList<>(1);
        ops.add(DefaultOperations.SORT_BY.toString());
        ops.add(DefaultOperations.GROUP_BY.toString());
        doRandomNavigationTest("testRandomGroupSortNavigationStaticQueryStaticSystem", false, false, false, ops);
        LOG.info("testRandomGroupSortNavigationStaticQueryStaticSystem stop");
    }



    @Test
    public void testSortNavigationStaticQueryDynamicSystem() throws InterruptedException {
        LOG.info("testSortNavigationStaticQueryDynamicSystem start");
        List<String> ops = new ArrayList<>(1);
        ops.add(DefaultOperations.SORT_BY.toString());
        doRandomNavigationTest("testSortNavigationStaticQueryDynamicSystem", true, true, false, ops);
        LOG.info("testSortNavigationStaticQueryDynamicSystem stop");
    }

    @Test
    public void testGroupNavigationStaticQueryDynamicSystem() throws InterruptedException {
        LOG.info("testRandomSortNavigationStaticQueryDynamicSystem start");
        List<String> ops = new ArrayList<>(1);
        ops.add(DefaultOperations.GROUP_BY.toString());
        doRandomNavigationTest("testRandomSortNavigationStaticQueryDynamicSystem", true, true, false, ops);
        LOG.info("testRandomSortNavigationStaticQueryDynamicSystem stop");
    }

    @Test
    public void testGroupSortNavigationStaticQueryDynamicSystem() throws InterruptedException {
        LOG.info("testRandomSortGroupNavigationStaticQueryDynamicSystem start");
        List<String> ops = new ArrayList<>(1);
        ops.add(DefaultOperations.GROUP_BY.toString());
        ops.add(DefaultOperations.SORT_BY.toString());
        doRandomNavigationTest("testRandomSortGroupNavigationStaticQueryDynamicSystem", true, true, false, ops);
        LOG.info("testRandomSortGroupNavigationStaticQueryDynamicSystem stop");
    }

    @Test
    public void testSortGroupRandomGroupNavigationStaticQueryDynamicSystem() throws InterruptedException {
        LOG.info("testRandomSortRandomGroupNavigationStaticQueryDynamicSystem start");
        List<String> ops = new ArrayList<>(1);
        ops.add(DefaultOperations.SORT_BY.toString());
        ops.add(DefaultOperations.GROUP_BY.toString());
        doRandomNavigationTest("testRandomSortGroupNavigationStaticQueryDynamicSystem", true, true, false, ops);
        LOG.info("testRandomSortRandomGroupNavigationStaticQueryDynamicSystem stop");
    }



    @Test
    public void testSortNavigationDynamicQueryDynamicSystem() throws InterruptedException {
        LOG.info("testSortNavigationDynamicQueryDynamicSystem start");
        List<String> ops = new ArrayList<>(1);
        ops.add(DefaultOperations.SORT_BY.toString());
        doRandomNavigationTest("testSortNavigationDynamicQueryDynamicSystem", true, true, true, ops);
        LOG.info("testSortNavigationDynamicQueryDynamicSystem stop");
    }

    @Test
    public void testGroupNavigationDynamicQueryDynamicSystem() throws InterruptedException {
        LOG.info("testGroupNavigationDynamicQueryDynamicSystem start");
        List<String> ops = new ArrayList<>(1);
        ops.add(DefaultOperations.GROUP_BY.toString());
        doRandomNavigationTest("testGroupNavigationDynamicQueryDynamicSystem", true, true, true, ops);
        LOG.info("testGroupNavigationDynamicQueryDynamicSystem stop");
    }

    @Test
    public void testGroupSortNavigationDynamicQueryDynamicSystem() throws InterruptedException {
        LOG.info("testRandomSortGroupNavigationDynamicQueryDynamicSystem start");
        List<String> ops = new ArrayList<>(1);
        ops.add(DefaultOperations.GROUP_BY.toString());
        ops.add(DefaultOperations.SORT_BY.toString());
        doRandomNavigationTest("testRandomSortGroupNavigationDynamicQueryDynamicSystem", true, true, true, ops);
        LOG.info("testRandomSortGroupNavigationDynamicQueryDynamicSystem stop");
    }

    @Test
    public void testSortGroupNavigationDynamicQueryDynamicSystem() throws InterruptedException {
        LOG.info("testRandomSortRandomGroupNavigationDynamicQueryDynamicSystem start");
        List<String> ops = new ArrayList<>(1);
        ops.add(DefaultOperations.SORT_BY.toString());
        ops.add(DefaultOperations.GROUP_BY.toString());
        doRandomNavigationTest("testRandomSortGroupNavigationDynamicQueryDynamicSystem", true, true, true, ops);
        LOG.info("testRandomSortRandomGroupNavigationDynamicQueryDynamicSystem stop");
    }

}
