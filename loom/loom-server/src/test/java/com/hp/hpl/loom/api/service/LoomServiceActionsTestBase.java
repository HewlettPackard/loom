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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.loom.adapter.os.BaseOsAdapter;
import com.hp.hpl.loom.adapter.os.OsInstance;
import com.hp.hpl.loom.adapter.os.OsInstanceType;
import com.hp.hpl.loom.adapter.os.fake.FakeConfig;
import com.hp.hpl.loom.api.service.utils.ActionHandling;
import com.hp.hpl.loom.api.service.utils.BasicQueryOperations;
import com.hp.hpl.loom.api.service.utils.TapestryHandling;
import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionResult;
import com.hp.hpl.loom.model.QueryResult;
import com.hp.hpl.loom.tapestry.PatternDefinition;
import com.hp.hpl.loom.tapestry.PatternDefinitionList;
import com.hp.hpl.loom.tapestry.TapestryDefinition;
import com.hp.hpl.loom.tapestry.ThreadDefinition;

public abstract class LoomServiceActionsTestBase extends IntegrationTestBase {
    private static final Log LOG = LogFactory.getLog(LoomServiceActionsTestBase.class);

    private TapestryDefinition tapestryDefinition;

    @After
    public void cleanUp() {
        logoutProvider();
        LOG.info("Logged out");
    }

    @Before
    public void setUp() throws Exception {
        super.setUp(1);
        PatternDefinitionList patternDefinitionList = loginProvider();
        assertNotNull("Null response in getting PatternDefinitionList", patternDefinitionList);
        // Thread.sleep(1850); // leave adapter room to create the model
        List<PatternDefinition> patterns = patternDefinitionList.getPatterns();
        assertNotNull("Null response in getting patterns", patterns);
        tapestryDefinition =
                TapestryHandling.createTapestryFromPatternId(client, "os-" + BaseOsAdapter.ALL_FIVE_PATTERN);
        assertEquals("Incorrect number of threads", 5, tapestryDefinition.getThreads().size());
    }

    @Test
    public void testExecuteStopStartOnInstanceAggregation() throws InterruptedException {
        LOG.info("Test execute action start");
        ThreadDefinition thread = TapestryHandling.findThreadDefinitionWithItemType(tapestryDefinition.getThreads(),
                OsInstanceType.TYPE_LOCAL_ID);
        String threadId = thread.getId();
        // get the result
        QueryResult qr1 = BasicQueryOperations.getThreadWithWait(client, tapestryDefinition.getId(), threadId,
                equalsNumInstancesPrivateProvider);
        assertEquals("Incorrect number of instances", testDataConfig.getExpectedInstanceNbr(FakeConfig.PRIVATE_INDEX),
                qr1.getElements().size());
        assertTrue("fibre should not be a da", !qr1.getElements().get(0).getEntity().isAggregation());

        // stop the whole aggregation but check only the first
        OsInstance vm = (OsInstance) (qr1.getElements().get(0).getEntity());
        String vmId = vm.getCore().getItemId();

        Action stopAction = ActionHandling.createPowerAction("stop");
        stopAction.setTargets(Arrays.asList(qr1.getLogicalId()));
        ActionResult result = client.executeAction(stopAction);

        assertEquals(ActionResult.Status.completed, result.getOverallStatus());
        // assertEquals(ActionResult.Status.pending, result.getOverallStatus());
        // Thread.sleep(1000);
        // result = client.actionResultStatus(result.getId().toString());
        // assertEquals(ActionResult.Status.completed, result.getOverallStatus());

        ActionHandling.checkTargetsPowerStatus(client, "SHUTOFF", tapestryDefinition.getId(), threadId, vmId);

        Action startAction = ActionHandling.createPowerAction("start");
        startAction.setTargets(Arrays.asList(qr1.getLogicalId()));
        result = client.executeAction(startAction);

        assertEquals(ActionResult.Status.completed, result.getOverallStatus());
        // assertEquals(ActionResult.Status.pending, result.getOverallStatus());
        // Thread.sleep(1000);
        // result = client.actionResultStatus(result.getId().toString());
        // assertEquals(ActionResult.Status.completed, result.getOverallStatus());

        ActionHandling.checkTargetsPowerStatus(client, "ACTIVE", tapestryDefinition.getId(), threadId, vmId);
    }

    @Test
    public void testExecuteRebootOnInstanceAggregation() throws InterruptedException {
        LOG.info("Test reboot aggregation start");
        ThreadDefinition thread = TapestryHandling.findThreadDefinitionWithItemType(tapestryDefinition.getThreads(),
                OsInstanceType.TYPE_LOCAL_ID);
        String threadId = thread.getId();
        // get the result
        QueryResult qr1 = BasicQueryOperations.getThreadWithWait(client, tapestryDefinition.getId(), threadId,
                equalsNumInstancesPrivateProvider);
        assertEquals("Incorrect number of instances", testDataConfig.getExpectedInstanceNbr(FakeConfig.PRIVATE_INDEX),
                qr1.getElements().size());
        assertTrue("fibre should not be a da", !qr1.getElements().get(0).getEntity().isAggregation());

        // stop the whole aggregation but check only the first
        OsInstance vm = (OsInstance) (qr1.getElements().get(0).getEntity());
        String vmId = vm.getCore().getItemId();

        // create a reboot action on all VMs
        Action rebootAction = ActionHandling.createPowerAction("softReboot");
        rebootAction.setTargets(Arrays.asList(qr1.getLogicalId()));
        ActionResult result = client.executeAction(rebootAction);

        assertEquals(ActionResult.Status.completed, result.getOverallStatus());
        // assertEquals(ActionResult.Status.pending, result.getOverallStatus());
        // Thread.sleep(1000);
        // result = client.actionResultStatus(result.getId().toString());
        // assertEquals(ActionResult.Status.completed, result.getOverallStatus());

        ActionHandling.checkTargetsPowerStatus(client, "SHUTOFF", tapestryDefinition.getId(), threadId, vmId);
        LOG.info("\n\n ******************** VM transitioned to OFF - now start checking for ON transition");
        ActionHandling.checkTargetsPowerStatus(client, "ACTIVE", tapestryDefinition.getId(), threadId, vmId);
        LOG.info("VM transitioned to ON - that's reboot for you");
    }

    @Test
    public void testExecuteRebootOnInstanceChangedAggregation() throws InterruptedException {
        LOG.info("Test reboot aggregation start");
        ThreadDefinition thread = TapestryHandling.findThreadDefinitionWithItemType(tapestryDefinition.getThreads(),
                OsInstanceType.TYPE_LOCAL_ID);
        String threadId = thread.getId();
        // get the result
        QueryResult qr1 = BasicQueryOperations.getThreadWithWait(client, tapestryDefinition.getId(), threadId,
                equalsNumInstancesPrivateProvider);
        assertEquals("Incorrect number of instances", testDataConfig.getExpectedInstanceNbr(FakeConfig.PRIVATE_INDEX),
                qr1.getElements().size());
        assertTrue("fibre should not be a da", !qr1.getElements().get(0).getEntity().isAggregation());

        // stop the whole aggregation but check only the first
        OsInstance vm = (OsInstance) (qr1.getElements().get(0).getEntity());
        vm.getCore().getItemId();

        // create a reboot action on all VMs
        Action rebootAction = ActionHandling.createPowerAction("softReboot");
        rebootAction.setActionIssueTimestamp(System.currentTimeMillis() - 100000);
        rebootAction.setTargets(Arrays.asList(qr1.getLogicalId()));
        ActionResult result = client.executeAction(rebootAction);
        assertEquals(ActionResult.Status.aborted, result.getOverallStatus());
    }

    @Test
    public void testExecuteStopStartOnOneInstance() throws InterruptedException {
        LOG.info("Test execute action start");
        ThreadDefinition thread = TapestryHandling.findThreadDefinitionWithItemType(tapestryDefinition.getThreads(),
                OsInstanceType.TYPE_LOCAL_ID);
        String threadId = thread.getId();
        // get the result
        QueryResult qr1 = BasicQueryOperations.getThreadWithWait(client, tapestryDefinition.getId(), threadId,
                equalsNumInstancesPrivateProvider);
        assertEquals("Incorrect number of instances", testDataConfig.getExpectedInstanceNbr(FakeConfig.PRIVATE_INDEX),
                qr1.getElements().size());
        assertTrue("fibre should not be a da", !qr1.getElements().get(0).getEntity().isAggregation());

        // pick an instance and stop it
        OsInstance vm = (OsInstance) (qr1.getElements().get(0).getEntity());
        String vmId = vm.getCore().getItemId();

        // create a stop action
        Action stopAction = ActionHandling.createPowerAction("stop");
        List<String> target = Arrays.asList(vm.getLogicalId());
        stopAction.setTargets(target);
        ActionResult result = client.executeAction(stopAction);
        assertEquals(ActionResult.Status.completed, result.getOverallStatus());
        // assertEquals(ActionResult.Status.pending, result.getOverallStatus());
        //
        // Thread.sleep(1000);
        //
        // result = client.actionResultStatus(result.getId().toString());
        //
        // assertEquals(ActionResult.Status.completed, result.getOverallStatus());

        ActionHandling.checkTargetsPowerStatus(client, "SHUTOFF", tapestryDefinition.getId(), threadId, vmId);
        //
        Action startAction = ActionHandling.createPowerAction("start");
        startAction.setTargets(target);
        result = client.executeAction(startAction);

        assertEquals(ActionResult.Status.completed, result.getOverallStatus());
        // assertEquals(ActionResult.Status.pending, result.getOverallStatus());
        // Thread.sleep(1000);
        // result = client.actionResultStatus(result.getId().toString());
        // assertEquals(ActionResult.Status.completed, result.getOverallStatus());

        ActionHandling.checkTargetsPowerStatus(client, "ACTIVE", tapestryDefinition.getId(), threadId, vmId);
    }

    @Test
    public void testExecuteRebootOnOneInstance() throws InterruptedException {
        LOG.info("Test execute action start");
        ThreadDefinition thread = TapestryHandling.findThreadDefinitionWithItemType(tapestryDefinition.getThreads(),
                OsInstanceType.TYPE_LOCAL_ID);
        String threadId = thread.getId();
        // get the result
        QueryResult qr1 = BasicQueryOperations.getThreadWithWait(client, tapestryDefinition.getId(), threadId,
                equalsNumInstancesPrivateProvider);
        assertEquals("Incorrect number of instances", testDataConfig.getExpectedInstanceNbr(FakeConfig.PRIVATE_INDEX),
                qr1.getElements().size());
        assertTrue("fibre should not be a da", !qr1.getElements().get(0).getEntity().isAggregation());

        // pick an instance and stop it
        OsInstance vm = (OsInstance) (qr1.getElements().get(0).getEntity());
        String vmId = vm.getCore().getItemId();

        // create a reboot action
        Action stopAction = ActionHandling.createPowerAction("softReboot");
        List<String> target = Arrays.asList(vm.getLogicalId());
        stopAction.setTargets(target);
        ActionResult result = client.executeAction(stopAction);

        assertEquals(ActionResult.Status.completed, result.getOverallStatus());
        // assertEquals(ActionResult.Status.pending, result.getOverallStatus());
        // Thread.sleep(1000);
        // result = client.actionResultStatus(result.getId().toString());
        // assertEquals(ActionResult.Status.completed, result.getOverallStatus());

        ActionHandling.checkTargetsPowerStatus(client, "SHUTOFF", tapestryDefinition.getId(), threadId, vmId);
        ActionHandling.checkTargetsPowerStatus(client, "ACTIVE", tapestryDefinition.getId(), threadId, vmId);
    }
}
