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

import org.apache.log4j.Logger;

import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.adapter.hpcloud.HpCloudAdapter;
import com.hp.hpl.loom.adapter.hpcloud.HpCloudCollector;
import com.hp.hpl.loom.adapter.hpcloud.item.HostItem;
import com.hp.hpl.loom.adapter.hpcloud.item.HostItemAttributes;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.HostType;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;

// public abstract class ProjectsUpdater<R> extends AggregationUpdater<OsProject,
// OsProjectAttributes, R>
public class HostUpdater extends HpCloudCommonUpdater<HostItem, HostItemAttributes> {
    // OsWorkload, OsWorkloadAttributes, FakeWorkload>
    @SuppressWarnings("unused")
    private static Logger log = Logger.getLogger(HostUpdater.class);

    // CONSTRUCTORS -----------------------------------------------------------

    public HostUpdater(final Aggregation aggregation, final HpCloudAdapter adapter, final HpCloudCollector collector)
            throws NoSuchItemTypeException, UnknownHostException {
        super(aggregation, adapter, collector, HostType.TYPE_LOCAL_ID);
    }

    // CONSTRUCTORS - END -----------------------------------------------------

    // METHODS ----------------------------------------------------------------

    @Override
    protected HostItem createEmptyItem(final String logicalId) {
        return new HostItem(logicalId, HpCloudAdapter.hostType);
    }

    @Override
    protected String getItemName() {
        return "host";
    }

    @Override
    protected String[] getPayloadFields() {
        return HostType.PAYLOAD_FIELDS;
    }

    @Override
    protected void setRelationships(final HostItem item) {
        // TODO Auto-generated method stub

    }

    @Override
    protected HostItemAttributes createItemAttributes(final String resource) {
        HostItemAttributes hostItemAttributes = new HostItemAttributes();
        return super.createItemAttributes(hostItemAttributes, resource);
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final HostItemAttributes itemAttr, final String resource) {
        HostItemAttributes otherAttr = new HostItemAttributes();
        super.createItemAttributes(otherAttr, resource);

        // attr checks
        if (itemAttr.getItemName() != null && !itemAttr.getItemName().equals(otherAttr.getItemName())) {
            return ChangeStatus.CHANGED_UPDATE;
        }
        return ChangeStatus.UNCHANGED;
    }

    @Override
    protected void setRelationships(final ConnectedItem item, final String resource) {
        // TODO Auto-generated method stub

    }

    // METHODS - END ----------------------------------------------------------

}
