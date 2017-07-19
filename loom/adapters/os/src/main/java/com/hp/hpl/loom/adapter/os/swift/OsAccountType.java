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
package com.hp.hpl.loom.adapter.os.swift;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.Attribute;
import com.hp.hpl.loom.adapter.NumericAttribute;
import com.hp.hpl.loom.adapter.OperationBuilder;
import com.hp.hpl.loom.adapter.os.OsItemType;
import com.hp.hpl.loom.adapter.os.OsProjectType;
import com.hp.hpl.loom.adapter.os.OsRegionType;
import com.hp.hpl.loom.exceptions.AttributeException;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.relationships.RelationshipUtil;

public class OsAccountType extends OsItemType {
    private static final Log LOG = LogFactory.getLog(OsAccountType.class);
    public static final String TYPE_LOCAL_ID = "account";

    public OsAccountType(final Provider provider) {
        super(TYPE_LOCAL_ID);

        try {
            Attribute containerCount =
                    new NumericAttribute.Builder(CORE_NAME + "containerCount").name("Container Count").visible(true)
                            .plottable(true).min("0").max("Inf").name("containers").build();

            Attribute bytesUsed = new NumericAttribute.Builder(CORE_NAME + "bytesUsed").name("Bytes Used").visible(true)
                    .plottable(true).min("0").max("Inf").name("MB").build();

            Attribute objectCount = new NumericAttribute.Builder(CORE_NAME + "objectCount").name("Object Count")
                    .visible(true).plottable(true).min("0").max("Inf").name("objects").build();

            Attribute region = new Attribute.Builder(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                    provider.getProviderType(), TYPE_LOCAL_ID, OsRegionType.TYPE_LOCAL_ID)).name("Region").visible(true)
                            .plottable(false).build();

            Attribute project = new Attribute.Builder(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                    provider.getProviderType(), TYPE_LOCAL_ID, OsProjectType.TYPE_LOCAL_ID)).name("Project")
                            .visible(true).plottable(false).build();

            Attribute container = new Attribute.Builder(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                    provider.getProviderType(), TYPE_LOCAL_ID, OsContainerType.TYPE_LOCAL_ID)).name("Container")
                            .visible(true).plottable(false).build();

            addAttributes(containerCount, bytesUsed, objectCount, region, project, container);
            addOperations(DefaultOperations.SORT_BY.toString(), new OperationBuilder()
                    .add(containerCount, bytesUsed, objectCount, region, project, container).build());
            addOperations(DefaultOperations.GROUP_BY.toString(), new OperationBuilder()
                    .add(bytesUsed, containerCount, objectCount, region, project, container).build());

        } catch (AttributeException e) {
            LOG.error("Problem constructing OsAccountType attributes: ", e);
        }
    }

}
