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
package com.hp.hpl.loom.manager.query;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.hp.hpl.loom.adapter.os.BaseOsAdapter;
import com.hp.hpl.loom.adapter.os.OsInstanceType;
import com.hp.hpl.loom.api.client.LoomClient;
import com.hp.hpl.loom.api.client.LoomMvcClient;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.QueryResult;
import com.hp.hpl.loom.tapestry.Operation;
import com.hp.hpl.loom.tapestry.PatternDefinition;
import com.hp.hpl.loom.tapestry.PatternDefinitionList;
import com.hp.hpl.loom.tapestry.QueryDefinition;
import com.hp.hpl.loom.tapestry.TapestryDefinition;
import com.hp.hpl.loom.tapestry.ThreadDefinition;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext-test-badQuery.xml")
@WebAppConfiguration
public class BadQueryParamErrorTest {

    private static final Log LOG = LogFactory.getLog(BadQueryParamErrorTest.class);

    @Autowired
    private WebApplicationContext ctx;

    private boolean isMvc = true;

    protected LoomClient client;

    private static final long WAIT_TIME = 5 * 1000;

    private static boolean waited = false;

    @Value("${test.username}")
    private String username;
    @Value("${test.password}")
    private String password;


    void setupTestAndClient() {
        LOG.info("setupTestAndClient start");
        setupMvcClient(ctx);
        LOG.info("setupTestAndClient end");
    }

    protected void setupMvcClient(final WebApplicationContext ctx) {
        LOG.info("setupMvcClient start");
        isMvc = true;
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
        client = new LoomMvcClient(mockMvc);
        waitForStartup();
        LOG.info("setupMvcClient end");
    }

    @Before
    public void setUp() throws Exception {
        setupTestAndClient();
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



    @Test(expected = AssertionError.class)
    public void testInvalidParameterException() throws InvalidParameterException {
        LOG.info("testInvalidParameterException start");
        StopWatch watch = new StopWatch();
        watch.start();
        loginProvider();
        PatternDefinitionList patternDefinitionList = client.getPatterns();
        Collection<PatternDefinition> patternDefinitions = patternDefinitionList.getPatterns();

        LOG.info(patternDefinitions);

        for (PatternDefinition pd : patternDefinitions) {
            if (pd.getId().equals("os-" + BaseOsAdapter.ALL_FIVE_PATTERN)) {
                LOG.info("found the right pattern - now get the aggregation");
                List<ThreadDefinition> threads = pd.getThreads();
                // only one thread to start with
                assertTrue("should have only five threads", threads.size() == 5);
                TapestryDefinition tapestryDefinition = new TapestryDefinition();
                tapestryDefinition.setThreads(new ArrayList<ThreadDefinition>(threads));
                String tapestryId = client.createTapestryDefinition(tapestryDefinition).getId();
                // String tapestryId =
                // verifyAndGetJsonId(client.createTapestryDefinition(tapestryDefinition));
                // must set it otherwise tapestryDefiniiton has a null Id
                tapestryDefinition.setId(tapestryId);

                ThreadDefinition thread = threads.iterator().next();
                String threadId = thread.getId();

                LOG.info(thread.getItemType());

                // get the result
                QueryResult qr1 = client.getAggregation(tapestryId, threadId);

                assertTrue("there should be 18 of them", qr1.getElements().size() == 18);
                assertTrue("fibre should not be a da", !qr1.getElements().get(0).getEntity().isAggregation());
                LOG.info("grouping and braiding - create new Thread");

                String qr1LogicalId = qr1.getLogicalId();
                LOG.info("selected logicalId: " + qr1LogicalId);
                ArrayList<String> ins = new ArrayList<String>(1);
                ins.add(qr1LogicalId);

                Map<String, Object> groupParams = new HashMap(1);
                groupParams.put("property", OsInstanceType.ATTR_FLAVOR);
                Operation groupOperation = new Operation((DefaultOperations.GROUP_BY.toString()), groupParams);

                Map<String, Object> braidParams = new HashMap(1);
                braidParams.put("max Fibres", 2);
                Operation braidOperation = new Operation((DefaultOperations.BRAID.toString()), braidParams);

                List<Operation> groupBraidPipe = new ArrayList<Operation>(2);
                groupBraidPipe.add(groupOperation);
                groupBraidPipe.add(braidOperation);
                QueryDefinition groupBraidQuery = new QueryDefinition(groupBraidPipe, ins);

                ThreadDefinition threadDefinition = new ThreadDefinition("reg8", thread.getItemType(), groupBraidQuery);
                // add the newly created thread to the tapestry and update
                tapestryDefinition.addThreadDefinition(threadDefinition);
                client.updateTapestryDefinition(tapestryId, tapestryDefinition);

                client.getAggregation(tapestryId, threadDefinition.getId());
            }
        }
        watch.stop();
        LOG.info("testInvalidParameterException end --> " + watch);
    }


    private PatternDefinitionList loginProvider() {
        Credentials credentials = new Credentials(username, password);
        return client.loginProvider("os", "private", credentials);
    }

    private void logoutProvider() {
        client.logoutProvider("os", "private");
    }

    public String verifyAndGetJsonId(String jsonId) {
        assertTrue(jsonId.contains("{"));
        assertTrue(jsonId.contains("}"));
        assertTrue(jsonId.contains("\"id\":"));
        assertNotNull("id was null", jsonId);
        jsonId = jsonId.replace("{", "");
        jsonId = jsonId.replace("}", "");
        jsonId = jsonId.replace("\"", "");
        String tokens[] = jsonId.split("\\s+");
        return tokens[tokens.length - 1];
    }

}
