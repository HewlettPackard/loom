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
 * Thrown when query results are pending.
 */
public class PendingQueryResultsException extends CheckedLoomException {

    private String logicalId;

    /**
     * @param logicalId the logcialId
     */
    public PendingQueryResultsException(final String logicalId) {
        super("Logical " + logicalId + " has not been populated yet");
        this.logicalId = logicalId;
    }

    /**
     * @param logicalId the logical id
     * @param cause the cause
     */
    public PendingQueryResultsException(final String logicalId, final Throwable cause) {
        super("Logical " + logicalId + " has not been populated yet", cause);
        this.logicalId = logicalId;
    }

    /**
     * Get the logical Id.
     *
     * @return the logicalId
     */
    public String getLogicalId() {
        return logicalId;
    }
}
