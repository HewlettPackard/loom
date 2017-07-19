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
 * Interface for Item Equivalence Rules rules that relate Items in a source collection with
 * candidate Items in a destination collection.
 *
 * @param <Src> Type of the source Items.
 * @param <Dest> Type of the destination Items.
 */
public interface StitcherRule<Src extends Item, Dest extends Item> {
    /**
     * Return the typeId of the source Items referred to by this rule.
     *
     * @return Type ID of source items for the rule.
     */
    String sourceTypeId();

    /**
     * Return the typeId of the destination Items referred to by this rule.
     *
     * @return Type ID of destination candidate items for the rule.
     */
    String otherTypeId();

    /**
     * Return true if the specified source and destination items are equivalent.
     *
     * @param from The source Item to match.
     * @param to The destination Item to match.
     * @return true if the two items are equivalent.
     */
    boolean matches(final Src from, final Dest to);

    /**
     * Return true if values referenced by a Rule can be indexed. If this method returns true, then
     * the implementing class must also implement {@link IndexableStitcherRule}.
     *
     * @return true if values referenced by a Rule can be indexed.
     */
    boolean isIndexable();
}
