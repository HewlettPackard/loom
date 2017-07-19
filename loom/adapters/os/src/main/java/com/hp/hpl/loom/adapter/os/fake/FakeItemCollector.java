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
package com.hp.hpl.loom.adapter.os.fake;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.AggregationUpdater;
import com.hp.hpl.loom.adapter.os.BaseOsAdapter;
import com.hp.hpl.loom.adapter.os.OsImageType;
import com.hp.hpl.loom.adapter.os.OsInstance;
import com.hp.hpl.loom.adapter.os.OsInstanceType;
import com.hp.hpl.loom.adapter.os.OsItemCollector;
import com.hp.hpl.loom.adapter.os.OsNetworkType;
import com.hp.hpl.loom.adapter.os.OsProjectType;
import com.hp.hpl.loom.adapter.os.OsRegionType;
import com.hp.hpl.loom.adapter.os.OsSubnetType;
import com.hp.hpl.loom.adapter.os.OsVolumeType;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.manager.adapter.AdapterManager;
import com.hp.hpl.loom.model.ActionResult;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.relationships.RelationshipUtil;


public class FakeItemCollector extends OsItemCollector {
    private static final Log LOG = LogFactory.getLog(FakeItemCollector.class);

    protected FakeOsSystem fos;
    private String authEndpoint;

    public FakeItemCollector(final Session session, final BaseOsAdapter adapter, final AdapterManager adapterManager,
            final Credentials creds, final String authEndpoint, final FakeConfig fc) {
        super(session, adapter, adapterManager, creds);
        fos = new FakeOsSystem(fc, fc.getIndex());
        fos.init();
        this.authEndpoint = authEndpoint;
    }

    public FakeItemCollector(final Session session, final BaseOsAdapter adapter, final AdapterManager adapterManager,
            final Credentials creds) {
        super(session, adapter, adapterManager, creds);
    }

    public String getAuthEndpoint() {
        return authEndpoint;
    }

    @Override
    protected AggregationUpdater<?, ?, ?> getAggregationUpdater(final Aggregation agg)
            throws NoSuchProviderException, NoSuchItemTypeException {
        try {
            if (aggregationMatchesItemType(agg, OsInstanceType.TYPE_LOCAL_ID)) {
                // instance
                return new FakeInstancesUpdater(agg, adapter, OsInstanceType.TYPE_LOCAL_ID, this, fos);
            } else if (aggregationMatchesItemType(agg, OsVolumeType.TYPE_LOCAL_ID)) {
                // volume
                return new FakeVolumesUpdater(agg, adapter, OsVolumeType.TYPE_LOCAL_ID, this, fos);
            } else if (aggregationMatchesItemType(agg, OsImageType.TYPE_LOCAL_ID)) {
                // image
                return new FakeImagesUpdater(agg, adapter, OsImageType.TYPE_LOCAL_ID, this, fos);
            } else if (aggregationMatchesItemType(agg, OsNetworkType.TYPE_LOCAL_ID)) {
                // network
                return new FakeNetworksUpdater(agg, adapter, OsNetworkType.TYPE_LOCAL_ID, this, fos);
            } else if (aggregationMatchesItemType(agg, OsSubnetType.TYPE_LOCAL_ID)) {
                // subnet
                return new FakeSubnetsUpdater(agg, adapter, OsSubnetType.TYPE_LOCAL_ID, this, fos);
            } else if (aggregationMatchesItemType(agg, OsRegionType.TYPE_LOCAL_ID)) {
                // region
                return new FakeRegionsUpdater(agg, adapter, OsRegionType.TYPE_LOCAL_ID, this, fos);
            } else if (aggregationMatchesItemType(agg, OsProjectType.TYPE_LOCAL_ID)) {
                // project
                return new FakeProjectsUpdater(agg, adapter, OsProjectType.TYPE_LOCAL_ID, this, fos);
            }
        } catch (RuntimeException ex) {
            throw new NoSuchProviderException("adapter has gone");
        }
        return null;
    }

    @Override
    protected ActionResult doScopedAction(final String actionValue, final Collection<Item> items) {
        // add a bit of a delay (0.25 sec) to make it a bit more realistic (and allow the tests to
        // behave properly)
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (Item item : items) {
            OsInstance osInstance = (OsInstance) item;
            String region = osInstance
                    .getFirstConnectedItemWithRelationshipName(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                            getProvider().getProviderType(), OsInstanceType.TYPE_LOCAL_ID, OsRegionType.TYPE_LOCAL_ID))
                    .getName();
            Item project = osInstance.getFirstConnectedItemWithRelationshipName(
                    RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(getProvider().getProviderType(),
                            OsInstanceType.TYPE_LOCAL_ID, OsProjectType.TYPE_LOCAL_ID));

            if (LOG.isDebugEnabled()) {
                LOG.debug("ACTION-DEBUG: forwarding action: " + actionValue + " region: " + region + " project: "
                        + project.getName());
            }

            if (!fos.getResourceManager(project, region).doAction(actionValue, osInstance.getCore().getItemId())) {
                return new ActionResult(ActionResult.Status.aborted);
            }
        }
        return new ActionResult(ActionResult.Status.completed);
    }

    // only used for testing
    public FakeOsSystem getFos() {
        return fos;
    }

}
