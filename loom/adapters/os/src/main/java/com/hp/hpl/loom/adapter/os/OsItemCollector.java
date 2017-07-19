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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.hp.hpl.loom.adapter.AggregationUpdaterBasedItemCollector;
import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.exceptions.InvalidActionSpecificationException;
import com.hp.hpl.loom.manager.adapter.AdapterManager;
import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionParameters;
import com.hp.hpl.loom.model.ActionResult;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.SeparableItem;
import com.hp.hpl.loom.model.Session;

public abstract class OsItemCollector extends AggregationUpdaterBasedItemCollector {

    protected Credentials creds;

    protected final List<String> instanceAllowedActions = Arrays.asList("start", "stop", "softReboot", "hardReboot");
    protected Collection<String> collectList;
    // always end update with region
    protected Collection<String> updaterList;

    public OsItemCollector(final Session session, final BaseAdapter adapter, final AdapterManager adapterManager,
            final Credentials creds) {
        super(session, adapter, adapterManager);
        // creds can be used to create project aggregation ?
        this.creds = creds;
    }

    @Override
    protected Collection<String> getUpdateItemTypeIdList() {
        if (updaterList == null) {
            updaterList = createUpdateItemTypeIdList();
        }
        return updaterList;
    }

    protected Collection<String> createUpdateItemTypeIdList() {
        // lists need to be ArrayList to be mutable
        // always end update with region
        return new ArrayList<String>(Arrays.asList(OsProjectType.TYPE_LOCAL_ID, OsImageType.TYPE_LOCAL_ID,
                OsInstanceType.TYPE_LOCAL_ID, OsVolumeType.TYPE_LOCAL_ID, OsSubnetType.TYPE_LOCAL_ID,
                OsNetworkType.TYPE_LOCAL_ID, OsRegionType.TYPE_LOCAL_ID));
    }

    @Override
    protected Collection<String> getCollectionItemTypeIdList() {
        if (collectList == null) {
            collectList = createCollectionItemTypeIdList();
        }
        return collectList;
    }

    protected Collection<String> createCollectionItemTypeIdList() {
        // lists need to be ArrayList to be mutable
        // always start collection with Projects since other types iterate over projects
        // region must come second
        return new ArrayList<String>(Arrays.asList(OsProjectType.TYPE_LOCAL_ID, OsRegionType.TYPE_LOCAL_ID,
                OsImageType.TYPE_LOCAL_ID, OsInstanceType.TYPE_LOCAL_ID, OsVolumeType.TYPE_LOCAL_ID,
                OsNetworkType.TYPE_LOCAL_ID, OsSubnetType.TYPE_LOCAL_ID));
        // return new ArrayList<String>(Arrays.asList(OsProjectType.TYPE_LOCAL_ID,
        // OsImageType.TYPE_LOCAL_ID,
        // OsInstanceType.TYPE_LOCAL_ID, OsVolumeType.TYPE_LOCAL_ID, OsSubnetType.TYPE_LOCAL_ID,
        // OsNetworkType.TYPE_LOCAL_ID, OsRegionType.TYPE_LOCAL_ID));
    }

    public ArrayList<? extends SeparableItem> getProjects() {
        return updaterMap.get(OsProjectType.TYPE_LOCAL_ID).getItems();
    }

    public ArrayList<? extends SeparableItem> getNetworks() {
        return updaterMap.get(OsNetworkType.TYPE_LOCAL_ID).getItems();
    }

    public Credentials getCredentials() {
        return creds;
    }

    public OsImage getImage(final String osId) {
        return (OsImage) updaterMap.get(OsImageType.TYPE_LOCAL_ID).getItem(osId);
    }

    public OsInstance getInstance(final String osId) {
        return (OsInstance) updaterMap.get(OsInstanceType.TYPE_LOCAL_ID).getItem(osId);
    }

    public OsSubnet getSubnet(final String osId) {
        return (OsSubnet) updaterMap.get(OsSubnetType.TYPE_LOCAL_ID).getItem(osId);
    }

    public OsRegion getRegion(final String osId) {
        return (OsRegion) updaterMap.get(OsRegionType.TYPE_LOCAL_ID).getItem(osId);
    }

    public OsProject getProject(final String osId) {
        return (OsProject) updaterMap.get(OsProjectType.TYPE_LOCAL_ID).getItem(osId);
    }

    @Override
    protected ActionResult doAction(final Action action, final String itemTypeId, final Collection<Item> items)
            throws InvalidActionSpecificationException {
        // for now, actions are only supported for instance
        if (!OsInstanceType.TYPE_LOCAL_ID.equals(itemTypeId)) {
            throw new InvalidActionSpecificationException("no action supported for typeId " + itemTypeId);
        }
        ActionParameters ap = action.getParams();
        if (ap == null || ap.isEmpty()) {
            throw new InvalidActionSpecificationException("no actionParameters", action);
        }
        String actionValue = ap.get(0).getValue();
        if (actionValue == null) {
            throw new InvalidActionSpecificationException("no actionValue", action);
        }
        if (!instanceAllowedActions.contains(actionValue)) {
            throw new InvalidActionSpecificationException("action not explicitly allowed for typeId " + itemTypeId,
                    action);
        }
        return doScopedAction(actionValue, items);
    }

    protected abstract ActionResult doScopedAction(String actionValue, Collection<Item> items);

}
