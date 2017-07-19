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
package com.hp.hpl.loom.adapter.hpcloud.db;

import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.hp.hpl.loom.adapter.hpcloud.HpCloudAdapter;
import com.hp.hpl.loom.adapter.hpcloud.item.HostItemAttributes;
import com.hp.hpl.loom.adapter.hpcloud.item.HpCloudItem;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.HostType;

public class MongoDbConnectionTestClass {

    static MongoDbConnection dbConn;

    public static void main(final String[] args) {
        // Setup! Necessary to prevent execution errors!
        HpCloudAdapter.hostType.setId("host"); // This is done at AdapterManagerImpl
        HpCloudAdapter.vmType.setId("vm"); // This is done at AdapterManagerImpl

        try {
            List<String> ids;
            HpCloudItem item;
            String id;
            int i;

            dbConn = new MongoDbConnection();

            ids = dbConn.getAllIds("source.type", "host");
            id = ids.get(0);
            System.out.println("--- HOSTS ---");
            i = 0;
            for (String s : ids) {
                System.out.printf("%d:\t%s\n", i++, s);
            }
            System.out.println();

            ids = dbConn.getAllIds("source.type", "vm");
            System.out.println("--- VMS ---");
            i = 0;
            for (String s : ids) {
                System.out.printf("%d:\t%s\n", i++, s);
            }
            System.out.println();
            item = new HpCloudItem(id, HpCloudAdapter.hostType);
            HostItemAttributes itemAttributes = new HostItemAttributes();
            item.setCore(itemAttributes);
            dbConn.getFullItem(itemAttributes, id, HostType.PAYLOAD_FIELDS);

            Map<String, Object> conditions = new Hashtable<String, Object>();
            conditions.put("source.type", "host");
            conditions.put("payload.os_vendor", "HP");

            ids = dbConn.getAllIds(conditions);
            System.out.println("--- HOSTS where { 'payload.os_vendor' : 'HP' } ---");
            i = 0;
            for (String s : ids) {
                System.out.printf("%d:\t%s\n", i++, s);
            }
            System.out.println();

            ids = dbConn.getAllIds("source.type", "vm");
            id = ids.get(0);
            String fields[] = {"_id", "record_id", "source.type", "source.location", "payload.ref_datastore",
                    "payload.datacenterName", "payload.addresses.0.ref_network"};
            System.out.printf("--- Some attributes for object with _id=%s ---\n", id);
            for (String s : fields) {
                System.out.printf("%s:\t%s\n", s, dbConn.getAttribute(id, s));
            }
            System.out.println();

            System.out.println("--- End of tests ---");
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
