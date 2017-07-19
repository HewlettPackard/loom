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

import com.hp.hpl.loom.model.Item;


/**
 * Specify changes to the relationships of an Item via the delta mechanism. All referenced items for
 * must be included in the updatedItems list of the corresponding {@link UpdateResult} for the
 * Grounded Aggregation. In fact, the updatedItems list of {@link UpdateResult} must also contain
 * the list of any item that will be affected by the application of any ItemRelationsDelta delta
 * operations, even if that operation is included in an {@link UpdateResult} for a different
 * Grounded Aggregation. For example, if the connected relationships of item <i>a</i> in GA <i>A</i>
 * are to be cleared by an ItemRelationsDelta associated with GA <i>A</i>, then not only must
 * <i>a</i> appear in the updatedItems list of the UpdateResult for GA <i>A</i>, but also any other
 * items <i>b</i> (in GA <i>B</i>) that <i>a</i> was previously connected to must also appear in the
 * updatedItems list of the UpdateResult for GA <i>B</i>, even though no delta operation is actually
 * specified for GA <i>B</i>.
 * <p>
 * As for all delta operations, the {@link Item#update()} method of the affected items, and those
 * connected to the affected items, will be invoked during the post-processing phase.
 * <p>
 * Instances of ItemRelationsDelta are created using one of the static constructor methods.
 * <p>
 *
 * @see UpdateDelta
 * @see UpdateResult
 * @see com.hp.hpl.loom.manager.adapter.AggregationManagement
 */
public final class ItemRelationsDelta {
    private RelationsDeltaType type;
    private Item item; // Always set (Clear, Add, Remove)
    private Item other; // Either other or others must be set if adding or removing a specific
                        // relationship (Add, Remove)
    private Collection<Item> others; // Either other or others must be set if adding or removing a
                                     // specific relationship (Add, Remove)
    private Collection<String> otherIds; // Only used by Adapter Framework
    private String relationsName; // Optionally set. If used in add or remove, explicitly specified
                                  // name of relationship. If used in clearing relationships will
                                  // limit to relations of a specific type, otherwise will clear all
                                  // relations.

    private ItemRelationsDelta(final RelationsDeltaType type, final Item item) {
        this.type = type;
        this.item = item;
    }

    /**
     * Add a relation between an item in the Grounded Aggregation (that corresponds with the
     * containing {@link UpdateResult}) and another item.
     *
     * @param item Item in matching GA.
     * @param other Another item.
     * @return New ItemRelationsDelta.
     */
    public static ItemRelationsDelta addRelationToOther(final Item item, final Item other) {
        ItemRelationsDelta delta = new ItemRelationsDelta(RelationsDeltaType.Add, item);
        delta.setOther(other);
        return delta;
    }

    /**
     * Remove a relation between an item in the Grounded Aggregation (that corresponds with the
     * containing {@link UpdateResult}) and another item.
     *
     * @param item Item in matching GA.
     * @param other Another item.
     * @return New ItemRelationsDelta.
     */
    public static ItemRelationsDelta removeRelationToOther(final Item item, final Item other) {
        ItemRelationsDelta delta = new ItemRelationsDelta(RelationsDeltaType.Remove, item);
        delta.setOther(other);

        return delta;
    }

    /**
     * Add relations between an item in the Grounded Aggregation (that corresponds with the
     * containing {@link UpdateResult}) and a set of other items.
     *
     * @param item Item in matching GA.
     * @param others Other items.
     * @return New ItemRelationsDelta.
     */
    public static ItemRelationsDelta addRelationsToOthers(final Item item, final Collection<Item> others) {
        ItemRelationsDelta delta = new ItemRelationsDelta(RelationsDeltaType.Add, item);
        delta.setOthers(others);
        return delta;
    }

    /**
     * Remove relations between an item in the Grounded Aggregation (that corresponds with the
     * containing {@link UpdateResult}) and a set of other items.
     *
     * @param item Item in matching GA.
     * @param others Other items.
     * @return New ItemRelationsDelta.
     */
    public static ItemRelationsDelta removeRelationsToOthers(final Item item, final Collection<Item> others) {
        ItemRelationsDelta delta = new ItemRelationsDelta(RelationsDeltaType.Remove, item);
        delta.setOthers(others);
        return delta;
    }

    /**
     * Add a named relation between an item in the Grounded Aggregation (that corresponds with the
     * containing {@link UpdateResult}) and another item.
     *
     * @param item Item in matching GA.
     * @param other Another item.
     * @param relationsName Name of relation.
     * @return New ItemRelationsDelta.
     */
    public static ItemRelationsDelta addNamedRelationToOther(final Item item, final Item other,
            final String relationsName) {
        ItemRelationsDelta delta = addRelationToOther(item, other);
        delta.setRelationsName(relationsName);
        return delta;
    }

    /**
     * Remove a named relation between an item in the Grounded Aggregation (that corresponds with
     * the containing {@link UpdateResult}) and another item.
     *
     * @param item Item in matching GA.
     * @param other Another item.
     * @param relationsName Name of relation.
     * @param relationsType the relationsType.
     * @return New ItemRelationsDelta.
     */
    public static ItemRelationsDelta removeNamedRelationToOther(final Item item, final Item other,
            final String relationsName, final String relationsType) {
        ItemRelationsDelta delta = removeRelationToOther(item, other);
        delta.setRelationsName(relationsName);
        return delta;
    }

    /**
     * Add named relations between an item in the Grounded Aggregation (that corresponds with the
     * containing {@link UpdateResult}) and a set of other items.
     *
     * @param item Item in matching GA.
     * @param others Other items.
     * @param relationsName Name of relation.
     * @param relationsType the relationsType.
     * @return New ItemRelationsDelta.
     */
    public static ItemRelationsDelta addNamedRelationsToOthers(final Item item, final Collection<Item> others,
            final String relationsName, final String relationsType) {
        ItemRelationsDelta delta = addRelationsToOthers(item, others);
        delta.setRelationsName(relationsName);
        return delta;
    }

    /**
     * Remove named relations between an item in the Grounded Aggregation (that corresponds with the
     * containing {@link UpdateResult}) and a set of other items.
     *
     * @param item Item in matching GA.
     * @param others Other items.
     * @param relationsName Name of relation.
     * @param relationsType the relationsType.
     * @return New ItemRelationsDelta.
     */
    public static ItemRelationsDelta removeNamedRelationsToOthers(final Item item, final Collection<Item> others,
            final String relationsName, final String relationsType) {
        ItemRelationsDelta delta = removeRelationsToOthers(item, others);
        delta.setRelationsName(relationsName);
        return delta;
    }

    /**
     * Remove all relations between an item in the Grounded Aggregation (that corresponds with the
     * containing {@link UpdateResult}) and all other items.
     *
     * @param item Item in matching GA.
     * @return New ItemRelationsDelta.
     */
    public static ItemRelationsDelta clearAllItemRelations(final Item item) {
        return new ItemRelationsDelta(RelationsDeltaType.Clear, item);
    }

    /**
     * Remove all specifically named relations between an item in the Grounded Aggregation (that
     * corresponds with the containing {@link UpdateResult}) and all other items.
     *
     * @param item Item in matching GA.
     * @param relationsName Name of relation.
     * @return New ItemRelationsDelta.
     */
    public static ItemRelationsDelta clearAllItemNamedRelations(final Item item, final String relationsName) {
        ItemRelationsDelta delta = clearAllItemRelations(item);
        delta.setRelationsName(relationsName);
        return delta;
    }

    /**
     * Gets the type.
     *
     * @return The relation delta type
     */
    public RelationsDeltaType getType() {
        return type;
    }

    /**
     * Gets the item.
     *
     * @return the item
     */
    public Item getItem() {
        return item;
    }

    /**
     * Gets the other item.
     *
     * @return the other item
     */
    public Item getOther() {
        return other;
    }

    private void setOther(final Item other) {
        this.other = other;
    }

    /**
     * Gets the collection of the other {@link Item}.
     *
     * @return gets the collection of items
     */
    public Collection<Item> getOthers() {
        return others;
    }

    /**
     * Gets the relationsName.
     *
     * @return the relations name
     */
    public String getRelationsName() {
        return relationsName;
    }

    private void setRelationsName(final String relationsName) {
        this.relationsName = relationsName;
    }

    // ///////////////////////////////////////////////////////////////////////
    // Operations used by adapter framework.
    // ///////////////////////////////////////////////////////////////////////

    /**
     * Sets the collection of other {@link Item}.
     *
     * @param others the other items
     */
    public void setOthers(final Collection<Item> others) {
        this.others = others;
    }

    /**
     * Gets the other ids.
     *
     * @return returns the other ids
     */
    public Collection<String> getOtherIds() {
        return otherIds;
    }

    /**
     * Sets the collection of other ids.
     *
     * @param otherIds a collection of other ids
     */
    public void setOtherIds(final Collection<String> otherIds) {
        this.otherIds = otherIds;
    }

    /**
     * Adds the Item to the others via an ItemRelationsDelta.
     *
     * @param item Item to add
     * @param others Others to set it on
     * @return the ItemRelationsDelta
     */
    public static ItemRelationsDelta addItemToOtherIdsRelations(final Item item, final Collection<String> others) {
        ItemRelationsDelta delta = new ItemRelationsDelta(RelationsDeltaType.Add, item);
        delta.setOtherIds(others);
        return delta;
    }


    /**
     * Adds the Item to the others using ids and via an ItemRelationsDelta.
     *
     * @param item Item to add
     * @param otherIds OtherIds to it to
     * @param relationsName the relationship name
     * @return the ItemRelationsDelta
     */
    public static ItemRelationsDelta addItemToOtherIdsNamedRelations(final Item item, final Collection<String> otherIds,
            final String relationsName) {
        ItemRelationsDelta delta = addItemToOtherIdsRelations(item, otherIds);
        delta.setRelationsName(relationsName);
        return delta;
    }

    /**
     * Remove the item from the other ids relationship.
     *
     * @param item Item to remove
     * @param otherIds OthersIds to remove it from
     * @param relationsName relationship name
     * @return the ItemRelationsDelta
     */
    public static ItemRelationsDelta removeItemToOtherIdsNamedRelations(final Item item,
            final Collection<String> otherIds, final String relationsName) {
        ItemRelationsDelta delta = removeItemToOtherIdsRelations(item, otherIds);
        delta.setRelationsName(relationsName);
        return delta;
    }

    /**
     * Remove the item from the other items relationship.
     *
     * @param item Item to remove
     * @param others Item to remove Item from
     * @return the ItemRelationsDelta
     */
    public static ItemRelationsDelta removeItemToOtherIdsRelations(final Item item, final Collection<String> others) {
        ItemRelationsDelta delta = new ItemRelationsDelta(RelationsDeltaType.Remove, item);
        delta.setOtherIds(others);
        return delta;
    }
}
