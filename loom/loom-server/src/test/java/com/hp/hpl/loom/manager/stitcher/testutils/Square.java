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

import com.hp.hpl.loom.model.ItemType;

class Square extends ColouredPolygon {

    public Square(final String logicalId, final ItemType type) {
        super(logicalId, type);
    }

    // @Override
    // public Square setId(String id) {
    // return (Square) super.setId(id);
    // }

    @Override
    public Square setArea(double area) {
        this.area = area;
        this.refLenght = Math.sqrt(area);
        return this;
    }

    @Override
    public Square setRefLenght(double refLenght) {
        this.refLenght = refLenght;
        this.area = refLenght * refLenght;
        return this;
    }

    @Override
    public double getPerimeter() {
        return 4 * refLenght;
    }

    @Override
    public Square setPerimeter(double perimeter) {
        setRefLenght(perimeter / 4);
        return this;
    }

    @Override
    public String kind() {
        return "Square";
    }
}
