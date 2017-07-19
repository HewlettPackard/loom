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

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hp.hpl.loom.adapter.annotations.ConnectedTo;
import com.hp.hpl.loom.adapter.annotations.ItemTypeInfo;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.relationships.RelationshipUtil;

@ItemTypeInfo(OsImageType.TYPE_LOCAL_ID)
@ConnectedTo(toClass = OsProject.class)
@ConnectedTo(toClass = OsInstance.class)
@ConnectedTo(toClass = OsRegion.class)
public class OsImage extends OsItem<OsImageAttributes> {
    private static final Log LOG = LogFactory.getLog(OsImage.class);
    private int usageCount;


    private OsImage() {
        super();
    }

    public OsImage(final String logicalId, final ItemType imageType) {
        super(logicalId, imageType);
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(final int usageCount) {
        this.usageCount = usageCount;
    }

    @JsonIgnore
    @Override
    public boolean update() {
        boolean superUpdate = super.update();
        int oldUsageCount = getUsageCount();
        Collection<Item> imgInstCollection =
                getConnectedItemsWithRelationshipName(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                        getProviderType(), OsImageType.TYPE_LOCAL_ID, OsInstanceType.TYPE_LOCAL_ID));
        if (imgInstCollection == null) {
            setUsageCount(0);
        } else {
            setUsageCount(imgInstCollection.size());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("usageCount is  now set to: " + usageCount + " for lid: " + getLogicalId());
        }
        return superUpdate | oldUsageCount != getUsageCount();
    }

}
