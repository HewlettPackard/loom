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
import java.util.function.Function;

import com.hp.hpl.loom.model.Item;

/**
 * Function-based implementation of StitcherRule that supports indexing via IndexableStitcherRule.
 *
 * @param <Src> Class of source items that can be stitched by this rule.
 * @param <Dest> Class of destination items that can be stitched by this rule.
 */
public class IndexableStitchFunction<Src extends Item, Dest extends Item> extends StitchFunction<Src, Dest>
        implements StitcherRule<Src, Dest>, IndexableStitcherRule<Src> {

    // Indexable
    private String indexKey;
    private Function<Src, String> indexValueFn;
    private String otherIndexKey;
    private Function<Src, String> otherIndexValueFn;

    /**
     * Constructor.
     *
     * @param sourceTypeId Type ID of the source Items.
     * @param otherTypeId Type ID of the destination Items.
     * @param matches Function to indicate if a source and destination Item are equivalent.
     * @param indexKey Indexable key for source items.
     * @param indexValue Function to return the index value for a source item.
     * @param otherIndexKey Indexable key for destination items.
     * @param otherIndexValue Function to return the expected index value for a matching destination
     *        item, given a source item as a parameter.
     */
    public IndexableStitchFunction(final String sourceTypeId, final String otherTypeId,
            final BiPredicate<Src, Dest> matches, final String indexKey, final Function<Src, String> indexValue,
            final String otherIndexKey, final Function<Src, String> otherIndexValue) {
        super(sourceTypeId, otherTypeId, matches);
        this.indexKey = indexKey;
        this.indexValueFn = indexValue;
        this.otherIndexKey = otherIndexKey;
        this.otherIndexValueFn = otherIndexValue;
    }


    // ////////////////////////////////////////
    // indexable methods
    // ////////////////////////////////////////

    @Override
    public boolean isIndexable() {
        return true;
    }

    @Override
    public String indexKey() {
        return indexKey;
    }

    @Override
    public String indexValue(final Src a) {
        return indexValueFn.apply(a);
    }

    @Override
    public String otherIndexKey() {
        return otherIndexKey;
    }

    @Override
    public String otherIndexValue(final Src a) {
        return otherIndexValueFn.apply(a);
    }
}
