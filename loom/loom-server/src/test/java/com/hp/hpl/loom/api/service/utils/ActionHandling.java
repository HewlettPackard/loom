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
package com.hp.hpl.loom.api.service.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import com.hp.hpl.loom.adapter.os.OsInstance;
import com.hp.hpl.loom.api.client.LoomClient;
import com.hp.hpl.loom.api.service.IntegrationTestBase;
import com.hp.hpl.loom.exceptions.InvalidActionSpecificationException;
import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionParameter;
import com.hp.hpl.loom.model.ActionParameters;
import com.hp.hpl.loom.model.ActionResult;
import com.hp.hpl.loom.model.QueryResult;

public interface ActionHandling {
    static final int iMax = 40;

    static Action createPowerAction(final String value) {
        ActionParameters powerParameters = new ActionParameters();
        try {
            ActionParameter ap = new ActionParameter("power", ActionParameter.Type.ENUMERATED, "power options", null);
            ap.setValue(value);
            powerParameters.add(ap);
            return new Action("power", "a", "b", "icon-cycle", powerParameters);
        } catch (InvalidActionSpecificationException e) {
            return null;
        }
    }

    static OsInstance checkTargetsPowerStatus(final LoomClient client, final String targetStatus,
            final String tapestryId, final String threadId, final String vmId) throws InterruptedException {
        for (int i = 0; i < iMax; ++i) {
            QueryResult qr1 = BasicQueryOperations.clientGetAggregation(client, tapestryId, threadId);
            // find the vm that was stopped and check status
            for (int j = 0; j < qr1.getElements().size(); ++j) {
                OsInstance obsVm = (OsInstance) (qr1.getElements().get(j).getEntity());
                if (obsVm.getCore().getItemId().equals(vmId)) {
                    if (obsVm.getCore().getStatus().equals(targetStatus)) {
                        return obsVm;
                    } else {
                        break;
                    }
                }
            }
            // should sleep same amount of time that the fakeAdapter does to poll the fake system
            // how to get to that value since it is set as a property of the fakeAdapter bean?
            if (i < iMax) {
                Thread.sleep(2000);
            }
        }
        assertTrue("Should not take " + iMax * 2 + " secs to detect status change into " + targetStatus, false);
        return null;
    }

    static void pickAnInstanceAndReboot(final LoomClient client, final String tapestryId, final String threadId)
            throws InterruptedException {
        // pick an instance and reboot it
        QueryResult qrInstances =
                BasicQueryOperations.getThreadWithWait(client, tapestryId, threadId, IntegrationTestBase.greaterThan0);
        OsInstance vm = (OsInstance) (qrInstances.getElements().get(0).getEntity());
        String vmId = vm.getCore().getItemId();
        Action stopAction = createPowerAction("softReboot");
        List<String> target = Arrays.asList(vm.getLogicalId());
        stopAction.setTargets(target);
        ActionResult result = client.executeAction(stopAction);

        assertEquals(ActionResult.Status.completed, result.getOverallStatus());
        // assertEquals(ActionResult.Status.pending, result.getOverallStatus());
        // Thread.sleep(1000);
        // result = client.actionResultStatus(result.getId().toString());
        // assertEquals(ActionResult.Status.completed, result.getOverallStatus());

        checkTargetsPowerStatus(client, "SHUTOFF", tapestryId, threadId, vmId);
        checkTargetsPowerStatus(client, "ACTIVE", tapestryId, threadId, vmId);
    }

    static OsInstance powerDownInstance(final LoomClient client, OsInstance vm, final String tapestryId,
            final String threadId) throws InterruptedException {
        String vmId = vm.getCore().getItemId();
        executePowerAction(client, vm, "stop");
        vm = checkTargetsPowerStatus(client, "SHUTOFF", tapestryId, threadId, vmId);
        return vm;
    }

    static OsInstance powerOnInstance(final LoomClient client, OsInstance vm, final String tapestryId,
            final String threadId) throws InterruptedException {
        String vmId = vm.getCore().getItemId();
        executePowerAction(client, vm, "start");
        vm = checkTargetsPowerStatus(client, "ACTIVE", tapestryId, threadId, vmId);
        return vm;
    }

    static void executePowerAction(final LoomClient client, final OsInstance vm, final String actionType) {
        Action action = createPowerAction(actionType);
        List<String> target = Arrays.asList(vm.getLogicalId());
        action.setTargets(target);
        client.executeAction(action);
    }
}
