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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.hp.hpl.loom.adapter.AggregationUpdate;
import com.hp.hpl.loom.adapter.ItemAttributeDelta;
import com.hp.hpl.loom.adapter.ItemDeletionDelta;
import com.hp.hpl.loom.adapter.ItemRelationsDelta;
import com.hp.hpl.loom.adapter.UpdateDelta;
import com.hp.hpl.loom.adapter.UpdateResult;
import com.hp.hpl.loom.adapter.os.TestTypeInstance;
import com.hp.hpl.loom.adapter.os.TestTypeInstanceAttributes;
import com.hp.hpl.loom.adapter.os.TestTypeProject;
import com.hp.hpl.loom.adapter.os.TestTypeVolume;
import com.hp.hpl.loom.exceptions.CheckedLoomException;
import com.hp.hpl.loom.exceptions.LogicalIdAlreadyExistsException;
import com.hp.hpl.loom.exceptions.NoSuchAggregationException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.ProviderImpl;
import com.hp.hpl.loom.model.SeparableItem;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.relationships.RelationshipUtil;

public class AggregationManagerDeltaUpdateTest extends AggregationManagerTestBase {

    private static final Log LOG = LogFactory.getLog(AggregationManagerDeltaUpdateTest.class);

    private static final String projectBase = "hplb";

    private static final int NumInstances = 6; // Initial number of instances
    private static final int NumProjects = 3;
    private static final int VolumesPerInstance = 2;

    private ItemType instanceType = createItemType(TestTypeInstance.TYPE_ID);
    private ItemType volumeType = createItemType(TestTypeVolume.TYPE_ID);
    private ItemType projectType = createItemType(TestTypeProject.TYPE_ID);

    private Aggregation instancesAgg;
    private Aggregation volumesAgg;
    private Aggregation projectsAgg;

    private ArrayList<Item> instancesList;
    private ArrayList<Item> volumesList;
    private ArrayList<Item> projectsList;

    private Provider provider = new ProviderImpl("os", "providerId", "authEndpoint", "providerName", "adapterPackage");


    private void createTypesAndGAs(final Session session, final int numInstances, final int numVolumes,
            final int numProjects)
            throws NoSuchSessionException, LogicalIdAlreadyExistsException, NoSuchAggregationException {
        Provider provider = createProvider();
        instancesAgg = createConnectedInstancesGroundedAggregation(session, provider, instanceType, numInstances);
        volumesAgg = createConnectedVolumesGroundedAggregation(session, provider, volumeType, numVolumes);
        projectsAgg = createConnectedProjectsGroundedAggregation(session, provider, projectType, numProjects);
    }


    private void resetDirtyBits() {
        // Reset dirty bit, ready for subsequent testing
        instancesAgg.setDirty(false);
        volumesAgg.setDirty(false);
        projectsAgg.setDirty(false);
    }

    private int projectIdForInstanceIndex(final int index) {
        return index % NumProjects;
    }

    private int projectIdForVolumeIndex(final int index) {
        return (index / VolumesPerInstance) % NumProjects;
    }

    private TestTypeInstance addInstance() {
        TestTypeInstance instance = createInstanceWithIndex(instancesList.size());
        instancesList.add(instance);
        return instance;
    }

    private TestTypeVolume addVolume() {
        TestTypeVolume volume = createVolumeWithIndex(volumesList.size());
        volumesList.add(volume);
        return volume;
    }

    private TestTypeVolume deleteVolume() {
        return (TestTypeVolume) volumesList.remove(volumesList.size() - 1);
    }

    private TestTypeProject addProject() {
        TestTypeProject project = createProjectWithIndex(projectsList.size());
        projectsList.add(project);
        return project;
    }

    private void setCreateTimes(final Item item) {
        Date now = new Date();
        item.setFibreCreated(now);
    }

    private TestTypeInstance createInstanceWithIndex(final int index) {
        TestTypeInstance instance = new TestTypeInstance(instancesAgg.getLogicalId() + "/" + index, "instance" + index,
                projectBase + projectIdForInstanceIndex(index), "small", instanceType);
        setCreateTimes(instance);
        return instance;
    }

    private TestTypeVolume createVolumeWithIndex(final int index) {
        TestTypeVolume volume = new TestTypeVolume(volumesAgg.getLogicalId() + "/" + index, "volume" + index,
                projectBase + projectIdForVolumeIndex(index), "/dev/sda", volumeType);
        setCreateTimes(volume);
        return volume;
    }

    private TestTypeProject createProjectWithIndex(final int id) {
        TestTypeProject project = new TestTypeProject(projectsAgg.getLogicalId() + "/" + id, "project" + id,
                projectBase + id, id, projectType);
        setCreateTimes(project);
        return project;
    }

    private TestTypeProject getProject(final int projectId) {
        return (TestTypeProject) projectsList.get(projectId);
    }

    private void createConnectedGroundedAggregationsWithItems(final Session session, final int numInstances,
            final int numProjects)
            throws NoSuchAggregationException, NoSuchSessionException, LogicalIdAlreadyExistsException {
        final int numVolumes = numInstances * VolumesPerInstance;
        // Create the Grounded Aggregations
        createTypesAndGAs(session, numInstances, numVolumes, numProjects);

        // Now populate them with data
        instancesList = new ArrayList<>(numInstances);
        volumesList = new ArrayList<>(numVolumes);
        projectsList = new ArrayList<>(NumProjects);
        for (int id = 0; id < NumProjects; id++) {
            addProject();
        }
        // TODO - define this elsewhere... perhaps?

        for (int cc = 0; cc < numInstances; cc++) {
            TestTypeProject project = getProject(projectIdForInstanceIndex(cc));

            TestTypeInstance instance = addInstance();
            addConnectedInstanceToConnectedProject(instance, project, null);

            for (int count = 0; count < VolumesPerInstance; count++) {
                TestTypeVolume volume = addVolume();
                addConnectedInstanceToConnectedVolume(instance, volume, null);
                addConnectedVolumeToConnectedProject(volume, project, null);
            }
        }
        // Calculate dynamic attributes
        for (Item item : instancesList) {
            item.update();
        }
        for (Item item : volumesList) {
            item.update();
        }
        for (Item item : projectsList) {
            item.update();
        }
        // Update the GAs with initial data
        UpdateResult instanceUpdate = new UpdateResult(instancesList, instancesList, null, null);
        UpdateResult volumeUpdate = new UpdateResult(volumesList, volumesList, null, null);
        UpdateResult projectUpdate = new UpdateResult(projectsList, projectsList, null, null);
        aggregationManager.updateGroundedAggregation(session, instancesAgg, instanceUpdate);
        aggregationManager.updateGroundedAggregation(session, volumesAgg, volumeUpdate);
        aggregationManager.updateGroundedAggregation(session, projectsAgg, projectUpdate);

        resetDirtyBits();

        checkStableGroundedAggregations(NumInstances);
    }

    private ArrayList<Item> itemsFromFibres(final List<Fibre> fibres) {
        ArrayList<Item> items = new ArrayList<Item>(fibres.size());
        for (Fibre fibre : fibres) {
            items.add((Item) fibre);
        }
        return items;
    }


    // //////////////////////////////////////////////
    // Build deltas
    // //////////////////////////////////////////////

    private class DeltaUpdates {

        private Map<String, DeltaBuilder> deltaBuilders = new HashMap<>();

        DeltaBuilder createDeltaBuilder(final Aggregation aggregation, final ArrayList<Item> items) {
            DeltaBuilder deltaBuilder = new DeltaBuilder(this, aggregation, items);
            deltaBuilders.put(deltaBuilder.getId(), deltaBuilder);
            return deltaBuilder;
        }

        Collection<AggregationUpdate> convertToUpdates() {
            List<AggregationUpdate> updates = new ArrayList<>(deltaBuilders.size());
            for (DeltaBuilder deltaBuilder : deltaBuilders.values()) {
                AggregationUpdate aggUpdate = deltaBuilder.createAggregationUpdate();
                aggUpdate.getUpdateResult().setIgnore(false);
                updates.add(aggUpdate);
            }
            return updates;
        }

        DeltaBuilder getDeltaBuilderForItem(final Item item) {
            return deltaBuilders.get(item.getGroundedAggregation().getLogicalId());
        }
    }

    private class DeltaBuilder {
        private DeltaUpdates deltaUpdates;
        private Aggregation aggregation;
        private ArrayList<Item> allItems;
        private Map<String, Item> newMap = new HashMap<>();
        private Map<String, Item> updatedMap = new HashMap<>();
        private ArrayList<Item> deletedItems = new ArrayList<>();
        private UpdateDelta delta = new UpdateDelta();

        DeltaBuilder(final DeltaUpdates deltaUpdates, final Aggregation aggregation, final ArrayList<Item> allItems) {
            this.deltaUpdates = deltaUpdates;
            this.aggregation = aggregation;
            this.allItems = allItems;
        }

        DeltaBuilder getDeltaBuilderForItem(final Item item) {
            return deltaUpdates.getDeltaBuilderForItem(item);
        }

        String getId() {
            return aggregation.getLogicalId();
        }

        private void addToUpdatedIfNotNew(final Item item) {
            if (!newMap.containsKey(item.getLogicalId())) {
                updatedMap.put(item.getLogicalId(), item);
            }
        }

        // Add specified new item to update
        void deltaNewItem(final Item item) {
            newMap.put(item.getLogicalId(), item);
        }

        // Add specified new item to update
        void deltaDeleteItem(final Item item) {
            deletedItems.add(item);
            delta.getDeletionDelta().add(new ItemDeletionDelta(item));
        }

        // Add specified updated item to update
        <A extends CoreItemAttributes> void deltaUpdatedItem(final SeparableItem<A> item, final A attributes) {
            addToUpdatedIfNotNew(item);
            ItemAttributeDelta<A> attributeDelta = new ItemAttributeDelta<>(item, attributes);
            delta.getAttributeDelta().add(attributeDelta);
        }

        void deltaAddRelation(final Item item, final Item other, final DeltaBuilder otherBuilder) {
            addToUpdatedIfNotNew(item);
            otherBuilder.addToUpdatedIfNotNew(other);
            ItemRelationsDelta relationsDelta = ItemRelationsDelta.addRelationToOther(item, other);
            delta.getRelationsDelta().add(relationsDelta);
        }

        void deltaAddRelationsToOthers(final Item item, final Collection<Item> others,
                final DeltaBuilder otherBuilder) {
            addToUpdatedIfNotNew(item);
            for (Item other : others) {
                otherBuilder.addToUpdatedIfNotNew(other);
            }
            ItemRelationsDelta relationsDelta = ItemRelationsDelta.addRelationsToOthers(item, others);
            delta.getRelationsDelta().add(relationsDelta);
        }

        void deltaRemoveRelation(final Item item, final Item other, final DeltaBuilder otherBuilder) {
            addToUpdatedIfNotNew(item);
            otherBuilder.addToUpdatedIfNotNew(other);
            ItemRelationsDelta relationsDelta = ItemRelationsDelta.removeRelationToOther(item, other);
            delta.getRelationsDelta().add(relationsDelta);
        }

        void deltaRemoveRelationsToOthers(final Item item, final Collection<Item> others,
                final DeltaBuilder otherBuilder) {
            addToUpdatedIfNotNew(item);
            for (Item other : others) {
                otherBuilder.addToUpdatedIfNotNew(other);
            }
            ItemRelationsDelta relationsDelta = ItemRelationsDelta.removeRelationsToOthers(item, others);
            delta.getRelationsDelta().add(relationsDelta);
        }

        void deltaClearAllRelationsWithName(final Item item, final String name, final DeltaBuilder otherBuilder) {
            addToUpdatedIfNotNew(item);
            for (Item connected : item.getConnectedItemsWithRelationshipName(name)) {
                otherBuilder.addToUpdatedIfNotNew(connected);
            }
            ItemRelationsDelta relationsDelta = ItemRelationsDelta.clearAllItemNamedRelations(item, name);
            delta.getRelationsDelta().add(relationsDelta);
        }

        void deltaClearAllRelations(final Item item) {
            addToUpdatedIfNotNew(item);
            for (Item connected : item.getAllConnectedItems()) {
                DeltaBuilder otherBuilder = getDeltaBuilderForItem(connected);
                otherBuilder.addToUpdatedIfNotNew(connected);
            }
            ItemRelationsDelta relationsDelta = ItemRelationsDelta.clearAllItemRelations(item);
            delta.getRelationsDelta().add(relationsDelta);
        }

        AggregationUpdate createAggregationUpdate() {
            return new AggregationUpdate(aggregation,
                    new UpdateResult(new ArrayList<Item>(allItems), new ArrayList<Item>(newMap.values()),
                            new ArrayList<Item>(updatedMap.values()), deletedItems, delta));
        }
    }


    // //////////////////////////////////////////////
    // Check sanity of data structures
    // //////////////////////////////////////////////

    /**
     * Check consistency of all GAs when they should have reached a consistent point that follows
     * the pattern.
     */
    private void checkStableGroundedAggregations(final int expectedInstances) {
        assertEquals("Incorrect no of Instances", expectedInstances, instancesAgg.getElements().size());
        assertEquals("Incorrect no of Volumes", expectedInstances * VolumesPerInstance,
                volumesAgg.getElements().size());
        assertEquals("Incorrect no of Projects", NumProjects, projectsAgg.getElements().size());

        for (Fibre fibre : instancesAgg.getElements()) {
            TestTypeInstance instance = (TestTypeInstance) fibre;
            assertEquals("Instance in wrong GA", instancesAgg, instance.getGroundedAggregation());
            assertEquals("Instance dynamic NumProjects incorrect", 1, instance.getNumProjects());
            assertEquals("Instance dynamic NumVolumes incorrect", VolumesPerInstance, instance.getNumVolumes());
        }

        for (Fibre fibre : volumesAgg.getElements()) {
            TestTypeVolume volume = (TestTypeVolume) fibre;
            assertEquals("Volume in wrong GA", volumesAgg, volume.getGroundedAggregation());
            assertEquals("Volume dynamic NumProjects incorrect", 1, volume.getNumProjects());
            assertEquals("Volume dynamic NumInstances incorrect", 1, volume.getNumInstances());
        }

        for (Fibre fibre : projectsAgg.getElements()) {
            TestTypeProject project = (TestTypeProject) fibre;
            assertEquals("Project in wrong GA", projectsAgg, project.getGroundedAggregation());
            int instancesInProject = expectedInstances / NumProjects;
            int extra = expectedInstances % NumProjects;
            if (extra > 0) {
                if (extra >= (project.getCore().getPid() + 1)) {
                    instancesInProject++;
                }
            }
            assertEquals("Project dynamic NumVolumes incorrect", instancesInProject * VolumesPerInstance,
                    project.getNumVolumes());
            assertEquals("Project dynamic NumInstances incorrect", instancesInProject, project.getNumInstances());
        }
    }

    private void checkDirtyBits(final boolean instances, final boolean volumes, final boolean projects) {
        assertEquals("Instances GA incorrect dirty bit after update", instances, instancesAgg.isDirty());
        assertEquals("Volumes GA incorrect dirty bit after update", volumes, volumesAgg.isDirty());
        assertEquals("Projects GA incorrect dirty bit after update", projects, projectsAgg.isDirty());
    }

    private void checkAggregationCounts(final Aggregation ga, final int all, final int created, final int updated,
            final int deleted) {
        assertEquals(ga.getName() + " GA all count incorrect", all, ga.getElements().size());
        assertEquals(ga.getName() + " GA new count incorrect", created, ga.getCreatedCount());
        assertEquals(ga.getName() + " GA updated count incorrect", updated, ga.getUpdatedCount());
        assertEquals(ga.getName() + " GA deleted count incorrect", deleted, ga.getDeletedCount());
    }

    /*
     * Add a new instance to instances GA, but with no connections to other existing Items
     */
    private void doAddInstance(final Session session) throws NoSuchSessionException, NoSuchAggregationException {
        LOG.info("doAddInstance start");
        DeltaUpdates aggregationUpdates = new DeltaUpdates();
        DeltaBuilder instancesUpdate = aggregationUpdates.createDeltaBuilder(instancesAgg, instancesList);

        TestTypeInstance instance = addInstance();
        instancesUpdate.deltaNewItem(instance);

        // Update and check results
        aggregationManager.updateGroundedAggregations(session, aggregationUpdates.convertToUpdates());
        assertEquals("New instance in incorrect aggregation", instancesAgg, instance.getGroundedAggregation());
        // Check dirty bit is only set for instances GA
        checkDirtyBits(true, false, false);
        // Check counts
        checkAggregationCounts(instancesAgg, NumInstances + 1, 1, 0, 0);
        // Other GAs Unchanged
        checkAggregationCounts(volumesAgg, NumInstances * VolumesPerInstance, NumInstances * VolumesPerInstance, 0, 0);
        checkAggregationCounts(projectsAgg, NumProjects, NumProjects, 0, 0);
        // Check dynamic attributes
        assertEquals("Instance dynamic NumProjects incorrect", 0, instance.getNumProjects());
        assertEquals("Instance dynamic NumVolumes incorrect", 0, instance.getNumVolumes());
        LOG.info("doAddInstance end");
    }

    /*
     * Update an instance in instances GA, with no side-effects in other GAs.
     */
    private void doUpdateInstance(final Session session) throws NoSuchSessionException, NoSuchAggregationException {
        LOG.info("doUpdateInstance start");
        DeltaUpdates aggregationUpdates = new DeltaUpdates();
        DeltaBuilder instancesUpdate = aggregationUpdates.createDeltaBuilder(instancesAgg, instancesList);
        int oldNumInstances = instancesList.size();
        int oldNumVolumes = volumesList.size();

        // Select an instance
        int index = instancesList.size() - 1;
        TestTypeInstance instance = (TestTypeInstance) instancesList.get(index);
        TestTypeInstanceAttributes oldInstanceAttributes = instance.getCore();
        TestTypeProject project = (TestTypeProject) projectsList.get(projectIdForInstanceIndex(index));

        new ArrayList<>(
                instance.getConnectedItemsWithRelationshipName(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                        provider.getProviderType(), TestTypeVolume.TYPE_ID, TestTypeInstance.TYPE_ID)));

        int oldInstanceNumVolumes = instance.getNumVolumes();
        int oldProjectNumVolumes = project.getNumVolumes();
        int oldProjectNumInstances = project.getNumInstances();

        int oldVolumesCreated = (int) volumesAgg.getCreatedCount();
        int oldVolumesUpdated = (int) volumesAgg.getUpdatedCount();
        int oldVolumesDeleted = (int) volumesAgg.getDeletedCount();

        int oldProjectsCreated = (int) projectsAgg.getCreatedCount();
        int oldProjectsUpdated = (int) projectsAgg.getUpdatedCount();
        int oldProjectsDeleted = (int) projectsAgg.getDeletedCount();

        // Update item
        instancesUpdate.deltaUpdatedItem(instance, new TestTypeInstanceAttributes(oldInstanceAttributes));

        // Update and check results
        aggregationManager.updateGroundedAggregations(session, aggregationUpdates.convertToUpdates());
        // Check instance updated
        assertEquals("Instance not updated", oldInstanceAttributes.getUpdateCount() + 1,
                instance.getCore().getUpdateCount());
        assertNotNull("Instance does not have updated time", instance.getFibreUpdated());
        // Check dirty bit is only set for instances
        checkDirtyBits(true, false, false);
        // Check counts
        checkAggregationCounts(instancesAgg, oldNumInstances, 0, 1, 0);
        // Other GAs Unchanged
        checkAggregationCounts(volumesAgg, oldNumVolumes, oldVolumesCreated, oldVolumesUpdated, oldVolumesDeleted);
        checkAggregationCounts(projectsAgg, NumProjects, oldProjectsCreated, oldProjectsUpdated, oldProjectsDeleted);
        // Check dynamic attributes
        assertEquals("Instance dynamic NumProjects incorrect", 1, instance.getNumProjects());
        assertEquals("Instance dynamic NumVolumes incorrect", oldInstanceNumVolumes, instance.getNumVolumes());

        assertEquals("Project dynamic NumInstances incorrect", oldProjectNumInstances, project.getNumInstances());
        assertEquals("Project dynamic NumVolumes incorrect", oldProjectNumVolumes, project.getNumVolumes());
        LOG.info("doUpdateInstance end");
    }

    /*
     * Add a relationship between the last instance and its project.
     */
    private void doAddRelationshipBetweenInstanceAndProject(final Session session)
            throws NoSuchSessionException, NoSuchAggregationException {
        LOG.info("doAddRelationshipBetweenInstanceAndProject start");
        DeltaUpdates aggregationUpdates = new DeltaUpdates();
        DeltaBuilder instancesUpdate = aggregationUpdates.createDeltaBuilder(instancesAgg, instancesList);
        DeltaBuilder projectsUpdate = aggregationUpdates.createDeltaBuilder(projectsAgg, projectsList);
        int index = instancesList.size() - 1;
        TestTypeInstance instance = (TestTypeInstance) instancesList.get(index);
        TestTypeProject project = (TestTypeProject) projectsList.get(projectIdForInstanceIndex(index));
        int oldProjectNumVolumes = project.getNumVolumes();
        int oldProjectNumInstances = project.getNumInstances();
        instancesUpdate.deltaAddRelation(instance, project, projectsUpdate);

        // Update and check results
        aggregationManager.updateGroundedAggregations(session, aggregationUpdates.convertToUpdates());
        assertNotNull("Instance does not have updated time", instance.getFibreUpdated());
        assertNotNull("Project does not have updated time", project.getFibreUpdated());
        // Check dirty bit is only set for instances and projects GA
        checkDirtyBits(true, false, true);
        // Check counts
        checkAggregationCounts(instancesAgg, NumInstances + 1, 0, 1, 0);
        checkAggregationCounts(projectsAgg, NumProjects, 0, 1, 0);
        // Other GAs Unchanged
        checkAggregationCounts(volumesAgg, NumInstances * VolumesPerInstance, NumInstances * VolumesPerInstance, 0, 0);
        // Check dynamic attributes
        assertEquals("Instance dynamic NumProjects incorrect", 1, instance.getNumProjects());
        assertEquals("Instance dynamic NumVolumes incorrect", 0, instance.getNumVolumes());

        assertEquals("Project dynamic NumInstances incorrect", oldProjectNumInstances + 1, project.getNumInstances());
        assertEquals("Project dynamic NumVolumes incorrect", oldProjectNumVolumes, project.getNumVolumes());
        LOG.info("doAddRelationshipBetweenInstanceAndProject end");
    }

    //
    // Add more volumes than required to the last instance in the list, and associated relations to
    // this instance
    // and corresponding project
    private void doAddVolumesToLastInstance(final Session session, final int numNewVolumes)
            throws NoSuchSessionException, NoSuchAggregationException {
        LOG.info("doAddVolumesToLastInstance start");
        List<TestTypeVolume> newVolumes = new ArrayList<>(numNewVolumes);
        DeltaUpdates aggregationUpdates = new DeltaUpdates();
        DeltaBuilder instancesUpdate = aggregationUpdates.createDeltaBuilder(instancesAgg, instancesList);
        DeltaBuilder volumesUpdate = aggregationUpdates.createDeltaBuilder(volumesAgg, volumesList);
        DeltaBuilder projectsUpdate = aggregationUpdates.createDeltaBuilder(projectsAgg, projectsList);
        int oldNumInstances = instancesList.size();
        int instanceIndex = oldNumInstances - 1;
        int oldNumVolumes = volumesList.size();
        TestTypeInstance instance = (TestTypeInstance) instancesList.get(instanceIndex);
        TestTypeProject project = (TestTypeProject) projectsList.get(projectIdForInstanceIndex(instanceIndex));

        int oldInstanceNumVolumes = instance.getNumVolumes();
        int oldProjectNumVolumes = project.getNumVolumes();
        int oldProjectNumInstances = project.getNumInstances();

        for (int count = 0; count < numNewVolumes; count++) {
            // Add new volume
            TestTypeVolume volume = addVolume();
            volumesUpdate.deltaNewItem(volume);
            newVolumes.add(volume);

            // Connect it to project
            volumesUpdate.deltaAddRelation(volume, project, projectsUpdate);
        }
        // Add new volumes to instance
        instancesUpdate.deltaAddRelationsToOthers(instance, new ArrayList<Item>(newVolumes), volumesUpdate);

        // Update and check results
        aggregationManager.updateGroundedAggregations(session, aggregationUpdates.convertToUpdates());
        assertNotNull("Instance does not have updated time", instance.getFibreUpdated());
        assertNotNull("Project does not have updated time", project.getFibreUpdated());
        // Check dirty bit is set for all GAs
        checkDirtyBits(true, true, true);
        // Check counts
        checkAggregationCounts(instancesAgg, oldNumInstances, 0, 1, 0);
        checkAggregationCounts(volumesAgg, oldNumVolumes + numNewVolumes, numNewVolumes, 0, 0);
        checkAggregationCounts(projectsAgg, NumProjects, 0, 1, 0);
        // Check dynamic attributes
        assertEquals("Instance dynamic NumProjects incorrect", 1, instance.getNumProjects());
        assertEquals("Instance dynamic NumVolumes incorrect", oldInstanceNumVolumes + numNewVolumes,
                instance.getNumVolumes());

        assertEquals("Project dynamic NumInstances incorrect", oldProjectNumInstances, project.getNumInstances());
        assertEquals("Project dynamic NumVolumes incorrect", oldProjectNumVolumes + numNewVolumes,
                project.getNumVolumes());

        for (TestTypeVolume volume : newVolumes) {
            assertEquals("Volume dynamic NumProjects incorrect", 1, volume.getNumProjects());
            assertEquals("Volume dynamic NumInstances incorrect", 1, volume.getNumInstances());
            assertNull("Volume has updated time even though new", volume.getFibreUpdated());
        }
        LOG.info("doAddVolumesToLastInstance end");
    }

    //
    // Delete a volume from the last instance in the list, and associated relations to
    // this instance and corresponding project
    private void doDeleteVolumeFromLastInstance(final Session session)
            throws NoSuchSessionException, NoSuchAggregationException {
        LOG.info("doDeleteVolumeFromLastInstance start");
        DeltaUpdates aggregationUpdates = new DeltaUpdates();
        DeltaBuilder volumesUpdate = aggregationUpdates.createDeltaBuilder(volumesAgg, volumesList);
        int oldNumInstances = instancesList.size();
        int oldNumVolumes = volumesList.size();

        // Select a volume to delete
        TestTypeVolume volume = deleteVolume();
        // Get connected instance and project
        TestTypeInstance instance = (TestTypeInstance) volume
                .getConnectedItemsWithRelationshipName(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                        provider.getProviderType(), TestTypeVolume.TYPE_ID, TestTypeInstance.TYPE_ID))
                .iterator().next();
        TestTypeProject project =
                (TestTypeProject) volume
                        .getConnectedItemsWithRelationshipName(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                                provider.getProviderType(), TestTypeVolume.TYPE_ID, TestTypeProject.TYPE_ID))
                        .iterator().next();

        int oldInstanceNumVolumes = instance.getNumVolumes();
        int oldProjectNumVolumes = project.getNumVolumes();
        int oldProjectNumInstances = project.getNumInstances();
        // Delete volume
        volumesUpdate.deltaDeleteItem(volume);

        // Update and check results
        aggregationManager.updateGroundedAggregations(session, aggregationUpdates.convertToUpdates());
        // Check dirty bit is set for all GAs
        checkDirtyBits(true, true, true);
        // Check counts
        checkAggregationCounts(instancesAgg, oldNumInstances, 0, 1, 0);
        checkAggregationCounts(volumesAgg, oldNumVolumes - 1, 0, 0, 1);
        checkAggregationCounts(projectsAgg, NumProjects, 0, 1, 0);
        // Check dynamic attributes
        assertEquals("Instance dynamic NumProjects incorrect", 1, instance.getNumProjects());
        assertEquals("Instance dynamic NumVolumes incorrect", oldInstanceNumVolumes - 1, instance.getNumVolumes());

        assertEquals("Project dynamic NumInstances incorrect", oldProjectNumInstances, project.getNumInstances());
        assertEquals("Project dynamic NumVolumes incorrect", oldProjectNumVolumes - 1, project.getNumVolumes());
        LOG.info("doDeleteVolumeFromLastInstance end");
    }

    //
    // Remove relationship between last instance and project
    private void doRemoveRelationshipInstanceAndProject(final Session session)
            throws NoSuchSessionException, NoSuchAggregationException {
        LOG.info("doRemoveRelationshipsInstanceAndVolumes start");
        DeltaUpdates aggregationUpdates = new DeltaUpdates();
        DeltaBuilder instancesUpdate = aggregationUpdates.createDeltaBuilder(instancesAgg, instancesList);

        DeltaBuilder projectsUpdate = aggregationUpdates.createDeltaBuilder(projectsAgg, projectsList);

        int oldNumInstances = instancesList.size();
        int oldNumVolumes = volumesList.size();

        // Select an instance
        int index = instancesList.size() - 1;
        TestTypeInstance instance = (TestTypeInstance) instancesList.get(index);
        Date oldInstanceUpdated = instance.getFibreUpdated();
        TestTypeProject project = (TestTypeProject) projectsList.get(projectIdForInstanceIndex(index));
        Date oldProjectUpdated = project.getFibreUpdated();

        int oldInstanceNumVolumes = instance.getNumVolumes();
        int oldProjectNumVolumes = project.getNumVolumes();
        int oldProjectNumInstances = project.getNumInstances();

        int oldVolumesCreated = (int) volumesAgg.getCreatedCount();
        int oldVolumesUpdated = (int) volumesAgg.getUpdatedCount();
        int oldVolumesDeleted = (int) volumesAgg.getDeletedCount();

        // Remove relationship
        instancesUpdate.deltaRemoveRelation(instance, project, projectsUpdate);

        // Update and check results
        aggregationManager.updateGroundedAggregations(session, aggregationUpdates.convertToUpdates());
        // Check instance and project updated
        assertNotNull("Instance does not have updated time", instance.getFibreUpdated());
        assertNotNull("Project does not have updated time", project.getFibreUpdated());
        assertNotEquals("Instance not updated", oldInstanceUpdated, instance.getFibreUpdated());
        assertNotEquals("Project not updated", oldProjectUpdated, project.getFibreUpdated());
        // Check dirty bit is set for instances and project
        checkDirtyBits(true, false, true);
        // Check counts
        checkAggregationCounts(instancesAgg, oldNumInstances, 0, 1, 0);
        checkAggregationCounts(projectsAgg, NumProjects, 0, 1, 0);
        // Other GAs Unchanged
        checkAggregationCounts(volumesAgg, oldNumVolumes, oldVolumesCreated, oldVolumesUpdated, oldVolumesDeleted);
        // Check dynamic attributes
        assertEquals("Instance dynamic NumProjects incorrect", 0, instance.getNumProjects());
        assertEquals("Instance dynamic NumVolumes incorrect", oldInstanceNumVolumes, instance.getNumVolumes());

        assertEquals("Project dynamic NumInstances incorrect", oldProjectNumInstances - 1, project.getNumInstances());
        assertEquals("Project dynamic NumVolumes incorrect", oldProjectNumVolumes, project.getNumVolumes());
        LOG.info("doRemoveRelationshipsInstanceAndVolumes end");
    }

    private void doRemoveRelationshipsInstanceAndVolumes(final Session session, final boolean useClear)
            throws NoSuchSessionException, NoSuchAggregationException {
        LOG.info("doRemoveRelationshipsInstanceAndVolumes start");
        DeltaUpdates aggregationUpdates = new DeltaUpdates();
        DeltaBuilder instancesUpdate = aggregationUpdates.createDeltaBuilder(instancesAgg, instancesList);
        DeltaBuilder volumesUpdate = aggregationUpdates.createDeltaBuilder(volumesAgg, volumesList);
        int oldNumInstances = instancesList.size();
        int oldNumVolumes = volumesList.size();

        // Select an instance
        int index = instancesList.size() - 1;
        TestTypeInstance instance = (TestTypeInstance) instancesList.get(index);
        Date oldInstanceUpdated = instance.getFibreUpdated();
        TestTypeProject project = (TestTypeProject) projectsList.get(projectIdForInstanceIndex(index));

        // Get connected volumes
        String relationName = RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(provider.getProviderType(),
                TestTypeVolume.TYPE_ID, TestTypeInstance.TYPE_ID);
        List<Item> volumeItems = new ArrayList<>(instance.getConnectedItemsWithRelationshipName(relationName));
        List<Date> oldVolumeUpdated = new ArrayList<>(volumeItems.size());
        for (Item item : volumeItems) {
            oldVolumeUpdated.add(item.getFibreUpdated());
        }

        int oldProjectNumVolumes = project.getNumVolumes();
        int oldProjectNumInstances = project.getNumInstances();

        int oldProjectsCreated = (int) projectsAgg.getCreatedCount();
        int oldProjectsUpdated = (int) projectsAgg.getUpdatedCount();
        int oldProjectsDeleted = (int) projectsAgg.getDeletedCount();

        // Update item
        if (useClear) {
            instancesUpdate.deltaClearAllRelationsWithName(instance, relationName, volumesUpdate);
        } else {
            instancesUpdate.deltaRemoveRelationsToOthers(instance, volumeItems, volumesUpdate);
        }

        // Update and check results
        aggregationManager.updateGroundedAggregations(session, aggregationUpdates.convertToUpdates());
        // Check instance and volumes updated
        assertNotNull("Instance does not have updated time", instance.getFibreUpdated());
        assertNotEquals("Instance not updated", oldInstanceUpdated, instance.getFibreUpdated());
        for (int count = 0; count < volumeItems.size(); count++) {
            TestTypeVolume volume = (TestTypeVolume) volumeItems.get(count);
            assertNotNull("Volume does not have updated time", volume.getFibreUpdated());
            assertNotEquals("Project not updated", oldVolumeUpdated.get(count), volume.getFibreUpdated());
        }
        // Check dirty bit is set for instances and project
        checkDirtyBits(true, true, false);
        // Check counts
        checkAggregationCounts(instancesAgg, oldNumInstances, 0, 1, 0);
        checkAggregationCounts(volumesAgg, oldNumVolumes, 0, volumeItems.size(), 0);
        // Other GAs Unchanged
        checkAggregationCounts(projectsAgg, NumProjects, oldProjectsCreated, oldProjectsUpdated, oldProjectsDeleted);
        // Check dynamic attributes
        assertEquals("Instance dynamic NumProjects incorrect", 1, instance.getNumProjects());
        assertEquals("Instance dynamic NumVolumes incorrect", 0, instance.getNumVolumes());

        assertEquals("Project dynamic NumInstances incorrect", oldProjectNumInstances, project.getNumInstances());
        assertEquals("Project dynamic NumVolumes incorrect", oldProjectNumVolumes, project.getNumVolumes());

        for (Item item : volumeItems) {
            TestTypeVolume volume = (TestTypeVolume) item;
            assertEquals("Volume dynamic NumInstances incorrect", 0, volume.getNumInstances());
            assertEquals("Volume dynamic NumProjects incorrect", 1, volume.getNumProjects());
        }
        LOG.info("doRemoveRelationshipsInstanceAndVolumes end");
    }

    private void doRemoveAllRelationshipsInstance(final Session session)
            throws NoSuchSessionException, NoSuchAggregationException {
        LOG.info("doRemoveAllRelationshipsInstance start");
        DeltaUpdates aggregationUpdates = new DeltaUpdates();
        DeltaBuilder instancesUpdate = aggregationUpdates.createDeltaBuilder(instancesAgg, instancesList);
        aggregationUpdates.createDeltaBuilder(volumesAgg, volumesList);
        aggregationUpdates.createDeltaBuilder(projectsAgg, projectsList);
        int oldNumInstances = instancesList.size();
        int oldNumVolumes = volumesList.size();

        // Select an instance
        int index = instancesList.size() - 1;
        TestTypeInstance instance = (TestTypeInstance) instancesList.get(index);
        Date oldInstanceUpdated = instance.getFibreUpdated();
        TestTypeProject project = (TestTypeProject) projectsList.get(projectIdForInstanceIndex(index));
        Date oldProjectUpdated = project.getFibreUpdated();

        // Get connected volumes
        String relationName = RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(provider.getProviderType(),
                TestTypeVolume.TYPE_ID, TestTypeInstance.TYPE_ID);
        List<Item> volumeItems = new ArrayList<>(instance.getConnectedItemsWithRelationshipName(relationName));
        List<Date> oldVolumeUpdated = new ArrayList<>(volumeItems.size());
        for (Item item : volumeItems) {
            oldVolumeUpdated.add(item.getFibreUpdated());
        }

        int oldProjectNumVolumes = project.getNumVolumes();
        int oldProjectNumInstances = project.getNumInstances();

        projectsAgg.getCreatedCount();
        projectsAgg.getUpdatedCount();
        projectsAgg.getDeletedCount();

        // Update item
        instancesUpdate.deltaClearAllRelations(instance);

        // Update and check results
        aggregationManager.updateGroundedAggregations(session, aggregationUpdates.convertToUpdates());
        // Check instance, project, and volumes updated
        assertNotNull("Instance does not have updated time", instance.getFibreUpdated());
        assertNotEquals("Instance not updated", oldInstanceUpdated, instance.getFibreUpdated());
        assertNotNull("Project does not have updated time", project.getFibreUpdated());
        assertNotEquals("Project not updated", oldProjectUpdated, project.getFibreUpdated());
        for (int count = 0; count < volumeItems.size(); count++) {
            TestTypeVolume volume = (TestTypeVolume) volumeItems.get(count);
            assertNotNull("Volume does not have updated time", volume.getFibreUpdated());
            assertNotEquals("Project not updated", oldVolumeUpdated.get(count), volume.getFibreUpdated());
        }
        // Check dirty bit is set for instances, volumes, and project
        checkDirtyBits(true, true, true);
        // Check counts
        checkAggregationCounts(instancesAgg, oldNumInstances, 0, 1, 0);
        checkAggregationCounts(volumesAgg, oldNumVolumes, 0, volumeItems.size(), 0);
        checkAggregationCounts(projectsAgg, NumProjects, 0, 1, 0);
        // Check dynamic attributes
        assertEquals("Instance dynamic NumProjects incorrect", 0, instance.getNumProjects());
        assertEquals("Instance dynamic NumVolumes incorrect", 0, instance.getNumVolumes());

        assertEquals("Project dynamic NumInstances incorrect", oldProjectNumInstances - 1, project.getNumInstances());
        assertEquals("Project dynamic NumVolumes incorrect", oldProjectNumVolumes, project.getNumVolumes());

        for (Item item : volumeItems) {
            TestTypeVolume volume = (TestTypeVolume) item;
            assertEquals("Volume dynamic NumInstances incorrect", 0, volume.getNumInstances());
            assertEquals("Volume dynamic NumProjects incorrect", 1, volume.getNumProjects());
        }
        LOG.info("doRemoveAllRelationshipsInstance end");
    }

    // //////////////////////////////////////////////////////////////////////////////////////
    // Tests for Updating Grounded Aggregations via "Delta" Mechanism
    // //////////////////////////////////////////////////////////////////////////////////////

    /**
     * Test sequence of delta updates to add a new instance and a set of connected volumes.
     */
    @Test
    public void testDeltaSequenceAddInstanceAndVolumes() throws CheckedLoomException {
        LOG.info("testDeltaSequenceAddInstanceAndVolumes start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Session session = createSession();
        createConnectedGroundedAggregationsWithItems(session, NumInstances, NumProjects);
        doAddInstance(session);
        doAddRelationshipBetweenInstanceAndProject(session);
        doAddVolumesToLastInstance(session, 3);
        doDeleteVolumeFromLastInstance(session);

        // If all of the above have worked properly, we should have a standard stable configuration
        // with one extra instance
        checkStableGroundedAggregations(NumInstances + 1);
        LOG.info("testDeltaSequenceAddInstanceAndVolumes end");
    }

    /**
     * Test update of a single instance.
     */
    @Test
    public void testUpdateInstance() throws CheckedLoomException {
        LOG.info("testDeltaSequenceAddInstanceAndVolumes start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Session session = createSession();
        createConnectedGroundedAggregationsWithItems(session, NumInstances, NumProjects);
        doUpdateInstance(session);

        // If all of the above have worked properly, we should have a standard stable configuration
        checkStableGroundedAggregations(NumInstances);
        LOG.info("testDeltaSequenceAddInstanceAndVolumes end");
    }

    /**
     * Test removal of relationships between instance and its project.
     */
    @Test
    public void testRemoveRelationshipsInstanceAndProject() throws CheckedLoomException {
        LOG.info("testRemoveRelationshipsInstanceAndProject start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Session session = createSession();
        createConnectedGroundedAggregationsWithItems(session, NumInstances, NumProjects);
        doRemoveRelationshipInstanceAndProject(session);
        LOG.info("testRemoveRelationshipsInstanceAndProject end");
    }

    /**
     * Test removal of relationships between instance and a set of connected volumes.
     */
    @Test
    public void testRemoveRelationshipsInstanceAndVolumes() throws CheckedLoomException {
        LOG.info("testRemoveRelationshipsInstanceAndVolumes start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Session session = createSession();
        createConnectedGroundedAggregationsWithItems(session, NumInstances, NumProjects);
        doRemoveRelationshipsInstanceAndVolumes(session, false);
        LOG.info("testRemoveRelationshipsInstanceAndVolumes end");
    }

    /**
     * Test clear of relationships between instance and a set of connected volumes.
     */
    @Test
    public void testClearRelationshipsInstanceAndVolumes() throws CheckedLoomException {
        LOG.info("testClearRelationshipsInstanceAndVolumes start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Session session = createSession();
        createConnectedGroundedAggregationsWithItems(session, NumInstances, NumProjects);
        doRemoveRelationshipsInstanceAndVolumes(session, true);
        LOG.info("testClearRelationshipsInstanceAndVolumes end");
    }

    /**
     * Test clear of all relationships between instance and connected volumes and project.
     */
    @Test
    public void testClearAllRelationshipsInstance() throws CheckedLoomException {
        LOG.info("testClearAllRelationshipsInstance start");
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Session session = createSession();
        createConnectedGroundedAggregationsWithItems(session, NumInstances, NumProjects);
        doRemoveAllRelationshipsInstance(session);
        LOG.info("testClearAllRelationshipsInstance end");
    }
}
