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

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.Attribute;
import com.hp.hpl.loom.adapter.OperationBuilder;
import com.hp.hpl.loom.adapter.OrderedString;
import com.hp.hpl.loom.exceptions.AttributeException;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.relationships.RelationshipUtil;

public class OsNetworkType extends OsItemType {
    public static final String TYPE_LOCAL_ID = "network";
    private static final Log LOG = LogFactory.getLog(OsItemType.class);

    public OsNetworkType(final Provider provider) {
        super(TYPE_LOCAL_ID);
        try {
            Attribute status =
                    new Attribute.Builder(CORE_NAME + "status").name("Status").visible(true).plottable(false).build();

            Attribute adminStateUp = new Attribute.Builder(CORE_NAME + "adminStateUp").name("Admin State Up")
                    .visible(true).plottable(false).build();

            Attribute shared =
                    new Attribute.Builder(CORE_NAME + "shared").name("Shared").visible(true).plottable(false).build();

            // Relationships
            Attribute region = new Attribute.Builder(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                    provider.getProviderType(), TYPE_LOCAL_ID, OsRegionType.TYPE_LOCAL_ID)).name("Region").visible(true)
                            .plottable(false).build();

            Attribute project = new Attribute.Builder(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                    provider.getProviderType(), TYPE_LOCAL_ID, OsProjectType.TYPE_LOCAL_ID)).name("Project")
                            .visible(true).plottable(false).build();

            addAttributes(status, adminStateUp, shared, region, project);


            Set<OrderedString> sortOperations = new OperationBuilder().add(status, region, project).build();

            Set<OrderedString> groupOperations =
                    new OperationBuilder().add(status, adminStateUp, shared, region, project).build();


            addOperations(DefaultOperations.SORT_BY.toString(), sortOperations);
            addOperations(DefaultOperations.GROUP_BY.toString(), groupOperations);
        } catch (AttributeException e) {
            LOG.error("Problem constructing OsNetworkType attributes: ", e);
        }
    }

}
