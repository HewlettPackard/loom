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
package com.hp.hpl.loom.stitcher;

import com.hp.hpl.loom.model.Item;

/**
 * A StitcherRulePair is a container for two complementary StitcherRule instances that define
 * equivalence between two types of Item, A and B. One rule defines equivalence from an item of type
 * A to candidate items of type B. The other rule defines an equivalence rule when comparing an item
 * of type B to candidate items of type A.
 *
 * @param <A> Type of one collection of items to be compared.
 * @param <B> Type of other collection of items that contains possibly equivalent items.
 */
public class StitcherRulePair<A extends Item, B extends Item> {
    private String id;
    private StitcherRule<A, B> left;
    private StitcherRule<B, A> right;

    /**
     * Constructor for StitcherRulePair, supplying the A to B, and complementary B to A rules. A
     * unique ID needs to be supplied to identify the rule.
     *
     * @param id Unique ID of the rule.
     * @param left StitcherRule defining equivalence between an item of type A with candidate items
     *        of type B.
     * @param right StitcherRule defining equivalence between an item of type B with candidate items
     *        of type A.
     */
    public StitcherRulePair(final String id, final StitcherRule<A, B> left, final StitcherRule<B, A> right) {
        this.id = id;
        this.left = left;
        this.right = right;
    }

    /**
     * Get the unique ID of the rule pair.
     *
     * @return unique ID of the rule pair.
     */
    public String getId() {
        return id;
    }

    /**
     * Get StitcherRule defining equivalence between items of type A with items of type B.
     *
     * @return StitcherRule defining equivalence between items of type A with items of type B.
     */
    public StitcherRule<A, B> getLeft() {
        return left;
    }

    /**
     * Get StitcherRule defining equivalence between items of type B with items of type A.
     *
     * @return StitcherRule defining equivalence between items of type B with items of type A.
     */
    public StitcherRule<B, A> getRight() {
        return right;
    }
}
