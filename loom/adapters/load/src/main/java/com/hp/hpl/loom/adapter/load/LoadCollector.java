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
package com.hp.hpl.loom.adapter.load;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.loom.adapter.AggregationUpdater;
import com.hp.hpl.loom.adapter.AggregationUpdaterBasedItemCollector;
import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.LoomUtils;
import com.hp.hpl.loom.adapter.load.data.GraphBuilder;
import com.hp.hpl.loom.exceptions.InvalidActionSpecificationException;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.manager.adapter.AdapterManager;
import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionResult;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.tapestry.PatternDefinition;

/**
 * Providers a updater for the FileSystemAdapter.
 *
 */
public class LoadCollector extends AggregationUpdaterBasedItemCollector {

    private GraphBuilder graphBuilder = new GraphBuilder();
    // localTypeId -> ItemType
    private Map<String, ItemType> itemTypeMap = new HashMap<>();

    private List<ItemType> types = new ArrayList<>();

    private boolean builtGraph = false;

    protected Credentials creds;

    /**
     * Constructor it takes a client session, adapter and adapter Manager to register back with.
     *
     * @param session - Client session
     * @param adapter - base adapter (the file adapter)
     * @param adapterManager adapterManager to register ourselves with
     */
    public LoadCollector(final Session session, final BaseAdapter adapter, final AdapterManager adapterManager,
            final Credentials creds) {
        super(session, adapter, adapterManager);
        this.creds = creds;

        try {
            setupTypes();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void setupTypes() throws FileNotFoundException {
        String dataConfig = adapter.getAdapterConfig().getPropertiesConfiguration().getString("dataConfig");
        System.out.println("dataConfig --> " + dataConfig);

        File f = new File(dataConfig);
        System.out.println("dataConfig --> " + dataConfig + " " + f.exists());
        graphBuilder.loadConfig(dataConfig, creds.getUsername());
        // Map<String, List<Node>> items = graphBuilder.getItems();
        Set<String> itemTypeNames = graphBuilder.getItemTypeNames();
        for (String itemTypeName : itemTypeNames) {
            LoadItemType a = new LoadItemType();
            a.setLocalId(itemTypeName);
            a.addAttribute("double1");
            a.addAttribute("double2");
            ItemType existingItem = adapter.getItemType(itemTypeName);
            if (existingItem == null) {
                types.add(a);
            } else {
                types.add(existingItem);
            }
        }

        // ItemType registration
        List<PatternDefinition> newPatterns = new ArrayList<>(types.size());
        try {
            for (ItemType it : types) {
                ItemType existingItem = adapter.getItemType(it.getLocalId());
                if (existingItem == null) {
                    String id = adapterManager.addItemType(provider, it);
                }
                // create a pattern for each new ItemType
                List<ItemType> itList = new ArrayList<>(1);
                itList.add(it);
                itemTypeMap.put(it.getLocalId(), it);
                newPatterns.add(LoomUtils.createPatternDefinitionWithSingleInputPerThread(it.getLocalId(), itList,
                        it.getId(), null, false, provider, null));
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        // create aggregations
        createAggregations();
        // register patterns if there are any new ones
        if (!newPatterns.isEmpty()) {
            try {
                adapterManager.addPatternDefinitions(provider, newPatterns);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }


    public void buildGraphAndRegister() {
        if (builtGraph) {
            return;
        }
        builtGraph = true;
        Integer scale = 1;
        if (creds.getPassword() != null) {
            scale = Integer.parseInt(creds.getPassword());
        }

        graphBuilder.build(scale);
    }

    public GraphBuilder getGraphBuilder() {
        return graphBuilder;
    }

    @Override
    protected AggregationUpdater<?, ?, ?> getAggregationUpdater(final Aggregation aggregation)
            throws NoSuchProviderException, NoSuchItemTypeException {
        try {
            String localTypeId = adapter.getItemTypeLocalIdFromLogicalId(aggregation.getLogicalId());
            return new LoadUpdater(aggregation, adapter, localTypeId, this);
        } catch (RuntimeException ex) {
            throw new NoSuchProviderException("adapter has gone");
        }

    }

    public Collection<ItemType> getItemTypes() {

        return itemTypeMap.values();
    }

    @Override
    protected Collection<String> getUpdateItemTypeIdList() {
        if (itemTypeMap != null) {
            return itemTypeMap.keySet();
        } else {
            return new ArrayList<String>(0);
        }
    }

    @Override
    protected Collection<String> getCollectionItemTypeIdList() {
        return getUpdateItemTypeIdList();
    }

    @Override
    protected ActionResult doAction(final Action action, final String itemTypeId, final Collection<Item> items)
            throws InvalidActionSpecificationException {
        return new ActionResult(ActionResult.Status.completed);
    }

    @Override
    public void close() {
        super.close();
        graphBuilder.clear();
        graphBuilder = null;
        itemTypeMap.clear();
        itemTypeMap = null;
        types.clear();
        types = null;
    }

}
