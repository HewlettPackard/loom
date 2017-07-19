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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hp.hpl.loom.adapter.os.BaseOsAdapter;
import com.hp.hpl.loom.adapter.os.OsImage;
import com.hp.hpl.loom.adapter.os.OsImageType;
import com.hp.hpl.loom.adapter.os.OsInstance;
import com.hp.hpl.loom.adapter.os.OsInstanceType;
import com.hp.hpl.loom.adapter.os.fake.FakeConfig;
import com.hp.hpl.loom.api.client.LoomClientException;
import com.hp.hpl.loom.api.service.utils.ActionHandling;
import com.hp.hpl.loom.api.service.utils.BasicQueryOperations;
import com.hp.hpl.loom.api.service.utils.DrillDownThreads;
import com.hp.hpl.loom.api.service.utils.QueryFormatting;
import com.hp.hpl.loom.api.service.utils.TapestryHandling;
import com.hp.hpl.loom.api.service.utils.ThreadNavigation;
import com.hp.hpl.loom.exceptions.NoSuchThreadDefinitionException;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.manager.query.QueryOperation;
import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionParameter;
import com.hp.hpl.loom.model.ActionParameters;
import com.hp.hpl.loom.model.ActionResult;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.ProviderImpl;
import com.hp.hpl.loom.model.QueryResult;
import com.hp.hpl.loom.model.QueryResultElement;
import com.hp.hpl.loom.relationships.RelationshipUtil;
import com.hp.hpl.loom.tapestry.Meta;
import com.hp.hpl.loom.tapestry.Operation;
import com.hp.hpl.loom.tapestry.PatternDefinition;
import com.hp.hpl.loom.tapestry.PatternDefinitionList;
import com.hp.hpl.loom.tapestry.QueryDefinition;
import com.hp.hpl.loom.tapestry.TapestryDefinition;
import com.hp.hpl.loom.tapestry.ThreadDefinition;

public abstract class LoomServicePipeTestBase extends IntegrationTestBase {
    private static final Log LOG = LogFactory.getLog(LoomServicePipeTestBase.class);
    private TapestryDefinition tapestryDefinition;
    private List<PatternDefinition> patterns;
    private Provider provider = new ProviderImpl("os", "providerId", "authEndpoint", "providerName", "adapterPackage");

    @After
    public void cleanUp() {
        logoutProvider();
        LOG.info("Logged out");
    }

    @Before
    public void setUp() throws Exception {
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

    @Test
    public void testAdapterCustomQueryOp() {
        StopWatch watch = new StopWatch();
        watch.start();

        ThreadDefinition thread = TapestryHandling.findThreadDefinitionWithItemType(tapestryDefinition.getThreads(),
                OsInstanceType.TYPE_LOCAL_ID);
        String threadId = thread.getId();

        // get the result
        QueryResult qr1 = BasicQueryOperations.getThreadWithWait(client, tapestryDefinition.getId(), threadId,
                equalsNumInstancesPrivateProvider);

        assertEquals("Incorrect number of instances", testDataConfig.getExpectedInstanceNbr(FakeConfig.PRIVATE_INDEX),
                qr1.getElements().size());
        assertTrue("fibre should not be a da", !qr1.getElements().get(0).getEntity().isAggregation());

        Operation customOperation =
                new Operation(("os" + Provider.PROV_SEPARATOR + "private" + Provider.PROV_SEPARATOR + "normaliseRam"),
                        new HashMap<>(0));
        List<Operation> customPipe = new ArrayList<Operation>(1);
        customPipe.add(customOperation);
        QueryDefinition customQuery = new QueryDefinition();
        customQuery.setOperationPipeline(customPipe);
        customQuery.setInputs(thread.getQuery().getInputs());

        ThreadDefinition threadDefinition = new ThreadDefinition("reg8", thread.getItemType(), customQuery);
        // add the newly created thread to the tapestry and update
        tapestryDefinition.addThreadDefinition(threadDefinition);
        client.updateTapestryDefinition(tapestryDefinition.getId(), tapestryDefinition);

        QueryResult qr2 = client.getAggregation(tapestryDefinition.getId(), threadDefinition.getId());

        assertEquals("Incorrect number of instances", testDataConfig.getExpectedInstanceNbr(FakeConfig.PRIVATE_INDEX),
                qr2.getElements().size());
        assertTrue("fibre should not be a da", !qr2.getElements().get(0).getEntity().isAggregation());

    }

    @Test
    public void testEndToEndGroupBraid() throws InterruptedException, JsonProcessingException {
        LOG.info("testEndToEndGroupBraid start");
        StopWatch watch = new StopWatch();
        watch.start();

        ThreadDefinition thread = TapestryHandling.findThreadDefinitionWithItemType(tapestryDefinition.getThreads(),
                OsInstanceType.TYPE_LOCAL_ID);
        String threadId = thread.getId();

        // get the result
        QueryResult qr1 = BasicQueryOperations.getThreadWithWait(client, tapestryDefinition.getId(), threadId,
                equalsNumInstancesPrivateProvider);
        assertEquals("Incorrect number of instances", testDataConfig.getExpectedInstanceNbr(FakeConfig.PRIVATE_INDEX),
                qr1.getElements().size());
        assertTrue("fibre should not be a da", !qr1.getElements().get(0).getEntity().isAggregation());

        Map<String, Object> groupParams = new HashMap<>(1);
        groupParams.put(QueryOperation.PROPERTY, OsInstanceType.ATTR_FLAVOR);
        QueryDefinition groupBraidQuery = QueryFormatting.createGroupByBraidQuery(qr1, groupParams, testFibres);
        ThreadDefinition threadDefinition = new ThreadDefinition("reg8", thread.getItemType(), groupBraidQuery);
        // add the newly created thread to the tapestry and update
        tapestryDefinition.addThreadDefinition(threadDefinition);
        client.updateTapestryDefinition(tapestryDefinition.getId(), tapestryDefinition);

        QueryResult qr2 = client.getAggregation(tapestryDefinition.getId(), threadDefinition.getId());
        ArrayList<QueryResultElement> qr2Elems = qr2.getElements();
        assertTrue("fibre should be an aggregation", qr2Elems.get(0).getEntity().isAggregation());
        assertEquals("Aggregation has not been properly braided", testFibres, qr2.getElements().size());

        QueryDefinition drillDownQuery = QueryFormatting.createDrillDownQuery(qr2);
        ThreadDefinition drilledThreadDefinition =
                new ThreadDefinition("drilledDown", thread.getItemType(), drillDownQuery);
        // add the newly created thread to the tapestry and update
        tapestryDefinition.addThreadDefinition(drilledThreadDefinition);
        client.updateTapestryDefinition(tapestryDefinition.getId(), tapestryDefinition);

        QueryResult qr3 = client.getAggregation(tapestryDefinition.getId(), drilledThreadDefinition.getId());
        ArrayList<QueryResultElement> qr3Elems = qr3.getElements();
        assertTrue("fibre should be an aggregation", qr3Elems.get(0).getEntity().isAggregation());
        assertTrue("it has been properly grouped", qr3.getElements().size() == numElemsFirstGroupByFlavour);

        QueryDefinition drillDown2Items = QueryFormatting.createDrillDownQuery(qr3);
        ThreadDefinition drilledThreadDefinition2 =
                new ThreadDefinition("drilledDown2", thread.getItemType(), drillDown2Items);
        // add the newly created thread to the tapestry and update
        tapestryDefinition.addThreadDefinition(drilledThreadDefinition2);
        client.updateTapestryDefinition(tapestryDefinition.getId(), tapestryDefinition);

        QueryResult qr4 = client.getAggregation(tapestryDefinition.getId(), drilledThreadDefinition2.getId());
        ArrayList<QueryResultElement> qr4Elems = qr4.getElements();
        assertTrue("fibre should be an item", qr4Elems.get(0).getEntity().isItem());

        watch.stop();
        LOG.info("testEndToEndGroupBraid end --> " + watch);
    }

    /**
     * Test for grouping on an attribute that is a relationship with other items. In this case,
     * group Instances by image.
     */
    @Test
    public void testEndToEndGroupRelationships() throws InterruptedException, JsonProcessingException {
        LOG.info("testEndToEndGroupRelationships start");
        StopWatch watch = new StopWatch();
        watch.start();

        // Get the images

        ThreadDefinition imagesThread = TapestryHandling
                .findThreadDefinitionWithItemType(tapestryDefinition.getThreads(), OsImageType.TYPE_LOCAL_ID);
        String imagesThreadId = imagesThread.getId();


        QueryResult qrImages = BasicQueryOperations.getThreadWithWait(client, tapestryDefinition.getId(),
                imagesThreadId, equalsNumImagesPrivateProvider);
        assertEquals("Incorrect number of images", testDataConfig.getExpectedImageNbr(FakeConfig.PRIVATE_INDEX),
                qrImages.getElements().size());
        assertTrue("Fibre should not be a da", !qrImages.getElements().get(0).getEntity().isAggregation());
        // Some images may not be connected to an instance, so count the number that are
        Map<String, OsImage> connectedImages = new HashMap<>(qrImages.getElements().size());
        for (QueryResultElement qre : qrImages.getElements()) {
            OsImage image = (OsImage) qre.getEntity();
            for (String relation : qre.getRelations()) {
                if (relation.contains(OsInstanceType.TYPE_LOCAL_ID)) {
                    connectedImages.put(image.getLogicalId(), image);
                    break;
                }
            }
        }

        // Get the instances
        ThreadDefinition instancesThread = TapestryHandling
                .findThreadDefinitionWithItemType(tapestryDefinition.getThreads(), OsInstanceType.TYPE_LOCAL_ID);
        String instancesThreadId = instancesThread.getId();

        QueryResult qr1 = BasicQueryOperations.getThreadWithWait(client, tapestryDefinition.getId(), instancesThreadId,
                equalsNumInstancesPrivateProvider);
        assertEquals("Incorrect number of instances", testDataConfig.getExpectedInstanceNbr(FakeConfig.PRIVATE_INDEX),
                qr1.getElements().size());
        assertTrue("fibre should not be a da", !qr1.getElements().get(0).getEntity().isAggregation());

        // Add a thread of instances, grouped by image
        Map<String, Object> groupParams = new HashMap<>(1);
        groupParams.put(QueryOperation.PROPERTY, RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                provider.getProviderType(), OsImageType.TYPE_LOCAL_ID, OsInstanceType.TYPE_LOCAL_ID)); // Group
                                                                                                       // by
                                                                                                       // image
        QueryDefinition groupBraidQuery = QueryFormatting.createGroupByQuery(qr1.getLogicalId(), groupParams);
        ThreadDefinition threadDefinition =
                new ThreadDefinition("reg8", instancesThread.getItemType(), groupBraidQuery);
        // add the newly created thread to the tapestry and update
        tapestryDefinition.addThreadDefinition(threadDefinition);
        client.updateTapestryDefinition(tapestryDefinition.getId(), tapestryDefinition);
        QueryResult qr2 = client.getAggregation(tapestryDefinition.getId(), threadDefinition.getId());
        ArrayList<QueryResultElement> qr2Elems = qr2.getElements();
        assertTrue("fibre should be an aggregation", qr2Elems.get(0).getEntity().isAggregation());
        assertEquals("Did not contain correct number of image groups", connectedImages.size(),
                qr2.getElements().size());

        watch.stop();
        LOG.info("testEndToEndGroupRelationships end --> " + watch);
    }

    @Test
    public void testSingleGroupBraidEndToEndTest() throws InterruptedException {
        LOG.info("testSingleGroupBraidEndToEndTest start");
        StopWatch watch = new StopWatch();
        watch.start();
        ThreadDefinition thread = TapestryHandling.findThreadDefinitionWithItemType(tapestryDefinition.getThreads(),
                OsInstanceType.TYPE_LOCAL_ID);
        String threadId = thread.getId();

        QueryResult qr1 = BasicQueryOperations.getThreadWithWait(client, tapestryDefinition.getId(), threadId,
                equalsNumInstancesPrivateProvider);
        assertTrue("fibre should not be a da", !qr1.getElements().get(0).getEntity().isAggregation());

        Map<String, Object> groupParams = new HashMap<>(1);
        groupParams.put(QueryOperation.PROPERTY, OsInstanceType.ATTR_STATUS);
        QueryDefinition groupBraidQuery = QueryFormatting.createGroupByBraidQuery(qr1, groupParams, testFibres);
        ThreadDefinition threadDefinition = new ThreadDefinition("reg8", thread.getItemType(), groupBraidQuery);
        // add the newly created thread to the tapestry and update
        tapestryDefinition.addThreadDefinition(threadDefinition);
        client.updateTapestryDefinition(tapestryDefinition.getId(), tapestryDefinition);

        QueryResult qr2 = client.getAggregation(tapestryDefinition.getId(), threadDefinition.getId());
        assertTrue("fibre should be of aggregations", qr2.getElements().get(0).getEntity().isAggregation());

        QueryDefinition drillDownQuery = QueryFormatting.createDrillDownQuery(qr2);
        ThreadDefinition drilledThreadDefinition =
                new ThreadDefinition("drilledDown", thread.getItemType(), drillDownQuery);
        // add the newly created thread to the tapestry and update
        tapestryDefinition.addThreadDefinition(drilledThreadDefinition);
        client.updateTapestryDefinition(tapestryDefinition.getId(), tapestryDefinition);

        // now query he the new thread
        QueryResult qr3 = client.getAggregation(tapestryDefinition.getId(), drilledThreadDefinition.getId());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        try {
            LOG.info(mapper.writeValueAsString(qr2));
            LOG.info(mapper.writeValueAsString(qr3));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        assertTrue("fibre should be an item", qr3.getElements().get(0).getEntity().isItem());

        watch.stop();
        LOG.info("testSingleGroupBraidEndToEndTest end --> " + watch);
    }

    @Test
    public void testPercentiles() throws NoSuchThreadDefinitionException {
        StopWatch watch = new StopWatch();
        LOG.info("testFilter start --> " + watch);
        watch.start();

        ThreadDefinition thread = TapestryHandling.findThreadDefinitionWithItemType(tapestryDefinition.getThreads(),
                OsInstanceType.TYPE_LOCAL_ID);
        String threadId = thread.getId();

        QueryResult qr1 = BasicQueryOperations.getThreadWithWait(client, tapestryDefinition.getId(), threadId,
                equalsNumInstancesPrivateProvider);
        assertTrue("fibre should not be a da", !qr1.getElements().get(0).getEntity().isAggregation());

        Map<String, Object> percentParams = new HashMap<>(1);
        percentParams.put(QueryOperation.BUCKETS, 7);
        percentParams.put(QueryOperation.PROPERTY, ItemType.CORE_NAME + "ram");
        QueryDefinition percentQuery = QueryFormatting.createPercentQuery(qr1, percentParams);
        ThreadDefinition threadDefinition = new ThreadDefinition("percent", thread.getItemType(), percentQuery);
        tapestryDefinition.addThreadDefinition(threadDefinition);
        client.updateTapestryDefinition(tapestryDefinition.getId(), tapestryDefinition);

        QueryResult qr2 = client.getAggregation(tapestryDefinition.getId(), threadDefinition.getId());



        assertTrue("Expected 7 groups", qr2.getElements().size() == 7);

        percentParams.put(QueryOperation.BUCKETS, 12);
        percentParams.put(QueryOperation.PROPERTY, ItemType.CORE_NAME + "ram");
        percentQuery = QueryFormatting.createPercentQuery(qr1, percentParams);
        threadDefinition = new ThreadDefinition("percent", thread.getItemType(), percentQuery);
        tapestryDefinition.addThreadDefinition(threadDefinition);
        client.updateTapestryDefinition(tapestryDefinition.getId(), tapestryDefinition);

        QueryResult qr3 = client.getAggregation(tapestryDefinition.getId(), threadDefinition.getId());

        assertTrue("Expected 12 groups", qr3.getElements().size() == 12);


        percentParams.put(QueryOperation.PROPERTY, ItemType.CORE_NAME + "ram");
        percentParams.put(QueryOperation.BUCKETS, null);
        percentQuery = QueryFormatting.createPercentQuery(qr1, percentParams);
        threadDefinition = new ThreadDefinition("percent", thread.getItemType(), percentQuery);
        tapestryDefinition.addThreadDefinition(threadDefinition);
        client.updateTapestryDefinition(tapestryDefinition.getId(), tapestryDefinition);

        QueryResult qr4 = client.getAggregation(tapestryDefinition.getId(), threadDefinition.getId());

        //
        // ObjectMapper mapper = new ObjectMapper();
        // mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        // try {
        // LOG.info(mapper.writeValueAsString(qr4));
        // } catch (JsonProcessingException e) {
        // e.printStackTrace(); // To change body of catch statement use File | Settings | File
        // }

        assertTrue("Expected 10 groups", qr4.getElements().size() == 10);


        watch.stop();
        LOG.info("testFilter end --> " + watch);
    }


    @Test
    public void testFilter() throws NoSuchThreadDefinitionException {
        StopWatch watch = new StopWatch();
        LOG.info("testFilter start --> " + watch);
        watch.start();

        ThreadDefinition thread = TapestryHandling.findThreadDefinitionWithItemType(tapestryDefinition.getThreads(),
                OsInstanceType.TYPE_LOCAL_ID);
        String threadId = thread.getId();

        QueryResult qr1 = BasicQueryOperations.getThreadWithWait(client, tapestryDefinition.getId(), threadId,
                equalsNumInstancesPrivateProvider);
        assertTrue("fibre should not be a da", !qr1.getElements().get(0).getEntity().isAggregation());

        Map<String, Object> filterParams = new HashMap<>(1);


        // LOG.info("************ Query to create an empty filtered output ************");
        // QueryDefinition filterBraidQuery = QueryFormatting.createFilterBraidQuery(qr1,
        // filterParams, testFibres);
        // ThreadDefinition threadDefinition = new ThreadDefinition("reg8", thread.getItemType(),
        // filterBraidQuery);
        // // add the newly created thread to the tapestry and update
        // tapestryDefinition.addThreadDefinition(threadDefinition);
        // client.updateTapestryDefinition(tapestryDefinition.getId(), tapestryDefinition);
        //
        // QueryResult qr2 = client.getAggregation(tapestryDefinition.getId(),
        // threadDefinition.getId());
        // assertEquals(0, qr2.getElements().size()); // no element matches this pseudo random
        // naming
        // // pattern
        //
        // LOG.info("************ Remove thread from tapestry ************");
        // // update tapestry
        // tapestryDefinition.removeThreadDefinition(threadDefinition.getId());
        // client.updateTapestryDefinition(tapestryDefinition.getId(), tapestryDefinition);


        filterParams.put("pattern", "*vm-*2*");
        QueryDefinition filterBraidQuery = QueryFormatting.createFilterBraidQuery(qr1, filterParams, testFibres);
        filterBraidQuery = QueryFormatting.createFilterBraidQuery(qr1, filterParams, testFibres);
        ThreadDefinition threadDefinition = new ThreadDefinition("reg8", thread.getItemType(), filterBraidQuery);
        tapestryDefinition.addThreadDefinition(threadDefinition);
        client.updateTapestryDefinition(tapestryDefinition.getId(), tapestryDefinition);

        QueryResult qr2 = client.getAggregation(tapestryDefinition.getId(), threadDefinition.getId());
        LOG.info("RES NAMES "
                + qr2.getElements().stream().map(s -> s.getEntity().getName()).collect(Collectors.toList()));


        // ObjectMapper mapper = new ObjectMapper();
        // mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        // try {
        // LOG.info(mapper.writeValueAsString(qr2));
        // } catch (JsonProcessingException e) {
        // e.printStackTrace(); //To change body of catch statement use File | Settings | File
        // Templates.
        // }

        List<Fibre> resultingEntities = new ArrayList<>();
        int i = 0;
        for (QueryResultElement qre : qr2.getElements()) {

            LOG.info("************ Update tap as user drills down ************");
            QueryDefinition drillDownQuery = QueryFormatting.indxDrillDown(qr2, 0, i);
            ThreadDefinition drilledThreadDefinition =
                    new ThreadDefinition("drilledDown" + i, thread.getItemType(), drillDownQuery);
            // add the newly created thread to the tapestry and update
            tapestryDefinition.addThreadDefinition(drilledThreadDefinition);
            client.updateTapestryDefinition(tapestryDefinition.getId(), tapestryDefinition);

            QueryResult qr = client.getAggregation(tapestryDefinition.getId(), drilledThreadDefinition.getId());

            LOG.info("Size " + qr.getElements().size());
            resultingEntities.addAll(
                    qr.getElements().stream().map(queryRes -> queryRes.getEntity()).collect(Collectors.toList()));
            i++;
        }

        int contains2 = (int) qr1.getElements().stream().filter(qr -> qr.getEntity().getName().contains("2")).count();
        int filtered = (int) resultingEntities.stream().count();

        assertEquals(contains2, filtered);

        watch.stop();
        LOG.info("testFilter end --> " + watch);
    }

    @Test
    public void testSetTapestryNullId() {
        LOG.info("testSetTapestryNullId start");
        // Setup should have created a tapestry already, so create a new one with null ID
        int maxFibres = 9;
        TapestryDefinition tapestryDefinition = TapestryHandling.createFullTapestryFromPatterns(client, maxFibres);
        BasicQueryOperations.queryAllThreads(client, tapestryDefinition);
        LOG.info("testSetTapestryNullId end");
    }

    @Test
    public void testGroupByAllAttributes() {
        LOG.info("testGroupByAllAttributes start");
        doOperationByAllAttributes(DefaultOperations.GROUP_BY.toString(), 45);
        LOG.info("testGroupByAllAttributes end");
    }

    @Test
    public void testSortByAllAttributes() {
        LOG.info("testSortByAllAttributes start");
        doOperationByAllAttributes(DefaultOperations.SORT_BY.toString(), 45);
        LOG.info("testSortByAllAttributes end");
    }

    private void doOperationByAllAttributes(final String op, final int maxFibres) {
        Map<String, ItemType> allItemTypes = new HashMap<>();
        for (PatternDefinition pattern : patterns) {
            Meta meta = pattern.get_meta();
            Map<String, ItemType> itemTypesMap = meta.getItemTypes();
            for (Map.Entry<String, ItemType> itemTypeEntry : itemTypesMap.entrySet()) {
                if (!itemTypeEntry.getKey().equals("os-provider")) {
                    allItemTypes.put(itemTypeEntry.getKey(), itemTypeEntry.getValue());
                }
            }
        }
        for (ItemType type : allItemTypes.values()) {
            doOperationByAllAttributesOnType(op, type, maxFibres);
        }
    }

    private void doOperationByAllAttributesOnType(final String op, final ItemType type, final int maxFibres) {
        TapestryDefinition tapestryDefinition = TapestryHandling.createFullTapestryFromPatterns(client, maxFibres);
        // Find thread
        ThreadDefinition thread =
                TapestryHandling.findThreadDefinitionWithItemType(tapestryDefinition.getThreads(), type.getId());
        QueryDefinition query = thread.getQuery();

        // And modify with a group by op parameter modified for each of the parameters for the op
        List<String> names = ThreadNavigation.getOpPropertyNames(op, type);
        for (String name : names) {
            LOG.info("modify thread for " + op + " " + type.getId() + " by " + name);
            Map<String, Object> paramMap = new HashMap<>(1);
            paramMap.put(QueryOperation.PROPERTY, name);
            if (op.equals(DefaultOperations.SORT_BY.toString())) {
                paramMap.put(QueryOperation.ORDER, QueryOperation.ASC_ORDER);
                query = QueryFormatting.createSortByBraidQuery(query.getInputs(), paramMap, maxFibres);
            } else if (op.equals(DefaultOperations.GROUP_BY.toString())) {
                query = QueryFormatting.createGroupByBraidQuery(paramMap, maxFibres, query.getInputs());
            }
            thread.setQuery(query);
            TapestryHandling.clientUpdateTapestryFromTapestryDefinition(client, tapestryDefinition);
            BasicQueryOperations.queryAllThreads(client, tapestryDefinition);
        }
    }

    @Test
    public void testGetSingleItem() {
        StopWatch watch = new StopWatch();
        LOG.info("testGetItem start --> " + watch);
        watch.start();

        TapestryDefinition tapestryDefinition = TapestryHandling.createFullTapestryFromPatterns(client, testFibres);
        DrillDownThreads drillDownThreads =
                ThreadNavigation.drillDownOnTypeId(client, tapestryDefinition, OsInstanceType.TYPE_LOCAL_ID, 0, true);

        String instancesLogicalId = drillDownThreads.getItemThreadId();
        QueryResult qr = client.getAggregation(tapestryDefinition.getId(), instancesLogicalId);

        QueryResultElement qre = qr.getElements().get(0);
        String logicalId = qre.getEntity().getLogicalId();

        QueryResultElement itemElement = client.getItem(logicalId);
        Item item = (Item) itemElement.getEntity();
        assertNotNull(item);
        assertEquals(logicalId, item.getLogicalId());
        watch.stop();
        LOG.info("testGetItem end --> " + watch);
    }

    @Test
    public void testSortByDescNameLargeAggregation() throws InterruptedException {
        LOG.info("testSortByDescNameAggregation start");
        StopWatch watch = new StopWatch();
        watch.start();
        ThreadDefinition thread = TapestryHandling.findThreadDefinitionWithItemType(tapestryDefinition.getThreads(),
                OsInstanceType.TYPE_LOCAL_ID);
        String threadId = thread.getId();

        int expectedInstances = testDataConfig.getExpectedInstanceNbr(FakeConfig.PRIVATE_INDEX);
        QueryResult qr1 = BasicQueryOperations.getThreadWithWait(client, tapestryDefinition.getId(), threadId,
                equalsNumInstancesPrivateProvider);
        assertEquals("Incorrect number of instances", testDataConfig.getExpectedInstanceNbr(FakeConfig.PRIVATE_INDEX),
                qr1.getElements().size());
        assertTrue("fibre should not be a da", !qr1.getElements().get(0).getEntity().isAggregation());

        Map<String, Object> revSortParams = new HashMap<>(1);
        revSortParams.put(QueryOperation.PROPERTY, "name");
        revSortParams.put(QueryOperation.ORDER, QueryOperation.DSC_ORDER);
        QueryDefinition revSortQuery = QueryFormatting.createSortByQuery(qr1, revSortParams, testFibres);
        ThreadDefinition threadDefinition = new ThreadDefinition("sorted", thread.getItemType(), revSortQuery);
        // add the newly created thread to the tapestry and update
        tapestryDefinition.addThreadDefinition(threadDefinition);
        client.updateTapestryDefinition(tapestryDefinition.getId(), tapestryDefinition);

        QueryResult qr2 = client.getAggregation(tapestryDefinition.getId(), threadDefinition.getId());
        // check names come in lexicographic order
        assertEquals("First instance name incorrect", "vm-" + (expectedInstances - 1),
                qr2.getElements().get(0).getEntity().getName().toLowerCase());
        assertEquals("Last instance name incorrect", "vm-0",
                qr2.getElements().get(testDataConfig.getExpectedInstanceNbr(FakeConfig.PRIVATE_INDEX) - 1).getEntity()
                        .getName().toLowerCase());

        watch.stop();
        LOG.info("testSortByDescNameAggregation end -->" + watch);
    }


    @Test
    public void testSortAndReboot() throws InterruptedException, JsonProcessingException {
        LOG.info("testSortAndReboot start");
        StopWatch watch = new StopWatch();
        watch.start();
        ThreadDefinition thread = TapestryHandling.findThreadDefinitionWithItemType(tapestryDefinition.getThreads(),
                OsInstanceType.TYPE_LOCAL_ID);
        String threadId = thread.getId();

        QueryResult qr1 = BasicQueryOperations.getThreadWithWait(client, tapestryDefinition.getId(), threadId,
                equalsNumInstancesPrivateProvider);
        assertEquals("Incorrect number of instances", testDataConfig.getExpectedInstanceNbr(FakeConfig.PRIVATE_INDEX),
                qr1.getElements().size());
        assertTrue("fibre should not be a da", !qr1.getElements().get(0).getEntity().isAggregation());

        Map<String, Object> revSortParams = new HashMap<>(1);
        revSortParams.put(QueryOperation.PROPERTY, Fibre.ATTR_LOGICAL_ID);
        revSortParams.put(QueryOperation.ORDER, QueryOperation.DSC_ORDER);
        QueryDefinition revSortQuery = QueryFormatting.createSortByQuery(qr1, revSortParams, testFibres);
        ThreadDefinition threadDefinition = new ThreadDefinition("sorted", thread.getItemType(), revSortQuery);
        // add the newly created thread to the tapestry and update
        tapestryDefinition.addThreadDefinition(threadDefinition);
        client.updateTapestryDefinition(tapestryDefinition.getId(), tapestryDefinition);

        QueryResult qr2 = client.getAggregation(tapestryDefinition.getId(), threadDefinition.getId());

        List<String> initialUuids =
                qr2.getElements().stream().map(e -> e.getEntity().getLogicalId()).collect(Collectors.toList());

        OsInstance vm = (OsInstance) (qr1.getElements().get(0).getEntity());
        String vmId = vm.getCore().getItemId();

        Action stopAction = ActionHandling.createPowerAction("stop");
        stopAction.setTargets(Arrays.asList(qr1.getLogicalId()));
        ActionResult result = client.executeAction(stopAction);

        assertEquals(ActionResult.Status.completed, result.getOverallStatus());
        // assertEquals(ActionResult.Status.pending, result.getOverallStatus());
        Thread.sleep(1000);
        result = client.actionResultStatus(result.getId().toString());
        assertEquals(ActionResult.Status.completed, result.getOverallStatus());

        ActionHandling.checkTargetsPowerStatus(client, "SHUTOFF", tapestryDefinition.getId(), threadId, vmId);

        QueryResult qr3 = client.getAggregation(tapestryDefinition.getId(), threadDefinition.getId());

        List<String> postUuids =
                qr3.getElements().stream().map(e -> e.getEntity().getLogicalId()).collect(Collectors.toList());

        assertTrue(postUuids.size() == initialUuids.size());
        int i = 0;
        String postUuid;
        for (String initUUid : initialUuids) {
            postUuid = postUuids.get(i++);
            LOG.info(initUUid + " : " + postUuid);
            assertEquals(initUUid, postUuid);
        }

        //
        // ObjectMapper mapper = new ObjectMapper();
        // mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        // LOG.info(mapper.writeValueAsString(qr2));

        // // check names come in lexicographic order
        // assertEquals("First instance name incorrect", "vm-9",
        // qr2.getElements().get(0).getEntity().getName().toLowerCase());
        // assertEquals("Last instance name incorrect", "vm-0",
        // qr2.getElements().get(expectedInstanceNbr[FakeConfig.PRIVATE_INDEX] -
        // 1).getEntity().getName().toLowerCase());

        watch.stop();
        LOG.info("testSortAndReboot end -->" + watch);
    }


    @Test
    public void testDetailedEndToEndFlow() throws InterruptedException {
        LOG.info("testDetailedEndToEndFlow start");
        // A client logins and gets a list of pattern as a response.
        Credentials credentials = new Credentials("NotWorking", "NotWorking");
        try {
            client.loginProvider("os", "private", credentials);
            fail("Expecting an error");
        } catch (LoomClientException ex) {
            assertEquals(HttpStatus.UNAUTHORIZED.value(), ex.getStatusCode());
        }

        PatternDefinitionList patternDefinitionList = client.getPatterns();
        List<PatternDefinition> patterns = patternDefinitionList.getPatterns();
        assertNotNull("Null response in getting patterns", patterns);

        ArrayList<String> threadIdRefs = new ArrayList<String>();
        ArrayList<String> itemTypeIdRefs = new ArrayList<String>();
        ArrayList<QueryDefinition> queryRefs = new ArrayList<QueryDefinition>();

        if (patterns.size() > 0) {
            for (PatternDefinition pattern : patterns) {
                assertNotNull("Pattern id is null", pattern.getId());
                assertNotNull("Pattern providerType is null", pattern.getProviderType());
                List<ThreadDefinition> threads = pattern.getThreads();
                assertNotNull("Pattern threads is null", threads);
                assertNotNull("Pattern _meta is null", pattern.get_meta());
                if (threads.size() > 0) {
                    for (ThreadDefinition thread : threads) {
                        assertNotNull("Thread id is null", thread.getId());
                        threadIdRefs.add(thread.getId());
                        assertNotNull("Thread itemType is null", thread.getItemType());
                        itemTypeIdRefs.add(thread.getItemType());
                        assertNotNull("Thread itemType does not exist in pattern _meta -> itemTypes",
                                pattern.get_meta().getItemTypes().get(thread.getItemType()));
                        assertNotNull("Thread query is null", thread.getQuery());
                        assertNotNull("Thread name is null", thread.getName());
                        QueryDefinition query = thread.getQuery();
                        assertNotNull("Query inputs is null", query.getInputs());
                        assertNotNull("Query operationPipeline is null", query.getOperationPipeline());
                        if (query.getOperationPipeline().size() > 0) {
                            LOG.warn(
                                    "operationPipeline can have value, but usually not at this stage, check if this is the case.");
                        }
                        assertTrue("Query must have at least one input string in inputs", query.getInputs().size() > 0);
                        queryRefs.add(thread.getQuery());
                    }
                }
                Meta _meta = pattern.get_meta();
                assertNotNull("Meta itemTypes is null", _meta.getItemTypes());
                for (Map.Entry<String, ItemType> itemTypesEntry : _meta.getItemTypes().entrySet()) {
                    ItemType itemType = itemTypesEntry.getValue();
                    assertNotNull("ItemType id is null", itemType.getId());
                    assertEquals("ItemType id and its key are different", itemTypesEntry.getKey(), itemType.getId());
                    Map<String, Map<String, Object>> attributes = itemType.getAttributes();
                    if (attributes != null) {
                        for (Map.Entry<String, Map<String, Object>> attributesEntry : attributes.entrySet()) {
                            Map<String, Object> attribute = attributesEntry.getValue(); // It is
                                                                                        // impossible
                                                                                        // to
                                                                                        // validate
                            // further when the data
                            // structure is more complex.
                            assertNotNull("An attribute must have a name", attribute.get("name"));
                        }
                    }
                    Map<String, Map<String, Action>> actions = itemType.getActions();
                    if (actions != null) {
                        for (Map.Entry<String, Map<String, Action>> actionType : actions.entrySet()) {
                            assertTrue("Action type must be item or aggregation",
                                    actionType.getKey().equals("item") || actionType.getKey().equals("aggregation"));
                            Map<String, Action> actionTypeMap = actionType.getValue();
                            for (Map.Entry<String, Action> actionMap : actionTypeMap.entrySet()) {
                                Action action = actionMap.getValue();
                                assertNotNull("Action id is null", action.getId());
                                assertNotNull("Action name is null", action.getName());
                                if (action.getParams() != null) {
                                    ActionParameters params = action.getParams();
                                    for (ActionParameter param : params) {
                                        assertNotNull("Param type is null", param.getType());
                                        assertTrue("Type value is not recognised",
                                                isInEnum(param.getType().toString(), ActionParameter.Type.class));
                                        assertNotNull("Param id is null", param.getId());
                                        assertNotNull("Param name is null", param.getName());
                                        assertNotNull("Param range is null", param.getRange());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // A client creates a tapestry.
        for (int i = 0; i < threadIdRefs.size(); i++) {
            String threadId = threadIdRefs.get(i);
            String itemTypeId = itemTypeIdRefs.get(i);
            QueryDefinition query = queryRefs.get(i);

            TapestryDefinition tapestryDefinition = new TapestryDefinition();
            ArrayList<ThreadDefinition> threads = new ArrayList<ThreadDefinition>();
            ThreadDefinition threadDefinition = new ThreadDefinition();
            threadDefinition.setId(null);
            threadDefinition.setItemType(itemTypeId);
            threadDefinition.setQuery(query);
            threads.add(threadDefinition);
            tapestryDefinition.setThreads(threads);

            try {
                client.createTapestryDefinition(tapestryDefinition);
                fail("A thread must contain id");
            } catch (LoomClientException ex) {
                assertEquals(400, ex.getStatusCode());
            }

            threadDefinition.setId(threadId);
            threadDefinition.setItemType(null);

            try {
                client.createTapestryDefinition(tapestryDefinition);
                fail("A thread must contain itemType");
            } catch (LoomClientException ex) {
                assertEquals(400, ex.getStatusCode());
            }

            threadDefinition.setItemType("fakeItemTypeThatDoesNotExist");

            try {
                client.createTapestryDefinition(tapestryDefinition);
                fail("A non-existing itemType must be rejected");
            } catch (LoomClientException ex) {
                assertEquals(400, ex.getStatusCode());
            }

            threadDefinition.setItemType(itemTypeId);
            threadDefinition.setQuery(null);

            try {
                client.createTapestryDefinition(tapestryDefinition);
                fail("A thread must contain query");
            } catch (LoomClientException ex) {
                assertEquals(400, ex.getStatusCode());
            }

            threadDefinition.setQuery(query);
            QueryDefinition emptyQuery = new QueryDefinition();
            threadDefinition.setQuery(emptyQuery);

            try {
                client.createTapestryDefinition(tapestryDefinition);
                fail("A query must not be empty");
            } catch (LoomClientException ex) {
                assertEquals(400, ex.getStatusCode());
            }

            List<String> inputs = query.getInputs();
            assertTrue("Query inputs must not be empty", inputs.size() > 0);
            //
            // List<String> wrongInputs = new ArrayList<String>(1);
            // wrongInputs.add("fakeLogicalIdThatDoesNotExist");
            // query.setInputs(wrongInputs);
            // assertTrue(
            // "Invalid logicalId(s) should throw 400 BAD_REQUEST",
            // Integer.parseInt(client.performWrongly(ApiConfig.TAPESTRY_BASE, HttpMethod.POST,
            // tapestryDefinition, String.class, 400).getStatus()) == 400);
            // query.setInputs(inputs);
            //
        }
        LOG.info("testDetailedEndToEndFlow end");
    }

    @Test
    public void testRelatedToItem() {
        LOG.info("testRelatedToItem start");

        TapestryDefinition tapestryDefinition = TapestryHandling.createFullTapestryFromPatterns(client, testFibres);

        // Find the ID of a specific Instance
        DrillDownThreads drillDownThreads =
                ThreadNavigation.drillDownOnTypeId(client, tapestryDefinition, OsInstanceType.TYPE_LOCAL_ID, 0, true);
        String instancesLogicalId = drillDownThreads.getItemThreadId();
        QueryResult qr = client.getAggregation(tapestryDefinition.getId(), instancesLogicalId);
        QueryResultElement qre = qr.getElements().get(0);
        String logicalId = qre.getEntity().getLogicalId();

        // Now find all images related to the Instance
        StopWatch watch = new StopWatch();
        watch.start();
        String imageTypeId = "os-" + OsImageType.TYPE_LOCAL_ID;
        QueryResult queryResult =
                ThreadNavigation.filterOnRelatedToId(client, tapestryDefinition, imageTypeId, logicalId);
        assertEquals("Invalid result item type returned from filter", imageTypeId, queryResult.getItemType().getId());
        assertEquals("Invalid number of related items", 1, queryResult.getElements().size());
        Fibre fibre = queryResult.getElements().get(0).getEntity();
        assertTrue("Returned fibre was not an item", fibre.isItem());
        Item item = (Item) fibre;
        assertEquals("Invalid item type returned from filter", imageTypeId, item.getTypeId());
        watch.stop();
        LOG.info("testRelatedToItem end");
    }



    // need to test getPatterns explicitly that is not at login too. And get pattern by id.

    public <T extends Enum<T>> boolean isInEnum(final String value, final Class<T> tClass) {
        for (T t : tClass.getEnumConstants()) {
            if (t.name().equals(value.toUpperCase())) {
                return true;
            }
        }
        return false;
    }
}
