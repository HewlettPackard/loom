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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ItemCollector;
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

public class TestBaseAdapterImpl extends BaseAdapter {

    public static final String MAP1_PATTERN = "map1";

    protected int itemTypeNbr = 7;
    protected int patternNbr = 2;

    protected Collection<ItemType> itemTypes;
    protected Collection<PatternDefinition> patterns;

    public TestBaseAdapterImpl() {}

    @Override
    protected Provider createProvider(final String providerType, final String providerId, final String authEndpoint,
            final String providerName) {
        return new FakeProviderImpl(providerType, providerId, authEndpoint, providerName, "test",
                this.getClass().getPackage().getName());
    }

    @Override
    public Collection<ItemType> getItemTypes() {
        if (itemTypes == null) {
            itemTypes = createItemTypes().values();
        }
        return itemTypes;
    }

    public Map<String, ItemType> createItemTypes() {
        Map<String, ItemType> newMap = new HashMap<>(itemTypeNbr);
        newMap.put("map1", new ItemType("map1"));
        newMap.put("map1/A", new ItemType("map1/A"));
        newMap.put("map1/As", new ItemType("map1/As"));
        newMap.put("map1s/A", new ItemType("map1s/A"));
        return newMap;
    }

    @Override
    public Collection<Class> getAnnotatedItemsClasses() {
        Collection<Class> types = new ArrayList<Class>();
        return types;
    }

    @Override
    public Collection<PatternDefinition> getPatternDefinitions() {
        if (patterns == null) {
            patterns = createPatterns().values();
        }
        return patterns;
    }

    protected Map<String, PatternDefinition> createPatterns() {
        Map<String, PatternDefinition> newMap = new HashMap<>(patternNbr);
        newMap.put(MAP1_PATTERN, createMap1Pattern());
        return newMap;
    }

    @Override
    protected ItemCollector getNewItemCollectorInstance(final Session session, final Credentials creds) {
        return new TestItemCollectorImpl(session);
    }

    private PatternDefinition createMap1Pattern() {
        List<ItemType> itemTypes = getItemTypesFromLocalIds(Arrays.asList("map1"));
        List<Integer> maxFibres = Arrays.asList(45);
        return createPatternDefinitionWithSingleInputPerThread(MAP1_PATTERN, itemTypes, "Test Map1 Pattern", maxFibres,
                false, null);
    }

    @Override
    public Map<String, QuadFunctionMeta> registerQueryOperations(
            final Map<String, QuadFunction<PipeLink<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, PipeLink<Fibre>>> allFunctions) {

        return new HashMap<>(0);
    }
}
