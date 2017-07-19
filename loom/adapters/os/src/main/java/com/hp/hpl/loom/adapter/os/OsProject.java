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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hp.hpl.loom.adapter.annotations.ConnectedTo;
import com.hp.hpl.loom.adapter.annotations.ItemTypeInfo;
import com.hp.hpl.loom.adapter.annotations.Root;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.relationships.RelationshipUtil;

@Root
@ItemTypeInfo(OsProjectType.TYPE_LOCAL_ID)
@ConnectedTo(toClass = OsSubnet.class)
@ConnectedTo(toClass = OsInstance.class)
@ConnectedTo(toClass = OsNetwork.class)
@ConnectedTo(toClass = OsImage.class)
@ConnectedTo(toClass = OsVolume.class)
@ConnectedTo(toClass = OsRegion.class)
public class OsProject extends OsItem<OsProjectAttributes> {

    private float coresQuotaUtilisation;
    private float instancesQuotaUtilisation;
    private float injectedFilesQuotaUtilisation;
    private float volumesQuotaUtilisation;
    private float snapshotsQuotaUtilisation;
    private float gigabytesQuotaUtilisation;
    private float ramQuotaUtilisation;
    private float securityGroupsQuotaUtilisation;
    private float securityGroupRulesQuotaUtilisation;
    private float floatingIpsQuotaUtilisation;


    private OsProject() {
        super();
    }

    public OsProject(final String logicalId, final ItemType projectType) {
        super(logicalId, projectType);
    }


    @JsonIgnore
    @Override
    public String getQualifiedName() {
        return "/" + getName();
    }


    @JsonIgnore
    public Collection<Item> getRegions(final Provider provider) {
        String thisToRegionRelName = RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(provider.getProviderType(),
                getItemType().getLocalId(), OsRegionType.TYPE_LOCAL_ID);
        return this.getConnectedItemsWithRelationshipName(thisToRegionRelName);
    }

    private float roundTwoDecimalPlaces(final float number) {
        return (float) (Math.round(number * 100.0) / 100.0);
    }

    @Override
    @JsonIgnore
    public boolean update() {
        boolean superUpdate = super.update();
        boolean anyChange = false;
        // Update VCPUs quota utilisation
        int usage = 0;
        Collection<Item> prjInstCollection =
                getConnectedItemsWithRelationshipName(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                        getProviderType(), OsInstanceType.TYPE_LOCAL_ID, OsProjectType.TYPE_LOCAL_ID));

        List<Item> projectInstances = new ArrayList<>();
        if (prjInstCollection != null) {
            projectInstances = new ArrayList<>(prjInstCollection);
        }
        Collection<Item> prjVolCollection =
                getConnectedItemsWithRelationshipName(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                        getProviderType(), OsVolumeType.TYPE_LOCAL_ID, OsProjectType.TYPE_LOCAL_ID));

        List<Item> projectVolumes = new ArrayList<>();
        if (prjVolCollection != null) {
            projectVolumes = new ArrayList<>(prjVolCollection);
        }

        for (Item instance : projectInstances) {
            usage += ((OsInstance) instance).getCore().getVcpus();
        }

        int quota = getCore().getCoresQuota();
        float oldUsage = this.getCoresQuotaUtilisation();
        if (quota != 0) {
            setCoresQuotaUtilisation(roundTwoDecimalPlaces(((float) usage / quota) * 100));
        } else {
            setCoresQuotaUtilisation(100);
        }

        anyChange = oldUsage != getCoresQuotaUtilisation();

        // Update instances quota utilisation
        quota = getCore().getInstancesQuota();
        float oldInstances = this.getInstancesQuotaUtilisation();
        if (quota != 0) {
            setInstancesQuotaUtilisation(roundTwoDecimalPlaces(((float) projectInstances.size() / quota) * 100));
        } else {
            setInstancesQuotaUtilisation(100);
        }

        anyChange = anyChange || oldInstances != getInstancesQuotaUtilisation();

        // Update injected files quota utilisation
        // TODO: not sure how to calculate this.

        // Update RAM quota utilisation
        usage = 0;

        for (Item instance : projectInstances) {
            usage += ((OsInstance) instance).getCore().getRam();
        }

        quota = getCore().getRamQuota();
        float oldRam = this.getRamQuotaUtilisation();
        if (quota != 0) {
            setRamQuotaUtilisation(roundTwoDecimalPlaces(((float) usage / quota) * 100));
        } else {
            setRamQuotaUtilisation(100);
        }
        anyChange = anyChange || oldRam != getRamQuotaUtilisation();

        // Update volumes quota utilisation
        quota = getCore().getVolumesQuota();
        float oldVolumes = this.getVolumesQuotaUtilisation();
        if (quota != 0) {
            setVolumesQuotaUtilisation(roundTwoDecimalPlaces(((float) projectVolumes.size() / quota) * 100));
        } else {
            setVolumesQuotaUtilisation(100);
        }

        anyChange = anyChange || oldVolumes != getVolumesQuotaUtilisation();

        // Update gigabytes quota utilisation
        usage = 0;

        for (Item volume : projectVolumes) {
            usage += ((OsVolume) volume).getCore().getSize();
        }

        quota = getCore().getGigabytesQuota();
        float oldGiga = this.getGigabytesQuotaUtilisation();
        if (quota != 0) {
            setGigabytesQuotaUtilisation(roundTwoDecimalPlaces(((float) usage / quota) * 100));
        } else {
            setGigabytesQuotaUtilisation(100);
        }

        anyChange = anyChange || oldGiga != getGigabytesQuotaUtilisation();

        // Update snapshot quota utilisation
        // TODO: not sure how to calculate this

        // Update security group quota utilisation
        // TODO: not sure how to calculate this

        // Update security group rules quota utilisation
        // TODO: not sure how to calculate this

        // Update floating IPs quota utilisation
        // TODO: not sure how to calculate this
        return superUpdate | anyChange;
    }

    /**
     * @return the coresQuotaUtilisation
     */
    public float getCoresQuotaUtilisation() {
        return coresQuotaUtilisation;
    }

    /**
     * @param coresQuotaUtilisation the coresQuotaUtilisation to set
     */
    public void setCoresQuotaUtilisation(final float coresQuotaUtilisation) {
        this.coresQuotaUtilisation = coresQuotaUtilisation;
    }

    /**
     * @return the instancesQuotaUtilisation
     */
    public float getInstancesQuotaUtilisation() {
        return instancesQuotaUtilisation;
    }

    /**
     * @param instancesQuotaUtilisation the instancesQuotaUtilisation to set
     */
    public void setInstancesQuotaUtilisation(final float instancesQuotaUtilisation) {
        this.instancesQuotaUtilisation = instancesQuotaUtilisation;
    }

    /**
     * @return the injectedFilesQuotaUtilisation
     */
    public float getInjectedFilesQuotaUtilisation() {
        return injectedFilesQuotaUtilisation;
    }

    /**
     * @param injectedFilesQuotaUtilisation the injectedFilesQuotaUtilisation to set
     */
    public void setInjectedFilesQuotaUtilisation(final float injectedFilesQuotaUtilisation) {
        this.injectedFilesQuotaUtilisation = injectedFilesQuotaUtilisation;
    }

    /**
     * @return the volumesQuotaUtilisation
     */
    public float getVolumesQuotaUtilisation() {
        return volumesQuotaUtilisation;
    }

    /**
     * @param volumesQuotaUtilisation the volumesQuotaUtilisation to set
     */
    public void setVolumesQuotaUtilisation(final float volumesQuotaUtilisation) {
        this.volumesQuotaUtilisation = volumesQuotaUtilisation;
    }

    /**
     * @return the snapshotsQuotaUtilisation
     */
    public float getSnapshotsQuotaUtilisation() {
        return snapshotsQuotaUtilisation;
    }

    /**
     * @param snapshotsQuotaUtilisation the snapshotsQuotaUtilisation to set
     */
    public void setSnapshotsQuotaUtilisation(final float snapshotsQuotaUtilisation) {
        this.snapshotsQuotaUtilisation = snapshotsQuotaUtilisation;
    }

    /**
     * @return the gigabytesQuotaUtilisation
     */
    public float getGigabytesQuotaUtilisation() {
        return gigabytesQuotaUtilisation;
    }

    /**
     * @param gigabytesQuotaUtilisation the gigabytesQuotaUtilisation to set
     */
    public void setGigabytesQuotaUtilisation(final float gigabytesQuotaUtilisation) {
        this.gigabytesQuotaUtilisation = gigabytesQuotaUtilisation;
    }

    /**
     * @return the ramQuotaUtilisation
     */
    public float getRamQuotaUtilisation() {
        return ramQuotaUtilisation;
    }

    /**
     * @param ramQuotaUtilisation the ramQuotaUtilisation to set
     */
    public void setRamQuotaUtilisation(final float ramQuotaUtilisation) {
        this.ramQuotaUtilisation = ramQuotaUtilisation;
    }

    /**
     * @return the securityGroupsQuotaUtilisation
     */
    public float getSecurityGroupsQuotaUtilisation() {
        return securityGroupsQuotaUtilisation;
    }

    /**
     * @param securityGroupsQuotaUtilisation the securityGroupsQuotaUtilisation to set
     */
    public void setSecurityGroupsQuotaUtilisation(final float securityGroupsQuotaUtilisation) {
        this.securityGroupsQuotaUtilisation = securityGroupsQuotaUtilisation;
    }

    /**
     * @return the securityGroupRulesQuotaUtilisation
     */
    public float getSecurityGroupRulesQuotaUtilisation() {
        return securityGroupRulesQuotaUtilisation;
    }

    /**
     * @param securityGroupRulesQuotaUtilisation the securityGroupRulesQuotaUtilisation to set
     */
    public void setSecurityGroupRulesQuotaUtilisation(final float securityGroupRulesQuotaUtilisation) {
        this.securityGroupRulesQuotaUtilisation = securityGroupRulesQuotaUtilisation;
    }

    /**
     * @return the floatingIpsQuotaUtilisation
     */
    public float getFloatingIpsQuotaUtilisation() {
        return floatingIpsQuotaUtilisation;
    }

    /**
     * @param floatingIpsQuotaUtilisation the floatingIpsQuotaUtilisation to set
     */
    public void setFloatingIpsQuotaUtilisation(final float floatingIpsQuotaUtilisation) {
        this.floatingIpsQuotaUtilisation = floatingIpsQuotaUtilisation;
    }

}
