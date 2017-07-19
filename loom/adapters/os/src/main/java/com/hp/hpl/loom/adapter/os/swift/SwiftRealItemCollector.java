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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.AggregationUpdater;
import com.hp.hpl.loom.adapter.os.OsProjectType;
import com.hp.hpl.loom.adapter.os.OsRegionType;
import com.hp.hpl.loom.adapter.os.real.RealItemCollector;
import com.hp.hpl.loom.adapter.os.real.RealRegionsUpdater;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.manager.adapter.AdapterManager;
import com.hp.hpl.loom.model.ActionResult;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.SeparableItem;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.openstack.swift.model.JsonAccount;

public class SwiftRealItemCollector extends RealItemCollector {
    private static final Log LOG = LogFactory.getLog(SwiftRealItemCollector.class);

    public final String SWIFT_ZONES_KEY = "swift";

    protected boolean swiftOnly;

    private Map<String, JsonAccount> accounts = new HashMap<>();

    public SwiftRealItemCollector(final Session session, final SwiftRealAdapter adapter,
            final AdapterManager adapterManager, final Credentials creds) {
        super(session, adapter, adapterManager, creds);
    }

    @Override
    protected void createAggregations() {
        swiftOnly = ((SwiftRealAdapter) adapter).getSwiftOnly();
        super.createAggregations();
    }

    public void setAccount(final String projectId, final String regionId, final JsonAccount account) {
        accounts.put(projectId + "-" + regionId, account);
    }

    public JsonAccount getAccount(final String projectId, final String regionId) {
        return accounts.get(projectId + "-" + regionId);
    }

    public ArrayList<? extends SeparableItem> getAccounts() {
        return updaterMap.get(OsAccountType.TYPE_LOCAL_ID).getItems();
    }

    public ArrayList<? extends SeparableItem> getContainers() {
        return updaterMap.get(OsContainerType.TYPE_LOCAL_ID).getItems();
    }

    public ArrayList<? extends SeparableItem> getRegions() {
        return updaterMap.get(OsRegionType.TYPE_LOCAL_ID).getItems();
    }

    @Override
    protected Collection<String> createUpdateItemTypeIdList() {
        Collection<String> superList;
        if (swiftOnly) {
            superList = new ArrayList<>(5);
            superList.add(OsProjectType.TYPE_LOCAL_ID);
        } else {
            superList = super.createUpdateItemTypeIdList();
            superList.remove(OsRegionType.TYPE_LOCAL_ID);
        }
        superList.add(OsAccountType.TYPE_LOCAL_ID);
        superList.add(OsContainerType.TYPE_LOCAL_ID);
        superList.add(OsObjectType.TYPE_LOCAL_ID);
        superList.add(OsRegionType.TYPE_LOCAL_ID);
        return superList;
    }

    @Override
    protected Collection<String> createCollectionItemTypeIdList() {
        Collection<String> superList;
        if (swiftOnly) {
            superList = new ArrayList<>(5);
            superList.add(OsProjectType.TYPE_LOCAL_ID);
            superList.add(OsRegionType.TYPE_LOCAL_ID);
        } else {
            superList = super.createCollectionItemTypeIdList();
        }
        superList.add(OsAccountType.TYPE_LOCAL_ID);
        superList.add(OsContainerType.TYPE_LOCAL_ID);
        superList.add(OsObjectType.TYPE_LOCAL_ID);
        return superList;
    }

    @Override
    protected AggregationUpdater<?, ?, ?> getAggregationUpdater(final Aggregation agg)
            throws NoSuchProviderException, NoSuchItemTypeException {

        AggregationUpdater<?, ?, ?> aggUp = null;

        try {
            if (aggregationMatchesItemType(agg, OsAccountType.TYPE_LOCAL_ID)) {
                aggUp = new RealAccountsUpdater(agg, adapter, OsAccountType.TYPE_LOCAL_ID, this);
            } else if (aggregationMatchesItemType(agg, OsContainerType.TYPE_LOCAL_ID)) {
                aggUp = new RealContainersUpdater(agg, adapter, OsContainerType.TYPE_LOCAL_ID, this);
            } else if (aggregationMatchesItemType(agg, OsObjectType.TYPE_LOCAL_ID)) {
                aggUp = new RealObjectsUpdater(agg, adapter, OsObjectType.TYPE_LOCAL_ID, this);
            } else if (aggregationMatchesItemType(agg, OsRegionType.TYPE_LOCAL_ID)) {
                aggUp = new RealRegionsUpdater(agg, adapter, OsRegionType.TYPE_LOCAL_ID, this);
            } else {
                aggUp = super.getAggregationUpdater(agg);
            }
        } catch (RuntimeException ex) {
            LOG.error("exception while creating an updater", ex);
            throw new NoSuchProviderException("adapter has gone");
        }

        return aggUp;
    }

    @Override
    protected ActionResult doScopedAction(final String actionValue, final Collection<Item> items) {
        return new ActionResult(ActionResult.Status.aborted);
    }

}
