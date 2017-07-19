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
package com.hp.hpl.loom.manager.query.filter.element;

import java.util.Collection;
import java.util.Map;

/**
 * A simple variable name expression like "abc".
 */
public class NameElement implements Element {
    private final String name;

    /**
     * Basic contructor with just a name.
     *
     * @param name the name element
     */
    public NameElement(final String name) {
        this.name = name;
    }

    /**
     * Get the elements name.
     *
     * @return the element name
     */
    public String getName() {
        return name;
    }

    @Override
    public void buildExpression(final StringBuilder builder) {
        builder.append(name);
    }

    @Override
    public boolean match(final Map<String, Object> values) {
        Collection<Object> v = values.values();
        for (Object obj : v) {
            if (obj.toString().matches(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return name;
    }


}
