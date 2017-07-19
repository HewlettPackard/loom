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
package com.hp.hpl.loom.adapter;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.Session;

/**
 * BaseItemCollector implements the {@link ItemCollector} interface.
 *
 * The class is abstract and requires collectItems to be implemented.
 */
public abstract class BaseItemCollector implements ItemCollector {
    private static final Log LOG = LogFactory.getLog(BaseItemCollector.class);

    private String status;
    protected Session session;
    protected Provider provider;

    protected HashMap<String, HashMap<String, String>> relationshipsDiscoveredOnCurrentUpdateCycle;

    /**
     * Private default constructor - it sets the status to
     * {@link com.hp.hpl.loom.adapter.ItemCollector#OPEN}.
     */
    private BaseItemCollector() {
        setStatus(ItemCollector.OPEN);
    }

    /**
     * Constructor accepting a session - it sets the status to
     * {@link com.hp.hpl.loom.adapter.ItemCollector#OPEN}.
     *
     * @param session session linked to this ItemCollector
     */
    public BaseItemCollector(final Session session) {
        this();
        this.session = session;
    }

    protected void setProvider(final Provider provider) {
        this.provider = provider;
    }

    // IC state machine
    private synchronized boolean setStatus(final String state) {
        boolean stateSet = false;
        // if ItemCollector is closed, subsequent status changes are irrelevant - don't error
        if (CLOSED.equals(status)) {
            return true;
        }
        // otherwise check if state is allowed
        if (IDLE.equals(state)) {
            if (OPEN.equals(status) || BUSY.equals(status) || PAUSED.equals(status)) {
                status = state;
                stateSet = true;
            }
        } else if (SCHEDULED.equals(state)) {
            if (IDLE.equals(status)) {
                status = state;
                stateSet = true;
            }
        } else if (BUSY.equals(state)) {
            if (SCHEDULED.equals(status)) {
                status = state;
                stateSet = true;
            }
        } else if (PAUSED.equals(state)) {
            if (BUSY.equals(status)) {
                status = state;
                stateSet = true;
            }
        } else if (CLOSED.equals(state) || OPEN.equals(state)) {
            status = state;
            stateSet = true;
        } else {
            LOG.warn("unknown ItemCollector state: " + state);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("setting IC_STATUS to " + state + " returns: " + stateSet + "(" + status + ")");
        }
        return stateSet;
    }

    private String getStatus() {
        return status;
    }

    @Override
    public boolean setScheduled() {
        return setStatus(SCHEDULED);
    }

    @Override
    public boolean setIdle() {
        boolean prevPausedState = PAUSED.equals(getStatus());
        boolean idle = setStatus(IDLE);
        if (idle && prevPausedState) {
            session.setReAuthenticate(provider, false);
        }
        return idle;
    }

    @Override
    public void setCredentials(final Credentials credentials) {}

    private boolean setBusy() {
        return setStatus(BUSY);
    }

    private boolean setPaused() {
        return setStatus(PAUSED);
    }

    @Override
    public void close() {
        setStatus(CLOSED);
    }

    @Override
    public void run() {
        if (CLOSED.equals(getStatus())) {
            return;
        }
        if (!setBusy()) {
            LOG.error("Collection task cannot run - wrong status: " + getStatus() + ", expected " + SCHEDULED);
            setIdle();
            return;
        }
        try {
            // do item collection
            collectItems();
        } catch (AuthenticationFailureException afe) {
            LOG.error("caught AuthenticationFailure for session: " + session.getId(), afe);
            if (setPaused()) {
                LOG.warn("ItemCollector paused while reauthentication is in progress for session: " + session.getId());
                // notify API that re-authentication is needed
                LOG.warn("Setting session as needing re-authentication");
                session.setReAuthenticate(provider, true);
                return;
            }

        } catch (Exception e) {
            LOG.error("unexpected behaviour while collecting data - abort this update for session: " + session.getId(),
                    e);
        }
        // wrap up this round
        if (!setIdle()) {
            LOG.error("cannot reset ItemCollector status to IDLE at end of CollectionTask: " + getStatus());
        }
    }

    protected abstract void collectItems();

    @Override
    public HashMap<String, HashMap<String, String>> getRelationshipsDiscoveredOnCurrentUpdateCycle() {
        return relationshipsDiscoveredOnCurrentUpdateCycle;
    }

}
