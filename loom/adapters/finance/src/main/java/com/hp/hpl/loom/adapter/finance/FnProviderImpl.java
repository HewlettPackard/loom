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
package com.hp.hpl.loom.adapter.finance;

import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.ProviderImpl;

public class FnProviderImpl extends ProviderImpl {

    public FnProviderImpl(final String providerType, final String providerId, final String authEndpoint,
            final String providerName, final String adapterPackage) {
        super(providerType, providerId, authEndpoint, providerName, adapterPackage);
    }

    @Override
    public boolean authenticate(final Credentials creds) {
        return creds != null && creds.getPassword().equals("test") && creds.getUsername().equals("test");
    }
}
