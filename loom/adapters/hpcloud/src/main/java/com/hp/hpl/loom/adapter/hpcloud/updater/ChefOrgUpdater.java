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
import com.hp.hpl.loom.adapter.hpcloud.item.ChefOrgItem;
import com.hp.hpl.loom.adapter.hpcloud.item.ChefOrgItemAttributes;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.ChefOrgType;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;

public class ChefOrgUpdater extends HpCloudCommonUpdater<ChefOrgItem, ChefOrgItemAttributes> {

    @SuppressWarnings("unused")
    private static Logger log = Logger.getLogger(ChefOrgUpdater.class);

    // CONSTRUCTORS -----------------------------------------------------------

    public ChefOrgUpdater(final Aggregation aggregation, final HpCloudAdapter adapter, final HpCloudCollector collector)
            throws NoSuchItemTypeException, UnknownHostException {
        super(aggregation, adapter, collector, ChefOrgType.TYPE_LOCAL_ID);
    }

    // CONSTRUCTORS - END -----------------------------------------------------

    // METHODS ----------------------------------------------------------------

    @Override
    protected ChefOrgItem createEmptyItem(final String logicalId) {
        return new ChefOrgItem(logicalId, HpCloudAdapter.chefOrgType);
    }

    @Override
    protected String getItemName() {
        return "chef_org";
    }

    @Override
    protected String[] getPayloadFields() {
        return ChefOrgType.PAYLOAD_FIELDS;
    }

    @Override
    protected void setRelationships(final ChefOrgItem item) {
        // In our current dataset, this method sets no relationships
    }

    @Override
    protected ChefOrgItemAttributes createItemAttributes(final String resource) {
        ChefOrgItemAttributes chefOrgItemAttributes = new ChefOrgItemAttributes();
        return super.createItemAttributes(chefOrgItemAttributes, resource);
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final ChefOrgItemAttributes itemAttr,
            final String resource) {
        return ChangeStatus.UNCHANGED;
    }

    @Override
    protected void setRelationships(final ConnectedItem item, final String resource) {
        // TODO Auto-generated method stub

    }


    // METHODS - END ----------------------------------------------------------

}
