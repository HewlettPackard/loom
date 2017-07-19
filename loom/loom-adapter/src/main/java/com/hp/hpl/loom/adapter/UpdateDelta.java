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
package com.hp.hpl.loom.adapter;

import java.util.Collection;
import java.util.LinkedList;

import com.hp.hpl.loom.model.CoreItemAttributes;


/**
 * Data structure that holds the complete set of additional deltas to apply to a Grounded
 * Aggregation during an update of
 * AggregationManagement#updateGroundedAggregation(com.hp.hpl.loom.model.Session,
 * com.hp.hpl.loom.model.Aggregation, UpdateResult) or
 * AggregationManagement#updateGroundedAggregations(com.hp.hpl.loom.model.Session, Collection) . The
 * deltas are encoded as three distinct lists.
 * <p>
 * <ul>
 * <li>deletionDelta - Optional list of Item deletion operations. Any connected relations between
 * the Items in this list and any other Item will be removed.</li>
 * <li>relationsDelta - Optional list of delta operations to be applied to relations. Modifications
 * to connected relationships are specified either as addition and removal of relations between
 * pairs of items, or as a removal of all relations for a specific item.</li>
 * <li>attributeDelta - Optional list of delta operations to be applied to attributes. Specifies a
 * set of [ {@link com.hp.hpl.loom.model.SeparableItem}, {@link CoreItemAttributes}] tuples, to
 * re-bind items with modified attributes.</li>
 * </ul>
 * <p>
 *
 * @see ItemDeletionDelta
 * @see ItemRelationsDelta
 * @see ItemAttributeDelta
 * @see UpdateResult
 * @see com.hp.hpl.loom.manager.adapter.AggregationManagement
 */
public class UpdateDelta {
    private Collection<ItemDeletionDelta> deletionDelta;
    private Collection<ItemRelationsDelta> relationsDelta;
    private Collection<ItemAttributeDelta<? extends CoreItemAttributes>> attributeDelta;

    /**
     * Create a new updateDelta, it creates the deletion, relations, attribute deltas.
     */
    public UpdateDelta() {
        deletionDelta = new LinkedList<>();
        relationsDelta = new LinkedList<>();
        attributeDelta = new LinkedList<>();
    }

    /**
     * Constructor for UpdateDelta.
     *
     * @param deletionDelta List of Item deletion operations.
     * @param relationsDelta List of delta operations to be applied to relations.
     * @param attributeDelta List of delta operations to be applied to attributes.
     */
    public UpdateDelta(final Collection<ItemDeletionDelta> deletionDelta,
            final Collection<ItemRelationsDelta> relationsDelta,
            final Collection<ItemAttributeDelta<? extends CoreItemAttributes>> attributeDelta) {
        this.deletionDelta = deletionDelta;
        this.relationsDelta = relationsDelta;
        this.attributeDelta = attributeDelta;
    }

    /**
     * Get the collection of Item Deletion Delta operations.
     *
     * @return The collection of Item Deletion Delta operations.
     */
    public Collection<ItemDeletionDelta> getDeletionDelta() {
        return deletionDelta;
    }

    /**
     * Set the collection of Item Deletion Delta operations.
     *
     * @param deletionDelta The collection of Item Deletion Delta operations.
     */
    public void setDeletionDelta(final Collection<ItemDeletionDelta> deletionDelta) {
        this.deletionDelta = deletionDelta;
    }

    /**
     * Add an Item Deletion Delta operation.
     *
     * @param itemDeletionDelta Item Deletion Delta operation.
     */
    public void addItemDeletionDelta(final ItemDeletionDelta itemDeletionDelta) {
        deletionDelta.add(itemDeletionDelta);
    }

    /**
     * Get the collection of Item Relations Delta operations.
     *
     * @return The collection of Item Relations Delta operations.
     */
    public Collection<ItemRelationsDelta> getRelationsDelta() {
        return relationsDelta;
    }

    /**
     * Set the collection of Item Relations Delta operations.
     *
     * @param relationsDelta The collection of Item Relations Delta operations.
     */
    public void setRelationsDelta(final Collection<ItemRelationsDelta> relationsDelta) {
        this.relationsDelta = relationsDelta;
    }

    /**
     * Add an Item Relations Delta operation.
     *
     * @param itemRelationsDelta Item Relations Delta operation.
     */
    public void addItemRelationsDelta(final ItemRelationsDelta itemRelationsDelta) {
        relationsDelta.add(itemRelationsDelta);
    }

    /**
     * Add multiple Item Relations Delta operations.
     *
     * @param itemRelationsDeltas Item Relations Delta operations.
     */
    public void addItemRelationsDeltas(final Collection<ItemRelationsDelta> itemRelationsDeltas) {
        relationsDelta.addAll(itemRelationsDeltas);
    }

    /**
     * Get the collection of Item Attribute Delta operations.
     *
     * @return The collection of Item Attribute Delta operations.
     */
    public Collection<ItemAttributeDelta<? extends CoreItemAttributes>> getAttributeDelta() {
        return attributeDelta;
    }

    /**
     * Set the collection of Item Attribute Delta operations.
     *
     * @param attributeDelta The collection of Item Attribute Delta operations.
     */
    public void setAttributeDelta(final Collection<ItemAttributeDelta<? extends CoreItemAttributes>> attributeDelta) {
        this.attributeDelta = attributeDelta;
    }

    /**
     * Add an Item Attribute Delta operation.
     *
     * @param itemAttributeDelta Item Attribute Delta operation.
     */
    public void addItemAttributeDelta(final ItemAttributeDelta<? extends CoreItemAttributes> itemAttributeDelta) {
        attributeDelta.add(itemAttributeDelta);
    }

    /**
     * Returns a summary of this deletionDelta.
     *
     * @return the summary of this deletionDelta
     */
    public String getSummary() {
        StringBuffer stBuf = new StringBuffer();
        stBuf.append("del: [" + deletionDelta.size() + "] rel: [" + relationsDelta.size() + "] attr: ["
                + attributeDelta.size() + "]\n");
        return stBuf.toString();
    }

    /**
     * Returns true if there are no update deltas.
     *
     * @return the true if empty
     */
    public boolean isEmpty() {
        return !(getAttributeDelta().size() != 0 || getDeletionDelta().size() != 0 || getRelationsDelta().size() != 0);
    }
}
