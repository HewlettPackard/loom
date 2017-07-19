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

/**
 * Thrown when no tapestry definition is found.
 */
public class NoSuchTapestryDefinitionException extends CheckedLoomException {
    private String sessionId;
    private String tapestryId;

    /**
     * @param sessionId the sessionid
     */
    public NoSuchTapestryDefinitionException(final String sessionId) {
        super("Tapestry for session " + sessionId + " does not exist");

        this.sessionId = sessionId;
        tapestryId = null;
    }

    /**
     * @param sessionId the sessionid
     * @param description the description
     */
    public NoSuchTapestryDefinitionException(final String sessionId, final String description) {
        super("Tapestry for session " + sessionId + " does not exist", description);

        this.sessionId = sessionId;
        tapestryId = null;
    }

    /**
     * @param sessionId the sessionid
     * @param tapestryId the tapestryid
     * @param description the description
     */
    public NoSuchTapestryDefinitionException(final String sessionId, final String tapestryId,
            final String description) {
        super("Tapestry# " + tapestryId + " for session " + sessionId + " does not exist", description);

        this.sessionId = sessionId;
        this.tapestryId = tapestryId;
    }

    /**
     * @param sessionId the sessionid
     * @param tapestryId the tapestryid
     * @param description the description
     * @param cause the cause
     */
    public NoSuchTapestryDefinitionException(final String sessionId, final String tapestryId, final String description,
            final Throwable cause) {
        super("Tapestry# " + tapestryId + " for session " + sessionId + " does not exist", description, cause);

        this.sessionId = sessionId;
        this.tapestryId = tapestryId;
    }

    /**
     * Returns the sessionId.
     *
     * @return the sessionid
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Returns the tapestryid.
     *
     * @return the tapestryid
     */
    public String getTapestryId() {
        return tapestryId;
    }
}
