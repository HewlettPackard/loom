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
package com.hp.hpl.loom.adapter.keystonev3.items;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.Attribute;
import com.hp.hpl.loom.adapter.OperationBuilder;
import com.hp.hpl.loom.exceptions.AttributeException;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.SeparableItemType;
import com.hp.hpl.loom.relationships.RelationshipUtil;

public class ProjectType extends SeparableItemType {
    private static final Log LOG = LogFactory.getLog(ProjectType.class);
    public static final String TYPE_LOCAL_ID = "project";

    public ProjectType(final Provider provider) {
        super(TYPE_LOCAL_ID);

        try {

            Attribute enabled =
                    new Attribute.Builder(CORE_NAME + "enabled").name("Enabled").visible(true).plottable(false).build();
            // relations
            Attribute domain = new Attribute.Builder(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                    provider.getProviderType(), TYPE_LOCAL_ID, DomainType.TYPE_LOCAL_ID)).name("Domain").visible(true)
                            .plottable(false).build();

            Attribute role = new Attribute.Builder(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                    provider.getProviderType(), TYPE_LOCAL_ID, RoleType.TYPE_LOCAL_ID)).name("Role").visible(true)
                            .plottable(false).build();

            Attribute user = new Attribute.Builder(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                    provider.getProviderType(), TYPE_LOCAL_ID, UserType.TYPE_LOCAL_ID)).name("User").visible(true)
                            .plottable(false).build();


            this.addAttributes(enabled, domain, role, user);
            this.addOperations(DefaultOperations.SORT_BY.toString(),
                    new OperationBuilder().add(enabled, domain, role, user).build());
            this.addOperations(DefaultOperations.GROUP_BY.toString(),
                    new OperationBuilder().add(enabled, user, role, domain).build());
        } catch (AttributeException e) {
            LOG.error("Problem constructing ProjectType attributes: ", e);
        }
    }

}
