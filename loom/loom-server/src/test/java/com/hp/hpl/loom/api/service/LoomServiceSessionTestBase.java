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
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.loom.api.client.LoomClient;
import com.hp.hpl.loom.api.client.LoomClientException;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.tapestry.PatternDefinitionList;

public abstract class LoomServiceSessionTestBase extends IntegrationTestBase {

    @Before
    public void setUp() throws Exception {
        super.setUp(5);
    }

    protected PatternDefinitionList loginProvider(final LoomClient client, final String providerType,
            final String providerId) {
        Credentials credentials = new Credentials(testDataConfig.getUsername(), testDataConfig.getPassword());
        return client.loginProvider(providerType, providerId, credentials);
    }

    protected void logoutProvider(final LoomClient client, final String providerType, final String providerId) {
        client.logoutProvider(providerType, providerId);
    }

    @Test
    public void testMultipleSession() throws InterruptedException {
        String providerType = "os";
        String providerId = "private";

        for (LoomClient loomClient : clients) {
            PatternDefinitionList patterns = loginProvider(loomClient, providerType, providerId);
            assertNotNull("PatternDefinitionList is null", patterns);
        }

        logoutProvider(client, providerType, providerId);

        for (LoomClient nextClient : clients) {
            if (nextClient.equals(client)) {
                try {
                    client.getPatterns();
                    fail("Expecting an exception");
                } catch (LoomClientException ex) {
                    assertEquals(401, ex.getStatusCode());
                }
            } else {
                PatternDefinitionList patterns = nextClient.getPatterns();
                assertNotNull("PatternDefinitionList is null", patterns);
                logoutProvider(nextClient, providerType, providerId);
            }
        }
    }
}
