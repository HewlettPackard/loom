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
package com.hp.hpl.loom.adapter.os;

import com.hp.hpl.loom.model.CoreItemAttributes;

public class OsVolumeAttributes extends CoreItemAttributes {

    private int size;
    private String status;
    private String availabilityZone;
    private String created;
    private String volumeType;
    private String snapshotId;

    public OsVolumeAttributes() {
        super();
    }

    public OsVolumeAttributes(final String name, final String id, final int size, final String status,
            final String availabilityZone, final String created, final String volumeType, final String snapshotId) {
        super(name, id);
        this.size = size;
        this.status = status;
        this.availabilityZone = availabilityZone;
        this.created = created;
        this.volumeType = volumeType;
        this.snapshotId = snapshotId;
    }

    /**
     * @return the size
     */
    public int getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(final int size) {
        this.size = size;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(final String status) {
        this.status = status;
    }

    /**
     * @return the availabilityZone
     */
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    /**
     * @param availabilityZone the availabilityZone to set
     */
    public void setAvailabilityZone(final String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    /**
     * @return the created
     */
    public String getCreated() {
        return created;
    }

    /**
     * @param created the created to set
     */
    public void setCreated(final String created) {
        this.created = created;
    }

    /**
     * @return the volumeType
     */
    public String getVolumeType() {
        return volumeType;
    }

    /**
     * @param volumeType the volumeType to set
     */
    public void setVolumeType(final String volumeType) {
        this.volumeType = volumeType;
    }

    /**
     * @return the snapshotId
     */
    public String getSnapshotId() {
        return snapshotId;
    }

    /**
     * @param snapshotId the snapshotId to set
     */
    public void setSnapshotId(final String snapshotId) {
        this.snapshotId = snapshotId;
    }

}
