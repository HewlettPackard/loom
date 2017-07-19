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
 * Thrown when the session already exists.
 */
public class SessionAlreadyExistsException extends CheckedLoomException {
    private Session session;

    /**
     * @param session the corresponding session
     */
    public SessionAlreadyExistsException(final Session session) {
        super("Session " + session + " already exists");
        this.session = session;
    }

    /**
     * @param session the corresponding session
     * @param cause the cause
     */
    public SessionAlreadyExistsException(final Session session, final Throwable cause) {
        super("Session " + session + " already exists", cause);
        this.session = session;
    }

    /**
     * @param session the corresponding session
     * @param msg the message
     */
    public SessionAlreadyExistsException(final Session session, final String msg) {
        super("Session " + session + " already exists. " + msg);
        this.session = session;
    }

    /**
     * @param session the corresponding session
     * @param msg the message
     * @param cause the cause
     */
    public SessionAlreadyExistsException(final Session session, final String msg, final Throwable cause) {
        super("Session " + session + " already exists. " + msg, cause);
        this.session = session;
    }

    /**
     * Get the session.
     *
     * @return the session.
     */
    public Session getSession() {
        return session;
    }
}
