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
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.hp.hpl.loom.adapter.OrderedString;

/**
 * Class to model the parameters for an operation.
 */
@JsonAutoDetect
@JsonInclude(Include.NON_NULL)
public class Parameter {
    private String id;
    private ParameterEnum type;
    private Map<String, Set<OrderedString>> attributes;
    private Set<String> range;
    private String defaultValue;

    /**
     * base constructor.
     */
    public Parameter() {}

    /**
     * @param id the parameter id
     * @param type the parameter enum type
     */
    public Parameter(final String id, final ParameterEnum type) {
        this.id = id;
        this.type = type;
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
     * @return the type
     */
    public ParameterEnum getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(final ParameterEnum type) {
        this.type = type;
    }

    /**
     * @return the attributes
     */
    public Map<String, Set<OrderedString>> getAttributes() {
        return attributes;
    }

    /**
     * @param attributes the attributes to set
     */
    public void setAttributes(final Map<String, Set<OrderedString>> attributes) {
        this.attributes = attributes;
    }

    /**
     * @return the range
     */
    public Set<String> getRange() {
        return range;
    }

    /**
     * @param range the range to set
     */
    public void setRange(final Set<String> range) {
        this.range = range;
    }

    /**
     * @return the defaultValue
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * @param defaultValue the defaultValue to set
     */
    public void setDefaultValue(final String defaultValue) {
        this.defaultValue = defaultValue;
    }


}
