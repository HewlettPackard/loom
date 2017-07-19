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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;

import com.hp.hpl.loom.adapter.os.BaseOsAdapter;
import com.hp.hpl.loom.adapter.os.fake.FakeConfig;
import com.hp.hpl.loom.api.service.utils.BasicQueryOperations;
import com.hp.hpl.loom.api.service.utils.TapestryHandling;
import com.hp.hpl.loom.manager.tapestry.TapestryManager;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.QueryResult;
import com.hp.hpl.loom.tapestry.PatternDefinition;
import com.hp.hpl.loom.tapestry.PatternDefinitionList;
import com.hp.hpl.loom.tapestry.TapestryDefinition;
import com.hp.hpl.loom.tapestry.ThreadDefinition;

public class DualProviderTestBase extends IntegrationTestBase {

    private static final String PUB_PROV = "public";
    private static final String PRIV_PROV = "private";

    @Autowired
    TapestryManager tapestryManager;

    private static final Log LOG = LogFactory.getLog(DualProviderTestBase.class);
    private TapestryDefinition tapestryDefinition;
    List<String> logins = new ArrayList(2);

    @Test
    public void testPrivPubLogin() throws InterruptedException {
        LOG.info("testPrivPubLogin start");
        StopWatch watch = new StopWatch();

        logins.add(PUB_PROV);
        PatternDefinitionList defList = loginProvider(PUB_PROV);
        LOG.info("**************************************************************************************************");
        LOG.info("Logged into a single provider:'" + PUB_PROV + "'");
        LOG.info("PatternDefinitionList: '" + defList.getPatterns().size() + "'");
        LOG.info("**************************************************************************************************");

        watch.start();
        PatternDefinitionList patternDefinitions = client.getPatterns();
        List<ThreadDefinition> threads = null;
        for (PatternDefinition pd : patternDefinitions.getPatterns()) {
            if (pd.getId().equals("os-" + BaseOsAdapter.ALL_FIVE_PATTERN)) {
                LOG.info("found the right pattern - now get the aggregation: " + pd.getId());
                threads = pd.getThreads();
                break;
            }
        }

        TapestryDefinition tapestryDefinition = TapestryHandling.createTapestryDefinitionFromThreadDefinitions(threads);
        tapestryDefinition = TapestryHandling.createTapestryFromTapestryDefinition(client, tapestryDefinition);
        ThreadDefinition thread = threads.iterator().next();
        String threadId = thread.getId();

        QueryResult qr1 = BasicQueryOperations.getThreadWithWait(client, tapestryDefinition.getId(), threadId,
                equalsNumInstancesPublicProvider);
        assertEquals("Incorrect number of instances", testDataConfig.getExpectedInstanceNbr(FakeConfig.PUBLIC_INDEX),
                qr1.getElements().size());
        assertTrue("fibre should not be a da", qr1.getElements().get(0).getEntity().isItem());

        logins.add(PRIV_PROV);
        loginProvider(PRIV_PROV);
        LOG.info("**************************************************************************************************");
        LOG.info("Logged into a second provider:'" + PRIV_PROV + "'");
        LOG.info("**************************************************************************************************");

        qr1 = BasicQueryOperations.getThreadWithWait(client, tapestryDefinition.getId(), threadId,
                equalsNumInstancesPrivateAndPublicProvider);
        assertEquals("Incorrect number of instances", testDataConfig.getExpectedInstanceNbr(FakeConfig.PRIVATE_INDEX)
                + testDataConfig.getExpectedInstanceNbr(FakeConfig.PUBLIC_INDEX), qr1.getElements().size());
        assertTrue("fibre should not be a da", qr1.getElements().get(0).getEntity().isItem());

        logins.remove(PRIV_PROV);
        logoutProvider(PRIV_PROV);
        LOG.info("**************************************************************************************************");
        LOG.info("Logged into a single provider:'" + PUB_PROV + "'");
        LOG.info("**************************************************************************************************");

        qr1 = client.getAggregation(tapestryDefinition.getId(), threadId);
        assertEquals("Incorrect number of instances", testDataConfig.getExpectedInstanceNbr(FakeConfig.PUBLIC_INDEX),
                qr1.getElements().size());
        assertTrue("fibre should not be a da", qr1.getElements().get(0).getEntity().isItem());

        watch.stop();
        LOG.info("testGetAggregation end --> " + watch);
    }

    private PatternDefinitionList loginProvider(final String providerId) throws InterruptedException {
        Credentials credentials = new Credentials(testDataConfig.getUsername(), testDataConfig.getPassword());
        PatternDefinitionList patt = client.loginProvider("os", providerId, credentials);
        return patt;
    }

    protected void logoutProvider(final String providerId) throws InterruptedException {
        client.logoutProvider("os", providerId);
        Thread.sleep(500);
    }

    protected void logoutProvider(final List<String> providerIds) throws InterruptedException {
        for (String providerId : providerIds) {
            logoutProvider(providerId);
        }
    }

    @Autowired
    private WebApplicationContext ctx;

    @Override
    void setupTestAndClient(final int i) {
        LOG.info("setupTestAndClient start");
        setupMvcClient(ctx, 1);
        LOG.info("setupTestAndClient end");
    }
}
