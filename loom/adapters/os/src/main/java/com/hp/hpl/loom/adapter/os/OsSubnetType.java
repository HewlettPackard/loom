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

public class OsSubnetType extends OsItemType {
    private static final Log LOG = LogFactory.getLog(OsSubnetType.class);
    public static final String TYPE_LOCAL_ID = "subnet";

    public OsSubnetType(final Provider provider) {
        super(TYPE_LOCAL_ID);
        try {
            // Attributes
            Attribute networkId = new Attribute.Builder(CORE_NAME + "networkId").name("NetworkID").visible(true)
                    .plottable(false).build();

            Attribute gatewayIp = new Attribute.Builder(CORE_NAME + "gatewayIp").name("Gateway IP").visible(true)
                    .plottable(false).build();

            Attribute ipVersion = new NumericAttribute.Builder(CORE_NAME + "ipVersion").name("IP Version").visible(true)
                    .plottable(false).min("0").max("6").unit("Version").build();

            Attribute enableDhcp = new Attribute.Builder(CORE_NAME + "enableDhcp").name("Enable DHCP").visible(true)
                    .plottable(false).build();

            Attribute cidr =
                    new Attribute.Builder(CORE_NAME + "cidr").name("CIDR").visible(true).plottable(false).build();

            // Relationships

            Attribute region = new Attribute.Builder(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                    provider.getProviderType(), TYPE_LOCAL_ID, OsRegionType.TYPE_LOCAL_ID)).name("Region").visible(true)
                            .plottable(false).build();

            Attribute project = new Attribute.Builder(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                    provider.getProviderType(), TYPE_LOCAL_ID, OsProjectType.TYPE_LOCAL_ID)).name("Project")
                            .visible(true).plottable(false).build();

            Attribute network = new Attribute.Builder(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                    provider.getProviderType(), TYPE_LOCAL_ID, OsNetworkType.TYPE_LOCAL_ID)).name("Network")
                            .visible(true).plottable(false).build();

            this.addAttributes(networkId, gatewayIp, ipVersion, enableDhcp, cidr, region, project, network);
            this.addOperations(DefaultOperations.SORT_BY.toString(), new OperationBuilder()
                    .add(gatewayIp, ipVersion, cidr, networkId, region, project, network).build());
            this.addOperations(DefaultOperations.GROUP_BY.toString(), new OperationBuilder()
                    .add(gatewayIp, ipVersion, enableDhcp, cidr, region, project, network).build());
        } catch (AttributeException e) {
            LOG.error("Problem constructing OsSubnetType attributes: ", e);
        }
    }

}
