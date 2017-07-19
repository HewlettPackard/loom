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

import java.util.Objects;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The model of the provider information.
 */
@JsonAutoDetect
public class ProviderImpl implements Provider {

    protected String providerType = null;
    protected String providerId = null;
    @JsonIgnore
    private String providerTypeId = null;
    @JsonIgnore
    protected String authEndpoint = null;
    @JsonProperty("name")
    protected String providerName = null;

    protected String adapterPackage = null;

    /**
     * No-arg constructor for JSON serialisation.
     */
    ProviderImpl() {}

    /**
     * @param providerType the providerType
     * @param providerId the providerId
     * @param authEndpoint the authEndpoint
     * @param providerName the providerName
     * @param adapterPackage the adapterPackage
     */
    public ProviderImpl(final String providerType, final String providerId, final String authEndpoint,
            final String providerName, final String adapterPackage) {
        setProviderType(providerType);
        setProviderId(providerId);
        setAuthEndpoint(authEndpoint);
        setProviderName(providerName);
        this.adapterPackage = adapterPackage;
    }

    @Override
    public boolean authenticate(final Credentials creds) {
        return true;
    }

    @Override
    public void setProviderType(final String providerType) {
        if (providerType == null || providerType.length() == 0) {
            throw new IllegalArgumentException("Provider Type is undefined");
        }

        this.providerType = providerType;
    }

    @Override
    public void setProviderId(final String providerId) {
        if (providerId == null || providerId.length() == 0) {
            throw new IllegalArgumentException("Provider ID is undefined");
        }

        this.providerId = providerId;
    }

    @Override
    public void setAuthEndpoint(final String authEndpoint) {
        if (authEndpoint == null || authEndpoint.length() == 0) {
            throw new IllegalArgumentException("Auth Endpoint is undefined");
        }

        this.authEndpoint = authEndpoint;
    }

    @Override
    public void setProviderName(final String providerName) {
        if (providerName == null || providerName.length() == 0) {
            throw new IllegalArgumentException("Provider Name is undefined");
        }

        this.providerName = providerName;
    }

    @Override
    public String getProviderType() {
        return providerType;
    }

    @Override
    public String getProviderId() {
        return providerId;
    }

    @Override
    @JsonIgnore
    public final String getProviderTypeAndId() {
        if (providerTypeId == null) {
            providerTypeId = Provider.getProviderTypeAndId(getProviderType(), getProviderId());
        }
        return providerTypeId;
    }


    @Override
    public String getAuthEndpoint() {
        return authEndpoint;
    }

    @Override
    public String getProviderName() {
        return providerName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("providerType", providerType).append("providerId", providerId)
                .append("authEndpoint", authEndpoint).append("providerName", providerName).toString();
    }

    @Override
    public boolean equals(final Object object) {
        if (object == null) {
            return false;
        }
        if (object == this) {
            return true;
        }
        if (object.getClass() != getClass()) {
            return false;
        }

        final ProviderImpl other = (ProviderImpl) object;

        return Objects.equals(providerType, other.providerType) && Objects.equals(providerId, other.providerId)
                && Objects.equals(authEndpoint, other.authEndpoint) && Objects.equals(providerName, other.providerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(providerType, providerId, authEndpoint, providerName);
    }

    /**
     * @return the adapterPackage
     */
    public String getAdapterPackage() {
        return adapterPackage;
    }
}
