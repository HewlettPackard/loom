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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hp.hpl.loom.openstack.nova.model.JsonMetadata;

/**
 * Object to model the Volume.
 */
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JsonVolume {
    private String id;
    @JsonProperty("display_name")
    private String displayName;
    @JsonProperty("display_description")
    private String displayDescription;
    private int size;
    @JsonProperty("volume_type")
    private String volumeType;
    private JsonMetadata metadata;
    @JsonProperty("availability_zone")
    private String availabilityZone;
    @JsonProperty("snapshot_id")
    private String snapshotId;

    @JsonProperty("created_at")
    private String createdAt;

    private String status;
    private boolean bootable;

    @JsonProperty("os-vol-tenant-attr:tenant_id")
    private String osVolTenantAttrTenantId;

    @JsonProperty("os-vol-host-attr:host")
    private String osVolHostAttrHost;

    @JsonProperty("source_volid")
    private String sourceVolid;

    @JsonProperty("os-vol-mig-status-attr:name_id")
    private String oVolMigStatusAttrNameId;

    @JsonProperty("os-vol-mig-status-attr:migstat")
    private String oVolMigStatusAttrMigstat;

    @JsonProperty("imageRef")
    private String imageRef;


    private List<JsonAttachment> attachments;

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
     * @return the displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName the displayName to set
     */
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the displayDescription
     */
    public String getDisplayDescription() {
        return displayDescription;
    }

    /**
     * @param displayDescription the displayDescription to set
     */
    public void setDisplayDescription(final String displayDescription) {
        this.displayDescription = displayDescription;
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
     * @return the metadata
     */
    public JsonMetadata getMetadata() {
        return metadata;
    }

    /**
     * @param metadata the metadata to set
     */
    public void setMetadata(final JsonMetadata metadata) {
        this.metadata = metadata;
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

    /**
     * @return the createdAt
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt the createdAt to set
     */
    public void setCreatedAt(final String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return the attachments
     */
    public List<JsonAttachment> getAttachments() {
        return attachments;
    }

    /**
     * @param attachments the attachments to set
     */
    public void setAttachments(final List<JsonAttachment> attachments) {
        this.attachments = attachments;
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
     * @return the bootable
     */
    public boolean isBootable() {
        return bootable;
    }

    /**
     * @param bootable the bootable to set
     */
    public void setBootable(final boolean bootable) {
        this.bootable = bootable;
    }

    /**
     * @return the osVolTenantAttrTenantId
     */
    public String getOsVolTenantAttrTenantId() {
        return osVolTenantAttrTenantId;
    }

    /**
     * @param osVolTenantAttrTenantId the osVolTenantAttrTenantId to set
     */
    public void setOsVolTenantAttrTenantId(final String osVolTenantAttrTenantId) {
        this.osVolTenantAttrTenantId = osVolTenantAttrTenantId;
    }

    /**
     * @return the osVolHostAttrHost
     */
    public String getOsVolHostAttrHost() {
        return osVolHostAttrHost;
    }

    /**
     * @param osVolHostAttrHost the osVolHostAttrHost to set
     */
    public void setOsVolHostAttrHost(final String osVolHostAttrHost) {
        this.osVolHostAttrHost = osVolHostAttrHost;
    }

    /**
     * @return the sourceVolid
     */
    public String getSourceVolid() {
        return sourceVolid;
    }

    /**
     * @param sourceVolid the sourceVolid to set
     */
    public void setSourceVolid(final String sourceVolid) {
        this.sourceVolid = sourceVolid;
    }

    /**
     * @return the oVolMigStatusAttrNameId
     */
    public String getoVolMigStatusAttrNameId() {
        return oVolMigStatusAttrNameId;
    }

    /**
     * @param oVolMigStatusAttrNameId the oVolMigStatusAttrNameId to set
     */
    public void setoVolMigStatusAttrNameId(final String oVolMigStatusAttrNameId) {
        this.oVolMigStatusAttrNameId = oVolMigStatusAttrNameId;
    }

    /**
     * @return the oVolMigStatusAttrMigstat
     */
    public String getoVolMigStatusAttrMigstat() {
        return oVolMigStatusAttrMigstat;
    }

    /**
     * @param oVolMigStatusAttrMigstat the oVolMigStatusAttrMigstat to set
     */
    public void setoVolMigStatusAttrMigstat(final String oVolMigStatusAttrMigstat) {
        this.oVolMigStatusAttrMigstat = oVolMigStatusAttrMigstat;
    }

    /**
     * @return the imageRef
     */
    public String getImageRef() {
        return imageRef;
    }

    /**
     * @param imageRef the imageRef to set
     */
    public void setImageRef(final String imageRef) {
        this.imageRef = imageRef;
    }


}
