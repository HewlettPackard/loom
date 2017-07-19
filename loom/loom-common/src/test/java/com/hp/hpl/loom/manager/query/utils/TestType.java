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
package com.hp.hpl.loom.manager.query.utils;

import com.hp.hpl.loom.adapter.NumericAttribute;
import com.hp.hpl.loom.exceptions.AttributeException;
import com.hp.hpl.loom.model.SeparableItemType;

public class TestType extends SeparableItemType {
    /**
     * The provider type_local_id.
     */
    public static final String TYPE_LOCAL_ID = "testType";

    /**
     * No-arg constructor for a providerType.
     */
    public TestType() {
        super(TYPE_LOCAL_ID);
        try {
            NumericAttribute size = new NumericAttribute.Builder("size").name("size").min("0").max("1000000").unit("kb")
                    .visible(false).plottable(false).build();
            this.addAttributes(size);
            NumericAttribute sizes = new NumericAttribute.Builder("sizes").name("sizes").min("0").max("1000000")
                    .unit("kb").visible(true).plottable(true).build();
            this.addAttributes(sizes);
            // Attribute sizes = new
            // Attribute.Builder("sizes").name("sizes").visible(true).plottable(true).build();
            // this.addAttributes(sizes);
        } catch (AttributeException e) {
            e.printStackTrace();
        }
    }
}
