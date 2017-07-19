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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hp.hpl.loom.model.introspection.IntrospectionContext;
import com.hp.hpl.loom.relationships.ConditionalStopInformation;
import com.hp.hpl.loom.relationships.ConnectedRelationships;

/**
 * Base class for all model objects created by Loom. A Fibre represents an entity on screen, either
 * an Item or an aggregation of Items.
 */
@JsonAutoDetect
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class Fibre {

    // private static final Log LOG = LogFactory.getLog(Fibre.class);

    // Initial size of the set that holds the aggregations that a member of
    private static final int INITIAL_SIZE_MEMBER_OF_SET = 20;

    /**
     * the Fibre logicalId key.
     */
    public static final String ATTR_LOGICAL_ID = "logicalId";

    /**
     * the Fibre typeId key.
     */
    public static final String ATTR_TYPE_ID = "typeId";

    /**
     * the Fibre name key.
     */
    public static final String ATTR_NAME = "name";

    /**
     * the Fibre description key.
     */
    public static final String ATTR_DESCRIPTION = "description";


    /**
     * The only valid types of Fibre.
     */
    public enum Type {
        /**
         * An single entity with a set of attributes.
         */
        Item,

        /**
         * A collection of 1 or more Items.
         */
        Aggregation
    }

    @JsonIgnore
    private String tags = "";

    @JsonProperty("l.entityType")
    private Type fibreType = Type.Item;

    @JsonProperty("l.logicalId")
    private String logicalId;

    @JsonProperty("l.typeId")
    private String typeId;

    @JsonIgnore
    private ItemType itemType;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String name;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String description;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("l.created")
    private Date fibreCreated;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("l.updated")
    private Date fibreUpdated;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("l.deleted")
    private Date fibreDeleted;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("l.providerId")
    private Set<String> providerIds = new HashSet<>();

    /**
     * Map indexed by class name that stores the set of rules to be applied on that class in order
     * to verify stop graph traversal condition.
     */
    @SuppressWarnings({"checkstyle:design", "checkstyle:linelength"})
    public static final Map<Class, List<BiFunction<ConnectedRelationships, ConditionalStopInformation, Boolean>>> STOP_TRAVERSAL_RULES_BY_CLASS =
            new HashMap<>();

    /**
     * This method is only included for JSON serialisation. Sub-classes should use of the the other
     * constructors.
     */
    protected Fibre() {}

    protected Fibre(final String logicalId, final String typeId, final String name, final String description) {
        validateLogicalId(logicalId);
        validateTypeId(typeId);
        this.logicalId = logicalId;
        this.typeId = typeId;
        this.name = name;
        this.description = description;

        reportStopTraversalRules();

    }

    /**
     * Minimal set of parameters to pass in constructor.
     *
     * @param logicalId Logical ID of the fibre.
     * @param itemType Type of the fibre. The ID and local ID of the type must first have been
     *        correctly set.
     */
    public Fibre(final String logicalId, final ItemType itemType) {
        validateLogicalId(logicalId);
        validateItemType(itemType);
        this.logicalId = logicalId;
        this.itemType = itemType;
        typeId = itemType.getId();

        reportStopTraversalRules();
    }

    /**
     * Convenience constructor, with name for the fibre.
     *
     * @param logicalId Logical ID of the fibre.
     * @param itemType Type of the fibre. The ID and local ID of the type must first have been
     *        correctly set.
     * @param name Name of the fibre.
     */
    public Fibre(final String logicalId, final ItemType itemType, final String name) {
        this(logicalId, itemType);
        this.name = name;
    }

    /**
     * Convenience constructor, with name and description for the fibre.
     *
     * @param logicalId Logical ID of the fibre.
     * @param itemType Type of the fibre. The ID and local ID of the type must first have been
     *        correctly set.
     * @param name Name of the fibre.
     * @param description Description of the fibre.
     */
    public Fibre(final String logicalId, final ItemType itemType, final String name, final String description) {
        this(logicalId, itemType, name);
        this.description = description;
    }

    /**
     * Getter for Tags.
     *
     * @return Tags.
     */
    public final String getTags() {
        return tags;
    }

    /**
     * Getter for derived data. Useful to allow adapter to customise the meta data per fibre. The
     * resulting data will be at the {@link QueryResultElement} level:
     *
     * <pre>
     *  {
     *     "entity": { "core.name": ... },
     *     "l.relations": [ ... ],
     *     < derived data unwrapped here >
     *  }
     * </pre>
     *
     * @return Returns a map of properties. The returns result can't be null.
     */
    public Map<String, Object> getDerivedData() {
        return Collections.emptyMap();
    }

    /**
     * Setter for Tags.
     *
     * @param tags New Tags.
     */
    public final void setTags(final String tags) {
        this.tags = tags;
    }

    /**
     * Getter for FibreType.
     *
     * @return FibreType.
     */
    public final Type getFibreType() {
        return fibreType;
    }

    protected final void setFibreType(final Type type) {
        fibreType = type;
    }

    /**
     * Return true if this fibre is an aggregation.
     *
     * @return true if this fibre is an aggregation.
     */
    @JsonIgnore
    public final boolean isAggregation() {
        return fibreType == Type.Aggregation;
    }

    /**
     * Return true if this fibre is an item.
     *
     * @return true if this fibre is an item.
     */
    @JsonIgnore
    public final boolean isItem() {
        return fibreType == Type.Item;
    }

    /**
     * Getter for Logical ID set at construction time.
     *
     * @return Logical ID.
     */
    public final String getLogicalId() {
        return logicalId;
    }

    /**
     * Getter for Type ID, set at construction time.
     *
     * @return Type ID.
     */
    public final String getTypeId() {
        return typeId;
    }

    /**
     * Getter for Item Type, set at construction time.
     *
     * @return Item Type.
     */
    public final ItemType getItemType() {
        return itemType;
    }

    /**
     * Getter for human readable name.
     *
     * @return Name.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for human readable name.
     *
     * @param name Name.
     */
    public final void setName(final String name) {
        this.name = name;
    }

    /**
     * Getter for human readable description.
     *
     * @return Description.
     */
    public final String getDescription() {
        return description;
    }

    /**
     * Setter for human readable description.
     *
     * @param description Description.
     */
    public final void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Getter for creation time.
     *
     * @return Creation time.
     */
    public final Date getFibreCreated() {
        return fibreCreated;
    }

    /**
     * Setter for creation time.
     *
     * @param created Creation time.
     */
    public final void setFibreCreated(final Date created) {
        fibreCreated = created;
    }

    /**
     * Getter for updated time.
     *
     * @return Updated time.
     */
    public final Date getFibreUpdated() {
        return fibreUpdated;
    }

    /**
     * Setter for updated time.
     *
     * @param updated Updated time.
     */
    public final void setFibreUpdated(final Date updated) {
        fibreUpdated = updated;
    }

    /**
     * Getter for deleted time.
     *
     * @return Deleted time.
     */
    public final Date getFibreDeleted() {
        return fibreDeleted;
    }

    /**
     * Setter for deleted time.
     *
     * @param deleted Deleted time.
     */
    public final void setFibreDeleted(final Date deleted) {
        fibreDeleted = deleted;
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Fibre that = (Fibre) o;

        return Objects.equals(fibreCreated, that.fibreCreated) && Objects.equals(fibreDeleted, that.fibreDeleted)
                && Objects.equals(description, that.description) && Objects.equals(fibreType, that.fibreType)
                && Objects.equals(logicalId, that.logicalId) && Objects.equals(name, that.name)
                && Objects.equals(typeId, that.typeId) && Objects.equals(fibreUpdated, that.fibreUpdated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fibreType, logicalId, typeId, name, description, fibreCreated, fibreUpdated, fibreDeleted);
    }

    @Override
    public String toString() {
        return name;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Remember which Aggregations this Entity belongs to.
    // /////////////////////////////////////////////////////////////////////////////////////////////

    // Reference to additional derived aggregations this entity is a member of, as a result of user
    // queries.
    @JsonIgnore
    private Map<String, Aggregation> memberOf = new HashMap<>(INITIAL_SIZE_MEMBER_OF_SET);

    /**
     * Return aggregations that this fibre is a member of.
     *
     * @return Aggregations that this fibre is a member of.
     */
    public final Collection<Aggregation> getMemberOf() {
        return memberOf.values();
    }

    /**
     * Add aggregation to the collection of aggregations that this fibre is a member of.
     *
     * @param aggregation An aggregation.
     */
    public final void addMemberOf(final Aggregation aggregation) {
        memberOf.put(aggregation.getLogicalId(), aggregation);
    }

    /**
     * Remove aggregation from the collection of aggregations that this fibre is a member of.
     *
     * @param aggregation An aggregation.
     */
    public final void removeMemberOf(final Aggregation aggregation) {
        memberOf.remove(aggregation.getLogicalId());
    }

    /**
     * Clear the collection of aggregations that this fibre is a member of.
     */
    public final void clearMemberOf() {
        memberOf.clear();
    }



    // //////////////////////////////////////////////////////////////////////////////
    // Validation
    // //////////////////////////////////////////////////////////////////////////////

    private void validateLogicalId(final String lid) {
        if (StringUtils.isEmpty(lid)) {
            throw new IllegalArgumentException("Invalid logical ID");
        }
    }

    private void validateTypeId(final String tid) {
        if (StringUtils.isEmpty(tid)) {
            throw new IllegalArgumentException("Invalid type ID");
        }
    }

    private void validateItemType(final ItemType it) {
        if (it == null) {
            throw new IllegalArgumentException("Invalid Item Type");
        }
        validateTypeId(it.getId());
        validateTypeId(it.getLocalId());
    }


    // ///////////////////////////////////////////////////////////////////////////////////////////
    // Property introspection
    // ///////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Return the, possibly nested, nested object that has the specified property name.
     *
     * @param propertyName Name of object.
     * @return IntrospectionContext containing object that has the specified property name
     */
    @JsonIgnore
    public IntrospectionContext getIntrospectionContextForProperty(final String propertyName) {
        return new IntrospectionContext(this, propertyName);
    }

    /**
     * Return the, possibly nested, nested objects that has can be introspected.
     *
     * @return IntrospectionContext containing objects that can be introspected.
     */
    @JsonIgnore
    public IntrospectionContext getIntrospectionContextForAllProperties() {
        return new IntrospectionContext(Arrays.asList(this));
    }

    /**
     * @return the providerIds
     */
    public Set<String> getProviderIds() {
        return providerIds;
    }

    /**
     * @param providerIds the providerIds to set
     */
    public void setProviderIds(final Set<String> providerIds) {
        this.providerIds = providerIds;
    }

    /**
     * Reports a list of stop predicates. In this scenario, the {@link ConnectedRelationships} will
     * assume that the default stop traversal method is the Root analysis
     */
    public void reportStopTraversalRules() {

        List<BiFunction<ConnectedRelationships, ConditionalStopInformation, Boolean>> stopRules =
                STOP_TRAVERSAL_RULES_BY_CLASS.get(this.getClass());

        if (stopRules == null) {
            stopRules = new ArrayList<>();
            stopRules.add(ConnectedRelationships.STOP_ON_ROOT);
            stopRules.add(ConnectedRelationships.VISIT_LAYER_ONLY_ONCE);
            STOP_TRAVERSAL_RULES_BY_CLASS.put(this.getClass(), stopRules);
        }
    }

}
