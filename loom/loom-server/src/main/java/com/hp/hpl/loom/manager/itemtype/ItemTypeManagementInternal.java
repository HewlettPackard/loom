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

import java.util.Collection;
import java.util.Map;

import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;

/**
 * All supported ItemType operations. Multiple ItemTypes may be associated with a single Provider.
 * The same ItemType may be registered by multiple Providers of the same type.
 */
public interface ItemTypeManagementInternal {



    /**
     * Retrieve all ItemTypes registered with the given Provider.
     *
     * @param provider the Provider to inspect.
     * @return Zero or more ItemTypes registered against the Provider.
     * @throws NoSuchProviderException The Provider does not exist.
     */
    Collection<ItemType> getItemTypes(Provider provider) throws NoSuchProviderException;

    /**
     * Retrieve all ItemTypes registered with the given Provider.
     *
     * @param providerType the provider type to inspect.
     * @return Zero or more ItemTypes registered against the Provider.
     */
    Collection<ItemType> getItemTypes(String providerType);

    /**
     * Retrieve all ItemTypes.
     *
     * @return Zero or more ItemTypes registered against the Provider.
     */
    Collection<ItemType> getItemTypes();

    /**
     * Retrieve the ItemType for the given unique identifier. Note that a single ItemType may be
     * registered with more than one Provider.
     *
     * @param itemTypeId the unique identifier of the ItemType to retrieve.
     * @return The ItemType or null if the identifier did not match any of the registered ItemTypes.
     */
    ItemType getItemType(String itemTypeId);


    Map<Provider, Collection<ItemType>> getProvidersToItemTypes();

    /**
     * Retrieve all ItemTypes registered using a given class.
     *
     * @param clazz class to lookup based on
     * @return Collection of ItemTypes based on this class
     */
    Collection<ItemType> getItemTypeByClass(Class<?> clazz);
}
