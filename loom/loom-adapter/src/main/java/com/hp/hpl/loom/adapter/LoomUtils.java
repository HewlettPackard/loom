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
package com.hp.hpl.loom.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.hp.hpl.loom.exceptions.NullItemTypeIdException;
import com.hp.hpl.loom.exceptions.NullPatternIdException;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.manager.query.QueryOperation;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.tapestry.Meta;
import com.hp.hpl.loom.tapestry.Operation;
import com.hp.hpl.loom.tapestry.PatternDefinition;
import com.hp.hpl.loom.tapestry.QueryDefinition;
import com.hp.hpl.loom.tapestry.ThreadDefinition;

/**
 * This class handles the setting of key entity id's such as {@link PatternDefinition} and
 * {@link ItemType} ids. Having the a common util ensures consist construction.
 */
public final class LoomUtils {
    /**
     * The separator used for Logical ids.
     */
    private static final String LOGICAL_SEPARATOR = "/";
    /**
     * The separator used for ID creation.
     */
    private static final String ID_SEPARATOR = "-";

    /**
     * The number of slashes before encountering the id.
     */
    private static final int NUMBER_SLASHES = 3;


    /**
     * Private constructor which throws an exception if you try and construct.
     */
    private LoomUtils() {
        throw new AssertionError("Not to be constructed");
    }

    /**
     * Sets the {@link PatternDefinition} id based on the provided provider and PatternDefinitionId.
     * The id is of the form 'providerType-patternId'.
     *
     * @param provider Provider to type to build id from
     * @param pattern PatternDefinition to set the id for
     * @throws NullPatternIdException Thrown if the patternId is null
     */
    public static void setPatternId(final Provider provider, final PatternDefinition pattern)
            throws NullPatternIdException {
        validateProvider(provider);
        if (pattern == null) {
            throw new IllegalArgumentException("Pattern is null");
        }
        String patternId = pattern.getId();
        if (patternId == null) {
            throw new NullPatternIdException("wrong id for pattern: " + pattern.getId());
        }
        // make sure that patternId is unique across provider types
        pattern.setId(provider.getProviderType() + ID_SEPARATOR + patternId);
    }

    private static void validateProvider(final Provider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("cannot parse a null provider");
        }
    }

    /**
     * Un-set the {@link PatternDefinition} id based on the provided provider. It sets it back to
     * 'patternId'.
     *
     * @param provider Provider to type to build id from
     * @param pattern PatternDefinition to set the id for
     */
    public static void unsetPatternId(final Provider provider, final PatternDefinition pattern) {
        validateProvider(provider);
        String patternId = pattern.getId();
        // make sure that patternId is unique across provider types

        String str = provider.getProviderType() + ID_SEPARATOR;
        if (patternId.startsWith(str)) {
            pattern.setId(patternId.substring(str.length()));
        }
    }

    /**
     * Setting id based on the value got for localId.
     *
     * @param provider Provider to type to build id from
     * @param itemType ItemType to set the id for
     * @throws NullItemTypeIdException Thrown if the typeLocalId is null
     */
    public static void setId(final Provider provider, final ItemType itemType) throws NullItemTypeIdException {
        validateProvider(provider);
        if (itemType == null) {
            throw new IllegalArgumentException("ItemType is null");
        }
        String typeLocalId = itemType.getLocalId();
        if (typeLocalId == null) {
            throw new NullItemTypeIdException("Wrong type id: " + typeLocalId);
        }
        // make sure that itemTypeId is unique across provider types
        itemType.setId(LoomUtils.getItemTypeId(provider, typeLocalId));
    }

    /**
     * Returns a ItemTypeId based on the provider type, ID_SEPERATOR and itemTypeLocalId. The id is
     * of the form 'providerType-itemTypeLocalId'.
     *
     * @param provider Provider to type to build id from
     * @param itemTypeLocalId itemtype to build id from
     * @return The ItemTypeId string
     */
    public static String getItemTypeId(final Provider provider, final String itemTypeLocalId) {
        validateProvider(provider);
        return getItemTypeIdFromProviderType(provider.getProviderType(), itemTypeLocalId);
    }

    /**
     * Returns a ItemTypeId based on the provider type, ID_SEPERATOR and itemTypeLocalId. The id is
     * of the form 'providerType-itemTypeLocalId'.
     *
     * @param providerType Type of the Provider to build id from.
     * @param itemTypeLocalId itemtype to build id from.
     * @return The ItemTypeId string
     */
    public static String getItemTypeIdFromProviderType(final String providerType, final String itemTypeLocalId) {
        return providerType + ID_SEPARATOR + itemTypeLocalId;
    }

    /**
     * Returns a MergedLogicalIdFromItemType based on the merging of provider type and
     * itemTypeLocalId. The id is of the form 'providerType/itemTypeLocalId(s)' with an 's' on the
     * end.
     *
     * @param provider Provider to type to build id from
     * @param itemTypeLocalId itemtype to build id from
     * @param localId the local id
     * @return The merged ItemTypeId string
     */
    public static String getMergedLogicalIdFromItemType(final Provider provider, final String itemTypeLocalId,
            final String localId) {
        validateProvider(provider);
        validateItemTypeLocalId(itemTypeLocalId);
        String id = provider.getProviderType() + LOGICAL_SEPARATOR + itemTypeLocalId + "s";
        if (localId != null) {
            id += LOGICAL_SEPARATOR + localId;
        }
        return id;
    }

    /**
     * Gets the logical Id based on the aggregation and itemId. The id is of the form
     * 'aggregaton.getLogicalId/itemId'.
     *
     * @param aggregation Aggregation to build the id from (based on the LogicalId)
     * @param itemId The itemId
     * @return The itemLogicalId based on the aggregation and itemId
     */
    public static String getItemLogicalId(final Aggregation aggregation, final String itemId) {
        if (aggregation == null || itemId == null) {
            throw new IllegalArgumentException("cannot build a logicalId based on null args");
        }
        return aggregation.getLogicalId() + LOGICAL_SEPARATOR + itemId;
    }

    /**
     * Gets the derived aggregation logical id from the itemLogicalId.
     *
     * @param itemLogicalId The itemLogicalId to derive from
     * @return The aggregationLogicalId
     */
    public static String deriveAggregationLogicalIdFromItemLogicalId(final String itemLogicalId) {
        if (itemLogicalId == null || !itemLogicalId.contains(LOGICAL_SEPARATOR)) {
            throw new IllegalArgumentException("cannot derive a logicalId based on null args");
        }

        try {
            return itemLogicalId.substring(0, StringUtils.ordinalIndexOf(itemLogicalId, "/", NUMBER_SLASHES)).trim();
        } catch (IndexOutOfBoundsException ex) {
            return itemLogicalId.substring(0, itemLogicalId.lastIndexOf("/")).trim();
        }
    }

    /**
     * Gets the aggregation logical id for the itemType. Takes the form
     * 'providerId/itemTypeLocalIds/localId'.
     *
     * @param provider provider to base the id of
     * @param itemTypeLocalId the itemTypeLocal id
     * @param localId the local id
     * @return The aggregationLogicalId
     */
    public static String getAggregationLogicalIdFromItemType(final Provider provider, final String itemTypeLocalId,
            final String localId) {
        validateProvider(provider);
        validateItemTypeLocalId(itemTypeLocalId);
        String id = provider.getProviderTypeAndId() + LOGICAL_SEPARATOR + itemTypeLocalId + "s";
        if (localId != null) {
            id += LOGICAL_SEPARATOR + localId;
        }
        return id;
    }

    private static void validateItemTypeLocalId(final String itemTypeLocalId) {
        if (itemTypeLocalId == null) {
            throw new IllegalArgumentException("cannot parse a null ItemTypeLocalId");
        }
    }

    /**
     * <<<<<<< HEAD Creates a patternDefinition based on the given ItemTypes.
     *
     * @param id id of the given pattern
     * @param itemTypes list of ItemTypes included in this pattern (1 per Thread)
     * @param description informational value only
     * @param maxFibres list of integers setting the default braiding for each thread in the
     *        pattern.
     * @param defaultPattern boolean set to true if this pattern should be the default one
     * @param provider handle on the provider for the given pattern
     * @param humanReadableThreadNames list of ThreadNames
     * @return a PatternDefinition to be registered with the adapterManager
     */
    public static PatternDefinition createPatternDefinitionWithSingleInputPerThread(final String id,
            final List<ItemType> itemTypes, final String description, final List<Integer> maxFibres,
            final boolean defaultPattern, final Provider provider, List<String> humanReadableThreadNames) {

        if (maxFibres != null && itemTypes.size() != maxFibres.size()) {
            throw new IllegalArgumentException(
                    "itemTypes (" + itemTypes.size() + ") & maxFibres (" + maxFibres.size() + " sizes should match");
        }

        List<Integer> fibreList;
        if (maxFibres == null) {
            fibreList = Collections.nCopies(itemTypes.size(), 0);
        } else {
            fibreList = maxFibres;
        }

        if (humanReadableThreadNames == null) {
            humanReadableThreadNames = new ArrayList<>(itemTypes.size());
            for (ItemType it : itemTypes) {
                humanReadableThreadNames.add(createHumanReadableThreadName(it));
            }
        }

        List<ThreadDefinition> threadDefs = new ArrayList<>(itemTypes.size());
        Map<String, ItemType> itMap = new HashMap<>(itemTypes.size());
        int threadIdx = 0;
        for (ItemType it : itemTypes) {
            List<String> ins = new ArrayList<String>(1);
            ins.add(getAggregationLogicalIdForPattern(provider, it.getLocalId()));
            QueryDefinition query;
            if (fibreList.get(threadIdx) == 0) {
                query = new QueryDefinition(ins);
            } else {
                Map<String, Object> braidParams = new HashMap<String, Object>(1);
                braidParams.put(QueryOperation.MAX_FIBRES, fibreList.get(threadIdx));
                Operation braidOperation = new Operation(DefaultOperations.BRAID.toString(), braidParams);
                List<Operation> braidPipe = new ArrayList<>(1);
                braidPipe.add(braidOperation);
                query = new QueryDefinition(braidPipe, ins);
            }
            ThreadDefinition threadDefinition = new ThreadDefinition(id + "-" + Integer.toString(threadIdx), it.getId(),
                    query, humanReadableThreadNames.get(threadIdx++));
            threadDefs.add(threadDefinition);
            itMap.put(it.getId(), it);
        }

        PatternDefinition pd =
                new PatternDefinition(id, threadDefs, provider.getProviderType(), new Meta(itMap), description);
        pd.setDefaultPattern(defaultPattern);

        return pd;
    }

    /**
     * Creates a human friendly name.
     *
     * @param type ItemType used for a given Thread
     * @return the name to be used for the Thread
     */
    public static String createHumanReadableThreadName(final ItemType type) {
        String localId = type.getLocalId();
        return localId.substring(0, 1).toUpperCase() + localId.substring(1) + "s";
    }

    private static String getAggregationLogicalIdForPattern(final Provider provider, final String itemTypeLocalId) {
        return getMergedLogicalIdFromItemType(provider, itemTypeLocalId, null);
    }

    /**
     * Get provider type from logical id.
     *
     * @param logicalId id of the DA
     * @return prodicer id
     */
    public static String getProviderIdTypeFromLogicalId(final String logicalId) {
        int index = logicalId.indexOf("/");
        return logicalId.substring(0, logicalId.indexOf("/", index + 1));
    }

    /**
     * Get provider logical id from item.
     *
     * @param item item
     * @return provider id
     */
    public static String getProviderIdFromItemLogicalId(final Item item) {
        return getProviderIdTypeFromLogicalId(item.getLogicalId()) + "/" + ProviderType.TYPE_LOCAL_ID + "s";
    }
}
