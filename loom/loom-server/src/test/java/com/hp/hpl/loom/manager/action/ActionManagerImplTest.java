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
package com.hp.hpl.loom.manager.action;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hp.hpl.loom.adapter.os.OsInstanceType;
import com.hp.hpl.loom.adapter.os.fake.FakeAdapter;
import com.hp.hpl.loom.api.util.SessionManager;
import com.hp.hpl.loom.exceptions.InvalidActionSpecificationException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.SessionAlreadyExistsException;
import com.hp.hpl.loom.exceptions.UserAlreadyConnectedException;
import com.hp.hpl.loom.manager.adapter.AdapterLoader;
import com.hp.hpl.loom.manager.adapter.AdapterManager;
import com.hp.hpl.loom.manager.aggregation.AggregationManager;
import com.hp.hpl.loom.manager.stitcher.Tacker;
import com.hp.hpl.loom.manager.tapestry.TapestryManager;
import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionParameter;
import com.hp.hpl.loom.model.ActionParameters;
import com.hp.hpl.loom.model.ActionResult;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.model.SessionImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext-testActionManager.xml")
public class ActionManagerImplTest {

    private static final Log LOG = LogFactory.getLog(ActionManagerImplTest.class);

    @Autowired
    ActionManager actionManager;

    @Autowired
    AggregationManager aggregationManager;

    @Autowired
    private Tacker stitcher;

    @Autowired
    TapestryManager tapestryManager;

    @Autowired
    AdapterManager adapterManager;

    @Autowired
    private AdapterLoader adapterLoader;

    private FakeAdapter fakeAdapter;

    @Autowired
    private SessionManager sessionManager;

    Session session;

    @Before
    public void setUp() throws Exception {
        LOG.info("Setup action test");
        fakeAdapter = (FakeAdapter) adapterLoader.getAdapter("fakeAdapterPrivate.properties");
        waitToSync();
    }

    @After
    public void shutDown() throws Exception {
        LOG.info("ShutDown action test");
    }

    public void waitToSync() throws InterruptedException, NoSuchSessionException, SessionAlreadyExistsException,
            NoSuchProviderException, UserAlreadyConnectedException {
        session = new SessionImpl("sessionOne", sessionManager.getInterval());

        aggregationManager.createSession(session);
        stitcher.createSession(session);
        // this triggers the data collection
        adapterManager.userConnected(session, fakeAdapter.getProvider(), new Credentials("as", "as"));
        Thread.sleep(1000);
    }

    @Test
    public void testDoAction() throws Exception {
        ItemType itemType = new OsInstanceType(fakeAdapter.getProvider());

        LOG.info("Action on " + itemType.getId());
        itemType.getActions();
        Action action = createPowerAction("stop");
        action.setTargets(Arrays.asList("os/private/instances"));
        ActionResult response = actionManager.doAction(session, action);

        UUID actionId = response.getId();
        ActionResult result = actionManager.getActionResult(session, actionId);
        assertEquals("Status should be 'completed' ", ActionResult.Status.completed, result.getOverallStatus());
        // assertEquals("Status should be 'pending' ", ActionResult.Status.pending,
        // result.getOverallStatus());
        // Thread.sleep(1000);
        // assertEquals("Status should be 'completed' ", ActionResult.Status.completed,
        // result.getOverallStatus());
    }

    private Action createPowerAction(final String value) {
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
}
