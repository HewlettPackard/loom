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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.hp.hpl.loom.api.ApiConfig;
import com.hp.hpl.loom.model.AdminSessionImpl;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.model.SessionImpl;

@Component
public class SessionManagerImpl implements SessionManager {

    @Value("${session.invalidation.interval}")
    private int interval;

    private Map<String, Session> sessions = new ConcurrentHashMap<String, Session>();

    public SessionManagerImpl() {}

    private void updateResponse(final Session session, final HttpServletResponse response) {
        Cookie c = new Cookie(SESSION_COOKIE, null);

        if (session == null) {
            c.setMaxAge(0);
        } else {
            c.setValue(session.getId());
        }

        c.setPath(ApiConfig.API_BASE);
        response.addCookie(c);
    }

    @Override
    public Session getSession(final String sessionId, final HttpServletResponse response) {
        if (sessionId == null || sessionId.isEmpty()) {
            return null;
        }
        Session session = null;
        synchronized (sessions) {
            session = sessions.get(sessionId);
        }
        if (session != null) {
            updateResponse(session, response);
            session.setLastAccessedTime(System.currentTimeMillis());
        }
        return session;
    }

    @Override
    public Session createSession(String sessionId, final HttpServletResponse response) {
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }
        Session session = null;
        synchronized (sessions) {
            session = sessions.get(sessionId);
            if (session == null) {
                session = new SessionImpl(sessionId, interval);
                sessions.put(sessionId, session);
            }
        }
        updateResponse(session, response);
        return session;
    }

    @Override
    public void releaseSession(final Session session, final HttpServletResponse response) {
        synchronized (sessions) {
            sessions.remove(session.getId());
            updateResponse(null, response);
        }
    }

    @Override
    public void releaseSession(final Session session) {
        synchronized (sessions) {
            sessions.remove(session.getId());
        }
    }

    @Override
    public Map<String, Session> getSessions() {
        return sessions;
    }

    @Override
    public int getInterval() {
        return interval;
    }

    @Override
    public boolean sessionExists(final String sessionId, final HttpServletResponse response) {
        if (sessionId == null) {
            return false;
        } else {
            if (getSession(sessionId, response) == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Session getSessionWithSessionId(final String sessionId, final HttpServletResponse response) {
        if (sessionId == null) {
            return createSession(sessionId, response);
        } else {
            if (sessionExists(sessionId, response)) {
                return getSession(sessionId, response);
            } else {
                return createSession(null, response);
            }
        }
    }

    @Override
    public Session createSuperSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }
        Session session = null;
        synchronized (sessions) {
            session = sessions.get(sessionId);
            if (session == null) {
                session = new AdminSessionImpl(sessionId, interval);
                sessions.put(sessionId, session);
            }
        }
        return session;
    }

}
