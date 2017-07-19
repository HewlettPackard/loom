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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.AggregationUpdater;
import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.adapter.os.OsRegionType;
import com.hp.hpl.loom.adapter.os.real.Constants;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;
import com.hp.hpl.loom.model.SeparableItem;
import com.hp.hpl.loom.openstack.NoSupportedApiVersion;
import com.hp.hpl.loom.openstack.swift.model.JsonAccount;
import com.hp.hpl.loom.openstack.swift.model.JsonContainer;

public class RealContainersUpdater extends AggregationUpdater<OsContainer, OsContainerAttributes, JsonContainer>
        implements Iterator<JsonContainer> {
    private static final Log LOG = LogFactory.getLog(RealContainersUpdater.class);

    private SwiftRealItemCollector sric;

    private int accIdx = 0;
    private int accMax = 0;
    private Iterator<? extends JsonContainer> currentIterator;
    private Iterator<? extends JsonContainer> nextIterator;
    protected ArrayList<? extends SeparableItem> accounts;
    protected JsonContainer currentResource;
    protected OsAccount currentOsAccount;

    public RealContainersUpdater(final Aggregation aggregation, final BaseAdapter adapter, final String itemTypeLocalId,
            final SwiftRealItemCollector sric) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemTypeLocalId, sric);
        this.sric = sric;
    }

    @Override
    protected Iterator<JsonContainer> getResourceIterator() {
        accounts = sric.getAccounts();
        accMax = accounts.size();
        if (accMax == 0) {
            return new ArrayList<JsonContainer>(0).iterator();
        }
        accIdx = -1;
        currentIterator = getNewIterator();
        if (currentIterator != null) {
            currentOsAccount = (OsAccount) accounts.get(accIdx);
        }
        return this;
    }

    @Override
    public boolean hasNext() {
        boolean retBool = false;
        if (currentIterator != null && currentIterator.hasNext()) {
            retBool = true;
        } else {
            // loop until an iterator has data or no more
            while (true) {
                nextIterator = getNewIterator();
                if (nextIterator != null) {
                    if (nextIterator.hasNext()) {
                        retBool = true;
                        break;
                    }
                } else {
                    retBool = false;
                    break;
                }
            }
        }
        if (!retBool) {
            currentResource = null;
        }
        return retBool;
    }

    @Override
    public JsonContainer next() {
        if (currentIterator != null && currentIterator.hasNext()) {
            currentResource = currentIterator.next();
            return currentResource;
        } else {
            if (nextIterator == null) {
                nextIterator = getNewIterator();
            }
            if (nextIterator != null && nextIterator.hasNext()) {
                currentIterator = nextIterator;
                currentOsAccount = (OsAccount) accounts.get(accIdx);
                nextIterator = null;
                currentResource = currentIterator.next();
                return currentResource;
            } else {
                currentResource = null;
            }
            throw new NoSuchElementException();
        }
    }

    @Override
    public void remove() {
        // not needed so not implemented
    }

    private Iterator<? extends JsonContainer> getNewIterator() {
        if (LOG.isTraceEnabled()) {
            LOG.trace("XXX 3: accIdx/accMax :" + accIdx + "/" + accMax);
        }
        ++accIdx;
        if (accIdx < accMax) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("XXX 4: accIdx/accMax :" + accIdx + "/" + accMax);
            }
            return getResources(accIdx);
        }
        return null;
    }

    protected Iterator<? extends JsonContainer> getResources(final int accIdx) {
        OsAccount osAccount = (OsAccount) accounts.get(accIdx);
        String regionId = sric.getAdapterItem(OsRegionType.TYPE_LOCAL_ID, osAccount.getRegionId(adapter.getProvider()))
                .getCore().getItemId();
        String projectId = getProjectId(osAccount.getCore().getItemId());
        JsonAccount account = sric.getAccount(projectId, regionId);
        if (account != null && account.getContainers() != null) {
            List<JsonContainer> items = new ArrayList<>(0);
            items.addAll(Arrays.asList(account.getContainers()));
            return items.iterator();
        } else {
            String objectStoreVersion = adapter.getAdapterConfig().getPropertiesConfiguration()
                    .getString(Constants.OBJECTSTORE + Constants.VERSION_SUFFIX);

            String[] objectStoreVersions = {};
            if (objectStoreVersion != null) {
                objectStoreVersions = objectStoreVersion.split(",");
            }

            try {
                return sric.getOpenstackApi().getSwiftApi(objectStoreVersions, projectId, regionId).getSwiftContainers()
                        .getIterator();
            } catch (NoSupportedApiVersion e) {
                throw new RuntimeException("Problem accessing swift containers for version: " + objectStoreVersion
                        + " projectId: " + projectId + " regionId: " + regionId);
            }
        }
    }

    private String getProjectId(final String accountId) {
        int idx = accountId.indexOf("-");
        return accountId.substring(0, idx);
        /*
         * if (idx == -1) { return accountId; } else { return accountId.substring(0, idx); }
         */
    }

    @Override
    protected String getItemId(final JsonContainer resource) {
        return currentOsAccount.getCore().getItemId() + "/" + resource.getName();
    }

    @Override
    protected OsContainer createEmptyItem(final String logicalId) {
        return new OsContainer(logicalId, itemType);
    }

    @Override
    protected OsContainerAttributes createItemAttributes(final JsonContainer container) {
        OsContainerAttributes oca = new OsContainerAttributes();
        oca.setItemName(container.getName());
        oca.setItemId(getItemId(container));
        oca.setBytesUsed(container.getBytes());
        oca.setObjectCount(container.getCount());
        return oca;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final OsContainerAttributes oca,
            final JsonContainer container) {
        if (oca.getBytesUsed() != container.getBytes() || oca.getObjectCount() != container.getCount()) {
            return ChangeStatus.CHANGED_UPDATE;
        } else {
            return ChangeStatus.UNCHANGED;
        }
    }

    @Override
    protected void setRelationships(final ConnectedItem osContainer, final JsonContainer container) {
        // relationships

        // account relationship
        osContainer.setRelationship(adapter.getProvider(), OsAccountType.TYPE_LOCAL_ID,
                currentOsAccount.getCore().getItemId());

    }
}
