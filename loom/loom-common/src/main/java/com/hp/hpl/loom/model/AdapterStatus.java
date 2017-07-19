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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * Model class for the adapterStutus information.
 */
@JsonAutoDetect
public class AdapterStatus {
    // the adapter provider type
    private String providerType;

    // the adapter provider Id
    private String providerId;

    // the build from the adapter status response
    private String build;

    // the version from the adapter status response
    private String version;

    // the adapter name
    private String name;

    // the adapter class
    private String className;

    // the adapter load time
    private long loadedTime;

    // the adapter id
    private String propertiesName;

    // the status events
    private List<StatusEvent> statusEvents;

    /**
     * No-arg constructor for JSON serialisation.
     */
    public AdapterStatus() {}

    /**
     * @param build adapter status build string
     * @param version adapter version number
     */
    public AdapterStatus(final String build, final String version) {
        this.build = build;
        this.version = version;
    }

    /**
     * Get adapter build.
     *
     * @return build string
     */
    public String getBuild() {
        return build;
    }

    /**
     * Get adapter version.
     *
     * @return version string
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
     * @return the adapter name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the adapterName to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the adapter class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className the adapter class to set
     */
    public void setClassName(final String className) {
        this.className = className;
    }

    /**
     * @return the loadedTime
     */
    public long getLoadedTime() {
        return loadedTime;
    }

    /**
     * @param loadedTime the loadedTime to set
     */
    public void setLoadedTime(final long loadedTime) {
        this.loadedTime = loadedTime;
    }

    /**
     * Returns an id based on the provider id and type.
     *
     * @return adapter Id - built from the providerId / providerType
     */
    public String getId() {
        return providerId + "/" + providerType;
    }

    /**
     * Ignored but there to keep JSON parsing happy.
     *
     * @param id id that is ignored.
     */
    public void setId(final String id) {}

    /**
     * @return the providerType
     */
    public String getProviderType() {
        return providerType;
    }

    /**
     * @param providerType the providerType to set
     */
    public void setProviderType(final String providerType) {
        this.providerType = providerType;
    }

    /**
     * @return the providerId
     */
    public String getProviderId() {
        return providerId;
    }

    /**
     * @param providerId the providerId to set
     */
    public void setProviderId(final String providerId) {
        this.providerId = providerId;
    }

    /**
     * @return the statusEvents
     */
    public List<StatusEvent> getStatusEvents() {
        return statusEvents;
    }

    /**
     * @param statusEvents the statusEvents to set
     */
    public void setStatusEvents(final List<StatusEvent> statusEvents) {
        this.statusEvents = statusEvents;
    }

    /**
     * @return the propertiesName
     */
    public String getPropertiesName() {
        return propertiesName;
    }

    /**
     * @param propertiesName the propertiesName to set
     */
    public void setPropertiesName(final String propertiesName) {
        this.propertiesName = propertiesName;
    }

}
