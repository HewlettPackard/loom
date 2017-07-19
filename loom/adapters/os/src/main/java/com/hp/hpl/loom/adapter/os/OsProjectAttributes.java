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

public class OsProjectAttributes extends CoreItemAttributes {

    private String providerId;

    private int coresQuota;
    private int instancesQuota;
    private int injectedFilesQuota;
    private int injectedFileContentBytes; // indication of allowed bytes, no
    // quota;
    private int volumesQuota;
    private int snapshotsQuota;
    private int gigabytesQuota;
    private int ramQuota;
    private int securityGroupsQuota;
    private int securityGroupRulesQuota;
    private int floatingIpsQuota;

    public OsProjectAttributes() {
        super();
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
     * @return the coresQuota
     */
    public int getCoresQuota() {
        return coresQuota;
    }

    /**
     * @param coresQuota the coresQuota to set
     */
    public void setCoresQuota(final int coresQuota) {
        this.coresQuota = coresQuota;
    }

    /**
     * @return the instancesQuota
     */
    public int getInstancesQuota() {
        return instancesQuota;
    }

    /**
     * @param instancesQuota the instancesQuota to set
     */
    public void setInstancesQuota(final int instancesQuota) {
        this.instancesQuota = instancesQuota;
    }

    /**
     * @return the injectedFilesQuota
     */
    public int getInjectedFilesQuota() {
        return injectedFilesQuota;
    }

    /**
     * @param injectedFilesQuota the injectedFilesQuota to set
     */
    public void setInjectedFilesQuota(final int injectedFilesQuota) {
        this.injectedFilesQuota = injectedFilesQuota;
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
     * @return the volumesQuota
     */
    public int getVolumesQuota() {
        return volumesQuota;
    }

    /**
     * @param volumesQuota the volumesQuota to set
     */
    public void setVolumesQuota(final int volumesQuota) {
        this.volumesQuota = volumesQuota;
    }

    /**
     * @return the snapshotsQuota
     */
    public int getSnapshotsQuota() {
        return snapshotsQuota;
    }

    /**
     * @param snapshotsQuota the snapshotsQuota to set
     */
    public void setSnapshotsQuota(final int snapshotsQuota) {
        this.snapshotsQuota = snapshotsQuota;
    }

    /**
     * @return the gigabytesQuota
     */
    public int getGigabytesQuota() {
        return gigabytesQuota;
    }

    /**
     * @param gigabytesQuota the gigabytesQuota to set
     */
    public void setGigabytesQuota(final int gigabytesQuota) {
        this.gigabytesQuota = gigabytesQuota;
    }

    /**
     * @return the ramQuota
     */
    public int getRamQuota() {
        return ramQuota;
    }

    /**
     * @param ramQuota the ramQuota to set
     */
    public void setRamQuota(final int ramQuota) {
        this.ramQuota = ramQuota;
    }

    /**
     * @return the securityGroupsQuota
     */
    public int getSecurityGroupsQuota() {
        return securityGroupsQuota;
    }

    /**
     * @param securityGroupsQuota the securityGroupsQuota to set
     */
    public void setSecurityGroupsQuota(final int securityGroupsQuota) {
        this.securityGroupsQuota = securityGroupsQuota;
    }

    /**
     * @return the securityGroupRulesQuota
     */
    public int getSecurityGroupRulesQuota() {
        return securityGroupRulesQuota;
    }

    /**
     * @param securityGroupRulesQuota the securityGroupRulesQuota to set
     */
    public void setSecurityGroupRulesQuota(final int securityGroupRulesQuota) {
        this.securityGroupRulesQuota = securityGroupRulesQuota;
    }

    /**
     * @return the floatingIpsQuota
     */
    public int getFloatingIpsQuota() {
        return floatingIpsQuota;
    }

    /**
     * @param floatingIpsQuota the floatingIpsQuota to set
     */
    public void setFloatingIpsQuota(final int floatingIpsQuota) {
        this.floatingIpsQuota = floatingIpsQuota;
    }


}
