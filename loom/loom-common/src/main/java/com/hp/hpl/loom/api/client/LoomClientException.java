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
package com.hp.hpl.loom.api.client;

/**
 * Exception thrown by LoomClient.
 */
public class LoomClientException extends RuntimeException {

    private int statusCode;

    /**
     * No-args constructor.
     */
    public LoomClientException() {
        super();
    }

    /**
     * @param message the message
     */
    public LoomClientException(final String message) {
        super(message);
    }

    /**
     * @param message the message
     * @param cause the cause
     */
    public LoomClientException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause the cause
     */
    public LoomClientException(final Throwable cause) {
        super(cause);
    }

    /**
     * @param message the message
     * @param status the status
     */
    public LoomClientException(final String message, final int status) {
        super(message);
        statusCode = status;
    }

    /**
     * @param message the message
     * @param status the status
     * @param cause the cause
     */
    public LoomClientException(final String message, final int status, final Throwable cause) {
        super(message, cause);
        statusCode = status;
    }

    /**
     * @param status the status
     * @param cause the cause
     */
    public LoomClientException(final int status, final Throwable cause) {
        super(cause);
        statusCode = status;
    }

    /**
     * @return the statusCode
     */
    public int getStatusCode() {
        return statusCode;
    }
}
