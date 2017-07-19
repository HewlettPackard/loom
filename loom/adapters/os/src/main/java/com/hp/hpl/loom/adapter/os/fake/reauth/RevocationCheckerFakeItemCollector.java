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

import com.hp.hpl.loom.adapter.AuthenticationFailureException;
import com.hp.hpl.loom.adapter.os.BaseOsAdapter;
import com.hp.hpl.loom.adapter.os.fake.FakeConfig;
import com.hp.hpl.loom.adapter.os.fake.FakeItemCollector;
import com.hp.hpl.loom.manager.adapter.AdapterManager;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Session;

public class RevocationCheckerFakeItemCollector extends FakeItemCollector {

    private int ctr = 0;

    public RevocationCheckerFakeItemCollector(final Session session, final BaseOsAdapter adapter,
            final AdapterManager adapterManager, final Credentials creds, final String authEndpoint,
            final FakeConfig fc) {
        super(session, adapter, adapterManager, creds, authEndpoint, fc);
    }

    @Override
    protected void collectItems() {
        if (ctr++ > 2) {
            SelfRevokingFakeAdapter srfa = (SelfRevokingFakeAdapter) adapter;
            if (((SelfRevokingFakeProviderImpl) srfa.getProvider()).getRevoked()) {
                throw new AuthenticationFailureException(new Exception());
            }
        }
        super.collectItems();
    }
}
