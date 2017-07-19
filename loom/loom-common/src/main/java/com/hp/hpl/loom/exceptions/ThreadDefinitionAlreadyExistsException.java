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
 * Thrown when the thread definition already exists.
 */
public class ThreadDefinitionAlreadyExistsException extends CheckedLoomException {
    private String threadId;
    private String tapestryId;

    /**
     * @param tapestryId the tapestry id
     * @param threadId the thread id
     */
    public ThreadDefinitionAlreadyExistsException(final String tapestryId, final String threadId) {
        super("Thread " + threadId + " already exists in tapestry " + tapestryId);
        this.threadId = threadId;
        this.tapestryId = tapestryId;
    }

    /**
     * @param tapestryId the tapestry id
     * @param threadId the thread id
     * @param cause the cause
     */
    public ThreadDefinitionAlreadyExistsException(final String tapestryId, final String threadId,
            final Throwable cause) {
        super("Thread " + threadId + " already exists in tapestry " + tapestryId, cause);
        this.threadId = threadId;
        this.tapestryId = tapestryId;
    }

    /**
     * Get the tapestry id.
     *
     * @return the tapestry id
     */
    public String getTapestryId() {
        return tapestryId;
    }

    /**
     * Get the thread id.
     *
     * @return the thread id
     */
    public String getThreadId() {
        return threadId;
    }
}
