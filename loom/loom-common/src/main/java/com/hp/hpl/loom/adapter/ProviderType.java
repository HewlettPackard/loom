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

import com.hp.hpl.loom.exceptions.AttributeException;
import com.hp.hpl.loom.model.SeparableItemType;

/**
 * Type that models the provider information.
 *
 */
public class ProviderType extends SeparableItemType {
    /**
     * The provider type_local_id.
     */
    public static final String TYPE_LOCAL_ID = "provider";

    /**
     * No-arg constructor for a providerType.
     */
    @SuppressWarnings({"checkstyle:emptyblock", "PMD.EmptyCatchBlock"})
    public ProviderType() {
        super(TYPE_LOCAL_ID);

        try {
            Attribute providerName =
                    new Attribute.Builder("providerName").name("providerName").visible(true).plottable(false).build();

            Attribute providerType =
                    new Attribute.Builder("providerType").name("providerType").visible(true).plottable(false).build();
            Attribute providerId =
                    new Attribute.Builder("providerId").name("providerId").visible(true).plottable(false).build();
            this.addAttributes(providerName, providerType, providerId);
        } catch (AttributeException e) {
        }
    }


}
