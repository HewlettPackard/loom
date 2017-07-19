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
package com.hp.hpl.loom.manager.adapter;

import java.util.Collection;

import com.hp.hpl.loom.adapter.AggregationUpdate;
import com.hp.hpl.loom.adapter.UpdateResult;
import com.hp.hpl.loom.exceptions.NoSuchAggregationException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Session;

/**
 * Operations to update or delete Grounded Aggregations.
 */
public interface AggregationManagement {

    /**
     * Update a single Grounded Aggregation. See
     * {@link #updateGroundedAggregations(Session, Collection)} for a more complete description of
     * the update mechanism for Grounded Aggregations.
     *
     * @param session Current Session.
     * @param aggregation The Grounded Aggregation to be updated.
     * @param update The set of updates to be applied to the aggregation.
     * @throws NoSuchSessionException The specified session does not exist.
     * @throws NoSuchAggregationException The specified aggregation does not exist.
     *
     * @see #updateGroundedAggregations(Session, Collection)
     */
    void updateGroundedAggregation(Session session, Aggregation aggregation, UpdateResult update)
            throws NoSuchSessionException, NoSuchAggregationException;

    /**
     * Update multiple Grounded Aggregations in a single operation. This method is called when an
     * Adapter needs to make changes to the collection of Items in one or more Grounded
     * Aggregations. An entry must exist in the supplied {@link AggregationUpdate} collection for
     * each Grounded Aggregation to be updated - an Adapter only needs to include those Grounded
     * Aggregations that have actually changed. The updates to each Grounded Aggregation can be
     * applied in one of two modes - Full or Delta.
     * <p>
     * Note that this method grabs a lock on the specified session to update the Item objects
     * associated with Grounded Aggregations. It is vital for the correct functioning of the server
     * that no changes are made by an Adapter to the Item objects outside of this lock.
     * <p>
     * In Full Mode, a complete, new, fully-connected set of Item objects replace the set of Item
     * objects in the aggregation. This mode is appropriate, for example, when a substantial set of
     * changes have occurred to the items since the last time the method was called, and it is
     * simpler or more efficient to simply supply a fresh new view of the world. Item objects cannot
     * be modified in any way by an adapter outside of the lock. In practise, this restriction means
     * that even a single attribute or relationship change to a single Item object may result in the
     * requirement for an Adapter to totally replace all Items in all of its Grounded Aggregations,
     * and all Item objects passed via this method will be totally disjoint from the previous set
     * passed on the previous invocation. The Items objects used in Full Mode can directly sub-class
     * {@link com.hp.hpl.loom.model.Item}.
     * <p>
     * In Delta Mode, a list of changes to be applied to the supplied set of Items is optionally
     * supplied. This set of changes is applied within the session lock, and therefore can reference
     * and modify, in-place, Item objects that were previously members of the aggregation. The
     * advantage of Delta Mode is that only Item objects directly affected by attribute changes need
     * to be created. All other Item objects can be reused, and if necessary relationships changed
     * between them by the list of delta instructions. See {@link AggregationUpdate},
     * {@link UpdateResult}, and {@link com.hp.hpl.loom.adapter.UpdateDelta} for more information on
     * how to specify the list of delta instructions. Because the list of update instructions can
     * include a change to the attributes of an item (see
     * {@link com.hp.hpl.loom.adapter.ItemAttributeDelta}), the Items objects used in Delta Mode
     * must sub-class {@link com.hp.hpl.loom.model.SeparableItem}. Delta Mode is more complex to use
     * than Full Mode, but can be substantially more efficient on both memory and CPU utilisation.
     * <p>
     * The recommended way to use {@link com.hp.hpl.loom.model.SeparableItem} is that the
     * domain-specific attributes of the item, typically obtained by interrogating entities in the
     * outside world, are set on a sub-class of {@link com.hp.hpl.loom.model.CoreItemAttributes}.
     * The subclass of {@link com.hp.hpl.loom.model.SeparableItem} can optionally define Dynamic
     * Attributes that are typically calculated by inspecting properties of objects to which the
     * Item is connected. These Dynamic Attributes are updated via the
     * {@link com.hp.hpl.loom.model.Item#update()} method.
     * <p>
     * The update to each Grounded Aggregation supplied in the {@link AggregationUpdate} collection
     * can be independently in Full or Delta Mode - for some aggregations the new set of items can
     * be specified as a complete replacement (Full Mode), while for other aggregations the new
     * state can be specified as sequence of changes to be applied (Delta Mode). However, if any one
     * aggregation is updated in Delta Mode, then the execution mechanism of this update call is
     * considered to be in Delta Mode. In the Delta Execution Mechanism, an extra "post-processing"
     * phase is executed to allow the Dynamic Attributes of new or updated items to be updated
     * within the lock (the {@link com.hp.hpl.loom.model.Item#update()} method of all affected Items
     * is called). The "post-processing" phase is executed after all other replacement and delta
     * operations have been applied across all supplied Grounded Aggregations, and when all Items
     * affected by modifications (new, updated, and affected by deletion of connected neighbours)
     * have been determined. Not only will the {@link com.hp.hpl.loom.model.Item#update()} method be
     * called on the set of all directly affected Items, but also on those Items that are connected
     * to this set. The update method will be called at most once in the post-processing phase.
     * <p>
     * Note that in both the Full and Delta Execution Mechanisms, any affected Grounded Aggregation
     * will be marked as dirty. A subtlety of the Delta Execution Mechanism is that it is possible
     * for Grounded Aggregations that are not included in the supplied {@link AggregationUpdate}
     * collection to be indirectly affected by the delta changes, and therefore marked as dirty, as
     * a result of the processing of a delta or the "post-processing" phase. This can occur if a
     * change in one GA indirectly affects at least one Item in a "neighbouring" GA, connected by an
     * Item relationship. There are two circumstances when this can happen - a) Item deletion and b)
     * an attribute delta to update Item attributes.
     * <p>
     * <ul>
     *
     * <li>a) Deletion of Item that is related to Items in neighbouring GA. Take the example of an
     * Item <i>a</i> in GA <i>A</i> is connected to Item <i>b</i> in GA <i>B</i>. Imagine there are
     * no direct changes to any Items in B, and therefore GA B is not listed in the supplied
     * {@link AggregationUpdate} collection. Also, Item <i>a</i> is deleted, and therefore GA
     * <i>A</i> is included in the supplied {@link AggregationUpdate} collection, <i>a</i> is listed
     * in the <i>deletedItems</i> list of {@link UpdateResult}, and an
     * {@link com.hp.hpl.loom.adapter.ItemDeletionDelta} for <i>a</i> is included in the
     * {@link com.hp.hpl.loom.adapter.UpdateDelta}. In the "post-processing" phase of the update,
     * the {@link com.hp.hpl.loom.model.Item#update()} method <i>b</i> is called, because <i>b</i>
     * was previously connected (related) to <i>a</i>. If the
     * {@link com.hp.hpl.loom.model.Item#update()} method b returns true, because it's Dynamic
     * Attributes are affected, then <i>b</i> is considered updated and GA <i>B</i> will be marked
     * as dirty.</li>
     *
     * <li>b) Attribute change that affects Dynamic Attributes of related Items. Take the example of
     * an Item <i>a</i> in GA <i>A</i> is connected to Item <i>b</i> in GA <i>B</i>. Imagine there
     * are no direct changes to any Items in B, and therefore GA B is not listed in the supplied
     * {@link AggregationUpdate} collection. Also, Item <i>a</i> has an attribute change, and
     * therefore GA <i>A</i> is included in the supplied {@link AggregationUpdate} collection,
     * <i>a</i> is listed in the <i>updatedItems</i> list of {@link UpdateResult}, and an
     * {@link com.hp.hpl.loom.adapter.ItemAttributeDelta} for <i>a</i> is included in the
     * {@link com.hp.hpl.loom.adapter.UpdateDelta}. In the "post-processing" phase of the update,
     * the {@link com.hp.hpl.loom.model.Item#update()} method of <i>a</i> and <i>b</i> is called,
     * because <i>b</i> is connected (related) to <i>a</i>. If the
     * {@link com.hp.hpl.loom.model.Item#update()} method b returns true, because it's Dynamic
     * Attributes are affected, then <i>b</i> is considered updated and GA <i>B</i> will be marked
     * as dirty.</li>
     * </ul>
     * <p>
     *
     * @param session Current Session.
     * @param updates The collection of Grounded Aggregations to be updated, and the corresponding
     *        set of updates to be applied to each aggregation.
     * @throws NoSuchSessionException The specified session does not exist.
     * @throws NoSuchAggregationException The specified aggregation does not exist.
     * @see AggregationUpdate
     * @see UpdateResult
     * @see com.hp.hpl.loom.adapter.UpdateDelta
     * @see com.hp.hpl.loom.model.SeparableItem
     */
    void updateGroundedAggregations(Session session, Collection<AggregationUpdate> updates)
            throws NoSuchSessionException, NoSuchAggregationException;

    /**
     * Delete the Grounded Aggregation with the specified Logical ID.
     *
     * @param session Current Session.
     * @param logicalId Logical ID of the Grounded Aggregation.
     * @throws NoSuchSessionException The specified session does not exist.
     * @throws NoSuchAggregationException The specified aggregation does not exist.
     */
    void deleteGroundedAggregation(Session session, String logicalId)
            throws NoSuchSessionException, NoSuchAggregationException;

}
