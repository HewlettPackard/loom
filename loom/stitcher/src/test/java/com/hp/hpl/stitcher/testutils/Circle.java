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

class Circle extends ColouredPolygon {

    public Circle(final String id) {
        super(id);
    }

    @Override
    public Circle setId(String id) {
        return (Circle) super.setId(id);
    }

    @Override
    public Circle setArea(double area) {
        this.area = area;
        this.refLenght = Math.sqrt(area / Math.PI);
        return this;
    }

    @Override
    public Circle setRefLenght(double refLenght) {
        this.refLenght = refLenght;
        this.area = Math.PI * refLenght * refLenght;
        return this;
    }

    @Override
    public double getPerimeter() {
        return 2 * Math.PI * refLenght;
    }

    @Override
    public Circle setPerimeter(double perimeter) {
        setRefLenght(perimeter / (2 * Math.PI));
        return this;
    }

    @Override
    public String kind() {
        return "Circle";
    }
}
