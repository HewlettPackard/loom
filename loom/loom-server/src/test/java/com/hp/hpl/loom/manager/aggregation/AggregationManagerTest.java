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
package com.hp.hpl.loom.manager.aggregation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hp.hpl.loom.adapter.UpdateResult;
import com.hp.hpl.loom.adapter.os.OsInstanceType;
import com.hp.hpl.loom.adapter.os.TestInstance;
import com.hp.hpl.loom.adapter.os.TestTypeInstance;
import com.hp.hpl.loom.adapter.os.TestTypeInstanceAttributes;
import com.hp.hpl.loom.exceptions.CheckedLoomException;
import com.hp.hpl.loom.exceptions.LogicalIdAlreadyExistsException;
import com.hp.hpl.loom.exceptions.NoSuchAggregationException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.SessionAlreadyExistsException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.ProviderImpl;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.relationships.AggregationsCalculator;
import com.hp.hpl.loom.relationships.GroupingCalculator;

public class AggregationManagerTest extends AggregationManagerTestBase {
    private Provider provider = new ProviderImpl("os", "providerId", "authEndpoint", "providerName", "adapterPackage");
    private static final Log LOG = LogFactory.getLog(AggregationManagerTest.class);

    private static final int NumItems = 5;
    private static final int NumGroups = 3;

    private ArrayList<Item> createInstanceItems(final int numItems, final int startId, final String typeId,
            final String logicalIdBase) {
        String projectId = "hplb";
        ArrayList<Item> instancesArray = new ArrayList<Item>(numItems);
        for (int cc = startId; cc < (numItems + startId); cc++) {
            TestInstance instance = new TestInstance(logicalIdBase + "/" + cc, "instance" + cc,
                    projectId + (cc % NumGroups), "small", createItemType(typeId));
            instancesArray.add(instance);
        }
        return instancesArray;
    }

    private ArrayList<Fibre> createInstanceEntities(final int numItems, final int startId, final String typeId,
            final String logicalIdBase) {
        String projectId = "hplb";
        ArrayList<Fibre> instancesArray = new ArrayList<Fibre>(numItems);
        for (int cc = startId; cc < (numItems + startId); cc++) {
            TestInstance instance = new TestInstance(logicalIdBase + "/" + cc, "instance" + cc,
                    projectId + (cc % NumGroups), "small", createItemType(typeId));
            instancesArray.add(instance);
        }
        return instancesArray;
    }

    private void updateGroundedAggregation(final Session session, final Aggregation aggregation, final int size)
            throws NoSuchSessionException, NoSuchAggregationException {
        // Populate GA with data
        ArrayList<Item> instances = createInstanceItems(size, 0, aggregation.getTypeId(), aggregation.getLogicalId());
        UpdateResult updateResult = new UpdateResult(instances, instances, null, null);
        aggregationManager.updateGroundedAggregation(session, aggregation, updateResult);
        checkReturnedGroundedAggregationAfterUpdate(session, aggregation, updateResult);
    }

    /*
     * Create an empty GA.
     */
    private Aggregation createGroundedAggregation(final Session session)
            throws NoSuchAggregationException, NoSuchSessionException, LogicalIdAlreadyExistsException {
        return createGroundedAggregation(session, 500, false);
    }

    /*
     * Create a GA with items.
     */
    private Aggregation createGroundedAggregationWithItems(final Session session, final int expectedSize)
            throws NoSuchAggregationException, NoSuchSessionException, LogicalIdAlreadyExistsException {
        return createGroundedAggregation(session, expectedSize, true);
    }

    private Aggregation createGroundedAggregation(final Session session, final int expectedSize,
            final boolean withItems)
            throws NoSuchSessionException, LogicalIdAlreadyExistsException, NoSuchAggregationException {
        // Create the Grounded Aggregation
        Provider provider = createProvider();
        String typeId = "TestInstance";
        ItemType itemType = createItemType(typeId);
        String logicalId = "/providers/os/instances";
        String mergedLogicalId = "/providers/os/instances";
        String name = "TestInstancesGroundedAggregation";
        String description = "Test Instances Grounded Aggregation";

        Aggregation aggregation = aggregationManager.createGroundedAggregation(session, provider, itemType, logicalId,
                mergedLogicalId, name, description, expectedSize);

        // Test creation results
        assertNotNull("Create GroundedAggregation returned null result", aggregation);
        assertEquals("Incorrect logicalId after creation of GA", logicalId, aggregation.getLogicalId());
        assertEquals("Incorrect number of items after creation of GA", 0, aggregation.getNumberOfItems());
        assertEquals("Incorrect number of fibres after creation of GA", 0, aggregation.getNumberOfFibres());
        assertNotNull("Creation date of GA not set after creation of GA", aggregation.getFibreCreated());
        assertNull("Updated date of GA is set after creation of GA", aggregation.getFibreUpdated());
        assertNull("Deleted date of GA is set after creation of GA", aggregation.getFibreDeleted());
        assertEquals("Created count of GA is set after creation of GA", 0, aggregation.getCreatedCount());
        assertEquals("Updated count of GA is set after creation of GA", 0, aggregation.getUpdatedCount());
        assertEquals("Deleted count of GA is set after creation of GA", 0, aggregation.getDeletedCount());
        assertEquals("Incorrect number of dependsOnMe entries after creation of GA", 0,
                aggregation.getDependsOnMeAggregations().size());
        assertEquals("Incorrect number of dependsOn entries after creation of GA", 0,
                aggregation.getDependsOnMeAggregations().size());
        assertEquals("Incorrect name after creation of GA", name, aggregation.getName());
        assertEquals("Incorrect description after creation of GA", description, aggregation.getDescription());
        assertTrue("EntityType not Aggregation after creation of GA",
                aggregation.getFibreType() == Fibre.Type.Aggregation);
        assertTrue("Aggregation does not contain Items after creation of GA",
                aggregation.getContains() == Fibre.Type.Item);

        assertEquals("Incorrect typeId after creation of GA", AddProviderIdToTypeId(typeId), aggregation.getTypeId());
        // More testing on AM
        Aggregation retrievedAggregation = aggregationManager.getAggregation(session, aggregation.getLogicalId());
        assertNotNull("Could not retrieve new GA from AM", retrievedAggregation);
        assertTrue("AM reports new GA as dirty", aggregationManager.isUpToDate(session, aggregation.getLogicalId()));
        assertTrue("AM reports new GA as non-existent",
                aggregationManager.groundedAggregationExists(session, aggregation.getLogicalId()));

        if (withItems) {
            updateGroundedAggregation(session, aggregation, expectedSize);
            // Reset dirty bit, ready for subsequent testing
            aggregation.setDirty(false);
        }
        return aggregation;
    }

    private Aggregation createDerivedAggregationContainingItems(final Session session,
            final Aggregation fromAggregation)
            throws LogicalIdAlreadyExistsException, NoSuchSessionException, NoSuchAggregationException {
        String logicalId = "/da/complexhash";
        Fibre.Type contains = Fibre.Type.Item;
        String name = "testDerivedAggregation";
        String description = "A test Derived Aggregation";
        int expectedSize = 500;
        return createDerivedAggregation(session, fromAggregation, logicalId, contains, name, description, expectedSize);
    }

    private Aggregation createDerivedAggregation(final Session session, final Aggregation fromAggregation,
            final String logicalId, final Fibre.Type contains, final String name, final String description,

            final int expectedSize)
            throws NoSuchAggregationException, NoSuchSessionException, LogicalIdAlreadyExistsException {
        String[] dependsOnLogicalIdArray =
                fromAggregation.isGrounded() ? new String[] {fromAggregation.getLogicalId()} : null;
        Aggregation aggregation = aggregationManager.createDerivedAggregation(session, fromAggregation.getTypeId(),
                logicalId, contains, name, description, dependsOnLogicalIdArray, expectedSize);

        // Test creation results
        assertNotNull("Create DerivedAggregation returned null result", aggregation);
        assertEquals("Incorrect logicalId after creation of DA", logicalId, aggregation.getLogicalId());
        assertEquals("Incorrect number of items after creation of DA", 0, aggregation.getNumberOfItems());
        assertEquals("Incorrect number of fibres after creation of DA", 0, aggregation.getNumberOfFibres());
        assertNotNull("Creation date of DA not set after creation of DA", aggregation.getFibreCreated());
        assertNull("Updated date of DA is set after creation of DA", aggregation.getFibreUpdated());
        assertNull("Deleted date of DA is set after creation of DA", aggregation.getFibreDeleted());
        assertEquals("Created count of DA is set after creation of DA", 0, aggregation.getCreatedCount());
        assertEquals("Updated count of DA is set after creation of DA", 0, aggregation.getUpdatedCount());
        assertEquals("Deleted count of DA is set after creation of DA", 0, aggregation.getDeletedCount());
        assertEquals("Incorrect number of dependsOnMe entries after creation of DA", 0,
                aggregation.getDependsOnMeAggregations().size());
        assertEquals("Incorrect number of dependsOn entries after creation of DA", 0,
                aggregation.getDependsOnMeAggregations().size());
        assertEquals("Incorrect name after creation of DA", name, aggregation.getName());
        assertEquals("Incorrect description after creation of DA", description, aggregation.getDescription());
        assertTrue("EntityType not Aggregation after creation of DA",
                aggregation.getFibreType() == Fibre.Type.Aggregation);

        assertTrue("Aggregation does not contain Items after creation of DA", aggregation.getContains() == contains);
        assertEquals("Incorrect typeId after creation of DA", aggregation.getTypeId(), aggregation.getTypeId());
        // More testing on AM
        Aggregation retrievedAggregation = aggregationManager.getAggregation(session, aggregation.getLogicalId());
        assertNotNull("Could not retrieve new DA from AM", retrievedAggregation);
        assertTrue("AM reports new DA as dirty", aggregationManager.isUpToDate(session, aggregation.getLogicalId()));
        assertTrue("AM reports new DA as non-existent",
                aggregationManager.derivedAggregationExists(session, aggregation.getLogicalId()));
        return aggregation;
    }

    /**
     * Test can instantiate the Aggregation Manager.
     */
    @Test
    public void testInstantiateAggregationManager() {
        LOG.info("testInstantiateAggregationManager start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        LOG.info("testInstantiateAggregationManager end");
    }

    // //////////////////////////////////////////////////////////////////////////////////////
    // Tests for Grounded Aggregations
    // //////////////////////////////////////////////////////////////////////////////////////

    /**
     * Test create Grounded Aggregation.
     */
    @Test
    public void testCreateGroundedAggregation() throws CheckedLoomException {
        LOG.info("testCreateGroundedAggregation start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Session session = createSession();
        createGroundedAggregation(session);
        LOG.info("testCreateGroundedAggregation end");
    }

    /**
     * Test that the grounded aggregation throws an error if too big.
     *
     * @throws CheckedLoomException
     */
    @Test
    public void testCreateDerivedAggregationSizeCheck() throws CheckedLoomException {
        LOG.info("testCreateGroundedAggregationTooBig start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Session session = createSession();
        Aggregation groundedAggregation = createGroundedAggregation(session);
        Aggregation firstLevelDA = createDerivedAggregationContainingItems(session, groundedAggregation);
        try {
            createDerivedAggregation(session, firstLevelDA, "/da/toplevel", Fibre.Type.Aggregation, "toplevel",
                    "Top level DA", 1000001);
            fail("Expecting size failure");
        } catch (IllegalArgumentException ex) {
        }
        try {
            createDerivedAggregation(session, firstLevelDA, "/da/toplevel", Fibre.Type.Aggregation, "toplevel",
                    "Top level DA", -1);
            fail("Expecting size failure");
        } catch (IllegalArgumentException ex) {
        }
        createDerivedAggregation(session, firstLevelDA, "/da/toplevel", Fibre.Type.Aggregation, "toplevel",
                "Top level DA", 100);
        LOG.info("testCreateGroundedAggregationTooBig end");
    }


    /**
     * Test that the grounded aggregation throws an error if too big.
     *
     * @throws CheckedLoomException
     */
    @Test
    public void testCreateGroundedAggregationSizeCheck() throws CheckedLoomException {
        LOG.info("testCreateGroundedAggregationTooBig start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Session session = createSession();
        try {
            createGroundedAggregation(session, 1000001, false);
            fail("Expecting size failure");
        } catch (IllegalArgumentException ex) {
        }
        try {
            createGroundedAggregation(session, -1, false);
            fail("Expecting size failure");
        } catch (IllegalArgumentException ex) {
        }
        createGroundedAggregation(session, 10, false);
        LOG.info("testCreateGroundedAggregationTooBig end");
    }

    /**
     * Test create Grounded Aggregation on Null Session.
     */
    @Test
    public void testCreateGroundedAggregationOnNullSession() throws CheckedLoomException {
        LOG.info("testCreateGroundedAggregationOnNullSession start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        createSession();
        // Attempt to use null session should fail
        try {
            createGroundedAggregation(null);
            assertTrue("Did not reject request for use of null session", false);
        } catch (IllegalArgumentException iae) {
            LOG.info("Successfully caught IllegalArgumentException when attempt to use null session");
        }
        LOG.info("testCreateGroundedAggregationOnNullSession end");
    }

    /**
     * Test list Grounded Aggregations.
     */
    @Test
    public void testListGroundedAggregation() throws CheckedLoomException {
        LOG.info("testListGroundedAggregation start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Session session = createSession();
        Aggregation aggregation = createGroundedAggregation(session);
        List<Aggregation> aList = aggregationManager.listGroundedAggregations(session);
        assertNotNull("List GroundedAggregations returned null result", aList);
        assertEquals("Incorrect number of GAs in list", 1, aList.size());
        Aggregation listedAggregation = aList.get(0);
        assertNotNull("First entry in list of GAs is null", listedAggregation);
        assertEquals("Returned aggregation from list had incorrect logicalId", aggregation.getLogicalId(),
                listedAggregation.getLogicalId());
        LOG.info("testListGroundedAggregation end");
    }

    /**
     * Test get Grounded Aggregation.
     */
    @Test
    public void testGetGroundedAggregation() throws CheckedLoomException {
        LOG.info("testGetGroundedAggregation start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Session session = createSession();
        Aggregation aggregation = createGroundedAggregation(session);
        Aggregation retrievedAggregation = aggregationManager.getAggregation(session, aggregation.getLogicalId());
        assertNotNull("Get Aggregation returned null result", retrievedAggregation);
        assertEquals("Returned aggregation had incorrect logicalId", aggregation.getLogicalId(),
                retrievedAggregation.getLogicalId());
        LOG.info("testGetGroundedAggregation end");
    }

    /**
     * Test delete Grounded Aggregation.
     */
    @Test
    public void testDeleteGroundedAggregation() throws CheckedLoomException {
        LOG.info("testDeleteGroundedAggregation start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Session session = createSession();
        Aggregation aggregation = createGroundedAggregation(session);
        Aggregation retrievedAggregation = aggregationManager.getAggregation(session, aggregation.getLogicalId());
        assertNotNull("Get Aggregation returned null result", retrievedAggregation);
        assertEquals("Returned aggregation had incorrect logicalId", aggregation.getLogicalId(),
                retrievedAggregation.getLogicalId());
        // Delete the aggregation and make sure deleted
        aggregationManager.deleteGroundedAggregation(session, aggregation.getLogicalId());
        assertNotNull("Deleted date of DA is not set after delete of DA", aggregation.getFibreDeleted());
        retrievedAggregation = aggregationManager.getAggregation(session, aggregation.getLogicalId());
        assertNull("Get Aggregation did not return null after delete", retrievedAggregation);
        LOG.info("testDeleteGroundedAggregation end");
    }

    // //////////////////////////////////////////////////////////////////////////////////////
    // Tests for Derived Aggregations
    // //////////////////////////////////////////////////////////////////////////////////////

    /**
     * Test create Derived Aggregation.
     */
    @Test
    public void testCreateDerivedAggregation() throws CheckedLoomException {
        LOG.info("testCreateDerivedAggregation start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Session session = createSession();
        Aggregation groundedAggregation = createGroundedAggregation(session);
        createDerivedAggregationContainingItems(session, groundedAggregation);
        LOG.info("testCreateDerivedAggregation end");
    }

    /**
     * Test create Derived Aggregation.
     */
    @Test
    public void testCreateDerivedAggregationFromNonExistentGA() throws NoSuchSessionException,
            SessionAlreadyExistsException, LogicalIdAlreadyExistsException, NoSuchAggregationException {
        LOG.info("testCreateDerivedAggregationFromNonExistentGA start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Session session = createSession();
        Aggregation groundedAggregation = createGroundedAggregation(session);
        Aggregation dummyAggregation =
                new Aggregation("dummyid", groundedAggregation.getTypeId(), "dummy", "dumy aggregation", 500);
        dummyAggregation.setGrounded(true);
        try {
            createDerivedAggregationContainingItems(session, dummyAggregation);
            assertTrue("AM did not detect non-existent aggregation", false);
        } catch (NoSuchAggregationException e) {
            LOG.info("Successfully caught NoSuchAggregationException");
        }
        LOG.info("testCreateDerivedAggregationFromNonExistentGA end");
    }

    /**
     * Test create Derived Aggregation.
     */
    @Test
    public void testCreateDerivedAggregationWithBadFromLogicalId() throws NoSuchSessionException,
            SessionAlreadyExistsException, LogicalIdAlreadyExistsException, NoSuchAggregationException {
        LOG.info("testCreateDerivedAggregationWithBadFromLogicalId start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Session session = createSession();
        Aggregation groundedAggregation = createGroundedAggregation(session);
        String logicalId = "/da/complexhash";
        String typeId = "dummytype";
        Fibre.Type contains = Fibre.Type.Item;

        String name = "testDerivedAggregation";
        String description = "A test Derived Aggregation";
        int expectedSize = 500;
        try {
            String[] dependsOnLogicalIdArray = new String[] {groundedAggregation.getLogicalId(), "badextra"};
            aggregationManager.createDerivedAggregation(session, typeId, logicalId, contains, name, description,
                    dependsOnLogicalIdArray, expectedSize);
            assertTrue("AM did not detect multi-valued fromLogicalIdArray", false);
        } catch (NoSuchAggregationException e) {
            LOG.info("Successfully caught IllegalArgumentException for multi-valued fromLogicalIdArray");
        }
        LOG.info("testCreateDerivedAggregationWithBadFromLogicalId end");
    }

    /**
     * Test list Derived Aggregations.
     */
    @Test
    public void testListDerivedAggregation() throws CheckedLoomException {
        LOG.info("testListDerivedAggregation start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Session session = createSession();
        Aggregation groundedAggregation = createGroundedAggregation(session);
        Aggregation aggregation = createDerivedAggregationContainingItems(session, groundedAggregation);
        {
            // Test list of derived aggregations
            List<Aggregation> daList = aggregationManager.listDerivedAggregations(session);
            assertNotNull("List DerivedAggregations returned null result", daList);
            assertEquals("Incorrect number of DAs in list", 1, daList.size());
            Aggregation listedAggregation = daList.get(0);
            assertNotNull("First entry in list of DAs is null", listedAggregation);
            assertEquals("Returned aggregation from list had incorrect logicalId", aggregation.getLogicalId(),
                    listedAggregation.getLogicalId());
        }
        {
            // Test list of all aggregations
            List<Aggregation> aList = aggregationManager.listAggregations(session);
            assertNotNull("List Aggregations returned null result", aList);
            assertEquals("Incorrect number of Aggregations in list", 2, aList.size());
            Map<String, Aggregation> aggregationMap = new HashMap<String, Aggregation>();
            aggregationMap.put(aList.get(0).getLogicalId(), aList.get(0));
            aggregationMap.put(aList.get(1).getLogicalId(), aList.get(1));

            Aggregation listedGroundedAggregation = aggregationMap.get(groundedAggregation.getLogicalId());
            assertNotNull("GA did not come back in list", listedGroundedAggregation);
            assertEquals("Returned GA from list had incorrect logicalId", groundedAggregation.getLogicalId(),
                    listedGroundedAggregation.getLogicalId());

            Aggregation listedDerivedAggregation = aggregationMap.get(aggregation.getLogicalId());
            assertNotNull("DA did not come back in list", listedDerivedAggregation);
            assertEquals("Returned DA from list had incorrect logicalId", aggregation.getLogicalId(),
                    listedDerivedAggregation.getLogicalId());
        }
        LOG.info("testListDerivedAggregation end");
    }

    /**
     * Test get Derived Aggregation.
     */
    @Test
    public void testGetDerivedAggregation() throws CheckedLoomException {
        LOG.info("testGetDerivedAggregation start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Session session = createSession();
        Aggregation groundedAggregation = createGroundedAggregation(session);
        Aggregation aggregation = createDerivedAggregationContainingItems(session, groundedAggregation);
        Aggregation retrievedAggregation = aggregationManager.getAggregation(session, aggregation.getLogicalId());
        assertNotNull("Get Aggregation returned null result", retrievedAggregation);
        assertEquals("Returned aggregation had incorrect logicalId", aggregation.getLogicalId(),
                retrievedAggregation.getLogicalId());
        LOG.info("testGetDerivedAggregation end");
    }

    /**
     * Test delete Derived Aggregation.
     */
    @Test
    public void testDeleteDerivedAggregation() throws CheckedLoomException {
        LOG.info("testDeleteGroundedAggregation start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Session session = createSession();
        Aggregation groundedAggregation = createGroundedAggregation(session);
        Aggregation aggregation = createDerivedAggregationContainingItems(session, groundedAggregation);
        Aggregation retrievedAggregation = aggregationManager.getAggregation(session, aggregation.getLogicalId());
        assertNotNull("Get Aggregation returned null result", retrievedAggregation);
        assertEquals("Returned aggregation had incorrect logicalId", aggregation.getLogicalId(),
                retrievedAggregation.getLogicalId());
        // Delete the aggregation and make sure deleted
        aggregationManager.deleteAggregation(session, aggregation.getLogicalId());
        assertNotNull("Deleted date of DA is not set after delete of DA", aggregation.getFibreDeleted());
        retrievedAggregation = aggregationManager.getAggregation(session, aggregation.getLogicalId());
        assertNull("Get Aggregation did not return null after delete", retrievedAggregation);
        // Make sure no other DAs
        List<Aggregation> das = aggregationManager.listDerivedAggregations(session);
        assertEquals("There are still DAs after a DA deletion", 0, das.size());
        LOG.info("testDeleteDerivedAggregation end");
    }

    /**
     * Test recursive delete of Derived Aggregation.
     */
    @Test
    public void testRecursiveDeleteDerivedAggregation() throws CheckedLoomException {
        LOG.info("testRecursiveDeleteDerivedAggregation start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Session session = createSession();
        String groupBy = "ProjectId";
        int aggregateSize = 2;

        // Create a populated GA
        Aggregation groundedAggregation = createGroundedAggregationWithItems(session, NumItems);

        // Compute a query DA from GA, involving grouping and aggregating
        Aggregation queryAggregation = createQueryDAFromDA(session, groundedAggregation, groupBy, aggregateSize);
        assertFalse("Query DA was dirty", queryAggregation.isDirty());
        List<Aggregation> topLevelAggregations = groundedAggregation.getTopLevelDependsOnMeAggregations();
        assertEquals("GA did not have correct number of dependent top-level aggregations: GA= " + groundedAggregation,
                1, topLevelAggregations.size());

        // Create another top-level DA
        Aggregation extraDa = AggregationsCalculator.AggregateEntities(session, 1, aggregationManager,
                groundedAggregation.getLogicalId(), groundedAggregation.getTypeId(), groundedAggregation.getName(),
                groundedAggregation.getDescription(), groundedAggregation.getElements(),
                new String[] {groundedAggregation.getLogicalId()}, NumItems);
        assertFalse("Extra DA was dirty", extraDa.isDirty());
        topLevelAggregations = groundedAggregation.getTopLevelDependsOnMeAggregations();
        assertEquals("GA did not have correct number of dependent top-level aggregations: GA= " + groundedAggregation,
                2, topLevelAggregations.size());
        assertEquals("Incorrect number of top-level DAs after a DA creation", 2,
                aggregationManager.listTopLevelDerivedAggregations(session).size());

        // Delete the query aggregation and make sure deleted, along with all children
        aggregationManager.deleteAggregationAndChildren(session, queryAggregation.getLogicalId(), false);
        assertNotNull("Deleted date of DA is not set after delete of DA", queryAggregation.getFibreDeleted());
        Aggregation retrievedAggregation = aggregationManager.getAggregation(session, queryAggregation.getLogicalId());
        assertNull("Get Aggregation did not return null after delete", retrievedAggregation);
        // Make sure 1 remaining DA
        assertEquals("Incorrect number of DAs after a DA deletion", 1,
                aggregationManager.listDerivedAggregations(session).size());
        assertEquals("Incorrect number of top-level DAs after a DA deletion", 1,
                aggregationManager.listTopLevelDerivedAggregations(session).size());
        LOG.info("testRecursiveDeleteDerivedAggregation end");
    }

    /**
     * Test delete all Derived Aggregations.
     */
    @Test
    public void testDeleteAllDerivedAggregations() throws CheckedLoomException {
        LOG.info("testDeleteAllDerivedAggregations start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Session session = createSession();
        Aggregation groundedAggregation = createGroundedAggregation(session);
        createDerivedAggregationContainingItems(session, groundedAggregation);
        aggregationManager.deleteAllDerivedAggregations(session);
        List<Aggregation> aList = aggregationManager.listDerivedAggregations(session);
        assertNotNull("List DerivedAggregations returned null result", aList);
        assertEquals("Incorrect number of GAs in list", 0, aList.size());
        LOG.info("testDeleteAllDerivedAggregations end");
    }

    // //////////////////////////////////////////////////////////////////////////////////////
    // Tests for Updating Aggregations
    // //////////////////////////////////////////////////////////////////////////////////////

    private void checkReturnedAggregationAfterUpdate(final Session session, final Aggregation aggregation,
            final ArrayList<Fibre> newItems) throws NoSuchSessionException, NoSuchAggregationException {
        Aggregation retrievedAggregation = aggregationManager.getAggregation(session, aggregation.getLogicalId());
        assertNotNull("Get Aggregation returned null result", retrievedAggregation);
        assertEquals("Returned aggregation had incorrect logicalId", aggregation.getLogicalId(),
                retrievedAggregation.getLogicalId());
        // Test that aggregation was updated
        assertTrue("Updated aggregation was not dirty", retrievedAggregation.isDirty());
        assertEquals("Incorrect number of items after update of DA", newItems.size(),
                retrievedAggregation.getNumberOfItems());
        assertEquals("Incorrect number of fibres after update of DA", newItems.size(),
                retrievedAggregation.getNumberOfFibres());
        assertNotNull("Creation date of DA not set after update of DA", retrievedAggregation.getFibreCreated());
        assertNotNull("Updated date of DA is set after update of DA", retrievedAggregation.getFibreUpdated());
        assertNull("Deleted date of DA is set after update of DA", retrievedAggregation.getFibreDeleted());

        List<Fibre> retrievedEntities = retrievedAggregation.getElements();
        assertNotNull("Retrieved aggregation returned null elements", retrievedEntities);
        assertEquals("Returned aggregation incorrect number of elements", newItems.size(), retrievedEntities.size());
        assertTrue("Updated aggregation was not dirty", retrievedAggregation.isDirty());
        assertFalse("AM reports updated aggregation as clean",
                aggregationManager.isUpToDate(session, retrievedAggregation.getLogicalId()));
        for (int count = 0; count < retrievedEntities.size(); count++) {
            Fibre retrievedEntity = retrievedEntities.get(count);
            assertNotNull("Retrieved entity was not an Item", retrievedEntity instanceof Item);
            Fibre retrievedItem = retrievedEntity;
            Fibre item = newItems.get(count);
            assertNotNull("Original item was null", item);
            assertEquals("Returned entity had incorrect logicalId", item.getLogicalId(), retrievedItem.getLogicalId());
            // not all Loom entities have a UUID
            // assertEquals("Returned entity had incorrect uuid", item.getUuid(),
            // retrievedItem.getUuid());
        }
    }

    private void checkReturnedGroundedAggregationAfterUpdate(final Session session, final Aggregation aggregation,
            final UpdateResult updateResult) throws NoSuchSessionException, NoSuchAggregationException {
        Aggregation retrievedGA = aggregationManager.getAggregation(session, aggregation.getLogicalId());
        assertNotNull("Get Aggregation returned null result", retrievedGA);
        assertEquals("Returned aggregation had incorrect logicalId", aggregation.getLogicalId(),
                retrievedGA.getLogicalId());
        // Test that aggregation was updated
        ArrayList<Item> allItems = updateResult.getAllItems();
        ArrayList<Item> newItems = updateResult.getNewItems();
        ArrayList<Item> updatedItems = updateResult.getUpdatedItems();
        ArrayList<Item> deletedItems = updateResult.getDeletedItems();
        long newItemsSize = newItems == null ? 0 : newItems.size();
        long updatedItemsSize = updatedItems == null ? 0 : updatedItems.size();
        long deletedItemsSize = deletedItems == null ? 0 : deletedItems.size();

        assertEquals("Incorrect number of items after update of GA", allItems.size(), retrievedGA.getNumberOfItems());
        assertEquals("Incorrect number of fibres after update of GA", allItems.size(), retrievedGA.getNumberOfFibres());
        assertNotNull("Creation date of GA not set after update of GA", retrievedGA.getFibreCreated());
        assertNotNull("Updated date of GA is set after update of GA", retrievedGA.getFibreUpdated());
        assertNull("Deleted date of GA is set after update of GA", retrievedGA.getFibreDeleted());
        assertEquals("Created count of GA incorrect after update of GA", newItemsSize, retrievedGA.getCreatedCount());
        assertEquals("Updated count of GA incorrect after update of GA", updatedItemsSize,
                retrievedGA.getUpdatedCount());
        assertEquals("Deleted count of GA incorrect after update of GA", deletedItemsSize,
                retrievedGA.getDeletedCount());

        assertTrue("Updated aggregation was not dirty", retrievedGA.isDirty());
        List<Fibre> retrievedEntities = retrievedGA.getElements();
        assertNotNull("Retrieved aggregation returned null elements", retrievedEntities);
        assertEquals("Returned aggregation incorrect number of elements", allItems.size(), retrievedEntities.size());
        assertTrue("Updated aggregation was not dirty", retrievedGA.isDirty());
        assertFalse("AM reports updated aggregation as clean",
                aggregationManager.isUpToDate(session, retrievedGA.getLogicalId()));
        for (int count = 0; count < retrievedEntities.size(); count++) {
            Fibre retrievedEntity = retrievedEntities.get(count);
            assertNotNull("Retrieved entity was not an Item", retrievedEntity instanceof Item);
            Fibre retrievedItem = retrievedEntity;
            Fibre item = allItems.get(count);
            assertNotNull("Original item was null", item);
            assertEquals("Returned entity had incorrect logicalId", item.getLogicalId(), retrievedItem.getLogicalId());
            // not all Loom entities have a UUID
            // assertEquals("Returned entity had incorrect uuid", item.getUuid(),
            // retrievedItem.getUuid());
        }

        // Check dirty bits of dependent aggregations
        checkDirtyBitOnAggregations(aggregation.getTopLevelDependsOnMeAggregations(), true);
        checkDirtyBitOnAggregations(aggregation.getDependsOnMeAggregations(), true);
    }

    /*
     * Create a top-level DA that simulates a grouped and aggregated query on a GA.
     */
    private Aggregation createQueryDAFromDA(final Session session, final Aggregation groundedAggregation,
            final String groupBy, final int aggregateSize)
            throws LogicalIdAlreadyExistsException, NoSuchSessionException, NoSuchAggregationException {
        Collection<Fibre> grouped =
                GroupingCalculator.GroupEntities(session, aggregationManager, groundedAggregation, groupBy);
        Aggregation derivedAggregation = AggregationsCalculator.AggregateEntities(session, 0, aggregationManager,
                groundedAggregation.getLogicalId(), groundedAggregation.getTypeId(), groundedAggregation.getName(),
                groundedAggregation.getDescription(), grouped, new String[] {groundedAggregation.getLogicalId()},
                aggregateSize);
        return derivedAggregation;
    }

    // //////////////////////////////////////////////////////////////////////////////////////
    // Tests for Updating Grounded Aggregations via "Full" Mechanism
    // //////////////////////////////////////////////////////////////////////////////////////

    /**
     * Test update Grounded Aggregation.
     */
    @Test
    public void testUpdateGroundedAggregation() throws CheckedLoomException {
        LOG.info("testUpdateGroundedAggregation start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Session session = createSession();
        String groupBy = "ProjectId";
        int aggregateSize = 2;

        // Create a populated GA
        Aggregation groundedAggregation = createGroundedAggregationWithItems(session, NumItems);

        // Compute a query DA from GA, involving grouping and aggregating
        Aggregation queryAggregation = createQueryDAFromDA(session, groundedAggregation, groupBy, aggregateSize);
        assertFalse("Query DA was dirty", queryAggregation.isDirty());
        List<Aggregation> topLevelAggregations = groundedAggregation.getTopLevelDependsOnMeAggregations();
        assertEquals("GA did not have correct number of dependent top-level aggregations: GA= " + groundedAggregation,
                1, topLevelAggregations.size());

        // Create another top-level DA
        Aggregation extraDa = AggregationsCalculator.AggregateEntities(session, 1, aggregationManager,
                groundedAggregation.getLogicalId(), groundedAggregation.getTypeId(), groundedAggregation.getName(),
                groundedAggregation.getDescription(), groundedAggregation.getElements(),
                new String[] {groundedAggregation.getLogicalId()}, NumItems);
        assertFalse("Extra DA was dirty", extraDa.isDirty());
        topLevelAggregations = groundedAggregation.getTopLevelDependsOnMeAggregations();
        assertEquals("GA did not have correct number of dependent top-level aggregations: GA= " + groundedAggregation,
                2, topLevelAggregations.size());

        // Update GA again to test updates and effect on dirty bits
        updateGroundedAggregation(session, groundedAggregation, NumItems);

        // Delete the GA and make sure all dependant DAs were also deleted
        aggregationManager.deleteAggregationAndChildren(session, groundedAggregation.getLogicalId(), false);
        assertNotNull("Deleted date of GA is not set after delete of GA", groundedAggregation.getFibreDeleted());
        assertEquals("There are still top-level DAs after deletion of GA", 0,
                aggregationManager.listTopLevelDerivedAggregations(session).size());
        assertEquals("There are still DAs after deletion of GA", 0,
                aggregationManager.listDerivedAggregations(session).size());
        LOG.info("testUpdateGroundedAggregation end");
    }

    /**
     * Test attempt to use update Grounded Aggregation API using a Derived Aggregation.
     */
    @Test
    public void testUpdateGroundedAggregationOnDerivedAggregation() throws NoSuchSessionException,
            NoSuchAggregationException, LogicalIdAlreadyExistsException, SessionAlreadyExistsException {
        LOG.info("testUpdateGroundedAggregationOnDerivedAggregation start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Session session = createSession();
        Aggregation groundedAggregation = createGroundedAggregation(session);
        Aggregation aggregation = createDerivedAggregationContainingItems(session, groundedAggregation);
        ArrayList<Item> newItems =
                createInstanceItems(20, 0, aggregation.getTypeId(), groundedAggregation.getLogicalId());
        try {
            aggregationManager.updateGroundedAggregation(session, aggregation,
                    new UpdateResult(newItems, newItems, null, null));
            assertTrue("AM did not detect update on a DA when expecting a GA", false);
        } catch (NoSuchAggregationException iae) {
            LOG.info("Successfully caught NoSuchAggregationException when attempt to update GA on a DA");
        }
        LOG.info("testUpdateGroundedAggregationOnDerivedAggregation end");
    }

    /**
     * Test update non-existent Grounded Aggregation.
     */
    @Test
    public void testUpdateNonExistentGroundedAggregation() throws CheckedLoomException {
        LOG.info("testUpdateNonExistentGroundedAggregation start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Session session = createSession();
        Aggregation aggregation = createGroundedAggregation(session);
        Aggregation dummyAggregation =
                new Aggregation("/dummy", aggregation.getTypeId(), "dummy", "dumy aggregation", 500);
        dummyAggregation.setGrounded(true);
        ArrayList<Item> newItems =
                createInstanceItems(20, 0, dummyAggregation.getTypeId(), dummyAggregation.getLogicalId());
        try {
            aggregationManager.updateGroundedAggregation(session, dummyAggregation,
                    new UpdateResult(newItems, newItems, null, null));
            assertTrue("Aggregation manager did not detect non-existent logicalId", false);
        } catch (NoSuchAggregationException sle) {
            LOG.info("testUpdateNonExistentGroundedAggregation correctly caught NoSuchAggregationException");
        }
        LOG.info("testUpdateNonExistentGroundedAggregation end");
    }



    // //////////////////////////////////////////////////////////////////////////////////////
    // Tests for Updating Derived Aggregations
    // //////////////////////////////////////////////////////////////////////////////////////

    /**
     * Test update Derived Aggregation.
     */
    @Test
    public void testUpdateDerivedAggregation() throws CheckedLoomException {
        LOG.info("testUpdateDerivedAggregation start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Session session = createSession();
        Aggregation groundedAggregation = createGroundedAggregation(session);

        // Create a simple top-level/first-level DA structure
        Aggregation firstLevelDA = createDerivedAggregationContainingItems(session, groundedAggregation);
        Aggregation topLevelDA = createDerivedAggregation(session, firstLevelDA, "/da/toplevel", Fibre.Type.Aggregation,

                "toplevel", "Top level DA", 1);
        topLevelDA.add(firstLevelDA);
        // Make sure correct number of DAs
        assertEquals("Incorrect number of DAs after a DA creation", 2,
                aggregationManager.listDerivedAggregations(session).size());
        assertEquals("Incorrect number of top-level DAs after DA creation", 1,
                aggregationManager.listTopLevelDerivedAggregations(session).size());
        // Check that all DAs are clean
        checkDirtyBitOnAggregations(aggregationManager.listDerivedAggregations(session), false);

        // Update derived aggregation
        ArrayList<Fibre> newItems =
                createInstanceEntities(20, 0, firstLevelDA.getTypeId(), groundedAggregation.getLogicalId());
        aggregationManager.updateDerivedAggregation(session, firstLevelDA, newItems);
        checkReturnedAggregationAfterUpdate(session, firstLevelDA, newItems);
        // Check that all DAs are now dirty
        checkDirtyBitOnAggregations(aggregationManager.listDerivedAggregations(session), true);
        LOG.info("testUpdateDerivedAggregation end");
    }


    /**
     * Test update non-existent Derived Aggregation.
     */
    @Test
    public void testUpdateNonExistentDerivedAggregation() throws CheckedLoomException {
        LOG.info("testUpdateNonExistentDerivedAggregation start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Session session = createSession();
        Aggregation groundedAggregation = createGroundedAggregation(session);
        Aggregation aggregation = createDerivedAggregationContainingItems(session, groundedAggregation);
        Aggregation dummyAggregation =
                new Aggregation("/dummy", aggregation.getTypeId(), "dummy", "dumy aggregation", 500);
        dummyAggregation.setGrounded(false);
        ArrayList<Fibre> newItems =
                createInstanceEntities(20, 0, dummyAggregation.getTypeId(), groundedAggregation.getLogicalId());
        try {
            aggregationManager.updateDerivedAggregation(session, dummyAggregation, newItems);
            assertTrue("Aggregation manager did not detect non-existent logicalId", false);
        } catch (NoSuchAggregationException sle) {
            LOG.info("testUpdateNonExistentDerivedAggregation correctly caught NoSuchAggregationException");
        }
        LOG.info("testUpdateNonExistentDerivedAggregation end");
    }

    // //////////////////////////////////////////////////////////////////////////////////////
    // Tests for Sessions
    // //////////////////////////////////////////////////////////////////////////////////////

    /**
     * Test Delete Session.
     */
    @Test
    public void testDeleteSession() throws CheckedLoomException {
        LOG.info("testDeleteSession start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Session session = createSession();
        Aggregation aggregation = createGroundedAggregation(session);
        aggregationManager.deleteSession(session);
        // Aggregation should now have gone
        try {
            aggregationManager.getAggregation(session, aggregation.getLogicalId());
            assertTrue("Did not reject request for aggregation for non-existent session", false);
        } catch (NoSuchSessionException nse) {
            LOG.info("Successfully caught NoSuchSessionException when get Aggregation for non-existent session");
        }
        // Attempt to delete same session should now fail
        try {
            aggregationManager.deleteSession(session);
            assertTrue("Did not reject request for delete session for non-existent session", false);
        } catch (NoSuchSessionException nse) {
            LOG.info("Successfully caught NoSuchSessionException when attempt to delete non-existent session");
        }
        LOG.info("testDeleteSession end");
    }

    /**
     * Test Delete Null Session.
     */
    @Test
    public void testDeleteNullSession() throws CheckedLoomException {
        LOG.info("testDeleteNullSession start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        // Attempt to delete null session should fail
        try {
            aggregationManager.deleteSession(null);
            assertTrue("Did not reject request for delete null session", false);
        } catch (NoSuchSessionException nse) {
            LOG.info("Successfully caught NoSuchSessionException when attempt to delete null session");
        }
        LOG.info("testDeleteNullSession end");
    }

    /**
     * Test Delete All Sessions.
     */
    @Test
    public void testDeleteAllSessions() throws CheckedLoomException {
        LOG.info("testDeleteAllSessions start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Session session = createSession();
        Aggregation aggregation = createGroundedAggregation(session);
        aggregationManager.deleteAllSessions();
        // Aggregation should now have gone
        try {
            aggregationManager.getAggregation(session, aggregation.getLogicalId());
            assertTrue("Did not reject request for aggregation for non-existent session", false);
        } catch (NoSuchSessionException nse) {
            LOG.info("Successfully caught NoSuchSessionException when get Aggregation for non-existent session");
        }
        // Attempt to delete same session should now fail
        try {
            aggregationManager.deleteSession(session);
            assertTrue("Did not reject request for delete session for non-existent session", false);
        } catch (NoSuchSessionException nse) {
            LOG.info("Successfully caught NoSuchSessionException when attempt to delete non-existent session");
        }
        LOG.info("testDeleteAllSessions end");
    }

    // //////////////////////////////////////////////////////////////////////////////////////
    // Tests for Json (De-)Seialisation of Items and Aggregations
    // //////////////////////////////////////////////////////////////////////////////////////

    private String toJson(final Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String jsonRep = "";
        jsonRep = mapper.writeValueAsString(object);
        return jsonRep;
    }

    private <T> T fromJson(final String json, final Class<T> classObj) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        T recovered = mapper.readValue(json, classObj);
        return recovered;
    }

    /**
     * Test JSON serialisation and de-serialisation of Aggregations and Items.
     */
    @Test
    public void testJsonSerialisationOfItem() throws IOException {
        LOG.info("testJsonSerialisationOfItem start");
        // Create an Item
        String name = "myname";
        String description = "My description";
        String logicalId = "/providers/test/items/item";
        String projectId = "project1";
        String deviceName = "/dev/null";
        ItemType it = new OsInstanceType(provider);
        it.setId(it.getLocalId());

        TestTypeInstance item = new TestTypeInstance(logicalId, name, projectId, deviceName, it);
        TestTypeInstanceAttributes core = item.getCore();
        core.setItemName(name);
        core.setItemDescription(description);
        item.setCore(core);
        item.setFibreCreated(new Date());
        // Convert to JSON
        String itemJson = toJson(item);
        assertNotNull("Json of item was null", itemJson);
        LOG.info("testJsonSerialisationOfItem serialised Item json = " + itemJson);
        LOG.info("testJsonSerialisationOfItem serialised Item hashCode = " + item.hashCode());

        // Get it back again
        Item recoveredItem = fromJson(itemJson, TestTypeInstance.class);
        LOG.info("testJsonSerialisationOfItem recovered Item = " + recoveredItem);
        LOG.info("testJsonSerialisationOfItem recovered Item hashCode = " + recoveredItem.hashCode());
        LOG.info("testJsonSerialisationOfItem recovered Item json = " + toJson(recoveredItem));
        assertNotNull("Recovered item was null", recoveredItem);
        assertEquals("Recovered item not same as original ", item, recoveredItem);
        assertNotNull("Recovered item did not have created date set", recoveredItem.getFibreCreated());

        LOG.info("testJsonSerialisationOfItem end");
    }

    /**
     * Test JSON serialisation and de-serialisation of Separable Items.
     */
    @Test
    public void testJsonSerialisationOfSeparableItem() throws IOException {
        LOG.info("testJsonSerialisationOfSeparableItem start");
        // Create an Item
        ItemType it = new OsInstanceType(provider);
        it.setId(it.getLocalId());
        TestTypeInstance item = new TestTypeInstance("mylogicalId", "myname", "myprojectid", "mydevivename", it);

        // Convert to JSON
        String itemJson = toJson(item);
        assertNotNull("Json of item was null", itemJson);
        LOG.info("testJsonSerialisationOfSeparableItem serialised Item json = " + itemJson);
        LOG.info("testJsonSerialisationOfSeparableItem serialised Item hashCode = " + item.hashCode());

        // Get it back again
        TestTypeInstance recoveredItem = fromJson(itemJson, TestTypeInstance.class);
        LOG.info("testJsonSerialisationOfSeparableItem recovered Item = " + recoveredItem);
        LOG.info("testJsonSerialisationOfSeparableItem recovered Item hashCode = " + recoveredItem.hashCode());
        LOG.info("testJsonSerialisationOfSeparableItem recovered Item json = " + toJson(recoveredItem));
        assertNotNull("Recovered item was null", recoveredItem);
        assertEquals("Recovered item not same as original", item.getLogicalId(), recoveredItem.getLogicalId());
        assertEquals("Recovered item attributes not same as original", item.getCore().getDeviceName(),
                recoveredItem.getCore().getDeviceName());

        LOG.info("testJsonSerialisationOfSeparableItem end");
    }

    /**
     * Test JSON serialisation and de-serialisation of Aggregations and Items.
     */
    @Test
    public void testJsonSerialisationOfAggregation() throws IOException {
        LOG.info("testJsonSerialisationOfAggregation start");
        // Create some Items
        int numItems = 3;
        String typeId = "/types/os/MyItem";
        ArrayList<Item> items = createInstanceItems(numItems, 0, typeId, "/a/logical/id");
        // Put them in an Aggregation
        String aggregationLogicalId = "/providers/test/items";
        String aggregationName = "myaggregationname";
        String aggregationDescription = "My aggregation description";
        Aggregation aggregation =
                new Aggregation(aggregationLogicalId, typeId, aggregationName, aggregationDescription, numItems);
        for (Fibre item : items) {
            aggregation.add(item);
        }
        aggregation.setGrounded(true);
        aggregation.setFibreCreated(new Date());
        // Convert to JSON
        String aggregationJson = toJson(aggregation);
        assertNotNull("Json of aggregation was null", aggregationJson);
        LOG.info("testJsonSerialisationOfAggregation serialised Aggregation json = " + aggregationJson);
        // Get it back again
        Aggregation recoveredAggregation = fromJson(aggregationJson, Aggregation.class);
        assertNotNull("Recovered aggregation was null", recoveredAggregation);
        assertEquals("Aggregation logicalId not same as original ", aggregation.getLogicalId(),
                recoveredAggregation.getLogicalId());
        assertEquals("Aggregation name not same as original ", aggregation.getName(), recoveredAggregation.getName());
        assertEquals("Aggregation description not same as original ", aggregation.getDescription(),
                recoveredAggregation.getDescription());
        assertEquals("Aggregation typeId not same as original ", aggregation.getTypeId(),
                recoveredAggregation.getTypeId());
        assertEquals("Aggregation contains not same as original ", aggregation.getContains(),
                recoveredAggregation.getContains());
        assertEquals("Aggregation EntityType not same as original ", aggregation.getFibreType(),
                recoveredAggregation.getFibreType());
        assertNotNull("Aggregation did not have created date set", recoveredAggregation.getFibreCreated());

        LOG.info("testJsonSerialisationOfAggregation end");
    }

}
