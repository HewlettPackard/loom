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
package com.hp.hpl.loom.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * Returns a list of providers.
 */
@JsonAutoDetect
public class ProviderList {
    private ArrayList<Provider> providers;

    /**
     * No-arg constructor for JSON serialisation.
     */
    public ProviderList() {
        providers = new ArrayList<Provider>();
    }

    /**
     * Get the list of providers.
     *
     * @return the provider list
     */
    public ArrayList<Provider> getProviders() {
        return providers;
    }

    /**
     * Set the list of providers.
     *
     * @param providers the list of providers.
     */
    public void setProviders(final ArrayList<Provider> providers) {
        this.providers = providers;
    }
}
