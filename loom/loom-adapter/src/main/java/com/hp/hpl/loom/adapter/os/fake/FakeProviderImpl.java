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
package com.hp.hpl.loom.adapter.os.fake;

import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.ProviderImpl;

/**
 * A fake provider implementation of the {@link ProviderImpl} - it checks that the details match the
 * credsCheck string.
 *
 */
public class FakeProviderImpl extends ProviderImpl {

    protected String credsCheck;

    /**
     * Constructs a instance of the {@link ProviderImpl} which just validates the details against
     * the provided credsCheck.
     *
     * @param providerType the provider type this provider is for
     * @param providerId the provider id this provider is for
     * @param authEndpoint the auth end point
     * @param providerName the provider name
     * @param credsCheck the string to check the details against (username and passsword need to
     *        match this)
     * @param adapterPackage The provider package
     */
    public FakeProviderImpl(final String providerType, final String providerId, final String authEndpoint,
            final String providerName, final String credsCheck, final String adapterPackage) {
        super(providerType, providerId, authEndpoint, providerName, adapterPackage);
        this.credsCheck = credsCheck;
    }

    @Override
    public boolean authenticate(final Credentials creds) {
        return creds != null && creds.getPassword().equals(credsCheck) && creds.getUsername().equals(credsCheck);
    }

}
