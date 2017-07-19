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
package com.hp.hpl.loom.openstack.cinder.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Object to model the VolumeQuota.
 */
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonVolumeQuota {
    private int gigabytes;
    private int snapshots;
    private int volumes;
    private String id;

    /**
     * @return the gigabytes
     */
    public int getGigabytes() {
        return gigabytes;
    }

    /**
     * @param gigabytes the gigabytes to set
     */
    public void setGigabytes(final int gigabytes) {
        this.gigabytes = gigabytes;
    }

    /**
     * @return the snapshots
     */
    public int getSnapshots() {
        return snapshots;
    }

    /**
     * @param snapshots the snapshots to set
     */
    public void setSnapshots(final int snapshots) {
        this.snapshots = snapshots;
    }

    /**
     * @return the volumes
     */
    public int getVolumes() {
        return volumes;
    }

    /**
     * @param volumes the volumes to set
     */
    public void setVolumes(final int volumes) {
        this.volumes = volumes;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(final String id) {
        this.id = id;
    }
}
