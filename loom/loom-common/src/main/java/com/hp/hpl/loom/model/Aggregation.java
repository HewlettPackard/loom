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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * The basic building block for aggregating Fibres. The Aggregation class is used to represent
 * either a Grounded Aggregation of Items collected by an Adapter, or Derived Aggregations that can
 * contain either Items or other aggregations.
 */
@JsonAutoDetect
public class Aggregation extends Fibre {
    private static final int INITIAL_SIZE_TOP_LEVEL_AGG = 20;
    private static final int INITIAL_SIZE_FIRST_LEVEL_AGG = 100;

    /**
     * Internal class used to represent data dependency relationships between aggregations.
     */
    private class Dependency {
        private Aggregation from;
        private Aggregation to;

        Dependency(final Aggregation from, final Aggregation to) {
            this.from = from;
            this.to = to;
        }

        public final Aggregation getFrom() {
            return from;
        }

        public final Aggregation getTo() {
            return to;
        }
    }

    private Type contains = Type.Item;

    @JsonIgnore
    private String semanticPrefix;

    /**
     * Semantic id of this aggregation.
     */
    @JsonProperty("l.semanticId")
    private String semanticId;

    /**
     * Minimum index in this cluster.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Long minIndex;

    /**
     * Maximum index in this cluster.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Long maxIndex;

    /**
     * Number of leaf items in this aggregation and all contained aggregations.
     */
    private long numberOfItems;

    /**
     * Number of fibres directly contained by this aggregation.
     */
    private long numberOfFibres;

    /**
     * Number of elements created since the last time this cluster was updated.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private long createdCount;

    /**
     * Number of elements updated since the last time this cluster was updated.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private long updatedCount;

    /**
     * Number of elements deleted since the last time this cluster was updated.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private long deletedCount;

    /**
     * Number of elements with one or more associated alerts.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Long alertCount = 0L;

    /**
     * Highest alert level in the cluster.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Integer highestAlertLevel = 0;

    /**
     * Description of (one of the) highest alert level(s) in the cluster.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String highestAlertDescription = "";

    @JsonIgnore
    private boolean grounded = false;

    @JsonIgnore
    private boolean indexed = false;

    @JsonIgnore
    private boolean deletePerformed = false;

    @JsonIgnore
    private boolean dirty = false;

    @JsonIgnore
    private String mergedLogicalId;

    @JsonIgnore
    private Map<String, Aggregation> parents = new HashMap<>();

    @JsonIgnore
    private boolean pending = false;

    private Map<String, Number> plottableAggregateStats;

    private Map<String, List<Number>> aggregationArrayStats;

    @JsonIgnore
    private List<Fibre> elements;

    @JsonIgnore
    private Map<String, Dependency> dependsOn = new HashMap<>();

    @JsonIgnore
    private Map<String, Dependency> dependsOnMe = new HashMap<>();

    /**
     * Required for JSON serialisation.
     */
    public Aggregation() {
        super();
        elements = new ArrayList<>(0);
    }

    /**
     * Convenience constructor for the aggregation. The default Fibre Type for contained in the
     * aggregation is #{@link Type#Item}.
     *
     * @param logicalId Logical ID of the aggregation.
     * @param typeId Type ID of the aggregation.
     * @param name Name of the aggregation.
     * @param description Description of the aggregation.
     * @param size Expected number of elements in the aggregation.
     */
    public Aggregation(final String logicalId, final String typeId, final String name, final String description,
            final int size) {
        super(logicalId, typeId, name, description);
        this.setFibreType(Type.Aggregation);
        elements = new ArrayList<>(size);
    }

    /**
     * Convenience constructor for the aggregation. The default Fibre Type for contained in the
     * aggregation is #{@link Type#Item}.
     *
     * @param logicalId Logical ID of the aggregation.
     * @param typeId Type ID of the aggregation.
     * @param name Name of the aggregation.
     * @param description Description of the aggregation.
     * @param size Expected number of elements in the aggregation.
     * @param mergedLogicalId Logical ID of the Merged Aggregation if this aggregation.
     */
    public Aggregation(final String logicalId, final String typeId, final String name, final String description,
            final int size, final String mergedLogicalId) {
        this(logicalId, typeId, name, description, size);
        this.mergedLogicalId = mergedLogicalId;
    }

    /**
     * Convenience constructor for the aggregation.
     *
     * @param logicalId Logical ID of the aggregation.
     * @param contains Fibre Type of the elements contained by the aggregation.
     * @param typeId Type ID of the aggregation.
     * @param name Name of the aggregation.
     * @param description Description of the aggregation.
     * @param size Expected number of elements in the aggregation.
     */
    public Aggregation(final String logicalId, final Type contains, final String typeId, final String name,
            final String description, final int size) {
        super(logicalId, typeId, name, description);
        this.setFibreType(Type.Aggregation);
        this.contains = contains;
        elements = new ArrayList<>(size);
    }

    /**
     * Getter for the plottable aggregate statistics of the aggregation.
     *
     * @return The plottable aggregate statistics of the aggregation.
     */
    public final Map<String, Number> getPlottableAggregateStats() {
        return plottableAggregateStats;
    }

    /**
     * Setter for the plottable aggregate statistics of the aggregation.
     *
     * @param plottableAggregateStats The plottable aggregate statistics of the aggregation.
     */
    public final void setPlottableAggregateStats(final Map<String, Number> plottableAggregateStats) {
        this.plottableAggregateStats = plottableAggregateStats;
    }

    /**
     * Returns the stats for the aggregation arrays.
     *
     * @return the stats
     */
    public Map<String, List<Number>> getAggregationArrayStats() {
        return aggregationArrayStats;
    }

    /**
     * @param aggregationArrayStats The stats to set
     */
    public final void setAggregationArrayStats(final Map<String, List<Number>> aggregationArrayStats) {
        this.aggregationArrayStats = aggregationArrayStats;
    }


    /**
     * Getter that indicates whether the elements of the aggregation are other aggregations.
     *
     * @return true if the elements of the aggregation are other aggregations.
     */
    public final boolean containsAggregations() {
        return contains == Type.Aggregation;
    }

    /**
     * Getter that indicates whether the contents of the aggregation are pending update.
     *
     * @return true if the contents of the aggregation are pending update.
     */
    public final boolean isPending() {
        return pending;
    }


    /**
     * Setter to indicate whether the contents of the aggregation are pending update.
     *
     * @param pending Set to true if the contents of the aggregation are pending update.
     */
    public final void setPending(final boolean pending) {
        this.pending = pending;
        this.setDependsOnMePending(pending);
    }

    private void setDependsOnMePending(final boolean pend) {
        for (Aggregation dependsOnMeAggregation : this.getDependsOnMeAggregations()) {
            dependsOnMeAggregation.setPending(pend);
        }
    }



    /**
     * If this aggregation is a Grounded Aggregation that is also part of a Merged Aggregation,
     * return the logical ID of the Merged Aggregation.
     *
     * @return The logical ID of the Merged Aggregation, or null if not part of a Merged
     *         Aggregation.
     */
    @JsonIgnore
    public final String getMergedLogicalId() {
        return mergedLogicalId;
    }

    /**
     * Getter for the lower index value for the first element contained in the aggregation.
     *
     * @return The lower index value for the first element contained in the aggregation.
     */
    public final Long getMinIndex() {
        return minIndex;
    }

    /**
     * Setter for the lower index value for the first element contained in the aggregation.
     *
     * @param minIndex The lower index value for the first element contained in the aggregation.
     */
    public final void setMinIndex(final long minIndex) {
        this.minIndex = minIndex;
    }

    /**
     * Getter for the upper index value for the first element contained in the aggregation.
     *
     * @return The upper index value for the first element contained in the aggregation.
     */
    public final Long getMaxIndex() {
        return maxIndex;
    }

    /**
     * Setter for the upper index value for the first element contained in the aggregation.
     *
     * @param maxIndex The upper index value for the first element contained in the aggregation.
     */
    public final void setMaxIndex(final long maxIndex) {
        this.maxIndex = maxIndex;
    }

    /**
     * Getter for the number of leaf Items contained in the aggregation.
     *
     * @return The number of element contained in the aggregation.
     */
    public final long getNumberOfItems() {
        return numberOfItems;
    }

    private void setNumberOfItems(final long numberOfItems) {
        this.numberOfItems = numberOfItems;
    }

    /**
     * Getter for the number of fibres contained in the aggregation.
     *
     * @return The number of element contained in the aggregation.
     */
    public final long getNumberOfFibres() {
        return numberOfFibres;
    }

    private void setNumberOfFibres(final long numberOfFibres) {
        this.numberOfFibres = numberOfFibres;
    }

    /**
     * Getter for the number of elements created in the aggregation.
     *
     * @return The number of elements created in the aggregation.
     */
    public final long getCreatedCount() {
        return createdCount;
    }

    /**
     * Setter for the number of elements created in the aggregation.
     *
     * @param createdCount The number of elements created in the aggregation.
     */
    public final void setCreatedCount(final long createdCount) {
        this.createdCount = createdCount;
    }

    /**
     * Getter for the number of elements updated in the aggregation.
     *
     * @return The number of elements updated in the aggregation.
     */
    public final long getUpdatedCount() {
        return updatedCount;
    }

    /**
     * Setter for the number of elements updated in the aggregation.
     *
     * @param updatedCount The number of elements updated in the aggregation.
     */
    public final void setUpdatedCount(final long updatedCount) {
        this.updatedCount = updatedCount;
    }

    /**
     * Getter for the number of elements deleted in the aggregation.
     *
     * @return The number of elements deleted in the aggregation.
     */
    public final long getDeletedCount() {
        return deletedCount;
    }

    /**
     * Setter for the number of elements deleted in the aggregation.
     *
     * @param deletedCount The number of elements deleted in the aggregation.
     */
    public final void setDeletedCount(final long deletedCount) {
        this.deletedCount = deletedCount;
    }

    /**
     * Getter for the number of elements in alert state in the aggregation.
     *
     * @return The number of elements in alert state in the aggregation.
     */
    public final Long getAlertCount() {
        return alertCount;
    }

    /**
     * Setter for the number of elements in alert state in the aggregation.
     *
     * @param alertCount The number of elements in alert state in the aggregation.
     */
    public final void setAlertCount(final long alertCount) {
        this.alertCount = alertCount;
    }

    /**
     * Getter for the highest alert level of all elements in the aggregation.
     *
     * @return The highest alert level of all elements in the aggregation.
     */
    public final Integer getHighestAlertLevel() {
        return highestAlertLevel;
    }

    /**
     * Setter for the highest alert level of all elements in the aggregation.
     *
     * @param highestAlertLevel The highest alert level of all elements in the aggregation.
     */
    public final void setHighestAlertLevel(final int highestAlertLevel) {
        this.highestAlertLevel = highestAlertLevel;
    }

    /**
     * Getter for alert description of the element with the highest alert level of all elements in
     * the aggregation.
     *
     * @return The alert description of the element with the highest alert level of all elements in
     *         the aggregation.
     */
    public final String getHighestAlertDescription() {
        return highestAlertDescription;
    }

    /**
     * Setter for alert description of the element with the highest alert level of all elements in
     * the aggregation.
     *
     * @param highestAlertDescription The alert description of the element with the highest alert
     *        level of all elements in the aggregation.
     */
    public final void setHighestAlertDescription(final String highestAlertDescription) {
        this.highestAlertDescription = highestAlertDescription;
    }

    /**
     * Clears alerts.
     */
    public final void clearAlerts() {
        this.alertCount = 0L;
        this.highestAlertLevel = 0;
        this.highestAlertDescription = "";
        for (Fibre element : elements) {
            if (contains == Type.Item) {
                ((Item) element).clearAlert();
            } else {
                ((Aggregation) element).clearAlerts();
            }
        }
    }

    /**
     * Getter for an indication that this is a grounded aggregation.
     *
     * @return true if this is a grounded aggregation.
     */
    public final boolean isGrounded() {
        return grounded;
    }

    /**
     * Setter for an indication that this is a grounded aggregation.
     *
     * @param grounded Set to true if this is a grounded aggregation.
     */
    public final void setGrounded(final boolean grounded) {
        this.grounded = grounded;
    }

    /**
     * Getter for an indication that this aggregation is indexed.
     *
     * @return true if this aggregation is indexed.
     */
    public final boolean isIndexed() {
        return indexed;
    }

    /**
     * Setter to indicate if this aggregation is indexed.
     *
     * @param indexed Set to true to indicate that this aggregation is indexed.
     */
    public final void setIndexed(final boolean indexed) {
        this.indexed = indexed;
    }

    /**
     * Getter for an indication that this aggregation is dirty.
     *
     * @return true if this aggregation is indexed.
     */
    public final boolean isDirty() {
        return dirty;
    }

    /**
     * Setter to indicate if this aggregation is dirty.
     *
     * @param dirty Set to true to indicate that this aggregation is dirty.
     */
    public final void setDirty(final boolean dirty) {
        this.dirty = dirty;
        if (dirty) {
            this.setDependsOnMeDirty();
            this.setDirtyBitUpTree();
        }
    }

    private void setDependsOnMeDirty() {
        for (Aggregation dependsOnMeAggregation : this.getDependsOnMeAggregations()) {
            dependsOnMeAggregation.setDirty(true);
        }
    }

    private void setDirtyBitUpTree() {
        for (Aggregation parent : this.getParents()) {
            parent.dirty = true;
            parent.setDirtyBitUpTree();
        }
    }



    /**
     * Get the deletePerformed flag.
     *
     * @return the deletePerformed flag
     */
    public final boolean isDeletePerformed() {
        return deletePerformed;
    }

    /**
     * Getter for the collection of parents of this aggregation.
     *
     * @return The collection of parents of this aggregation.
     */
    public final Collection<Aggregation> getParents() {
        return parents.values();
    }

    private void removeParent(final Aggregation parent) {
        parents.remove(parent.getLogicalId());
    }

    private void addParent(final Aggregation parent) {
        parents.put(parent.getLogicalId(), parent);
    }

    /**
     * Return true if this aggregation is the top level of the aggregation tree.
     *
     * @return true if this aggregation is the top level of the aggregation tree.
     */
    @JsonIgnore
    public final boolean isTopLevel() {
        return parents.size() == 0;
    }

    /**
     * Return the list of top-level aggregations reachable via parents.
     *
     * @return The list of top-level aggregations reachable via parents.
     */
    public final List<Aggregation> calcTopLevelAggregations() {
        List<Aggregation> collection = new ArrayList<>(Aggregation.INITIAL_SIZE_TOP_LEVEL_AGG);
        this.addTopLevelAggregations(collection);
        return collection;
    }

    private void addTopLevelAggregations(final List<Aggregation> collection) {
        if (this.isTopLevel()) {
            collection.add(this);
        }
        for (Aggregation parent : this.getParents()) {
            parent.addTopLevelAggregations(collection);
        }
    }

    /**
     * Return the list of first level aggregations reachable from parents.
     *
     * @return The list of first level aggregations reachable from parents.
     */
    public final List<Aggregation> calcFirstLevelAggregations() {
        List<Aggregation> collection = new ArrayList<>(Aggregation.INITIAL_SIZE_FIRST_LEVEL_AGG);
        this.addFirstLevelAggregations(collection);
        return collection;
    }

    private void addFirstLevelAggregations(final List<Aggregation> collection) {
        if (this.isFirstLevel()) {
            collection.add(this);
        }
        for (Aggregation parent : this.getParents()) {
            parent.addFirstLevelAggregations(collection);
        }
    }

    /**
     * Is this aggregation the immediate child of a root Collection.
     *
     * @return true if the aggregation is the immediate child of a root Collection
     */
    @JsonIgnore
    public final boolean isFirstLevel() {
        for (Aggregation parent : this.getParents()) {
            if (parent.isTopLevel()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Getter for the Fibre Type of elements contained by this aggregation.
     *
     * @return The Fibre Type of elements contained by this aggregation.
     */
    public final Type getContains() {
        return contains;
    }

    /**
     * Setter for the Fibre Type of elements contained by this aggregation.
     *
     * @param contains The Fibre Type of elements contained by this aggregation.
     */
    public final void setContains(final Type contains) {
        this.contains = contains;
    }

    /**
     * Getter for the elements contained by this aggregation.
     *
     * @return The elements contained by this aggregation.
     */
    public final List<Fibre> getElements() {
        return elements;
    }

    /**
     * Returns the semantic prefix for this aggregation.
     *
     * @return String which is the prefix of the semantic id of this aggregation.
     */
    public String getSemanticPrefix() {
        return semanticPrefix;
    }

    /**
     * Set the semantic prefix.
     *
     * @param semanticPrefix Client-usable id that conveys the same meaning for the user regardless
     *        of the actual data being handled.
     */
    public void setSemanticPrefix(final String semanticPrefix) {
        this.semanticPrefix = semanticPrefix;
        this.setSemanticId(semanticPrefix + "/" + this.getName());
    }

    /**
     * Some queries result in DA creation if there are enough data or nor (little data) semantic Id
     * that some clients may use to refer to DAs based on the operations performed to obtain them
     * This is different to a logicalId, which varies depending on the underlying data.
     *
     * @return semanticId Client meaningful id.
     */
    public String getSemanticId() {
        return semanticId;
    }

    /**
     * Set the semantic Id directly. Shouldn't be use directly.
     *
     * @see Aggregation#setSemanticPrefix(String)
     * @param semanticId is the semanticId to set.
     */
    public void setSemanticId(final String semanticId) {
        this.semanticId = semanticId;
    }

    // Remove membership of any contained items
    private void removeMembership() {
        if (this.isIndexed() && elements != null && !this.containsAggregations()) {
            for (Fibre element : elements) {
                element.removeMemberOf(this);
            }
        }
    }

    // Disown any child aggregations.
    private void disownChildAggregations() {
        if (elements != null && this.containsAggregations()) {
            for (Fibre element : elements) {
                ((Aggregation) element).removeParent(this);
            }
        }
    }

    /**
     * Delete an Aggregation. The aggregation will only be deleted if it has no parents.
     *
     * @return true if the aggregation was actually deleted.
     */
    public final boolean delete() {
        if (!deletePerformed && parents.isEmpty()) {
            this.removeMembership();
            this.disownChildAggregations();
            this.setDirty(true);
            this.fixDependenciesOnDeletion();
            indexed = false;
            deletePerformed = true;
            this.setFibreDeleted(new Date());
        }
        return deletePerformed;
    }

    /**
     * Setter for the elements contained by this aggregation.
     *
     * @param newElements The elements contained by this aggregation.
     */
    public final void setElements(final List<Fibre> newElements) {
        this.removeMembership();
        this.disownChildAggregations();

        elements = newElements;
        this.setNumberOfItems(this.getNumContainedItems());
        this.setNumberOfFibres(elements.size());
        if (newElements.size() > 0) {
            this.setContains(newElements.get(0).isAggregation() ? Type.Aggregation : Type.Item);
        }

        if (this.containsAggregations()) {
            for (Fibre element : elements) {
                Aggregation collection = (Aggregation) element;
                collection.addParent(this);
            }
        } else if (this.isIndexed()) {
            for (Fibre element : elements) {
                element.addMemberOf(this);
            }
        }

        this.setFibreUpdated(new Date());
    }

    /**
     * Return the first element of the aggregation.
     *
     * @return The first element of the aggregation.
     */
    public final Fibre first() {
        return elements.get(0);
    }

    /**
     * Return the element at the specified index.
     *
     * @param index The element at the specified index.
     * @return the Fibre
     */
    public final Fibre get(final int index) {
        return elements.get(index);
    }

    /**
     * Adds a Fibre to the Aggregation. The numberOfItems in the Aggregation will be updated, but
     * the updated date will not be modified.
     *
     * @param extra Fibre to add to the Aggregation
     */
    public final void add(final Fibre extra) {
        if (extra.isAggregation()) {
            Aggregation collection = (Aggregation) extra;
            collection.addParent(this);
        }
        // Record that this item is a member of indexed aggregation
        if (this.isIndexed() && extra.isItem()) {
            extra.addMemberOf(this);
        }
        numberOfItems++;
        elements.add(extra);
    }


    // ///////////////////////////////////////////////////////////////////////////////////////
    // Dependency management
    // ///////////////////////////////////////////////////////////////////////////////////////

    /**
     * Gets the list of depends on aggregations.
     *
     * @return list of depends on aggregations
     */
    @JsonIgnore
    public final List<Aggregation> getDependsOnAggregations() {
        List<Aggregation> dependsOnAggregations = new ArrayList<>(dependsOn.size());
        for (Dependency dependency : dependsOn.values()) {
            dependsOnAggregations.add(dependency.getTo());
        }
        return dependsOnAggregations;
    }

    /**
     * Gets the list of top level depends on me aggregations.
     *
     * @return list of top level depends on me aggregations
     */
    @JsonIgnore
    public final List<Aggregation> getTopLevelDependsOnMeAggregations() {
        Map<String, Aggregation> topLevelDependsOnAggregations =
                new HashMap<>(2 * Aggregation.INITIAL_SIZE_TOP_LEVEL_AGG);
        for (Dependency dependency : dependsOnMe.values()) {
            List<Aggregation> topLevels = dependency.getFrom().calcTopLevelAggregations();
            for (Aggregation topLevel : topLevels) {
                topLevelDependsOnAggregations.put(topLevel.getLogicalId(), topLevel);
            }
        }
        return new ArrayList<>(topLevelDependsOnAggregations.values());
    }

    /**
     * Gets the list of depends on me aggregations.
     *
     * @return list of depends on me aggregations
     */
    @JsonIgnore
    public final List<Aggregation> getDependsOnMeAggregations() {
        List<Aggregation> dependsOnAggregations = new ArrayList<>(dependsOnMe.size());
        for (Dependency dependency : dependsOnMe.values()) {
            dependsOnAggregations.add(dependency.getFrom());
        }
        return dependsOnAggregations;
    }

    /**
     * Adds the aggregation to the depends on list.
     *
     * @param dependsOnAggregation aggregation to add
     */
    public final void addDependsOn(final Aggregation dependsOnAggregation) {
        Dependency dependency = new Dependency(this, dependsOnAggregation);
        dependsOn.put(dependsOnAggregation.getLogicalId(), dependency);
        dependsOnAggregation.dependsOnMe.put(this.getLogicalId(), dependency);
    }

    private void removeDependsOn(final String logicalId) {
        dependsOn.remove(logicalId);
    }

    // Patch up dependencies on deletion
    private void fixDependenciesOnDeletion() {
        // If I am gone, others no longer depend on me
        for (Dependency dependency : dependsOnMe.values()) {
            Aggregation agg = dependency.getFrom();
            agg.removeDependsOn(this.getLogicalId());
        }
        // Nor do I depend on any of them
        dependsOnMe.clear();

        // clean "dependOnMe" entries in those aggregations I depend on
        for (Dependency dependency : dependsOn.values()) {
            Aggregation agg = dependency.getTo();
            agg.removeDependOnMe(this.getLogicalId());
        }
        dependsOn.clear();
    }

    private void removeDependOnMe(final String logicalId) {
        dependsOnMe.remove(logicalId);
    }

    // ///////////////////////////////////////////////////////////////////////////////////////
    // Retrieval of contained elements
    // ///////////////////////////////////////////////////////////////////////////////////////

    /**
     * Return the number of elements in this aggregation.
     *
     * @return The number of elements in this aggregation.
     */
    @JsonIgnore
    public final int getSize() {
        return elements.size();
    }

    /**
     * Return the total number of Items contained in this aggregation and all descendants.
     *
     * @return The total number of Items contained in this aggregation and all descendants.
     */
    @JsonIgnore
    public final int getNumContainedItems() {
        if (this.containsAggregations()) {
            int total = 0;
            for (Fibre fibre : this.getElements()) {
                Aggregation collection = (Aggregation) fibre;
                total += collection.getNumContainedItems();
            }
            return total;
        } else {
            return this.getSize();
        }
    }

    private void addContainedItemsToList(final List<Item> allItems) {
        if (this.containsAggregations()) {
            for (Fibre fibre : this.getElements()) {
                Aggregation collection = (Aggregation) fibre;
                collection.addContainedItemsToList(allItems);
            }
        } else {
            for (Fibre fibre : this.getElements()) {
                allItems.add((Item) fibre);
            }
        }
    }

    /**
     * Recursively get all Items contained by Aggregation and all of its children.
     *
     * @return List of Items.
     */
    @JsonIgnore
    public final List<Item> getContainedItems() {
        List<Item> items = new ArrayList<>(this.getNumContainedItems());
        this.addContainedItemsToList(items);
        return items;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Aggregation{");
        sb.append(super.toString());
        sb.append("elements=").append(elements);
        sb.append("dependsOn=").append(dependsOn.keySet());
        sb.append("dependsOnMe=").append(dependsOnMe.keySet());
        sb.append('}');
        return sb.toString();
    }

    /**
     * Get an Iterator for all of the elements of the directly contained by the aggregation.
     *
     * @return Iterator for all of the elements of the directly contained by the aggregation.
     */
    @JsonIgnore
    public final Iterator<Fibre> getIterator() {
        return elements.iterator();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        Aggregation that = (Aggregation) o;

        return Objects.equals(dirty, that.dirty) && Objects.equals(grounded, that.grounded)
                && Objects.equals(contains, that.contains) && Objects.equals(elements, that.elements)
                && Objects.equals(parents, parents);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hash(contains, grounded, dirty, parents.size(), elements);
    }
}
