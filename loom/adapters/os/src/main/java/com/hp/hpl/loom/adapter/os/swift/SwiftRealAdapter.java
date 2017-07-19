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
package com.hp.hpl.loom.adapter.os.swift;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.loom.adapter.ItemCollector;
import com.hp.hpl.loom.adapter.os.OsProjectType;
import com.hp.hpl.loom.adapter.os.OsRegionType;
import com.hp.hpl.loom.adapter.os.real.RealAdapter;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.tapestry.PatternDefinition;

public class SwiftRealAdapter extends RealAdapter {
    public static final String ALL_SWIFT_PATTERN = "allSwiftPattern";
    public static OsAccountType OS_ACCOUNT_TYPE = null;
    public static OsContainerType OS_CONTAINER_TYPE = null;
    public static OsObjectType OS_OBJECT_TYPE = null;

    protected boolean swiftOnly = false;

    // public SwiftRealAdapter(final String providerType, final String providerId, final String
    // authEndpoint,
    // final String providerName) {
    // super(providerType, providerId, authEndpoint, providerName);
    // }

    protected boolean getSwiftOnly() {
        return swiftOnly;
    }

    @Override
    protected Map<String, ItemType> createItemTypes() {

        if (OS_ACCOUNT_TYPE != null) {
            OS_ACCOUNT_TYPE = new OsAccountType(this.getProvider());
        }

        if (OS_CONTAINER_TYPE != null) {
            OS_CONTAINER_TYPE = new OsContainerType(this.getProvider());
        }

        if (OS_OBJECT_TYPE != null) {
            OS_OBJECT_TYPE = new OsObjectType(this.getProvider());
        }

        Map<String, ItemType> newMap;
        if (swiftOnly) {
            newMap = new HashMap<>(5);
            newMap.put(OsProjectType.TYPE_LOCAL_ID, OS_PROJECT_TYPE);
            newMap.put(OsRegionType.TYPE_LOCAL_ID, OS_REGION_TYPE);
        } else {
            newMap = super.createItemTypes();
        }
        newMap.put(OsAccountType.TYPE_LOCAL_ID, OS_ACCOUNT_TYPE);
        newMap.put(OsContainerType.TYPE_LOCAL_ID, OS_CONTAINER_TYPE);
        newMap.put(OsObjectType.TYPE_LOCAL_ID, OS_OBJECT_TYPE);
        return newMap;
    }

    @Override
    protected Map<String, PatternDefinition> createPatterns() {
        Map<String, PatternDefinition> newMap;
        if (swiftOnly) {
            newMap = new HashMap<>(2);
            newMap.put(REG_PROJ_PATTERN, createRegProjPattern(false));
        } else {
            newMap = super.createPatterns();
        }
        newMap.put(ALL_SWIFT_PATTERN, createSwiftPattern(swiftOnly));
        return newMap;
    }

    private PatternDefinition createSwiftPattern(final boolean defaultPattern) {
        List<ItemType> itemTypes = getItemTypesFromLocalIds(
                Arrays.asList(OsAccountType.TYPE_LOCAL_ID, OsContainerType.TYPE_LOCAL_ID, OsObjectType.TYPE_LOCAL_ID));
        return createPatternDefinitionWithSingleInputPerThread(ALL_SWIFT_PATTERN, itemTypes, "Object Storage (Swift)",
                null, defaultPattern, null);
    }

    // own methods
    @Override
    protected ItemCollector getNewItemCollectorInstance(final Session session, final Credentials creds) {
        return new SwiftRealItemCollector(session, this, adapterManager, creds);
    }
}
