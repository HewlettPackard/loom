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
package com.hp.hpl.loom.adapter.os.deltas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.hp.hpl.loom.adapter.ItemCollector;
import com.hp.hpl.loom.adapter.os.BaseOsAdapter;
import com.hp.hpl.loom.adapter.os.fake.FakeConfig;
import com.hp.hpl.loom.adapter.os.fake.FakeProviderImpl;
import com.hp.hpl.loom.manager.query.OperationContext;
import com.hp.hpl.loom.manager.query.OperationErrorCode;
import com.hp.hpl.loom.manager.query.PipeLink;
import com.hp.hpl.loom.manager.query.QuadFunction;
import com.hp.hpl.loom.manager.query.QuadFunctionMeta;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.Session;

public class DeltaAdapter extends BaseOsAdapter {
    // private static final Log LOG = LogFactory.getLog(DeltaAdapter.class);

    protected FakeConfig fc;


    // public FakeAdapter(final String providerType, final String providerId, final String
    // authEndpoint,
    // final String providerName) {
    // super(providerType, providerId, authEndpoint, providerName);
    // }

    public FakeConfig getConfig() {
        if (fc == null) {
            fc = new FakeConfig();
            fc.loadFromProperties(adapterConfig.getPropertiesConfiguration());
        }

        return fc;
    }

    @Override
    public Collection<Class> getAnnotatedItemsClasses() {
        Collection<Class> types = new ArrayList<Class>();
        return types;
    }

    @Override
    protected Provider createProvider(final String providerType, final String providerId, final String authEndpoint,
            final String providerName) {
        return new FakeProviderImpl(providerType, providerId, authEndpoint, providerName, "test",
                this.getClass().getPackage().getName());
    }

    // own methods
    @Override
    protected ItemCollector getNewItemCollectorInstance(final Session session, final Credentials creds) {
        return new DeltaItemCollector(session, this, adapterManager, creds, getConfig());
    }

    @Override
    public Map<String, QuadFunctionMeta> registerQueryOperations(
            final Map<String, QuadFunction<PipeLink<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, PipeLink<Fibre>>> allFunctions) {
        return null;
    }
}
