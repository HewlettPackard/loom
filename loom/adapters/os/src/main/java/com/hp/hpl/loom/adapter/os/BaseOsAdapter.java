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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.os.discover.OsWorkloadType;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.tapestry.PatternDefinition;

public abstract class BaseOsAdapter extends BaseAdapter {

    // private static final int DEFAULT_FIBRES = 45;
    private static final int DEFAULT_PATTERN_NBR = 2;
    private static final int DEFAULT_ITEMTYPE_NBR = 7;

    public static final String ALL_FIVE_PATTERN = "allFivePattern";
    public static final String REG_PROJ_PATTERN = "regProjpattern";
    public static final String SUBNET_PATTERN = "subnetPattern";

    protected int itemTypeNbr = DEFAULT_ITEMTYPE_NBR;
    protected int patternNbr = DEFAULT_PATTERN_NBR;

    protected Collection<ItemType> itemTypes;
    protected Collection<PatternDefinition> patterns;

    public static final OsProjectType OS_PROJECT_TYPE = new OsProjectType();
    public static final OsRegionType OS_REGION_TYPE = new OsRegionType();
    public static OsInstanceType OS_INSTANCE_TYPE = null;
    public static OsVolumeType OS_VOLUME_TYPE = null;
    public static OsImageType OS_IMAGE_TYPE = null;
    public static OsNetworkType OS_NETWORK_TYPE = null;
    public static OsSubnetType OS_SUBNET_TYPE = null;
    public static final OsWorkloadType OS_WORKLOAD_TYPE = new OsWorkloadType();


    // public BaseOsAdapter(final String providerType, final String providerId, final String
    // authEndpoint,
    // final String providerName) {
    // super(providerType, providerId, authEndpoint, providerName);
    // }

    @Override
    protected Provider createProvider(final String providerType, final String providerId, final String authEndpoint,
            final String providerName) {
        return new KeystoneProviderImpl(providerType, providerId, authEndpoint, providerName, adapterConfig);
    }

    // Adapter methods

    @Override
    public Collection<ItemType> getItemTypes() {
        if (itemTypes == null) {
            itemTypes = createItemTypes().values();
        }
        return itemTypes;
    }

    protected Map<String, ItemType> createItemTypes() {
        OS_INSTANCE_TYPE = new OsInstanceType(provider);
        OS_VOLUME_TYPE = new OsVolumeType(provider);
        OS_IMAGE_TYPE = new OsImageType(provider);
        OS_NETWORK_TYPE = new OsNetworkType(provider);
        OS_SUBNET_TYPE = new OsSubnetType(provider);


        Map<String, ItemType> newMap = new HashMap<>(itemTypeNbr);
        newMap.put(OsProjectType.TYPE_LOCAL_ID, OS_PROJECT_TYPE);
        newMap.put(OsRegionType.TYPE_LOCAL_ID, OS_REGION_TYPE);
        newMap.put(OsInstanceType.TYPE_LOCAL_ID, OS_INSTANCE_TYPE);
        newMap.put(OsVolumeType.TYPE_LOCAL_ID, OS_VOLUME_TYPE);
        newMap.put(OsImageType.TYPE_LOCAL_ID, OS_IMAGE_TYPE);
        newMap.put(OsNetworkType.TYPE_LOCAL_ID, OS_NETWORK_TYPE);
        newMap.put(OsSubnetType.TYPE_LOCAL_ID, OS_SUBNET_TYPE);
        return newMap;
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
        newMap.put(ALL_FIVE_PATTERN, createFiveThreadsPattern(true));
        newMap.put(REG_PROJ_PATTERN, createRegProjPattern(false));
        return newMap;
    }

    private PatternDefinition createFiveThreadsPattern(final boolean defaultPattern) {
        List<ItemType> itemTypesNew =
                getItemTypesFromLocalIds(Arrays.asList(OsInstanceType.TYPE_LOCAL_ID, OsVolumeType.TYPE_LOCAL_ID,
                        OsImageType.TYPE_LOCAL_ID, OsNetworkType.TYPE_LOCAL_ID, OsSubnetType.TYPE_LOCAL_ID));
        return createPatternDefinitionWithSingleInputPerThread(ALL_FIVE_PATTERN, itemTypesNew, "Infrastructure", null,
                defaultPattern, null);
    }

    protected PatternDefinition createRegProjPattern(final boolean defaultPattern) {
        List<ItemType> itemTypesNew =
                getItemTypesFromLocalIds(Arrays.asList(OsProjectType.TYPE_LOCAL_ID, OsRegionType.TYPE_LOCAL_ID));
        return createPatternDefinitionWithSingleInputPerThread(REG_PROJ_PATTERN, itemTypesNew, "Regions & Projects",
                null, defaultPattern, null);
    }

    //
    // private PatternDefinition createSubnetsPattern() {
    // List<ItemType> itemTypesNew =
    // getItemTypesFromLocalIds(Arrays.asList(OsSubnetType.TYPE_LOCAL_ID));
    // List<Integer> maxFibres = Arrays.asList(DEFAULT_FIBRES);
    // return createPatternDefinitionWithSingleInputPerThread(SUBNET_PATTERN, itemTypesNew,
    // "Test Subnet Pattern",
    // maxFibres, false);
    // }

    // // For Testing only
    // public PatternDefinition getDefaultPatternDefinition() {
    // return getPatternDefinition(ALL_FIVE_PATTERN);
    // }

    public Collection<PatternDefinition> getAllPatterns() {
        return this.getPatternDefinitions();
    }

}
