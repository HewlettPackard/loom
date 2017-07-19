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
package com.hp.hpl.loom.adapter.os.swift;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.adapter.os.OsAggregationUpdater;
import com.hp.hpl.loom.adapter.os.OsProjectType;
import com.hp.hpl.loom.adapter.os.OsRegionType;
import com.hp.hpl.loom.adapter.os.real.Constants;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;
import com.hp.hpl.loom.openstack.NoSupportedApiVersion;
import com.hp.hpl.loom.openstack.swift.model.JsonAccount;

public class RealAccountsUpdater extends OsAggregationUpdater<OsAccount, OsAccountAttributes, JsonAccount> {
    private static final Log LOG = LogFactory.getLog(RealAccountsUpdater.class);

    protected SwiftRealItemCollector sric;

    public RealAccountsUpdater(final Aggregation aggregation, final BaseAdapter adapter, final String itemTypeLocalId,
            final SwiftRealItemCollector sric) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemTypeLocalId, sric);
        this.sric = sric;
    }

    @Override
    protected Iterator<? extends JsonAccount> getResources(final int prjIdx, final int regIdx) {
        ArrayList<JsonAccount> accs = new ArrayList<>(1);

        String regionId = regions[regIdx];
        String projectId = projects.get(prjIdx).getCore().getItemId();
        String objectStoreVersion = adapter.getAdapterConfig().getPropertiesConfiguration()
                .getString(Constants.OBJECTSTORE + Constants.VERSION_SUFFIX);

        String[] objectStoreVersions = {};
        if (objectStoreVersion != null) {
            objectStoreVersions = objectStoreVersion.split(",");
        }

        JsonAccount jsonAccount;
        try {
            jsonAccount = sric.getOpenstackApi().getSwiftApi(objectStoreVersions, projectId, regionId)
                    .getSwiftAccounts().getJsonResourcesWithContainer();
        } catch (NoSupportedApiVersion e) {
            throw new RuntimeException("Problem accessing swift accounts for version: " + objectStoreVersion
                    + " projectId: " + projectId + " regionId: " + regionId);
        }
        sric.setAccount(projectId, regionId, jsonAccount);
        accs.add(jsonAccount);
        return accs.iterator();
    }

    @Override
    protected String[] getConfiguredRegions(final String projectName) {
        String projectId = getProjectId(projectName);
        return sric.getOpenstackApi().getTokenManager().getTokenHolder().getRegions(projectId, Constants.OBJECTSTORE,
                Constants.PUBLIC);
    }

    private String getRegionalAccountId() {
        return currentProject.getCore().getItemId() + "-" + currentRegion;
        /*
         * String[] regs = getConfiguredRegions(currentProject.getName()); if (regs.length > 1) {
         * return currentProject.getCore().getItemId() + "-" + currentRegion; } else { return
         * currentProject.getCore().getItemId(); }
         */
    }

    @Override
    protected String getItemId(final JsonAccount resource) {
        return getRegionalAccountId();
    }

    @Override
    protected OsAccount createEmptyItem(final String logicalId) {
        return new OsAccount(logicalId, itemType);
    }

    @Override
    protected OsAccountAttributes createItemAttributes(final JsonAccount account) {
        OsAccountAttributes oaa = new OsAccountAttributes();
        oaa.setItemName(currentProject.getName() + "(" + currentRegion + ")");
        oaa.setItemId(getItemId(account));
        oaa.setBytesUsed(account.getBytesUsed());
        oaa.setContainerCount(account.getContainerCount());
        oaa.setObjectCount(account.getObjectCount());
        return oaa;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final OsAccountAttributes oaa, final JsonAccount account) {
        if (oaa.getBytesUsed() != account.getBytesUsed() || oaa.getContainerCount() != account.getContainerCount()
                || oaa.getObjectCount() != account.getObjectCount()) {
            return ChangeStatus.CHANGED_UPDATE;
        } else {
            return ChangeStatus.UNCHANGED;
        }
    }

    @Override
    protected void setRelationships(final ConnectedItem osAccount, final JsonAccount account) {
        // region relationship
        osAccount.setRelationship(adapter.getProvider(), OsRegionType.TYPE_LOCAL_ID, currentRegion);

        // project relationship
        osAccount.setRelationship(adapter.getProvider(), OsProjectType.TYPE_LOCAL_ID,
                currentProject.getCore().getItemId());
    }

}
