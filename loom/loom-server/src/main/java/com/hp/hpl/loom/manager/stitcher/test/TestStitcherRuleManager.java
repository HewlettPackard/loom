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
package com.hp.hpl.loom.manager.stitcher.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.manager.stitcher.StitcherRuleManager;
import com.hp.hpl.loom.stitcher.StitcherRule;
import com.hp.hpl.loom.stitcher.StitcherRulePair;

class TestStitcherRuleManager implements StitcherRuleManager {

    private static final Log LOG = LogFactory.getLog(TestStitcherRuleManager.class);

    private Map<String, StitcherRulePair> rules = new HashMap<>();

    TestStitcherRuleManager() {}

    @Override
    public void addStitcherRulePair(final String ruleId, final StitcherRulePair<?, ?> rule) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Added StitcherRulePair ruleId=" + ruleId);
        }
        rules.put(ruleId, rule);
    }

    @Override
    public void removeStitcherRulePair(final String ruleId) {
        rules.remove(ruleId);
    }

    /**
     * Return true if any equivalence rules apply for the specified typeId.
     *
     * @param itemTypeId
     * @return
     */
    boolean rulesExistForItemTypeId(final String itemTypeId) {
        return !getReleventSourceRules(itemTypeId).isEmpty();
    }

    /**
     * Return true if an equivalence rule applies from the specified source item typeId to the
     * destination item typeId.
     *
     * @param sourceItemTypeId
     * @param destItemTypeId
     * @return
     */
    boolean rulesExistForItemTypeIds(final String sourceItemTypeId, final String destItemTypeId) {
        return !getReleventRules(sourceItemTypeId, destItemTypeId).isEmpty();
    }

    /*
     * Return a map of {ruleId, StitcherRule} that apply to the specified source type.
     */
    Map<String, StitcherRule> getReleventSourceRules(final String typeId) {
        Map<String, StitcherRule> applicable = new HashMap<>();
        for (StitcherRulePair rulePair : rules.values()) {
            if (rulePair.getLeft().sourceTypeId().equals(typeId)) {
                applicable.put(rulePair.getId(), rulePair.getLeft());
            } else if (rulePair.getRight().sourceTypeId().equals(typeId)) {
                applicable.put(rulePair.getId(), rulePair.getRight());
            }
        }
        return applicable;
    }

    Collection<StitcherRule> getReleventRules(final String sourceItemTypeId, final String destItemTypeId) {
        List<StitcherRule> applicable = new ArrayList<>();
        for (StitcherRulePair rulePair : rules.values()) {
            if (ruleApplies(rulePair.getLeft(), sourceItemTypeId, destItemTypeId)) {
                applicable.add(rulePair.getLeft());
            } else if (ruleApplies(rulePair.getRight(), sourceItemTypeId, destItemTypeId)) {
                applicable.add(rulePair.getRight());
            }
        }
        return applicable;
    }

    private boolean ruleApplies(final StitcherRule rule, final String sourceItemTypeId, final String destItemTypeId) {
        return rule.sourceTypeId().equals(sourceItemTypeId) && rule.otherTypeId().equals(destItemTypeId);
    }

    @Override
    public Collection<StitcherRulePair> getRules() {
        return rules.values();
    }
}
