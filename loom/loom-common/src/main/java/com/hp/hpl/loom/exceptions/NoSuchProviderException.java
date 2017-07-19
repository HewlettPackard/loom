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
package com.hp.hpl.loom.exceptions;

import com.hp.hpl.loom.model.Provider;

/**
 * Thrown when the provider isn't found.
 */
public class NoSuchProviderException extends CheckedLoomException {
    private Provider provider;

    /**
     * @param msg the message
     */
    public NoSuchProviderException(final String msg) {
        super(msg);
    }

    /**
     * @param providerType the providerType
     * @param providerId the providerId
     */
    public NoSuchProviderException(final String providerType, final String providerId) {
        super("Provider " + providerId + " of type " + providerType + " does not exist");
    }

    /**
     * @param provider the provider
     */
    public NoSuchProviderException(final Provider provider) {
        super("Provider " + provider.getProviderId() + " of type " + provider.getProviderType() + " does not exist");
        this.provider = provider;
    }

    /**
     * @param provider the provider
     * @param cause the cause
     */
    public NoSuchProviderException(final Provider provider, final Throwable cause) {
        super("Provider " + provider.getProviderId() + " of type " + provider.getProviderType() + " does not exist",
                cause);
        this.provider = provider;
    }

    /**
     * Get the provider.
     *
     * @return the provider
     */
    public Provider getProvider() {
        return provider;
    }
}
