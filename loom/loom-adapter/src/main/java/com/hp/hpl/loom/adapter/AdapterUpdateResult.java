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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * Wrapper for the UpdateResult that models the Adapter update results.
 */
public class AdapterUpdateResult extends UpdateResult {
    private static final Log LOG = LogFactory.getLog(AdapterUpdateResult.class);
    private boolean forceUpdate = false; // Only used by Adapter
    private int updateEstimate = 0; // Only used by Adapter

    /**
     * Constructor for the wrapper.
     *
     * @param expectedSize the number of results we expect, used to size lists
     */
    public AdapterUpdateResult(final int expectedSize) {
        super(expectedSize);
    }

    /**
     * Set the update estimate. This is used by the adapter.
     *
     * @param updateEstimate the estimate of number of updates.
     */
    public void setUpdateEstimate(final int updateEstimate) { // Only used by Adapter
        this.updateEstimate = updateEstimate;
    }

    /**
     * Return the update estimate. Only used by Adapter.
     *
     * @return the estimated updates.
     */
    public int getUpdateEstimate() {
        return updateEstimate;
    }

    /**
     * Force an update on this results. Only used by Adapter.
     */
    public void forceUpdate() {
        forceUpdate = true;
    }

    /**
     * Returns true if the results have changed.
     *
     * @return true if the results have changed.
     */
    public boolean hasAnyChange() {
        if (forceUpdate || !getNewItems().isEmpty() || !getDeletedItems().isEmpty() || !getUpdatedItems().isEmpty()) {
            this.setIgnore(false);
            return true;
        }
        return getUpdateDelta() != null && !getUpdateDelta().isEmpty();
    }
}
