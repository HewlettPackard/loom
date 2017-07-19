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
package com.hp.hpl.stitcher;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.priv.garshol.duke.Configuration;
import no.priv.garshol.duke.Property;
import no.priv.garshol.duke.comparators.ExactComparator;

import com.hp.hpl.loom.adapter.hpcloud.HpCloudAdapter;
import com.hp.hpl.loom.adapter.hpcloud.db.MongoDbConnection;
import com.hp.hpl.loom.adapter.hpcloud.item.ChefClientItem;
import com.hp.hpl.loom.adapter.hpcloud.item.ChefClientItemAttributes;
import com.hp.hpl.loom.adapter.hpcloud.item.ChefOrgItem;
import com.hp.hpl.loom.adapter.hpcloud.item.ChefOrgItemAttributes;
import com.hp.hpl.loom.adapter.hpcloud.item.HostItem;
import com.hp.hpl.loom.adapter.hpcloud.item.HostItemAttributes;
import com.hp.hpl.loom.adapter.hpcloud.item.HpCloudItem;
import com.hp.hpl.loom.adapter.hpcloud.item.HpCloudItemAttributes;
import com.hp.hpl.loom.adapter.hpcloud.item.VmItem;
import com.hp.hpl.loom.adapter.hpcloud.item.VmItemAttributes;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.ChefClientType;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.ChefOrgType;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.HostType;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.VmType;

public class SaulsDukeStitcherTestClass {

    static VmType vmType = new VmType();
    static HostType hostType = new HostType();
    static ChefClientType chefClientType = new ChefClientType();
    static ChefOrgType chefOrgType = new ChefOrgType();
    static MongoDbConnection dbConn;

    public static void main(String[] args) {

        List<String> ids;
        List<VmItem> vms = new ArrayList<VmItem>();
        List<HostItem> hosts = new ArrayList<HostItem>();
        List<ChefClientItem> chefClients = new ArrayList<ChefClientItem>();
        List<ChefOrgItem> chefOrgs = new ArrayList<ChefOrgItem>();
        HpCloudItem<HpCloudItemAttributes> item;
        Stitcher<VmItem, HostItem> vmHostStitcher;
        Map<VmItem, Collection<HostItem>> vmHostStitches;
        Stitcher<HostItem, VmItem> hostVmStitcher;
        Map<HostItem, Collection<VmItem>> hostVmStitches;
        Stitcher<ChefClientItem, ChefOrgItem> chefClientChefOrgStitcher;
        Map<ChefClientItem, Collection<ChefOrgItem>> chefClientChefOrgStitches;
        Configuration config;
        long startTime, stopTime, setupTime, stitchingTime;

        // Setup! Necessary to prevent execution errors!
        vmType.setId("vm");
        hostType.setId("host");
        chefClientType.setId("chef_client");
        chefOrgType.setId("chef_org");

        try {
            dbConn = new MongoDbConnection();

            startTime = System.currentTimeMillis();

            // Get vms
            ids = dbConn.getAllIds("source.type", "vm");
            for (String id : ids) {
                VmItem vm = new VmItem(id, vmType);
                vm.setCore(new VmItemAttributes());
                dbConn.getFullItem(vm.getCore(), id, VmType.PAYLOAD_FIELDS);
                vms.add(vm);
            }

            // Get hosts
            ids = dbConn.getAllIds("source.type", "host");
            for (String id : ids) {
                HostItem host = new HostItem(id, hostType);
                host.setCore(new HostItemAttributes());
                dbConn.getFullItem(host.getCore(), id, HostType.PAYLOAD_FIELDS);
                hosts.add(host);
            }

            // Get chefClients
            // ids = dbConn.getAllIds("source.type", "chef_client");
            // for (String id : ids) {
            // ChefClientItem chefClient = new ChefClientItem(id, chefClientType);
            // chefClient.setCore(new ChefClientItemAttributes());
            // dbConn.getFullItem(chefClient.getCore(), id, ChefClientType.PAYLOAD_FIELDS);
            // chefClients.add(chefClient);
            // }

            // Get chefOrgs
            // ids = dbConn.getAllIds("source.type", "chef_org");
            // for (String id : ids) {
            // ChefOrgItem chefOrg = new ChefOrgItem(id, chefOrgType);
            // chefOrg.setCore(new ChefOrgItemAttributes());
            // dbConn.getFullItem(chefOrg.getCore(), id, ChefOrgType.PAYLOAD_FIELDS);
            // chefOrgs.add(chefOrg);
            // }

            stopTime = System.currentTimeMillis();
            setupTime = stopTime - startTime;

            Map<String, String> vmProps = new HashMap<String, String>();
            vmProps.put("ID", "_id");
            vmProps.put("vmRefHost", "payload.ref_host");
            Map<String, String> hostProps = new HashMap<String, String>();
            hostProps.put("ID", "_id");
            hostProps.put("vmRefHost", "record_id");

            List<Property> properties = new ArrayList<Property>();
            Property idProp = new Property("ID"); // NOTE: Changed to PropertyImpl in Duke 1.3?
            properties.add(idProp);
            // WARNING! Setting the thresholds to 0.0 and 1.0 will make Duke find no matches!
            Property reference = new Property("vmRefHost", new ExactComparator(), 0.01, 0.99);
            properties.add(reference);

            // Vms link to Hosts (expected link)
            config = new Configuration(); // NOTE: Changed to ConfigurationImpl in Duke 1.3?
            config.setProperties(properties);
            config.setThreshold(0.95);
            config.setMaybeThreshold(0.8);
            vmHostStitcher = new HpCloudDukeStitcher<VmItem, HostItem>(config, vmProps, hostProps);
            vmHostStitcher.addBaseElements(vms);
            vmHostStitcher.addCandidateElements(hosts);
            vmHostStitches = vmHostStitcher.stitch();
            printStitches(vmHostStitches);

            // Hosts link to vms (reversed link)
            config = new Configuration(); // NOTE: Changed to ConfigurationImpl in Duke 1.3?
            config.setProperties(properties);
            config.setThreshold(0.95);
            config.setMaybeThreshold(0.8);
            hostVmStitcher = new HpCloudDukeStitcher<HostItem, VmItem>(config, hostProps, vmProps);
            hostVmStitcher.addBaseElements(hosts);
            hostVmStitcher.addCandidateElements(vms);
            hostVmStitches = hostVmStitcher.stitch();
            printStitches(hostVmStitches);

            System.out.println("--- End of tests ---");
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static <B extends HpCloudItem<? extends HpCloudItemAttributes>, C extends HpCloudItem<? extends HpCloudItemAttributes>> void printStitches(
            Map<B, Collection<C>> stitches) {
        for (B b : stitches.keySet()) {
            System.out.printf("Stitches found for <%s> (%s):\n", b.getCore().getAttributeAsString("source.type"),
                    b.getCore().getHpCloudId());
            for (C c : stitches.get(b)) {
                System.out.printf("- <%s> (%s)\n", c.getCore().getAttributeAsString("source.type"),
                        c.getCore().getHpCloudId());
            }
            System.out.println();
        }
    }

}
