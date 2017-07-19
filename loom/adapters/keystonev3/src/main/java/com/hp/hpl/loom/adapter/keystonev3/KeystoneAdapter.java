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
package com.hp.hpl.loom.adapter.keystonev3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ItemCollector;
import com.hp.hpl.loom.adapter.keystonev3.items.DomainType;
import com.hp.hpl.loom.adapter.keystonev3.items.ProjectType;
import com.hp.hpl.loom.adapter.keystonev3.items.RoleAssignmentType;
import com.hp.hpl.loom.adapter.keystonev3.items.RoleType;
import com.hp.hpl.loom.adapter.keystonev3.items.UserType;
import com.hp.hpl.loom.manager.query.OperationContext;
import com.hp.hpl.loom.manager.query.OperationErrorCode;
import com.hp.hpl.loom.manager.query.PipeLink;
import com.hp.hpl.loom.manager.query.QuadFunction;
import com.hp.hpl.loom.manager.query.QuadFunctionMeta;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.tapestry.PatternDefinition;


public class KeystoneAdapter extends BaseAdapter {

    public static final String ALL_PATTERN = "allPattern";

    public static DomainType DOMAIN_TYPE = null;
    public static ProjectType PROJECT_TYPE = null;
    public static UserType USER_TYPE = null;
    public static RoleType ROLE_TYPE = null;
    public static RoleAssignmentType ROLE_ASSIGNMENT_TYPE = new RoleAssignmentType();

    @Override
    public Collection<ItemType> getItemTypes() {
        if (DOMAIN_TYPE == null) {
            DOMAIN_TYPE = new DomainType(provider);
        }
        if (PROJECT_TYPE == null) {
            PROJECT_TYPE = new ProjectType(provider);
        }
        if (USER_TYPE == null) {
            USER_TYPE = new UserType(provider);
        }
        if (ROLE_TYPE == null) {
            ROLE_TYPE = new RoleType(provider);
        }

        Collection<ItemType> types = new ArrayList<ItemType>();
        types.add(DOMAIN_TYPE);
        types.add(PROJECT_TYPE);
        types.add(USER_TYPE);
        types.add(ROLE_TYPE);
        types.add(ROLE_ASSIGNMENT_TYPE);
        return types;
    }

    @Override
    public Collection<Class> getAnnotatedItemsClasses() {
        return new ArrayList<Class>();
    }

    @Override
    public Collection<PatternDefinition> getPatternDefinitions() {
        List<ItemType> itemTypes = new ArrayList<>();
        itemTypes.add(DOMAIN_TYPE);
        itemTypes.add(PROJECT_TYPE);
        itemTypes.add(USER_TYPE);
        itemTypes.add(ROLE_TYPE);
        PatternDefinition patternDef =
                createPatternDefinitionWithSingleInputPerThread(ALL_PATTERN, itemTypes, "all", null, true, null);
        Collection<PatternDefinition> list = new ArrayList<>();
        list.add(patternDef);
        return list;
    }

    @Override
    protected ItemCollector getNewItemCollectorInstance(final Session session, final Credentials creds) {
        return new KeystoneCollector(session, this, adapterManager, creds.getUsername());
    }

    @Override
    protected Provider createProvider(final String providerType, final String providerId, final String authEndpoint,
            final String providerName) {
        return new SelfRevokingKeystoneV3Provider(providerType, providerId, authEndpoint, providerName,
                this.getClass().getPackage().getName());
        // return new KeystoneV3Provider(providerType, providerId, authEndpoint, providerName);
    }

    @Override
    public Map<String, QuadFunctionMeta> registerQueryOperations(
            final Map<String, QuadFunction<PipeLink<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, PipeLink<Fibre>>> map) {
        Map<String, QuadFunctionMeta> ops = new HashMap<>(0);
        return ops;
    }

}
