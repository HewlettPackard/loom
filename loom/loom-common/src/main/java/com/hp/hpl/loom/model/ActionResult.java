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
package com.hp.hpl.loom.model;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The result of an action.
 */
@JsonAutoDetect
public class ActionResult {

    /**
     * The status of the action result.
     */
    public enum Status {
        /**
         * Action Completed.
         */
        completed,
        /**
         * Action Aborted.
         */
        aborted,
        /**
         * Action Pending.
         */
        pending;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private UUID id;

    private Status status;

    private Map<String, Status> targetToStatus = new HashMap<>();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private ActionResultFile file;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String errorMessage;

    private int interval = 0;

    /**
     * No-arg constructor for JSON.
     */
    public ActionResult() {}

    /**
     * Used for most cases where the status gets completed on invoking the method (no "future"-like
     * behaviour) status= completed.
     *
     * @param status the status
     */
    public ActionResult(final Status status) {
        this.status = status;
    }

    /**
     *
     * @param status status of the action
     * @param file an ActionResultFile object
     */
    public ActionResult(final Status status, final ActionResultFile file) {
        this(status);
        setFile(file);
    }

    /**
     * @return ActionResultFile
     */
    public ActionResultFile getFile() {
        return file;
    }

    /**
     * @param file an ActionResultFile object
     */
    public void setFile(ActionResultFile file) {
        if (file != null) {
            if (file.content != null) {
                String content = file.content.toString();
                if (content != null && !content.isEmpty()) {
                    try {
                        // Support only UTF-8 encoded string at the moment.
                        content.getBytes("UTF-8");
                        this.file = file;
                    } catch (UnsupportedEncodingException e) {
                        this.file.content = "";
                    }
                }
            }
        }
    }

    /**
     * Get error message obtained when a sync action is completed unsuccessfully.
     *
     * @return error message.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Set error message obtained when a sync action is completed unsuccessfully.
     *
     * @param errorMessage error message.
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Get the id (a UUID).
     *
     * @return the id
     */
    public UUID getId() {
        return id;
    }

    /**
     * @param id the UUId
     */
    public void setId(final UUID id) {
        this.id = id;
    }

    /**
     * Get the status.
     *
     * @return the status.
     */
    public Status getOverallStatus() {
        return status;
    }

    /**
     * @param s the status
     */
    public void setOverallStatus(final Status s) {
        status = s;
    }

    /**
     * Get the interval.
     *
     * @return the interval
     */
    public int getInterval() {
        return interval;
    }

    /**
     * @param interval the interval
     */
    public void setInterval(final int interval) {
        this.interval = interval;
    }


    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("id -> " + id);
        str.append("; status-> " + status);
        str.append("; interval-> " + interval);
        return str.toString();
    }

    /**
     * @return the targetToStatus
     */
    public Map<String, Status> getTargetToStatus() {
        return targetToStatus;
    }

    /**
     * @param targetToStatus the targetToStatus to set
     */
    public void setTargetToStatus(final Map<String, Status> targetToStatus) {
        this.targetToStatus = targetToStatus;
    }

}
