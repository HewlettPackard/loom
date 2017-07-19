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
package com.hp.hpl.loom.manager.query;

import java.util.Collection;
import java.util.Map;

import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.manager.stitcher.ItemEquivalence;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.relationships.GraphProcessor;
import com.hp.hpl.loom.relationships.RelationshipsModel;

/**
 *
 * Class that is created by the query manager at runtime and passed to the delta describing the
 * function to be implemented. It provides the function with information on its session, its
 * itemtype and any other features that might be needed in the future. It is the fourth argument to
 * the QuadFunction.
 */
public interface OperationContext {

    /**
     * Lets operations obtain an interface to retrieve equivalent items.
     *
     * @param errors in the retrieval of the interface
     * @return Interface for Item equivalence determination.
     */
    ItemEquivalence getItemEquivalence(Map<OperationErrorCode, String> errors);

    /**
     * Lets operations obtain the relationships model defined by the adapter.
     *
     * @param errors in the retrieval of the relationship model
     * @return the relationshipsModel
     */
    RelationshipsModel getRelationshipsModel(Map<OperationErrorCode, String> errors);

    /**
     * Get Item based on its logicalId.
     *
     * @param logicalId of the item type
     * @param errors in the retrieval of the Item
     * @return Item of the specified logicalId
     */
    Item getItemWithLogicalId(String logicalId, Map<OperationErrorCode, String> errors);

    /**
     * Get Items based on the logicalId of the DA they belong into.
     *
     * @param logicalDaId id of the DA
     * @param errors in the retrieval of the Item
     * @return collection of items in that DA
     */
    Collection<Item> getItemsWithLogicalId(String logicalDaId, Map<OperationErrorCode, String> errors);

    /**
     * Used in some operations to get relationships of an Item.
     *
     * @return Graph Processor for the current session
     */
    GraphProcessor getGraphProcessor();

    /**
     * Get item type of the elements used in the current stage of the query pipeline (it is assumed
     * this does not change at the moment: no split, merge, join operations are supported that would
     * create "runtime"-derived ItemTypes.
     *
     * @return ItemType of the current stage of the pipeline
     */
    ItemType getType();


    /**
     * Get equivalent items of an item.
     *
     * @param item Item which equivalences we are after.
     * @return Equivalent Items.
     * @throws NoSuchSessionException Bad session.
     */
    Collection<Item> getEquivalentItems(Item item) throws NoSuchSessionException;


}
