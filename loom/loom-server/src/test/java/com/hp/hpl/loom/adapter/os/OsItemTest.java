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
package com.hp.hpl.loom.adapter.os;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Date;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.ProviderImpl;

public class OsItemTest {
    private Provider provider = new ProviderImpl("os", "providerId", "authEndpoint", "providerName", "adapterPackage");
    private static final Log LOG = LogFactory.getLog(OsItemTest.class);

    @Before
    public void setUp() throws Exception {
        LOG.info("Setup test");
    }

    @After
    public void shutDown() throws Exception {
        LOG.info("shutDown test");
    }

    // test creation

    @Test
    public void testInstanceCreation() {
        StopWatch watch = new StopWatch();
        LOG.info("testing InstanceCreation");
        watch.start();
        // create an instance Item
        String logicalId = "/os/fake/instances/i1";
        String name = "vm1";
        OsFlavour flavour = new OsFlavour("2", "m1.small", 20, 2048, 1);
        ItemType type = new OsInstanceType(provider);


        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        try {
            System.out.println(mapper.writeValueAsString(type));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        type.setId("os-" + type.getLocalId());
        OsInstance instance = new OsInstance(logicalId, type);
        OsInstanceAttributes oia = new OsInstanceAttributes();
        oia.setItemName(name);
        oia.setItemId(name);
        oia.setOsFlavour(flavour);
        instance.setCore(oia);
        assertEquals("Incorrect logicalId after creation of Instance", logicalId, instance.getLogicalId());
        assertEquals("Incorrect name after creation of Instance", name, instance.getCore().getItemName());
        assertEquals("Incorrect id after creation of Instance", name, instance.getCore().getItemId());
        assertEquals("Incorrect flavour after creation of Instance", flavour, instance.getCore().getOsFlavour());
        assertEquals("Incorrect type after creation of Instance", type, instance.getItemType());
        watch.stop();
        LOG.info("tested InstanceCreation --> " + watch);
    }

    @Test
    public void testProjectCreation() {
        StopWatch watch = new StopWatch();
        LOG.info("testing ProjectCreation");
        watch.start();
        // create an instance Item
        String logicalId = "/os/fake/projects/p1";
        String name = "p1";
        String id = "p1Id";
        String provId = "fake";
        ItemType type = new OsProjectType();
        type.setId("os-" + type.getLocalId());
        String description = "description";
        OsProject item = new OsProject(logicalId, type);
        OsProjectAttributes opa = new OsProjectAttributes();
        opa.setItemId(id);
        opa.setItemName(name);
        opa.setItemDescription(description);
        opa.setProviderId(provId);
        item.setCore(opa);
        assertEquals("Incorrect logicalId after creation", logicalId, item.getLogicalId());
        assertEquals("Incorrect name after creation", name, item.getCore().getItemName());
        assertEquals("Incorrect id after creation", id, item.getCore().getItemId());
        assertEquals("Incorrect providerId after creation", provId, item.getCore().getProviderId());
        assertEquals("Incorrect type after creation", type, item.getItemType());
        assertEquals(description, item.getCore().getItemDescription());
        watch.stop();
        LOG.info("tested ProjectCreation --> " + watch);
    }

    @Test
    public void testRegionCreation() {
        StopWatch watch = new StopWatch();
        LOG.info("testing RegionCreation");
        watch.start();
        // create an instance Item
        String logicalId = "/os/fake/regions/r1";
        String name = "r1";
        String provId = "fake";
        ItemType type = new OsRegionType();
        type.setId("os-" + type.getLocalId());
        OsRegion item = new OsRegion(logicalId, type);
        OsRegionAttributes ora = new OsRegionAttributes();
        ora.setItemId(name);
        ora.setItemName(name);
        ora.setProviderId(provId);
        item.setCore(ora);
        assertEquals("Incorrect logicalId after creation", logicalId, item.getLogicalId());
        assertEquals("Incorrect name after creation", name, item.getCore().getItemName());
        assertEquals("Incorrect id after creation", name, item.getCore().getItemId());
        assertEquals("Incorrect providerId after creation", provId, item.getCore().getProviderId());
        assertEquals("Incorrect type after creation", type, item.getItemType());
        watch.stop();
        LOG.info("tested RegionCreation --> " + watch);
    }

    @Test
    public void testVolumeCreation() {
        StopWatch watch = new StopWatch();
        LOG.info("testing VolumeCreation");
        watch.start();
        // create an instance Item
        String logicalId = "/os/fake/volumes/v1";
        String name = "v1";
        String id = "v1Id";
        int size = 60;
        ItemType type = new OsVolumeType(provider);
        type.setId("os-" + type.getLocalId());
        String status = "AVAILABLE";
        String availabilityZone = "zone";
        String created = new Date().toString();
        String volumeType = "None";
        String snapshotId = "1234";
        String description = "A description";

        OsVolume item = new OsVolume(logicalId, type);
        OsVolumeAttributes ova =
                new OsVolumeAttributes(name, id, size, status, availabilityZone, created, volumeType, snapshotId);
        ova.setItemDescription(description);
        item.setCore(ova);
        assertEquals("Incorrect logicalId after creation", logicalId, item.getLogicalId());
        assertEquals("Incorrect name after creation", name, item.getCore().getItemName());
        assertEquals("Incorrect id after creation", id, item.getCore().getItemId());
        assertEquals("Incorrect size after creation", size, item.getCore().getSize());
        assertEquals("Incorrect type after creation", type, item.getItemType());
        assertEquals(status, item.getCore().getStatus());
        assertEquals(availabilityZone, item.getCore().getAvailabilityZone());
        assertEquals(created, item.getCore().getCreated());
        assertEquals(volumeType, item.getCore().getVolumeType());
        assertEquals(snapshotId, item.getCore().getSnapshotId());
        assertEquals(description, item.getCore().getItemDescription());

        watch.stop();
        LOG.info("tested VolumeCreation --> " + watch);
    }

    @Test
    public void testImageCreation() {
        StopWatch watch = new StopWatch();
        LOG.info("testing ImageCreation");
        watch.start();
        // create an instance Item
        String logicalId = "/os/fake/images/im1";
        String name = "im1";
        String id = "im1Id";
        ItemType type = new OsImageType(provider);
        type.setId("os-" + type.getLocalId());
        OsImage item = new OsImage(logicalId, type);
        OsImageAttributes oia = new OsImageAttributes();
        oia.setItemName(name);
        oia.setItemId(id);
        item.setCore(oia);
        assertEquals("Incorrect logicalId after creation", logicalId, item.getLogicalId());
        assertEquals("Incorrect name after creation", name, item.getCore().getItemName());
        assertEquals("Incorrect id after creation", id, item.getCore().getItemId());
        // assertEquals("Incorrect usageCount after creation", usageCount,
        // item.getUsageCount());
        assertEquals("Incorrect type after creation", type, item.getItemType());
        watch.stop();
        LOG.info("tested ImageCreation --> " + watch);
    }

    @Test
    public void testNetworkCreation() {
        StopWatch watch = new StopWatch();
        LOG.info("testing NetworkCreation");
        watch.start();
        // create an instance Item
        String logicalId = "/os/fake/networks/n1";
        String name = "n1";
        String id = "n1Id";
        ItemType type = new OsNetworkType(provider);
        type.setId("os-" + type.getLocalId());
        String status = "ACTIVE";
        OsNetwork item = new OsNetwork(logicalId, type);
        OsNetworkAttributes ona = new OsNetworkAttributes(false, false, status);
        ona.setItemName(name);
        ona.setItemId(id);
        item.setCore(ona);
        assertEquals("Incorrect logicalId after creation", logicalId, item.getLogicalId());
        assertEquals("Incorrect name after creation", name, item.getCore().getItemName());
        assertEquals("Incorrect id after creation", id, item.getCore().getItemId());
        assertEquals("Incorrect type after creation", type, item.getItemType());
        assertEquals("Incorrect status after creation", status, item.getCore().getStatus());
        assertFalse("Incorrect adminStateUp after creation", item.getCore().isAdminStateUp());
        assertFalse("Incorrect shared after creation", item.getCore().isShared());

        watch.stop();
        LOG.info("tested NetworkCreation --> " + watch);
    }

    @Test
    public void testSubnetCreation() {
        StopWatch watch = new StopWatch();
        LOG.info("testing SubnetCreation");
        watch.start();
        // create an instance Item
        String logicalId = "/os/fake/subnets/p1";
        String name = "s1";
        String id = "s1Id";
        ItemType type = new OsSubnetType(provider);
        type.setId("os-" + type.getLocalId());
        OsSubnet item = new OsSubnet(logicalId, type);
        OsSubnetAttributes osa = new OsSubnetAttributes(false, null, null, 0, null);
        osa.setItemName(name);
        osa.setItemId(id);
        item.setCore(osa);
        assertEquals("Incorrect logicalId after creation", logicalId, item.getLogicalId());
        assertEquals("Incorrect name after creation", name, item.getCore().getItemName());
        assertEquals("Incorrect id after creation", id, item.getCore().getItemId());
        assertEquals("Incorrect type after creation", type, item.getItemType());
        watch.stop();
        LOG.info("tested SubnetCreation --> " + watch);
    }

    // test json serialization
    private String toJson(final Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String jsonRep = "";
        jsonRep = mapper.writeValueAsString(object);
        return jsonRep;
    }

    @Test
    public void testInstanceJson() throws Exception {
        StopWatch watch = new StopWatch();
        LOG.info("testing InstanceJson");
        watch.start();
        // create an instance Item
        ItemType type = new OsInstanceType(provider);
        type.setId("os-" + type.getLocalId());
        OsInstance instance = new OsInstance("/os/fake/instances/i1", type);
        OsInstanceAttributes oia = new OsInstanceAttributes(new OsFlavour("2", "m1.small", 20, 2048, 1));
        oia.setItemName("vm1");
        oia.setItemId("vmId1");
        oia.setStatus("ACTIVE");
        instance.setCore(oia);
        LOG.info("created JSON is\n" + toJson(instance));
        watch.stop();
        LOG.info("tested InstanceJson --> " + watch);
    }

    @Test
    public void testProjectJson() throws Exception {
        StopWatch watch = new StopWatch();
        LOG.info("testing ProjectJson");
        watch.start();
        // create a project Item
        ItemType type = new OsProjectType();
        type.setId("os-" + type.getLocalId());
        OsProject project = new OsProject("/os/fake/projects/p1", type);
        OsProjectAttributes opa = new OsProjectAttributes();
        opa.setItemName("p1");
        opa.setItemId("pId1");
        opa.setItemDescription("description");
        opa.setProviderId("fake");
        project.setCore(opa);
        LOG.info("created JSON is\n" + toJson(project));
        watch.stop();
        LOG.info("tested ProjectJson --> " + watch);
    }

    @Test
    public void testRegionJson() throws Exception {
        StopWatch watch = new StopWatch();
        LOG.info("testing regionJson");
        watch.start();
        // create a region Item
        ItemType type = new OsRegionType();
        type.setId("os-" + type.getLocalId());
        OsRegion region = new OsRegion("/os/fake/regions/r1", type);
        OsRegionAttributes ora = new OsRegionAttributes();
        ora.setItemName("r1");
        ora.setItemId("r1");
        ora.setProviderId("fake");
        region.setCore(ora);
        LOG.info("created JSON is\n" + toJson(region));
        watch.stop();
        LOG.info("tested RegionJson --> " + watch);
    }

    @Test
    public void testNetworkJson() throws Exception {
        StopWatch watch = new StopWatch();
        LOG.info("testing NetworkJson");
        watch.start();
        // create a network Item
        ItemType type = new OsNetworkType(provider);
        type.setId("os-" + type.getLocalId());
        OsNetwork network = new OsNetwork("/os/fake/networks/n1", type);
        OsNetworkAttributes ona = new OsNetworkAttributes(false, false, "ACTIVE");
        ona.setItemName("n1");
        ona.setItemId("nId1");
        network.setCore(ona);
        LOG.info("created JSON is\n" + toJson(network));
        watch.stop();
        LOG.info("tested NetworkJson --> " + watch);
    }

    @Test
    public void testSubnetJson() throws Exception {
        StopWatch watch = new StopWatch();
        LOG.info("testing SubnetJson");
        watch.start();
        // create a subnet Item
        ItemType type = new OsSubnetType(provider);
        type.setId("os-" + type.getLocalId());
        OsSubnet subnet = new OsSubnet("/os/fake/subnets/s1", type);
        OsSubnetAttributes osa =
                new OsSubnetAttributes(true, "d32019d3-bc6e-4319-9c1d-6722fc136a22", "192.0.0.1", 4, "192.0.0.0/8");
        osa.setItemName("s1");
        osa.setItemId("sId1");
        subnet.setCore(osa);
        LOG.info("created JSON is\n" + toJson(subnet));
        watch.stop();
        LOG.info("tested SubnetJson --> " + watch);
    }

    @Test
    public void testVolumeJson() throws Exception {
        StopWatch watch = new StopWatch();
        LOG.info("testing VolumeJson");
        watch.start();
        // create a volume Item
        ItemType type = new OsVolumeType(provider);
        type.setId("os-" + type.getLocalId());
        OsVolume volume = new OsVolume("/os/fake/volumes/v1", type);
        OsVolumeAttributes ova =
                new OsVolumeAttributes("v1", "vId1", 40, "AVAILABLE", "zone", new Date().toString(), "None", "1234");
        ova.setItemDescription("A description");
        volume.setCore(ova);
        LOG.info("created JSON is\n" + toJson(volume));
        watch.stop();
        LOG.info("tested VolumeJson --> " + watch);
    }

    @Test
    public void testImageJson() throws Exception {
        StopWatch watch = new StopWatch();
        LOG.info("testing ImageJson");
        watch.start();
        // create an image Item
        ItemType type = new OsImageType(provider);
        type.setId("os-" + type.getLocalId());
        OsImage image = new OsImage("/os/fake/images/im1", type);
        OsImageAttributes oia = new OsImageAttributes();
        oia.setItemName("im1");
        oia.setItemId("imId1");
        image.setCore(oia);
        LOG.info("created JSON is\n" + toJson(image));
        watch.stop();
        LOG.info("tested ImageJson --> " + watch);
    }
}
