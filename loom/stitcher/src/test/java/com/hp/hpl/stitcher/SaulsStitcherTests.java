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

import com.hp.hpl.loom.adapter.hpcloud.db.MongoDbConnection;
import com.hp.hpl.loom.adapter.hpcloud.item.ChefClientItem;
import com.hp.hpl.loom.adapter.hpcloud.item.ChefClientItemAttributes;
import com.hp.hpl.loom.adapter.hpcloud.item.ChefOrgItem;
import com.hp.hpl.loom.adapter.hpcloud.item.ChefOrgItemAttributes;
import com.hp.hpl.loom.adapter.hpcloud.item.HostItem;
import com.hp.hpl.loom.adapter.hpcloud.item.HostItemAttributes;
import com.hp.hpl.loom.adapter.hpcloud.item.HpCloudItem;
import com.hp.hpl.loom.adapter.hpcloud.item.VmItem;
import com.hp.hpl.loom.adapter.hpcloud.item.VmItemAttributes;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.ChefClientType;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.ChefOrgType;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.HostType;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.VmType;
import com.hp.hpl.stitcher.extras.ChefClientChefOrgStitchChecker;
import com.hp.hpl.stitcher.extras.VmHostStitchChecker;

public class SaulsStitcherTests {

    private static final class DnsApiRecordDnsApiZoneQuickAndDirtyComparator implements StitchChecker<String, String> {
        Map<String, String> DnsApiZoneIds, DnsApiRecordZoneIds;

        public DnsApiRecordDnsApiZoneQuickAndDirtyComparator(List<String> baseElements,
                List<String> candidateElements) {
            DnsApiZoneIds = new HashMap<String, String>(baseElements.size());
            DnsApiRecordZoneIds = new HashMap<String, String>(candidateElements.size());

            for (String s : baseElements) {
                DnsApiZoneIds.put(s, dbConn.getAttribute(s, "payload.id"));
            }
            for (String s : candidateElements) {
                DnsApiRecordZoneIds.put(s, dbConn.getAttribute(s, "payload.zone_id"));
            }
        }

        public double checkStitch(String baseElement, String candidateElement) {
            return (DnsApiZoneIds.get(baseElement).equals(DnsApiRecordZoneIds.get(candidateElement)) ? 1.0 : 0.0);
        }
    }

    static HostType hostType = new HostType();
    static VmType vmType = new VmType();
    static ChefClientType chefClientType = new ChefClientType();
    static ChefOrgType chefOrgType = new ChefOrgType();
    static MongoDbConnection dbConn;

    public static void main(String[] args) {
        hostType.setId("host"); // Necessary to prevent errors
        vmType.setId("vm"); // Necessary to prevent errors
        chefClientType.setId("chef_client"); // Necessary to prevent errors
        chefOrgType.setId("chef_org"); // Necessary to prevent errors

        int howManyRuns = 1;

        for (int i = 0; i < howManyRuns; i++) {
            testsUsingMongoDB(args);
        }
    }

    public static void testsUsingMongoDB(String[] args) {

        // Stitcher and stitcher (Vm-Host)
        // Stitcher<VmItem, HostItem> stitcher;
        // Map<VmItem, Collection<HostItem>> stitches;

        // Stitcher and stitcher (ChefClient-ChefOrg)
        // Stitcher<ChefClientItem, ChefOrgItem> stitcher;
        // Map<ChefClientItem, Collection<ChefOrgItem>> stitches;

        // Stitcher and stitcher (DnsApiRecord-DnsApiZone)
        Stitcher<String, String> stitcher;
        Map<String, Collection<String>> stitches;

        try {

            dbConn = new MongoDbConnection();

            // Prepare stitcher (Vm-Host)
            // stitcher = new BruteStitcher<VmItem, HostItem>();
            // stitcher.addBaseElements(getVms());
            // stitcher.addCandidateElements(getHosts());
            // stitcher.setStitchChecker(new VmHostStitchChecker());

            // Prepare stitcher (ChefClient-ChefOrg)
            // stitcher = new BruteStitcher<ChefClientItem, ChefOrgItem>();
            // stitcher.addBaseElements(getChefClients());
            // stitcher.addCandidateElements(getChefOrg());
            // stitcher.setStitchChecker(new ChefClientChefOrgStitchChecker());

            // Prepare stitcher (DnsApiRecord-DnsApiZone)
            stitcher = new BruteStitcher<String, String>();
            List<String> baseElements = dbConn.getAllIds("source.type", "dnsapi_zone");
            stitcher.addBaseElements(baseElements);
            List<String> candidateElements = dbConn.getAllIds("source.type", "dnsapi_record");
            stitcher.addCandidateElements(candidateElements);
            stitcher.addStitchChecker(
                    new DnsApiRecordDnsApiZoneQuickAndDirtyComparator(baseElements, candidateElements));

            // Get stitches
            long startTime, stopTime;
            startTime = System.currentTimeMillis();
            stitches = stitcher.stitch();
            stopTime = System.currentTimeMillis();

            // Print stitches (Vm-Host)
            // for (VmItem vm : stitches.keySet()) {
            // System.out.printf("Stitches found for %s (VmItem):\n", vm.getCore().getHpCloudId());
            // for (HostItem host : stitches.get(vm)) {
            // System.out.printf("- %s (HostItem)\n", host.getCore().getHpCloudId());
            // }
            // System.out.println();
            // }

            System.out.printf("Elapsed time (seconds): %f\n", ((double) (stopTime - startTime) / 1000));
            long stitchCount = 0;
            for (Object o : stitches.keySet()) {
                stitchCount += stitches.get(o).size();
            }
            System.out.printf("No. base elements: %d - No. stitches found: %d\n", stitches.keySet().size(),
                    stitchCount);

            System.out.println();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    private static List<HostItem> getHosts() {
        List<String> ids = dbConn.getAllIds("source.type", "host");
        List<HostItem> hosts = new ArrayList<HostItem>(ids.size());
        for (String _id : ids) {
            HostItem host = new HostItem(_id, hostType);
            host.setCore(new HostItemAttributes());
            dbConn.getFullItem(host.getCore(), _id, HostType.PAYLOAD_FIELDS);
            hosts.add(host);
        }

        return hosts;
    }

    private static List<VmItem> getVms() {
        List<String> ids = dbConn.getAllIds("source.type", "vm");
        List<VmItem> vms = new ArrayList<VmItem>(ids.size());
        for (String _id : ids) {
            VmItem vm = new VmItem(_id, vmType);
            vm.setCore(new VmItemAttributes());
            dbConn.getFullItem(vm.getCore(), _id, VmType.PAYLOAD_FIELDS);
            vms.add(vm);
        }

        return vms;
    }

    private static List<ChefClientItem> getChefClients() {
        List<String> ids = dbConn.getAllIds("source.type", "chef_client");
        List<ChefClientItem> chefClients = new ArrayList<ChefClientItem>(ids.size());
        for (String _id : ids) {
            ChefClientItem chefClient = new ChefClientItem(_id, chefClientType);
            chefClient.setCore(new ChefClientItemAttributes());
            dbConn.getFullItem(chefClient.getCore(), _id, ChefClientType.PAYLOAD_FIELDS);
            chefClients.add(chefClient);
        }

        return chefClients;
    }

    private static List<ChefOrgItem> getChefOrg() {
        List<String> ids = dbConn.getAllIds("source.type", "chef_org");
        List<ChefOrgItem> chefOrgs = new ArrayList<ChefOrgItem>(ids.size());
        for (String _id : ids) {
            ChefOrgItem chefOrg = new ChefOrgItem(_id, chefOrgType);
            chefOrg.setCore(new ChefOrgItemAttributes());
            dbConn.getFullItem(chefOrg.getCore(), _id, ChefOrgType.PAYLOAD_FIELDS);
            chefOrgs.add(chefOrg);
        }

        return chefOrgs;
    }

}
