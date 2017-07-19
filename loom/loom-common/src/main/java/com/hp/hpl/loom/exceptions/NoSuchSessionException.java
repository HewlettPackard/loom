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
package com.hp.hpl.loom.exceptions;

import com.hp.hpl.loom.model.Session;

/**
 * Thrown when there isn't a corresponding session.
 */
public class NoSuchSessionException extends CheckedLoomException {
    private Session session;

    /**
     * No-arg constructor.
     */
    public NoSuchSessionException() {
        super("Session does not exist");
    }

    /**
     * @param sessionId the session id
     */
    public NoSuchSessionException(final String sessionId) {
        super("Session does not exist: " + sessionId);
    }

    /**
     * @param description the description
     * @param session the session
     */
    public NoSuchSessionException(final String description, final Session session) {
        super(description + ": Session " + session + " does not exist");
        this.session = session;
    }

    /**
     * @param session the session
     */
    public NoSuchSessionException(final Session session) {
        super("Session " + session + " does not exist");
        this.session = session;
    }

    /**
     * @param session the session
     * @param cause the cause
     */
    public NoSuchSessionException(final Session session, final Throwable cause) {
        super("Session " + session + " does not exist", cause);
        this.session = session;
    }

    /**
     * Get the session.
     *
     * @return the session
     */
    public Session getSession() {
        return session;
    }
}
