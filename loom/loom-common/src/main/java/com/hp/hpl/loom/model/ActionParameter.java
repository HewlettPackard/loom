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

import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hp.hpl.loom.exceptions.InvalidActionSpecificationException;

/**
 * Description of a parameter for an ActionOperation.
 */
@JsonAutoDetect
public class ActionParameter {

    /**
     * Type enumeration.
     */
    public enum Type {
        /**
         * Enumerated Type.
         */
        ENUMERATED,
        /**
         * String type.
         */
        STRING,
        /**
         * Number type.
         */
        NUMBER,
        /**
         * File type.
         */
        FILE;
    }

    /**
     * Type of parameter.
     */
    private ActionParameter.Type type;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String value;

    private String id;

    /**
     * Human-readable name of the parameter.
     */
    private String name;
    /**
     * Overloaded description of the set of possible values the parameter can take.
     */
    private Map<String, String> range;

    /**
     * No-arg constructor for JSON serialisation.
     */
    public ActionParameter() {}

    /**
     * @param id action id
     * @param type action type
     * @param name action name
     * @param range range of action values
     * @throws InvalidActionSpecificationException thrown if id fails validation
     */
    public ActionParameter(final String id, final ActionParameter.Type type, final String name,
            final Map<String, String> range) throws InvalidActionSpecificationException {
        validate(id);
        this.id = id;
        this.type = type;
        this.name = name;
        this.range = range;
    }

    private void validate(final String idInput) throws InvalidActionSpecificationException {
        if (idInput == null || idInput.isEmpty()) {
            throw new InvalidActionSpecificationException("Failed to specify a valid id for the action parameter.");
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("; type -> " + type);
        str.append("; name -> " + name);
        str.append("; range-> " + range);
        str.append("; id   -> " + id);
        str.append("; value-> " + value);
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
        ActionParameter that = (ActionParameter) o;
        return Objects.equals(name, that.name) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    /**
     * @return the type
     */
    public ActionParameter.Type getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(final ActionParameter.Type type) {
        this.type = type;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(final String value) {
        this.value = value;
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
     * @return the range
     */
    public Map<String, String> getRange() {
        return range;
    }

    /**
     * @param range the range to set
     */
    public void setRange(final Map<String, String> range) {
        this.range = range;
    }
}
