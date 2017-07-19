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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * A wrapper for the list of TapestryDefinition.
 */
@JsonAutoDetect
public class TapestryDefinitionList {

    private List<TapestryDefinition> tapestries;

    /**
     * No-arg constructor.
     */
    public TapestryDefinitionList() {
        tapestries = new ArrayList<TapestryDefinition>();
    }

    /**
     * @param tapestries list of tapestryDefinitons
     */
    public TapestryDefinitionList(final List<TapestryDefinition> tapestries) {
        this.tapestries = tapestries;
    }

    /**
     * Get the list of tapestries.
     *
     * @return the list of tapestries.
     */
    public List<TapestryDefinition> getTapestries() {
        return tapestries;
    }

    /**
     * @param tapestries the list of tapestries.
     */
    public void setTapestries(final List<TapestryDefinition> tapestries) {
        this.tapestries = tapestries;
    }
}
