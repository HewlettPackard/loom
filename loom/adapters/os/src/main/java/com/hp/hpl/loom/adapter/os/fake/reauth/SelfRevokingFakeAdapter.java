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

import com.hp.hpl.loom.adapter.ItemCollector;
import com.hp.hpl.loom.adapter.os.fake.FakeAdapter;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.Session;

public class SelfRevokingFakeAdapter extends FakeAdapter {

    @Override
    protected Provider createProvider(final String providerType, final String providerId, final String authEndpoint,
            final String providerName) {
        return new SelfRevokingFakeProviderImpl(providerType, providerId, authEndpoint, providerName, "test",
                this.getClass().getPackage().getName());
    }

    @Override
    protected ItemCollector getNewItemCollectorInstance(final Session session, final Credentials creds) {
        return new RevocationCheckerFakeItemCollector(session, this, adapterManager, creds, provider.getAuthEndpoint(),
                getConfig());
    }
}
