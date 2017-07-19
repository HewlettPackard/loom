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
package com.hp.hpl.loom.api.client;

import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Provider;

public class TestingProvider implements Provider {

    @Override
    public String getProviderType() {
        return "TEST";
    }

    @Override
    public void setProviderType(final String providerType) {}

    @Override
    public String getProviderId() {
        return "TEST_ID";
    }

    @Override
    public void setProviderId(final String providerId) {}

    @Override
    public String getProviderTypeAndId() {
        return "TEST-TEST_ID";
    }

    @Override
    public String getAuthEndpoint() {
        return "http://test";
    }

    @Override
    public void setAuthEndpoint(final String authEndpoint) {}

    @Override
    public String getProviderName() {
        return "TESTING";
    }

    @Override
    public void setProviderName(final String providerName) {}

    @Override
    public boolean authenticate(final Credentials creds) {
        return true;
    }

    @Override
    public String getAdapterPackage() {
        return "com.hp";
    }

}
