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
import com.hp.hpl.loom.adapter.annotations.ItemTypeInfo;
import com.hp.hpl.loom.adapter.annotations.LoomAttribute;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.model.Item;

@ItemTypeInfo(value = TestType.TYPE_LOCAL_ID)
public class TestItem extends Item {
    @LoomAttribute(key = "size", supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
            plottable = true, visible = true, type = NumericAttribute.class)
    private Long size;

    @LoomAttribute(key = "sizes", supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
            plottable = true, visible = true, type = NumericAttribute.class)
    private Long[] sizes;

    /**
     * @return the size
     */
    public Long getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(final Long size) {
        this.size = size;
    }

    /**
     * @return the sizes
     */
    public Long[] getSizes() {
        return sizes;
    }

    /**
     * @param sizes the sizes to set
     */
    public void setSizes(final Long[] sizes) {
        this.sizes = sizes;
    }


}
