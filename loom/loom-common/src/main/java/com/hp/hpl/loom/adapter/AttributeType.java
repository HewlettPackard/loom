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

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * Class to model the attributeType - NUMERIC or LITERAL.
 */
@JsonAutoDetect
public class AttributeType {

    /**
     * Numeric type.
     */
    public static final String NUMERIC = "numeric";
    /**
     * Literal type.
     */
    public static final String LITERAL = "literal";

    private String name;
    private boolean mappable;

    /**
     * Constructor for the AttributeType that takes the name and mappable flag.
     *
     * @param name the AttributeType name
     * @param mappable is the attribute mappable
     */
    public AttributeType(final String name, final boolean mappable) {
        super();
        if (name == null || name.isEmpty()) {
            this.name = LITERAL; // assume default one
        } else {
            this.name = name;
        }
        this.mappable = mappable;
    }

    /**
     * Get the attributeType name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the attributeType name.
     *
     * @param name the name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Is the attributeType mappable.
     *
     * @return true if mappable.
     */
    public boolean isMappable() {
        return mappable;
    }

    /**
     * Set the mappable flag.
     *
     * @param mappable true if mappable.
     */
    public void setMappable(final boolean mappable) {
        this.mappable = mappable;
    }
}
