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
package com.hp.hpl.loom.tapestry;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The thread definition.
 *
 */
@JsonAutoDetect
public class ThreadDefinition {

    private String id;
    private String itemType;
    private QueryDefinition query;
    private String name;

    @JsonIgnore
    private boolean replaced = false;

    @JsonIgnore
    private boolean deleted = false;

    /**
     * The thread definition constructor.
     *
     * @param threadId the threadId
     * @param itemType the itemType
     * @param queryDefinition the queryDefinition
     */
    public ThreadDefinition(final String threadId, final String itemType, final QueryDefinition queryDefinition) {

        validate(threadId, itemType, queryDefinition);
        id = threadId;

        this.itemType = itemType;
        query = queryDefinition;
        name = itemType;
    }

    private void validate(final String threadId, final String itemTypeValue,
            final QueryDefinition queryDefinitionValue) {
        if (threadId == null || threadId.isEmpty() || itemTypeValue == null || queryDefinitionValue == null) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * The thread definition constructor.
     *
     * @param threadId the threadId
     * @param itemType the itemType
     * @param queryDefinition the queryDefinition
     * @param humanReadableName the humanReadablename
     */
    public ThreadDefinition(final String threadId, final String itemType, final QueryDefinition queryDefinition,
            final String humanReadableName) {

        validate(threadId, itemType, queryDefinition);
        id = threadId;
        name = humanReadableName;
        this.itemType = itemType;
        query = queryDefinition;
    }


    /**
     * No-arg constructor.
     */
    public ThreadDefinition() {}

    /**
     * Get the aggregation string for this thread.
     *
     * @return the aggregation
     */
    @JsonIgnore
    public String getAggregation() {
        return query.getInputs().get(0);
    }


    /**
     * Get the replaced flag.
     *
     * @return true if replaced
     */
    @JsonIgnore
    public boolean isReplaced() {
        return replaced;
    }

    /**
     * Set the replaced flag.
     */
    public void setReplaced() {
        replaced = true;
    }

    /**
     * Is the threadDefinition deleted.
     *
     * @return deleted
     */
    @JsonIgnore
    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("id-> " + id);
        str.append("; itemType-> " + itemType);
        str.append("; query-> " + query);
        str.append("; name-> " + name);
        return str.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ThreadDefinition that = (ThreadDefinition) o;
        return Objects.equals(id, that.id) && Objects.equals(itemType, that.itemType)
                && Objects.equals(query, that.query) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, itemType, query, name);
    }


    /**
     * @return the id
     */
    public String getId() {
        return id;
    }


    /**
     * @param id the id to set
     */
    public void setId(final String id) {
        this.id = id;
    }


    /**
     * @return the itemType
     */
    public String getItemType() {
        return itemType;
    }


    /**
     * @param itemType the itemType to set
     */
    public void setItemType(final String itemType) {
        this.itemType = itemType;
    }


    /**
     * @return the query
     */
    public QueryDefinition getQuery() {
        return query;
    }


    /**
     * @param query the query to set
     */
    public void setQuery(final QueryDefinition query) {
        this.query = query;
    }


    /**
     * @return the name
     */
    public String getName() {
        return name;
    }


    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }


    /**
     * @param deleted the deleted to set
     */
    public void setDeleted(final boolean deleted) {
        this.deleted = deleted;
    }
}
