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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hp.hpl.loom.adapter.hpcloud.HpCloudAdapter;
import com.hp.hpl.loom.adapter.hpcloud.item.HpCloudItemAttributes;
import com.hp.hpl.loom.adapter.hpcloud.records.Payload;
import com.hp.hpl.loom.adapter.hpcloud.records.Source;
import com.hp.hpl.loom.adapter.hpcloud.records.Version;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class MongoDbConnection {

    // CONSTANTS --------------------------------------------------------------

    // TODO These should fo to the hpcloudAdapter.properties file and read from there in the Adapter
    // and passed through until they end up here
    protected static final String defaultDbName = "hpcloud";
    protected static final String defaultCollectionName = "test";
    protected static final String defaultHost = "localhost";
    protected static final int defaultPort = 27017;
    // CONSTANTS - END --------------------------------------------------------

    // VARIABLES --------------------------------------------------------------
    private static Logger log = Logger.getLogger(HpCloudAdapter.class);

    protected MongoClient mongoClient;
    protected DB db;
    protected DBCollection coll;

    // VARIABLES - END --------------------------------------------------------

    // CONSTRUCTORS -----------------------------------------------------------

    public MongoDbConnection() throws UnknownHostException {
        this(defaultHost, defaultPort);
    }

    public MongoDbConnection(final String host, final int port) throws UnknownHostException {
        mongoClient = new MongoClient(host, port);

        // For these tests, the database is going to be protected against writes
        // mongoClient.fsyncAndLock();

        db = mongoClient.getDB(defaultDbName);
        coll = db.getCollection(defaultCollectionName);

        log.debug("Saul: MongoDB connection created");
    }

    // CONSTRUCTORS - END -----------------------------------------------------

    // METHODS ----------------------------------------------------------------

    public String getAttribute(final String id, final String attribute) {
        DBObject query = new BasicDBObject("_id", id);
        DBObject obj = coll.findOne(query);

        String[] fields = attribute.split("\\."); // Split string at dots
        int i;
        for (i = 0; i < fields.length - 1; i++) {
            obj = (DBObject) obj.get(fields[i]);
        }

        return obj.get(fields[i]).toString();
    }

    public <T extends HpCloudItemAttributes> void getFullItem(final T hpCloudItemAttributes, final String id,
            final String[] payloadFields) {
        // String itemType;
        DBObject query = new BasicDBObject("_id", id);
        DBObject obj = coll.findOne(query);

        // itemType = obj.get("source.type").toString(); // To be deleted. Dot notation doesn't work
        // itemType = ((DBObject) obj.get("source")).get("type").toString();
        // switch (itemType) {
        // case HostType.TYPE_LOCAL_ID:
        // item = new HostItem(id, HpCloudAdapter.hostType);
        // payloadFields = HostType.PAYLOAD_FIELDS;
        // break;
        // case VmType.TYPE_LOCAL_ID:
        // item = new VmItem(id, HpCloudAdapter.vmType);
        // payloadFields = VmType.PAYLOAD_FIELDS;
        // break;
        // default:
        // // If the type can't be found, an item can't be returned
        // return null;
        // }

        try {

            // HpCloudItemAttributes hpCloudItemAttributes = new HpCloudItemAttributes();
            // item.setCore(hpCloudItemAttributes);
            // --- Quick and dirty... - TODO: Review and adapt if necessary
            // -----------------------------------------------
            Object o;
            hpCloudItemAttributes.setItemId(id);

            hpCloudItemAttributes.setHpCloudId(id);

            hpCloudItemAttributes.setBatch_ts(obj.get("batch_ts").toString());
            o = obj.get("batch_isodate");
            if (o instanceof Date) {
                hpCloudItemAttributes.setBatch_isodate((Date) o);
            } else {
                hpCloudItemAttributes.setBatch_isodate(o.toString());
            }

            hpCloudItemAttributes.setImport_ts(obj.get("import_ts").toString());
            o = obj.get("import_isodate");
            if (o instanceof Date) {
                hpCloudItemAttributes.setImport_isodate((Date) o);
            } else {
                hpCloudItemAttributes.setImport_isodate(o.toString());
            }

            hpCloudItemAttributes.setRecord_id(obj.get("record_id").toString());
            hpCloudItemAttributes.setRecord_ts(obj.get("record_ts").toString());
            o = obj.get("record_isodate");
            if (o instanceof Date) {
                hpCloudItemAttributes.setRecord_isodate((Date) o);
            } else {
                hpCloudItemAttributes.setRecord_isodate(o.toString());
            }

            Source source = new Source();
            source.setLocation(((DBObject) obj.get("source")).get("location").toString());
            source.setSystem(((DBObject) obj.get("source")).get("system").toString());
            source.setType(((DBObject) obj.get("source")).get("type").toString());
            hpCloudItemAttributes.setSource(source);

            Version version = new Version();
            version.setMajor(((DBObject) obj.get("version")).get("major").toString());
            // Watch out! Not all the items include a minor version!
            // version.setMinor(((DBObject) obj.get("version")).get("minor").toString());
            hpCloudItemAttributes.setVersion(version);

            Payload payload = new Payload();
            for (String s : payloadFields) {
                Object pl = ((DBObject) obj.get("payload")).get(s);
                if (pl != null) {
                    payload.setAttribute(s, pl.toString());
                } else {
                    payload.setAttribute(s, null);
                }
            }
            hpCloudItemAttributes.setPayload(payload);
            // --- Quick and dirty... - END
            // -------------------------------------------------------------------------------
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public List<String> getAllIds(final String key, final Object value) {
        Map<String, Object> conditions = new Hashtable<String, Object>();
        conditions.put(key, value);
        return getAllIds(conditions);
    }

    public List<String> getAllIds(final Map<String, Object> conditions) {
        DBObject query = new BasicDBObject(conditions);
        DBCursor cursor = coll.find(query);

        List<String> ids = new ArrayList<String>(cursor.count());
        for (DBObject o : cursor) {
            ids.add(o.get("_id").toString());
        }

        return ids;
    }

    // METHODS - END ----------------------------------------------------------

}
