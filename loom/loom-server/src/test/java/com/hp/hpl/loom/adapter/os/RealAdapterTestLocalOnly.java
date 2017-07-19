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
package com.hp.hpl.loom.adapter.os;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hp.hpl.loom.adapter.os.real.RealAdapter;
import com.hp.hpl.loom.api.util.SessionManager;
import com.hp.hpl.loom.exceptions.DuplicateAdapterException;
import com.hp.hpl.loom.exceptions.InvalidActionSpecificationException;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.SessionAlreadyExistsException;
import com.hp.hpl.loom.exceptions.UserAlreadyConnectedException;
import com.hp.hpl.loom.manager.adapter.AdapterLoader;
import com.hp.hpl.loom.manager.adapter.AdapterManager;
import com.hp.hpl.loom.manager.aggregation.AggregationManager;
import com.hp.hpl.loom.manager.stitcher.Tacker;
import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionParameter;
import com.hp.hpl.loom.model.ActionParameters;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.ProviderImpl;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.model.SessionImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext-testRealAdapter.xml")
public class RealAdapterTestLocalOnly {

    private static final Log LOG = LogFactory.getLog(RealAdapterTestLocalOnly.class);

    @Autowired
    private SessionManager sessionManager;
    @Autowired
    private AggregationManager aggregationManager;
    @Autowired
    private Tacker stitcher;
    @Autowired
    private AdapterManager adapterManager;
    @Autowired
    private AdapterLoader adapterLoader;

    RealAdapter realAdapter;

    Credentials creds;
    Provider prov;
    Session session;

    public RealAdapterTestLocalOnly() {
        // need to define credentials here for test to run
        creds = new Credentials("loom", "Lubwaqit");
    }

    @Before
    public void setUp() throws Exception {
        LOG.info("Setup test");
        realAdapter = (RealAdapter) adapterLoader.getAdapter("helionReal.properties");
        prov = realAdapter.getProvider();
        session = new SessionImpl("sessionOne", sessionManager.getInterval());
        aggregationManager.createSession(session);
        stitcher.createSession(session);
        // this triggers the data collection
        adapterManager.userConnected(session, prov, creds);
    }

    @After
    public void shutDown() throws Exception {
        LOG.info("shutDown test");
        adapterManager.userDisconnected(session, prov, creds);
        aggregationManager.deleteSession(session);
        stitcher.deleteSession(session);
    }

    @Test
    public void testAdapterCreation() {
        StopWatch watch = new StopWatch();
        LOG.info("testing AdapterCreation");
        watch.start();
        Provider prov = new ProviderImpl("os", "hplbress", "http://16.25.166.21:5000/v2.0", "Hplbress", "com");
        assertNotNull("adapter not created", realAdapter);
        assertEquals(true, prov.equals(realAdapter.getProvider()));
        watch.stop();
        LOG.info("tested AdapterCreation --> " + watch);
    }

    @Test
    public void testJustSleep() throws InterruptedException {
        StopWatch watch = new StopWatch();
        LOG.info("testing JustSleep");
        watch.start();
        LOG.info("test just sleeping...");
        sleep(3 * 60000);
        watch.stop();
        LOG.info("tested JustSleep --> " + watch);
    }

    @Test
    public void testDataGeneration()
            throws NoSuchSessionException, InterruptedException, NoSuchProviderException, SessionAlreadyExistsException,
            DuplicateAdapterException, UserAlreadyConnectedException, NoSuchItemTypeException {
        StopWatch watch = new StopWatch();
        LOG.info("testing DataGeneration");
        watch.start();
        // get aggregations
        List<String> itemTypeIdList = Arrays.asList(OsInstanceType.TYPE_LOCAL_ID, OsVolumeType.TYPE_LOCAL_ID,
                OsImageType.TYPE_LOCAL_ID, OsNetworkType.TYPE_LOCAL_ID, OsSubnetType.TYPE_LOCAL_ID,
                OsRegionType.TYPE_LOCAL_ID, OsProjectType.TYPE_LOCAL_ID);
        // List<String> itemTypeIdList = Arrays.asList(InstanceType.TYPE_ID, ImageType.TYPE_ID,
        // VolumeType.TYPE_ID,
        // NetworkType.TYPE_ID, SubnetType.TYPE_ID);
        // compare adapter data with what the aggregationManager has
        for (String itemTypeId : itemTypeIdList) {
            int i = 0;
            int iMax = 6000;
            for (i = 0; i < iMax; ++i) {
                // check if aggregationManager and adapterManager view match
                LOG.info("data check: iteration " + i);
                // LOG.warn("------------> DATA FROM ADAPTER:
                // \n"+((OsItemCollector)fakeAdapter.getItemCollector(session)).getNewItems(itemTypeId));
                // LOG.warn("zzz trying to get aggregation for logicalId:
                // "+fakeAdapter.getAggregationLogicalId(itemTypeId));
                Aggregation agg =
                        aggregationManager.getAggregation(session, realAdapter.getAggregationLogicalId(itemTypeId));
                if (agg != null) {
                    List<Fibre> elems = agg.getElements();
                    if ((elems != null) && (!elems.isEmpty())) {
                        checkEquality(elems,
                                ((OsItemCollector) realAdapter.getItemCollector(session)).getNewItems(itemTypeId));
                        break;
                    }
                }
                if (i < iMax) {
                    sleep(1000);
                }
            }
            assertTrue("should not take 10 secs for aggregationManager to get groundedAggregation", i < iMax);
        }
        sleep(60 * 60000);
        watch.stop();
        LOG.info("tested DataGeneration --> " + watch);
    }

    private void checkEquality(final List<Fibre> elems, final Collection<Item> items) {
        LOG.info("Testing equality of aggregation and adapter content");
        assertEquals(true, elems.size() == items.size());
        for (Item item : items) {
            assertTrue("item not found", elems.contains(item));
        }
        /*
         * // hack to debug relationship generation if (elems.get(0) instanceof Instance) {
         * LOG.warn(
         * "RELATIONSHIP-DEBUG: from instance, Image is "+((Instance)elems.get(0)).getImage(
         * ).getLogicalId()); for (int i=0; i<elems.size(); ++i) { try{
         * LOG.warn("Instance: "+toJson((Instance)elems.get(i))); }catch(JsonProcessingException
         * e){} } } if (elems.get(0) instanceof Image){
         * LOG.warn("RELATIONSHIP-DEBUG: from Image, instance is"
         * +((Image)elems.get(0)).getThreadWithWait().iterator().next().getLogicalId());
         * LOG.warn("Count: "+((Image)elems.get(0)).getThreadWithWait().size()); try{
         * LOG.warn("Image: "+toJson((Image)elems.get(0))); }catch(JsonProcessingException e){} for
         * (LoomEntity elem : elems) { LOG.warn("COUNT: "+((Image)elem).getUsageCount()); } } if
         * (elems.get(0) instanceof Region) { for (int i=0; i<elems.size(); ++i) { try{
         * LOG.warn("Region: "+toJson((Region)elems.get(i))); }catch(JsonProcessingException e){} }
         * } if (elems.get(0) instanceof Project) { for (int i=0; i<elems.size(); ++i) { try{
         * LOG.warn("Project: "+toJson((Project)elems.get(i))); }catch(JsonProcessingException e){}
         * } }
         */
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

    @Test
    public void testAction()
            throws NoSuchSessionException, InterruptedException, NoSuchProviderException, SessionAlreadyExistsException,
            DuplicateAdapterException, UserAlreadyConnectedException, NoSuchItemTypeException {
        StopWatch watch = new StopWatch();
        LOG.info("testing Action");
        watch.start();
        int iMax = 30;
        int i = 0;
        List<Fibre> elems = null;
        Aggregation agg = null;
        for (i = 0; i < iMax; ++i) {
            // check if aggregationManager and adapterManager view match
            LOG.info("action check: iteration " + i);
            agg = aggregationManager.getAggregation(session,
                    realAdapter.getAggregationLogicalId(OsInstanceType.TYPE_LOCAL_ID));
            if (agg != null) {
                elems = agg.getElements();
                if ((elems != null) && (!elems.isEmpty())) {
                    // ok we've got data
                    break;
                }
            }
            if (i < iMax) {
                sleep(500);
            }
        }
        assertTrue("should not take " + iMax / 2 + " secs for aggregationManager to get groundedAggregation", i < iMax);
        // wait 5 secs then grab one instance and stop it
        assertTrue("elems should not be null", (elems != null));
        assertTrue("elems should include OsInstances", (elems.get(0) instanceof OsInstance));
        // grab the instance that is called
        String vmName = "test2";
        OsInstance selectVm = null;
        for (int j = 0; j < elems.size(); ++j) {
            selectVm = (OsInstance) elems.get(j);
            if (selectVm.getName().equals(vmName)) {
                break;
            }
        }
        assertTrue("selectVm should have been found: " + vmName, (selectVm != null));
        assertTrue("selectVm should be named test2", selectVm.getName().equals(vmName));
        try {
            LOG.info("BEFORE ACTION: vm json: \n" + toJson(selectVm));
        } catch (JsonProcessingException e) {
        }
        assertTrue("selectVM should be ACTIVE", selectVm.getCore().getStatus().equals("ACTIVE"));
        LOG.info("\n\n ********** NOW STOP test2 ************** \n\n");
        ArrayList<Item> vmList = new ArrayList<>();
        vmList.add(selectVm);
        Action stopAction = createPowerAction("stop");
        realAdapter.doAction(session, stopAction, vmList);
        iMax = 12;
        long waitTime = 10000;
        for (i = 0; i < iMax; ++i) {
            // check if aggregationManager and adapterManager view match
            LOG.info("action check: iteration " + i);
            elems = agg.getElements();
            for (int j = 0; j < elems.size(); ++j) {
                selectVm = (OsInstance) elems.get(j);
                if (selectVm.getName().equals(vmName)) {
                    break;
                }
            }
            assertTrue("selectVm should have been found: " + vmName, (selectVm != null));
            assertTrue("selectVm should be named test2", selectVm.getName().equals(vmName));
            try {
                LOG.info("vm json: \n" + toJson(selectVm));
            } catch (JsonProcessingException e) {
            }
            if (selectVm.getCore().getStatus().equals("SHUTOFF")
                    || selectVm.getCore().getStatus().equals("UNRECOGNIZED")) {
                break;
            }
            if (i < iMax) {
                sleep(waitTime);
            }
        }
        LOG.info("\n\n ********** NOW START test2 ************** \n\n");
        // now start it again
        vmList = new ArrayList<>();
        vmList.add(selectVm);
        Action startAction = createPowerAction("start");
        realAdapter.doAction(session, startAction, vmList);
        iMax = 12;
        for (i = 0; i < iMax; ++i) {
            // check if aggregationManager and adapterManager view match
            LOG.info("action check: iteration " + i);
            elems = agg.getElements();
            for (int j = 0; j < elems.size(); ++j) {
                selectVm = (OsInstance) elems.get(j);
                if (selectVm.getName().equals(vmName)) {
                    break;
                }
            }
            assertTrue("selectVm should have been found: " + vmName, (selectVm != null));
            assertTrue("selectVm should be named test2", selectVm.getName().equals(vmName));
            try {
                LOG.info("vm json: \n" + toJson(selectVm));
            } catch (JsonProcessingException e) {
            }
            if (selectVm.getCore().getStatus().equals("ACTIVE")) {
                break;
            }
            if (i < iMax) {
                sleep(waitTime);
            }
        }

        // now try reboot
        LOG.info("\n\n ********** NOW REBOOT test2 **************\n\n");
        vmList = new ArrayList<>();
        vmList.add(selectVm);
        Action rebootAction = createPowerAction("softReboot");
        realAdapter.doAction(session, rebootAction, vmList);
        iMax = 60;
        String oldCheckValue = "BAH";
        String checkValue = "REBOOT";
        for (i = 0; i < iMax; ++i) {
            // check if aggregationManager and adapterManager view match
            LOG.info("action check: iteration " + i);
            elems = agg.getElements();
            for (int j = 0; j < elems.size(); ++j) {
                selectVm = (OsInstance) elems.get(j);
                if (selectVm.getName().equals(vmName)) {
                    break;
                }
            }
            assertTrue("selectVm should have been found: " + vmName, (selectVm != null));
            assertTrue("selectVm should be named test2", selectVm.getName().equals(vmName));
            try {
                LOG.info("vm json: \n" + toJson(selectVm));
            } catch (JsonProcessingException e) {
            }
            if (selectVm.getCore().getStatus().equals(checkValue)) {
                if ((oldCheckValue.equals("REBOOT")) && (checkValue.equals("ACTIVE"))) {
                    break;
                }
                oldCheckValue = "REBOOT";
                checkValue = "ACTIVE";
            }
            if (i < iMax) {
                sleep(waitTime);
            }
        }
        watch.stop();
        LOG.info("tested Action --> " + watch);
    }

    // test json serialization
    private String toJson(final Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String jsonRep = "";
        jsonRep = mapper.writeValueAsString(object);
        return jsonRep;
    }

    private void sleep(final long sleepTime) throws InterruptedException {
        LOG.info("test sleeping for: " + sleepTime);
        Thread.sleep(sleepTime);
        LOG.info("test just woke up");
    }

}
