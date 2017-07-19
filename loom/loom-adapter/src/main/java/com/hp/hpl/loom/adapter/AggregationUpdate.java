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

import com.hp.hpl.loom.model.Aggregation;

/**
 * Data structure to hold the set of updates to be applied to a Grounded Aggregation.
 *
 * @see UpdateResult
 */
public class AggregationUpdate {
    private Aggregation aggregation;
    private UpdateResult updateResult;

    /**
     * Constructor to specify Grounded Aggregation and update to apply.
     *
     * @param aggregation Grounded Aggregation.
     * @param updateResult Update to apply.
     */
    public AggregationUpdate(final Aggregation aggregation, final UpdateResult updateResult) {
        this.aggregation = aggregation;
        this.updateResult = updateResult;
    }

    /**
     * Returns the grounded aggregation.
     *
     * @return Returns a grounded aggregation
     */
    public final Aggregation getAggregation() {
        return aggregation;
    }

    /**
     * Return update to apply.
     *
     * @return Update to apply.
     */
    public final UpdateResult getUpdateResult() {
        return updateResult;
    }
}
