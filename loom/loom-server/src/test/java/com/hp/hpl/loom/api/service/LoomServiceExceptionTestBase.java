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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.hp.hpl.loom.adapter.os.BaseOsAdapter;
import com.hp.hpl.loom.api.client.LoomClientException;
import com.hp.hpl.loom.api.service.utils.TapestryHandling;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.tapestry.PatternDefinition;
import com.hp.hpl.loom.tapestry.PatternDefinitionList;
import com.hp.hpl.loom.tapestry.TapestryDefinition;
import com.hp.hpl.loom.tapestry.ThreadDefinition;

public abstract class LoomServiceExceptionTestBase extends IntegrationTestBase {
    private static final Log LOG = LogFactory.getLog(LoomServiceExceptionTestBase.class);
    private TapestryDefinition tapestryDefinition;
    private List<PatternDefinition> patterns;
    protected Credentials credentials = new Credentials(testDataConfig.getUsername(), testDataConfig.getPassword());
    private Provider provider;

    public void cleanUp() {
        logoutProvider();
        LOG.info("Logged out");
    }

    private PatternDefinitionList loginProvider(final String type, final String id) {
        return client.loginProvider(type, id, credentials);
    }

    private void logoutProvider(final String type, final String id) {
        client.logoutProvider(type, id);
    }


    public void setUp() throws Exception {

    }

    public void connect() throws Exception {
        super.setUp(1);
        PatternDefinitionList patternDefinitionList = loginProvider();
        assertNotNull("Null response in getting PatternDefinitionList", patternDefinitionList);
        // Thread.sleep(1850); // leave adapter room to create the model
        patterns = patternDefinitionList.getPatterns();
        assertNotNull("Null response in getting patterns", patterns);
        // Create a starting tapestry from a specific pattern
        String patternId = "os-" + BaseOsAdapter.ALL_FIVE_PATTERN;
        PatternDefinition patternDefinition = TapestryHandling.getPatternDefinitionMatchingId(client, patternId);
        assertNotNull("Pattern could not be found " + patternId, patternDefinition);
        tapestryDefinition = TapestryHandling.createTapestryFromPattern(client, patternDefinition);
        assertEquals("Incorrect number of threads", 5, tapestryDefinition.getThreads().size());
    }

    @Test(expected = LoomClientException.class)
    public void testLoginNoSession() throws Exception {
        super.setUp(1);
        client.loginProvider("", "private", credentials, true);
    }

    @Test(expected = LoomClientException.class)
    public void testLoginBadProviderType() throws Exception {
        super.setUp(1);
        loginProvider("", "private");
    }

    @Test(expected = LoomClientException.class)
    public void testLoginBadProviderId() throws Exception {
        super.setUp(1);
        loginProvider("os", "");
    }

    @Test(expected = LoomClientException.class)
    public void testLoginBadOperationId() throws Exception {
        super.setUp(1);
        client.loginProviderBadOp("os", "private", credentials, "breakit");
    }

    @Test(expected = LoomClientException.class)
    public void testLoginBadCredentials() throws Exception {
        super.setUp(1);
        client.loginProviderBadOp("os", "private", null, "breakit");
    }

    @Test(expected = AssertionError.class)
    public void testLogoutNoSession() throws Exception {
        super.setUp(1);
        client.logoutAllProviders(true);
    }

    @Test(expected = LoomClientException.class)
    public void testGetProvidersNoType() throws Exception {
        connect();
        client.getProviders(null);
        cleanUp();
    }

    @Test(expected = LoomClientException.class)
    public void testCreateNullTapestry() throws Exception {
        connect();
        client.createTapestryDefinition(null);
        cleanUp();
    }

    @Test(expected = LoomClientException.class)
    public void testCreateTapestryNoThreads() throws Exception {
        connect();
        TapestryDefinition td = new TapestryDefinition();
        client.createTapestryDefinition(td);
        cleanUp();
    }

    @Test(expected = LoomClientException.class)
    public void testCreateTapestryNullThreads() throws Exception {
        connect();
        tapestryDefinition.setThreads(null);
        client.createTapestryDefinition(tapestryDefinition);
    }

    @Test(expected = LoomClientException.class)
    public void testUpdateTapestryNullThreads() throws Exception {
        connect();
        tapestryDefinition.setThreads(null);
        client.updateTapestryDefinition(tapestryDefinition.getId(), tapestryDefinition);
    }

    @Test(expected = AssertionError.class)
    public void testCreateTapestryWringId() throws Exception {
        connect();
        client.updateTapestryDefinition("breakIt", tapestryDefinition);
    }

    @Test(expected = LoomClientException.class)
    public void testGetTapestryNoTapId() throws Exception {
        connect();
        client.getTapestry(null);
    }

    @Test(expected = LoomClientException.class)
    public void deleteTapestryNoTapId() throws Exception {
        connect();
        client.getTapestry(null);
    }

    @Test(expected = AssertionError.class)
    public void createThreadDefNoTapId() throws Exception {
        connect();
        ThreadDefinition threadDefinition = tapestryDefinition.getThreads().get(0);
        client.createThreadDefinition("null", threadDefinition);
    }

    @Test(expected = AssertionError.class)
    public void upateThreadDefNoTapId() throws Exception {
        connect();
        ThreadDefinition threadDefinition = tapestryDefinition.getThreads().get(0);
        client.updateThreadDefinition(null, threadDefinition.getId(), threadDefinition);
    }

    @Test(expected = AssertionError.class)
    public void upateThreadDefBadTapId() throws Exception {
        connect();
        ThreadDefinition threadDefinition = tapestryDefinition.getThreads().get(0);
        client.updateThreadDefinition("breakIt", threadDefinition.getId(), threadDefinition);
    }

    @Test(expected = AssertionError.class)
    public void updateThreadDefBadThreadId() throws Exception {
        connect();
        ThreadDefinition threadDefinition = tapestryDefinition.getThreads().get(0);
        client.updateThreadDefinition(tapestryDefinition.getId(), "breakIt", threadDefinition);
    }

    @Test(expected = LoomClientException.class)
    public void updateThreadDefNoThread() throws Exception {
        connect();
        ThreadDefinition threadDefinition = tapestryDefinition.getThreads().get(0);
        client.updateThreadDefinition(tapestryDefinition.getId(), threadDefinition.getId(), null);
    }

    @Test(expected = LoomClientException.class)
    public void getThreadDefNoTap() throws Exception {
        connect();
        ThreadDefinition threadDefinition = tapestryDefinition.getThreads().get(0);
        client.getThreadDefinition(null, threadDefinition.getId());
    }

    @Test(expected = LoomClientException.class)
    public void getThreadDefBadTap() throws Exception {
        connect();
        ThreadDefinition threadDefinition = tapestryDefinition.getThreads().get(0);
        client.getThreadDefinition("breakit", threadDefinition.getId());
    }

    @Test(expected = LoomClientException.class)
    public void getThreadDefNoThread() throws Exception {
        connect();
        client.getThreadDefinition(tapestryDefinition.getId(), null);
    }

    @Test(expected = LoomClientException.class)
    public void deleteThreadDefNoThread() throws Exception {
        connect();
        client.getThreadDefinition(tapestryDefinition.getId(), null);
    }

    @Test(expected = LoomClientException.class)
    public void getThreadDefsNoTap() throws Exception {
        connect();
        client.getThreadDefinitions(null);
    }

    @Test(expected = LoomClientException.class)
    public void getThreadDefsBadTap() throws Exception {
        connect();
        client.getThreadDefinitions("breakIt");
    }

    @Test(expected = AssertionError.class)
    public void delThreadDefNoTap() throws Exception {
        connect();
        client.deleteThreadDefinitions(null);
    }

    @Test(expected = AssertionError.class)
    public void delThreadDefBadTap() throws Exception {
        connect();
        client.deleteThreadDefinitions("breakIt");
    }

    @Test(expected = LoomClientException.class)
    public void getResultsDefNoTap() throws Exception {
        connect();
        ThreadDefinition threadDefinition = tapestryDefinition.getThreads().get(0);
        client.getAggregation(null, threadDefinition.getId());
    }

    @Test(expected = LoomClientException.class)
    public void getResultsBadThread() throws Exception {
        connect();
        client.getAggregation(tapestryDefinition.getId(), "breakIt");
    }

    @Test(expected = LoomClientException.class)
    public void getResultsNoThread() throws Exception {
        connect();
        client.getAggregation(tapestryDefinition.getId(), null);
    }

    @Test(expected = LoomClientException.class)
    public void doActionNullAction() throws Exception {
        connect();
        client.executeAction(null);
    }

    @Test(expected = LoomClientException.class)
    public void getItemNullId() throws Exception {
        connect();
        client.getItem(null);
    }

    @Test(expected = LoomClientException.class)
    public void getItemNoId() throws Exception {
        connect();
        client.getItem("");
    }

    @Test(expected = LoomClientException.class)
    public void getProviderNullType() throws Exception {
        connect();
        client.getProvider(null, "private");
    }

    @Test(expected = LoomClientException.class)
    public void getProviderBadType() throws Exception {
        connect();
        client.getProvider("hp", "private");
    }

    @Test(expected = LoomClientException.class)
    public void getProviderNullId() throws Exception {
        connect();
        client.getProvider("os", null);
    }

    @Test(expected = LoomClientException.class)
    public void getProviderBadId() throws Exception {
        connect();
        client.getProvider("os", "hp");
    }

    @Test(expected = LoomClientException.class)
    public void getItemTypeNullType() throws Exception {
        connect();
        client.getItemTypes(null, "private");
    }

    @Test(expected = LoomClientException.class)
    public void getItemTypeBadType() throws Exception {
        connect();
        client.getItemTypes("hp", "private");
    }

    @Test(expected = LoomClientException.class)
    public void getItemTypeNullId() throws Exception {
        connect();
        client.getItemTypes("os", null);
    }

    @Test(expected = LoomClientException.class)
    public void getItemTypeBadId() throws Exception {
        connect();
        client.getItemTypes("os", "hp");
    }

    @Test(expected = LoomClientException.class)
    public void getItemTypesNullType() throws Exception {
        connect();
        client.getItemTypes(null);
    }

}
