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
import com.hp.hpl.loom.adapter.hpcloud.item.ChefUserItem;
import com.hp.hpl.loom.adapter.hpcloud.item.ChefUserItemAttributes;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.ChefUserType;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;

public class ChefUserUpdater extends HpCloudCommonUpdater<ChefUserItem, ChefUserItemAttributes> {

    @SuppressWarnings("unused")
    private static Logger log = Logger.getLogger(ChefUserUpdater.class);

    // CONSTRUCTORS -----------------------------------------------------------

    public ChefUserUpdater(final Aggregation aggregation, final HpCloudAdapter adapter,
            final HpCloudCollector collector) throws NoSuchItemTypeException, UnknownHostException {
        super(aggregation, adapter, collector, ChefUserType.TYPE_LOCAL_ID);
    }

    // CONSTRUCTORS - END -----------------------------------------------------

    // METHODS ----------------------------------------------------------------

    @Override
    protected ChefUserItem createEmptyItem(final String logicalId) {
        return new ChefUserItem(logicalId, HpCloudAdapter.chefUserType);
    }

    @Override
    protected String getItemName() {
        return "chef_user";
    }

    @Override
    protected String[] getPayloadFields() {
        return ChefUserType.PAYLOAD_FIELDS;
    }

    @Override
    protected void setRelationships(final ChefUserItem item) {

        // Saul, TODO: Find relationships between ChefUserItems and ChefOrgItems.
        // The reason why this is not done yet is because as of today (10th Oct
        // 2014) the attributes in the payload are retrieved as Strings, and the
        // organisations at the ChefUserItem object are stored as an Array. It is
        // necessary to do some modifications before this method will work.

        // if (log.isDebugEnabled()) {
        // log.debug("Saul: Setting relationships for item " + item.get_id() +
        // " (type: " + getItemName() + ")");
        // }
        //
        // List<String> targetItemList;
        // String localAttributeToCheck, remoteAttributeToCheck, localAttribute;
        //
        // targetItemList = dbConn.getAllIds("source.type", "chef_org");
        // localAttributeToCheck = "orgs";
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
    protected ChefUserItemAttributes createItemAttributes(final String resource) {
        ChefUserItemAttributes chefUserItemAttributes = new ChefUserItemAttributes();
        return super.createItemAttributes(chefUserItemAttributes, resource);
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final ChefUserItemAttributes itemAttr,
            final String resource) {
        return ChangeStatus.UNCHANGED;
    }

    @Override
    protected void setRelationships(final ConnectedItem item, final String resource) {
        // TODO Auto-generated method stub

    }

    // METHODS - END ----------------------------------------------------------

}
