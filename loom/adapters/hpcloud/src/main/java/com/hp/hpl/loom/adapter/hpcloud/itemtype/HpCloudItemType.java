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
package com.hp.hpl.loom.adapter.hpcloud.itemtype;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.Attribute;
import com.hp.hpl.loom.adapter.OperationBuilder;
import com.hp.hpl.loom.exceptions.AttributeException;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.model.ItemType;

public class HpCloudItemType extends ItemType {
    private static final Log LOG = LogFactory.getLog(ChefNodeType.class);

    public HpCloudItemType(final String localId) {
        super(localId);

        try {
            Attribute id = new Attribute.Builder("_id").name("_id").visible(true).plottable(false).build();

            Attribute importIsodate = new Attribute.Builder("import_isodate").name("import_isodate").visible(true)
                    .plottable(false).build();

            Attribute importTs =
                    new Attribute.Builder("import_ts").name("import_ts").visible(true).plottable(false).build();

            Attribute recordId =
                    new Attribute.Builder("record_id").name("record_id").visible(true).plottable(false).build();

            Attribute recordTs =
                    new Attribute.Builder("record_ts").name("record_ts").visible(true).plottable(false).build();

            Attribute recordIsodate = new Attribute.Builder("record_isodate").name("record_isodate").visible(true)
                    .plottable(false).build();

            Attribute batchTs =
                    new Attribute.Builder("batch_ts").name("batch_ts").visible(true).plottable(false).build();

            Attribute batchIsodate =
                    new Attribute.Builder("batch_isodate").name("batch_isodate").visible(true).plottable(false).build();

            Attribute sourceType =
                    new Attribute.Builder("source.type").name("source.type").visible(true).plottable(false).build();

            Attribute sourceLocation = new Attribute.Builder("source.location").name("source.location").visible(true)
                    .plottable(false).build();

            Attribute sourceSystem =
                    new Attribute.Builder("source.system").name("source.system").visible(true).plottable(false).build();

            Attribute versionMajor =
                    new Attribute.Builder("version.major").name("version.major").visible(true).plottable(false).build();

            Attribute versionMinor =
                    new Attribute.Builder("version.minor").name("version.minor").visible(true).plottable(false).build();

            this.addAttributes(id, importIsodate, importTs, recordId, recordTs, recordIsodate, batchTs, batchIsodate,
                    sourceType, sourceLocation, sourceSystem, versionMajor, versionMinor);

            this.addOperations(DefaultOperations.SORT_BY.toString(),
                    new OperationBuilder().add(id, importIsodate, importTs, recordId, recordTs, recordIsodate, batchTs,
                            batchIsodate, sourceType, sourceLocation, sourceSystem, versionMajor, versionMinor)
                            .build());

        } catch (AttributeException e) {
            LOG.error("Problem creating ChefUploadedCookbookType", e);
        }
    }

}
