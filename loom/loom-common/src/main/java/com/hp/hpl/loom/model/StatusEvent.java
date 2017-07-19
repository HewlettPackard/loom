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

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * This class holds details of a particular status event, this could be a warning/error/info.
 */
@JsonAutoDetect
public class StatusEvent {
    private String message;
    private StatusTypeEnum type = null;
    private long timestamp;

    /**
     * Constructs an adapterEvent.
     *
     * @param message adapter message (warning etc)
     * @param type the message type (info/warning/error)
     * @param timestamp the timestamp it occurred.
     */
    public StatusEvent(final String message, final StatusTypeEnum type, final long timestamp) {
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the type
     */
    public StatusTypeEnum getType() {
        return type;
    }

    /**
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }
}
