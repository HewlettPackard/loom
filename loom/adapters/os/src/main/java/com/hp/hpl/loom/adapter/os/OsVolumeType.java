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

public class OsVolumeType extends OsItemType {
    private static final Log LOG = LogFactory.getLog(OsVolumeType.class);
    public static final String TYPE_LOCAL_ID = "volume";

    public OsVolumeType(final Provider provider) {
        super(TYPE_LOCAL_ID);

        try {

            Attribute status =
                    new Attribute.Builder(CORE_NAME + "status").name("Status").visible(true).plottable(false).build();

            Attribute size = new NumericAttribute.Builder(CORE_NAME + "size").name("Size").visible(true).plottable(true)
                    .min("0").max("Inf").unit("GB").build();

            Attribute availabilityZone = new Attribute.Builder(CORE_NAME + "availabilityZone").name("Availability Zone")
                    .visible(true).plottable(false).build();

            Attribute volumeType = new Attribute.Builder(CORE_NAME + "volumeType").name("Volume Type").visible(true)
                    .plottable(false).build();


            Attribute snapshotId = new Attribute.Builder(CORE_NAME + "snapshotId").name("Snapshot ID").visible(true)
                    .plottable(false).build();

            Attribute description = new Attribute.Builder(CORE_NAME + "description").name("Description").visible(true)
                    .plottable(false).build();

            Attribute created =
                    new Attribute.Builder(CORE_NAME + "created").name("Created").visible(true).plottable(false).build();

            Attribute region = new Attribute.Builder(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                    provider.getProviderType(), TYPE_LOCAL_ID, OsRegionType.TYPE_LOCAL_ID)).name("Region").visible(true)
                            .plottable(false).build();

            Attribute project = new Attribute.Builder(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                    provider.getProviderType(), TYPE_LOCAL_ID, OsProjectType.TYPE_LOCAL_ID)).name("Project")
                            .visible(true).plottable(false).build();

            this.addAttributes(status, size, availabilityZone, volumeType, snapshotId, description, created, region,
                    project);
            this.addOperations(DefaultOperations.SORT_BY.toString(), new OperationBuilder()
                    .add(status, size, created, availabilityZone, description, region, project, volumeType, snapshotId)
                    .build());
            this.addOperations(DefaultOperations.GROUP_BY.toString(),
                    new OperationBuilder().add(status, volumeType, availabilityZone, size, region, project).build());
        } catch (AttributeException e) {
            LOG.error("Problem constructing OsVolumeType attributes: ", e);
        }
    }

}
