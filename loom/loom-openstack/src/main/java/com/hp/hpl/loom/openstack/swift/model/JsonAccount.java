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
package com.hp.hpl.loom.openstack.swift.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Models the swift accounts.
 *
 */
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonAccount {
    private int containerCount;
    private int objectCount;
    private int bytesUsed;

    private JsonContainer[] containers;

    /**
     * @return the containerCount
     */
    public int getContainerCount() {
        return containerCount;
    }

    /**
     * @param containerCount the containerCount to set
     */
    public void setContainerCount(final int containerCount) {
        this.containerCount = containerCount;
    }

    /**
     * @return the objectCount
     */
    public int getObjectCount() {
        return objectCount;
    }

    /**
     * @param objectCount the objectCount to set
     */
    public void setObjectCount(final int objectCount) {
        this.objectCount = objectCount;
    }

    /**
     * @return the bytesUsed
     */
    public int getBytesUsed() {
        return bytesUsed;
    }

    /**
     * @param bytesUsed the bytesUsed to set
     */
    public void setBytesUsed(final int bytesUsed) {
        this.bytesUsed = bytesUsed;
    }

    /**
     * @return the containers
     */
    public JsonContainer[] getContainers() {
        return containers;
    }

    /**
     * @param containers the containers to set
     */
    public void setContainers(final JsonContainer[] containers) {
        this.containers = containers;
    }
}
