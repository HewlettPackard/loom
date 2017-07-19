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
package com.hp.hpl.loom.openstack.keystonev3;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hp.hpl.loom.openstack.NoSupportedApiVersion;
import com.hp.hpl.loom.openstack.OpenstackApi;
import com.hp.hpl.loom.openstack.cinder.CinderApi;
import com.hp.hpl.loom.openstack.cinder.model.JsonVolume;
import com.hp.hpl.loom.openstack.cinder.model.JsonVolumeQuota;
import com.hp.hpl.loom.openstack.cinder.model.JsonVolumes;
import com.hp.hpl.loom.openstack.common.RestUtils;
import com.hp.hpl.loom.openstack.glance.GlanceApi;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonAuth;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonProject;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonProjects;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonUser;
import com.hp.hpl.loom.openstack.neutron.NeutronApi;
import com.hp.hpl.loom.openstack.neutron.model.JsonNetwork;
import com.hp.hpl.loom.openstack.neutron.model.JsonNetworks;
import com.hp.hpl.loom.openstack.neutron.model.JsonSubnet;
import com.hp.hpl.loom.openstack.neutron.model.JsonSubnets;
import com.hp.hpl.loom.openstack.nova.NovaApi;
import com.hp.hpl.loom.openstack.nova.model.JsonFlavor;
import com.hp.hpl.loom.openstack.nova.model.JsonImage;
import com.hp.hpl.loom.openstack.nova.model.JsonNetworkUuid;
import com.hp.hpl.loom.openstack.nova.model.JsonQuota;
import com.hp.hpl.loom.openstack.nova.model.JsonServer;
import com.hp.hpl.loom.openstack.nova.model.JsonServers;
import com.hp.hpl.loom.openstack.swift.SwiftApi;
import com.hp.hpl.loom.openstack.swift.SwiftContainer;
import com.hp.hpl.loom.openstack.swift.SwiftObjects;
import com.hp.hpl.loom.openstack.swift.model.JsonAccount;
import com.hp.hpl.loom.openstack.swift.model.JsonContainer;
import com.hp.hpl.loom.openstack.swift.model.JsonObject;

public class RestUtilTest {
    private static final Log LOG = LogFactory.getLog(RestUtilTest.class);

    private String[] cinderVersions = {"v1", "v1.1"};
    private String[] novaVersions = {"v2"};
    private String[] neutronVersions = {"v2.0"};
    private String[] swiftVersions = {"v1", "v1.1"};
    private String[] glanceVersions = {"v2.0"};

    @Before
    public void setUp() throws Exception {
        LOG.info("Setup test");
    }

    @After
    public void shutDown() throws Exception {
        LOG.info("shutDown test");
    }

    @Test
    public void testUnscopedAuthStructure() throws JsonProcessingException {
        JsonAuth auth = KeystoneUtils.getUnscopedAuth("aloha", "hawai");
        LOG.info(toJson(auth));
    }

    @Test
    @Ignore
    public void testUnscopedAuth() throws JsonProcessingException {
        // OpenstackApi openstackApp = new OpenstackApi("http://16.25.180.11:5000/v3", "loom");
        // boolean auth = openstackApp.authenticate("Lubwaqit");
        // OpenstackApi openstackApp =
        // new OpenstackApi("https://region-a.geo-1.identity.hpcloudsvc.com:35357/v3",
        // "eric.deliot@hp.com");
        // openstackApp.setProxy("web-proxy.corp.hp.com", 8088);
        //
        // // http://16.25.180.11:5000/v2.0
        // openstackApp.authenticate("Totototo67");

        OpenstackApi openstackApp = new OpenstackApi("http://16.25.166.21:5000/v3", "loom");

        openstackApp.setProxy("web-proxy.corp.hp.com", 8088);
        openstackApp.authenticate("Lubwaqit");

        JsonUser jsonUser = openstackApp.getTokenManager().getJsonUser();

        // KEYSTONE CALLS
        JsonProjects jsonProjects =
                openstackApp.getKeystoneApi().getKeystoneProject().getUsersProjects(jsonUser.getId());
        List<JsonProject> projects = jsonProjects.getProjects();
        for (JsonProject jsonProject : projects) {
            System.out.println(RestUtils.toJson(jsonProject));
        }
    }

    @Test
    @Ignore
    public void loadSwift() throws NoSupportedApiVersion {
        // OpenstackApi openstackApp =
        // new OpenstackApi("https://region-a.geo-1.identity.hpcloudsvc.com:35357/v3",
        // "eric.deliot@hp.com");
        // openstackApp.setProxy("web-proxy.corp.hp.com", 8088);
        // openstackApp.authenticate("Totototo67");

        OpenstackApi openstackApp = new OpenstackApi("http://16.25.188.49:5000/v3", "loom");
        openstackApp.authenticate("Lubwaqit");

        // SwiftContainers swiftContainers = openstackApp.getSwiftApi().getSwiftContainers();
        JsonUser jsonUser = openstackApp.getTokenManager().getJsonUser();
        JsonProjects jsonProjects =
                openstackApp.getKeystoneApi().getKeystoneProject().getUsersProjects(jsonUser.getId());

        List<JsonProject> projects = jsonProjects.getProjects();

        for (JsonProject jsonProject : projects) {
            openstackApp.getKeystoneApi().getKeystoneProject().getServiceCatalog(jsonProject.getId(),
                    jsonProject.getName());
        }

        Collection<String> projectIds = openstackApp.getTokenManager().getTokenHolder().getAllScopedProjectIds();
        String projectId = (String) projectIds.toArray()[0];
        String[] regionsId =
                openstackApp.getTokenManager().getTokenHolder().getRegions(projectId, "object-store", "public");
        Iterator<? extends JsonContainer> iterator =
                openstackApp.getSwiftApi(swiftVersions, projectId, regionsId[0]).getSwiftContainers().getIterator();

        SwiftContainer swiftContainers =
                openstackApp.getSwiftApi(swiftVersions, projectId, regionsId[0]).getSwiftContainers();

        JsonContainer sowpodsContainer = null;
        while (iterator.hasNext()) {
            JsonContainer container = iterator.next();
            if (container.getName().equals("sowpods")) {
                sowpodsContainer = container;
            }
        }
        try {
            new FileInputStream(new File("c:/development/sowpods.txt"));
            try (Stream<String> lines = Files.lines(new File("c:/development/sowpods.txt").toPath())) {
                for (String line : (Iterable<String>) lines::iterator) {

                    swiftContainers.addObject(sowpodsContainer, line.toLowerCase());

                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    @Ignore
    public void testSwift() throws JsonProcessingException, NoSupportedApiVersion {
        // OpenstackApi openstackApp = new OpenstackApi("http://16.25.180.11:5000/v3", "loom");
        // openstackApp.setProxy("web-proxy.corp.hp.com", 8088);
        // boolean auth = openstackApp.authenticate("Lubwaqit");

        OpenstackApi openstackApp =
                new OpenstackApi("https://region-a.geo-1.identity.hpcloudsvc.com:35357/v3", "eric.deliot@hp.com");
        openstackApp.setProxy("web-proxy.corp.hp.com", 8088);
        boolean auth = openstackApp.authenticate("Totototo67");

        Assert.assertTrue(auth);


        JsonUser jsonUser = openstackApp.getTokenManager().getJsonUser();
        JsonProjects jsonProjects =
                openstackApp.getKeystoneApi().getKeystoneProject().getUsersProjects(jsonUser.getId());

        List<JsonProject> projects = jsonProjects.getProjects();
        for (JsonProject jsonProject : projects) {
            System.out.println(RestUtils.toJson(jsonProject));
            openstackApp.getKeystoneApi().getKeystoneProject().getServiceCatalog(jsonProject.getId(),
                    jsonProject.getName());
            // openstackApp.getNovaImages().getJsonResources(regionId, projectId)
        }

        // SwiftAccounts swiftAccounts = openstackApp.getSwiftApi().getSwiftAccounts();
        // SwiftContainers swiftContainers = openstackApp.getSwiftApi().getSwiftContainers();
        // SwiftObjects swiftObjects = openstackApp.getSwiftApi().getSwiftObjects();
        // openstackApp.getSwiftApi().getSwiftObjects();
        Collection<String> projectIds = openstackApp.getTokenManager().getTokenHolder().getAllScopedProjectIds();
        // System.out.println("******** START ********");
        // for (String projectId : projectIds) {
        // String[] regionIds =
        // openstackApp.getTokenManager().getTokenHolder().getRegions(projectId, "object-store",
        // "public");
        // for (String regionId : regionIds) {
        // openstackApp.getPublicEndPoints(regionId, projectId, "object-store");
        // }
        // }
        // System.out.println("******** END ********");
        for (String projectId : projectIds) {
            String[] regionIds =
                    openstackApp.getTokenManager().getTokenHolder().getRegions(projectId, "object-store", "public");

            for (String regionId : regionIds) {

                JsonAccount jsonAccount = openstackApp.getSwiftApi(swiftVersions, projectId, regionId)
                        .getSwiftAccounts().getJsonResourcesWithContainer();
                System.out.println("jsonAccount.getContainers().size() >>> " + jsonAccount.getContainers().length);

                JsonContainer[] jsonContainers = jsonAccount.getContainers();

                SwiftObjects foo = openstackApp.getSwiftApi(swiftVersions, projectId, regionId).getSwiftObjects();
                for (JsonContainer jsonContainer : jsonContainers) {
                    System.out.println("CONTAINER NAME ->>>>>>>>>>> " + jsonContainer.getName());
                }
                Iterator<? extends JsonContainer> jsonContainersIterator =
                        openstackApp.getSwiftApi(swiftVersions, projectId, regionId).getSwiftContainers().getIterator();


                while (jsonContainersIterator.hasNext()) {
                    JsonContainer jsonContainer = jsonContainersIterator.next();
                    System.out.println("XXXXXXXXXXXXX CONTAINER NAME2 ->>>>>>>>>>> " + jsonContainer.getName());
                    Iterator<? extends JsonObject> iterator = foo.getIterator(jsonContainer.getName());
                    while (iterator.hasNext()) {
                        System.out.println("XXXXXXXXXXXXX OBJECT NAME ->>>>>>>>>>> " + iterator.next().getName());
                    }
                }
            }
        }
    }

    @Test
    @Ignore
    public void testPaging() throws JsonProcessingException, NoSupportedApiVersion {
        OpenstackApi openstackApp =
                new OpenstackApi("https://region-a.geo-1.identity.hpcloudsvc.com:35357/v3", "eric.deliot@hp.com");
        openstackApp.setProxy("web-proxy.corp.hp.com", 8088);
        boolean auth = openstackApp.authenticate("Totototo67");

        Assert.assertTrue(auth);

        JsonUser jsonUser = openstackApp.getTokenManager().getJsonUser();
        JsonProjects jsonProjects =
                openstackApp.getKeystoneApi().getKeystoneProject().getUsersProjects(jsonUser.getId());

        List<JsonProject> projects = jsonProjects.getProjects();
        for (JsonProject jsonProject : projects) {
            System.out.println(RestUtils.toJson(jsonProject));
            openstackApp.getKeystoneApi().getKeystoneProject().getServiceCatalog(jsonProject.getId(),
                    jsonProject.getName());
            // openstackApp.getNovaImages().getJsonResources(regionId, projectId)
        }

        assertEquals(2, openstackApp.getTokenManager().getTokenHolder().getAllScopedProjectIds().size());

        Collection<String> projectIds = openstackApp.getTokenManager().getTokenHolder().getAllScopedProjectIds();
        for (String projectId : projectIds) {
            String[] regionIds =
                    openstackApp.getTokenManager().getTokenHolder().getRegions(projectId, "compute", "public");
            for (String regionId : regionIds) {

                // get the images printOutImages(openstackApp, projectId, regionId);

                // get the servers
                Iterator<JsonServer> jsonServers =
                        openstackApp.getNovaApi(novaVersions, projectId, regionId).getNovaServers().getIterator();
                while (jsonServers.hasNext()) {
                    RestUtils.toJson(jsonServers.next());
                }
            }
        }
    }

    @Test
    @Ignore
    public void testGetAvailableVersions() throws JsonProcessingException, NoSupportedApiVersion {
        // OpenstackApi openstackApp = new OpenstackApi("http://16.25.180.11:5000/v3", "loom");
        // openstackApp.setProxy("web-proxy.corp.hp.com", 8088);
        // boolean auth = openstackApp.authenticate("Lubwaqit");

        OpenstackApi openstackApp =
                new OpenstackApi("https://region-a.geo-1.identity.hpcloudsvc.com:35357/v3", "eric.deliot@hp.com");
        openstackApp.setProxy("web-proxy.corp.hp.com", 8088);
        boolean auth = openstackApp.authenticate("Totototo67");
        // OpenstackApi openstackApp =
        // new OpenstackApi("https://region-a.geo-1.identity.hpcloudsvc.com:35357/v3",
        // "james.brook@hp.com");
        // boolean auth = openstackApp.authenticate("ThisIsACloudTest123");

        // OpenstackApi openstackApp = new OpenstackApi("http://16.25.166.21:5000/v3", "loom");
        //
        // openstackApp.setProxy("web-proxy.corp.hp.com", 8088);
        // boolean auth = openstackApp.authenticate("Lubwaqit");
        Assert.assertTrue(auth);

        JsonUser jsonUser = openstackApp.getTokenManager().getJsonUser();
        JsonProjects jsonProjects =
                openstackApp.getKeystoneApi().getKeystoneProject().getUsersProjects(jsonUser.getId());

        List<JsonProject> projects = jsonProjects.getProjects();
        for (JsonProject jsonProject : projects) {
            System.out.println(RestUtils.toJson(jsonProject));
            openstackApp.getKeystoneApi().getKeystoneProject().getServiceCatalog(jsonProject.getId(),
                    jsonProject.getName());
        }

        Collection<String> projectIds = openstackApp.getTokenManager().getTokenHolder().getAllScopedProjectIds();
        for (String projectId : projectIds) {
            String[] regionIds =
                    openstackApp.getTokenManager().getTokenHolder().getRegions(projectId, "compute", "public");
            for (String regionId : regionIds) {
                Set<String> versions = openstackApp.getAvailableVersions(projectId, regionId, NovaApi.SERVICE_NAME);
                for (String verson : versions) {
                    System.out.println("NovaApi: " + verson);
                }
                versions = openstackApp.getAvailableVersions(projectId, regionId, NeutronApi.SERVICE_NAME);
                for (String verson : versions) {
                    System.out.println("NeutronApi: " + verson);
                }
                versions = openstackApp.getAvailableVersions(projectId, regionId, CinderApi.SERVICE_NAME);
                for (String verson : versions) {
                    System.out.println("CinderApi: " + verson);
                }
                versions = openstackApp.getAvailableVersions(projectId, regionId, SwiftApi.SERVICE_NAME);
                for (String verson : versions) {
                    System.out.println("SwiftApi: " + verson);
                }

            }
        }
    }

    @Test
    @Ignore
    public void testGlance() throws NoSupportedApiVersion {
        OpenstackApi openstackApp = new OpenstackApi("http://16.25.188.49:5000/v3", "loom");
        // openstackApp.setProxy("web-proxy.corp.hp.com", 8088);
        boolean auth = openstackApp.authenticate("Lubwaqit");
        Assert.assertTrue(auth);

        JsonUser jsonUser = openstackApp.getTokenManager().getJsonUser();
        JsonProjects jsonProjects =
                openstackApp.getKeystoneApi().getKeystoneProject().getUsersProjects(jsonUser.getId());
        List<JsonProject> projects = jsonProjects.getProjects();
        for (JsonProject jsonProject : projects) {
            System.out.println(RestUtils.toJson(jsonProject));
            openstackApp.getKeystoneApi().getKeystoneProject().getServiceCatalog(jsonProject.getId(),
                    jsonProject.getName());
            // openstackApp.getNovaImages().getJsonResources(regionId, projectId)
        }
        Collection<String> projectIds = openstackApp.getTokenManager().getTokenHolder().getAllScopedProjectIds();
        for (String projectId : projectIds) {
            String[] regionIds =
                    openstackApp.getTokenManager().getTokenHolder().getRegions(projectId, "network", "public");
            for (String regionId : regionIds) {
                GlanceApi glanceApi = openstackApp.getGlanceApi(glanceVersions, projectId, regionId);
                Iterator<com.hp.hpl.loom.openstack.glance.model.JsonImage> glanceImages =
                        glanceApi.getGlanceImage().getIterator();
                while (glanceImages.hasNext()) {
                    System.out.println(RestUtils.toJson(glanceImages.next()));
                }

            }
        }
    }

    @Test
    @Ignore
    public void testScopedAuth() throws JsonProcessingException, NoSupportedApiVersion {
        // OpenstackApi openstackApp = new OpenstackApi("http://16.25.180.11:5000/v3", "loom");
        // openstackApp.setProxy("web-proxy.corp.hp.com", 8088);
        // boolean auth = openstackApp.authenticate("Lubwaqit");

        // OpenstackApi openstackApp =
        // new OpenstackApi("https://region-a.geo-1.identity.hpcloudsvc.com:35357/v3",
        // "eric.deliot@hp.com");
        // openstackApp.setProxy("web-proxy.corp.hp.com", 8088);
        // boolean auth = openstackApp.authenticate("Totototo67");
        // OpenstackApi openstackApp =
        // new OpenstackApi("https://region-a.geo-1.identity.hpcloudsvc.com:35357/v3",
        // "james.brook@hp.com");
        // boolean auth = openstackApp.authenticate("ThisIsACloudTest123");

        OpenstackApi openstackApp = new OpenstackApi("http://16.25.166.21:5000/v3", "loom");

        openstackApp.setProxy("web-proxy.corp.hp.com", 8088);
        boolean auth = openstackApp.authenticate("Lubwaqit");
        Assert.assertTrue(auth);

        JsonUser jsonUser = openstackApp.getTokenManager().getJsonUser();
        JsonProjects jsonProjects =
                openstackApp.getKeystoneApi().getKeystoneProject().getUsersProjects(jsonUser.getId());

        List<JsonProject> projects = jsonProjects.getProjects();
        for (JsonProject jsonProject : projects) {
            System.out.println(RestUtils.toJson(jsonProject));
            openstackApp.getKeystoneApi().getKeystoneProject().getServiceCatalog(jsonProject.getId(),
                    jsonProject.getName());
            // openstackApp.getNovaImages().getJsonResources(regionId, projectId)
        }

        // assertEquals(3,
        // openstackApp.getTokenManager().getTokenHolder().getAllScopedProjectIds().size());

        Collection<String> projectIds = openstackApp.getTokenManager().getTokenHolder().getAllScopedProjectIds();
        for (String projectId : projectIds) {
            String[] regionIds =
                    openstackApp.getTokenManager().getTokenHolder().getRegions(projectId, "compute", "public");
            for (String regionId : regionIds) {
                // get the images printOutImages(openstackApp, projectId, regionId);

                // get the servers
                Iterator<JsonServer> jsonServers =
                        openstackApp.getNovaApi(novaVersions, projectId, regionId).getNovaServers().getIterator();
                while (jsonServers.hasNext()) {
                    System.out.println(RestUtils.toJson(jsonServers.next()));
                }
                System.out.println(projectId + " " + regionId + " >>> " + RestUtils.toJson(jsonServers));

                Iterator<JsonNetwork> jsonNetworks = openstackApp.getNeutronApi(neutronVersions, projectId, regionId)
                        .getNeutronNetwork().getIterator();
                while (jsonNetworks.hasNext()) {
                    System.out.println(RestUtils.toJson(jsonNetworks.next()));
                }

                JsonSubnets jsonSubnets = openstackApp.getNeutronApi(neutronVersions, projectId, regionId)
                        .getNeutronSubnets().getJsonResources();
                System.out.println(RestUtils.toJson(jsonSubnets));


                Iterator<JsonFlavor> jsonFlavors =
                        openstackApp.getNovaApi(novaVersions, projectId, regionId).getNovaFlavors().getIterator();
                while (jsonFlavors.hasNext()) {
                    System.out.println(RestUtils.toJson(jsonFlavors.next()));
                }
            }
        }

        for (String projectId : projectIds) {
            String[] regionIds =
                    openstackApp.getTokenManager().getTokenHolder().getRegions(projectId, "network", "public");
            for (String regionId : regionIds) {
                Iterator<JsonNetwork> jsonNetworks = openstackApp.getNeutronApi(neutronVersions, projectId, regionId)
                        .getNeutronNetwork().getIterator();
                while (jsonNetworks.hasNext()) {
                    System.out.println(RestUtils.toJson(jsonNetworks.next()));
                }

                JsonSubnets jsonSubnets = openstackApp.getNeutronApi(neutronVersions, projectId, regionId)
                        .getNeutronSubnets().getJsonResources();
                System.out.println(RestUtils.toJson(jsonSubnets));

                System.out.println(RestUtils.toJson(openstackApp.getNeutronApi(neutronVersions, projectId, regionId)
                        .getNeutronPorts().getJsonResources()));
            }
        }

        for (String projectId : projectIds) {
            String[] regionIds =
                    openstackApp.getTokenManager().getTokenHolder().getRegions(projectId, "network", "public");
            for (String regionId : regionIds) {
                Iterator<JsonVolume> iterator =
                        openstackApp.getCinderApi(cinderVersions, projectId, regionId).getCinderVolume().getIterator();
                while (iterator.hasNext()) {
                    System.out.println(RestUtils.toJson(iterator.next()));
                }
            }
        }

        for (String projectId : projectIds) {
            String[] regionIds =
                    openstackApp.getTokenManager().getTokenHolder().getRegions(projectId, "network", "public");
            for (String regionId : regionIds) {
                Iterator<JsonQuota> iterator =
                        openstackApp.getNovaApi(novaVersions, projectId, regionId).getNovaQuotas().getIterator();

                while (iterator.hasNext()) {
                    System.out.println(RestUtils.toJson(iterator.next()));
                }
            }
        }

        for (String projectId : projectIds) {
            String[] cinderZones =
                    openstackApp.getTokenManager().getTokenHolder().getRegions(projectId, "volume", "public");
            for (String cinderZone : cinderZones) {
                Iterator<JsonVolumeQuota> iterator = openstackApp.getCinderApi(cinderVersions, projectId, cinderZone)
                        .getCinderVolumeQuota().getIterator();
                while (iterator.hasNext()) {
                    System.out.println(RestUtils.toJson(iterator.next()));
                }

                Iterator<JsonVolume> iterator2 = openstackApp.getCinderApi(cinderVersions, projectId, cinderZone)
                        .getCinderVolume().getIterator();
                while (iterator2.hasNext()) {
                    System.out.println(RestUtils.toJson(iterator2.next()));
                }

                Iterator<String> iterator3 = openstackApp.getCinderApi(cinderVersions, projectId, cinderZone)
                        .getCinderVolumeType().getIterator();
                while (iterator3.hasNext()) {
                    System.out.println(RestUtils.toJson(iterator3.next()));
                }
            }
        }

        // for (String projectId : projectIds) {
        // String[] regionIds =
        // openstackApp.getTokenManager().getTokenHolder().getRegions(projectId, "compute",
        // "public");
        // for (String regionId : regionIds) {
        // // get the servers
        // JsonServers jsonServers =
        // openstackApp.getNovaApi().getNovaServers().getJsonResources(projectId, regionId);
        // if (jsonServers.getServers().size() != 0) {
        // for (JsonServer jsonServer : jsonServers.getServers()) {
        // if (jsonServer.getStatus().equals("ACTIVE")) {
        // System.out.println(RestUtils.toJson(jsonServer));
        // openstackApp.getNovaApi().getNovaServers().stop(projectId, regionId, jsonServer.getId());
        // break;
        // } else {
        // System.out.println(RestUtils.toJson(jsonServer));
        // openstackApp.getNovaApi().getNovaServers().start(projectId, regionId,
        // jsonServer.getId());
        // break;
        // }
        // }
        // }
        //
        // }
        // }

    }

    @Test
    @Ignore
    public void buildNetwork() throws NoSupportedApiVersion {
        OpenstackApi openstackApp = new OpenstackApi("http://16.25.188.49:5000/v3", "loom");
        // openstackApp.setProxy("web-proxy.corp.hp.com", 8088);
        boolean auth = openstackApp.authenticate("Lubwaqit");
        Assert.assertTrue(auth);

        JsonUser jsonUser = openstackApp.getTokenManager().getJsonUser();
        JsonProjects jsonProjects =
                openstackApp.getKeystoneApi().getKeystoneProject().getUsersProjects(jsonUser.getId());
        List<JsonProject> projects = jsonProjects.getProjects();
        for (JsonProject jsonProject : projects) {
            System.out.println(RestUtils.toJson(jsonProject));
            openstackApp.getKeystoneApi().getKeystoneProject().getServiceCatalog(jsonProject.getId(),
                    jsonProject.getName());
            // openstackApp.getNovaImages().getJsonResources(regionId, projectId)
        }
        Collection<String> projectIds = openstackApp.getTokenManager().getTokenHolder().getAllScopedProjectIds();
        boolean addNetwork = false;
        boolean addSubnet = false;
        boolean addVolumes = false;
        boolean addInstances = false;


        for (String projectId : projectIds) {
            String[] regionIds =
                    openstackApp.getTokenManager().getTokenHolder().getRegions(projectId, "network", "public");
            for (String regionId : regionIds) {
                NeutronApi neutronApi = openstackApp.getNeutronApi(neutronVersions, projectId, regionId);
                Iterator<JsonNetwork> jsonNetworks = neutronApi.getNeutronNetwork().getIterator();
                while (jsonNetworks.hasNext()) {
                    jsonNetworks.next();
                }
            }
        }

        for (String projectId : projectIds) {
            String[] regionIds =
                    openstackApp.getTokenManager().getTokenHolder().getRegions(projectId, "network", "public");
            for (String regionId : regionIds) {
                NeutronApi neutronApi = openstackApp.getNeutronApi(neutronVersions, projectId, regionId);
                List<JsonNetwork> networksList = new ArrayList<JsonNetwork>();
                for (int i = 8; i < 10; i++) {
                    JsonNetwork network = new JsonNetwork();
                    network.setName("network-" + i);
                    network.setAdminStateUp(true);
                    networksList.add(network);
                }
                JsonNetworks jsonNetworks = new JsonNetworks();
                jsonNetworks.setNetworks(networksList);

                System.out.println(RestUtils.toJson(jsonNetworks));
                if (addNetwork) {
                    neutronApi.getNeutronNetwork().createNetworks(jsonNetworks);
                }
            }
        }


        for (String projectId : projectIds) {
            String[] regionIds =
                    openstackApp.getTokenManager().getTokenHolder().getRegions(projectId, "network", "public");
            for (String regionId : regionIds) {
                NeutronApi neutronApi = openstackApp.getNeutronApi(neutronVersions, projectId, regionId);
                Iterator<JsonNetwork> jsonNetworks = neutronApi.getNeutronNetwork().getIterator();
                while (jsonNetworks.hasNext()) {
                    JsonNetwork jsonNetwork = jsonNetworks.next();
                    System.out.println("NUMBER OF SUBNETS --> " + jsonNetwork.getSubnets().size());
                    JsonSubnets subnets = new JsonSubnets();
                    List<JsonSubnet> subnetsList = new ArrayList<JsonSubnet>();

                    int total = 10 - jsonNetwork.getSubnets().size();
                    if (total > 0) {
                        for (int i = 0; i < total; i++) {
                            // add subnets
                            JsonSubnet jsonSubnet = new JsonSubnet();

                            jsonSubnet.setCidr(i + ".2.3.4/28");
                            jsonSubnet.setEnableDhcp(false);
                            jsonSubnet.setName("subnet-" + i);
                            jsonSubnet.setNetworkId(jsonNetwork.getId());
                            jsonSubnet.setTenantId(projectId);
                            jsonSubnet.setIpVersion(4);
                            subnetsList.add(jsonSubnet);
                        }
                        subnets.setSubnets(subnetsList);
                        System.out.println(RestUtils.toJson(subnets));
                        if (addSubnet) {
                            neutronApi.getNeutronSubnets().createSubnets(subnets);
                        }
                    }


                }
            }
        }

        // build the volumes

        for (String projectId : projectIds) {
            String[] regionIds =
                    openstackApp.getTokenManager().getTokenHolder().getRegions(projectId, "network", "public");
            for (String regionId : regionIds) {
                CinderApi cinderApi = openstackApp.getCinderApi(cinderVersions, projectId, regionId);
                for (int i = 0; i < 1; i++) {
                    JsonVolume jsonVolume = new JsonVolume();
                    jsonVolume.setSize(1);
                    jsonVolume.setDisplayName("Xvol-" + i);
                    jsonVolume.setBootable(true);
                    JsonVolumes jsonVolumes = new JsonVolumes();
                    jsonVolumes.setVolume(jsonVolume);
                    if (addVolumes) {
                        cinderApi.getCinderVolume().createVolume(projectId, jsonVolumes);
                    }
                }

            }
        }

        // build the instances

        for (String projectId : projectIds) {
            String[] regionIds =
                    openstackApp.getTokenManager().getTokenHolder().getRegions(projectId, "network", "public");
            for (String regionId : regionIds) {
                NeutronApi neutronApi = openstackApp.getNeutronApi(neutronVersions, projectId, regionId);
                Iterator<JsonNetwork> jsonNetworks = neutronApi.getNeutronNetwork().getIterator();
                List<JsonNetworkUuid> networkIds = new ArrayList<>();

                while (jsonNetworks.hasNext()) {
                    JsonNetwork jsonNetwork = jsonNetworks.next();
                    JsonNetworkUuid id = new JsonNetworkUuid();
                    id.setUuid(jsonNetwork.getId());
                    if (jsonNetwork.getSubnets().size() != 0) {
                        networkIds.add(id);
                    }
                }
                for (int count = 0; count < 50; count++) {
                    int i = 0;
                    for (JsonNetworkUuid jsonNetworkUuid : networkIds) {
                        JsonImage image = openstackApp.getNovaApi(novaVersions, projectId, regionId).getNovaImages()
                                .getIterator().next();
                        JsonFlavor flavor = openstackApp.getNovaApi(novaVersions, projectId, regionId).getNovaFlavors()
                                .getIterator().next();
                        JsonServer jsonServer = new JsonServer();
                        jsonServer.setName("instance-" + i + "-" + count);
                        jsonServer.setImage(image);
                        jsonServer.setFlavor(flavor);
                        jsonServer.setTenantId(projectId);
                        jsonServer.setImageRef(image.getId());
                        jsonServer.setFlavorRef(flavor.getId());
                        List<JsonNetworkUuid> foo = new ArrayList<>();
                        foo.add(jsonNetworkUuid);
                        jsonServer.setNetworks(foo);
                        JsonServers jsonServers = new JsonServers();
                        jsonServers.setServer(jsonServer);
                        if (addInstances) {
                            openstackApp.getNovaApi(novaVersions, projectId, regionId).getNovaServers()
                                    .createInstance(jsonServers);
                        }
                    }
                }
            }
        }
    }

    private void printOutImages(final OpenstackApi openstackApp, final String projectId, final String regionId)
            throws NoSupportedApiVersion {
        Iterator<JsonImage> jsonImages =
                openstackApp.getNovaApi(novaVersions, projectId, regionId).getNovaImages().getIterator();
        while (jsonImages.hasNext()) {
            System.out.println(projectId + " " + regionId + " >>> " + RestUtils.toJson(jsonImages.next()));
        }
    }

    private String toJson(final Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String jsonRep = "";
        jsonRep = mapper.writeValueAsString(object);
        return jsonRep;
    }
}
