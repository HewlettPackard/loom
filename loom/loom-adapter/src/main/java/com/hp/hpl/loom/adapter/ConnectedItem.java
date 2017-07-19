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

import com.hp.hpl.loom.model.Provider;


/**
 * Interface for ConnectedItems it covers the relationships, accessing name and logicalId.
 *
 */
public interface ConnectedItem {

    /**
     * @param provider provider
     * @param itemTypeId matching the connected resource
     * @param connectedResourceId the connected resource Id
     */
    void setRelationship(final Provider provider, final String itemTypeId, final String connectedResourceId);

    /**
     * Sets a resource Id based relationship one at a time.
     *
     * @param provider provider
     * @param itemTypeLocalId matching the connected resource
     * @param connectedResourceId the connected resource Id
     * @param type the relation type
     */
    @SuppressWarnings("checkstyle:linelength")
    void setRelationshipWithType(final Provider provider, final String itemTypeLocalId,
            final String connectedResourceId, String type);

    // /**
    // * Sets all resource Id based relationships listed in the Collection.
    // *
    // * @param itemTypeLocalId matching the connected resources
    // * @param types the relation types
    // * @param connectedResourceIds the connected resource Id
    // */
    // void setRelationship(final String itemTypeLocalId, final Collection<String>
    // connectedResourceIds, String types);

    /**
     * Gets the name of this connected item.
     *
     * @return the name
     */
    String getName();

    /**
     * Gets the logical id for this connected item.
     *
     * @return the logical id
     */
    String getLogicalId();
}
