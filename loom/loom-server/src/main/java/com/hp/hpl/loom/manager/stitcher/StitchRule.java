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
package com.hp.hpl.loom.manager.stitcher;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * General equivalence rule that applies across many sessions, possibly parsed from JSON
 * representation.
 *
 * IGNORE - Just a place-holder at the moment for a data structure for JSON-encoded StitcherRules. .
 */
public class StitchRule {
    private String id; // Unique ID of Rule
    private String left; // e.g. "slim-host#hostname"
    private String right; // e.g. "cmu-node#name"
    private String matchRule; // e.g. "equals"

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getLeft() {
        return left;
    }

    public void setLeft(final String left) {
        this.left = left;
    }

    public String getRight() {
        return right;
    }

    public void setRight(final String right) {
        this.right = right;
    }

    public String getMatchRule() {
        return matchRule;
    }

    public void setMatchRule(final String matchRule) {
        this.matchRule = matchRule;
    }


    // ///////////////////////////
    // Possibly useful methods
    // ///////////////////////////

    @JsonIgnore
    String getLeftItemTypeId() {
        return null;
    }

    String getRightItemTypeId() {
        return null;
    }

    String getAttributeName(final String typeId) {
        return null;
    }

    String getFqAttributeId(final String typeId) {
        return null;
    }

    String getOtherTypeId(final String thisTypeId) {
        return null;
    }
}
