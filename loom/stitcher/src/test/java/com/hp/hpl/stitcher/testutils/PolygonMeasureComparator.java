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
package com.hp.hpl.stitcher.testutils;

import java.util.function.ToDoubleFunction;

import com.hp.hpl.stitcher.StitchChecker;

public abstract class PolygonMeasureComparator implements StitchChecker<ColouredPolygon, ColouredPolygon> {

    private ToDoubleFunction<ColouredPolygon> function;
    private double delta;

    public PolygonMeasureComparator(ToDoubleFunction<ColouredPolygon> function, double delta) {
        this.function = function;
        this.delta = delta;
    }

    @Override
    public double checkStitch(ColouredPolygon baseElement, ColouredPolygon candidateElement) {
        return (Math.abs(function.applyAsDouble(baseElement) - function.applyAsDouble(candidateElement)) <= delta ? 1.0
                : 0.0);
    }

    public double getDelta() {
        return delta;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }

    public static double getArea(ColouredPolygon p) {
        return p.getArea();
    }

    public static double getPerimeter(ColouredPolygon p) {
        return p.getPerimeter();
    }

    public static double getRefLength(ColouredPolygon p) {
        return p.getRefLenght();
    }
}
