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



import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.loom.adapter.os.BaseOsAdapter;
import com.hp.hpl.loom.adapter.os.OsImageType;
import com.hp.hpl.loom.adapter.os.OsInstanceType;
import com.hp.hpl.loom.api.service.utils.ActionHandling;
import com.hp.hpl.loom.api.service.utils.BasicQueryOperations;
import com.hp.hpl.loom.api.service.utils.QueryFormatting;
import com.hp.hpl.loom.api.service.utils.RelationshipsHandling;
import com.hp.hpl.loom.api.service.utils.TapestryHandling;
import com.hp.hpl.loom.model.QueryResult;
import com.hp.hpl.loom.tapestry.PatternDefinition;
import com.hp.hpl.loom.tapestry.PatternDefinitionList;
import com.hp.hpl.loom.tapestry.QueryDefinition;
import com.hp.hpl.loom.tapestry.TapestryDefinition;
import com.hp.hpl.loom.tapestry.ThreadDefinition;


/**
 * Example base class for Loom service integration tests.
 */
public abstract class LoomServiceRelationshipsTestBase extends IntegrationTestBase {
    private static final Log LOG = LogFactory.getLog(LoomServiceRelationshipsTestBase.class);

    @Before
    public void setUp() throws Exception {
        super.setUp(1);
        PatternDefinitionList patternDefinitionList = loginProvider();
        assertNotNull("Null response in getting PatternDefinitionList", patternDefinitionList);
    }

    @After
    public void cleanUp() {
        logoutProvider();
        LOG.info("Logged out");
    }

    /**
     * Simple test of relationships. Query all threads and make sure that all threads contain
     * elements, and that every element contains at least one relationship.
     */
    @Test
    public void testRelationshipsNonAggregated() throws InterruptedException {
        LOG.info("testRelationshipsNonAggregated start");
        TapestryDefinition tapestryDefinition = TapestryHandling.createFullTapestryFromPatterns(client, 0);
        List<ThreadDefinition> threads = tapestryDefinition.getThreads();

        new ArrayList<QueryResult>(threads.size());
        for (ThreadDefinition thread : threads) {
            String threadId = thread.getId();
            QueryResult qr =
                    BasicQueryOperations.getThreadWithWait(client, tapestryDefinition.getId(), threadId, greaterThan0);
            assertFalse("Result of a query did not contain elements for " + threadId, qr.getElements().isEmpty());
            RelationshipsHandling.checkAllElementsContainRelationships("For initial query.", qr);
        }

        // Lets get them again to make sure still have relationships
        for (ThreadDefinition thread : threads) {
            String threadId = thread.getId();
            QueryResult qr = BasicQueryOperations.clientGetAggregation(client, tapestryDefinition.getId(), threadId);
            assertFalse("Result of second query did not contain elements for " + threadId, qr.getElements().isEmpty());
            RelationshipsHandling.checkAllElementsContainRelationships("For second query.", qr);
        }
        LOG.info("testRelationshipsNonAggregated end");
    }

    /**
     * Test that an aggregated thread and any drill downs, have parent child relationships.
     *
     * @throws InterruptedException
     */
    @Test
    public void testParentChildRelationships() throws InterruptedException {
        LOG.info("testParentChildRelationships start");
        PatternDefinition patternDefinition =
                TapestryHandling.getPatternDefinitionMatchingId(client, BaseOsAdapter.ALL_FIVE_PATTERN);
        assertNotNull("Could not find pattern " + BaseOsAdapter.ALL_FIVE_PATTERN, patternDefinition);
        // Create a braided tapestry just for Instances
        ThreadDefinition instancesThread = TapestryHandling
                .findThreadDefinitionWithItemType(patternDefinition.getThreads(), OsInstanceType.TYPE_LOCAL_ID);
        QueryDefinition instancesQuery = QueryFormatting.createBraidQuery(instancesThread.getQuery().getInputs(), 6);
        ThreadDefinition simpleInstancesThread =
                new ThreadDefinition("instances", instancesThread.getItemType(), instancesQuery);
        TapestryDefinition tapestryDefinition =
                TapestryHandling.createTapestryDefinitionFromThreadDefinition(simpleInstancesThread);
        tapestryDefinition = TapestryHandling.createTapestryFromTapestryDefinition(client, tapestryDefinition);
        QueryResult qrInstances = BasicQueryOperations.getThreadWithWait(client, tapestryDefinition.getId(),
                simpleInstancesThread.getId(), greaterThan0);
        assertFalse("Result of a query did not contain elements for " + simpleInstancesThread.getId(),
                qrInstances.getElements().isEmpty());

        // Add a new thread to the tapestry to drill down
        QueryDefinition instances1Query = QueryFormatting.createDrillDownQuery(qrInstances);
        ThreadDefinition instances1Thread =
                new ThreadDefinition("instances1", instancesThread.getItemType(), instances1Query);
        tapestryDefinition.addThreadDefinition(instances1Thread);
        TapestryHandling.clientUpdateTapestryFromTapestryDefinition(client, tapestryDefinition);

        // Now check relationships
        QueryResult parentQr = RelationshipsHandling.checkRelationships(client, tapestryDefinition.getId(),
                simpleInstancesThread.getId(), false);
        QueryResult childQr =
                RelationshipsHandling.checkRelationships(client, tapestryDefinition.getId(), instances1Thread.getId());
        RelationshipsHandling.checkCrossThreadRelationships(parentQr, childQr);
        LOG.info("testParentChildRelationships end");
    }

    /**
     * Test that an Instances still compute relationships after perform an action.
     *
     * @throws InterruptedException
     */
    @Test
    public void testRelationshipsAfterAction() throws InterruptedException {
        LOG.info("testRelationshipsAfterAction start");
        PatternDefinition patternDefinition =
                TapestryHandling.getPatternDefinitionMatchingId(client, BaseOsAdapter.ALL_FIVE_PATTERN);
        assertNotNull("Could not find pattern " + BaseOsAdapter.ALL_FIVE_PATTERN, patternDefinition);

        // Create a non-braided tapestry for Instances and Images
        List<ThreadDefinition> threads = new ArrayList<ThreadDefinition>(2);
        ThreadDefinition instancesThread = TapestryHandling
                .findThreadDefinitionWithItemType(patternDefinition.getThreads(), OsInstanceType.TYPE_LOCAL_ID);
        QueryDefinition instancesQuery = QueryFormatting.createSimpleQuery(instancesThread.getQuery().getInputs());
        ThreadDefinition simpleInstancesThread =
                new ThreadDefinition("instances", instancesThread.getItemType(), instancesQuery);
        threads.add(simpleInstancesThread);
        ThreadDefinition imagesThread = TapestryHandling
                .findThreadDefinitionWithItemType(patternDefinition.getThreads(), OsImageType.TYPE_LOCAL_ID);
        QueryDefinition imagesQuery = QueryFormatting.createSimpleQuery(imagesThread.getQuery().getInputs());
        ThreadDefinition simpleImagesThread = new ThreadDefinition("images", imagesThread.getItemType(), imagesQuery);
        threads.add(simpleImagesThread);
        TapestryDefinition tapestryDefinition = TapestryHandling.createTapestryDefinitionFromThreadDefinitions(threads);
        tapestryDefinition = TapestryHandling.createTapestryFromTapestryDefinition(client, tapestryDefinition);

        // Check that Instances and Images have relationships with each other
        QueryResult qrInstances = RelationshipsHandling.checkRelationships(client, tapestryDefinition.getId(),
                simpleInstancesThread.getId());
        QueryResult qrImages = RelationshipsHandling.checkRelationships(client, tapestryDefinition.getId(),
                simpleImagesThread.getId(), false);
        RelationshipsHandling.checkCrossThreadRelationships(qrInstances, qrImages);

        ActionHandling.pickAnInstanceAndReboot(client, tapestryDefinition.getId(), simpleInstancesThread.getId());

        RelationshipsHandling.checkRelationships(client, tapestryDefinition.getId(), simpleInstancesThread.getId());
        RelationshipsHandling.checkRelationships(client, tapestryDefinition.getId(), simpleImagesThread.getId(), false);
        RelationshipsHandling.checkCrossThreadRelationships(qrInstances, qrImages);

        LOG.info("testRelationshipsAfterAction end");
    }
}
