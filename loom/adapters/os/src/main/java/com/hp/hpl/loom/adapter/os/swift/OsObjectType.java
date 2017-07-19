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
import com.hp.hpl.loom.exceptions.AttributeException;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.relationships.RelationshipUtil;

public class OsObjectType extends OsItemType {
    private static final Log LOG = LogFactory.getLog(OsObjectType.class);
    public static final String TYPE_LOCAL_ID = "swobject";

    public OsObjectType(final Provider provider) {
        super(TYPE_LOCAL_ID);
        try {
            Attribute etag =
                    new Attribute.Builder(CORE_NAME + "etag").name("Etag").visible(true).plottable(false).build();

            Attribute lastModified = new Attribute.Builder(CORE_NAME + "lastModified").name("Last Modified")
                    .visible(true).plottable(false).build();

            Attribute size = new NumericAttribute.Builder(CORE_NAME + "size").name("Last Size").visible(true)
                    .plottable(true).min("0").max("Inf").unit("MB").build();

            Attribute container = new Attribute.Builder(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                    provider.getProviderType(), TYPE_LOCAL_ID, OsContainerType.TYPE_LOCAL_ID)).name("Container")
                            .visible(true).plottable(false).build();

            addAttributes(etag, lastModified, size, container);
            addOperations(DefaultOperations.SORT_BY.toString(),
                    new OperationBuilder().add(etag, lastModified, size, container).build());
            addOperations(DefaultOperations.GROUP_BY.toString(),
                    new OperationBuilder().add(etag, lastModified, size, container).build());
        } catch (AttributeException e) {
            LOG.error("Problem constructing OsObjectType attributes: ", e);
        }
    }

}
