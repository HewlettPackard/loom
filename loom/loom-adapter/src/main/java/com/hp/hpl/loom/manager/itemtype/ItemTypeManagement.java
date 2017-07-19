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

import com.hp.hpl.loom.exceptions.DuplicateItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NullItemTypeIdException;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;

/**
 * Interface that covers the {@link ItemType} management.
 */
public interface ItemTypeManagement {
    /**
     * Register a new ItemType for the given Provider. The ItemType's unique identifier must be set
     * before calling this method. Attempts to re-register the same ItemType by the same Provider or
     * register an ItemType previously registered by a different Provider will result in a
     * DuplicateItemTypeException.
     *
     * @param provider the Provider registering the ItemType.
     * @param itemType the ItemType to register.
     * @return The unique identifier of the ItemType that has been registered.
     * @throws DuplicateItemTypeException the ItemType already exists for this Provider/has been
     *         registered by a different Provider.
     * @throws NullItemTypeIdException if the item type id is null
     * @throws NoSuchProviderException if the provider isn't found
     */
    String addItemType(Provider provider, ItemType itemType)
            throws DuplicateItemTypeException, NoSuchProviderException, NullItemTypeIdException;

    /**
     * Deregister the previously registered ItemType from the given Provider. If the ItemType was
     * also registered by another Provider of the same type, the ItemType is only disassociated with
     * the given Provider. Only when all Providers of the same type that registered the same
     * ItemType remove all references to it, will the ItemType actually be removed.
     *
     * @param provider the Provider whose ItemType will be deregistered.
     * @param itemTypeId the ItemType to deregister.
     * @return The unique identifier of the ItemType that has been deregistered.
     * @throws NoSuchProviderException The Provider does not exist.
     * @throws NoSuchItemTypeException The ItemType was not registered to the Provider.
     */
    String removeItemType(Provider provider, String itemTypeId) throws NoSuchProviderException, NoSuchItemTypeException;

    /**
     * Remove all the itemTypes for the provided provider.
     *
     * @param provider the Provider whose ItemTypes will be deregistered.
     * @return True if all item types could be removed. False if some of them could not.
     * @throws NoSuchProviderException The Provider does not exist.
     * @throws NoSuchItemTypeException The ItemType was not registered to the Provider.
     */
    boolean removeAllItemTypes(Provider provider) throws NoSuchProviderException, NoSuchItemTypeException;
}
