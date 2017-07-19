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
package com.hp.hpl.loom.adapter.hpcloud;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ItemCollector;
import com.hp.hpl.loom.adapter.hpcloud.db.MongoDbConnection;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.ChefClientType;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.ChefEnvironmentType;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.ChefNodeType;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.ChefOrgType;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.ChefUploadedCookbookType;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.ChefUserType;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.HostType;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.VmType;
import com.hp.hpl.loom.adapter.os.fake.FakeProviderImpl;
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

public class HpCloudAdapter extends BaseAdapter {

    @SuppressWarnings("unused")
    private static Logger log = Logger.getLogger(HpCloudAdapter.class);

    public static final String PATTERN = "HP-Cloud-Testing-Pattern";

    public static final HostType hostType = new HostType();
    public static final VmType vmType = new VmType();
    public static final ChefClientType chefClientType = new ChefClientType();
    public static final ChefEnvironmentType chefEnvironmentType = new ChefEnvironmentType();
    public static final ChefNodeType chefNodeType = new ChefNodeType();
    public static final ChefOrgType chefOrgType = new ChefOrgType();
    public static final ChefUploadedCookbookType chefUploadedCookbookType = new ChefUploadedCookbookType();
    public static final ChefUserType chefUserType = new ChefUserType();

    private MongoDbConnection dbConn;

    public HpCloudAdapter() {
        super();
        try {
            dbConn = new MongoDbConnection();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not establish a connection to the database");
        }
    }

    @Override
    public Map<String, QuadFunctionMeta> registerQueryOperations(
            final Map<String, QuadFunction<PipeLink<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, PipeLink<Fibre>>> map) {
        // Saul, TODO: Review (this was copied from another adapter)
        return new HashMap<String, QuadFunctionMeta>(0);
    }

    @Override
    public Collection<ItemType> getItemTypes() {
        Collection<ItemType> types = new ArrayList<ItemType>();
        types.add(hostType);
        types.add(vmType);
        types.add(chefClientType);
        types.add(chefEnvironmentType);
        types.add(chefNodeType);
        types.add(chefOrgType);
        types.add(chefUploadedCookbookType);
        types.add(chefUserType);
        return types;
    }

    @Override
    public Collection<PatternDefinition> getPatternDefinitions() {
        // Saul, TODO: Review (this function was copied from another adapter and I don' really
        // understand what's happening here)
        List<ItemType> itemTypes = getItemTypesFromLocalIds(Arrays.asList(HostType.TYPE_LOCAL_ID, VmType.TYPE_LOCAL_ID,
                ChefClientType.TYPE_LOCAL_ID, ChefEnvironmentType.TYPE_LOCAL_ID, ChefNodeType.TYPE_LOCAL_ID,
                ChefOrgType.TYPE_LOCAL_ID, ChefUploadedCookbookType.TYPE_LOCAL_ID, ChefUserType.TYPE_LOCAL_ID));
        PatternDefinition patternDef =
                createPatternDefinitionWithSingleInputPerThread(PATTERN, itemTypes, "Testing", null, true, null);

        Collection<PatternDefinition> list = new ArrayList<PatternDefinition>();
        list.add(patternDef);
        return list;
    }

    @Override
    protected ItemCollector getNewItemCollectorInstance(final Session session, final Credentials creds) {
        return new HpCloudCollector(session, this, adapterManager);
    }

    @Override
    protected Provider createProvider(final String providerType, final String providerId, final String authEndpoint,
            final String providerName) {
        // Saul, TODO: Review (this was copied from another adapter)
        // return new com.hp.hpl.loom.model.ProviderImpl(providerType, providerId, authEndpoint,
        // providerName);
        return new FakeProviderImpl(providerType, providerId, authEndpoint, providerName, "test",
                this.getClass().getPackage().getName());
    }

    public MongoDbConnection getDbConnection() {
        return dbConn;
    }

    @Override
    public Collection<Class> getAnnotatedItemsClasses() {
        return new ArrayList<Class>();
    }

}
