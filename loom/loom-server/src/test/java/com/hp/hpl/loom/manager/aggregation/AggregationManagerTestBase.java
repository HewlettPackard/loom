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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hp.hpl.loom.adapter.os.TestTypeInstance;
import com.hp.hpl.loom.adapter.os.TestTypeProject;
import com.hp.hpl.loom.adapter.os.TestTypeVolume;
import com.hp.hpl.loom.api.util.SessionManager;
import com.hp.hpl.loom.exceptions.LogicalIdAlreadyExistsException;
import com.hp.hpl.loom.exceptions.NoSuchAggregationException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.SessionAlreadyExistsException;
import com.hp.hpl.loom.manager.stitcher.Tacker;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.ProviderImpl;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.model.SessionImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext-testAggregationManager.xml")
public class AggregationManagerTestBase {

    private static final Log LOG = LogFactory.getLog(AggregationManagerTestBase.class);

    protected static String AddProviderIdToTypeId(final String localId) {
        return "os-" + localId;
    }

    @Autowired
    protected SessionManager sessionManager;
    @Autowired
    protected AggregationManager aggregationManager;
    @Autowired
    protected Tacker stitcher;

    @Before
    public void setUp() throws Exception {
        LOG.info("Setup test");
    }

    @After
    public void shutDown() throws Exception {
        LOG.info("shutDown test");
        if (aggregationManager != null) {
            aggregationManager.deleteAllSessions();
        }
        if (stitcher != null) {
            stitcher.deleteAllSessions();
        }
    }

    private String createUuid() {
        return java.util.UUID.randomUUID().toString();
    }

    protected Session createSession() throws NoSuchSessionException, SessionAlreadyExistsException {
        Session session = new SessionImpl(createUuid(), sessionManager.getInterval());
        aggregationManager.createSession(session);
        stitcher.createSession(session);
        return session;
    }

    private Provider createProvider(final String providerType, final String providerId, final String authEndpoint,
            final String name, final String adapterPackage) {
        return new ProviderImpl(providerType, providerId, authEndpoint, name, adapterPackage);
    }

    protected Provider createProvider() {
        return createProvider("openstack", "test", "http://openstack/v1.1/auth", "Test", "test");
    }

    protected ItemType createItemType(final String typeId) {
        ItemType it = new ItemType(typeId);
        it.setId(AddProviderIdToTypeId(it.getLocalId()));
        return it;
    }

    protected Aggregation createGroundedAggregation(final Session session, final Provider provider,
            final ItemType itemType, final String logicalId, final String mergedLogicalId, final String name,
            final String description, final int expectedSize)
            throws NoSuchSessionException, LogicalIdAlreadyExistsException, NoSuchAggregationException {
        Aggregation aggregation = aggregationManager.createGroundedAggregation(session, provider, itemType, logicalId,
                mergedLogicalId, name, description, expectedSize);
        assertFalse("Dirty bit not clear on GA creation", aggregation.isDirty());
        return aggregation;
    }

    protected Aggregation createConnectedInstancesGroundedAggregation(final Session session, final Provider provider,
            final ItemType itemType, final int expectedSize)
            throws NoSuchSessionException, LogicalIdAlreadyExistsException, NoSuchAggregationException {
        String logicalId = "/providers/os/conninstances";
        String mergedLogicalId = "/providers/os/conninstances";
        String name = "InstancesGA";
        String description = "Test Standard Instances Grounded Aggregation";
        return createGroundedAggregation(session, provider, itemType, logicalId, mergedLogicalId, name, description,
                expectedSize);
    }

    protected Aggregation createConnectedVolumesGroundedAggregation(final Session session, final Provider provider,
            final ItemType itemType, final int expectedSize)
            throws NoSuchSessionException, LogicalIdAlreadyExistsException, NoSuchAggregationException {
        String logicalId = "/providers/os/connvolumes";
        String mergedLogicalId = "/providers/os/connvolumes";
        String name = "VolumesGA";
        String description = "Test Standard Volume Grounded Aggregation";
        return createGroundedAggregation(session, provider, itemType, logicalId, mergedLogicalId, name, description,
                expectedSize);
    }

    protected Aggregation createConnectedProjectsGroundedAggregation(final Session session, final Provider provider,
            final ItemType itemType, final int expectedSize)
            throws NoSuchSessionException, LogicalIdAlreadyExistsException, NoSuchAggregationException {
        String logicalId = "/providers/os/connprojects";
        String mergedLogicalId = "/providers/os/connprojects";
        String name = "ProjectsGA";
        String description = "Test Standard Project Grounded Aggregation";
        return createGroundedAggregation(session, provider, itemType, logicalId, mergedLogicalId, name, description,
                expectedSize);
    }

    protected void addConnectedInstanceToConnectedVolume(final TestTypeInstance instance, final TestTypeVolume volume,
            final String relationsType) {
        volume.addConnectedRelationships(instance, relationsType);
    }

    protected void addConnectedInstanceToConnectedProject(final TestTypeInstance instance,
            final TestTypeProject project, final String relationsType) {
        project.addConnectedRelationships(instance, relationsType);
    }

    protected void addConnectedVolumeToConnectedProject(final TestTypeVolume volume, final TestTypeProject project,
            final String relationsType) {
        project.addConnectedRelationships(volume, relationsType);
    }


    protected void checkDirtyBitOnAggregations(final List<Aggregation> aggregations, final boolean expectedDirty) {
        for (Aggregation aggregation : aggregations) {
            checkDirtyBit(aggregation, expectedDirty);
        }
    }

    /*
     * Recursively check the dirty bit of an aggregation and all descendants.
     */
    protected void checkDirtyBit(final Aggregation aggregation, final boolean expectedDirty) {
        assertEquals("Dirty bit not set correctly for " + aggregation, expectedDirty, aggregation.isDirty());

        if (aggregation.containsAggregations()) {
            for (Fibre entity : aggregation.getElements()) {
                if (entity.isAggregation()) {
                    checkDirtyBit((Aggregation) entity, expectedDirty);
                }
            }
        }
    }
}
