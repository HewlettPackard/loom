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
 * Thrown when there isn't a aggregation.
 */
public class NoSuchAggregationException extends CheckedLoomException {
    private String logicalId;

    /**
     * @param aggrId the aggregationId
     */
    public NoSuchAggregationException(final String aggrId) {
        super("Aggregation " + aggrId + " does not exist");
        logicalId = aggrId;
    }

    /**
     * @param aggrId the aggregationId
     * @param cause the cause
     */
    public NoSuchAggregationException(final String aggrId, final Throwable cause) {
        super("Aggregation " + aggrId + " does not exist", cause);
        logicalId = aggrId;
    }

    /**
     * The logical id.
     *
     * @return the logical id
     */
    public String getLogicalId() {
        return logicalId;
    }
}
