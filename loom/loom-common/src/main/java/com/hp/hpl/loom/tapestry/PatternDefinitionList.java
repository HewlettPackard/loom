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
 * Wrapper for the array of PatternDefinitions.
 */
@JsonAutoDetect
public class PatternDefinitionList {
    private ArrayList<PatternDefinition> patterns;

    /**
     * No-arg constructor.
     */
    public PatternDefinitionList() {
        patterns = new ArrayList<PatternDefinition>();
    }

    /**
     * Get the list of pattern definitions.
     *
     * @return the list of patterns
     */
    public List<PatternDefinition> getPatterns() {
        return patterns;
    }

    /**
     * @param patterns list of pattern definitions
     */
    public void setPatterns(final ArrayList<PatternDefinition> patterns) {
        this.patterns = patterns;
    }
}
