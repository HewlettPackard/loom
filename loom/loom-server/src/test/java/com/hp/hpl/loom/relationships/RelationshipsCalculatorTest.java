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
package com.hp.hpl.loom.relationships;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.hp.hpl.loom.adapter.UpdateResult;
import com.hp.hpl.loom.adapter.os.TestInstance;
import com.hp.hpl.loom.adapter.os.TestMtoMInstance;
import com.hp.hpl.loom.adapter.os.TestMtoMProject;
import com.hp.hpl.loom.adapter.os.TestMtoMVolume;
import com.hp.hpl.loom.adapter.os.TestProject;
import com.hp.hpl.loom.adapter.os.TestTypeInstance;
import com.hp.hpl.loom.adapter.os.TestTypeProject;
import com.hp.hpl.loom.adapter.os.TestTypeVolume;
import com.hp.hpl.loom.adapter.os.TestVolume;
import com.hp.hpl.loom.exceptions.CheckedLoomException;
import com.hp.hpl.loom.exceptions.LogicalIdAlreadyExistsException;
import com.hp.hpl.loom.exceptions.NoSuchAggregationException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.RelationPropertyNotFound;
import com.hp.hpl.loom.manager.aggregation.AggregationManagerTestBase;
import com.hp.hpl.loom.manager.stitcher.ItemEquivalence;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.QueryResult;
import com.hp.hpl.loom.model.QueryResultElement;
import com.hp.hpl.loom.model.SeparableItem;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.model.introspection.FibreIntrospectionUtils;

public class RelationshipsCalculatorTest extends AggregationManagerTestBase {

    private static final Log LOG = LogFactory.getLog(RelationshipsCalculatorTest.class);

    private static final int NumItems = 6;
    private static final int NumProjects = 3;

    private static final String TYPE_ID_INSTANCE = "/typeids/testinstance";
    private static final String TYPE_ID_VOLUME = "/typeids/testvolume";
    private static final String TYPE_ID_PROJECT = "/typeids/testproject";

    private static String[] ProjectNotFriendsWithArray = {AddProviderIdToTypeId(TYPE_ID_VOLUME)};
    private static final Set<String> ProjectNotFriendsWith =
            new HashSet<String>(Arrays.asList(ProjectNotFriendsWithArray));


    @Autowired
    private RelationshipCalculator relationshipCalculator;

    private Aggregation createInstancesGroundedAggregation(final Session session, final Provider provider,
            final ItemType itemType, final int expectedSize)
            throws NoSuchSessionException, LogicalIdAlreadyExistsException, NoSuchAggregationException {
        String logicalId = "/providers/os/instances";
        String mergedLogicalId = "/providers/os/instances";
        String name = "TestInstancesGroundedAggregation";
        String description = "Test Instances Grounded Aggregation";

        return aggregationManager.createGroundedAggregation(session, provider, itemType, logicalId, mergedLogicalId,
                name, description, expectedSize);
    }

    private Aggregation createVolumesGroundedAggregation(final Session session, final Provider provider,
            final ItemType itemType, final int expectedSize)
            throws NoSuchSessionException, LogicalIdAlreadyExistsException, NoSuchAggregationException {
        String logicalId = "/providers/os/volumes";
        String mergedLogicalId = "/providers/os/volumes";
        String name = "TestVolumesGroundedAggregation";
        String description = "Test Volume Grounded Aggregation";

        return aggregationManager.createGroundedAggregation(session, provider, itemType, logicalId, mergedLogicalId,
                name, description, expectedSize);
    }

    private Aggregation createProjectsGroundedAggregation(final Session session, final Provider provider,
            final ItemType itemType, final int expectedSize)
            throws NoSuchSessionException, LogicalIdAlreadyExistsException, NoSuchAggregationException {
        String logicalId = "/providers/os/projects";
        String mergedLogicalId = "/providers/os/projects";
        String name = "TestProjectsGroundedAggregation";
        String description = "Test Project Grounded Aggregation";

        return aggregationManager.createGroundedAggregation(session, provider, itemType, logicalId, mergedLogicalId,
                name, description, expectedSize);
    }


    // //////////////////////////////////////////////////////////////////////////////////////////
    // MtoM-based GA creation
    // //////////////////////////////////////////////////////////////////////////////////////////

    private Aggregation createMtoMInstancesGroundedAggregation(final Session session, final Provider provider,
            final ItemType itemType, final int expectedSize)
            throws NoSuchSessionException, LogicalIdAlreadyExistsException, NoSuchAggregationException {
        String logicalId = "/providers/os/mtominstances";
        String mergedLogicalId = "/providers/os/mtominstances";
        String name = "TestMtoMInstancesGroundedAggregation";
        String description = "Test MtoM Instances Grounded Aggregation";

        return aggregationManager.createGroundedAggregation(session, provider, itemType, logicalId, mergedLogicalId,
                name, description, expectedSize);
    }

    private Aggregation createMtoMVolumesGroundedAggregation(final Session session, final Provider provider,
            final ItemType itemType, final int expectedSize)
            throws NoSuchSessionException, LogicalIdAlreadyExistsException, NoSuchAggregationException {
        String logicalId = "/providers/os/mtomvolumes";
        String mergedLogicalId = "/providers/os/mtomvolumes";
        String name = "TestMtoMVolumesGroundedAggregation";
        String description = "Test MtoM Volume Grounded Aggregation";

        return aggregationManager.createGroundedAggregation(session, provider, itemType, logicalId, mergedLogicalId,
                name, description, expectedSize);
    }


    private Aggregation createMtoMProjectsGroundedAggregation(final Session session, final Provider provider,
            final ItemType itemType, final int expectedSize)
            throws NoSuchSessionException, LogicalIdAlreadyExistsException, NoSuchAggregationException {
        String logicalId = "/providers/os/mtomprojects";
        String mergedLogicalId = "/providers/os/mtomprojects";
        String name = "TestMtoMProjectsGroundedAggregation";
        String description = "Test MtoM Project Grounded Aggregation";

        return aggregationManager.createGroundedAggregation(session, provider, itemType, logicalId, mergedLogicalId,
                name, description, expectedSize);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////
    // "Standard-data-structure" relationship GA creation
    // //////////////////////////////////////////////////////////////////////////////////////////

    private void addInstanceToVolume(final TestInstance instance, final TestVolume volume) {
        volume.addInstance(instance);
        instance.setVolume(volume);
    }

    private void addMtoMInstanceToMtoMVolume(final TestMtoMInstance instance, final TestMtoMVolume volume) {
        volume.addInstance(instance);
        instance.addVolume(volume);
    }

    private void addInstanceToProject(final TestInstance instance, final TestProject project) {
        project.addInstance(instance);
        instance.setProject(project);
    }

    private void addMtoMInstanceToMtoMProject(final TestMtoMInstance instance, final TestMtoMProject project) {
        project.addInstance(instance);
        instance.addProject(project);
    }

    private void addVolumeToProject(final TestVolume volume, final TestProject project) {
        project.addVolume(volume);
        volume.setProject(project);
    }

    private void addMtoMVolumeToMtoMProject(final TestMtoMVolume volume, final TestMtoMProject project) {
        project.addVolume(volume);
        volume.addProject(project);
    }

    private void createGroundedAggregationsWithItems(final Session session, final int expectedSize)
            throws NoSuchAggregationException, NoSuchSessionException, LogicalIdAlreadyExistsException {
        Provider provider = createProvider();
        // Create the Grounded Aggregations
        String projectBase = "hplb";
        ItemType instanceType = createItemType(TYPE_ID_INSTANCE);
        ItemType volumeType = createItemType(TYPE_ID_VOLUME);
        ItemType projectType = createItemType(TYPE_ID_PROJECT);
        Aggregation instancesAgg = createInstancesGroundedAggregation(session, provider, instanceType, expectedSize);
        Aggregation volumesAgg = createVolumesGroundedAggregation(session, provider, volumeType, expectedSize);
        Aggregation projectsAgg = createProjectsGroundedAggregation(session, provider, projectType, NumProjects);
        // Now populate them with data
        ArrayList<Item> instancesArray = new ArrayList<Item>(expectedSize);
        ArrayList<Item> volumesArray = new ArrayList<Item>(expectedSize);
        ArrayList<Item> projectsArray = new ArrayList<Item>(NumProjects);
        for (int cc = 0; cc < NumProjects; cc++) {
            TestProject project = new TestProject(projectsAgg.getLogicalId() + "/" + cc, "project" + cc,
                    projectBase + cc, projectType);
            projectsArray.add(project);
        }
        for (int cc = 0; cc < expectedSize; cc++) {
            int projectId = (cc % NumProjects);
            TestInstance instance = new TestInstance(instancesAgg.getLogicalId() + "/" + cc, "instance" + cc,
                    projectBase + projectId, "small", instanceType);
            TestVolume volume = new TestVolume(volumesAgg.getLogicalId() + "/" + cc, "volume" + cc,
                    projectBase + projectId, "/dev/sda", volumeType);
            TestProject project = (TestProject) projectsArray.get(projectId);
            addInstanceToVolume(instance, volume);
            addInstanceToProject(instance, project);
            addVolumeToProject(volume, project);
            instancesArray.add(instance);
            volumesArray.add(volume);
        }
        // Update the GAs
        UpdateResult instanceUpdate = new UpdateResult(instancesArray, instancesArray, null, null);
        UpdateResult volumeUpdate = new UpdateResult(volumesArray, volumesArray, null, null);
        UpdateResult projectUpdate = new UpdateResult(projectsArray, projectsArray, null, null);
        // force the ignore to false for testing (and kicks)

        aggregationManager.updateGroundedAggregation(session, instancesAgg, instanceUpdate);
        aggregationManager.updateGroundedAggregation(session, volumesAgg, volumeUpdate);
        aggregationManager.updateGroundedAggregation(session, projectsAgg, projectUpdate);

    }

    private void createMtoMGroundedAggregationsWithItems(final Session session, final int expectedSize)
            throws NoSuchAggregationException, NoSuchSessionException, LogicalIdAlreadyExistsException {
        Provider provider = createProvider();
        // Create the Grounded Aggregations
        String projectBase = "hplb";
        ItemType instanceType = createItemType(TYPE_ID_INSTANCE);
        ItemType volumeType = createItemType(TYPE_ID_VOLUME);
        ItemType projectType = createItemType(TYPE_ID_PROJECT);
        Aggregation instancesAgg =
                createMtoMInstancesGroundedAggregation(session, provider, instanceType, expectedSize);
        Aggregation volumesAgg = createMtoMVolumesGroundedAggregation(session, provider, volumeType, expectedSize);
        Aggregation projectsAgg = createMtoMProjectsGroundedAggregation(session, provider, projectType, NumProjects);
        // Now populate them with data
        ArrayList<Item> instancesArray = new ArrayList<Item>(expectedSize);
        ArrayList<Item> volumesArray = new ArrayList<Item>(expectedSize);
        ArrayList<Item> projectsArray = new ArrayList<Item>(NumProjects);
        for (int cc = 0; cc < NumProjects; cc++) {
            TestMtoMProject project = new TestMtoMProject(projectsAgg.getLogicalId() + "/" + cc, "project" + cc,
                    projectBase + cc, projectType);
            projectsArray.add(project);
        }
        for (int cc = 0; cc < expectedSize; cc++) {
            int projectId = (cc % NumProjects);
            TestMtoMInstance instance = new TestMtoMInstance(instancesAgg.getLogicalId() + "/" + cc, "instance" + cc,
                    projectBase + projectId, "small", instanceType);
            TestMtoMVolume volume = new TestMtoMVolume(volumesAgg.getLogicalId() + "/" + cc, "volume" + cc,
                    projectBase + projectId, "/dev/sda", volumeType);

            TestMtoMProject project = (TestMtoMProject) projectsArray.get(projectId);
            addMtoMInstanceToMtoMVolume(instance, volume);
            addMtoMInstanceToMtoMProject(instance, project);
            addMtoMVolumeToMtoMProject(volume, project);
            instancesArray.add(instance);
            volumesArray.add(volume);
        }
        // Update the GAs
        UpdateResult instanceTestSortedRes = new UpdateResult(instancesArray, instancesArray, null, null);
        UpdateResult volumeTestSortedRes = new UpdateResult(volumesArray, volumesArray, null, null);
        UpdateResult projectTestSortedRes = new UpdateResult(projectsArray, volumesArray, null, null);
        // force the ignore to false for testing (and kicks)

        aggregationManager.updateGroundedAggregation(session, instancesAgg, instanceTestSortedRes);
        aggregationManager.updateGroundedAggregation(session, volumesAgg, volumeTestSortedRes);
        aggregationManager.updateGroundedAggregation(session, projectsAgg, projectTestSortedRes);
    }


    protected void createConnectedGroundedAggregationsWithItems(final Provider provider, final Session session,
            final int expectedSize)
            throws NoSuchAggregationException, NoSuchSessionException, LogicalIdAlreadyExistsException {

        // Create the Grounded Aggregations
        String projectBase = "hplb";
        ItemType instanceType = createItemType(TestTypeInstance.TYPE_ID);
        ItemType volumeType = createItemType(TestTypeVolume.TYPE_ID);
        ItemType projectType = createItemType(TestTypeProject.TYPE_ID);
        Aggregation instancesAgg =
                createConnectedInstancesGroundedAggregation(session, provider, instanceType, expectedSize);
        Aggregation volumesAgg = createConnectedVolumesGroundedAggregation(session, provider, volumeType, expectedSize);
        Aggregation projectsAgg =
                createConnectedProjectsGroundedAggregation(session, provider, projectType, NumProjects);
        // Now populate them with data
        ArrayList<Item> instancesArray = new ArrayList<Item>(expectedSize);
        ArrayList<Item> volumesArray = new ArrayList<Item>(expectedSize);
        ArrayList<Item> projectsArray = new ArrayList<Item>(NumProjects);
        for (int cc = 0; cc < NumProjects; cc++) {
            TestTypeProject project = new TestTypeProject(projectsAgg.getLogicalId() + "/" + cc, "project" + cc,
                    projectBase + cc, cc, projectType);
            projectsArray.add(project);
        }
        for (int cc = 0; cc < expectedSize; cc++) {
            int projectId = (cc % NumProjects);
            TestTypeInstance instance = new TestTypeInstance(instancesAgg.getLogicalId() + "/" + cc, "instance" + cc,
                    projectBase + projectId, "small", instanceType);
            TestTypeVolume volume = new TestTypeVolume(volumesAgg.getLogicalId() + "/" + cc, "volume" + cc,
                    projectBase + projectId, "/dev/sda", volumeType);

            TestTypeProject project = (TestTypeProject) projectsArray.get(projectId);
            addConnectedInstanceToConnectedVolume(instance, volume, "");
            addConnectedInstanceToConnectedProject(instance, project, "");
            addConnectedVolumeToConnectedProject(volume, project, "");
            instancesArray.add(instance);
            volumesArray.add(volume);
        }
        // Update the GAs
        UpdateResult instanceTestSortedRes = new UpdateResult(instancesArray, instancesArray, null, null);
        UpdateResult volumeTestSortedRes = new UpdateResult(volumesArray, volumesArray, null, null);
        UpdateResult projectTestSortedRes = new UpdateResult(projectsArray, volumesArray, null, null);
        // force the ignore to false for testing (and kicks)

        aggregationManager.updateGroundedAggregation(session, instancesAgg, instanceTestSortedRes);
        aggregationManager.updateGroundedAggregation(session, volumesAgg, volumeTestSortedRes);
        aggregationManager.updateGroundedAggregation(session, projectsAgg, projectTestSortedRes);
    }


    //
    // Additional sanity checking of the relations
    private void checkInstanceVolumeRelations(final int numElements, final QueryResult instanceResult,
            final QueryResult volumeResult) {
        for (int count = 0; count < numElements; count++) {
            QueryResultElement instanceResultElement = instanceResult.getElements().get(count);
            Set<String> instanceRelatesTo = instanceResultElement.getRelations();
            Fibre instanceEntity = instanceResultElement.getEntity();
            String instanceLogicalId = instanceEntity.getLogicalId();
            assertTrue("Instance result element not instance logical id", instanceLogicalId.contains("instance"));
            assertTrue("Instance result element not instance type id", instanceEntity.getTypeId().contains("instance"));

            QueryResultElement volumeResultElement = volumeResult.getElements().get(count);
            Set<String> volumeRelatesTo = volumeResultElement.getRelations();
            Fibre volumeEntity = volumeResultElement.getEntity();
            String volumeLogicalId = volumeEntity.getLogicalId();
            assertTrue("Volume result element not volume logical id", volumeLogicalId.contains("volume"));
            assertTrue("Volume result element not volume type id", volumeEntity.getTypeId().contains("volume"));

            assertTrue("Instance result element did not relate to volume result element",
                    instanceRelatesTo.contains(volumeLogicalId));
            assertTrue("Volume result element did not relate to instance result element",
                    volumeRelatesTo.contains(instanceLogicalId));

            // Instances and volumes must both relate to projects
            boolean instanceRelatesToProject = false;
            for (String id : instanceRelatesTo) {
                if (id.contains("project")) {
                    instanceRelatesToProject = true;
                }
            }
            assertTrue("Instance result element did not relate to a project", instanceRelatesToProject);

            boolean volumeRelatesToProject = false;
            for (String id : volumeRelatesTo) {
                if (id.contains("project")) {
                    volumeRelatesToProject = true;
                }
            }
            assertTrue("Instance result element did not relate to a project", volumeRelatesToProject);
        }
    }

    private void checkRelations(final int numElements, final int expectedNumRelations,
            final boolean resultContainsItems, final List<QueryResult> queryResults,
            final boolean restrictProjectRelations) {
        QueryResult instanceResult = null;
        QueryResult volumeResult = null;
        for (QueryResult queryResult : queryResults) {
            if (queryResult.getLogicalId().contains("instance")) {
                instanceResult = queryResult;
            }
            if (queryResult.getLogicalId().contains("volume")) {
                volumeResult = queryResult;
            }
            if (queryResult.getLogicalId().contains("project")) {
                // Check project relations
                for (QueryResultElement resultElement : queryResult.getElements()) {
                    Fibre resultEntity = resultElement.getEntity();
                    boolean relatedToInstance = false;
                    boolean relatedToVolume = false;
                    int numberOfRelations = resultElement.getRelations().size();
                    LOG.info("Project related to " + numberOfRelations + " restrictProjectRelations="
                            + restrictProjectRelations);
                    for (String relatedTo : resultElement.getRelations()) {
                        if (relatedTo.contains("instance")) {
                            relatedToInstance = true;
                        }
                        if (relatedTo.contains("volume")) {
                            relatedToVolume = true;
                        }
                    }
                    assertTrue("Project was not related to an Instance " + resultEntity.getLogicalId(),
                            relatedToInstance);
                    if (restrictProjectRelations) {
                        assertFalse("Project was related to a Volume " + resultEntity.getLogicalId(), relatedToVolume);
                    } else {
                        assertTrue("Project was not related to a Volume " + resultEntity.getLogicalId(),
                                relatedToVolume);
                    }
                }
                continue;
            }

            int numRelations = 0;
            assertEquals("Number of Elements in Result incorrect", numElements, queryResult.getElements().size());
            for (QueryResultElement resultElement : queryResult.getElements()) {
                Fibre resultEntity = resultElement.getEntity();
                numRelations += resultElement.getRelations().size();
                assertFalse("Relationships empty for " + resultEntity.getLogicalId(),
                        resultElement.getRelations().isEmpty());
                assertEquals("Result element not an item", resultContainsItems, resultEntity.isItem());
            }
            assertEquals("Relationships incorrect no of fibres " + queryResult.getLogicalId(), expectedNumRelations,
                    numRelations);
        }

        checkInstanceVolumeRelations(numElements, instanceResult, volumeResult);
    }

    enum RelationType {
        // ToOne, MtoM,
        Connected
    }

    private void doRelationshipsTest(final String testName, final String groupBy, final int aggregateSize,
            final boolean createSecondDa, final RelationType relationType, final Set<String> projectNotRelatedToTypes,
            final boolean useVisitedIds, final boolean useMultipleThreads)
            throws CheckedLoomException, IllegalAccessException, InvocationTargetException {
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Session session = createSession();
        Provider provider = createProvider();
        switch (relationType) {
            // case ToOne:
            // createGroundedAggregationsWithItems(session, NumItems);
            // break;
            //
            // case MtoM:
            // createMtoMGroundedAggregationsWithItems(session, NumItems);
            // break;

            case Connected:
                createConnectedGroundedAggregationsWithItems(provider, session, NumItems);
                break;
        }

        // Assume tapestry is for every Grounded Aggregation.
        // Calculate Derived Aggregations for every Grounded Aggregation in the model
        List<QueryResult> queryResults = new ArrayList<>(3);
        List<Set<String>> notRelatedToTypesList = new ArrayList<>(queryResults.size());
        for (Aggregation groundedAggregation : aggregationManager.listGroundedAggregations(session)) {
            boolean isProject = groundedAggregation.getLogicalId().contains("project");
            Collection<Fibre> grouped =
                    GroupingCalculator.GroupEntities(session, aggregationManager, groundedAggregation, groupBy);
            Aggregation derivedAggregation = AggregationsCalculator.AggregateEntities(session, 0, aggregationManager,
                    groundedAggregation.getLogicalId(), groundedAggregation.getTypeId(), groundedAggregation.getName(),
                    groundedAggregation.getDescription(), grouped, new String[] {groundedAggregation.getLogicalId()},
                    isProject ? 0 : aggregateSize); // Don't
                                                    // aggregate
                                                    // projects
            QueryResult queryResult = new QueryResult(derivedAggregation);
            queryResult.setItemType(new ItemType(derivedAggregation.getTypeId()));
            queryResults.add(queryResult);
            notRelatedToTypesList.add(isProject ? projectNotRelatedToTypes : null);
        }

        ArrayList<Aggregation> extraDAs = new ArrayList<>(2);
        if (createSecondDa) {
            // Create another set of threads in simulated tapestry for instances and volumes
            for (Aggregation groundedAggregation : aggregationManager.listGroundedAggregations(session)) {
                if (groundedAggregation.getLogicalId().contains("project")) {
                    continue;
                }
                Aggregation derivedAggregation = AggregationsCalculator.AggregateEntities(session, 1,
                        aggregationManager, groundedAggregation.getLogicalId(), groundedAggregation.getTypeId(),
                        groundedAggregation.getName(), groundedAggregation.getDescription(),
                        groundedAggregation.getElements(), new String[] {groundedAggregation.getLogicalId()}, NumItems);
                extraDAs.add(derivedAggregation);
            }
        }

        //
        // Extract the Connected Relationships for all entities in the model.
        List<Aggregation> gas = aggregationManager.listGroundedAggregations(session);
        // Now compute all of the relationships
        relationshipCalculator.doCalculateRelationshipsMultiple(session, queryResults, notRelatedToTypesList, gas,
                false, useVisitedIds, useMultipleThreads);
        if (LOG.isDebugEnabled()) {
            for (QueryResult queryResult : queryResults) {
                LOG.debug(testName + ": Query result with relationships: " + queryResult);
            }
        }

        //
        // Sanity check the results
        boolean aggregated = aggregateSize > 0;
        int numElements = (groupBy == null) ? (aggregateSize > 0 && aggregateSize < NumItems) ? aggregateSize : NumItems
                : (aggregateSize > 0 && aggregateSize < NumProjects) ? aggregateSize : NumProjects;
        boolean resultContainsItems = numElements == NumItems;
        // int expectedNumRelations = createSecondDa ?
        // (numElements == NumItems ? 2 * numElements : numElements + NumItems + NumProjects) :
        // 2 * numElements;
        int expectedNumRelations = aggregated ? ((groupBy == null) ? numElements + 2 * NumProjects /*
                                                                                                    * Items
                                                                                                    * spread
                                                                                                    * across
                                                                                                    * projects
                                                                                                    */
                : numElements + NumProjects) /* Groups match number of projects */
                : 2 * numElements;
        int expectedNumRelations2Da = aggregated ? expectedNumRelations + 2 * NumItems
                : ((groupBy == null) ? expectedNumRelations : numElements + 2 * NumItems + NumProjects);
        checkRelations(numElements, createSecondDa ? expectedNumRelations2Da : expectedNumRelations,
                resultContainsItems, queryResults, projectNotRelatedToTypes != null);

        if (createSecondDa) {
            //
            // Now delete the extra DAs
            for (Aggregation da : extraDAs) {
                aggregationManager.deleteAggregation(session, da.getLogicalId());
            }

            //
            // .. and recompute and recheck the relations
            relationshipCalculator.doCalculateRelationshipsMultiple(session, queryResults, notRelatedToTypesList, gas,
                    false, useVisitedIds, useMultipleThreads);
            if (LOG.isDebugEnabled()) {
                for (QueryResult queryResult : queryResults) {
                    LOG.debug(testName + ": Query result with relationships after deletion of DA: " + queryResult);
                } // case ToOne:
                // createGroundedAggregationsWithItems(session, NumItems);
                // break;
                //
                // case MtoM:
                // createMtoMGroundedAggregationsWithItems(session, NumItems);
                // break;
            }
            checkRelations(numElements, expectedNumRelations, resultContainsItems, queryResults,
                    projectNotRelatedToTypes != null);
        }


        //
        // Now delete all of the remaining derived aggregations to simulate an empty tapestry.
        // Hopefully now all of the relationships are empty.
        aggregationManager.deleteAllDerivedAggregations(session);
        // Now compute all of the relationships
        relationshipCalculator.doCalculateRelationshipsMultiple(session, queryResults, notRelatedToTypesList, gas,
                false, useVisitedIds, useMultipleThreads);
        if (LOG.isDebugEnabled()) {
            for (QueryResult queryResult : queryResults) {
                LOG.debug(testName + ": Query result without relationships: " + queryResult);
            }
        }
        // Check the relations are now empty
        for (QueryResult queryResult : queryResults) {
            for (QueryResultElement resultElement : queryResult.getElements()) {
                Fibre resultEntity = resultElement.getEntity();
                assertTrue("Relationships not empty after deleting all DAs for " + resultEntity.getLogicalId(),
                        resultElement.getRelations().isEmpty());
            }
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Unit tests for establishing relationships between Items
    // /////////////////////////////////////////////////////////////////////////////////////////////

    private void checkConnectedMap(final String relationName, final Map<String, Item> connectedItemsMap,
            final Item item, final Item other) {
        String typeId = item.getTypeId();
        assertNotNull("connectedItemsMap was null for " + typeId, connectedItemsMap);
        assertEquals("Incorrect no. Connected Instances in Map for " + typeId, 1, connectedItemsMap.size());
        Item connectedItem = connectedItemsMap.get(other.getLogicalId());
        assertNotNull("connected Item not connected for relationship " + relationName + " " + typeId, connectedItem);
        assertTrue("Incorrect Connected Item for relationship " + relationName + " " + typeId,
                other.equals(connectedItem));
    }

    @Test
    public void testInterItemRelationships() {
        ItemType instanceType = createItemType(TestTypeInstance.TYPE_ID);
        ItemType volumeType = createItemType(TestTypeVolume.TYPE_ID);

        TestTypeInstance instance =
                new TestTypeInstance("/instances/instance1", "instance1", "myproject", "small", instanceType);
        TestTypeVolume volume = new TestTypeVolume("/volumes/volume1", "volume1", "myproject", "/dev/sda", volumeType);

        String relationName = RelationshipUtil.getRelationshipNameBetweenItems(instance, volume, "");
        String extraRelationName = "boot";

        //
        // Connect instance and volume with two relationships.
        instance.addConnectedRelationships(volume, "");
        instance.addConnectedRelationshipsWithName(extraRelationName, volume);

        // Check getAllConnectedItems
        Collection<Item> connectedVolumes = instance.getAllConnectedItems();
        assertEquals("Incorrect number of Connected Volumes", 1, connectedVolumes.size());
        Item connectedVolume = connectedVolumes.iterator().next();
        assertTrue("Connected volume was not the expected volume", volume.equals(connectedVolume));

        Collection<Item> connectedInstances = volume.getAllConnectedItems();
        assertEquals("Incorrect number of Connected Instances", 1, connectedInstances.size());
        Item connectedInstance = connectedInstances.iterator().next();
        assertTrue("Connected instance was not the expected instance", instance.equals(connectedInstance));

        // Check getConnectedItemMapWithRelationshipName
        Map<String, Item> connectedVolumesMap = instance.getConnectedItemMapWithRelationshipName(relationName);
        checkConnectedMap(relationName, connectedVolumesMap, instance, volume);
        connectedVolumesMap = instance.getConnectedItemMapWithRelationshipName(extraRelationName);
        checkConnectedMap(extraRelationName, connectedVolumesMap, instance, volume);

        Map<String, Item> connectedInstancesMap = volume.getConnectedItemMapWithRelationshipName(relationName);
        checkConnectedMap(relationName, connectedInstancesMap, volume, instance);
        connectedInstancesMap = volume.getConnectedItemMapWithRelationshipName(extraRelationName);
        checkConnectedMap(extraRelationName, connectedInstancesMap, volume, instance);

        // Check getConnectedRelationships
        Map<String, Map<String, Item>> allConnectedVolumesMap = instance.getConnectedRelationships();
        assertEquals("Incorrect number of Connected Relationship types for instance", 2, allConnectedVolumesMap.size());
        connectedVolumesMap = allConnectedVolumesMap.get(relationName);
        checkConnectedMap(relationName, connectedVolumesMap, instance, volume);
        connectedVolumesMap = allConnectedVolumesMap.get(extraRelationName);
        checkConnectedMap(extraRelationName, connectedVolumesMap, instance, volume);

        Map<String, Map<String, Item>> allConnectedInstancesMap = volume.getConnectedRelationships();
        assertEquals("Incorrect number of Connected Relationship types for volume", 2, allConnectedInstancesMap.size());
        connectedInstancesMap = allConnectedInstancesMap.get(relationName);
        checkConnectedMap(relationName, connectedInstancesMap, volume, instance);
        connectedInstancesMap = allConnectedInstancesMap.get(extraRelationName);
        checkConnectedMap(extraRelationName, connectedInstancesMap, volume, instance);

        //
        // Remove extra relationship
        instance.removeConnectedRelationshipsWithName(extraRelationName, volume);

        // Check getConnectedItemMapWithRelationshipName
        connectedVolumesMap = instance.getConnectedItemMapWithRelationshipName(relationName);
        checkConnectedMap(relationName, connectedVolumesMap, instance, volume);
        connectedVolumesMap = instance.getConnectedItemMapWithRelationshipName(extraRelationName);
        assertTrue("Extra relation not removed from instance",
                connectedVolumesMap == null || connectedVolumesMap.size() == 0);

        connectedInstancesMap = volume.getConnectedItemMapWithRelationshipName(relationName);
        checkConnectedMap(relationName, connectedInstancesMap, volume, instance);
        connectedInstancesMap = volume.getConnectedItemMapWithRelationshipName(extraRelationName);
        assertTrue("Extra relation not removed from volume", isMapEmpty(connectedInstancesMap));

        //
        // Remove other relationship
        instance.removeConnectedRelationships(volume, "");

        // Check getConnectedItemMapWithRelationshipName
        connectedVolumesMap = instance.getConnectedItemMapWithRelationshipName(relationName);
        assertTrue("Relation not removed from instance", isMapEmpty(connectedVolumesMap));

        connectedInstancesMap = volume.getConnectedItemMapWithRelationshipName(relationName);
        assertTrue("Relation not removed from volume", isMapEmpty(connectedInstancesMap));
    }

    private <K, V> boolean isMapEmpty(final Map<K, V> map) {
        return map == null || map.size() == 0;
    }

    @Test
    public void testRemoveAllInterItemRelationships() {
        ItemType instanceType = createItemType(TestTypeInstance.TYPE_ID);
        ItemType volumeType = createItemType(TestTypeVolume.TYPE_ID);

        TestTypeInstance instance =
                new TestTypeInstance("/instances/instance1", "instance1", "myproject", "small", instanceType);
        TestTypeVolume volume = new TestTypeVolume("/volumes/volume1", "volume1", "myproject", "/dev/sda", volumeType);

        String relationName = RelationshipUtil.getRelationshipNameBetweenItems(instance, volume, "");
        String extraRelationName = "boot";

        //
        // Connect instance and volume with two relationships.
        instance.addConnectedRelationships(volume, "");
        instance.addConnectedRelationshipsWithName(extraRelationName, volume);

        //
        // Disconnect all extra relations on instance, which should have corresponding effect on
        // volume.
        instance.removeAllConnectedRelationshipsWithRelationshipName(extraRelationName);

        // Check getConnectedItemMapWithRelationshipName
        Map<String, Item> connectedVolumesMap = instance.getConnectedItemMapWithRelationshipName(relationName);
        checkConnectedMap(relationName, connectedVolumesMap, instance, volume);
        connectedVolumesMap = instance.getConnectedItemMapWithRelationshipName(extraRelationName);
        assertTrue("Extra relation not removed from instance", isMapEmpty(connectedVolumesMap));

        Map<String, Item> connectedInstancesMap = volume.getConnectedItemMapWithRelationshipName(relationName);
        checkConnectedMap(relationName, connectedInstancesMap, volume, instance);
        connectedInstancesMap = volume.getConnectedItemMapWithRelationshipName(extraRelationName);
        assertTrue("Extra relation not removed from volume", isMapEmpty(connectedInstancesMap));

        //
        // Remove all relationship
        instance.removeAllConnectedRelationships();

        // Check getConnectedItemMapWithRelationshipName
        connectedVolumesMap = instance.getConnectedItemMapWithRelationshipName(relationName);
        assertTrue("Relation not removed from instance", isMapEmpty(connectedVolumesMap));

        connectedInstancesMap = volume.getConnectedItemMapWithRelationshipName(relationName);
        assertTrue("Relation not removed from volume", isMapEmpty(connectedInstancesMap));
    }

    @Test
    public void testIntrospectionOnInterItemRelationships() {
        ItemType instanceType = createItemType(TestTypeInstance.TYPE_ID);
        ItemType volumeType = createItemType(TestTypeVolume.TYPE_ID);

        TestTypeInstance instance =
                new TestTypeInstance("/instances/instance1", "instance1", "myproject", "small", instanceType);
        TestTypeVolume volume1 = new TestTypeVolume("/volumes/volume1", "volume1", "myproject", "/dev/sda", volumeType);

        String relationName = RelationshipUtil.getRelationshipNameBetweenItems(instance, volume1, "");
        String extraRelationName = "boot";

        //
        // Connect instance and volume with two relationships.
        instance.addConnectedRelationships(volume1, "");
        instance.addConnectedRelationshipsWithName(extraRelationName, volume1);

        //
        // Check introspection for no relationships
        Object relatedObject = FibreIntrospectionUtils.introspectProperty("dummy", instance, null);
        assertNull("Introspection returned a related Object for non-existent relationship", relatedObject);

        //
        // Check introspection for relationships to a single object
        relatedObject = FibreIntrospectionUtils.introspectProperty(relationName, instance, null);
        assertNotNull("Introspected related Object was null", relatedObject);
        assertTrue("Related Object was not an Item", relatedObject instanceof Item);
        Item relatedItem = (Item) relatedObject;
        assertEquals("Incorrect related Item returned from introspection", volume1, relatedItem);

        //
        // Check introspection for relationships to a multiple objects
        TestTypeVolume volume2 = new TestTypeVolume("/volumes/volume2", "volume2", "myproject", "/dev/sda", volumeType);
        instance.addConnectedRelationships(volume2, "");
        Object relatedObjects = FibreIntrospectionUtils.introspectProperty(relationName, instance, null);
        assertNotNull("Introspected related Objects was null", relatedObjects);
        assertTrue("Related Object was not a Collection", relatedObjects instanceof Collection);
        Collection<Item> relatedItems = (Collection<Item>) relatedObjects;
        assertEquals("Incorrect number of related Items returned from introspection", 2, relatedItems.size());
        List<Item> relatedItemsList = new ArrayList<Item>(relatedItems);

        Item relatedItem1 =
                relatedItemsList.get(0).getLogicalId().equals(volume1.getLogicalId()) ? relatedItemsList.get(0)
                        : relatedItemsList.get(1);
        Item relatedItem2 =
                relatedItemsList.get(1).getLogicalId().equals(volume2.getLogicalId()) ? relatedItemsList.get(1)
                        : relatedItemsList.get(0);
        assertEquals("Incorrect related Item returned from introspection", volume1, relatedItem1);
        assertEquals("Incorrect related Item returned from introspection", volume2, relatedItem2);
    }

    @Test
    public void testChangesInItemRelationships() {
        ItemType instanceType = createItemType(TestTypeInstance.TYPE_ID);
        ItemType volumeType = createItemType(TestTypeVolume.TYPE_ID);

        TestTypeInstance instance =
                new TestTypeInstance("/instances/instance1", "instance1", "myproject", "small", instanceType);
        TestTypeInstance otherInstance =
                new TestTypeInstance("/instances/instance1", "instance1", "myproject", "small", instanceType);
        TestTypeVolume volume1 = new TestTypeVolume("/volumes/volume1", "volume1", "myproject", "/dev/sda", volumeType);
        TestTypeVolume volume2 = new TestTypeVolume("/volumes/volume2", "volume2", "myproject", "/dev/sda", volumeType);

        RelationshipUtil.getRelationshipNameBetweenItems(instance, volume1, "");
        //
        // Connect instance and volume with two relationships.
        instance.addConnectedRelationships(volume1, "");
        instance.addConnectedRelationships(volume2, "");

        otherInstance.addConnectedRelationships(volume1, "");
        assertTrue("Relationships should be different", instance.isDifferentFrom(otherInstance));

        otherInstance.addConnectedRelationships(volume2, "");
        assertFalse("Relationships should be same", instance.isDifferentFrom(otherInstance));
    }


    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Tests for OneToMany relationships
    // /////////////////////////////////////////////////////////////////////////////////////////////

    // /**
    // * Test relationships calculation for non-aggregated queries.
    // */
    // @Test
    // public void testNonAggregatedQueryWithRelationships() throws CheckedLoomException,
    // IllegalAccessException,
    // InvocationTargetException {
    // LOG.info("testNonAggregatedQueryWithRelationships start");
    // doRelationshipsTest("testNonAggregatedQueryWithRelationships", null, 0, false,
    // RelationType.ToOne, null, false,
    // false);
    // LOG.info("testNonAggregatedQueryWithRelationships end");
    // }
    //
    // /**
    // * Test relationships calculation for aggregated queries.
    // */
    // @Test
    // public void testAggregatedQueryWithRelationships() throws CheckedLoomException,
    // IllegalAccessException,
    // InvocationTargetException {
    // LOG.info("testAggregatedQueryWithRelationships start");
    // doRelationshipsTest("testAggregatedQueryWithRelationships", null, 2, false,
    // RelationType.ToOne, null, false,
    // false);
    // LOG.info("testAggregatedQueryWithRelationships end");
    // }
    //
    // /**
    // * Test relationships calculation for non-aggregated groups.
    // */
    // @Test
    // public void testNonAggregatedGroupQueryWithRelationships() throws CheckedLoomException,
    // IllegalAccessException,
    // InvocationTargetException {
    // LOG.info("testNonAggregatedGroupQueryWithRelationships start");
    // doRelationshipsTest("testNonAggregatedGroupQueryWithRelationships", "ProjectId", 0, false,
    // RelationType.ToOne,
    // null, false, false);
    // LOG.info("testNonAggregatedGroupQueryWithRelationships end");
    // }
    //
    // /**
    // * Test relationships calculation for aggregated groups.
    // */
    // @Test
    // public void testAggregatedGroupQueryWithRelationships() throws CheckedLoomException,
    // IllegalAccessException,
    // InvocationTargetException {
    // LOG.info("testAggregatedGroupQueryWithRelationships start");
    // doRelationshipsTest("testAggregatedGroupQueryWithRelationships", "ProjectId", 2, false,
    // RelationType.ToOne,
    // null, false, false);
    // LOG.info("testAggregatedGroupQueryWithRelationships end");
    // }
    //
    // /**
    // * Test relationships calculation for non-aggregated queries.
    // */
    // @Test
    // public void testNonAggregatedQueryTwoDasWithRelationships() throws CheckedLoomException,
    // IllegalAccessException,
    // InvocationTargetException {
    // LOG.info("testNonAggregatedQueryTwoDasWithRelationships start");
    // doRelationshipsTest("testNonAggregatedQueryTwoDasWithRelationships", null, 0, true,
    // RelationType.ToOne, null,
    // false, false);
    // LOG.info("testNonAggregatedQueryTwoDasWithRelationships end");
    // }
    //
    // /**
    // * Test relationships calculation for aggregated queries.
    // */
    // @Test
    // public void testAggregatedQueryTwoDasWithRelationships() throws CheckedLoomException,
    // IllegalAccessException,
    // InvocationTargetException {
    // LOG.info("testAggregatedQueryTwoDasWithRelationships start");
    // doRelationshipsTest("testAggregatedQueryTwoDasWithRelationships", null, 2, true,
    // RelationType.ToOne, null,
    // false, false);
    // LOG.info("testAggregatedQueryTwoDasWithRelationships end");
    // }
    //
    // /**
    // * Test relationships calculation for non-aggregated groups.
    // */
    // @Test
    // public void testNonAggregatedGroupQueryTwoDasWithRelationships() throws CheckedLoomException,
    // IllegalAccessException, InvocationTargetException {
    // LOG.info("testNonAggregatedGroupQueryTwoDasWithRelationships start");
    // doRelationshipsTest("testNonAggregatedGroupQueryTwoDasWithRelationships", "ProjectId", 0,
    // true,
    // RelationType.ToOne, null, false, false);
    // LOG.info("testNonAggregatedGroupQueryTwoDasWithRelationships end");
    // }
    //
    // /**
    // * Test relationships calculation for aggregated groups.
    // */
    // @Test
    // public void testAggregatedGroupQueryTwoDasWithRelationships() throws CheckedLoomException,
    // IllegalAccessException,
    // InvocationTargetException {
    // LOG.info("testAggregatedGroupQueryTwoDasWithRelationships start");
    // doRelationshipsTest("testAggregatedGroupQueryTwoDasWithRelationships", "ProjectId", 2, true,
    // RelationType.ToOne, null, false, false);
    // LOG.info("testAggregatedGroupQueryTwoDasWithRelationships end");
    // }
    //

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Tests for ManyToMany relationships
    // /////////////////////////////////////////////////////////////////////////////////////////////
    //
    // /**
    // * Test MtoM relationships calculation for non-aggregated queries.
    // */
    // @Test
    // public void testMtoMNonAggregatedQueryWithRelationships() throws CheckedLoomException,
    // IllegalAccessException,
    // InvocationTargetException {
    // LOG.info("testMtoMNonAggregatedQueryWithRelationships start");
    // doRelationshipsTest("testMtoMNonAggregatedQueryWithRelationships", null, 0, false,
    // RelationType.MtoM, null,
    // false, false);
    // LOG.info("testMtoMNonAggregatedQueryWithRelationships end");
    // }
    //
    // /**
    // * Test MtoM relationships calculation for aggregated queries.
    // */
    // @Test
    // @Ignore
    // public void testMtoMAggregatedQueryWithRelationships() throws CheckedLoomException,
    // IllegalAccessException,
    // InvocationTargetException {
    // LOG.info("testMtoMAggregatedQueryWithRelationships start");
    // doRelationshipsTest("testMtoMAggregatedQueryWithRelationships", null, 2, false,
    // RelationType.MtoM, null, false,
    // false);
    // LOG.info("testMtoMAggregatedQueryWithRelationships end");
    // }
    //
    // /**
    // * Test MtoM relationships calculation for non-aggregated groups.
    // */
    // @Test
    // @Ignore
    // public void testMtoMNonAggregatedGroupQueryWithRelationships() throws CheckedLoomException,
    // IllegalAccessException,
    // InvocationTargetException {
    // LOG.info("testMtoMNonAggregatedGroupQueryWithRelationships start");
    // doRelationshipsTest("testMtoMNonAggregatedGroupQueryWithRelationships", "ProjectId", 0,
    // false,
    // RelationType.MtoM, null, false, false);
    // LOG.info("testMtoMNonAggregatedGroupQueryWithRelationships end");
    // }
    //
    // /**
    // * Test MtoM relationships calculation for aggregated groups.
    // */
    // @Test
    // @Ignore
    // public void testMtoMAggregatedGroupQueryWithRelationships() throws CheckedLoomException,
    // IllegalAccessException,
    // InvocationTargetException {
    // LOG.info("testMtoMAggregatedGroupQueryWithRelationships start");
    // doRelationshipsTest("testMtoMAggregatedGroupQueryWithRelationships", "ProjectId", 2, false,
    // RelationType.MtoM,
    // null, false, false);
    // LOG.info("testMtoMAggregatedGroupQueryWithRelationships end");
    // }
    //
    // /**
    // * Test MtoM relationships calculation for non-aggregated queries.
    // */
    // @Test
    // @Ignore
    // public void testMtoMNonAggregatedQueryTwoDasWithRelationships() throws CheckedLoomException,
    // IllegalAccessException, InvocationTargetException {
    // LOG.info("testMtoMNonAggregatedQueryTwoDasWithRelationships start");
    // doRelationshipsTest("testMtoMNonAggregatedQueryTwoDasWithRelationships", null, 0, true,
    // RelationType.MtoM,
    // null, false, false);
    // LOG.info("testMtoMNonAggregatedQueryTwoDasWithRelationships end");
    // }
    //
    // /**
    // * Test MtoM relationships calculation for aggregated queries.
    // */
    // @Test
    // @Ignore
    // public void testMtoMAggregatedQueryTwoDasWithRelationships() throws CheckedLoomException,
    // IllegalAccessException,
    // InvocationTargetException {
    // LOG.info("testMtoMAggregatedQueryTwoDasWithRelationships start");
    // doRelationshipsTest("testMtoMAggregatedQueryTwoDasWithRelationships", null, 2, true,
    // RelationType.MtoM, null,
    // false, false);
    // LOG.info("testMtoMAggregatedQueryTwoDasWithRelationships end");
    // }
    //
    // /**
    // * Test MtoM relationships calculation for non-aggregated groups.
    // */
    // @Test
    // @Ignore
    // public void testMtoMNonAggregatedGroupQueryTwoDasWithRelationships() throws
    // CheckedLoomException,
    // IllegalAccessException, InvocationTargetException {
    // LOG.info("testMtoMNonAggregatedGroupQueryTwoDasWithRelationships start");
    // doRelationshipsTest("testMtoMNonAggregatedGroupQueryTwoDasWithRelationships", "ProjectId", 0,
    // true,
    // RelationType.MtoM, null, false, false);
    // LOG.info("testMtoMNonAggregatedGroupQueryTwoDasWithRelationships end");
    // }
    //
    // /**
    // * Test MtoMA relationships calculation for aggregated groups.
    // */
    // @Test
    // @Ignore
    // public void testMtoMAggregatedGroupQueryTwoDasWithRelationships() throws
    // CheckedLoomException,
    // IllegalAccessException, InvocationTargetException {
    // LOG.info("testMtoMAggregatedGroupQueryTwoDasWithRelationships start");
    // doRelationshipsTest("testMtoMAAggregatedGroupQueryTwoDasWithRelationships", "ProjectId", 2,
    // true,
    // RelationType.MtoM, null, false, false);
    // LOG.info("testMtoMAggregatedGroupQueryTwoDasWithRelationships end");
    // }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Tests for OneToMany relationships with Not Friends With
    // /////////////////////////////////////////////////////////////////////////////////////////////
    //
    // /**
    // * Test relationships calculation for non-aggregated queries with Not Friends With.
    // */
    // @Test
    // public void testNonAggregatedQueryNotFriendsWith() throws CheckedLoomException,
    // IllegalAccessException,
    // InvocationTargetException {
    // LOG.info("testNonAggregatedQueryNotFriendsWith start");
    // doRelationshipsTest("testNonAggregatedQueryNotFriendsWith", null, 0, false,
    // RelationType.ToOne,
    // ProjectNotFriendsWith, false, false);
    // LOG.info("testNonAggregatedQueryNotFriendsWith end");
    // }
    //


    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Tests for ConnectedTo relationships
    // /////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Test standard-representation relationships calculation for non-aggregated queries.
     */
    @Test
    public void testConnectedNonAggregatedQueryWithRelationships()
            throws CheckedLoomException, IllegalAccessException, InvocationTargetException {
        LOG.info("testConnectedNonAggregatedQueryWithRelationships start");
        doRelationshipsTest("testConnectedNonAggregatedQueryWithRelationships", null, 0, false, RelationType.Connected,
                null, false, false);
        LOG.info("testConnectedNonAggregatedQueryWithRelationships end");
    }

    /**
     * Test standard-representation relationships calculation for aggregated queries.
     */
    @Test
    public void testConnectedAggregatedQueryWithRelationships()
            throws CheckedLoomException, IllegalAccessException, InvocationTargetException {
        LOG.info("testConnectedAggregatedQueryWithRelationships start");
        doRelationshipsTest("testConnectedAggregatedQueryWithRelationships", null, 2, false, RelationType.Connected,
                null, false, false);
        LOG.info("testConnectedAggregatedQueryWithRelationships end");
    }

    /**
     * Test standard-representation relationships calculation for non-aggregated groups.
     */
    @Test
    public void testConnectedNonAggregatedGroupQueryWithRelationships()
            throws CheckedLoomException, IllegalAccessException, InvocationTargetException {
        LOG.info("testConnectedNonAggregatedGroupQueryWithRelationships start");
        doRelationshipsTest("testConnectedNonAggregatedGroupQueryWithRelationships",
                SeparableItem.CORE_NAME + "projectId", 0, false, RelationType.Connected, null, false, false);
        LOG.info("testConnectedNonAggregatedGroupQueryWithRelationships end");
    }

    /**
     * Test standard-representation relationships calculation for aggregated groups.
     */
    @Test
    public void testConnectedAggregatedGroupQueryWithRelationships()
            throws CheckedLoomException, IllegalAccessException, InvocationTargetException {
        LOG.info("testConnectedAggregatedGroupQueryWithRelationships start");
        doRelationshipsTest("testConnectedAggregatedGroupQueryWithRelationships", SeparableItem.CORE_NAME + "projectId",
                2, false, RelationType.Connected, null, false, false);
        LOG.info("testConnectedAggregatedGroupQueryWithRelationships end");
    }

    /**
     * Test standard-representation relationships calculation for non-aggregated queries.
     */
    @Test
    public void testConnectedNonAggregatedQueryTwoDasWithRelationships()
            throws CheckedLoomException, IllegalAccessException, InvocationTargetException {
        LOG.info("testConnectedNonAggregatedQueryTwoDasWithRelationships start");
        doRelationshipsTest("testConnectedNonAggregatedQueryTwoDasWithRelationships", null, 0, true,
                RelationType.Connected, null, false, false);
        LOG.info("testConnectedNonAggregatedQueryTwoDasWithRelationships end");
    }

    /**
     * Test standard-representation relationships calculation for aggregated queries.
     */
    @Test
    public void testConnectedAggregatedQueryTwoDasWithRelationships()
            throws CheckedLoomException, IllegalAccessException, InvocationTargetException {
        LOG.info("testConnectedAggregatedQueryTwoDasWithRelationships start");
        doRelationshipsTest("testConnectedAggregatedQueryTwoDasWithRelationships", null, 2, true,
                RelationType.Connected, null, false, false);
        LOG.info("testConnectedAggregatedQueryTwoDasWithRelationships end");
    }

    /**
     * Test standard-representation relationships calculation for non-aggregated groups.
     */
    @Test
    public void testConnectedNonAggregatedGroupQueryTwoDasWithRelationships()
            throws CheckedLoomException, IllegalAccessException, InvocationTargetException {
        LOG.info("testConnectedNonAggregatedGroupQueryTwoDasWithRelationships start");
        doRelationshipsTest("testConnectedNonAggregatedGroupQueryTwoDasWithRelationships",
                SeparableItem.CORE_NAME + "projectId", 0, true, RelationType.Connected, null, false, false);
        LOG.info("testConnectedNonAggregatedGroupQueryTwoDasWithRelationships end");
    }

    /**
     * Test standard-representation relationships calculation for aggregated groups.
     */
    @Test
    public void testConnectedAggregatedGroupQueryTwoDasWithRelationships()
            throws CheckedLoomException, IllegalAccessException, InvocationTargetException {
        LOG.info("testConnectedAggregatedGroupQueryTwoDasWithRelationships start");
        doRelationshipsTest("testConnectedAggregatedGroupQueryTwoDasWithRelationships",
                SeparableItem.CORE_NAME + "projectId", 2, true, RelationType.Connected, null, false, false);
        LOG.info("testConnectedAggregatedGroupQueryTwoDasWithRelationships end");
    }



    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Tests for ConnectedTo relationships with multiple threads
    // /////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Test standard-representation relationships calculation for non-aggregated queries.
     */
    @Test
    public void testConnectedNonAggregatedQueryWithRelationshipsThreaded()
            throws CheckedLoomException, IllegalAccessException, InvocationTargetException {
        LOG.info("testConnectedNonAggregatedQueryWithRelationshipsThreaded start");
        doRelationshipsTest("testConnectedNonAggregatedQueryWithRelationshipsThreaded", null, 0, false,
                RelationType.Connected, null, true, true);
        LOG.info("testConnectedNonAggregatedQueryWithRelationshipsThreaded end");
    }

    /**
     * Test standard-representation relationships calculation for aggregated queries.
     */
    @Test
    public void testConnectedAggregatedQueryWithRelationshipsThreaded()
            throws CheckedLoomException, IllegalAccessException, InvocationTargetException {
        LOG.info("testConnectedAggregatedQueryWithRelationshipsThreaded start");
        doRelationshipsTest("testConnectedAggregatedQueryWithRelationshipsThreaded", null, 2, false,
                RelationType.Connected, null, true, true);
        LOG.info("testConnectedAggregatedQueryWithRelationshipsThreaded end");
    }

    /**
     * Test standard-representation relationships calculation for non-aggregated groups.
     */
    @Test
    public void testConnectedNonAggregatedGroupQueryWithRelationshipsThreaded()
            throws CheckedLoomException, IllegalAccessException, InvocationTargetException {
        LOG.info("testConnectedNonAggregatedGroupQueryWithRelationshipsThreaded start");
        doRelationshipsTest("testConnectedNonAggregatedGroupQueryWithRelationshipsThreaded",
                SeparableItem.CORE_NAME + "projectId", 0, false, RelationType.Connected, null, true, true);
        LOG.info("testConnectedNonAggregatedGroupQueryWithRelationshipsThreaded end");
    }

    /**
     * Test standard-representation relationships calculation for aggregated groups.
     */
    @Test
    public void testConnectedAggregatedGroupQueryWithRelationshipsThreaded()
            throws CheckedLoomException, IllegalAccessException, InvocationTargetException {
        LOG.info("testConnectedAggregatedGroupQueryWithRelationshipsThreaded start");
        doRelationshipsTest("testConnectedAggregatedGroupQueryWithRelationshipsThreaded",
                SeparableItem.CORE_NAME + "projectId", 2, false, RelationType.Connected, null, true, true);
        LOG.info("testConnectedAggregatedGroupQueryWithRelationshipsThreaded end");
    }

    /**
     * Test standard-representation relationships calculation for non-aggregated queries.
     */
    @Test
    public void testConnectedNonAggregatedQueryTwoDasWithRelationshipsThreaded()
            throws CheckedLoomException, IllegalAccessException, InvocationTargetException {
        LOG.info("testConnectedNonAggregatedQueryTwoDasWithRelationshipsThreaded start");
        doRelationshipsTest("testConnectedNonAggregatedQueryTwoDasWithRelationshipsThreaded", null, 0, true,
                RelationType.Connected, null, true, true);
        LOG.info("testConnectedNonAggregatedQueryTwoDasWithRelationshipsThreaded end");
    }

    /**
     * Test standard-representation relationships calculation for aggregated queries.
     */
    @Test
    public void testConnectedAggregatedQueryTwoDasWithRelationshipsThreaded()
            throws CheckedLoomException, IllegalAccessException, InvocationTargetException {
        LOG.info("testConnectedAggregatedQueryTwoDasWithRelationshipsThreaded start");
        doRelationshipsTest("testConnectedAggregatedQueryTwoDasWithRelationshipsThreaded", null, 2, true,
                RelationType.Connected, null, true, true);
        LOG.info("testConnectedAggregatedQueryTwoDasWithRelationshipsThreaded end");
    }

    /**
     * Test standard-representation relationships calculation for non-aggregated groups.
     */
    @Test
    public void testConnectedNonAggregatedGroupQueryTwoDasWithRelationshipsThreaded()
            throws CheckedLoomException, IllegalAccessException, InvocationTargetException {
        LOG.info("testConnectedNonAggregatedGroupQueryTwoDasWithRelationshipsThreaded start");
        doRelationshipsTest("testConnectedNonAggregatedGroupQueryTwoDasWithRelationshipsThreaded",
                SeparableItem.CORE_NAME + "projectId", 0, true, RelationType.Connected, null, true, true);
        LOG.info("testConnectedNonAggregatedGroupQueryTwoDasWithRelationshipsThreaded end");
    }

    /**
     * Test standard-representation relationships calculation for aggregated groups.
     */
    @Test
    public void testConnectedAggregatedGroupQueryTwoDasWithRelationshipsThreaded()
            throws CheckedLoomException, IllegalAccessException, InvocationTargetException {
        LOG.info("testConnectedAggregatedGroupQueryTwoDasWithRelationshipsThreaded start");
        doRelationshipsTest("testConnectedAggregatedGroupQueryTwoDasWithRelationshipsThreaded",
                SeparableItem.CORE_NAME + "projectId", 2, true, RelationType.Connected, null, true, true);
        LOG.info("testConnectedAggregatedGroupQueryTwoDasWithRelationshipsThreaded end");
    }



    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Tests for graph traversal
    // /////////////////////////////////////////////////////////////////////////////////////////////

    private void checkGraphResults(final Item item, final Set<String> relations, final int numItems,
            final int numProjects, final boolean restrictProjectRelations, final boolean specific,
            final String otherType) {
        if (item.getLogicalId().contains("instance")) {
            assertEquals("Incorrect no of relations for " + item.getLogicalId(), specific ? 1 : 2, relations.size());
            int numRelationsItems = 0;
            int numRelationsProjects = 0;
            for (String relation : relations) {
                if (relation.contains("volume")) {
                    numRelationsItems++;
                }
                if (relation.contains("project")) {
                    numRelationsProjects++;
                }
            }
            assertEquals("Incorrect no of volumes for " + item.getLogicalId(), specific ? 0 : 1, numRelationsItems);
            assertEquals("Incorrect no of projects for " + item.getLogicalId(), 1, numRelationsProjects);
        }
        if (item.getLogicalId().contains("volume")) {
            assertEquals("Incorrect no of relations for " + item.getLogicalId(), specific ? 1 : 2, relations.size());
            int numRelationsItems = 0;
            int numRelationsProjects = 0;
            for (String relation : relations) {
                if (relation.contains("instance")) {
                    numRelationsItems++;
                }
                if (relation.contains("project")) {
                    numRelationsProjects++;
                }
            }
            assertEquals("Incorrect no of instances for " + item.getLogicalId(), specific ? 0 : 1, numRelationsItems);
            assertEquals("Incorrect no of projects for " + item.getLogicalId(), 1, numRelationsProjects);
        }

        if (item.getLogicalId().contains("project")) {
            int itemsPerProject = numItems / numProjects;
            assertEquals("Incorrect no of relations for " + item.getLogicalId(),
                    specific ? itemsPerProject : 2 * itemsPerProject, relations.size());
            // Check project relations
            boolean relatedToInstance = false;
            boolean relatedToVolume = false;
            int numberOfRelations = relations.size();
            LOG.info("Project related to " + numberOfRelations + " restrictProjectRelations="
                    + restrictProjectRelations);
            for (String relatedTo : relations) {
                if (relatedTo.contains("instance")) {
                    relatedToInstance = true;
                }
                if (relatedTo.contains("volume")) {
                    relatedToVolume = true;
                }
            }
            assertTrue("Project was not related to an Instance " + item.getLogicalId(), relatedToInstance);
            if (restrictProjectRelations || specific) {
                assertFalse("Project was related to a Volume " + item.getLogicalId(), relatedToVolume);
            } else {
                assertTrue("Project was not related to a Volume " + item.getLogicalId(), relatedToVolume);
            }
        }
    }

    private void doGraphTraversalTest(final String testName, final Set<String> projectNotRelatedToTypes,
            final boolean specific) throws CheckedLoomException, IllegalAccessException, InvocationTargetException {
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Session session = createSession();
        Provider provider = createProvider();
        createConnectedGroundedAggregationsWithItems(provider, session, NumItems);

        //
        // Extract the Connected Relationships for all entities in the model.
        List<Aggregation> gas = aggregationManager.listGroundedAggregations(session);
        RelationshipsModelImpl model = new RelationshipsModelImpl();
        model.calculateClassRelationships(gas);
        GraphProcessor graphProcessor = new GraphProcessorImplIter();

        Set<String> relations = new HashSet<>();
        Map<String, Set<String>> relations2 = new HashMap<>();
        Map<String, List<String>> relationPaths = new HashMap<>();

        for (Aggregation agg : gas) {
            boolean isProject = agg.getLogicalId().contains("project");
            // For a specific test, see how instances or volumes relate to projects, or projects
            // relate to an instance
            String otherType = isProject ? "instance" : "project";
            for (Fibre fibre : agg.getElements()) {
                Item item = (Item) fibre;
                relations.clear();
                RelationsReporter relationsReporter = (final Item i, String relType, final Set<String> r,
                        Map<String, Set<String>> r2, final Map<String, List<String>> rp, List<String> p) -> {
                    if (!specific || (specific && i.getTypeId().contains(otherType))) {
                        r.add(i.getLogicalId()); // Report relations to item
                    }
                    Set<String> rel = r2.get(relType);
                    if (rel == null) {
                        rel = new HashSet<>();
                        r2.put(relType, rel);
                    }
                    rel.add(i.getLogicalId());
                    return true;
                };
                graphProcessor.doProcessGraphForItem(item, relations, relations2, relationPaths, relationsReporter,
                        model, isProject ? projectNotRelatedToTypes : null, new HashSet<String>(),
                        new HashSet<String>(), false, null, null, new HashSet<>());


                if (LOG.isDebugEnabled()) {
                    LOG.debug(testName + ": Graph result " + item.getLogicalId() + " relationships: " + relations);
                }

                // Sanity check the results
                checkGraphResults(item, relations, NumItems, NumProjects, projectNotRelatedToTypes != null, specific,
                        otherType);
            }
        }
    }

    @Test
    public void testGraphTraversalAll() throws CheckedLoomException, IllegalAccessException, InvocationTargetException {
        LOG.info("testGraphTraversalAll start");
        doGraphTraversalTest("testGraphTraversalAll", null, false);
        LOG.info("testGraphTraversalAll end");
    }

    @Test
    public void testGraphTraversalSpecifc()
            throws CheckedLoomException, IllegalAccessException, InvocationTargetException {
        LOG.info("testGraphTraversalSpecifc start");
        doGraphTraversalTest("testGraphTraversalSpecifc", null, true);
        LOG.info("testGraphTraversalSpecifc end");
    }
}
