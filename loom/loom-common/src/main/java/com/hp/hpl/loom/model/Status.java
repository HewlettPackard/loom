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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Model class for the adapter status information.
 */
@JsonAutoDetect
public class Status {

    // the build from the status response
    private String build;

    // the version from the status response
    private String version;

    private List<AdapterStatus> adapters = new ArrayList<>();
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, List<StatusEvent>> statusEvents = new HashMap<>();

    /**
     * No-arg constructor for JSON serialisation.
     */
    public Status() {}

    /**
     * @param build adapter build
     * @param version adapter version
     */
    public Status(final String build, final String version) {
        this.build = build;
        this.version = version;
    }

    /**
     * The adapter status build.
     *
     * @return the status build.
     */
    public String getBuild() {
        return build;
    }

    /**
     * The adapter status version.
     *
     * @return the status version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param build the build to set
     */
    public void setBuild(final String build) {
        this.build = build;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(final String version) {
        this.version = version;
    }

    /**
     * @param adapterStatus the list of adapterStatus
     */
    public void setAdapters(final List<AdapterStatus> adapterStatus) {
        adapters = adapterStatus;
    }

    /**
     * Get the list of adapterStatus.
     *
     * @return List of adapterStatus
     */
    public List<AdapterStatus> getAdapters() {
        return adapters;
    }

    /**
     * @return the statusEvents
     */
    public Map<String, List<StatusEvent>> getStatusEvents() {
        return statusEvents;
    }

    /**
     * @param statusEvents the statusEvents to set
     */
    public void setStatusEvents(final Map<String, List<StatusEvent>> statusEvents) {
        this.statusEvents = statusEvents;
    }
}
