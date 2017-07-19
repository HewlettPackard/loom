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
package com.hp.hpl.loom.adapter.hpcloud.updater;

import java.net.UnknownHostException;
import java.util.List;

import org.apache.log4j.Logger;

import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.adapter.hpcloud.HpCloudAdapter;
import com.hp.hpl.loom.adapter.hpcloud.HpCloudCollector;
import com.hp.hpl.loom.adapter.hpcloud.item.VmItem;
import com.hp.hpl.loom.adapter.hpcloud.item.VmItemAttributes;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.HostType;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.VmType;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;

public class VmUpdater extends HpCloudCommonUpdater<VmItem, VmItemAttributes> {

    @SuppressWarnings("unused")
    private static Logger log = Logger.getLogger(VmUpdater.class);

    // CONSTRUCTORS -----------------------------------------------------------

    public VmUpdater(final Aggregation aggregation, final HpCloudAdapter adapter, final HpCloudCollector collector)
            throws NoSuchItemTypeException, UnknownHostException {
        super(aggregation, adapter, collector, VmType.TYPE_LOCAL_ID);
    }

    // CONSTRUCTORS - END -----------------------------------------------------

    // METHODS ----------------------------------------------------------------

    @Override
    protected VmItem createEmptyItem(final String logicalId) {
        // Saul (10th Oct 2014), TODO: Review this method. Probably we should call the
        // "getVmItem(String)" method from the
        // collector class for the objects to be tracked. This way, if they exist, they will be
        // retrieved, and if the don't
        // they will be created.
        // NOTE: This will affect all the Updater classes!

        return new VmItem(logicalId, HpCloudAdapter.vmType);
    }

    @Override
    protected String getItemName() {
        return "vm";
    }

    @Override
    protected String[] getPayloadFields() {
        return VmType.PAYLOAD_FIELDS;
    }

    @Override
    protected VmItemAttributes createItemAttributes(final String resource) {
        VmItemAttributes vmItemAttributes = new VmItemAttributes();
        return super.createItemAttributes(vmItemAttributes, resource);
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final VmItemAttributes itemAttr, final String resource) {

        VmItemAttributes otherAttr = new VmItemAttributes();
        super.createItemAttributes(otherAttr, resource);

        // attr checks
        if (itemAttr.getItemName() != null && !itemAttr.getItemName().equals(otherAttr.getItemName())) {
            return ChangeStatus.CHANGED_UPDATE;
        }
        return ChangeStatus.UNCHANGED;

    }

    @Override
    protected void setRelationships(final ConnectedItem item, final String resource) {
        VmItem vmItem = (VmItem) item;
        if (log.isDebugEnabled()) {
            log.debug("Saul: Setting relationships for item " + vmItem.getCore().getItemId());
        }

        // Quick and dirty! The objects are going to be saved in this instance, although they look
        // like they should be kept somewhere else.
        List<String> hostList = dbConn.getAllIds("source.type", "host");

        String refHost = (String) vmItem.getCore().getPayload().getAttribute("ref_host");
        for (String hostId : hostList) {
            if (refHost.equals(dbConn.getAttribute(hostId, "record_id"))) {
                if (log.isDebugEnabled()) {
                    log.debug("Saul: Vm " + vmItem.getCore().getItemId() + " is connected to " + hostId);
                }
                // item.addConnectedRelationships(collector.getHostItem(hostId));
                item.setRelationship(adapter.getProvider(), HostType.TYPE_LOCAL_ID,
                        collector.getHostItem(hostId).getCore().getItemId());
            }
        }

        // Intended to be an enhanced version of the previous code (above)
        // As of Oct 10th 2014, this code doesn't work because the collector seems not to return
        // any Item. Most probably it is not tracking the objects that are created.
        // String ref_host = (String) item.getPayload().getAttribute("ref_host");
        // String hostId;
        // HostItem hostItem;
        // Iterator<String> hostList = getResourceIterator();
        // while (hostList.hasNext()) {
        // hostId = hostList.next();
        // hostItem = this.collector.getHostItem(hostId);
        // if (ref_host.equals(hostItem.getRecord_id())) {
        // if (log.isDebugEnabled()) {
        // log.debug("Saul: Vm " + item.get_id() + " is connected to " + hostId);
        // }
        // item.addConnectedRelationships(this.collector.getHostItem(hostId));
        // }
        // }
    }

    @Override
    protected void setRelationships(final VmItem item) {
        // TODO Auto-generated method stub

    }

    // METHODS - END ----------------------------------------------------------

}
