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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hp.hpl.loom.adapter.UpdateResult;
import com.hp.hpl.loom.adapter.os.OsFlavour;
import com.hp.hpl.loom.adapter.os.OsInstance;
import com.hp.hpl.loom.adapter.os.OsInstanceAttributes;
import com.hp.hpl.loom.adapter.os.OsInstanceType;
import com.hp.hpl.loom.adapter.os.OsVolume;
import com.hp.hpl.loom.adapter.os.OsVolumeAttributes;
import com.hp.hpl.loom.adapter.os.OsVolumeType;
import com.hp.hpl.loom.api.util.SessionManager;
import com.hp.hpl.loom.exceptions.InvalidQueryInputException;
import com.hp.hpl.loom.exceptions.InvalidQueryParametersException;
import com.hp.hpl.loom.exceptions.ItemPropertyNotFound;
import com.hp.hpl.loom.exceptions.LogicalIdAlreadyExistsException;
import com.hp.hpl.loom.exceptions.NoSuchAggregationException;
import com.hp.hpl.loom.exceptions.NoSuchQueryDefinitionException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.NoSuchTapestryDefinitionException;
import com.hp.hpl.loom.exceptions.NoSuchThreadDefinitionException;
import com.hp.hpl.loom.exceptions.OperationException;
import com.hp.hpl.loom.exceptions.OperationNotSupportedException;
import com.hp.hpl.loom.exceptions.PendingQueryResultsException;
import com.hp.hpl.loom.exceptions.RelationPropertyNotFound;
import com.hp.hpl.loom.exceptions.SessionAlreadyExistsException;
import com.hp.hpl.loom.exceptions.ThreadDeletedByDynAdapterUnload;
import com.hp.hpl.loom.manager.aggregation.AggregationManager;
import com.hp.hpl.loom.manager.itemtype.ItemTypeManager;
import com.hp.hpl.loom.manager.query.executor.QueryExecutor;
import com.hp.hpl.loom.manager.stitcher.Tacker;
import com.hp.hpl.loom.manager.tapestry.TapestryManager;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.ProviderImpl;
import com.hp.hpl.loom.model.QueryResult;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.model.SessionImpl;
import com.hp.hpl.loom.relationships.RelationshipUtil;
import com.hp.hpl.loom.tapestry.Operation;
import com.hp.hpl.loom.tapestry.QueryDefinition;
import com.hp.hpl.loom.tapestry.TapestryDefinition;
import com.hp.hpl.loom.tapestry.ThreadDefinition;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext-testQueryManager.xml")
public class QueryExecutorImplTest {

    private static final Log LOG = LogFactory.getLog(QueryExecutorImplTest.class);

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    QueryExecutor queryExec;

    @Autowired
    AggregationManager aggregationManager;

    @Autowired
    Tacker stitcher;

    @Autowired
    TapestryManager tapestryManager;

    @Autowired
    ItemTypeManager itemTypeManager;

    ItemType itemType = null;

    Session session;
    List<ThreadDefinition> identityThreads, defaultIdentityThreads, groupThreads, groupAggregationThreads,
            braidOneThreads, braidThreads, identBraidThreads, groupBraidThreads, percentThreads, sortThreads,
            badSortThreads, revSortThreads, sortGroupBraidThreads, braidThreads21, bucketThreads, bucketBraidThreads,
            packThreads, distributeThreads, customThreads;
    TapestryDefinition tap;
    QueryDefinition identityQuery, defaultIdentityQuery, groupQuery, groupAggregationQuery, braidQuery, braidOneQuery,
            identBraidQuery, groupBraidQuery, sortQuery, badSortQuery, revSortQuery, sortGroupBraidQuery, braidQuery21,
            bucketQuery, bucketBraidQuery, percentQuery, packQuery, distributeQuery, customQuery;

    Aggregation groundedAggregation;
    List<String> sources;

    List<Operation> identityPipe, groupPipe, groupAggregationPipe, braidOnePipe, braidPipe, identBraidPipe,
            groupBraidPipe, percentPipe, sortPipe, badSortPipe, revSortPipe, sortGroupBraidPipe, braidPipe21,
            bucketPipe, bucketBraidPipe, packPipe, distributePipe, customPipe;

    Provider provider = null;

    @Before
    public void setUp() throws Exception {
        LOG.info("Setup test");
        session = new SessionImpl(UUID.randomUUID().toString(), sessionManager.getInterval());

        provider = new ProviderImpl("os", "providerId", "http://whatever", "providerName", "com");
        itemType = new OsInstanceType(provider);

        itemType.setId(itemType.getLocalId());
        if (itemTypeManager.getItemType(itemType.getId()) == null) {
            itemTypeManager.addItemType(provider, itemType);
        }

        packThreads = new ArrayList<ThreadDefinition>(1);
        String in1 = "/providers/openstack/test/testGroundedAggregation";
        Map<String, Object> packParams1 = new HashMap<>(0);
        sources = new ArrayList<String>(1);
        sources.add(in1);
        Operation packOperation = new Operation(DefaultOperations.SUMMARY.toString(), packParams1);
        packPipe = new ArrayList<>(1);
        packPipe.add(packOperation);
        packQuery = new QueryDefinition(packPipe, sources);
        ThreadDefinition packThreadDefinition = new ThreadDefinition("0", "/os/instances", packQuery);
        packThreadDefinition.setItemType(itemType.getId());
        packThreads.add(packThreadDefinition);

        // /////////////////////////
        // Build Identity Op
        // /////////////////////////
        identityThreads = new ArrayList<ThreadDefinition>(1);

        Map<String, Object> identityParams1 = new HashMap<>(0);
        sources = new ArrayList<String>(1);
        sources.add(in1);
        Operation identityOperation = new Operation(DefaultOperations.IDENTITY.toString(), identityParams1);
        identityPipe = new ArrayList<>(1);
        identityPipe.add(identityOperation);
        identityQuery = new QueryDefinition(identityPipe, sources);
        ThreadDefinition identityThreadDefinition = new ThreadDefinition("0", "/os/instances", identityQuery);
        identityThreadDefinition.setItemType(itemType.getId());
        identityThreads.add(identityThreadDefinition);

        // /////////////////////////
        // Build Default Identity Op
        // /////////////////////////
        defaultIdentityThreads = new ArrayList<ThreadDefinition>(1);
        defaultIdentityQuery = new QueryDefinition();
        defaultIdentityQuery.setInputs(sources);
        ThreadDefinition defaultIdentityThreadDefinition = new ThreadDefinition("0", "/os/instances", identityQuery);
        defaultIdentityThreadDefinition.setItemType(itemType.getId());
        defaultIdentityThreads.add(defaultIdentityThreadDefinition);
        defaultIdentityThreads.add(defaultIdentityThreadDefinition);

        // /////////////////////////
        // Build Group Op
        // /////////////////////////
        groupThreads = new ArrayList<ThreadDefinition>(1);
        Map<String, Object> groupParams = new HashMap<>(1);
        groupParams.put(QueryOperation.PROPERTY, OsInstanceType.ATTR_FLAVOR);
        Operation groupOperation = new Operation((DefaultOperations.GROUP_BY.toString()), groupParams);
        groupPipe = new ArrayList<>(1);
        groupPipe.add(groupOperation);
        groupQuery = new QueryDefinition(groupPipe, sources);
        ThreadDefinition groupThreadDefinition = new ThreadDefinition("0", "/os/instances", groupQuery);
        groupThreadDefinition.setItemType(itemType.getId());
        groupThreads.add(groupThreadDefinition);


        // /////////////////////////
        // Build BRAID Op
        // /////////////////////////
        braidThreads = new ArrayList<ThreadDefinition>(1);
        Map<String, Object> braidParams = new HashMap<>(1);
        braidParams.put(QueryOperation.MAX_FIBRES, 2);
        Operation braidOperation = new Operation((DefaultOperations.BRAID.toString()), braidParams);
        braidPipe = new ArrayList<>(1);
        braidPipe.add(braidOperation);
        braidQuery = new QueryDefinition(braidPipe, sources);
        ThreadDefinition braidThreadDefinition = new ThreadDefinition("0", "/os/instances", braidQuery);
        braidThreadDefinition.setItemType(itemType.getId());
        braidThreads.add(braidThreadDefinition);


        distributeThreads = new ArrayList<ThreadDefinition>(1);
        Map<String, Object> distributeParams = new HashMap<>(1);
        distributeParams.put(QueryOperation.BUCKETS, 5);
        distributeParams.put(QueryOperation.PROPERTY, "core.vcpus");
        Operation distributeOperation = new Operation((DefaultOperations.DISTRIBUTE.toString()), distributeParams);
        distributePipe = new ArrayList<>(1);
        distributePipe.add(distributeOperation);
        distributeQuery = new QueryDefinition(distributePipe, sources);
        ThreadDefinition distributeThreadDefinition = new ThreadDefinition("0", "/os/instances", distributeQuery);
        distributeThreadDefinition.setItemType(itemType.getId());
        distributeThreads.add(distributeThreadDefinition);


        braidOneThreads = new ArrayList<ThreadDefinition>(1);
        Map<String, Object> braidOneParams = new HashMap<>(1);
        braidOneParams.put(QueryOperation.MAX_FIBRES, 1);
        Operation braidOneOperation = new Operation((DefaultOperations.SUMMARY.toString()), braidOneParams);
        braidOnePipe = new ArrayList<>(1);
        braidOnePipe.add(braidOneOperation);
        braidOneQuery = new QueryDefinition(braidOnePipe, sources);
        ThreadDefinition braidOneThreadDefinition = new ThreadDefinition("0", "/os/instances", braidOneQuery);
        braidOneThreadDefinition.setItemType(itemType.getId());
        braidOneThreads.add(braidOneThreadDefinition);



        // /////////////////////////
        // Build BRAID Op
        // /////////////////////////
        braidThreads21 = new ArrayList<ThreadDefinition>(1);
        Map<String, Object> braidParams21 = new HashMap<>(1);
        braidParams21.put(QueryOperation.MAX_FIBRES, 21);
        Operation braidOperation21 = new Operation((DefaultOperations.BRAID.toString()), braidParams21);
        braidPipe21 = new ArrayList<>(1);
        braidPipe21.add(braidOperation21);
        braidQuery21 = new QueryDefinition(braidPipe21, sources);
        ThreadDefinition braidThreadDefinition21 = new ThreadDefinition("0", "/os/instances", braidQuery21);
        braidThreadDefinition21.setItemType(itemType.getId());
        braidThreads21.add(braidThreadDefinition21);

        // ///////////////////////////
        // Build IDENTITY - BRAID Op
        // ///////////////////////////
        identBraidThreads = new ArrayList<ThreadDefinition>(1);
        identBraidPipe = new ArrayList<Operation>(2);
        identBraidPipe.add(identityOperation);
        identBraidPipe.add(braidOperation);
        identBraidQuery = new QueryDefinition(identBraidPipe, sources);
        ThreadDefinition identBraidThreadDefinition = new ThreadDefinition("0", "/os/instances", identBraidQuery);
        identBraidThreadDefinition.setItemType(itemType.getId());
        identBraidThreads.add(identBraidThreadDefinition);

        // ///////////////////////////
        // Build GROUP - BRAID Op
        // ///////////////////////////
        groupBraidThreads = new ArrayList<ThreadDefinition>(1);
        groupBraidPipe = new ArrayList<Operation>(2);
        groupBraidPipe.add(groupOperation);

        Map<String, Object> braidParams2 = new HashMap<>(1);
        braidParams2.put(QueryOperation.MAX_FIBRES, 2);
        Operation braidOperation2 = new Operation((DefaultOperations.BRAID.toString()), braidParams2);
        groupBraidPipe.add(braidOperation2);
        groupBraidQuery = new QueryDefinition(groupBraidPipe, sources);
        ThreadDefinition groupBraidThreadDefinition = new ThreadDefinition("0", "/os/instances", groupBraidQuery);
        groupBraidThreadDefinition.setItemType(itemType.getId());
        groupBraidThreads.add(groupBraidThreadDefinition);

        // /////////////////////////////
        // Reverse Sorting
        // /////////////////////////////
        revSortThreads = new ArrayList<ThreadDefinition>(1);
        Map<String, Object> revSortParams = new HashMap<>(1);
        revSortParams.put(QueryOperation.PROPERTY, OsInstanceType.ATTR_FLAVOR);
        revSortParams.put(QueryOperation.ORDER, QueryOperation.DSC_ORDER);
        Operation revSortOperation = new Operation((DefaultOperations.SORT_BY.toString()), revSortParams);
        revSortPipe = new ArrayList<>(1);
        revSortPipe.add(revSortOperation);
        revSortQuery = new QueryDefinition(revSortPipe, sources);
        ThreadDefinition revSortThreadDefinition = new ThreadDefinition("0", "/os/instances", revSortQuery);
        revSortThreadDefinition.setItemType(itemType.getId());
        revSortThreads.add(revSortThreadDefinition);

        // /////////////////////////////
        // Sorting
        // /////////////////////////////
        sortThreads = new ArrayList<ThreadDefinition>(1);
        Map<String, Object> sortParams = new HashMap<>(1);
        sortParams.put(QueryOperation.PROPERTY, OsInstanceType.ATTR_FLAVOR);
        sortParams.put(QueryOperation.ORDER, QueryOperation.ASC_ORDER);
        Operation sortOperation = new Operation((DefaultOperations.SORT_BY.toString()), sortParams);
        sortPipe = new ArrayList<>(1);
        sortPipe.add(sortOperation);
        sortQuery = new QueryDefinition(sortPipe, sources);
        ThreadDefinition sortThreadDefinition = new ThreadDefinition("0", "/os/instances", sortQuery);
        sortThreadDefinition.setItemType(itemType.getId());
        sortThreads.add(sortThreadDefinition);

        // /////////////////////////////
        // Custom
        // /////////////////////////////
        customThreads = new ArrayList<ThreadDefinition>(1);
        Operation customOperation = new Operation("custom1", new HashMap<>(0));
        customPipe = new ArrayList<>(1);
        customPipe.add(customOperation);
        customQuery = new QueryDefinition(customPipe, sources);
        ThreadDefinition customThreadDefinition = new ThreadDefinition("0", "/os/instances", customQuery);
        customThreadDefinition.setItemType(itemType.getId());
        customThreads.add(customThreadDefinition);

        // /////////////////////////////
        // Percentile
        // /////////////////////////////
        percentThreads = new ArrayList<ThreadDefinition>(1);
        Map<String, Object> percentParams = new HashMap<>(1);
        percentParams.put(QueryOperation.PROPERTY, "core.vcpus");

        Operation percentOperation = new Operation((DefaultOperations.PERCENTILES.toString()), percentParams);
        percentPipe = new ArrayList<>(1);
        percentPipe.add(percentOperation);
        percentQuery = new QueryDefinition(percentPipe, sources);
        ThreadDefinition percentThreadDefinition = new ThreadDefinition("0", "/os/instances", percentQuery);
        percentThreadDefinition.setItemType(itemType.getId());
        percentThreads.add(percentThreadDefinition);


        //
        // BAD SORT
        //
        badSortThreads = new ArrayList<ThreadDefinition>(1);
        Map<String, Object> badSortParams = new HashMap<>(2);
        badSortParams.put(QueryOperation.PROPERTY, "WRONG");
        badSortParams.put(QueryOperation.ORDER, QueryOperation.ASC_ORDER);
        Operation badSortOperation = new Operation((DefaultOperations.SORT_BY.toString()), badSortParams);
        badSortPipe = new ArrayList<>(1);
        badSortPipe.add(badSortOperation);
        badSortQuery = new QueryDefinition(badSortPipe, sources);
        ThreadDefinition badSortThreadDefinition = new ThreadDefinition("0", "/os/instances", badSortQuery);
        badSortThreadDefinition.setItemType(itemType.getId());
        badSortThreads.add(badSortThreadDefinition);

        // /////////////////////////
        // group - sort - braid
        // /////////////////////////
        sortGroupBraidThreads = new ArrayList<ThreadDefinition>(1);
        sortGroupBraidPipe = new ArrayList<Operation>(2);
        sortGroupBraidPipe.add(sortOperation);
        sortGroupBraidPipe.add(groupOperation);
        sortGroupBraidPipe.add(braidOperation);
        sortGroupBraidQuery = new QueryDefinition(sortGroupBraidPipe, sources);
        ThreadDefinition groupSortBraidThreadDefinition =
                new ThreadDefinition("0", "/os/instances", sortGroupBraidQuery);
        groupSortBraidThreadDefinition.setItemType(itemType.getId());
        sortGroupBraidThreads.add(groupSortBraidThreadDefinition);



        // /////////////////////////
        // stub aggregManager
        // /////////////////////////
        aggregationManager.createSession(session);
        stitcher.createSession(session);
        groundedAggregation = createGroundedAggregation(session);
        ArrayList<Item> newItems = createItems(20, 0, groundedAggregation.getTypeId());
        UpdateResult updateResult = new UpdateResult(newItems, newItems, null, null);
        aggregationManager.updateGroundedAggregation(session, groundedAggregation, updateResult);

        bucketThreads = new ArrayList<ThreadDefinition>(1);
        Map<String, Object> bucketParams = new HashMap<>(1);
        bucketParams.put(QueryOperation.PROPERTY, RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                provider.getProviderType(), OsInstanceType.TYPE_LOCAL_ID, OsVolumeType.TYPE_LOCAL_ID));
        Operation bucketOperation = new Operation((DefaultOperations.BUCKETIZE.toString()), bucketParams);
        bucketPipe = new ArrayList<>(1);
        bucketPipe.add(bucketOperation);
        bucketQuery = new QueryDefinition(bucketPipe, sources);
        ThreadDefinition bucketThreadDefinition = new ThreadDefinition("0", "/os/instances", bucketQuery);
        bucketThreadDefinition.setItemType(itemType.getId());
        bucketThreads.add(bucketThreadDefinition);

        bucketBraidThreads = new ArrayList<ThreadDefinition>(1);
        bucketBraidPipe = new ArrayList<>(1);
        bucketBraidPipe.add(bucketOperation);
        bucketBraidPipe.add(braidOperation);
        bucketBraidQuery = new QueryDefinition(bucketBraidPipe, sources);
        ThreadDefinition bucketBraidThreadDefinition = new ThreadDefinition("0", "/os/instances", bucketBraidQuery);
        bucketBraidThreadDefinition.setItemType(itemType.getId());
        bucketBraidThreads.add(bucketBraidThreadDefinition);


        LOG.info("Setup test done.");
    }

    @Test
    public void testIsSupportedOperation() {

        assertFalse(queryExec.isSupportedOperation("WRONG"));

        assertTrue(queryExec.isSupportedOperation(DefaultOperations.BRAID.toString()));
    }

    @Test
    public void testBucketBraidOperations() throws ItemPropertyNotFound, LogicalIdAlreadyExistsException,
            NoSuchQueryDefinitionException, OperationException, NoSuchTapestryDefinitionException,
            NoSuchAggregationException, NoSuchThreadDefinitionException, InvalidQueryParametersException,
            NoSuchSessionException, InvalidQueryInputException, JsonProcessingException, RelationPropertyNotFound,
            UnsupportedOperationException, IllegalArgumentException, ThreadDeletedByDynAdapterUnload {
        tap = new TapestryDefinition();
        tap.setThreads(bucketBraidThreads);
        tapestryManager.setTapestryDefinition(session, tap);

        StopWatch watch = new StopWatch();
        LOG.info("testing Bucket-braid Query process");
        watch.start();
        QueryResult results = queryExec.processQuery(session, tap.getThreads().get(0));
        assertEquals("unexpected size", results.getElements().size(), 2);

        QueryResult res2 = queryExec.processQuery(session, tap.getThreads().get(0),
                tap.getThreads().get(0).getQuery().hashCode() + "/0");
        assertEquals("unexpected size", res2.getElements().size(), 3);

        watch.stop();
        LOG.info("tested Bucket-braid Query process --> " + watch);
    }


    @Test
    public void testBucketOperations() throws LogicalIdAlreadyExistsException, ItemPropertyNotFound,
            NoSuchQueryDefinitionException, OperationException, NoSuchTapestryDefinitionException,
            NoSuchAggregationException, NoSuchThreadDefinitionException, InvalidQueryParametersException,
            NoSuchSessionException, InvalidQueryInputException, JsonProcessingException, RelationPropertyNotFound,
            UnsupportedOperationException, IllegalArgumentException, ThreadDeletedByDynAdapterUnload {
        tap = new TapestryDefinition();
        tap.setThreads(bucketThreads);
        tapestryManager.setTapestryDefinition(session, tap);

        StopWatch watch = new StopWatch();
        LOG.info("testing Bucket Query process");
        watch.start();
        QueryResult results = queryExec.processQuery(session, tap.getThreads().get(0));
        assertEquals("unexpected size", 5, results.getElements().size());
        watch.stop();
        LOG.info("tested Bucket Query process --> " + watch);
    }

    @Test
    public void testProcessPackQuery()
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, IllegalAccessException,
            UnsupportedOperationException, OperationException, NoSuchAggregationException, InvalidQueryInputException,
            LogicalIdAlreadyExistsException, NoSuchQueryDefinitionException, NoSuchThreadDefinitionException,
            OperationNotSupportedException, InvalidQueryParametersException, PendingQueryResultsException,
            ItemPropertyNotFound, RelationPropertyNotFound, IllegalArgumentException, ThreadDeletedByDynAdapterUnload {
        tap = new TapestryDefinition();
        tap.setThreads(packThreads);
        tapestryManager.setTapestryDefinition(session, tap);

        StopWatch watch = new StopWatch();
        LOG.info("testing Identity Query process");
        watch.start();
        QueryResult results = queryExec.processQuery(session, tap.getThreads().get(0));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        try {
            System.out.println(mapper.writeValueAsString(results));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        assertTrue(results.getLogicalId().equals("/da/" + packQuery.hashCode()));
        assertTrue(results.getElements().size() == 1);

        watch.stop();
        LOG.info("tested Identity Query process --> " + watch);
    }

    @Test
    public void testProcessIdentityQuery()
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, IllegalAccessException,
            UnsupportedOperationException, OperationException, NoSuchAggregationException, InvalidQueryInputException,
            LogicalIdAlreadyExistsException, NoSuchQueryDefinitionException, NoSuchThreadDefinitionException,
            OperationNotSupportedException, InvalidQueryParametersException, PendingQueryResultsException,
            ItemPropertyNotFound, RelationPropertyNotFound, IllegalArgumentException, ThreadDeletedByDynAdapterUnload {
        tap = new TapestryDefinition();
        tap.setThreads(identityThreads);
        tapestryManager.setTapestryDefinition(session, tap);

        StopWatch watch = new StopWatch();
        LOG.info("testing Identity Query process");
        watch.start();
        QueryResult results = queryExec.processQuery(session, tap.getThreads().get(0));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        // try {
        // System.out.println(mapper.writeValueAsString(results));
        // } catch (JsonProcessingException e) {
        // e.printStackTrace();
        // }
        assertTrue(results.getLogicalId().equals("/da/" + identityQuery.hashCode()));
        assertTrue(results.getElements().size() == 20);

        watch.stop();
        LOG.info("tested Identity Query process --> " + watch);
    }

    @Test
    public void testProcessDefaultIdentityQuery()
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, IllegalAccessException,
            UnsupportedOperationException, OperationException, NoSuchAggregationException, InvalidQueryInputException,
            LogicalIdAlreadyExistsException, NoSuchQueryDefinitionException, NoSuchThreadDefinitionException,
            OperationNotSupportedException, InvalidQueryParametersException, PendingQueryResultsException,
            ItemPropertyNotFound, RelationPropertyNotFound, IllegalArgumentException, ThreadDeletedByDynAdapterUnload {
        tap = new TapestryDefinition();
        tap.setThreads(defaultIdentityThreads);
        tapestryManager.setTapestryDefinition(session, tap);

        StopWatch watch = new StopWatch();
        LOG.info("testing Identity Query process");
        watch.start();
        QueryResult results = queryExec.processQuery(session, defaultIdentityThreads.get(0));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        try {
            System.out.println(mapper.writeValueAsString(results));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        assertTrue(results.getElements().size() == 20);

        watch.stop();
        LOG.info("tested Identity Query process --> " + watch);
    }

    @Test
    public void testGetSameQuery()
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, IllegalAccessException,
            UnsupportedOperationException, OperationException, NoSuchAggregationException, InvalidQueryInputException,
            LogicalIdAlreadyExistsException, NoSuchQueryDefinitionException, NoSuchThreadDefinitionException,
            OperationNotSupportedException, InvalidQueryParametersException, PendingQueryResultsException,
            ItemPropertyNotFound, RelationPropertyNotFound, IllegalArgumentException, ThreadDeletedByDynAdapterUnload {

        tap = new TapestryDefinition();
        tap.setThreads(identityThreads);
        tapestryManager.setTapestryDefinition(session, tap);

        StopWatch watch = new StopWatch();
        LOG.info("testing Get Same Query process");
        watch.start();
        QueryResult results = queryExec.processQuery(session, tap.getThreads().get(0));
        QueryResult sameResults = queryExec.processQuery(session, tap.getThreads().get(0));

        assertTrue(results.getLogicalId().equals(sameResults.getLogicalId()));
        assertTrue(results.getElements().size() == sameResults.getElements().size());

        watch.stop();
        LOG.info("tested Get Same Query process --> " + watch);
    }

    @Test
    public void testProcessGroupByQuery()
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, IllegalAccessException,
            UnsupportedOperationException, OperationException, NoSuchAggregationException, InvalidQueryInputException,
            LogicalIdAlreadyExistsException, NoSuchQueryDefinitionException, NoSuchThreadDefinitionException,
            OperationNotSupportedException, InvalidQueryParametersException, PendingQueryResultsException,
            JsonProcessingException, ItemPropertyNotFound, RelationPropertyNotFound, ThreadDeletedByDynAdapterUnload {

        tap = new TapestryDefinition();
        tap.setThreads(groupThreads);

        tapestryManager.setTapestryDefinition(session, tap);

        StopWatch watch = new StopWatch();
        LOG.info("testing Query process");
        watch.start();

        QueryResult results = queryExec.processQuery(session, tap.getThreads().get(0));

        // ObjectMapper mapper = new ObjectMapper();
        // mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        // System.out.println(mapper.writeValueAsString(results));

        assertTrue(results.getLogicalId().equals("/da/" + groupQuery.hashCode()));
        assertTrue(results.getElements().size() == 4);

        watch.stop();
        LOG.info("tested Group By Query process --> " + watch);
    }


    @Test
    public void testProcessBraidOneQuery() throws NoSuchSessionException, NoSuchTapestryDefinitionException,
            IllegalAccessException, UnsupportedOperationException, OperationException, NoSuchAggregationException,
            InvalidQueryInputException, LogicalIdAlreadyExistsException, NoSuchQueryDefinitionException,
            NoSuchThreadDefinitionException, OperationNotSupportedException, InvalidQueryParametersException,
            JsonProcessingException, PendingQueryResultsException, ItemPropertyNotFound, RelationPropertyNotFound,
            IllegalArgumentException, ThreadDeletedByDynAdapterUnload {

        tap = new TapestryDefinition();
        tap.setThreads(braidOneThreads);
        tapestryManager.setTapestryDefinition(session, tap);

        StopWatch watch = new StopWatch();
        LOG.info("testing Braid by One process");
        watch.start();
        QueryResult results = queryExec.processQuery(session, tap.getThreads().get(0));
        //
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        System.out.println(mapper.writeValueAsString(results));

        assertEquals(results.getElements().size(), 1);
        QueryResult results2 =
                queryExec.processQuery(session, tap.getThreads().get(0), braidOneQuery.hashCode() + "/id/0");

        assertTrue(results2.getElements().size() == 20);


        watch.stop();
        LOG.info("tested Braid One Query process --> " + watch);
    }


    @Test
    public void testDistributeQuery()
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, IllegalAccessException,
            UnsupportedOperationException, OperationException, NoSuchAggregationException, InvalidQueryInputException,
            LogicalIdAlreadyExistsException, NoSuchQueryDefinitionException, NoSuchThreadDefinitionException,
            InvalidQueryParametersException, JsonProcessingException, PendingQueryResultsException,
            ItemPropertyNotFound, RelationPropertyNotFound, IllegalArgumentException, ThreadDeletedByDynAdapterUnload {

        tap = new TapestryDefinition();
        tap.setThreads(distributeThreads);
        tapestryManager.setTapestryDefinition(session, tap);

        StopWatch watch = new StopWatch();
        LOG.info("testing Distributing process");
        watch.start();
        QueryResult results = queryExec.processQuery(session, tap.getThreads().get(0));
        //
        // ObjectMapper mapper = new ObjectMapper();
        // mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        // System.out.println(mapper.writeValueAsString(results));

        assertTrue(results.getLogicalId().equals("/da/" + distributeQuery.hashCode()));
        assertTrue(results.getElements().size() == 5);

        watch.stop();
        LOG.info("tested Distributing process --> " + watch);
    }


    @Test
    public void testProcessBraidQuery() throws NoSuchSessionException, NoSuchTapestryDefinitionException,
            IllegalAccessException, UnsupportedOperationException, OperationException, NoSuchAggregationException,
            InvalidQueryInputException, LogicalIdAlreadyExistsException, NoSuchQueryDefinitionException,
            NoSuchThreadDefinitionException, OperationNotSupportedException, InvalidQueryParametersException,
            JsonProcessingException, PendingQueryResultsException, ItemPropertyNotFound, RelationPropertyNotFound,
            IllegalArgumentException, ThreadDeletedByDynAdapterUnload {

        tap = new TapestryDefinition();
        tap.setThreads(braidThreads);
        tapestryManager.setTapestryDefinition(session, tap);

        StopWatch watch = new StopWatch();
        LOG.info("testing Braid process");
        watch.start();
        QueryResult results = queryExec.processQuery(session, tap.getThreads().get(0));

        assertTrue(results.getLogicalId().equals("/da/" + braidQuery.hashCode()));
        assertTrue(results.getElements().size() == 2);
        assertTrue(results.getElements().get(0).getEntity().getTags().equals("/loom/loom/BRAID"));

        QueryResult results2 = queryExec.processQuery(session, tap.getThreads().get(0), braidQuery.hashCode() + "/1");
        // ObjectMapper mapper = new ObjectMapper();
        // mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        // System.out.println(mapper.writeValueAsString(results2));

        assertTrue(results2.getLogicalId().equals("/da/" + braidQuery.hashCode() + "/1"));
        assertTrue(results2.getElements().size() == 10);
        assertTrue(results2.getElements().get(0).getEntity().getTags().isEmpty());
        watch.stop();
        LOG.info("tested Braid Query process --> " + watch);


    }

    @Test
    public void testBraidIdentityPipeQueries()
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, IllegalAccessException,
            UnsupportedOperationException, OperationException, NoSuchAggregationException, InvalidQueryInputException,
            LogicalIdAlreadyExistsException, NoSuchQueryDefinitionException, NoSuchThreadDefinitionException,
            OperationNotSupportedException, InvalidQueryParametersException, PendingQueryResultsException,
            ItemPropertyNotFound, RelationPropertyNotFound, IllegalArgumentException, ThreadDeletedByDynAdapterUnload {

        tap = new TapestryDefinition();
        tap.setThreads(identBraidThreads);
        tapestryManager.setTapestryDefinition(session, tap);

        StopWatch watch = new StopWatch();
        LOG.info("testing Piping identity and braid ops process");
        watch.start();
        QueryResult results = queryExec.processQuery(session, tap.getThreads().get(0));
        //
        // ObjectMapper mapper = new ObjectMapper();
        // mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        // System.out.println(mapper.writeValueAsString(results));
        assertTrue(results.getLogicalId().equals("/da/" + identBraidQuery.hashCode()));
        assertTrue(results.getElements().size() == 2);
        // validate is braid tagged
        assertTrue(results.getElements().get(0).getEntity().getTags().equals("/loom/loom/BRAID"));

        QueryResult results2 =
                queryExec.processQuery(session, tap.getThreads().get(0), identBraidQuery.hashCode() + "/1");
        // System.out.println(mapper.writeValueAsString(results2));
        assertTrue(results2.getLogicalId().equals("/da/" + identBraidQuery.hashCode() + "/1"));
        assertTrue(results2.getElements().size() == 10);
        assertTrue(results2.getElements().get(0).getEntity().getTags().isEmpty());

        watch.stop();
        LOG.info("tested Piping identity and braid ops process --> " + watch);
    }

    @Test
    public void test2GroupingPipedQueries() throws NoSuchSessionException, NoSuchTapestryDefinitionException,

            IllegalAccessException, UnsupportedOperationException, OperationException, NoSuchAggregationException,
            InvalidQueryInputException, LogicalIdAlreadyExistsException, NoSuchQueryDefinitionException,
            NoSuchThreadDefinitionException, OperationNotSupportedException, InvalidQueryParametersException,
            PendingQueryResultsException, JsonProcessingException, ItemPropertyNotFound, RelationPropertyNotFound,
            IllegalArgumentException, ThreadDeletedByDynAdapterUnload {


        tap = new TapestryDefinition();
        tap.setThreads(groupBraidThreads);
        tapestryManager.setTapestryDefinition(session, tap);

        StopWatch watch = new StopWatch();
        LOG.info("testing Piping with 2 grouping ops process");
        watch.start();
        QueryResult results = queryExec.processQuery(session, tap.getThreads().get(0));

        assertTrue(results.getLogicalId().equals("/da/" + groupBraidQuery.hashCode()));
        assertTrue(results.getElements().size() == 2);
        assertTrue(results.getElements().get(0).getEntity().getTags().equals("/loom/loom/BRAID"));

        QueryResult results2 =
                queryExec.processQuery(session, tap.getThreads().get(0), groupBraidQuery.hashCode() + "/1");

        assertTrue(results2.getLogicalId().equals("/da/" + groupBraidQuery.hashCode() + "/1"));
        assertTrue(results2.getElements().size() == 2);
        assertTrue(results2.getElements().get(0).getEntity().getTags().equals("/loom/loom/GROUP_BY"));

        QueryResult results3 = queryExec.processQuery(session, tap.getThreads().get(0),
                groupBraidQuery.hashCode() + "/group_by/id/small2");

        assertTrue(results3.getLogicalId().equals("/da/" + groupBraidQuery.hashCode() + "/group_by/id/small2"));
        assertTrue(results3.getElements().size() == 2);
        // TODO don't understand
        // assertTrue(results3.getElements().get(0).getEntity().getTags().isEmpty());
        watch.stop();
        LOG.info("tested Piping with 2 grouping ops process --> " + watch);
    }


    @Test
    public void testBadSorting() throws NoSuchSessionException, NoSuchTapestryDefinitionException,
            LogicalIdAlreadyExistsException, NoSuchAggregationException, InvalidQueryInputException,
            IllegalAccessException, OperationException, UnsupportedOperationException, NoSuchQueryDefinitionException,
            NoSuchThreadDefinitionException, OperationNotSupportedException, InvalidQueryParametersException,
            JsonProcessingException, PendingQueryResultsException, ItemPropertyNotFound, RelationPropertyNotFound,
            IllegalArgumentException, ThreadDeletedByDynAdapterUnload {

        tap = new TapestryDefinition();
        tap.setThreads(badSortThreads);
        tapestryManager.setTapestryDefinition(session, tap);

        LOG.info("testing Bad Sorting ops process");
        queryExec.processQuery(session, tap.getThreads().get(0));

        LOG.info("tested Bad Sorting process");
    }



    @Test
    public void testSorting() throws NoSuchSessionException, NoSuchTapestryDefinitionException,
            LogicalIdAlreadyExistsException, NoSuchAggregationException, InvalidQueryInputException,
            IllegalAccessException, OperationException, UnsupportedOperationException, NoSuchQueryDefinitionException,
            NoSuchThreadDefinitionException, OperationNotSupportedException, InvalidQueryParametersException,
            JsonProcessingException, PendingQueryResultsException, ItemPropertyNotFound, RelationPropertyNotFound,
            IllegalArgumentException, ThreadDeletedByDynAdapterUnload {

        tap = new TapestryDefinition();
        tap.setThreads(sortThreads);
        tapestryManager.setTapestryDefinition(session, tap);

        StopWatch watch = new StopWatch();
        LOG.info("testing Reverse Sorting ops process");
        watch.start();
        QueryResult results = queryExec.processQuery(session, tap.getThreads().get(0));

        // ObjectMapper mapper = new ObjectMapper();
        // mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        // System.out.println(mapper.writeValueAsString(results));

        OsInstance i1 = (OsInstance) results.getElements().get(0).getEntity();
        assertEquals("small0", i1.getCore().getFlavor());

        OsInstance i20 = (OsInstance) results.getElements().get(19).getEntity();
        assertEquals("small3", i20.getCore().getFlavor());

        watch.stop();
        LOG.info("tested Reverse Sorting process --> " + watch);
    }

    @Test
    public void testRevSorting() throws NoSuchSessionException, NoSuchTapestryDefinitionException,
            LogicalIdAlreadyExistsException, NoSuchAggregationException, UnsupportedOperationException,
            InvalidQueryInputException, OperationException, IllegalAccessException, NoSuchQueryDefinitionException,
            NoSuchThreadDefinitionException, OperationNotSupportedException, InvalidQueryParametersException,
            JsonProcessingException, PendingQueryResultsException, ItemPropertyNotFound, RelationPropertyNotFound,
            IllegalArgumentException, ThreadDeletedByDynAdapterUnload {

        tap = new TapestryDefinition();
        tap.setThreads(revSortThreads);
        tapestryManager.setTapestryDefinition(session, tap);

        StopWatch watch = new StopWatch();
        LOG.info("testing Sorting ops process");
        watch.start();
        QueryResult results = queryExec.processQuery(session, revSortThreads.get(0));

        // ObjectMapper mapper = new ObjectMapper();
        // mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        // System.out.println(mapper.writeValueAsString(results));

        OsInstance i20 = (OsInstance) results.getElements().get(19).getEntity();
        assertEquals("small0", i20.getCore().getFlavor());

        OsInstance i1 = (OsInstance) results.getElements().get(0).getEntity();
        assertEquals("small3", i1.getCore().getFlavor());

        watch.stop();
        LOG.info("tested Sorting process --> " + watch);
    }

    @Test
    public void testSortGroupBraid() throws NoSuchSessionException, NoSuchTapestryDefinitionException,
            LogicalIdAlreadyExistsException, NoSuchAggregationException, IllegalAccessException, OperationException,
            UnsupportedOperationException, InvalidQueryInputException, NoSuchQueryDefinitionException,
            NoSuchThreadDefinitionException, OperationNotSupportedException, InvalidQueryParametersException,
            JsonProcessingException, PendingQueryResultsException, ItemPropertyNotFound, RelationPropertyNotFound,
            IllegalArgumentException, ThreadDeletedByDynAdapterUnload {

        tap = new TapestryDefinition();
        tap.setThreads(sortGroupBraidThreads);
        tapestryManager.setTapestryDefinition(session, tap);

        StopWatch watch = new StopWatch();
        LOG.info("testing Sort Group Braid Chain ops process");
        watch.start();
        QueryResult results = queryExec.processQuery(session, tap.getThreads().get(0));

        assertTrue(results.getLogicalId().equals("/da/" + sortGroupBraidQuery.hashCode()));
        assertTrue(results.getElements().size() == 2);
        assertTrue(results.getElements().get(0).getEntity().getTags().equals("/loom/loom/BRAID"));
        //
        QueryResult results2 =
                queryExec.processQuery(session, tap.getThreads().get(0), sortGroupBraidQuery.hashCode() + "/0");

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        assertTrue(results2.getLogicalId().equals("/da/" + sortGroupBraidQuery.hashCode() + "/0"));
        assertTrue(results2.getElements().size() == 2);
        assertTrue(results2.getElements().get(0).getEntity().getTags().equals("/loom/loom/GROUP_BY"));

        QueryResult results3 = queryExec.processQuery(session, tap.getThreads().get(0),
                sortGroupBraidQuery.hashCode() + "/sort_by/group_by/id/small2");

        assertTrue(results3.getLogicalId()
                .equals("/da/" + sortGroupBraidQuery.hashCode() + "/sort_by/group_by/id/small2"));
        assertTrue(results3.getElements().size() == 2);
        // TODO confirm why?
        // assertTrue(results3.getElements().get(0).getEntity().getTags().isEmpty());

        watch.stop();
        LOG.info("tested Sort Group Braid Chain process --> " + watch);
    }



    @Test
    public void testPercentiles() throws NoSuchSessionException, NoSuchTapestryDefinitionException,
            LogicalIdAlreadyExistsException, NoSuchAggregationException, InvalidQueryInputException,
            IllegalAccessException, OperationException, UnsupportedOperationException, NoSuchQueryDefinitionException,
            NoSuchThreadDefinitionException, OperationNotSupportedException, InvalidQueryParametersException,
            JsonProcessingException, PendingQueryResultsException, ItemPropertyNotFound, RelationPropertyNotFound,
            IllegalArgumentException, ThreadDeletedByDynAdapterUnload {

        tap = new TapestryDefinition();
        tap.setThreads(percentThreads);
        tapestryManager.setTapestryDefinition(session, tap);

        StopWatch watch = new StopWatch();
        LOG.info("testing Percentiles on Memory ops process");
        watch.start();
        QueryResult results = queryExec.processQuery(session, tap.getThreads().get(0));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        System.out.println(mapper.writeValueAsString(results));

        assertTrue(results.getElements().size() == 10);
        watch.stop();
        LOG.info("tested Percentiles on memory process --> " + watch);
    }

    @Test(expected = NoSuchSessionException.class)
    public void nonExistentSession()
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, LogicalIdAlreadyExistsException,
            NoSuchAggregationException, IllegalAccessException, OperationException, UnsupportedOperationException,
            InvalidQueryInputException, NoSuchQueryDefinitionException, NoSuchThreadDefinitionException,
            OperationNotSupportedException, InvalidQueryParametersException, PendingQueryResultsException,
            ItemPropertyNotFound, RelationPropertyNotFound, IllegalArgumentException, ThreadDeletedByDynAdapterUnload {
        tap = new TapestryDefinition();
        tap.setThreads(groupBraidThreads);
        tapestryManager.setTapestryDefinition(session, tap);

        StopWatch watch = new StopWatch();
        LOG.info("testing bad session process");
        watch.start();
        queryExec.processQuery(new SessionImpl(UUID.randomUUID().toString(), sessionManager.getInterval()),
                tap.getThreads().get(0));

        watch.stop();
        LOG.info("tested bad session process --> " + watch);
    }

    @Test(expected = NoSuchAggregationException.class)
    public void nonExistentAggregation()
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, LogicalIdAlreadyExistsException,
            NoSuchAggregationException, IllegalAccessException, OperationException, UnsupportedOperationException,
            InvalidQueryInputException, NoSuchQueryDefinitionException, NoSuchThreadDefinitionException,
            OperationNotSupportedException, InvalidQueryParametersException, PendingQueryResultsException,
            ItemPropertyNotFound, RelationPropertyNotFound, IllegalArgumentException, ThreadDeletedByDynAdapterUnload {
        tap = new TapestryDefinition();
        tap.setThreads(groupBraidThreads);
        tapestryManager.setTapestryDefinition(session, tap);

        StopWatch watch = new StopWatch();
        LOG.info("testing bad aggregation process");
        List<String> badSources = new ArrayList<>(1);
        badSources.add("/bad/sources");

        watch.start();
        groupBraidQuery.setInputs(badSources);
        queryExec.processQuery(session, tap.getThreads().get(0));

        watch.stop();
        LOG.info("tested bad aggregation process --> " + watch);
    }

    @Test
    public void testOverBraid()
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, IllegalAccessException,
            UnsupportedOperationException, OperationException, NoSuchAggregationException, InvalidQueryInputException,
            LogicalIdAlreadyExistsException, NoSuchQueryDefinitionException, NoSuchThreadDefinitionException,
            OperationNotSupportedException, InvalidQueryParametersException, PendingQueryResultsException,
            ItemPropertyNotFound, RelationPropertyNotFound, ThreadDeletedByDynAdapterUnload {

        tap = new TapestryDefinition();
        tap.setThreads(braidThreads21);
        tapestryManager.setTapestryDefinition(session, tap);

        StopWatch watch = new StopWatch();
        LOG.info("testing over Braid process");
        watch.start();
        QueryResult results = queryExec.processQuery(session, tap.getThreads().get(0));
        //
        // ObjectMapper mapper = new ObjectMapper();
        // mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        // System.out.println(mapper.writeValueAsString(results));

        assertTrue(results.getLogicalId().equals("/da/" + braidQuery21.hashCode()));
        assertTrue(results.getElements().size() == 20);
        assertTrue(results.getElements().get(0).getEntity().getTags().isEmpty());

        QueryResult results2 = queryExec.processQuery(session, tap.getThreads().get(0), braidQuery21.hashCode() + "/1");
        assertTrue(results2.getLogicalId().equals("/da/" + braidQuery21.hashCode() + "/1"));
        assertTrue(results2.getElements().size() == 20);
        assertTrue(results2.getElements().get(0).getEntity().getTags().isEmpty());
        watch.stop();
        LOG.info("tested over Braid Query process --> " + watch);

    }

    private Aggregation createGroundedAggregation(final Session session)
            throws NoSuchSessionException, SessionAlreadyExistsException, LogicalIdAlreadyExistsException {
        Provider provider = createProvider();
        String typeId = "/typeids/TestType";
        ItemType itemType = createItemType(typeId);
        String logicalId = "/providers/openstack/test/testGroundedAggregation";
        String mergedLogicalId = "/providers/openstack/testGroundedAggregation";
        String name = "testGroundedAggregation";
        String description = "A test Grounded Aggregation";
        int expectedSize = 500;

        Aggregation aggregation = aggregationManager.createGroundedAggregation(session, provider, itemType, logicalId,
                mergedLogicalId, name, description, expectedSize);

        return aggregation;
    }

    private Provider createProvider() {
        return createProvider("openstack", "test", "http://openstack/v1.1/auth", "Test");
    }

    private Provider createProvider(final String providerType, final String providerId, final String authEndpoint,
            final String name) {
        return new ProviderImpl(providerType, providerId, authEndpoint, name, "com");
    }

    private ItemType createItemType(final String typeId) {
        ItemType type = new ItemType(typeId);
        type.setId("test-" + type.getLocalId());
        return type;
    }

    private ArrayList<Item> createItems(final int numItems, final int startId, final String typeId) {
        ArrayList<Item> items = new ArrayList<Item>(numItems);
        ItemType volType = new OsVolumeType(provider);
        volType.setId("os-" + volType.getLocalId());

        OsVolumeAttributes ova1 = new OsVolumeAttributes("vol1", "vId1", 0, "", "", "", "", "");
        OsVolumeAttributes ova2 = new OsVolumeAttributes("vol2", "vId2", 0, "", "", "", "", "");
        OsVolumeAttributes ova3 = new OsVolumeAttributes("vol3", "vId3", 0, "", "", "", "", "");
        OsVolumeAttributes ova4 = new OsVolumeAttributes("vol4", "vId4", 0, "", "", "", "", "");
        OsVolume vol1 = new OsVolume("vol1", volType);
        OsVolume vol2 = new OsVolume("vol2", volType);
        OsVolume vol3 = new OsVolume("vol3", volType);
        OsVolume vol4 = new OsVolume("vol4", volType);
        vol1.setCore(ova1);
        vol2.setCore(ova2);
        vol3.setCore(ova3);
        vol4.setCore(ova4);
        List<OsVolume> volumes = new ArrayList<>(4);
        volumes.add(vol1);
        volumes.add(vol2);
        volumes.add(vol3);
        volumes.add(vol4);

        ItemType instanceType = new OsInstanceType(provider);
        instanceType.setId("os-" + instanceType.getLocalId());
        for (int count = startId; count < (numItems + startId); count++) {
            String logicalId = "/os/fake/instances/i" + count;
            String name = "vm1";
            int flvId = count % 4;
            OsFlavour flavour = new OsFlavour(Integer.toString(flvId), "small" + flvId, 1, 2048, 3);
            OsInstance instance = new OsInstance(logicalId, instanceType);
            OsInstanceAttributes oia = new OsInstanceAttributes(flavour);
            oia.setItemName(name);
            oia.setItemId(name);
            instance.setCore(oia);
            int vcpu = 4 + (int) (Math.random() * 80);
            instance.getCore().setVcpus(vcpu);
            instance.addConnectedRelationships(volumes.get(count % 4), "");
            instance.addConnectedRelationships(volumes.get(0), "");
            items.add(instance);
        }
        return items;
    }
}
