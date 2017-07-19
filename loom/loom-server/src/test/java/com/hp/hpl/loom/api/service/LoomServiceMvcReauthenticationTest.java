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
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

import com.hp.hpl.loom.api.client.LoomClientException;
import com.hp.hpl.loom.api.service.utils.BasicQueryOperations;
import com.hp.hpl.loom.api.service.utils.QueryFormatting;
import com.hp.hpl.loom.api.service.utils.TapestryHandling;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.tapestry.PatternDefinition;
import com.hp.hpl.loom.tapestry.PatternDefinitionList;
import com.hp.hpl.loom.tapestry.QueryDefinition;
import com.hp.hpl.loom.tapestry.TapestryDefinition;
import com.hp.hpl.loom.tapestry.ThreadDefinition;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext-testReauthentication.xml")
@WebAppConfiguration
public class LoomServiceMvcReauthenticationTest extends IntegrationTestBase {

    private static final Log LOG = LogFactory.getLog(LoomServiceMvcReauthenticationTest.class);

    @Autowired
    PropertyPlaceholderConfigurer propertyConfigurer;

    @Autowired
    private WebApplicationContext ctx;

    @Before
    public void setUp() throws Exception {
        LOG.info("setUp start");
        super.setUp(1);
        LOG.info("setUp end");
    }

    @Override
    void setupTestAndClient(final int noOfClients) {
        LOG.info("setupTestAndClient start");
        setupMvcClient(ctx, noOfClients);
        LOG.info("setupTestAndClient end");
    }

    @Test
    public void testReauthentication() throws InterruptedException {
        LOG.info("testReauthentication start");
        Credentials credentials = new Credentials("test", "test");
        PatternDefinitionList patternDefinitionList = client.loginProvider("os", "private", credentials);
        LOG.debug("SESSION_ID: " + client.getSessionId());
        TapestryDefinition tapDef = getTapestryDefinition(patternDefinitionList);
        LOG.debug("OK - queried server - sleep for 10 secs - session should need reauthentication");
        Thread.sleep(10000);
        LOG.debug("finished sleeping: session should need reauthentication: " + client.getSessionId());
        try {
            BasicQueryOperations.queryAllThreads(client, tapDef);
            fail("Expecting a client exception");
        } catch (LoomClientException ex) {
            LOG.debug("OK - got 423");
            assertEquals(423, ex.getStatusCode());
            LOG.debug("REAUTHENTICATING " + client.getSessionId());
            client.loginProvider("os", "private", credentials);
            LOG.debug("SESSION_ID: " + client.getSessionId());
            LOG.debug("query after authentication ");
            BasicQueryOperations.queryAllThreads(client, tapDef);
            LOG.debug("OK - basic query worked");
        }
        LOG.info("testReauthentication end");
    }

    private TapestryDefinition getTapestryDefinition(final PatternDefinitionList patternDefinitionList) {
        PatternDefinition patternDefinition = patternDefinitionList.getPatterns().get(0);
        ThreadDefinition thread = patternDefinition.getThreads().get(0);
        QueryDefinition query = QueryFormatting.createSimpleQuery(thread.getQuery().getInputs());
        ThreadDefinition myThread = new ThreadDefinition(thread.getItemType(), thread.getItemType(), query);
        List<ThreadDefinition> myThreads = new ArrayList<>();
        myThreads.add(myThread);
        return TapestryHandling.createTapestryFromThreadDefinitions(client, myThreads);
    }
}
