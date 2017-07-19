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
package com.hp.hpl.loom.adapter.cubesensors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ItemCollector;
import com.hp.hpl.loom.manager.query.OperationContext;
import com.hp.hpl.loom.manager.query.OperationErrorCode;
import com.hp.hpl.loom.manager.query.PipeLink;
import com.hp.hpl.loom.manager.query.QuadFunction;
import com.hp.hpl.loom.manager.query.QueryOperation;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.tapestry.PatternDefinition;

/**
 * The main implementation class for the CubeSensorsAdapter - it extends the BaseAdatper to provide
 * data on the cube sensors. https://my.cubesensors.com/docs
 *
 * Types are registered via the getItemTypes and getAnnotatedItemsClasses.
 *
 */
public class CubeSensorsAdapter extends BaseAdapter {
    /**
     * Test pattern for the DeviceItem type.
     */
    public static final String DEVICE_PATTERN = "testingPattern";


    @Override
    public Collection<ItemType> getItemTypes() {
        Collection<ItemType> types = new ArrayList<ItemType>();
        return types;
    }

    @Override
    public Collection<Class> getAnnotatedItemsClasses() {
        Collection<Class> types = new ArrayList<Class>();
        types.add(DeviceItem.class);
        return types;
    }

    @Override
    public Collection<PatternDefinition> getPatternDefinitions() {
        List<ItemType> itemTypes = getItemTypesFromLocalIds(Arrays.asList(Types.DEVICE_TYPE_LOCAL_ID));
        PatternDefinition patternDef =
                createPatternDefinitionWithSingleInputPerThread(DEVICE_PATTERN, itemTypes, "Devices", null, true);

        Collection<PatternDefinition> list = new ArrayList<PatternDefinition>();
        list.add(patternDef);
        return list;
    }

    @Override
    protected ItemCollector getNewItemCollectorInstance(final Session session, final Credentials creds) {
        return new DeviceCollector(session, this, adapterManager);
    }

    @Override
    protected Provider createProvider(final String providerType, final String providerId, final String authEndpoint,
            final String providerName) {
        return new com.hp.hpl.loom.model.ProviderImpl(providerType, providerId, authEndpoint, providerName);
    }

    @Override
    @SuppressWarnings("checkstyle:linelength")
    public Map<String, QueryOperation> registerQueryOperations(
            final Map<String, QuadFunction<PipeLink<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, PipeLink<Fibre>>> map) {
        // we declare a map to contain a key with the name of the new operation and the actual
        // operation itself
        // in this example we will declare just one operation
        Map<String, QueryOperation> ops = new HashMap<>(1);


        return ops;
    }

}
