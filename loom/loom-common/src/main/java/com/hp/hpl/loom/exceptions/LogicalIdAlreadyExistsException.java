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
 * Thrown when a logicalId already exists.
 */
public class LogicalIdAlreadyExistsException extends CheckedLoomException {
    private String logicalId;

    /**
     * @param logicalId the logicalId
     */
    public LogicalIdAlreadyExistsException(final String logicalId) {
        super("LogicalId " + logicalId + " already exists");
        this.logicalId = logicalId;
    }

    /**
     * @param logicalId the logicalId
     * @param cause the cause
     */
    public LogicalIdAlreadyExistsException(final String logicalId, final Throwable cause) {
        super("LogicalId " + logicalId + " already exists", cause);
        this.logicalId = logicalId;
    }

    /**
     * Get the logicalId.
     *
     * @return the logicalId
     */
    public String getLogicalId() {
        return logicalId;
    }
}
