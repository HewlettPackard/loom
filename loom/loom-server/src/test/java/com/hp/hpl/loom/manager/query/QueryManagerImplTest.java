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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hp.hpl.loom.adapter.UpdateResult;
import com.hp.hpl.loom.adapter.os.OsInstance;
import com.hp.hpl.loom.adapter.os.OsInstanceAttributes;
import com.hp.hpl.loom.adapter.os.OsInstanceType;
import com.hp.hpl.loom.api.util.SessionManager;
import com.hp.hpl.loom.exceptions.LogicalIdAlreadyExistsException;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.NoSuchTapestryDefinitionException;
import com.hp.hpl.loom.exceptions.NoSuchThreadDefinitionException;
import com.hp.hpl.loom.exceptions.SessionAlreadyExistsException;
import com.hp.hpl.loom.manager.aggregation.AggregationManager;
import com.hp.hpl.loom.manager.itemtype.ItemTypeManager;
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
import com.hp.hpl.loom.tapestry.Operation;
import com.hp.hpl.loom.tapestry.QueryDefinition;
import com.hp.hpl.loom.tapestry.TapestryDefinition;
import com.hp.hpl.loom.tapestry.ThreadDefinition;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext-testQueryManager.xml")
public class QueryManagerImplTest {
    private static final Log LOG = LogFactory.getLog(QueryManagerImplTest.class);

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    QueryManager queryManager;

    @Autowired
    TapestryManager tapestryManager;

    @Autowired
    ItemTypeManager itemTypeManager;

    @Autowired
    AggregationManager aggregationManager;

    @Autowired
    private Tacker stitcher;

    List<ThreadDefinition> threads;
    TapestryDefinition tap;
    QueryDefinition identityQuery;

    Aggregation groundedAggregation;

    Session session;
    ItemType itemType = null;

    Provider prov = new ProviderImpl("os", "os", "os", "os", "com");


    @After
    public void clean() throws NoSuchProviderException, NoSuchItemTypeException {

    }

    @Before
    public void setUp() throws Exception {
        LOG.info("Setup test...");
        itemType = new OsInstanceType(prov);
        session = new SessionImpl("testSession", sessionManager.getInterval());

        itemType.setId(itemType.getLocalId());
        if (itemTypeManager.getItemType(itemType.getId()) == null) {
            itemTypeManager.addItemType(prov, itemType);
        }

        // stub tapestryManager
        threads = new ArrayList<ThreadDefinition>(1);

        String identity = "/loom/loom/IDENTITY";

        String in1 = "/providers/openstack/test/testGroundedAggregation";

        Map<String, Object> identityParams1 = new HashMap(0);
        List<String> sources = new ArrayList<String>(1);
        sources.add(in1);
        Operation groupByOperation = new Operation(identity, identityParams1);

        List<Operation> pipe = new ArrayList<Operation>(1);
        pipe.add(groupByOperation);
        identityQuery = new QueryDefinition(pipe, sources);

        ThreadDefinition threadDefinition = new ThreadDefinition("0", "/os/instances", identityQuery);
        threadDefinition.setItemType(itemType.getId());
        threads.add(threadDefinition);
        tap = new TapestryDefinition();
        tap.setThreads(threads);

        // stub aggregManager
        aggregationManager.createSession(session);
        stitcher.createSession(session);
        groundedAggregation = createGroundedAggregation(session);

        ArrayList<Item> newItems = createItems(20, 0, groundedAggregation.getTypeId());
        UpdateResult updateResult = new UpdateResult(newItems, newItems, null, null);
        aggregationManager.updateGroundedAggregation(session, groundedAggregation, updateResult);

        tapestryManager.setTapestryDefinition(session, tap);

        LOG.info("Setup test done.");
    }

    @After
    public void shutDown() throws Exception {
        aggregationManager.deleteAllSessions();
        stitcher.deleteAllSessions();
        LOG.info("shutDown test");
    }

    @Test(expected = NoSuchThreadDefinitionException.class)
    public void testGetNonExistingThread() throws Exception, UnsupportedOperationException {

        StopWatch watch = new StopWatch();
        LOG.info("testing non-existing getThread...");
        queryManager.getThread(session, "3", false);
        LOG.info("tested non-existing getThread --> " + watch);
    }

    @Test(expected = NoSuchTapestryDefinitionException.class)
    public void testGetNonExistingTapestry() throws Exception, UnsupportedOperationException {
        StopWatch watch = new StopWatch();
        LOG.info("testing getThread for wrong session ...");
        Session session3 = new SessionImpl("oneNosSoRandomSession", sessionManager.getInterval());
        aggregationManager.createSession(session3);
        queryManager.getThread(session3, "3", false);
        LOG.info("tested getThread for wrong session --> " + watch);
    }

    @Test
    public void testGetIdentityOperatorAggregationThread() throws Exception, UnsupportedOperationException {

        StopWatch watch = new StopWatch();
        LOG.info("testing getThread...");

        QueryResult result = queryManager.getThread(session, "0", false);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        // System.out.println(mapper.writeValueAsString(result));

        assertTrue(result.getLogicalId().equals("/da/" + identityQuery.hashCode()));
        assertTrue(result.getElements().size() == 20);

        LOG.info("tested getThread --> " + watch);
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

    private Provider createProvider(final String providerType, final String providerId, final String authEndpoint,
            final String name) {
        return new ProviderImpl(providerType, providerId, authEndpoint, name, "com");
    }

    private Provider createProvider() {
        return createProvider("openstack", "test", "http://openstack/v1.1/auth", "Test");
    }

    private ItemType createItemType(final String typeId) {
        ItemType type = new ItemType(typeId);
        type.setId("test-" + type.getLocalId());
        return type;
    }

    private ArrayList<Item> createItems(final int numItems, final int startId, final String typeId) {
        ArrayList<Item> items = new ArrayList<Item>(numItems);
        OsInstanceType type = new OsInstanceType(prov);
        type.setId("os-" + type.getLocalId());
        for (int count = startId; count < (numItems + startId); count++) {
            String itemName = "myname" + count;
            String itemDescription = "My description " + count;
            String itemLogicalId = "/providers/test/items/item" + count;
            // Item item = new OsInstance(itemLogicalId, itemName, Integer.toString(count), null,
            // type);
            OsInstance item = new OsInstance(itemLogicalId, type);
            OsInstanceAttributes oia = new OsInstanceAttributes(null);
            oia.setItemName(itemName);
            oia.setItemId(Integer.toString(count));
            oia.setItemDescription(itemDescription);
            item.setCore(oia);
            // item.setDescription(itemDescription);
            item.setFibreCreated(new Date());
            items.add(item);
        }
        return items;
    }

}
