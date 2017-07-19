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
package com.hp.hpl.loom.adapter.os.real;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.AggregationUpdater;
import com.hp.hpl.loom.adapter.os.BaseOsAdapter;
import com.hp.hpl.loom.adapter.os.OsImageType;
import com.hp.hpl.loom.adapter.os.OsInstance;
import com.hp.hpl.loom.adapter.os.OsInstanceType;
import com.hp.hpl.loom.adapter.os.OsItemCollector;
import com.hp.hpl.loom.adapter.os.OsNetworkType;
import com.hp.hpl.loom.adapter.os.OsPortType;
import com.hp.hpl.loom.adapter.os.OsProject;
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
import com.hp.hpl.loom.openstack.NoSupportedApiVersion;
import com.hp.hpl.loom.openstack.OpenstackApi;
import com.hp.hpl.loom.relationships.RelationshipUtil;

public class RealItemCollector extends OsItemCollector {
    private static final Log LOG = LogFactory.getLog(RealItemCollector.class);

    // all compute set to same zones
    public static final String NOVA_ZONES_KEY = "nova";
    public static final String CINDER_ZONES_KEY = NOVA_ZONES_KEY;
    public static final String NEUTRON_ZONES_KEY = NOVA_ZONES_KEY;

    // private Map<String, NovaApi> novaApiMap;
    protected Map<String, Map<String, String[]>> zonesMap;
    // private Map<String, CinderApi> cinderApiMap;
    // private Map<String, NeutronApi> neutronApiMap;
    private OpenstackApi openstackApi;
    private ExecutorService actionExec;
    protected int actionThreads = 1;
    private String proxyHost = null;
    private int proxyPort = -1;

    // protected Iterable<Module> modules = ImmutableSet.<Module>of(new Log4JLoggingModule());
    // protected Properties overrides = new Properties();

    public RealItemCollector(final Session session, final BaseOsAdapter adapter, final AdapterManager adapterManager,
            final Credentials creds) {
        super(session, adapter, adapterManager, creds);
        // novaApiMap = new HashMap<>();
        zonesMap = new HashMap<>();
        // cinderApiMap = new HashMap<>();
        // neutronApiMap = new HashMap<>();
        setCredentials(creds);

        actionExec = Executors.newFixedThreadPool(actionThreads);

    }

    @Override
    public void setCredentials(final Credentials creds) {
        if (creds != null && creds.getContext() instanceof OpenstackApi) {
            openstackApi = (OpenstackApi) creds.getContext();
        }
    }

    // only add to collectList, no need to update
    @Override
    protected Collection<String> createCollectionItemTypeIdList() {
        Collection<String> superList = super.createCollectionItemTypeIdList();
        superList.add(OsPortType.TYPE_LOCAL_ID);
        return superList;
    }

    public OpenstackApi getOpenstackApi() {
        return openstackApi;
    }

    protected String[] setNewZones(final Map<String, String[]> targetMap, final String projectName,
            final Set<String> zoneSet) {
        String[] zones = zoneSet.toArray(new String[0]);
        targetMap.put(projectName, zones);
        return zones;
    }

    @Override
    protected AggregationUpdater<?, ?, ?> getAggregationUpdater(final Aggregation agg)
            throws NoSuchProviderException, NoSuchItemTypeException {
        // this typeId out of the Aggregation is the global one, prefixed by providerId
        try {
            if (aggregationMatchesItemType(agg, OsInstanceType.TYPE_LOCAL_ID)) {
                // instance
                return new RealInstancesUpdater(agg, adapter, OsInstanceType.TYPE_LOCAL_ID, this);
            } else if (aggregationMatchesItemType(agg, OsVolumeType.TYPE_LOCAL_ID)) {
                // volume
                return new RealVolumesUpdater(agg, adapter, OsVolumeType.TYPE_LOCAL_ID, this);
            } else if (aggregationMatchesItemType(agg, OsImageType.TYPE_LOCAL_ID)) {
                // image
                return new RealImagesUpdater(agg, adapter, OsImageType.TYPE_LOCAL_ID, this);
            } else if (aggregationMatchesItemType(agg, OsNetworkType.TYPE_LOCAL_ID)) {
                // network
                return new RealNetworksUpdater(agg, adapter, OsNetworkType.TYPE_LOCAL_ID, this);
            } else if (aggregationMatchesItemType(agg, OsSubnetType.TYPE_LOCAL_ID)) {
                // subnet
                return new RealSubnetsUpdater(agg, adapter, OsSubnetType.TYPE_LOCAL_ID, this);
            } else if (aggregationMatchesItemType(agg, OsRegionType.TYPE_LOCAL_ID)) {
                // region
                return new RealRegionsUpdater(agg, adapter, OsRegionType.TYPE_LOCAL_ID, this);
            } else if (aggregationMatchesItemType(agg, OsProjectType.TYPE_LOCAL_ID)) {
                // project
                return new RealProjectsUpdater(agg, adapter, OsProjectType.TYPE_LOCAL_ID, this);
            } else if (aggregationMatchesItemType(agg, OsPortType.TYPE_LOCAL_ID)) {
                // region
                return new RealPortsUpdater(agg, adapter, this);
            }
        } catch (RuntimeException ex) {
            LOG.error("exception while creating an updater", ex);
            throw new NoSuchProviderException("Adapter has gone");
        }
        return null;
    }


    @Override
    protected ActionResult doScopedAction(final String actionValue, final Collection<Item> items) {
        for (Item item : items) {
            OsInstance osInstance = (OsInstance) item;
            String region = osInstance.getFirstConnectedItemWithRelationshipName(
                    RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(this.getProvider().getProviderType(),
                            OsInstanceType.TYPE_LOCAL_ID, OsRegionType.TYPE_LOCAL_ID))
                    .getName();
            OsProject project = (OsProject) osInstance.getFirstConnectedItemWithRelationshipName(
                    RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(this.getProvider().getProviderType(),
                            OsInstanceType.TYPE_LOCAL_ID, OsProjectType.TYPE_LOCAL_ID));
            String computeVersion = adapter.getAdapterConfig().getPropertiesConfiguration()
                    .getString(Constants.COMPUTE + Constants.VERSION_SUFFIX);
            String[] computeVersions = {};
            if (computeVersion != null) {
                computeVersions = computeVersion.split(",");
            }
            InstanceActionTask iat;
            try {
                iat = new InstanceActionTask(openstackApi
                        .getNovaApi(computeVersions, project.getCore().getItemId(), region).getNovaServers(),
                        osInstance.getCore().getItemId(), actionValue);
            } catch (NoSupportedApiVersion e) {
                throw new RuntimeException("Problem accessing compute servers for version: " + computeVersion
                        + " projectId: " + project.getCore().getItemId() + " regionId: " + region);
            }
            actionExec.execute(iat);

        }
        return new ActionResult(ActionResult.Status.completed);
    }
}
