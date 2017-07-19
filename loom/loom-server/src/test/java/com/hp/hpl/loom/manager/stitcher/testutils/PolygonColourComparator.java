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
package com.hp.hpl.loom.manager.stitcher.testutils;

import com.hp.hpl.loom.stitcher.IndexableStitcherRule;
import com.hp.hpl.loom.stitcher.StitcherRule;

public abstract class PolygonColourComparator<Src extends ColouredPolygon, Dest extends ColouredPolygon>
        implements StitcherRule<Src, Dest>, IndexableStitcherRule<Src> {

    private boolean indexable;

    public PolygonColourComparator(final boolean indexable) {
        this.indexable = indexable;
    }

    @Override
    public boolean matches(final Src from, final Dest to) {
        return from.getColour().equals(to.getColour());
    }

    @Override
    public boolean isIndexable() {
        return indexable;
    }

    //
    // IndexableStitcherRule methods

    @Override
    public String indexKey() {
        return "colour";
    }

    @Override
    public String indexValue(final Src source) {
        return source.getColour().toString();
    }

    @Override
    public String otherIndexKey() {
        return "colour";
    }

    @Override
    public String otherIndexValue(final Src source) {
        return source.getColour().toString();
    }
}
