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
package com.hp.hpl.loom.adapter;

import java.util.Set;
import java.util.TreeSet;

/**
 * OperationBuilder constructs Operations based on attributes.
 */
public class OperationBuilder {

    private Set<OrderedString> operations = new TreeSet<>();

    /**
     * Create a OperationBuilder using attribute(s).
     *
     * @param attributes attributes to add to the builder
     * @return the builder
     */
    public OperationBuilder add(final Attribute... attributes) {
        for (Attribute attribute : attributes) {
            operations.add(new OrderedString(attribute.getFieldName(), operations.size()));
        }
        return this;
    }

    /**
     * Create a OperationBuilder using attribute and order.
     *
     * @param attribute the attribute to add
     * @param order the order for that attribute
     * @return the builder
     */
    public OperationBuilder add(final Attribute attribute, final int order) {
        operations.add(new OrderedString(attribute.getFieldName(), order));
        return this;
    }

    /**
     * Returns the set of operations based on the additions above.
     *
     * @return the Set of operations.
     */
    public Set<OrderedString> build() {
        return operations;
    }
}
