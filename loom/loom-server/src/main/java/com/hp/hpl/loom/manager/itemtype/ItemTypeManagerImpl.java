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
package com.hp.hpl.loom.manager.itemtype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.hp.hpl.loom.exceptions.DuplicateItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;

/**
 * ItemTypes can be registered by different Providers but only the ProviderType is respected when
 * storing their definitions. An attempt by the same Provider to register the same ItemType will
 * result in an exception, registration of an ItemType with the same ID by different Providers will
 * succeed but any previous definitions will be overwritten.
 */
@Component
public class ItemTypeManagerImpl implements ItemTypeManager {
    private static final int INITIAL_PROVIDER_ITEMTYPES_SIZE = 5;
    private static final int INITIAL_ITEMTYPES_PER_PROVIDER_SIZE = 1;
    private static final int INITIAL_ITEMTYPES_SIZE = 5;

    private Map<Provider, List<String>> providerItemTypes = new HashMap<>(INITIAL_PROVIDER_ITEMTYPES_SIZE);
    private Map<String, ItemType> itemTypes = new HashMap<>(INITIAL_ITEMTYPES_SIZE);

    @Override
    public String addItemType(final Provider provider, final ItemType itemType)
            throws DuplicateItemTypeException, NoSuchProviderException {
        validateProvider(provider);
        validateItemType(provider, itemType);

        String id = itemType.getId();

        if (itemTypes.get(id) != null && (hasProviderRegisteredItemType(provider, id))
                || itemTypeRegisteredByDifferentProviderType(id, provider.getProviderType())) {
            throw new DuplicateItemTypeException(id);
        }

        itemTypes.put(id, itemType);

        List<String> ids = providerItemTypes.get(provider);

        if (ids == null) {
            ids = new ArrayList<>(INITIAL_ITEMTYPES_PER_PROVIDER_SIZE);
            providerItemTypes.put(provider, ids);
        }

        ids.add(id);

        return id;
    }

    private void validateProvider(final Provider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider not specified");
        }
    }

    private boolean itemTypeRegisteredByDifferentProviderType(final String itemTypeId, final String providerType) {
        boolean differentProviderType = false;

        for (Provider p : providerItemTypes.keySet()) {
            List<String> providerIds = providerItemTypes.get(p);

            if (providerIds.contains(itemTypeId) && !p.getProviderType().equals(providerType)) {
                differentProviderType = true;
                break;
            }
        }

        return differentProviderType;
    }

    private boolean hasProviderRegisteredItemType(final Provider provider, final String itemTypeId) {
        List<String> ids = providerItemTypes.get(provider);
        boolean registered = false;

        if (ids != null && ids.contains(itemTypeId)) {
            registered = true;
        }

        return registered;
    }

    @Override
    public ItemType getItemType(final String itemTypeId) {
        if (itemTypeId == null || itemTypeId.isEmpty()) {
            throw new IllegalArgumentException("ItemType ID not specified");
        }
        return itemTypes.get(itemTypeId);
    }

    /**
     * Validate if the itemType is valid to be added to the provided provider (making sure the
     * action id's are unique)
     *
     * @param provider
     * @param itemType
     * @throws NoSuchProviderException
     */
    private void validateItemType(final Provider provider, final ItemType itemType) throws NoSuchProviderException {
        if (itemType == null) {
            throw new IllegalArgumentException("ItemType not specified (Provider: " + provider.toString() + ")");
        } else if (itemType.getId() == null || itemType.getId().isEmpty()) {
            throw new IllegalArgumentException("ItemType ID not specified (Provider: " + provider.toString() + ")");
        }
        // Check if the itemType's actions match an existing itemType from this provider
        List<String> itemTypeIds = providerItemTypes.get(provider);
        if (itemTypeIds != null && !itemTypeIds.isEmpty()) {
            Set<String> existingItemTypeActionIds = new HashSet<>();

            for (String itemTypeId : itemTypeIds) {
                ItemType it = itemTypes.get(itemTypeId);
                Set<String> actionIds = getActionIds(it);
                existingItemTypeActionIds.addAll(actionIds);
            }

            Set<String> newActionIds = getActionIds(itemType);
            for (String id : newActionIds) {
                if (existingItemTypeActionIds.contains(id)) {
                    throw new IllegalArgumentException("ItemType Action " + id
                            + " already exists for this provider (Provider: " + provider.toString() + ")");
                }
            }
        }
    }

    /**
     * Returns a set of the ActionIds for this ItemType the ActionId is prefix with the action type
     * (item/aggregation)
     *
     * @param itemType
     * @return
     */
    private Set<String> getActionIds(final ItemType itemType) {
        Set<String> ids = new HashSet<String>();
        Map<String, Map<String, Action>> allActions = itemType.getActions();
        for (String type : allActions.keySet()) {
            Map<String, Action> actions = allActions.get(type);
            Set<String> keys = actions.keySet();
            for (String key : keys) {
                ids.add(type + "-" + key);
            }
        }
        return ids;
    }

    @Override
    public String removeItemType(final Provider provider, final String itemTypeId)
            throws NoSuchProviderException, NoSuchItemTypeException {
        validateProvider(provider);

        if (itemTypeId == null || itemTypeId.isEmpty()) {
            throw new IllegalArgumentException("ItemType ID not specified (Provider: " + provider.toString() + ")");
        }

        List<String> ids = getItemTypesForProvider(provider);

        if (!ids.remove(itemTypeId)) {
            throw new NoSuchItemTypeException(itemTypeId);
        }

        // Now check to see if there are any other Providers that have
        // registered this ItemType. If not, then we must remove the
        // ItemType as well.

        boolean sharedRegistration = false;

        for (Provider p : providerItemTypes.keySet()) {
            List<String> providerIds = providerItemTypes.get(p);

            if (providerIds.contains(itemTypeId)) {
                sharedRegistration = true;
                break;
            }
        }

        if (!sharedRegistration) {
            itemTypes.remove(itemTypeId);
        }

        // If there are no more ItemTypes registered against a Provider, then
        // remove the Provider record too.
        if (ids.isEmpty()) {
            providerItemTypes.remove(provider);
        }

        return itemTypeId;
    }

    /**
     * Returns all the ItemType ids for a given provider
     *
     * @param provider
     * @return
     * @throws NoSuchProviderException
     */
    private List<String> getItemTypesForProvider(final Provider provider) throws NoSuchProviderException {
        List<String> ids = providerItemTypes.get(provider);

        if (ids == null) {
            throw new NoSuchProviderException(provider);
        }

        return ids;
    }

    @Override
    public boolean removeAllItemTypes(final Provider provider) throws NoSuchProviderException, NoSuchItemTypeException {
        List<String> itemTypeIds = new ArrayList<String>(getItemTypesForProvider(provider));
        List<String> removed = new ArrayList<>(itemTypeIds.size());
        for (String itemTypeId : itemTypeIds) {
            removed.add(removeItemType(provider, itemTypeId));
        }
        return removed.size() == itemTypeIds.size();
    }

    @Override
    public Collection<ItemType> getItemTypes(final Provider provider) throws NoSuchProviderException {
        validateProvider(provider);

        List<String> itemTypeIds = getItemTypesForProvider(provider);
        Collection<ItemType> result = new ArrayList<>(itemTypeIds.size());

        for (String itemTypeId : itemTypeIds) {
            result.add(itemTypes.get(itemTypeId));
        }

        return result;
    }

    // Only intended to be used by test code.
    protected void removeAllItemTypes() {
        providerItemTypes.clear();
        itemTypes.clear();
    }

    @Override
    public Collection<ItemType> getItemTypes(final String providerType) {

        Set<ItemType> itemTypeIds = new HashSet<>();
        for (Provider p : providerItemTypes.keySet()) {
            if (p.getProviderType().equals(providerType)) {
                try {
                    itemTypeIds.addAll(getItemTypes(p));
                } catch (NoSuchProviderException e) {
                    e.printStackTrace();
                }
            }
        }

        return itemTypeIds;
    }

    @Override
    public Collection<ItemType> getItemTypes() {
        Set<String> providerTypes = new HashSet<>();
        for (Provider p : providerItemTypes.keySet()) {
            providerTypes.add(p.getProviderType());
        }
        Set<ItemType> itemTypeIds = new HashSet<>();
        for (String providerType : providerTypes) {
            itemTypeIds.addAll(getItemTypes(providerType));
        }
        return itemTypeIds;
    }

    @Override
    public Map<Provider, Collection<ItemType>> getProvidersToItemTypes() {
        Map<Provider, Collection<ItemType>> providersToLocalIds = new HashMap<>();
        for (Provider p : providerItemTypes.keySet()) {
            Collection<ItemType> itemTypes = getItemTypes(p.getProviderType());
            providersToLocalIds.put(p, itemTypes);
        }
        return providersToLocalIds;
    }

    @Override
    public Collection<ItemType> getItemTypeByClass(Class<?> clazz) {
        return null;
    }
}
