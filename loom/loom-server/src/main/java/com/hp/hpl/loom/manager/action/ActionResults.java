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
package com.hp.hpl.loom.manager.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.loom.model.ActionResult;

public class ActionResults extends ActionResult {
    // links to other other actionResults

    // @JsonIgnore
    private List<ActionResult> results = new ArrayList<>();

    /**
     * @return the results
     */
    public List<ActionResult> getResults() {
        return results;
    }

    /**
     * @param results the results to set
     */
    public void setResults(final List<ActionResult> results) {
        this.results = results;
    }

    public void finalProcessing() {
        for (ActionResult ar : results) {
            this.setFile(ar.getFile());
            this.setErrorMessage(ar.getErrorMessage());
        }
    }

    /**
     * Get the status.
     *
     * @return the status.
     */
    @Override
    public Status getOverallStatus() {
        boolean complete = true;
        boolean aborted = false;
        for (ActionResult actionResult : results) {
            if (!actionResult.getOverallStatus().equals(Status.completed)) {
                complete = false;
                if (actionResult.getOverallStatus().equals(Status.aborted)) {
                    aborted = true;
                }
            } else if (actionResult.getOverallStatus().equals(Status.aborted)) {
                aborted = true;
            }
        }

        if (complete) {
            return Status.completed;
        } else if (aborted) {
            return Status.aborted;
        } else {
            return Status.pending;
        }
    }


    /**
     * @return the targetToStatus
     */
    @Override
    public Map<String, Status> getTargetToStatus() {
        Map<String, Status> allTargetToStatus = new HashMap<>();
        for (ActionResult actionResult : results) {
            allTargetToStatus.putAll(actionResult.getTargetToStatus());
        }
        return allTargetToStatus;
    }

    @Override
    public void setTargetToStatus(final Map<String, Status> targetToStatus) {
        throw new UnsupportedOperationException("Setting the targetToStatus on the ActionResults isn't supported");
    }

    @Override
    public void setOverallStatus(final Status status) {
        throw new UnsupportedOperationException("Setting the overall status on the ActionResults isn't supported");
    }

}
