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
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.AdapterItem;
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
import com.hp.hpl.loom.openstack.swift.model.JsonObject;

public class RealObjectsUpdater extends AggregationUpdater<OsObject, OsObjectAttributes, JsonObject>
        implements Iterator<JsonObject> {
    private static final Log LOG = LogFactory.getLog(RealObjectsUpdater.class);

    private SwiftRealItemCollector sric;

    private int contIdx = 0;
    private int contMax = 0;
    private Iterator<? extends JsonObject> currentIterator;
    private Iterator<? extends JsonObject> nextIterator;
    protected ArrayList<? extends SeparableItem> containers;
    protected JsonObject currentResource;
    protected OsContainer currentOsContainer;

    public RealObjectsUpdater(final Aggregation aggregation, final BaseAdapter adapter, final String itemTypeLocalId,
            final SwiftRealItemCollector sric) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemTypeLocalId, sric);
        this.sric = sric;
    }

    @Override
    protected Iterator<JsonObject> getResourceIterator() {
        containers = sric.getContainers();
        contMax = containers.size();
        if (contMax == 0) {
            return new ArrayList<JsonObject>(0).iterator();
        }
        contIdx = -1;
        currentIterator = getNewIterator();
        if (currentIterator != null) {
            currentOsContainer = (OsContainer) containers.get(contIdx);
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
    public JsonObject next() {
        if (currentIterator != null && currentIterator.hasNext()) {
            currentResource = currentIterator.next();
            return currentResource;
        } else {
            if (nextIterator == null) {
                nextIterator = getNewIterator();
            }
            if (nextIterator != null && nextIterator.hasNext()) {
                currentIterator = nextIterator;
                currentOsContainer = (OsContainer) containers.get(contIdx);
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

    protected AdapterItem<?> getOsAccount(final String logicalId) {
        return sric.getAdapterItem(OsAccountType.TYPE_LOCAL_ID, logicalId);
    }

    private Iterator<? extends JsonObject> getNewIterator() {
        if (LOG.isTraceEnabled()) {
            LOG.trace("XXX 3: contIdx/contMax :" + contIdx + "/" + contMax);
        }
        ++contIdx;
        if (contIdx < contMax) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("XXX 4: contIdx/contMax :" + contIdx + "/" + contMax);
            }
            return getResources(contIdx);
        }
        return null;
    }

    private String getProjectId(final String accountId) {
        int idx = accountId.indexOf("-");
        if (idx == -1) {
            return accountId;
        } else {
            return accountId.substring(0, idx);
        }
    }

    protected Iterator<? extends JsonObject> getResources(final int contIdx) {
        OsContainer cont = (OsContainer) containers.get(contIdx);
        OsAccount osAccount = (OsAccount) getOsAccount(cont.getOsAccountId(adapter.getProvider()));
        String regionId = sric.getAdapterItem(OsRegionType.TYPE_LOCAL_ID, osAccount.getRegionId(adapter.getProvider()))
                .getCore().getItemId();
        String projectId = getProjectId(osAccount.getCore().getItemId());

        String objectStoreVersion = adapter.getAdapterConfig().getPropertiesConfiguration()
                .getString(Constants.OBJECTSTORE + Constants.VERSION_SUFFIX);

        String[] objectStoreVersions = {};
        if (objectStoreVersion != null) {
            objectStoreVersions = objectStoreVersion.split(",");
        }

        try {
            return sric.getOpenstackApi().getSwiftApi(objectStoreVersions, projectId, regionId).getSwiftObjects()
                    .getIterator(cont.getName());
        } catch (NoSupportedApiVersion e) {
            throw new RuntimeException("Problem accessing swift objects for version: " + objectStoreVersion
                    + " region: " + regionId + " cont.getName(): " + cont.getName());
        }
    }

    @Override
    protected String getItemId(final JsonObject resource) {
        return currentOsContainer.getCore().getItemId() + "/" + resource.getName();
    }

    @Override
    protected OsObject createEmptyItem(final String logicalId) {
        return new OsObject(logicalId, itemType);
    }

    @Override
    protected OsObjectAttributes createItemAttributes(final JsonObject resource) {
        OsObjectAttributes ooa = new OsObjectAttributes();
        ooa.setItemName(resource.getName());
        ooa.setItemId(getItemId(resource));
        ooa.setEtag(resource.getHash());
        ooa.setLastModified(resource.getLastModified().toString());
        ooa.setSize(resource.getBytes());
        return ooa;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final OsObjectAttributes ooa, final JsonObject resource) {
        if (!ooa.getEtag().equals(resource.getHash())
                || !ooa.getLastModified().equals(resource.getLastModified().toString())
                || ooa.getSize() != resource.getBytes()) {
            return ChangeStatus.CHANGED_UPDATE;
        } else {
            return ChangeStatus.UNCHANGED;
        }
    }

    @Override
    protected void setRelationships(final ConnectedItem osObject, final JsonObject resource) {
        // relationships
        osObject.setRelationship(adapter.getProvider(), OsContainerType.TYPE_LOCAL_ID,
                currentOsContainer.getCore().getItemId());
    }
}
