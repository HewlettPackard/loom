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

public class OsImageAttributes extends CoreItemAttributes {

    protected String updated;
    protected String created;
    protected String status;
    protected int minDisk;
    protected int minRam;

    protected long size;
    protected String checksum;
    protected String containerFormat;
    protected String diskFormat;
    protected String visibility;

    public OsImageAttributes() {
        super();
    }

    /**
     * @return the updated
     */
    public String getUpdated() {
        return updated;
    }

    /**
     * @param updated the updated to set
     */
    public void setUpdated(final String updated) {
        this.updated = updated;
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
     * @return the minDisk
     */
    public int getMinDisk() {
        return minDisk;
    }

    /**
     * @param minDisk the minDisk to set
     */
    public void setMinDisk(final int minDisk) {
        this.minDisk = minDisk;
    }

    /**
     * @return the minRam
     */
    public int getMinRam() {
        return minRam;
    }

    /**
     * @param minRam the minRam to set
     */
    public void setMinRam(final int minRam) {
        this.minRam = minRam;
    }

    /**
     * @return the size
     */
    public long getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(final long size) {
        this.size = size;
    }

    /**
     * @return the checksum
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * @param checksum the checksum to set
     */
    public void setChecksum(final String checksum) {
        this.checksum = checksum;
    }

    /**
     * @return the containerFormat
     */
    public String getContainerFormat() {
        return containerFormat;
    }

    /**
     * @param containerFromat the containerFormat to set
     */
    public void setContainerFormat(final String containerFormat) {
        this.containerFormat = containerFormat;
    }

    /**
     * @return the diskFormat
     */
    public String getDiskFormat() {
        return diskFormat;
    }

    /**
     * @param diskFormat the diskFormat to set
     */
    public void setDiskFormat(final String diskFormat) {
        this.diskFormat = diskFormat;
    }

    /**
     * @return the visibility
     */
    public String getVisibility() {
        return visibility;
    }

    /**
     * @param visibility the visibility to set
     */
    public void setVisibility(final String visibility) {
        this.visibility = visibility;
    }



}
