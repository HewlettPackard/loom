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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.Attribute;
import com.hp.hpl.loom.adapter.NumericAttribute;
import com.hp.hpl.loom.adapter.OperationBuilder;
import com.hp.hpl.loom.exceptions.AttributeException;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.relationships.RelationshipUtil;

public class OsImageType extends OsItemType {
    private static final Log LOG = LogFactory.getLog(OsImageType.class);
    public static final String TYPE_LOCAL_ID = "image";

    public OsImageType(final Provider provider) {
        super(TYPE_LOCAL_ID);
        /* attributes */
        // Note: Openstack assigns quota per project and thus the max for each
        // volume
        // decreases as volumes are created within a project
        // how do we model this?
        // new
        // Attribute.Builder<Attribute.Builder>("status").name("status").visible(true).plotable(false).

        try {
            Attribute index = new NumericAttribute.Builder(CORE_NAME + "idx").name("idx").visible(true).plottable(true)
                    .min("0").max("Inf").unit("BLAH").build();

            Attribute status =
                    new Attribute.Builder(CORE_NAME + "status").name("status").visible(true).plottable(false).build();

            Attribute usageCount = new NumericAttribute.Builder("usageCount").name("Usage Count").visible(true)
                    .plottable(true).min("0").max("Inf").unit("Volumes").build();
            Attribute minDisk = new NumericAttribute.Builder(CORE_NAME + "minDisk").name("Min. Disk").visible(true)
                    .plottable(true).min("0").max("Inf").unit("GB").build();

            Attribute minRam = new NumericAttribute.Builder(CORE_NAME + "minRam").name("Min. RAM").visible(true)
                    .plottable(true).min("0").max("Inf").unit("GB").build();

            Attribute size = new NumericAttribute.Builder(CORE_NAME + "size").name("Size.").visible(true)
                    .plottable(true).min("0").max("Inf").unit("Bytes").build();

            Attribute checksum = new Attribute.Builder(CORE_NAME + "checksum").name("checksum").visible(true)
                    .plottable(false).build();

            Attribute containerFormat = new Attribute.Builder(CORE_NAME + "containerFormat").name("containerFormat")
                    .visible(true).plottable(false).build();

            Attribute diskFormat = new Attribute.Builder(CORE_NAME + "diskFormat").name("diskFormat").visible(true)
                    .plottable(false).build();

            Attribute visibility = new Attribute.Builder(CORE_NAME + "visibility").name("visibility").visible(true)
                    .plottable(false).build();

            Attribute created =
                    new Attribute.Builder(CORE_NAME + "created").name("Created").visible(true).plottable(false).build();

            Attribute updated =
                    new Attribute.Builder(CORE_NAME + "updated").name("Updated").visible(true).plottable(false).build();

            // Relationships
            Attribute region = new Attribute.Builder(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                    provider.getProviderType(), TYPE_LOCAL_ID, OsRegionType.TYPE_LOCAL_ID)).name("Region").visible(true)
                            .plottable(false).build();


            Attribute project = new Attribute.Builder(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                    provider.getProviderType(), TYPE_LOCAL_ID, OsRegionType.TYPE_LOCAL_ID)).name("Project")
                            .visible(true).plottable(false).build();

            addAttributes(status, usageCount, region, project, minDisk, minRam, containerFormat, diskFormat, visibility,
                    size, checksum, created, updated, super.itemName, super.itemDescription);

            addOperations(DefaultOperations.SORT_BY.toString(), new OperationBuilder().add(minDisk, minRam, status,
                    containerFormat, diskFormat, visibility, usageCount, created, updated, region, project).build());
            addOperations(DefaultOperations.GROUP_BY.toString(), new OperationBuilder()
                    .add(project, status, minDisk, minRam, containerFormat, diskFormat, visibility).build());
        } catch (AttributeException e) {
            LOG.error("Problem constructing OsImageType attributes: ", e);
        }

    }
}
