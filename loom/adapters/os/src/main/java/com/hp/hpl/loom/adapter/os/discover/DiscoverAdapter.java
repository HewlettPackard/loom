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
package com.hp.hpl.loom.adapter.os.discover;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.loom.adapter.ItemCollector;
import com.hp.hpl.loom.adapter.os.BaseOsAdapter;
import com.hp.hpl.loom.adapter.os.OsImageType;
import com.hp.hpl.loom.adapter.os.OsInstanceType;
import com.hp.hpl.loom.adapter.os.OsNetworkType;
import com.hp.hpl.loom.adapter.os.OsProjectType;
import com.hp.hpl.loom.adapter.os.OsRegionType;
import com.hp.hpl.loom.adapter.os.OsSubnetType;
import com.hp.hpl.loom.adapter.os.OsVolumeType;
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

public class DiscoverAdapter extends BaseOsAdapter {
    /**
     * Pattern containing only VMs.
     */
    public static final String INSTANCES_PATTERN = "instancesPattern";

    /**
     * Pattern containing all virtual infrastructure resources.
     */
    public static final String ALL_INFRASTRUCTURE_PATTERN = "allInfrastructurePattern";

    /**
     * Pattern containing all virtual infrastructure resources except Images and Subnets.
     */
    public static final String INFRASTRUCTURE_PATTERN = "infrastructurePattern";

    /**
     * Pattern containing just Workloads.
     */
    public static final String WORKLOADS_PATTERN = "workloadPattern";

    /**
     * Pattern containing Workloads, Projects, Regions and Instances.
     */
    public static final String OVERVIEW_PATTERN = "overviewPattern";

    // public DiscoverAdapter(final String providerType, final String providerId, final String
    // authEndpoint,
    // final String providerName) {
    // super(providerType, providerId, authEndpoint, providerName);
    // itemTypeNbr = 8;
    // patternNbr = 4;
    // }

    public DiscoverAdapter() {
        super();
        itemTypeNbr = 8;
        patternNbr = 4;
    }


    @Override
    protected Provider createProvider(final String providerType, final String providerId, final String authEndpoint,
            final String providerName) {
        return new FakeProviderImpl(providerType, providerId, authEndpoint, providerName, "discover",
                this.getClass().getPackage().getName());
    }

    // own methods
    @Override
    protected ItemCollector getNewItemCollectorInstance(final Session session, final Credentials creds) {
        DiscoverConfig dc = new DiscoverConfig();
        dc.loadFromProperties(adapterConfig.getPropertiesConfiguration());
        return new DiscoverItemCollector(session, this, adapterManager, creds, provider.getAuthEndpoint(), dc,
                dc.getIndex());
    }

    /**
     * Adds workload ItemType to super class ones.
     */
    @Override
    protected Map<String, ItemType> createItemTypes() {
        Map<String, ItemType> newMap = super.createItemTypes();
        newMap.put(OsWorkloadType.TYPE_LOCAL_ID, OS_WORKLOAD_TYPE);
        return newMap;
    }

    @Override
    public Collection<Class> getAnnotatedItemsClasses() {
        Collection<Class> types = new ArrayList<Class>();
        return types;
    }

    /**
     * Ignores super class patterns and registers its own.
     */
    @Override
    protected Map<String, PatternDefinition> createPatterns() {
        Map<String, PatternDefinition> newMap = new HashMap<>(patternNbr);
        newMap.put(OVERVIEW_PATTERN, createOverviewPattern(false));
        newMap.put(INFRASTRUCTURE_PATTERN, createInfrastructurePattern(false));
        newMap.put(ALL_INFRASTRUCTURE_PATTERN, createInfrastructureAllPattern(false));
        newMap.put(INSTANCES_PATTERN, createInstancesPattern(true));
        // newMap.put(WORKLOADS_PATTERN, createWorkloadsPattern(false));
        return newMap;
    }

    // private PatternDefinition createWorkloadsPattern(final boolean defaultPattern) {
    // List<ItemType> itemTypes =
    // getItemTypesFromLocalIds(Arrays.asList(OsWorkloadType.TYPE_LOCAL_ID));
    // List<Integer> maxFibres = Arrays.asList(45);
    // return createPatternDefinitionWithSingleInputPerThread(WORKLOADS_PATTERN, itemTypes,
    // "Workloads", maxFibres,
    // defaultPattern);
    // }

    private PatternDefinition createInstancesPattern(final boolean defaultPattern) {
        List<ItemType> itemTypes = getItemTypesFromLocalIds(Arrays.asList(OsInstanceType.TYPE_LOCAL_ID));
        return createPatternDefinitionWithSingleInputPerThread(INSTANCES_PATTERN, itemTypes, "Instances", null,
                defaultPattern, null);
    }

    private PatternDefinition createInfrastructurePattern(final boolean defaultPattern) {
        List<ItemType> itemTypes = getItemTypesFromLocalIds(
                Arrays.asList(OsInstanceType.TYPE_LOCAL_ID, OsVolumeType.TYPE_LOCAL_ID, OsNetworkType.TYPE_LOCAL_ID));
        return createPatternDefinitionWithSingleInputPerThread(INFRASTRUCTURE_PATTERN, itemTypes, "Infrastructure",
                null, defaultPattern, null);
    }

    private PatternDefinition createInfrastructureAllPattern(final boolean defaultPattern) {
        List<ItemType> itemTypes =
                getItemTypesFromLocalIds(Arrays.asList(OsInstanceType.TYPE_LOCAL_ID, OsVolumeType.TYPE_LOCAL_ID,
                        OsImageType.TYPE_LOCAL_ID, OsNetworkType.TYPE_LOCAL_ID, OsSubnetType.TYPE_LOCAL_ID));
        return createPatternDefinitionWithSingleInputPerThread(ALL_INFRASTRUCTURE_PATTERN, itemTypes,
                "Infrastructure (All)", null, defaultPattern, null);
    }

    private PatternDefinition createOverviewPattern(final boolean defaultPattern) {
        List<ItemType> itemTypes = getItemTypesFromLocalIds(Arrays.asList(OsWorkloadType.TYPE_LOCAL_ID,
                OsProjectType.TYPE_LOCAL_ID, OsRegionType.TYPE_LOCAL_ID, OsInstanceType.TYPE_LOCAL_ID));
        return createPatternDefinitionWithSingleInputPerThread(OVERVIEW_PATTERN, itemTypes, "Overview", null,
                defaultPattern, null);
    }

    @Override
    public Map<String, QuadFunctionMeta> registerQueryOperations(
            final Map<String, QuadFunction<PipeLink<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, PipeLink<Fibre>>> allFunctions) {

        return new HashMap<>(0);
    }
}
