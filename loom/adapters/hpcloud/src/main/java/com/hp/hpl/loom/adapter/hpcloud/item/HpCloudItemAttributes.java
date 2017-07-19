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
package com.hp.hpl.loom.adapter.hpcloud.item;

import java.text.ParseException;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hp.hpl.loom.adapter.hpcloud.db.ISO8601DateParser;
import com.hp.hpl.loom.adapter.hpcloud.records.Payload;
import com.hp.hpl.loom.adapter.hpcloud.records.Source;
import com.hp.hpl.loom.adapter.hpcloud.records.Version;
import com.hp.hpl.loom.model.CoreItemAttributes;

public class HpCloudItemAttributes extends CoreItemAttributes {
    // VARIABLES --------------------------------------------------------------
    private String hpCloudId = null;

    private Date import_isodate = null;
    private long import_ts = 0;

    private String record_id = null;
    private Date record_isodate = null;
    private long record_ts = 0;

    private Date batch_isodate = null;
    private long batch_ts = 0;

    @JsonIgnore
    private Source source = null;
    @JsonIgnore
    private Version version = null;
    @JsonIgnore
    private Payload payload = null;

    // METHODS ----------------------------------------------------------------

    public String getHpCloudId() {
        return hpCloudId;
    }

    public void setHpCloudId(String hpCloudId) {
        this.hpCloudId = hpCloudId;
    }

    public Date getImport_isodate() {
        return import_isodate;
    }

    public void setImport_isodate(final Date import_isodate) {
        this.import_isodate = import_isodate;
    }

    public void setImport_isodate(final String import_isodate) throws ParseException {
        // // TODO: Implement parsing
        // throw new RuntimeException("Not implemented yet"); // To be deleted in the future

        this.import_isodate = ISO8601DateParser.parse(import_isodate);
    }

    public long getImport_ts() {
        return import_ts;
    }

    public void setImport_ts(final long import_ts) {
        this.import_ts = import_ts;
    }

    public void setImport_ts(final String import_ts) {
        this.import_ts = Long.parseLong(import_ts);
    }

    public String getRecord_id() {
        return record_id;
    }

    public void setRecord_id(final String record_id) {
        this.record_id = record_id;
    }

    public Date getRecord_isodate() {
        return record_isodate;
    }

    public void setRecord_isodate(final Date record_isodate) {
        this.record_isodate = record_isodate;
    }

    public void setRecord_isodate(final String record_isodate) throws ParseException {
        // // TODO: Implement parsing
        // throw new RuntimeException("Not implemented yet"); // To be deleted in the future

        this.record_isodate = ISO8601DateParser.parse(record_isodate);
    }

    public long getRecord_ts() {
        return record_ts;
    }

    public void setRecord_ts(final long record_ts) {
        this.record_ts = record_ts;
    }

    public void setRecord_ts(final String record_ts) {
        this.record_ts = Long.parseLong(record_ts);
    }

    public Date getBatch_isodate() {
        return batch_isodate;
    }

    public void setBatch_isodate(final Date batch_isodate) {
        this.batch_isodate = batch_isodate;
    }

    public void setBatch_isodate(final String batch_isodate) throws ParseException {
        // // TODO: Implement parsing
        // throw new RuntimeException("Not implemented yet"); // To be deleted in the future

        this.batch_isodate = ISO8601DateParser.parse(batch_isodate);
    }

    public long getBatch_ts() {
        return batch_ts;
    }

    public void setBatch_ts(final long batch_ts) {
        this.batch_ts = batch_ts;
    }

    public void setBatch_ts(final String batch_ts) {
        this.batch_ts = Long.parseLong(batch_ts);
    }

    public Source getSource() {
        return source;
    }

    public void setSource(final Source source) {
        this.source = source;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(final Version version) {
        this.version = version;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(final Payload payload) {
        this.payload = payload;
    }

    public String getAttributeAsString(String attribute) {
        String result;

        if (attribute.startsWith("payload.")) {
            attribute = attribute.substring("payload.".length());
            result = payload.getAttribute(attribute).toString();
        } else if (attribute.startsWith("source.")) {
            attribute = attribute.substring("source.".length());
            switch (attribute) {
                case "type":
                    result = source.getType();
                    break;
                case "location":
                    result = source.getLocation();
                    break;
                case "system":
                    result = source.getSystem();
                    break;
                default:
                    result = null;
            }
        } else if (attribute.startsWith("version.")) {
            attribute = attribute.substring("version.".length());
            switch (attribute) {
                case "major":
                    result = version.getMajor();
                    break;
                case "minor":
                    result = version.getMajor();
                    break;
                default:
                    result = null;
            }
        } else {
            switch (attribute) {
                // Saul: These fields are ordered attending to how often they are used based on my
                // experience
                case "_id":
                    result = getHpCloudId();
                    break;
                case "record_id":
                    result = getRecord_id();
                    break;
                // Saul: From here on, they are in alphabetical order
                case "batch_isodate":
                    result = getBatch_isodate().toString();
                    break;
                case "batch_ts":
                    result = Long.toString(getBatch_ts());
                    break;
                case "import_isodate":
                    result = getImport_isodate().toString();
                    break;
                case "import_ts":
                    result = Long.toString(getImport_ts());
                    break;
                case "record_isodate":
                    result = getRecord_isodate().toString();
                    break;
                case "record_ts":
                    result = Long.toString(getRecord_ts());
                    break;
                default:
                    result = null;
                    break;
            }
        }

        return result;
    }

    public void setRecordAttributes(String hpCloudId, Date import_isodate, long import_ts, String record_id,
            Date record_isodate, long record_ts, Date batch_isodate, long batch_ts) {
        this.hpCloudId = hpCloudId;
        this.import_isodate = import_isodate;
        this.import_ts = import_ts;
        this.record_id = record_id;
        this.record_isodate = record_isodate;
        this.record_ts = record_ts;
        this.batch_isodate = batch_isodate;
        this.batch_ts = batch_ts;
    }

    // /**
    // * Copies all the attributes of this item into the one received as parameter. Notice that this
    // * will keep the references, so changing the former one will also change the latter!
    // *
    // * @param target Item into which the attributes should be copied
    // */
    // public void copyInto(final HpCloudItem target) {
    // target.setRecordAttributes(_id, import_isodate, import_ts, record_id, record_isodate,
    // record_ts, batch_isodate,
    // batch_ts);
    // target.setSource(source);
    // target.setVersion(version);
    // target.setPayload(payload);
    // }

    // METHODS - END ----------------------------------------------------------
}
