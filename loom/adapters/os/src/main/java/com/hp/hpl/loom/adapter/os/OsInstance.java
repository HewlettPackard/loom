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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hp.hpl.loom.adapter.annotations.ConnectedTo;
import com.hp.hpl.loom.adapter.annotations.ItemTypeInfo;
import com.hp.hpl.loom.adapter.os.discover.OsWorkload;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.relationships.RelationshipUtil;

@ItemTypeInfo(value = OsInstanceType.TYPE_LOCAL_ID, supportedAdditionalOperations = {"normaliseRam"})
@ConnectedTo(toClass = OsSubnet.class)
@ConnectedTo(toClass = OsWorkload.class)
@ConnectedTo(toClass = OsImage.class)
@ConnectedTo(toClass = OsVolume.class)
@ConnectedTo(toClass = OsRegion.class)
@ConnectedTo(toClass = OsProject.class)
public class OsInstance extends OsItem<OsInstanceAttributes> {
    private static final Log LOG = LogFactory.getLog(OsInstance.class);
    public static final int POWER_OFF_ALERT_LEVEL = 6;

    private OsInstance() {
        super();
    }


    public OsInstance(final String logicalId, final ItemType instanceType) {
        super(logicalId, instanceType);
    }

    @JsonIgnore
    @Override
    public boolean update() {
        boolean superUpdate = super.update();
        OsInstanceAttributes oia = getCore();
        if (oia.getStatus() != null && !oia.getStatus().equals("ACTIVE")) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Instance " + oia.getItemId() + " is " + oia.getStatus());
            }

            Integer oldAlertLevel = getAlertLevel();

            if (oldAlertLevel == null || oldAlertLevel < POWER_OFF_ALERT_LEVEL) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Setting alertLevel for Instance " + oia.getItemId() + " to " + POWER_OFF_ALERT_LEVEL);
                }

                setAlertLevel(POWER_OFF_ALERT_LEVEL);
                setAlertDescription("Not ACTIVE");

                return true;
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No change to alertLevel for Instance " + oia.getItemId() + " : " + oldAlertLevel);
                }
            }
        } else if (getAlertLevel() == POWER_OFF_ALERT_LEVEL) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Clearing alertlevel for Instance " + oia.getItemId());
            }
            setAlertLevel(0);
            setAlertDescription("");
            return true;
        }

        return superUpdate;
    }
}
