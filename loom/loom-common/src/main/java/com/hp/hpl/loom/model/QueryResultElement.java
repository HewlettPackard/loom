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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Pointer to either an Item or an Aggregation, and associated relationships to other Fibres. Used
 * for on-the-wire wrapping of a query result.
 */
@JsonAutoDetect
public class QueryResultElement {


    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Fibre entity;

    @JsonProperty("l.relations")
    private Set<String> relations = new HashSet<>();

    @JsonProperty("l.relationTypes")
    private Map<String, Set<String>> relationTypes = new HashMap<>();

    @JsonProperty("l.relationPaths")
    private Map<String, List<String>> relationPaths = new HashMap<>();

    @JsonProperty("l.equivalenceRelations")
    private Set<String> equivalenceRelations = new HashSet<>();


    @JsonProperty("l.tags")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @SuppressWarnings("PMD.UnusedPrivateField")
    private String tags;

    /**
     * @param entity Fibre to set as the QueryResult entity
     */
    public QueryResultElement(final Fibre entity) {
        setEntity(entity);
    }

    /**
     * No-arg constructor for JSON serialisation.
     */
    public QueryResultElement() {}

    /**
     * Hook to allow more flexibility on the produced json sent to the client. <br/>
     *
     * @see Fibre#getDerivedData()
     * @return Returns a map of properties that are going to be added to the query result.
     */
    @JsonAnyGetter
    public Map<String, Object> getEntityDerivedData() {
        return entity.getDerivedData();
    }

    /**
     * Get the tags for set on this results entity.
     *
     * @return the entity tags
     */
    public String getTags() {
        return entity.getTags();
    }

    /**
     * @param tags tags to set on the result
     */
    public void setTags(final String tags) {
        entity.setTags(tags);
        this.tags = tags;
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////
    // Reference to wrapped entity/entityCollection
    // ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Get the entity this query result element is based on.
     *
     * @return the fibre
     */
    public Fibre getEntity() {
        return entity;
    }

    /**
     * Set the entity on the queryResult element.
     *
     * @param entity the Fibre to base this result on
     */
    public void setEntity(final Fibre entity) {
        this.entity = entity;
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////
    // Relations to other Fibres
    // ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Get the relations Set of strings.
     *
     * @return set of strings that define the relations.
     */
    public Set<String> getRelations() {
        return relations;
    }

    /**
     * Clears the relations set then returns it.
     *
     * @return the now empty relations Set
     */
    public Set<String> clearRelations() {
        relations.clear();
        return relations;
    }

    /**
     * Returns the relation Types map.
     *
     * @return the relation types map
     */
    public Map<String, Set<String>> getRelationTypes() {
        relationTypes.remove(null);
        return relationTypes;
    }

    /**
     * Clears the relation types map.
     *
     * @return returns the empty map
     */
    public Map<String, Set<String>> clearRelationTypes() {
        relationTypes.clear();
        return relationTypes;
    }

    /**
     * Clears the relation paths map.
     *
     * @return returns the empty map
     */
    public Map<String, List<String>> clearRelationPaths() {
        relationPaths.clear();
        return relationPaths;
    }

    /**
     * Get the set of equivalence relations.
     *
     * @return The set of equivalence relations.
     */
    public Set<String> getEquivalenceRelations() {
        return equivalenceRelations;
    }

    /**
     * Clear the set of equivalence relations.
     *
     * @return The cleared set.
     */
    public Set<String> clearEquivalenceRelations() {
        equivalenceRelations.clear();
        return equivalenceRelations;
    }

    /**
     * @return the relationPaths
     */
    public Map<String, List<String>> getRelationPaths() {
        return relationPaths;
    }

    /**
     * @param relationPaths the relationPaths to set
     */
    public void setRelationPaths(Map<String, List<String>> relationPaths) {
        this.relationPaths = relationPaths;
    }
}
