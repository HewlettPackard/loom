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

import java.util.Collection;

import com.hp.hpl.loom.stitcher.StitcherRulePair;

/**
 * Interface to manage stitching rules.
 */
public interface StitcherRuleManager {

    /**
     * Add a StitcherRulePair to the active stitching rules.
     *
     * @param ruleId Unique ID of the rule.
     * @param rule A StitcherRulePair.
     */
    void addStitcherRulePair(String ruleId, StitcherRulePair<?, ?> rule);

    /**
     * Remove StitcherRulePair with specified ID from the set of active stitching rules.
     *
     * @param ruleId ID of StitcherRulePair to be removed.
     */
    void removeStitcherRulePair(String ruleId);

    Collection<StitcherRulePair> getRules();
}
