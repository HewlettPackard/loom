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
package com.hp.hpl.loom.manager.query;

import java.util.Set;

import com.hp.hpl.loom.model.Parameter;

/**
 * Models the meta data required for a new opeation.
 */
public class QuadFunctionMeta {
    private QueryOperation queryOperation = null;
    private String name;
    private String icon;
    private String[] displayParameters;
    private Set<Parameter> params;
    private boolean applyToAllItems = false;
    private boolean canExcludeItems = false;

    /**
     * QuadFunctionMeta basic constructor.
     *
     * @param name the operation name
     * @param queryOperation the query operation
     * @param applyToAllItems whether to apply this to all items
     * @param canExcludeItems can we exclude items with this function
     */
    public QuadFunctionMeta(final String name, final QueryOperation queryOperation, final boolean applyToAllItems,
            final boolean canExcludeItems) {
        this.queryOperation = queryOperation;
        this.name = name;
        this.applyToAllItems = applyToAllItems;
        this.canExcludeItems = canExcludeItems;
    }

    /**
     * Get the query operation.
     *
     * @return the query operation
     */
    public QueryOperation getQueryOperation() {
        return queryOperation;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
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
     * @return the applyToAllItems
     */
    public boolean isApplyToAllItems() {
        return applyToAllItems;
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
