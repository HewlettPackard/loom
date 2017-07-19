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

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hp.hpl.loom.exceptions.InvalidActionSpecificationException;

/**
 * Represents an action supported by an Item or Aggregation.
 */
public class Action {
    /*
     * id used by the adapter to know what to do when a request comes
     */
    private String id;

    /*
     * Human readable name
     */
    private String name;

    /**
     * Human-readable description of the action.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String description;

    /**
     * Identifier of an icon to represent the action, to be used by a UI.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String icon;

    /**
     * Set of parameters required by the action.
     */
    private ActionParameters params;

    /*
     * set by the client when the action is to be executed
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> targets;

    /**
     * The timestamp the action was issued.
     */
    private Long actionIssueTimestamp;

    /**
     * No-arg constructor.
     */
    public Action() {}

    /**
     * Constructor that takes the id, name, description, icon and params.
     *
     * @param id the action id
     * @param name the action name
     * @param description the action description
     * @param icon the action icon
     * @param params the action params
     * @throws InvalidActionSpecificationException thrown if the action has an invalid specification
     */
    public Action(final String id, final String name, final String description, final String icon,
            final ActionParameters params) throws InvalidActionSpecificationException {
        // adapterId is mandatory
        validate(id);
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.params = params;
    }

    private void validate(final String adapterId) throws InvalidActionSpecificationException {
        if (adapterId == null || adapterId.isEmpty()) {
            throw new InvalidActionSpecificationException("Failed to specify a valid id for the action.");
        }
    }

    /**
     * Get the action id.
     *
     * @return the action id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the action id
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Get the list of targets (logical ids).
     *
     * @return the list of targets
     */
    public List<String> getTargets() {
        return targets;
    }

    /**
     * @param targetLogicalIds the target logicalIds
     */
    public void setTargets(final List<String> targetLogicalIds) {
        targets = targetLogicalIds;
    }

    /**
     * Get the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Get the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Get the icon.
     *
     * @return the icon.
     */
    public String getIcon() {
        return icon;
    }

    /**
     * @param icon the icon
     */
    public void setIcon(final String icon) {
        this.icon = icon;
    }

    /**
     * Get the action params.
     *
     * @return the ActionParameters
     */
    public ActionParameters getParams() {
        return params;
    }

    /**
     * @param params the ActionParameters
     */
    public void setParams(final ActionParameters params) {
        this.params = params;
    }

    /**
     * @return the actionIssueTimestamp
     */
    public Long getActionIssueTimestamp() {
        return actionIssueTimestamp;
    }

    /**
     * @param actionIssueTimestamp the actionIssueTimestamp to set
     */
    public void setActionIssueTimestamp(Long actionIssueTimestamp) {
        this.actionIssueTimestamp = actionIssueTimestamp;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("id -> " + id);
        str.append("; name -> " + name);
        str.append("; params-> " + params);
        str.append("; icon-> " + icon);
        str.append("; description-> " + description);
        str.append("; targets -> " + targets);
        str.append("; actionIssueTimestamp-> " + actionIssueTimestamp);
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
        Action that = (Action) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
