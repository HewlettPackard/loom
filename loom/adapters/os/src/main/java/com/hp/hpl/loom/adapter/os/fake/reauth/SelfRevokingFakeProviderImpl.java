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
package com.hp.hpl.loom.adapter.os.fake.reauth;

import com.hp.hpl.loom.adapter.os.fake.FakeProviderImpl;
import com.hp.hpl.loom.model.Credentials;

public class SelfRevokingFakeProviderImpl extends FakeProviderImpl {

    private boolean revoked = false;

    public SelfRevokingFakeProviderImpl(final String providerType, final String providerId, final String authEndpoint,
            final String providerName, final String credsCheck, final String adapterPackage) {
        super(providerType, providerId, authEndpoint, providerName, credsCheck, adapterPackage);
    }

    @Override
    public boolean authenticate(final Credentials creds) {
        revoked = !revoked;
        return creds != null && creds.getPassword().equals(credsCheck) && creds.getUsername().equals(credsCheck);
    }

    public boolean getRevoked() {
        return revoked;
    }
}
