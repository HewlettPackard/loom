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
package com.hp.hpl.loom.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hp.hpl.loom.relationships.RelationshipUtil;

/**
 * Base class for all items created in Loom by an adapter.
 * <p>
 * Relationships between Items can be established using the ConnectedTo mechanism. Items can be
 * Relationship names follow a convention, using the type IDs of the two items in the relationship.
 * The class #{@link com.hp.hpl.loom.relationships.RelationshipUtil} can be used to construct names
 * that follow this convention.
 */
@JsonAutoDetect
public abstract class Item extends Fibre {

    /**
     * The attr alert level key.
     */
    public static final String ATTR_ALERT_LEVEL = "alertLevel";
    /**
     * The attr alert description key.
     */
    public static final String ATTR_ALERT_DESCRIPTION = "alertDescription";
    /**
     * The attr fully qualified name key.
     */
    public static final String ATTR_FULLY_QUALIFIED_NAME = "fullyQualifiedName";


    // Initial size of the map that holds the relationship names
    private static final int INITIAL_SIZE_CONNECTED_KEY_MAP = 20;

    // Initial size of the map contained as a value that holds the actual related items
    private static final int INITIAL_SIZE_CONNECTED_VALUE_MAP = 50;

    private static long uuidCount = 1;

    private String uuid;

    private String fullyQualifiedName;

    /**
     * Alert level. 0 == no alert, >0 means alert is present.
     */
    @JsonProperty("l.alertLevel")
    private int alertLevel = 0;

    /**
     * Optional description of alert level.
     */
    @JsonProperty("l.alertDescription")
    private String alertDescription = "";

    /**
     * The Grounded Aggregation that this Item is a member of.
     */
    @JsonIgnore
    private Aggregation groundedAggregation;

    /**
     * Map of name of relationship to a Map that contains an index (by logical ID) of all target
     * Item objects with that type of relationship.
     */
    @JsonIgnore
    private Map<String, Map<String, Item>> connectedRelationships = new HashMap<>(INITIAL_SIZE_CONNECTED_KEY_MAP);

    /**
     * This method is only included for JSON serialisation. Sub-classes should use of the the other
     * constructors.
     */
    protected Item() {
        super();
    }

    /**
     * Minimal set of parameters to pass in constructor.
     *
     * @param logicalId Logical ID of the item.
     * @param type Type of the item. The ID and local ID of the type must first have been correctly
     *        set.
     */
    public Item(final String logicalId, final ItemType type) {
        super(logicalId, type);
        assignUuid();
    }

    /**
     * Convenience constructor, with name for the item.
     *
     * @param logicalId Logical ID of the item.
     * @param type Type of the item. The ID and local ID of the type must first have been correctly
     *        set.
     * @param name Name of the item.
     */
    public Item(final String logicalId, final ItemType type, final String name) {
        super(logicalId, type, name);
        assignUuid();
    }

    /**
     * Convenience constructor, with name and description for the item.
     *
     * @param logicalId Logical ID of the item.
     * @param type Type of the item. The ID and local ID of the type must first have been correctly
     *        set.
     * @param name Name of the item.
     * @param description Description of the item.
     */
    public Item(final String logicalId, final ItemType type, final String name, final String description) {
        super(logicalId, type, name, description);
        assignUuid();
    }

    private void assignUuid() {
        uuid = Long.toString(uuidCount++);
    }

    /**
     * Getter for the unique identifier of the item.
     *
     * @return The unique identifier of the item.
     */
    public final String getUuid() {
        return uuid;
    }



    /**
     * Setter for the unique identifier of the item.
     *
     * @param uuid The unique identifier of the item.
     */
    public final void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    /**
     * Getter for the fully qualified name of the item.
     *
     * @return The fully qualified name of the item.
     */
    public final String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    /**
     * Returns the value of a property held by this Item. This method should return a value quckly
     * and not use introspection. This is to allow implementation of a private cache or dynamically
     * discovered attributes held in a map by subclasses.
     *
     * @param name of the property to look up
     * @return the value of the named property
     */
    public Object getPropertyValueForName(final String name) {
        return null;
    }

    /**
     * Setter for the fully qualified name of the item.
     *
     * @param fullyQualifiedName The fully qualified name of the item.
     */
    public final void setFullyQualifiedName(final String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
    }

    /**
     * Getter for the alert level of the item.
     *
     * @return The alert level of the item.
     */
    public final int getAlertLevel() {
        return alertLevel;
    }

    /**
     * Setter for the alert level of the item.
     *
     * @param alertLevel The alert level of the item.
     */
    public final void setAlertLevel(final int alertLevel) {
        this.alertLevel = alertLevel;
    }

    /**
     * Clears alert both alertLevel and alertDescription.
     */
    public final void clearAlert() {
        this.alertLevel = 0;
        this.alertDescription = "";
    }

    /**
     * Getter for the alert description of the item.
     *
     * @return The alert description of the item.
     */
    public final String getAlertDescription() {
        return alertDescription;
    }

    /**
     * Setter for the alert description of the item.
     *
     * @param alertDescription The alert description of the item.
     */
    public final void setAlertDescription(final String alertDescription) {
        this.alertDescription = alertDescription;
    }

    /**
     * Method to be overridden by each subclass to reflect changes in some selected attributes and
     * relationships. Subclasses should use helper methods checkToManyRels and checkToOneRels
     * defined below for ManyToMany, ManyToOne, and OneToMany relationships. Also
     * checkConnectedRelationshipsDifferent to detect changes in ConnectedTo relationships.
     *
     * @param oldItem The other Item to compare against.
     * @return true if this item and the item passed as a parameter are different.
     */
    @JsonIgnore
    public boolean isDifferentFrom(final Item oldItem) {
        return checkConnectedRelationshipsDifferent(oldItem);
    }

    /**
     *
     *
     * @param oldItem The other Item to compare against.
     * @return true if the items are the same
     */
    @JsonIgnore
    public boolean isDifferent(final Item oldItem) {
        boolean relationDifferent = checkConnectedRelationshipsDifferent(oldItem);
        if (relationDifferent) {
            return true;
        }

        return !Objects.equals(uuid, oldItem.uuid) || !Objects.equals(fullyQualifiedName, oldItem.fullyQualifiedName)
                || !Objects.equals(alertLevel, oldItem.alertLevel)
                || !Objects.equals(alertDescription, oldItem.alertDescription);
    }

    /**
     * Method to be optionally overridden by subclasses to reflect changes in some attributes and
     * relationships.
     *
     * @return The qualified name of the Item.
     */
    @JsonIgnore
    public String getQualifiedName() {
        return null;
    }

    /**
     * Return the groundedAggregation that contains this item.
     *
     * @return the groundedAggregation
     */
    public final Aggregation getGroundedAggregation() {
        return groundedAggregation;
    }

    /**
     * @param groundedAggregation the groundedAggregation that contains this item.
     */
    public final void setGroundedAggregation(final Aggregation groundedAggregation) {
        this.groundedAggregation = groundedAggregation;
    }

    /**
     * Request that an Item update itself, using information in the available attributes and
     * connected relationships, and return true if the attributes of an Item were actually changed
     * as a result of the call. This method is typically used in the post-processing phase of the
     * Delta Mechanism to update one or more Grounded Aggregation in the AdapterManager interface.
     * Sub-classes can optionally override this method. The default implementation is to compute and
     * set the fullyQualifiedName. If a sub-class overrides this method, the default implementation
     * must be called by invoking {@code super.update()}.
     *
     * @return True if the Item was actually changed.
     */
    public boolean update() {
        String currentFQN = getFullyQualifiedName();
        boolean setFQN = currentFQN == null;
        String fqnPrefix = this.getGroundedAggregation().getLogicalId() + "/";
        String nextQN = getQualifiedName();
        if (!setFQN) {
            String currentQN = currentFQN.substring(fqnPrefix.length());
            setFQN = nextQN != null && !nextQN.equals(currentQN);
        }
        if (setFQN) {
            setFullyQualifiedName(fqnPrefix + nextQN);
        }
        return setFQN;
    }

    @Deprecated
    protected final boolean checkToManyRels(final Map<String, ? extends Item> itemMap,
            final Collection<? extends Item> oldItems) {
        if (itemMap.size() != oldItems.size()) {
            return true;
        }
        if (itemMap.isEmpty()) {
            return false;
        }
        for (Item oldItem : oldItems) {
            if (!itemMap.containsKey(oldItem.getLogicalId())) {
                return true;
            }
        }
        return false;
    }

    @Deprecated
    protected final boolean checkToOneRels(final Item item, final Item otherItem) {
        return item != null && item.getLogicalId() != null && !item.getLogicalId().equals(otherItem.getLogicalId());
    }

    protected final boolean checkConnectedRelationshipsDifferent(final Item oldItem) {
        if (this == oldItem) {
            return false;
        }

        if (getConnectedRelationships().size() != oldItem.getConnectedRelationships().size()) {
            return true;
        }
        for (Map.Entry<String, Map<String, Item>> oldRelationshipsEntry : oldItem.getConnectedRelationships()
                .entrySet()) {
            Map<String, Item> relationships = getConnectedItemMapWithRelationshipName(oldRelationshipsEntry.getKey());
            if (relationships == null) {
                return true;
            }
            Map<String, Item> oldRelationships = oldRelationshipsEntry.getValue();
            if (relationships.size() != oldRelationships.size()) {
                return true;
            }
            for (String logicalId : oldRelationships.keySet()) {
                if (!relationships.containsKey(logicalId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Return the complete set of ConnectedTo relationships. The relationships are represented as a
     * Map of Maps. The keys for the outer map are the named relationships with other Items; the
     * entry is a map containing all of the connected items with that named relationship type,
     * indexed by the logical ID of the Item.
     *
     * @return Complete set of ConnectedTo relationships.
     */
    @JsonIgnore
    public final Map<String, Map<String, Item>> getConnectedRelationships() {
        return connectedRelationships;
    }

    /**
     * Return all items connected with this Item.
     *
     * @return All items connected with this Item.
     */
    @JsonIgnore
    public final Collection<Item> getAllConnectedItems() {
        int totalItems = 0;
        for (Map<String, Item> itemMap : connectedRelationships.values()) {
            totalItems += itemMap.size();
        }
        Map<String, Item> connectedItems = new HashMap<>(2 * totalItems);
        for (Map<String, Item> itemMap : connectedRelationships.values()) {
            for (Item item : itemMap.values()) {
                connectedItems.put(item.getLogicalId(), item);
            }
        }
        return connectedItems.values();
    }

    /**
     * Return all items connected to this Item with specified relationship name.
     *
     * @param name Name of the relationship.
     * @return Connected Items with specified relationship name. This method may return a null value
     *         if no relationships with that name have been defined.
     */
    @JsonIgnore
    public final Collection<Item> getConnectedItemsWithRelationshipName(final String name) {
        Map<String, Item> itemMap = connectedRelationships.get(name);
        return itemMap == null ? null : itemMap.values();
    }

    private <T> int collectionSizeSafe(final Collection<T> collection) {
        return collection == null ? 0 : collection.size();
    }

    /**
     * Gets the number of connected items using the provided relationship name.
     *
     * @param name relationship name to look up with
     * @return the number of connected items.
     */
    @JsonIgnore
    public final int getNumConnectedItemsWithRelationshipName(final String name) {
        return collectionSizeSafe(getConnectedItemsWithRelationshipName(name));
    }

    /**
     * Return the first item connected to this Item with specified relationship name.
     *
     * @param name Name of the relationship.
     * @return Connected first Item with specified relationship name. This method may return a null
     *         value if no relationships with that name have been defined.
     */
    @JsonIgnore
    public final Item getFirstConnectedItemWithRelationshipName(final String name) {
        Map<String, Item> itemMap = connectedRelationships.get(name);
        if (itemMap == null) {
            return null;
        } else {
            if (itemMap.isEmpty()) {
                return null;
            } else {
                return itemMap.values().iterator().next();
            }
        }
    }


    /**
     * Return a map of all items connected to this Item with specified relationship name, indexed by
     * the logical ID of the item.
     *
     * @param name Name of the relationship.
     * @return Map of all Connected Items with specified relationship name. This method may return a
     *         null value if no relationships with that name have been defined.
     */
    @JsonIgnore
    public final Map<String, Item> getConnectedItemMapWithRelationshipName(final String name) {
        return connectedRelationships.get(name);
    }

    // /**
    // * Adds a connected relationship with a name and item.
    // *
    // * @param relationshipName the relationshipName
    // * @param other the other item
    // */
    // public final void addConnectedRelationshipsWithNameAndType(final String relationshipName,
    // final Item other) {
    // addRelationshipToOther(relationshipName, other);
    // other.addRelationshipToOther(relationshipName, this);
    // }
    //

    /**
     * Removes a connected relationship with a given name and for a particular type.
     *
     * @param relationshipName the relationshipName
     * @param other the other type
     */
    public final void removeConnectedRelationshipsWithNameAndType(final String relationshipName, final Item other) {
        removeRelationshipDelta(relationshipName, other);
        other.removeRelationshipDelta(relationshipName, this);
    }

    // private void addRelationshipDelta(final String relationshipName, final Item other) {
    // Map<String, Item> itemMap = connectedRelationships.get(relationshipName);
    // if (itemMap == null) {
    // itemMap = new HashMap<>(INITIAL_SIZE_CONNECTED_VALUE_MAP);
    // connectedRelationships.put(relationshipName, itemMap);
    // }
    // itemMap.put(other.getLogicalId(), other);
    // }

    private void removeRelationshipDelta(final String relationshipName, final Item other) {
        Map<String, Item> itemMap = connectedRelationships.get(relationshipName);
        if (itemMap != null) {
            itemMap.remove(other.getLogicalId());
            if (itemMap.isEmpty()) {
                connectedRelationships.remove(relationshipName);
            }
        }
    }

    /*
     * Add uni-directional relationship of specified name to another Item.
     */
    private void addRelationshipToOther(final String relationshipName, final Item other) {
        Map<String, Item> itemMap = connectedRelationships.get(relationshipName);
        if (itemMap == null) {
            itemMap = new HashMap<>(INITIAL_SIZE_CONNECTED_VALUE_MAP);
            connectedRelationships.put(relationshipName, itemMap);
        }
        itemMap.put(other.getLogicalId(), other);
    }

    /*
     * Remove uni-directional relationship of specified name to another Item.
     */
    private void removeRelationshipToOther(final String relationshipName, final Item other) {
        Map<String, Item> itemMap = connectedRelationships.get(relationshipName);
        if (itemMap != null) {
            itemMap.remove(other.getLogicalId());
            if (itemMap.isEmpty()) {
                connectedRelationships.remove(relationshipName);
            }
        }
    }

    private void validateItemType() {
        if (getItemType() == null || getItemType().getLocalId() == null) {
            throw new IllegalArgumentException("ItemType does not have an local ID " + getLogicalId());
        }
    }

    private void validateConnectedToRelationshipToOther(final Item other) {
        if (other == null) {
            throw new IllegalArgumentException("Other Item in connected relationship was null");
        }
        validateItemType();
        other.validateItemType();
    }

    /**
     * Add bi-directional connected relationship to another item. The method computes the
     * relationship name from the ItemTypes of the item and the specified other item.
     *
     * @param other The other Item to establish a ConnectTo relationship with.
     * @param relType The relationship type.
     */
    public final void addConnectedRelationships(final Item other, final String relType) {
        validateConnectedToRelationshipToOther(other);
        String relationName = RelationshipUtil.getRelationshipNameBetweenTypesWithRelType(getItemType(),
                other.getItemType(), relType);
        addConnectedRelationshipsWithName(relationName, other);
    }

    /**
     * Add bi-directional connected relationships to another item, with specified relationship name.
     *
     * @param name The name of the relationship.
     * @param other The other Item to establish a ConnectTo relationship with.
     */
    public final void addConnectedRelationshipsWithName(final String name, final Item other) {
        addRelationshipToOther(name, other);
        other.addRelationshipToOther(name, this);
    }

    /**
     * Remove bi-directional connected relationship to another item. The method computes the
     * relationship name from the ItemTypes of the item and the specified other item.
     *
     * @param other The other Item to establish a ConnectTo relationship with.
     * @param relType The relationship type.
     */
    public final void removeConnectedRelationships(final Item other, final String relType) {
        validateConnectedToRelationshipToOther(other);
        String relationName = RelationshipUtil.getRelationshipNameBetweenTypesWithRelType(getItemType(),
                other.getItemType(), relType);
        removeConnectedRelationshipsWithName(relationName, other);
    }


    /**
     * Remove bi-directional connected relationships to another item, with specified relationship
     * name.
     *
     * @param name The name of the relationship.
     * @param other The other Item to establish a ConnectTo relationship with.
     */
    public final void removeConnectedRelationshipsWithName(final String name, final Item other) {
        removeRelationshipToOther(name, other);
        other.removeRelationshipToOther(name, this);
    }

    /**
     * Remove all connected relationships with specified name. More efficient, but caller must
     * correctly specify the relationship name.
     *
     * @param name The name of the relationship.
     */
    public final void removeAllConnectedRelationshipsWithRelationshipName(final String name) {
        Map<String, Item> itemMap = connectedRelationships.get(name);
        if (itemMap == null || itemMap.size() == 0) {
            return;
        }
        List<Item> otherItems = new ArrayList<>(itemMap.values());
        for (Item other : otherItems) {
            removeConnectedRelationshipsWithName(name, other);
        }
    }

    // /**
    // * Remove all connected relationships with specified name. More efficient, but caller must
    // * correctly specify the relationship name.
    // *
    // * @param name The name of the relationship.
    // */
    // public final void removeAllConnectedRelationshipsWithRelationshipName(final String name) {
    // Map<String, Item> itemMap = connectedRelationships.get(name); // TODO - test
    // if (itemMap == null || itemMap.size() == 0) {
    // return;
    // }
    //
    // List<String> idsToRemove = new ArrayList<>();
    // List<Item> otherRemove = new ArrayList<>();
    // for (String r : itemMap.keySet()) {
    // Item other = itemMap.get(r);
    // idsToRemove.add(other.getLogicalId());
    // // itemMap.remove(other.getLogicalId());
    // // if (itemMap.isEmpty()) {
    // // connectedRelationships.remove(name);
    // // }
    // otherRemove.add(other);
    // // other.removeAllConnectedRelationshipsWithRelationshipName(name);
    // }
    //
    // for (String id : idsToRemove) {
    // itemMap.remove(id);
    // }
    // for (Item other : otherRemove) {
    // other.removeAllConnectedRelationshipsWithRelationshipName(name);
    // }
    //
    // if (itemMap.isEmpty()) {
    // connectedRelationships.remove(name);
    // }
    //
    // // List<Item> otherItems = new ArrayList<>(itemMap.values());
    // // for (Item other : otherItems) {
    // // removeConnectedRelationshipsWithName(name, other);
    // // }
    // }

    /**
     * Remove all connected relationships between this item and all other items.
     */
    public final void removeAllConnectedRelationships() {
        for (String relationName : new ArrayList<String>(connectedRelationships.keySet())) {
            removeAllConnectedRelationshipsWithRelationshipName(relationName);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        if (!super.equals(o)) {
            return false;
        }

        Item that = (Item) o;

        return Objects.equals(uuid, that.uuid) && Objects.equals(fullyQualifiedName, that.fullyQualifiedName)
                && Objects.equals(alertLevel, that.alertLevel)
                && Objects.equals(alertDescription, that.alertDescription);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hash(uuid, fullyQualifiedName, alertLevel, alertDescription);
    }

    /**
     * Returns the provider type for this item.
     *
     * @return The string providerType
     */
    @JsonIgnore
    public String getProviderType() {
        return getItemType().getId().substring(0, getItemType().getId().indexOf("-"));
    }

}
