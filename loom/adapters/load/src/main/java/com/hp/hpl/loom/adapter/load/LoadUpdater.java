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

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.hp.hpl.loom.adapter.AggregationUpdater;
import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.adapter.load.data.Node;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;

/**
 * FileSystemUpdater - this creates the FileItems based on the files on the file system.
 */
public class LoadUpdater extends AggregationUpdater<LoadItem, LoadItemAttributes, Node> {
    protected LoadCollector loadCollector = null;
    private String localTypeId;

    private Random rand = new Random();


    /**
     * Constructs a FileSystemUpdater.
     *
     * @param aggregation The aggregation this update will update
     * @param adapter The baseAdapter this updater is part of
     * @param fileSystemCollector The collector it uses
     * @throws NoSuchItemTypeException Thrown if the itemtype isn't found
     * @throws NoSuchProviderException thrown if adapter is not known
     */
    public LoadUpdater(final Aggregation aggregation, final BaseAdapter adapter, String localTypeId,
            final LoadCollector loadCollector) throws NoSuchItemTypeException, NoSuchProviderException {
        super(aggregation, adapter, loadCollector);
        this.loadCollector = loadCollector;
        this.localTypeId = localTypeId;
    }


    @Override
    protected String getItemId(final Node resource) {
        return resource.toString();
    }


    @Override
    protected Iterator<Node> getResourceIterator() {
        loadCollector.buildGraphAndRegister();
        List<Node> nodes = loadCollector.getGraphBuilder().getItems().get(localTypeId);
        return nodes.iterator();
    }


    @Override
    protected LoadItem createEmptyItem(String logicalId) {
        LoadItem loadItem = new LoadItem(logicalId, itemType);
        return loadItem;
    }


    @Override
    protected LoadItemAttributes createItemAttributes(Node resource) {
        LoadItemAttributes lia = new LoadItemAttributes();
        lia.set("double1", rand.nextDouble());
        lia.set("double2", rand.nextDouble());
        return lia;
    }


    @Override
    protected ChangeStatus compareItemAttributesToResource(LoadItemAttributes itemAttr, Node resource) {
        return ChangeStatus.UNCHANGED;
    }


    @Override
    protected void setRelationships(ConnectedItem connectedItem, Node resource) {
        Set<String> types = resource.getRelationsMap().keySet();
        for (String type : types) {
            Set<Node> nodes = resource.getRelationsMap().get(type);
            for (Node node : nodes) {
                connectedItem.setRelationshipWithType(adapter.getProvider(), node.getItemType(), node.getId(), type);
            }
        }
    }


}
