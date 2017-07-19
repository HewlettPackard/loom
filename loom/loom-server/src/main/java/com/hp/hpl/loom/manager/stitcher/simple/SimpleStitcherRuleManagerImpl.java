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
package com.hp.hpl.loom.manager.stitcher.simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.manager.stitcher.StitchRule;
import com.hp.hpl.loom.manager.stitcher.StitcherRuleManager;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.stitcher.StitcherRule;
import com.hp.hpl.loom.stitcher.StitcherRulePair;
import com.hp.hpl.stitcher.StitchChecker;

public class SimpleStitcherRuleManagerImpl implements StitcherRuleManager {

    // CLASSES -------------------------------------------------------------------------------------

    private class RuleAsStitchChecker implements StitchChecker<Item, Item> {

        private StitcherRule stitcherRule;

        public RuleAsStitchChecker(final StitcherRule stitcherRule) {
            this.stitcherRule = stitcherRule;
        }

        @Override
        public double checkStitch(final Item baseElement, final Item candidateElement) {
            if (!stitcherRule.sourceTypeId().equals(baseElement.getTypeId())) {
                return 0.0;
            }
            if (!stitcherRule.otherTypeId().equals(candidateElement.getTypeId())) {
                return 0.0;
            }
            try {
                return (stitcherRule.matches(baseElement, candidateElement) ? 1.0 : 0.0);
            } catch (ClassCastException e) {
                LOG.info("Controlled ClassCastException when stitching two items. Result: No Stitch.");
                return 0.0;
            }
        }

    }

    // CLASSES - END -------------------------------------------------------------------------------

    // VARIABLES -----------------------------------------------------------------------------------

    private static final Log LOG = LogFactory.getLog(SimpleStitcherRuleManagerImpl.class);

    private Map<String, StitcherRulePair> rulesById;
    private Map<String, Collection<StitcherRule<? extends Item, ? extends Item>>> rulesByTypeId;
    private Map<String, Map<String, LoomMicroStitcher>> microStitchers;

    // VARIABLES - END -----------------------------------------------------------------------------

    // CONSTRUCTORS --------------------------------------------------------------------------------

    public SimpleStitcherRuleManagerImpl() {
        rulesById = new HashMap<String, StitcherRulePair>();
        rulesByTypeId = new HashMap<String, Collection<StitcherRule<? extends Item, ? extends Item>>>();
        microStitchers = new HashMap<String, Map<String, LoomMicroStitcher>>();
    }

    // CONSTRUCTORS - END --------------------------------------------------------------------------

    // METHODS -------------------------------------------------------------------------------------

    @Override
    public void addStitcherRulePair(final String ruleId, final StitcherRulePair rule) {
        LOG.info("Adding StitcherRulePair");

        // TODO Put in checks for rule id clashes
        rulesById.put(ruleId, rule);

        // Add both rules by Type ID
        List<StitcherRule<?, ?>> leftAndRight = Arrays.asList(rule.getLeft(), rule.getRight());
        for (StitcherRule<?, ?> currentRule : leftAndRight) {
            String currentTypeId = currentRule.sourceTypeId();

            // Add rule to index
            retrieveRulesCollection(currentTypeId, true).add(currentRule);

            // Add rule to its corresponding microstitchers (as StitchChecker)
            retrieveMicroStitcher(currentRule).addStitchChecker(new RuleAsStitchChecker(currentRule));
        }
    }

    @Override
    public void removeStitcherRulePair(final String id) {
        LOG.info("Removing StitcherRulePair with id " + id);

        // Delete from <rulesById>
        StitcherRulePair toBeDeleted = rulesById.remove(id);
        // Method finishes if no such rule pair existed
        if (toBeDeleted == null) {
            return;
        }

        // Delete both rules from <rulesByTypeId>
        List<StitcherRule<?, ?>> leftAndRight = Arrays.asList(toBeDeleted.getLeft(), toBeDeleted.getRight());
        for (StitcherRule<?, ?> currentRule : leftAndRight) {
            String currentTypeId = currentRule.sourceTypeId();

            // Watch out! This will throw a NullPointerException if there is no rules collection for
            // this type ID! However, if the rules have been added correctly, this should never
            // happen.
            retrieveRulesCollection(currentTypeId, false).remove(currentRule);

            // Check whether the microstitcher related to this rule has no other rules left. In such
            // case, delete it.
            LoomMicroStitcher microstitcher = retrieveMicroStitcher(currentRule);
            if (microstitcher.getStitchCheckers().isEmpty()) {
                microStitchers.remove(currentTypeId);
            }
        }

    }

    /*
     * Analogous to getRelevantSourceRules(String)
     */
    public Collection<StitcherRule<? extends Item, ? extends Item>> getRulesByTypeId(final String typeId) {
        return rulesByTypeId.get(typeId);
    }

    public Map<String, LoomMicroStitcher> getMicroStitchersByTypeId(final String sourceTypeId) {
        Map<String, LoomMicroStitcher> toBeReturned = microStitchers.get(sourceTypeId);
        return (toBeReturned == null ? Collections.emptyMap() : toBeReturned);
    }

    public Collection<StitchRule> parseRules(final String rulesStr) {
        // TODO Auto-generated method stub
        return new ArrayList<StitchRule>();
    }

    public StitchRule parseRule(final String ruleStr) {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String, LoomMicroStitcher> getMicroStitchers(final String typeId) {
        return microStitchers.get(typeId);
    }

    private Collection<StitcherRule<?, ?>> retrieveRulesCollection(final String typeId, final boolean createNew) {
        Collection<StitcherRule<?, ?>> coll = rulesByTypeId.get(typeId);

        // No rules of this type have been added yet -> Create new collection?
        if (coll == null && createNew) {
            coll = new ArrayList<StitcherRule<? extends Item, ? extends Item>>();
            rulesByTypeId.put(typeId, coll);
        }

        return coll;
    }

    private LoomMicroStitcher retrieveMicroStitcher(final StitcherRule<?, ?> rule) {
        // Get source and destination type IDs
        String srcTypeId = rule.sourceTypeId();
        String destTypeId = rule.otherTypeId();

        // Retrieve microstitcher map (create it if it did not exist yet)
        Map<String, LoomMicroStitcher> microStitchersByTypeId = getMicroStitchers(srcTypeId);
        if (microStitchersByTypeId == null) {
            // First rule with this source type ID -> Create new map for its microstitchers
            microStitchersByTypeId = new HashMap<String, LoomMicroStitcher>();
            microStitchers.put(srcTypeId, microStitchersByTypeId);
        }

        // Retrieve microstitcher (create it if it did not exist yet)
        LoomMicroStitcher microStitcher = microStitchersByTypeId.get(destTypeId);
        if (microStitcher == null) {
            microStitcher = createLoomMicroStitcher();
            microStitchersByTypeId.put(destTypeId, microStitcher);
        }

        return microStitcher;
    }

    protected LoomMicroStitcher createLoomMicroStitcher() {
        return new LoomMicroStitcher();
    }

    @Override
    public Collection<StitcherRulePair> getRules() {
        // TODO Auto-generated method stub
        return rulesById.values();
    }

    // METHODS - END -------------------------------------------------------------------------------

}
