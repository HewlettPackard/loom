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
import com.hp.hpl.loom.adapter.hpcloud.item.ChefEnvironmentItem;
import com.hp.hpl.loom.adapter.hpcloud.item.ChefEnvironmentItemAttributes;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.ChefEnvironmentType;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;

public class ChefEnvironmentUpdater extends HpCloudCommonUpdater<ChefEnvironmentItem, ChefEnvironmentItemAttributes> {

    @SuppressWarnings("unused")
    private static Logger log = Logger.getLogger(ChefEnvironmentUpdater.class);

    // CONSTRUCTORS -----------------------------------------------------------

    public ChefEnvironmentUpdater(final Aggregation aggregation, final HpCloudAdapter adapter,
            final HpCloudCollector collector) throws NoSuchItemTypeException, UnknownHostException {
        super(aggregation, adapter, collector, ChefEnvironmentType.TYPE_LOCAL_ID);
    }

    // CONSTRUCTORS - END -----------------------------------------------------

    // METHODS ----------------------------------------------------------------

    @Override
    protected ChefEnvironmentItem createEmptyItem(final String logicalId) {
        return new ChefEnvironmentItem(logicalId, HpCloudAdapter.chefEnvironmentType);
    }

    @Override
    protected String getItemName() {
        return "chef_environment";
    }

    @Override
    protected String[] getPayloadFields() {
        return ChefEnvironmentType.PAYLOAD_FIELDS;
    }

    @Override
    protected void setRelationships(final ChefEnvironmentItem item) {
        // if (log.isDebugEnabled()) {
        // log.debug("Saul: Setting relationships for item " + item.get_id() +
        // " (type: " + getItemName() + ")");
        // }
        //
        // List<String> targetItemList;
        // String localAttributeToCheck, remoteAttributeToCheck, localAttribute;
        //
        // targetItemList = dbConn.getAllIds("source.type", "chef_org");
        // localAttributeToCheck = "chef_org_guid";
        // remoteAttributeToCheck = "payload.guid";
        //
        // localAttribute = (String) item.getPayload().getAttribute(localAttributeToCheck);
        // for (String chefOrgId : targetItemList) {
        // if (localAttribute.equals(dbConn.getAttribute(chefOrgId, remoteAttributeToCheck))) {
        // if (log.isDebugEnabled()) {
        // log.debug("Saul: " + item.get_id() + " is connected to " + chefOrgId +
        // " (local attribute: " + localAttributeToCheck +
        // "; remote attribute: " + remoteAttributeToCheck + ")");
        // }
        // item.addConnectedRelationships(this.collector.getChefOrgItem(chefOrgId));
        // }
        // }
    }

    @Override
    protected ChefEnvironmentItemAttributes createItemAttributes(final String resource) {
        ChefEnvironmentItemAttributes chefEnvironmentItemAttributes = new ChefEnvironmentItemAttributes();
        return super.createItemAttributes(chefEnvironmentItemAttributes, resource);
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final ChefEnvironmentItemAttributes itemAttr,
            final String resource) {
        return ChangeStatus.UNCHANGED;
    }

    @Override
    protected void setRelationships(final ConnectedItem item, final String resource) {
        // TODO Auto-generated method stub

    }

    // METHODS - END ----------------------------------------------------------

}
