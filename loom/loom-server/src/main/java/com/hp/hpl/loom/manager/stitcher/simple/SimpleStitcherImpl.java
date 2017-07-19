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
package com.hp.hpl.loom.manager.stitcher.simple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.SessionAlreadyExistsException;
import com.hp.hpl.loom.manager.stitcher.ItemEquivalence;
import com.hp.hpl.loom.manager.stitcher.StitcherRuleManager;
import com.hp.hpl.loom.manager.stitcher.StitcherUpdater;
import com.hp.hpl.loom.manager.stitcher.Tacker;
import com.hp.hpl.loom.model.Session;

/**
 * Implementation of the Stitcher.
 */
@Component
public class SimpleStitcherImpl implements Tacker {

    private static final Log LOG = LogFactory.getLog(SimpleStitcherImpl.class);

    private SimpleStitcherRuleManagerImpl ruleManager = new SimpleStitcherRuleManagerImpl();

    /** Map of Session to TackerContext */
    private Map<String, SimpleStitcherSession> stitcherSessions = new HashMap<>();

    private SimpleStitcherSession getStitcherSession(final Session session, final boolean createOnNotExist)
            throws NoSuchSessionException {
        if (session == null) {
            throw new IllegalArgumentException("session must not be null");
        }

        String sessionId = session.getId();
        SimpleStitcherSession model = stitcherSessions.get(sessionId);
        if (model == null) {
            if (createOnNotExist) {
                model = new SimpleStitcherSession(ruleManager);
                stitcherSessions.put(sessionId, model);
            } else {
                throw new NoSuchSessionException(session);
            }
        }
        return model;
    }

    private SimpleStitcherSession getStitcherSession(final Session session) throws NoSuchSessionException {
        return getStitcherSession(session, false);
    }

    private void deleteSession(final String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new IllegalArgumentException("session must not be null");
        }
        SimpleStitcherSession stitcherSession = stitcherSessions.get(sessionId);
        stitcherSessions.remove(sessionId);
    }

    @Override
    public StitcherRuleManager getStitcherRuleManager() {
        return ruleManager;
    }

    @Override
    public void createSession(final Session session) throws NoSuchSessionException, SessionAlreadyExistsException {
        if (session == null) {
            throw new IllegalArgumentException("session must not be null");
        }
        // if (stitcherSessions.containsKey(session.getId())) {
        // throw new SessionAlreadyExistsException(session);
        // }
        LOG.info("createSession " + session);
        synchronized (session) {
            getStitcherSession(session, true);
        }
    }

    @Override
    public void deleteSession(final Session session) throws NoSuchSessionException {
        if (session == null) {
            throw new NoSuchSessionException(session);
        }
        LOG.info("Delete session " + session);
        String sessionId = session.getId();
        if (!stitcherSessions.containsKey(sessionId)) {
            throw new NoSuchSessionException(session);
        }
        synchronized (session) {
            deleteSession(sessionId);
        }
    }

    @Override
    public void deleteAllSessions() {
        LOG.info("deleteAllSessions ");
        List<String> sessionIds = new ArrayList<String>(stitcherSessions.keySet());
        for (String sessionId : sessionIds) {
            deleteSession(sessionId);
        }
    }

    @Override
    public ItemEquivalence getItemEquivalence(final Session session) throws NoSuchSessionException {
        LOG.info("getItemEquivalence for session " + session.getId());
        return getStitcherSession(session);
    }

    @Override
    public StitcherUpdater getStitcherUpdater(final Session session) throws NoSuchSessionException {
        LOG.info("getStitcherUpdater for session " + session.getId());
        return getStitcherSession(session);
    }
}
