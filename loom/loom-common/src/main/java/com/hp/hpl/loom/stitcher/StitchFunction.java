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

import java.util.function.BiPredicate;

import com.hp.hpl.loom.model.Item;

/**
 * Function-based implementation of StitcherRule.
 *
 * @param <Src> Class of source items that can be stitched by this rule.
 * @param <Dest> Class of destination items that can be stitched by this rule.
 */
public class StitchFunction<Src extends Item, Dest extends Item> implements StitcherRule<Src, Dest> {
    private String sourceTypeId;
    private String otherTypeId;
    private BiPredicate<Src, Dest> matchesFn;

    /**
     * Constructor.
     *
     * @param sourceTypeId Type ID of the source Items.
     * @param otherTypeId Type ID of the destination Items.
     * @param matches Function to indicate if a source and destination Item are equivalent.
     */
    public StitchFunction(final String sourceTypeId, final String otherTypeId, final BiPredicate<Src, Dest> matches) {
        this.sourceTypeId = sourceTypeId;
        this.otherTypeId = otherTypeId;
        matchesFn = matches;
    }

    @Override
    public String sourceTypeId() {
        return sourceTypeId;
    }

    @Override
    public String otherTypeId() {
        return otherTypeId;
    }

    @Override
    public boolean isIndexable() {
        return false;
    }

    @Override
    public boolean matches(final Src from, final Dest to) {
        return matchesFn.test(from, to);
    }
}
