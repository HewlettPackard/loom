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
package com.hp.hpl.loom.adapter.hpcloud;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.hp.hpl.loom.adapter.AggregationUpdater;
import com.hp.hpl.loom.adapter.AggregationUpdaterBasedItemCollector;
import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.hpcloud.item.ChefClientItem;
import com.hp.hpl.loom.adapter.hpcloud.item.ChefEnvironmentItem;
import com.hp.hpl.loom.adapter.hpcloud.item.ChefNodeItem;
import com.hp.hpl.loom.adapter.hpcloud.item.ChefOrgItem;
import com.hp.hpl.loom.adapter.hpcloud.item.ChefUploadedCookbookItem;
import com.hp.hpl.loom.adapter.hpcloud.item.ChefUserItem;
import com.hp.hpl.loom.adapter.hpcloud.item.HostItem;
import com.hp.hpl.loom.adapter.hpcloud.item.VmItem;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.ChefClientType;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.ChefEnvironmentType;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.ChefNodeType;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.ChefOrgType;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.ChefUploadedCookbookType;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.ChefUserType;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.HostType;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.VmType;
import com.hp.hpl.loom.adapter.hpcloud.updater.ChefClientUpdater;
import com.hp.hpl.loom.adapter.hpcloud.updater.ChefEnvironmentUpdater;
import com.hp.hpl.loom.adapter.hpcloud.updater.ChefNodeUpdater;
import com.hp.hpl.loom.adapter.hpcloud.updater.ChefOrgUpdater;
import com.hp.hpl.loom.adapter.hpcloud.updater.ChefUploadedCookbookUpdater;
import com.hp.hpl.loom.adapter.hpcloud.updater.ChefUserUpdater;
import com.hp.hpl.loom.adapter.hpcloud.updater.HostUpdater;
import com.hp.hpl.loom.adapter.hpcloud.updater.VmUpdater;
import com.hp.hpl.loom.exceptions.InvalidActionSpecificationException;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.manager.adapter.AdapterManager;
import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionResult;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.Session;

public class HpCloudCollector extends AggregationUpdaterBasedItemCollector {

    @SuppressWarnings("unused")
    private static Logger log = Logger.getLogger(HpCloudCollector.class);

    public HpCloudCollector(final Session session, final BaseAdapter adapter, final AdapterManager adapterManager) {
        super(session, adapter, adapterManager);

        if (log.isDebugEnabled()) {
            log.debug("Saul: Creating HpCloudCollector");
        }
    }

    @Override
    protected Collection<String> getUpdateItemTypeIdList() {
        List<String> list = new ArrayList<String>();
        list.add(HostType.TYPE_LOCAL_ID);
        list.add(VmType.TYPE_LOCAL_ID);
        list.add(ChefClientType.TYPE_LOCAL_ID);
        list.add(ChefEnvironmentType.TYPE_LOCAL_ID);
        list.add(ChefNodeType.TYPE_LOCAL_ID);
        list.add(ChefOrgType.TYPE_LOCAL_ID);
        list.add(ChefUploadedCookbookType.TYPE_LOCAL_ID);
        list.add(ChefUserType.TYPE_LOCAL_ID);
        return list;
    }

    @Override
    protected Collection<String> getCollectionItemTypeIdList() {
        List<String> list = new ArrayList<String>();
        list.add(HostType.TYPE_LOCAL_ID);
        list.add(VmType.TYPE_LOCAL_ID);
        list.add(ChefClientType.TYPE_LOCAL_ID);
        list.add(ChefEnvironmentType.TYPE_LOCAL_ID);
        list.add(ChefNodeType.TYPE_LOCAL_ID);
        list.add(ChefOrgType.TYPE_LOCAL_ID);
        list.add(ChefUploadedCookbookType.TYPE_LOCAL_ID);
        list.add(ChefUserType.TYPE_LOCAL_ID);
        return list;
    }

    @Override
    protected ActionResult doAction(final Action action, final String itemTypeId, final Collection<Item> items)
            throws InvalidActionSpecificationException {
        return new ActionResult(ActionResult.Status.completed);
    }

    public HostItem getHostItem(final String hostId) {
        return (HostItem) updaterMap.get(HostType.TYPE_LOCAL_ID).getItem(hostId);
    }

    public VmItem getVmItem(final String vmId) {
        return (VmItem) updaterMap.get(VmType.TYPE_LOCAL_ID).getItem(vmId);
    }

    public ChefClientItem getChefClientItem(final String chefClientId) {
        return (ChefClientItem) updaterMap.get(ChefClientType.TYPE_LOCAL_ID).getItem(chefClientId);
    }

    public ChefEnvironmentItem getChefEnvironmentItem(final String chefEnvironmentId) {
        return (ChefEnvironmentItem) updaterMap.get(ChefEnvironmentType.TYPE_LOCAL_ID).getItem(chefEnvironmentId);
    }

    public ChefNodeItem getChefNodeItem(final String chefNodeId) {
        return (ChefNodeItem) updaterMap.get(ChefNodeType.TYPE_LOCAL_ID).getItem(chefNodeId);
    }

    public ChefOrgItem getChefOrgItem(final String chefOrgId) {
        return (ChefOrgItem) updaterMap.get(ChefOrgType.TYPE_LOCAL_ID).getItem(chefOrgId);
    }

    public ChefUploadedCookbookItem getChefUploadedCookbookItem(final String chefUploadedCookbookId) {
        return (ChefUploadedCookbookItem) updaterMap.get(ChefUploadedCookbookType.TYPE_LOCAL_ID)
                .getItem(chefUploadedCookbookId);
    }

    public ChefUserItem getChefUserItem(final String chefUserId) {
        return (ChefUserItem) updaterMap.get(ChefUserType.TYPE_LOCAL_ID).getItem(chefUserId);
    }

    // @Override
    // protected AggregationUpdater<? extends AdapterItem<?>, ? extends CoreItemAttributes, ?>
    // getAggregationUpdater(
    // Aggregation aggregation) throws NoSuchProviderException, NoSuchItemTypeException {
    // // TODO Auto-generated method stub
    // return null;
    // }
    // @Override
    // protected AggregationUpdater<?, ?, ?> getAggregationUpdater(final Aggregation aggregation)
    // throws NoSuchProviderException, NoSuchItemTypeException {
    //
    // String typeId = aggregation.getTypeId();
    // ItemType itemType;
    //
    // itemType = adapter.getItemType(Types.FILE_TYPE_LOCAL_ID);
    // if (itemType.getId().equals(typeId)) {
    // return new FileSystemUpdater(aggregation, adapter, this);
    // } else if (aggregation.getTypeId().equals(Types.DIR_TYPE_LOCAL_ID)) {
    // return new DirSystemUpdater(aggregation, adapter, this);
    // }
    //
    // throw new NoSuchItemTypeException(aggregation.getTypeId());
    // }

    @Override
    protected AggregationUpdater<?, ?, ?> getAggregationUpdater(final Aggregation aggregation)
            throws NoSuchProviderException, NoSuchItemTypeException {
        if (log.isDebugEnabled()) {
            log.debug("Saul: At getAggregationUpdater (Creating updater for type " + aggregation.getTypeId() + ")");
        }

        try {
            if (aggregation.getTypeId().equals(HpCloudAdapter.hostType.getId())) {
                return new HostUpdater(aggregation, (HpCloudAdapter) adapter, this);
            } else if (aggregation.getTypeId().equals(HpCloudAdapter.vmType.getId())) {
                return new VmUpdater(aggregation, (HpCloudAdapter) adapter, this);
            } else if (aggregation.getTypeId().equals(HpCloudAdapter.chefClientType.getId())) {
                return new ChefClientUpdater(aggregation, (HpCloudAdapter) adapter, this);
            } else if (aggregation.getTypeId().equals(HpCloudAdapter.chefEnvironmentType.getId())) {
                return new ChefEnvironmentUpdater(aggregation, (HpCloudAdapter) adapter, this);
            } else if (aggregation.getTypeId().equals(HpCloudAdapter.chefNodeType.getId())) {
                return new ChefNodeUpdater(aggregation, (HpCloudAdapter) adapter, this);
            } else if (aggregation.getTypeId().equals(HpCloudAdapter.chefOrgType.getId())) {
                return new ChefOrgUpdater(aggregation, (HpCloudAdapter) adapter, this);
            } else if (aggregation.getTypeId().equals(HpCloudAdapter.chefUploadedCookbookType.getId())) {
                return new ChefUploadedCookbookUpdater(aggregation, (HpCloudAdapter) adapter, this);
            } else if (aggregation.getTypeId().equals(HpCloudAdapter.chefUserType.getId())) {
                return new ChefUserUpdater(aggregation, (HpCloudAdapter) adapter, this);
            } else {
                throw new NoSuchProviderException("Error retrieving aggregation type");
            }
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            throw new NoSuchProviderException("adapter has gone");
        } catch (UnknownHostException ex) {
            // Saul, TODO: Review. Should this be a RuntimeException or any other?
            throw new RuntimeException("Could not set a connection to the database");
        }
    }

}
