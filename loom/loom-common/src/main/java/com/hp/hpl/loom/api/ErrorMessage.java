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
package com.hp.hpl.loom.api;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Class used to encode additional information, if an error occurred on the server.
 */
@JsonAutoDetect
public class ErrorMessage {

    /** HTTP status code, guaranteed to be included. */
    private String status;

    /** Error message for non-200 HTTP responses. */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String message;

    /* If error was caused by another error, the message of that other error. */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String causedBy;

    /**
     * If available, a stack trace from the server, of the origination of the error.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String stackTrace;

    /**
     * Map of error name to error message.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private HashMap<String, String> errors;

    /**
     * No-arg constructor for JSON.
     */
    public ErrorMessage() {}

    /**
     * Constructor based on the status and a map of error strings.
     *
     * @param status the status
     * @param errors a map of errors
     */
    public ErrorMessage(final String status, final HashMap<String, String> errors) {
        this.status = status;
        this.errors = errors;
    }

    /**
     * Get the status.
     *
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status
     */
    public void setStatus(final String status) {
        this.status = status;
    }

    /**
     * Get the message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message.
     */
    public void setMessage(final String message) {
        this.message = message;
    }

    /**
     * Get the caused by.
     *
     * @return the caused by string.
     */
    public String getCausedBy() {
        return causedBy;
    }

    /**
     * @param causedBy the caused by string
     */
    public void setCausedBy(final String causedBy) {
        this.causedBy = causedBy;
    }

    /**
     * Get the stacktrace string.
     *
     * @return the stack trace string.
     */
    public String getStackTrace() {
        return stackTrace;
    }

    /**
     * @param stackTrace the stacktrace string.
     */
    public void setStackTrace(final String stackTrace) {
        this.stackTrace = stackTrace;
    }

    /**
     * Get the errors map.
     *
     * @return the map of errors.
     */
    public HashMap<String, String> getErrors() {
        return errors;
    }

    /**
     * @param errors the map of errors.
     */
    public void setErrors(final HashMap<String, String> errors) {
        this.errors = errors;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ErrorMessage{");
        sb.append("message='").append(message).append('\'');
        sb.append(", status='").append(status).append('\'');
        sb.append(", causedBy='").append(causedBy).append('\'');
        sb.append(", stackTrace='").append(stackTrace).append('\'');
        sb.append(", errors=").append(errors);
        sb.append('}');
        return sb.toString();
    }
}
