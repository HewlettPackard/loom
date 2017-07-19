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

import java.util.function.ToDoubleFunction;

import com.hp.hpl.loom.stitcher.IndexableStitcherRule;
import com.hp.hpl.loom.stitcher.StitcherRule;

abstract class PolygonMeasureComparator<Src extends ColouredPolygon, Dest extends ColouredPolygon>
        implements StitcherRule<Src, Dest>, IndexableStitcherRule<Src> {

    private boolean indexable;
    private ToDoubleFunction<ColouredPolygon> function;
    private double delta;

    public PolygonMeasureComparator(final ToDoubleFunction<ColouredPolygon> function, final double delta,
            final boolean indexable) {
        this.function = function;
        this.delta = delta;
        this.indexable = indexable;
    }

    @Override
    public boolean matches(final Src from, final Dest to) {
        return Math.abs(function.applyAsDouble(from) - function.applyAsDouble(to)) <= delta;
    }

    @Override
    public boolean isIndexable() {
        return false;
    }

    @Override
    public String indexValue(final Src from) {
        return Long.toString(Math.round(function.applyAsDouble(from)));
    }

    @Override
    public String otherIndexValue(final Src from) {
        return Long.toString(Math.round(function.applyAsDouble(from)));
    }


    public double getDelta() {
        return delta;
    }

    public void setDelta(final double delta) {
        this.delta = delta;
    }

    public static double getArea(final ColouredPolygon p) {
        return p.getArea();
    }

    public static double getPerimeter(final ColouredPolygon p) {
        return p.getPerimeter();
    }

    public static double getRefLength(final ColouredPolygon p) {
        return p.getRefLenght();
    }
}
