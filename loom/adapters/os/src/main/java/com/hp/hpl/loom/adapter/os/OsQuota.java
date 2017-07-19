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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hp.hpl.loom.exceptions.QuotaIsNotSetException;

@JsonAutoDetect
public class OsQuota {

    private String id;
    @JsonIgnore
    private int cores;
    @JsonIgnore
    private int instances;
    @JsonIgnore
    private int injectedFiles;
    @JsonIgnore
    private int injectedFileContentBytes; // indication of allowed bytes, no quota;
    @JsonIgnore
    private int volumes;
    @JsonIgnore
    private int snapshots;
    @JsonIgnore
    private int gigabytes;
    @JsonIgnore
    private int ram;
    @JsonIgnore
    private int securityGroups;
    @JsonIgnore
    private int securityGroupRules;
    @JsonIgnore
    private int floatingIps;

    // private int networks;
    // private int usedNetworks;
    // private int ports;
    // private int usedPorts;
    // private int routers;
    // private int used routers;
    // private int subnets;
    // private int usedSubnets;

    private double coresUtil;
    private double instancesUtil;
    private double injectedFilesUtil;
    private double volumesUtil;
    private double snapshotsUtil;
    private double gigabytesUtil;
    private double ramUtil;
    private double securityGroupsUtil;
    private double securityGroupRulesUtil;
    private double floatingIpsUtil;

    private boolean isQuotaSet;

    public OsQuota() {
        isQuotaSet = false;
    }

    public OsQuota(final String id, final int cores, final int instances, final int injectedFiles,
            final int injectedFileContentBytes, final int volumes, final int snapshots, final int gigabytes,
            final int ram, final int securityGroups, final int securityGroupRules, final int floatingIps) {
        this.id = id;
        this.cores = cores;
        this.instances = instances;
        this.injectedFiles = injectedFiles;
        this.injectedFileContentBytes = injectedFileContentBytes;
        this.volumes = volumes;
        this.snapshots = snapshots;
        this.gigabytes = gigabytes;
        this.ram = ram;
        this.securityGroups = securityGroups;
        this.securityGroupRules = securityGroupRules;
        this.floatingIps = floatingIps;
        isQuotaSet = true;
    }

    public void setUtilisation(final int usedCores, final int usedInstances, final int usedInjectedFiles,
            final int usedVolumes, final int usedSnapshots, final int usedGigabytes, final int usedRam,
            final int usedSecurityGroups, final int usedSecurityGroupRules, final int usedFloatingIps)
            throws QuotaIsNotSetException {
        if (isQuotaSet) {
            if (cores > 0) {
                setCoresUtil((double) usedCores / (double) cores);
            } else {
                setCoresUtil(0);
            }
            if (instances > 0) {
                setInstancesUtil((double) usedInstances / (double) instances);
            } else {
                setInstancesUtil(0);
            }
            if (injectedFiles > 0) {
                setInjectedFilesUtil((double) usedInjectedFiles / (double) injectedFiles);
            } else {
                setInjectedFilesUtil(0);
            }
            if (volumes > 0) {
                setVolumesUtil((double) usedVolumes / (double) volumes);
            } else {
                setVolumesUtil(0);
            }
            if (snapshots > 0) {
                setSnapshotsUtil((double) usedSnapshots / (double) snapshots);
            } else {
                setSnapshotsUtil(0);
            }
            if (gigabytes > 0) {
                setGigabytesUtil((double) usedGigabytes / (double) gigabytes);
            } else {
                setGigabytesUtil(0);
            }
            if (ram > 0) {
                setRamUtil((double) usedRam / (double) ram);
            } else {
                setRamUtil(0);
            }
            if (securityGroups > 0) {
                setSecurityGroupsUtil((double) usedSecurityGroups / (double) securityGroups);
            } else {
                setSecurityGroupsUtil(0);
            }
            if (securityGroupRules > 0) {
                setSecurityGroupRulesUtil((double) usedSecurityGroupRules / (double) securityGroupRules);
            } else {
                setSecurityGroupRulesUtil(0);
            }
            if (floatingIps > 0) {
                setFloatingIpsUtil((double) usedFloatingIps / (double) floatingIps);
            } else {
                setFloatingIpsUtil(0);
            }
        } else {
            throw new QuotaIsNotSetException();
        }
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
     * @return the cores
     */
    public int getCores() {
        return cores;
    }

    /**
     * @param cores the cores to set
     */
    public void setCores(final int cores) {
        this.cores = cores;
    }

    /**
     * @return the instances
     */
    public int getInstances() {
        return instances;
    }

    /**
     * @param instances the instances to set
     */
    public void setInstances(final int instances) {
        this.instances = instances;
    }

    /**
     * @return the injectedFiles
     */
    public int getInjectedFiles() {
        return injectedFiles;
    }

    /**
     * @param injectedFiles the injectedFiles to set
     */
    public void setInjectedFiles(final int injectedFiles) {
        this.injectedFiles = injectedFiles;
    }

    /**
     * @return the injectedFileContentBytes
     */
    public int getInjectedFileContentBytes() {
        return injectedFileContentBytes;
    }

    /**
     * @param injectedFileContentBytes the injectedFileContentBytes to set
     */
    public void setInjectedFileContentBytes(final int injectedFileContentBytes) {
        this.injectedFileContentBytes = injectedFileContentBytes;
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
     * @return the ram
     */
    public int getRam() {
        return ram;
    }

    /**
     * @param ram the ram to set
     */
    public void setRam(final int ram) {
        this.ram = ram;
    }

    /**
     * @return the securityGroups
     */
    public int getSecurityGroups() {
        return securityGroups;
    }

    /**
     * @param securityGroups the securityGroups to set
     */
    public void setSecurityGroups(final int securityGroups) {
        this.securityGroups = securityGroups;
    }

    /**
     * @return the securityGroupRules
     */
    public int getSecurityGroupRules() {
        return securityGroupRules;
    }

    /**
     * @param securityGroupRules the securityGroupRules to set
     */
    public void setSecurityGroupRules(final int securityGroupRules) {
        this.securityGroupRules = securityGroupRules;
    }

    /**
     * @return the floatingIps
     */
    public int getFloatingIps() {
        return floatingIps;
    }

    /**
     * @param floatingIps the floatingIps to set
     */
    public void setFloatingIps(final int floatingIps) {
        this.floatingIps = floatingIps;
    }

    /**
     * @return the coresUtil
     */
    public double getCoresUtil() {
        return coresUtil;
    }

    /**
     * @param coresUtil the coresUtil to set
     */
    public void setCoresUtil(final double coresUtil) {
        this.coresUtil = coresUtil;
    }

    /**
     * @return the instancesUtil
     */
    public double getInstancesUtil() {
        return instancesUtil;
    }

    /**
     * @param instancesUtil the instancesUtil to set
     */
    public void setInstancesUtil(final double instancesUtil) {
        this.instancesUtil = instancesUtil;
    }

    /**
     * @return the injectedFilesUtil
     */
    public double getInjectedFilesUtil() {
        return injectedFilesUtil;
    }

    /**
     * @param injectedFilesUtil the injectedFilesUtil to set
     */
    public void setInjectedFilesUtil(final double injectedFilesUtil) {
        this.injectedFilesUtil = injectedFilesUtil;
    }

    /**
     * @return the volumesUtil
     */
    public double getVolumesUtil() {
        return volumesUtil;
    }

    /**
     * @param volumesUtil the volumesUtil to set
     */
    public void setVolumesUtil(final double volumesUtil) {
        this.volumesUtil = volumesUtil;
    }

    /**
     * @return the snapshotsUtil
     */
    public double getSnapshotsUtil() {
        return snapshotsUtil;
    }

    /**
     * @param snapshotsUtil the snapshotsUtil to set
     */
    public void setSnapshotsUtil(final double snapshotsUtil) {
        this.snapshotsUtil = snapshotsUtil;
    }

    /**
     * @return the gigabytesUtil
     */
    public double getGigabytesUtil() {
        return gigabytesUtil;
    }

    /**
     * @param gigabytesUtil the gigabytesUtil to set
     */
    public void setGigabytesUtil(final double gigabytesUtil) {
        this.gigabytesUtil = gigabytesUtil;
    }

    /**
     * @return the ramUtil
     */
    public double getRamUtil() {
        return ramUtil;
    }

    /**
     * @param ramUtil the ramUtil to set
     */
    public void setRamUtil(final double ramUtil) {
        this.ramUtil = ramUtil;
    }

    /**
     * @return the securityGroupsUtil
     */
    public double getSecurityGroupsUtil() {
        return securityGroupsUtil;
    }

    /**
     * @param securityGroupsUtil the securityGroupsUtil to set
     */
    public void setSecurityGroupsUtil(final double securityGroupsUtil) {
        this.securityGroupsUtil = securityGroupsUtil;
    }

    /**
     * @return the securityGroupRulesUtil
     */
    public double getSecurityGroupRulesUtil() {
        return securityGroupRulesUtil;
    }

    /**
     * @param securityGroupRulesUtil the securityGroupRulesUtil to set
     */
    public void setSecurityGroupRulesUtil(final double securityGroupRulesUtil) {
        this.securityGroupRulesUtil = securityGroupRulesUtil;
    }

    /**
     * @return the floatingIpsUtil
     */
    public double getFloatingIpsUtil() {
        return floatingIpsUtil;
    }

    /**
     * @param floatingIpsUtil the floatingIpsUtil to set
     */
    public void setFloatingIpsUtil(final double floatingIpsUtil) {
        this.floatingIpsUtil = floatingIpsUtil;
    }

    /**
     * @return the isQuotaSet
     */
    public boolean isQuotaSet() {
        return isQuotaSet;
    }

    /**
     * @param isQuotaSet the isQuotaSet to set
     */
    public void setQuotaSet(final boolean isQuotaSet) {
        this.isQuotaSet = isQuotaSet;
    }


}
