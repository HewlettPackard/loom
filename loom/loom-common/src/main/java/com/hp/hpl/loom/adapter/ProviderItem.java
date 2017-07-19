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

import com.hp.hpl.loom.adapter.annotations.ItemTypeInfo;
import com.hp.hpl.loom.adapter.annotations.LoomAttribute;
import com.hp.hpl.loom.adapter.annotations.Root;
import com.hp.hpl.loom.adapter.annotations.Sort;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.model.Item;

/**
 * Models the provider type.
 */
@Root
@ItemTypeInfo(value = ProviderType.TYPE_LOCAL_ID,
        sorting = {@Sort(operation = DefaultOperations.SORT_BY, fieldOrder = {"Type"}),
                @Sort(operation = DefaultOperations.GROUP_BY, fieldOrder = {"Type"})})
public class ProviderItem extends Item {
    private String providerName;
    @LoomAttribute(key = "Type", supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
    private String providerType;
    private String providerId;

    /**
     * @param logicalId the logical id for this provider item
     * @param providerType the providerType
     */
    public ProviderItem(final String logicalId, final ProviderType providerType) {
        super(logicalId, providerType);
    }

    @Override
    public String getQualifiedName() {
        return providerName;
    }

    /**
     * @return the providerType
     */
    public String getProviderType() {
        return providerType;
    }

    /**
     * @param providerType the providerType to set
     */
    public void setProviderType(final String providerType) {
        this.providerType = providerType;
    }

    /**
     * @return the providerId
     */
    public String getProviderId() {
        return providerId;
    }

    /**
     * @param providerId the providerId to set
     */
    public void setProviderId(final String providerId) {
        this.providerId = providerId;
    }

    /**
     * @return the providerName
     */
    public String getProviderName() {
        return providerName;
    }

    /**
     * @param providerName the providerName to set
     */
    public void setProviderName(final String providerName) {
        this.providerName = providerName;
    }
}
