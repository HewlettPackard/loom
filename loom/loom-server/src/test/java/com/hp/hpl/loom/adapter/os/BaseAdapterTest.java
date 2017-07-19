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

import java.util.UUID;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hp.hpl.loom.adapter.os.fake.FakeProviderImpl;
import com.hp.hpl.loom.api.util.SessionManager;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.manager.adapter.AdapterManager;
import com.hp.hpl.loom.manager.aggregation.AggregationManager;
import com.hp.hpl.loom.manager.stitcher.Tacker;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.model.SessionImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext-testBaseAdapter.xml")
public class BaseAdapterTest {

    private static final Log LOG = LogFactory.getLog(BaseAdapterTest.class);

    @Autowired
    private SessionManager sessionManager;
    @Autowired
    private AggregationManager aggregationManager;
    @Autowired
    private Tacker stitcher;
    @Autowired
    private AdapterManager adapterManager;

    TestBaseAdapterImpl adapter;
    @Autowired
    ApplicationContext appContext;

    Provider prov;
    Session session;

    @Before
    public void setUp() throws Exception {
        LOG.info("Setup test");
        adapter = new TestBaseAdapterImpl();
        adapter.setAdapterManager(adapterManager, loadProperties());

        adapter.onLoad();
        adapterManager.registerAdapter(adapter);
        prov = adapter.getProvider();
        session = new SessionImpl("session-" + UUID.randomUUID().toString(), sessionManager.getInterval());
        aggregationManager.createSession(session);
        stitcher.createSession(session);
        // this triggers the data collection
        adapterManager.userConnected(session, prov, null);
    }

    @After
    public void shutDown() throws Exception {
        LOG.info("shutDown test");
        adapterManager.userDisconnected(session, prov, null);
        aggregationManager.deleteSession(session);
        stitcher.deleteSession(session);
    }

    private PropertiesConfiguration loadProperties() throws ConfigurationException {
        String deploymentProperties = "src/test/resources/loomAdapterTest.properties";
        PropertiesConfiguration props = new PropertiesConfiguration(deploymentProperties);
        return props;
    }

    private int[] parseIntArray(final String arrayOfInts) {
        String[] intStringArray = arrayOfInts.split(",");
        int[] intArray = new int[intStringArray.length];
        for (int count = 0; count < intStringArray.length; count++) {
            intArray[count] = Integer.parseInt(intStringArray[count]);
        }
        return intArray;
    }

    @Test
    public void testGetItemTypeLocalIdFromLogicalId() throws NoSuchProviderException {
        StopWatch watch = new StopWatch();
        LOG.info("testing GetItemTypeLocalIdFromLogicalId");
        watch.start();
        Provider prov = new FakeProviderImpl("os", "fake", "http://whatever", "Fake", "test", "test");
        assertNotNull("adapter not created", adapter);
        LOG.info("prov: " + prov);
        LOG.info("faProv: " + adapter.getProvider());
        assertEquals(true, prov.equals(adapter.getProvider()));
        String logicalIdPrefix = (prov.getProviderType() + "/" + prov.getProviderId() + "/");
        // test corner case
        // aggregation logicalIds
        String logicalId = logicalIdPrefix + "map1s";
        assertEquals(true, adapter.getItemTypeLocalIdFromLogicalId(logicalId).equals("map1"));
        logicalId = logicalIdPrefix + "map1/As";
        assertEquals(true, adapter.getItemTypeLocalIdFromLogicalId(logicalId).equals("map1/A"));
        logicalId = logicalIdPrefix + "map1/Ass";
        assertEquals(true, adapter.getItemTypeLocalIdFromLogicalId(logicalId).equals("map1/As"));
        // item logicalIds
        logicalId = logicalIdPrefix + "map1s/123";
        assertEquals(true, adapter.getItemTypeLocalIdFromLogicalId(logicalId).equals("map1"));
        logicalId = logicalIdPrefix + "map1/As/123";
        assertEquals(true, adapter.getItemTypeLocalIdFromLogicalId(logicalId).equals("map1/A"));
        logicalId = logicalIdPrefix + "map1s/As/123s/456";
        assertEquals(true, adapter.getItemTypeLocalIdFromLogicalId(logicalId).equals("map1s/A"));
        logicalId = logicalIdPrefix + "map1/Ass/123";
        assertEquals(true, adapter.getItemTypeLocalIdFromLogicalId(logicalId).equals("map1/As"));
        // unregistered ones
        logicalId = logicalIdPrefix + "map2s/As/123s/456";
        assertEquals(true, adapter.getItemTypeLocalIdFromLogicalId(logicalId) == null);
        logicalId = logicalIdPrefix + "map2s";
        assertEquals(true, adapter.getItemTypeLocalIdFromLogicalId(logicalId) == null);
        // wrong format
        logicalId = logicalIdPrefix + "map1";
        assertEquals(true, adapter.getItemTypeLocalIdFromLogicalId(logicalId) == null);
        logicalId = logicalIdPrefix + "map1/123";
        assertEquals(true, adapter.getItemTypeLocalIdFromLogicalId(logicalId) == null);

        watch.stop();
        LOG.info("tested GetItemTypeLocalIdFromLogicalId --> " + watch);
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
