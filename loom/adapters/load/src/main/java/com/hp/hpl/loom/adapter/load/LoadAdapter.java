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
package com.hp.hpl.loom.adapter.load;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ItemCollector;
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

/**
 * The main implementation class for the LoadAdapter - it extends the BaseAdapter
 *
 * Types are registered via the getItemTypes and getAnnotatedItemsClasses.
 *
 */
public class LoadAdapter extends BaseAdapter {
    /**
     * Test pattern for the FileItem type.
     */
    public static final String TESTING_PATTERN = "All Pattern";
    private Collection<ItemType> types = new ArrayList<ItemType>();

    @Override
    public Collection<ItemType> getItemTypes() {
        return new ArrayList<ItemType>();
    }

    @Override
    public Collection<Class> getAnnotatedItemsClasses() {
        return new ArrayList<Class>();
    }

    @Override
    public Collection<PatternDefinition> getPatternDefinitions() {

        Collection<PatternDefinition> list = new ArrayList<PatternDefinition>();
        return list;
    }

    @Override
    protected ItemCollector getNewItemCollectorInstance(final Session session, final Credentials creds) {
        return new LoadCollector(session, this, adapterManager, creds);
    }

    @SuppressWarnings("checkstyle:linelength")
    @Override
    protected Provider createProvider(final String providerType, final String providerId, final String authEndpoint,
            final String providerName) {
        return new com.hp.hpl.loom.model.ProviderImpl(providerType, providerId, authEndpoint, providerName,
                this.getClass().getPackage().getName().toString());
    }

    @Override
    @SuppressWarnings("checkstyle:linelength")
    public Map<String, QuadFunctionMeta> registerQueryOperations(
            final Map<String, QuadFunction<PipeLink<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, PipeLink<Fibre>>> map) {

        Map<String, QuadFunctionMeta> ops = new HashMap<>(0);
        return ops;
    }

}
