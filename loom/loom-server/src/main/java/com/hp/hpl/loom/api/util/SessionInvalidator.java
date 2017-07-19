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
package com.hp.hpl.loom.api.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.manager.adapter.AdapterManager;
import com.hp.hpl.loom.manager.aggregation.AggregationManager;
import com.hp.hpl.loom.manager.stitcher.Tacker;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.Session;

@Component
public class SessionInvalidator implements Runnable {
    private static final Log LOG = LogFactory.getLog(SessionInvalidator.class);

    @Autowired
    private SessionManager sessionManager;
    @Autowired
    private AdapterManager adapterManager;
    @Autowired
    private AggregationManager aggregationManager;
    @Autowired
    private Tacker stitcher;

    public SessionInvalidator() {}

    @Override
    public void run() {
        Map<String, Session> sessions = sessionManager.getSessions();
        Iterator<Entry<String, Session>> it = sessions.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Session> pairs = it.next();
            Session session = pairs.getValue();
            if (session.isExpired()) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Session" + session.getId() + " is expired");
                }
                Collection<Provider> providers = adapterManager.getProviders();
                for (Provider provider : providers) {
                    try {
                        adapterManager.userDisconnected(session, provider, null);
                    } catch (NoSuchProviderException e) {
                        LOG.warn(
                                "No provider registered with the adapterManager though exists in its variable 'providers'");
                    } catch (NoSuchSessionException e) {
                        LOG.warn("No session " + session.getId());
                    }
                }

                try {
                    stitcher.deleteSession(session);
                    aggregationManager.deleteSession(session);
                } catch (NoSuchSessionException e) {
                    LOG.warn("No session found by aggregationManager though found by sessionManager");
                }
                sessionManager.releaseSession(session);
            }
        }
    }
}
