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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.hp.hpl.loom.adapter.LoomUtils;
import com.hp.hpl.loom.adapter.UpdateResult;
import com.hp.hpl.loom.adapter.os.OsInstance;
import com.hp.hpl.loom.adapter.os.OsInstanceType;
import com.hp.hpl.loom.adapter.os.TestTypeInstance;
import com.hp.hpl.loom.adapter.os.TestTypeProject;
import com.hp.hpl.loom.adapter.os.TestTypeUser;
import com.hp.hpl.loom.adapter.os.TestTypeVolume;
import com.hp.hpl.loom.adapter.os.stitch.TestTypeInstanceStitch;
import com.hp.hpl.loom.adapter.os.stitch.TestTypeOwnerStitch;
import com.hp.hpl.loom.exceptions.DuplicateItemTypeException;
import com.hp.hpl.loom.exceptions.InvalidQueryInputException;
import com.hp.hpl.loom.exceptions.ItemPropertyNotFound;
import com.hp.hpl.loom.exceptions.LogicalIdAlreadyExistsException;
import com.hp.hpl.loom.exceptions.NoSuchAggregationException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NoSuchQueryDefinitionException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.NoSuchTapestryDefinitionException;
import com.hp.hpl.loom.exceptions.NoSuchThreadDefinitionException;
import com.hp.hpl.loom.exceptions.NullItemTypeIdException;
import com.hp.hpl.loom.exceptions.OperationException;
import com.hp.hpl.loom.exceptions.RelationPropertyNotFound;
import com.hp.hpl.loom.exceptions.SessionAlreadyExistsException;
import com.hp.hpl.loom.exceptions.ThreadDeletedByDynAdapterUnload;
import com.hp.hpl.loom.manager.itemtype.ItemTypeManager;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.manager.query.QueryManager;
import com.hp.hpl.loom.manager.query.QueryOperation;
import com.hp.hpl.loom.manager.stitcher.ItemEquivalence;
import com.hp.hpl.loom.manager.tapestry.TapestryManager;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.ProviderImpl;
import com.hp.hpl.loom.model.QueryResult;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.stitcher.StitchFunction;
import com.hp.hpl.loom.stitcher.StitcherRulePair;
import com.hp.hpl.loom.tapestry.Operation;
import com.hp.hpl.loom.tapestry.QueryDefinition;
import com.hp.hpl.loom.tapestry.TapestryDefinition;
import com.hp.hpl.loom.tapestry.ThreadDefinition;

public class SimpleRelTest extends RelationshipsCalculatorTest {
    private static final Log LOG = LogFactory.getLog(SimpleRelTest.class);
    @Autowired
    private RelationshipCalculator relationshipCalculator;

    @Autowired
    QueryManager queryManager;

    @Autowired
    TapestryManager tapestryManager;

    @Autowired
    ItemTypeManager itemTypeManager;

    private void createConnectedGroundedAggUser(Session session, Provider provider, int expectedSize)
            throws NoSuchSessionException, LogicalIdAlreadyExistsException, NoSuchAggregationException {
        int NumProjects = 3;
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
        UpdateResult instanceTestSortedRes = new UpdateResult(instancesArray, instancesArray, instancesArray, null);
        UpdateResult volumeTestSortedRes = new UpdateResult(volumesArray, volumesArray, volumesArray, null);
        UpdateResult projectTestSortedRes = new UpdateResult(projectsArray, projectsArray, projectsArray, null);
        // force the ignore to false for testing (and kicks)

        aggregationManager.updateGroundedAggregation(session, instancesAgg, instanceTestSortedRes);
        aggregationManager.updateGroundedAggregation(session, volumesAgg, volumeTestSortedRes);
        aggregationManager.updateGroundedAggregation(session, projectsAgg, projectTestSortedRes);

        for (Item item : projectsArray) {
            item.addMemberOf(projectsAgg);
        }
        for (Item item : instancesArray) {
            item.addMemberOf(instancesAgg);
        }
        for (Item item : volumesArray) {
            item.addMemberOf(volumesAgg);
        }

        // now setup my users
        ItemType userType = createItemType(TestTypeUser.TYPE_ID);

        String logicalId = "/providers/os/connusers";
        String mergedLogicalId = "/providers/os/connusers";
        String name = "UsersGA";
        String description = "Test Standard Users Grounded Aggregation";
        Aggregation usersAgg = createGroundedAggregation(session, provider, userType, logicalId, mergedLogicalId, name,
                description, 3);
        // Now populate them with data
        ArrayList<Item> usersArray = new ArrayList<Item>(3); // one user has access to all the
                                                             // projects, one user is admin to all,
                                                             // and one is admin + access
        for (int i = 0; i < 3; i++) {
            TestTypeUser user = new TestTypeUser(usersAgg.getLogicalId() + "/" + i, "user" + i, userType);
            usersArray.add(user);
            for (int cc = 0; cc < expectedSize; cc++) {
                int projectId = (cc % NumProjects);

                TestTypeProject project = (TestTypeProject) projectsArray.get(projectId);
                project.addConnectedRelationships(user, "admin");
                project.addConnectedRelationships(user, "owner");
                project.addConnectedRelationships(user, "user");
            }
        }
        UpdateResult userTestSortedRes = new UpdateResult(usersArray, usersArray, null, null);
        aggregationManager.updateGroundedAggregation(session, usersAgg, userTestSortedRes);

        try {
            if (itemTypeManager.getItemType(userType.getId()) == null) {
                itemTypeManager.addItemType(provider, userType);
            }
            if (itemTypeManager.getItemType(instanceType.getId()) == null) {
                itemTypeManager.addItemType(provider, instanceType);
            }
            if (itemTypeManager.getItemType(volumeType.getId()) == null) {
                itemTypeManager.addItemType(provider, volumeType);
            }
            if (itemTypeManager.getItemType(projectType.getId()) == null) {
                itemTypeManager.addItemType(provider, projectType);
            }

        } catch (DuplicateItemTypeException | NoSuchProviderException | NullItemTypeIdException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void createConnectedGroundedAggSlim(Session session, Provider provider, int expectedSize)
            throws NoSuchSessionException, LogicalIdAlreadyExistsException, NoSuchAggregationException {

        // Create the Grounded Aggregations

        ItemType instanceStitchType = new ItemType(TestTypeInstanceStitch.TYPE_ID);
        ItemType ownerType = new ItemType(TestTypeOwnerStitch.TYPE_ID);
        instanceStitchType.setId(provider.getProviderType() + "-" + instanceStitchType.getLocalId());
        ownerType.setId(provider.getProviderType() + "-" + ownerType.getLocalId());


        String logicalId = "/slim/slim/conninstances";
        String mergedLogicalId = "/slim/slim/conninstances";
        String name = "InstancesSlimGA";
        String description = "Slim Test Standard Instances Grounded Aggregation";

        Aggregation instancesStitchAgg = createGroundedAggregation(session, provider, instanceStitchType, logicalId,
                mergedLogicalId, name, description, expectedSize);

        String logicalId2 = "/slim/slim/connowner";
        String mergedLogicalId2 = "/slim/slim/connowner";
        String name2 = "OwnerSlimGA";
        String description2 = "Slim Test Standard Owner Grounded Aggregation";

        Aggregation ownerStitchAgg = createGroundedAggregation(session, provider, ownerType, logicalId2,
                mergedLogicalId2, name2, description2, expectedSize);
        // Now populate them with data
        ArrayList<Item> instancesStitchArray = new ArrayList<Item>(expectedSize);
        ArrayList<Item> ownerStitchArray = new ArrayList<Item>(expectedSize);


        TestTypeInstanceStitch instanceStitch = new TestTypeInstanceStitch(instancesStitchAgg.getLogicalId() + "/" + 1,
                "instance" + 1, "small", instanceStitchType);
        TestTypeOwnerStitch ownerStitch =
                new TestTypeOwnerStitch(ownerStitchAgg.getLogicalId() + "/" + 1, "owner" + 1, "/dev/sda", ownerType);

        instancesStitchArray.add(instanceStitch);
        ownerStitchArray.add(ownerStitch);

        instanceStitch.addConnectedRelationships(ownerStitch, "relationsType");

        // Update the GAs
        UpdateResult instanceStitchTestSortedRes =
                new UpdateResult(instancesStitchArray, instancesStitchArray, instancesStitchArray, null);
        UpdateResult owernStitchTestSortedRes =
                new UpdateResult(ownerStitchArray, ownerStitchArray, instancesStitchArray, null);
        // force the ignore to false for testing (and kicks)

        aggregationManager.updateGroundedAggregation(session, instancesStitchAgg, instanceStitchTestSortedRes);
        aggregationManager.updateGroundedAggregation(session, ownerStitchAgg, owernStitchTestSortedRes);

        instanceStitch.addMemberOf(instancesStitchAgg);
        ownerStitch.addMemberOf(ownerStitchAgg);
    }

    @Before
    public void setUp() throws Exception {
        stitcher.getStitcherRuleManager().getRules().clear();
        stitcher.deleteAllSessions();
    }

    @After
    public void after() {
        stitcher.getStitcherRuleManager().getRules().clear();
        stitcher.deleteAllSessions();
    }

    @Test
    public void testStitch() throws NoSuchSessionException, SessionAlreadyExistsException, NoSuchAggregationException,
            LogicalIdAlreadyExistsException, RelationPropertyNotFound {

        Provider provider = new ProviderImpl("os", "test", "http://openstack/v1.1/auth", "Test", "test");
        Provider providerSlim = new ProviderImpl("slim", "slim", "http://openstack/v1.1/auth", "Test", "test");

        String instanceTypeId =
                LoomUtils.getItemTypeIdFromProviderType(provider.getProviderType(), TestTypeInstance.TYPE_ID);
        String instanceStitchTypeId =
                LoomUtils.getItemTypeIdFromProviderType(providerSlim.getProviderType(), TestTypeInstanceStitch.TYPE_ID);

        StitchFunction<TestTypeInstance, TestTypeInstanceStitch> instanceToSlimInstance = new StitchFunction<>(
                instanceTypeId, instanceStitchTypeId, (final TestTypeInstance h, final TestTypeInstanceStitch i) -> i
                        .getCore().getDeviceName().equals(h.getCore().getDeviceName()));

        StitchFunction<TestTypeInstanceStitch, TestTypeInstance> slimInstanceToInstance =
                new StitchFunction<>(instanceStitchTypeId, instanceTypeId, (final TestTypeInstanceStitch i,
                        final TestTypeInstance h) -> i.getCore().getDeviceName().equals(h.getCore().getDeviceName()));

        StitcherRulePair<TestTypeInstance, TestTypeInstanceStitch> rulePair =
                new StitcherRulePair<>("slim-instanceStitch", instanceToSlimInstance, slimInstanceToInstance);


        // register a stitching rule between TestTypeInstanceStitch and TestTypeInstance
        stitcher.getStitcherRuleManager().addStitcherRulePair("slim-instanceStitch", rulePair);

        TestTypeInstanceStitch instanceStitch = null;
        TestTypeInstance instance = null;

        Session session = createSession();
        createConnectedGroundedAggUser(session, provider, 6);
        createConnectedGroundedAggSlim(session, providerSlim, 1);

        List<TestTypeProject> projects = new ArrayList<>();

        //
        // Extract the Connected Relationships for all entities in the model.
        List<Aggregation> gas = aggregationManager.listGroundedAggregations(session);
        RelationshipsModelImpl model = new RelationshipsModelImpl();
        model.calculateClassRelationships(gas);
        for (Aggregation aggregation : gas) {
            List<Fibre> fibres = aggregation.getElements();
            for (Fibre fibre : fibres) {
                if (fibre.isItem()) {
                    Item item = (Item) fibre;
                    if (item instanceof TestTypeInstanceStitch) {
                        instanceStitch = (TestTypeInstanceStitch) item;
                    }
                    if (item instanceof TestTypeInstance) {
                        instance = (TestTypeInstance) item;
                    }
                    if (item instanceof TestTypeProject) {
                        projects.add((TestTypeProject) item);
                    }
                }
            }
        }

        GraphProcessor graphProcessor = new GraphProcessorImplIter();

        Set<String> relations = new HashSet<>();
        Set<String> equalRelations = new HashSet<>();
        Map<String, Set<String>> relations2 = new HashMap<>();
        Map<String, List<String>> relationPaths = new HashMap<>();


        ItemEquivalence equivalence = stitcher.getItemEquivalence(session);
        graphProcessor.doProcessGraphForItem(instanceStitch, relations, relations2, relationPaths,
                relationshipCalculator::reportRelationToItem, model, null, new HashSet<String>(), new HashSet<String>(),
                true, equivalence, relationshipCalculator::reportEquivalenceRelationToItem, equalRelations);

        Assert.assertEquals("instanceStitch should be relations to 16 items", 16, relations.size());
        Assert.assertEquals("instanceStitch should be equalRelations to 6 items", 6, equalRelations.size());



        for (Aggregation aggregation : gas) {
            List<Fibre> fibres = aggregation.getElements();
            for (Fibre fibre : fibres) {
                if (fibre.isItem()) {
                    Item item = (Item) fibre;
                    if (item instanceof TestTypeInstance) {
                        relations.clear();
                        relationPaths.clear();
                        relations2.clear();
                        equalRelations.clear();

                        graphProcessor.doProcessGraphForItem(item, relations, relations2, relationPaths,
                                relationshipCalculator::reportRelationToItem, model, null, new HashSet<String>(),
                                new HashSet<String>(), true, equivalence,
                                relationshipCalculator::reportEquivalenceRelationToItem, equalRelations);

                        Assert.assertEquals("Instance should be relations to 4 items", 4, relations.size());
                        Assert.assertEquals("Instance should be equalRelations to 2 items", 1, equalRelations.size());
                    }
                }
            }
        }



        // Check the Project is related via the stitch to the other items
        for (TestTypeProject project : projects) {
            relations.clear();
            relationPaths.clear();
            relations2.clear();
            equalRelations.clear();

            graphProcessor.doProcessGraphForItem(project, relations, relations2, relationPaths,
                    relationshipCalculator::reportRelationToItem, model, null, new HashSet<String>(),
                    new HashSet<String>(), true, equivalence, relationshipCalculator::reportEquivalenceRelationToItem,
                    equalRelations);

            Assert.assertEquals("Projects should be related to 6 items", 6, relations.size());
            // check that two are via the stitch
            int viaStitch = 0;
            for (String rel : relations) {
                if (rel.startsWith("/slim")) {
                    viaStitch++;
                }
            }
            Assert.assertEquals("Expecting two relationships via stitch", 2, viaStitch);
        }
    }


    /**
     * the checks are 1. everything in l.relations should be in the relationTypes map 2. everything
     * in relationTypes map should be in l.relations
     * 
     * @param result2
     */
    private void checkRelationsVsMap(QueryResult result2) {
        Set<String> lRelations = new HashSet<>();
        Set<String> lRelations2 = new HashSet<>();
        lRelations.addAll(result2.getElements().get(0).getRelations());
        Map<String, Set<String>> lRelationTypes = result2.getElements().get(0).getRelationTypes();
        for (String key : lRelationTypes.keySet()) {
            lRelations.removeAll(lRelationTypes.get(key));
            lRelations2.addAll(lRelationTypes.get(key));
        }
        Assert.assertTrue(lRelations.size() == 0);
        lRelations2.removeAll(result2.getElements().get(0).getRelations());
        Assert.assertTrue(lRelations2.size() == 0);
    }
}
