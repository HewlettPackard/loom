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
import com.hp.hpl.loom.manager.adapter.AdapterLoader;
import com.hp.hpl.loom.manager.adapter.AdapterManager;
import com.hp.hpl.loom.manager.aggregation.AggregationManager;
import com.hp.hpl.loom.manager.stitcher.Tacker;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.model.SessionImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext-testSwiftRealAdapter.xml")
public class SwiftRealAdapterTestLocalOnly {

    private static final Log LOG = LogFactory.getLog(SwiftRealAdapterTestLocalOnly.class);

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

    public SwiftRealAdapterTestLocalOnly() {
        // need to define credentials here for test to run
        // creds = new Credentials("loom", "Lubwaqit");
        creds = new Credentials("eric.deliot@hp.com", "Totototo67");
    }

    @Before
    public void setUp() throws Exception {
        LOG.info("Setup test");
        realAdapter = (RealAdapter) adapterLoader.getAdapter("helionSwiftReal.properties");
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
    public void testJustSleep() throws InterruptedException {
        StopWatch watch = new StopWatch();
        LOG.info("testing JustSleep");
        watch.start();
        LOG.info("test just sleeping...");
        sleep(3 * 60000);
        watch.stop();
        LOG.info("tested JustSleep --> " + watch);
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
