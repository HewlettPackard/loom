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

import com.hp.hpl.stitcher.Identifiable;

abstract class ColouredPolygon implements Identifiable<String> {

    protected String id;
    protected Colour colour;
    protected double area, refLenght;

    public ColouredPolygon(final String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public ColouredPolygon setId(String id) {
        this.id = id;
        return this;
    }

    public Colour getColour() {
        return colour;
    }

    public ColouredPolygon setColour(Colour colour) {
        this.colour = colour;
        return this;
    }

    public double getArea() {
        return area;
    }

    public double getRefLenght() {
        return refLenght;
    }

    @Override
    public String toString() {
        return getId();
    }

    public abstract ColouredPolygon setArea(double area);

    public abstract ColouredPolygon setRefLenght(double refLenght);

    public abstract double getPerimeter();

    public abstract ColouredPolygon setPerimeter(double perimeter);

    public abstract String kind();
}
