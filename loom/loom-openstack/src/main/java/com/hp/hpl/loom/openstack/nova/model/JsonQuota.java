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
package com.hp.hpl.loom.openstack.nova.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Object to model the Quota.
 */
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonQuota {
    @JsonProperty("injected_file_content_bytes")
    private int injectedFileContentBytes;
    @JsonProperty("metadata_items")
    private int metadataItems;
    @JsonProperty("ram")
    private int ram;
    @JsonProperty("floating_ips")
    private int floatingIps;
    @JsonProperty("key_pairs")
    private int keyPairs;
    private String id;
    @JsonProperty("security_group_rules")
    private int securityGroupRules;
    @JsonProperty("injected_files")
    private int injectedFiles;
    private int cores;
    @JsonProperty("fixed_ips")
    private int fixedIps;
    @JsonProperty("injected_file_path_bytes")
    private int injectedFilePathBytes;
    @JsonProperty("security_groups")
    private int securityGroups;
    private int instances;

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
     * @return the metadataItems
     */
    public int getMetadataItems() {
        return metadataItems;
    }

    /**
     * @param metadataItems the metadataItems to set
     */
    public void setMetadataItems(final int metadataItems) {
        this.metadataItems = metadataItems;
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
     * @return the keyPairs
     */
    public int getKeyPairs() {
        return keyPairs;
    }

    /**
     * @param keyPairs the keyPairs to set
     */
    public void setKeyPairs(final int keyPairs) {
        this.keyPairs = keyPairs;
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
     * @return the fixedIps
     */
    public int getFixedIps() {
        return fixedIps;
    }

    /**
     * @param fixedIps the fixedIps to set
     */
    public void setFixedIps(final int fixedIps) {
        this.fixedIps = fixedIps;
    }

    /**
     * @return the injectedFilePathBytes
     */
    public int getInjectedFilePathBytes() {
        return injectedFilePathBytes;
    }

    /**
     * @param injectedFilePathBytes the injectedFilePathBytes to set
     */
    public void setInjectedFilePathBytes(final int injectedFilePathBytes) {
        this.injectedFilePathBytes = injectedFilePathBytes;
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


}
