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
 * Thrown when the thread definition isn't found.
 */
public class NoSuchThreadDefinitionException extends CheckedLoomException {
    private String threadId;

    /**
     * @param threadId the threadId
     */
    public NoSuchThreadDefinitionException(final String threadId) {
        super("Thread " + threadId + " does not exist");
        this.threadId = threadId;
    }

    /**
     * @param threadId the threadId
     * @param cause the cause
     */
    public NoSuchThreadDefinitionException(final String threadId, final Throwable cause) {
        super("Thread " + threadId + " does not exist", cause);
        this.threadId = threadId;
    }

    /**
     * Get the threadId.
     *
     * @return the threadId
     */
    public String getThreadId() {
        return threadId;
    }
}
