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

public class OsFlavour {

    private int disk;
    private String id;
    private String name;
    private double ram;
    private int vcpus;

    public OsFlavour() {}

    public OsFlavour(final String id, final String name, final int disk, final int ram, final int vcpus) {
        this.id = id;
        this.name = name;
        this.disk = disk;
        this.ram = ram;
        this.vcpus = vcpus;
    }

    /**
     * @return the disk
     */
    public int getDisk() {
        return disk;
    }

    /**
     * @param disk the disk to set
     */
    public void setDisk(final int disk) {
        this.disk = disk;
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

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the ram
     */
    public double getRam() {
        return ram;
    }

    /**
     * @param ram the ram to set
     */
    public void setRam(final double ram) {
        this.ram = ram;
    }

    /**
     * @return the vcpus
     */
    public int getVcpus() {
        return vcpus;
    }

    /**
     * @param vcpus the vcpus to set
     */
    public void setVcpus(final int vcpus) {
        this.vcpus = vcpus;
    }


}
