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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * The provider interface that provides authenication services to the adapters.
 *
 */
@JsonDeserialize(as = ProviderImpl.class)
public interface Provider {
    /**
     * The provider separator.
     */
    String PROV_SEPARATOR = "/";

    /**
     * Get the provider type.
     *
     * @return the provider Type
     */
    String getProviderType();

    /**
     * Set the providerType.
     *
     * @param providerType the providerType
     */
    void setProviderType(String providerType);

    /**
     * Get the provider id.
     *
     * @return the provider id
     */
    String getProviderId();

    /**
     * Set the provider id.
     *
     * @param providerId the providerId
     */
    void setProviderId(String providerId);

    /**
     * Set the provider type and id.
     *
     * @return the provider type and id separated by the PROV_SEPARATOR
     */
    String getProviderTypeAndId();

    /**
     * Get the providerType and id.
     *
     * @param providerType the providerType
     * @param providerId the providerId
     * @return the provider type and id separated by the PROV_SEPARATOR
     */
    @JsonIgnore
    static String getProviderTypeAndId(final String providerType, final String providerId) {
        return providerType + PROV_SEPARATOR + providerId;
    }

    /**
     * Get the authEndpoint.
     *
     * @return get the auth endpoint
     */
    String getAuthEndpoint();

    /**
     * Set the auth endpoint.
     *
     * @param authEndpoint the authEndpoint
     */
    void setAuthEndpoint(String authEndpoint);

    /**
     * Get the provider name.
     *
     * @return the provider name
     */
    @JsonProperty("name")
    String getProviderName();

    /**
     * Set the provider name.
     *
     * @param providerName the providerName
     */
    void setProviderName(String providerName);

    /**
     * Authenticate with the provider using the credentials.
     *
     * @param creds credentials for the authenticate
     * @return true if authenticated
     */
    boolean authenticate(Credentials creds);

    /**
     * Returns the adapter classes package.
     *
     * @return Package of the adapter
     */
    String getAdapterPackage();
}
