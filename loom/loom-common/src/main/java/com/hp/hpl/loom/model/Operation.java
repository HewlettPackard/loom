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

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * The model for the operation json.
 */
@JsonAutoDetect
@JsonInclude(Include.NON_NULL)
public class Operation {

    private String id;
    private String name;
    private String icon;

    private String[] displayParameters;

    private Set<String> itemTypes = new HashSet<>();

    private Set<Parameter> params = new HashSet<>();

    // private Map<String, Param> params;

    private boolean canExcludeItems = false;

    /**
     * No-arg constructor for JSON serialisation.
     */
    public Operation() {
        itemTypes = new HashSet<>();
    }

    /**
     * @param id operation id
     * @param name the operation name
     */
    public Operation(final String id, final String name) {
        this.id = id;
        this.name = name;
        itemTypes = new HashSet<>();
    }

    /**
     * @param id operation id
     * @param name the operation name
     * @param itemTypes Set of operation item types
     */
    public Operation(final String id, final String name, final Set<String> itemTypes) {
        this.id = id;
        this.name = name;
        this.itemTypes = itemTypes;
    }

    /**
     * Get the operation id.
     *
     * @return the operation id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id operation id
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Get the operation itemTypes.
     *
     * @return the map of operation itemTypes.
     */
    public Set<String> getItemTypes() {
        return itemTypes;
    }

    /**
     * @param itemTypes the map of operation itemTypes
     */
    public void setItemTypes(final Set<String> itemTypes) {
        this.itemTypes = itemTypes;
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
     * @return the icon
     */
    public String getIcon() {
        return icon;
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(final String icon) {
        this.icon = icon;
    }

    /**
     * @return the displayParameters
     */
    public String[] getDisplayParameters() {
        return displayParameters;
    }

    /**
     * @param displayParameters the displayParameters to set
     */
    public void setDisplayParameters(final String[] displayParameters) {
        this.displayParameters = displayParameters;
    }

    /**
     * @return the params
     */
    public Set<Parameter> getParams() {
        return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(final Set<Parameter> params) {
        this.params = params;
    }

    /**
     * @return the canExcludeItems
     */
    public boolean isCanExcludeItems() {
        return canExcludeItems;
    }

    /**
     * @param canExcludeItems the canExcludeItems to set
     */
    public void setCanExcludeItems(boolean canExcludeItems) {
        this.canExcludeItems = canExcludeItems;
    }

}
