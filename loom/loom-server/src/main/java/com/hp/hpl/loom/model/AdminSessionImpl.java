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

import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.UserAlreadyConnectedException;
import com.hp.hpl.loom.manager.adapter.AdapterManager;

public class AdminSessionImpl extends SessionImpl {
    private Credentials creds = null;
    private AdapterManager adapterManager = null;

    public AdminSessionImpl(final String id, final int maxInactivityPeriod) {
        super(id, maxInactivityPeriod);
    }


    @Override
    public void setReAuthenticate(final Provider provider, final Boolean requireReAuth) {
        super.setReAuthenticate(provider, requireReAuth);
        provider.authenticate(creds);
        try {
            adapterManager.userConnected(this, provider, creds);
        } catch (NoSuchProviderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchSessionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UserAlreadyConnectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setManager(final Credentials creds, final AdapterManager adapterManager) {
        this.creds = creds;
        this.adapterManager = adapterManager;
    }
}
