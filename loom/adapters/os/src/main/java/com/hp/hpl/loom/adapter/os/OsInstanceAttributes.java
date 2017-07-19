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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hp.hpl.loom.model.CoreItemAttributes;

public class OsInstanceAttributes extends CoreItemAttributes {

    private OsFlavour osFlavour;
    // flat structure for now
    private String flavor = null;
    private String flavorId = null;
    private double ram = -1.0;
    private int disk = -1;
    private int vcpus = -1;

    private String accessIPv4;
    private String accessIPv6;
    private String created;
    private String updated;
    private int progress;
    private String status;
    private String hostId;

    public OsInstanceAttributes() {
        super();
    }

    public OsInstanceAttributes(final OsFlavour osFlavour, final String accessIPv4, final String accessIPv6,
            final String created, final String updated, final int progress, final String status, final String hostId) {
        super();
        setOsFlavour(osFlavour);
        this.accessIPv4 = accessIPv4;
        this.accessIPv6 = accessIPv6;
        this.created = created;
        this.updated = updated;
        this.progress = progress;
        this.status = status;
        this.hostId = hostId;
    }

    public OsInstanceAttributes(final OsFlavour osFlavour) {
        super();
        setOsFlavour(osFlavour);
        accessIPv4 = null;
        accessIPv6 = null;
        created = null;
        updated = null;
        progress = 0;
        status = null;
        hostId = null;
    }

    @JsonIgnore
    public void setOsFlavour(final OsFlavour osFlavour) {
        this.osFlavour = osFlavour;
        if (osFlavour != null) {
            flavor = osFlavour.getName();
            flavorId = osFlavour.getId();
            ram = osFlavour.getRam();
            disk = osFlavour.getDisk();
            vcpus = osFlavour.getVcpus();
        }
    }

    @JsonIgnore
    public OsFlavour getOsFlavour() {
        return osFlavour;
    }


    /**
     * Sets the flavorName
     * 
     * @param flavorName
     */
    public void setFlavor(final String flavourName) {
        flavor = flavourName;
        if (osFlavour == null) {
            osFlavour = new OsFlavour();
        }
        osFlavour.setName(flavourName);
    }

    /**
     * Set the flavor id
     * 
     * @param flavorId
     */
    public void setFlavorId(final String flavourId) {
        flavorId = flavourId;
        if (osFlavour == null) {
            osFlavour = new OsFlavour();
        }
        osFlavour.setId(flavourId);
    }

    /**
     * Set the ram
     * 
     * @param ram
     */
    public void setRam(final double ram) {
        this.ram = ram;
        if (osFlavour == null) {
            osFlavour = new OsFlavour();
        }
        osFlavour.setRam(ram);
    }

    /**
     * Set the disk
     * 
     * @param disk
     */
    public void setDisk(final int disk) {
        this.disk = disk;
        if (osFlavour == null) {
            osFlavour = new OsFlavour();
        }
        osFlavour.setDisk(disk);
    }

    /**
     * Set the vcpus
     * 
     * @param vcpus
     */
    public void setVcpus(final int vcpus) {
        this.vcpus = vcpus;
        if (osFlavour == null) {
            osFlavour = new OsFlavour();
        }
        osFlavour.setVcpus(vcpus);
    }

    /**
     * Get the flavor
     * 
     * @return
     */
    public String getFlavor() {
        return flavor;
    }

    /**
     * Get the flavor id
     * 
     * @return
     */
    public String getFlavorId() {
        return flavorId;
    }

    /**
     * Get the ram
     * 
     * @return
     */
    public double getRam() {
        return ram;
    }

    /**
     * Get the disk
     * 
     * @return
     */
    public int getDisk() {
        return disk;
    }

    /**
     * Get the vcpus
     * 
     * @return
     */
    public int getVcpus() {
        return vcpus;
    }

    // end of flavour related attrs


    /**
     * Set the accessIPv4
     * 
     * @param accessIPv4
     */
    public void setAccessIPv4(final String accessIPv4) {
        this.accessIPv4 = accessIPv4;
    }

    /**
     * Get the accessIPv4
     * 
     * @return
     */
    public String getAccessIPv4() {
        return accessIPv4;
    }

    /**
     * Set the accessIPv6
     * 
     * @param accessIPv6
     */
    public void setAccessIPv6(final String accessIPv6) {
        this.accessIPv6 = accessIPv6;
    }

    /**
     * Get the accessIPv6
     * 
     * @return
     */
    public String getAccessIPv6() {
        return accessIPv6;
    }

    /**
     * Get the created string
     * 
     * @return
     */
    public String getCreated() {
        return created;
    }

    /**
     * Set the created string
     * 
     * @param created
     */
    public void setCreated(final String created) {
        this.created = created;
    }

    /**
     * Get the updated string
     * 
     * @return
     */
    public String getUpdated() {
        return updated;
    }

    /**
     * Set the updated string
     * 
     * @param updated
     */
    public void setUpdated(final String updated) {
        this.updated = updated;
    }

    /**
     * Get the progress
     * 
     * @return
     */
    public int getProgress() {
        return progress;
    }

    /**
     * Set the progress
     * 
     * @param progress
     */
    public void setProgress(final int progress) {
        this.progress = progress;
    }

    /**
     * Get the status
     * 
     * @return
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set the status
     * 
     * @param status
     */
    public void setStatus(final String status) {
        this.status = status;
    }

    /**
     * Get the host id
     * 
     * @return
     */
    public String getHostId() {
        return hostId;
    }

    /**
     * Set the host id
     * 
     * @param hostId
     */
    public void setHostId(final String hostId) {
        this.hostId = hostId;
    }

    @Override
    public String toString() {
        return "status: " + status;
    }
}
