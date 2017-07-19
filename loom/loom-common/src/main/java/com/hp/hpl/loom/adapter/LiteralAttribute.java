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

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.loom.exceptions.AttributeException;

/**
 * Class to model the literal attributes.
 */
public class LiteralAttribute extends Attribute {

    /**
     * Builds the LiteralAttribute from the provided {@link Builder}.
     *
     * @param builder the builder
     */
    public LiteralAttribute(final Builder builder) {
        super(builder);
    }

    /**
     * Get the range of values for this literal attribute.
     *
     * @return the range of attributes.
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getRange() {
        Object obj = displayMap.get("range");
        return (Map<String, String>) obj;
    }

    /**
     * A builder for the {@link LiteralAttribute}.
     */
    public static class Builder extends Attribute.Builder {
        /**
         * Constructor for the {@link LiteralAttribute} using the fieldName.
         *
         * @param fieldName the field to construct for
         */
        public Builder(final String fieldName) {
            super(fieldName);
            displayMap.put("type", Attribute.TYPE_LITERAL);
            if (displayMap.get("range") == null) {
                displayMap.put("range", new HashMap<String, String>());
            }
        }

        /**
         * Set the allowed values for this literal value.
         *
         * @param allowedValues the allowed value
         * @return a constructed {@link LiteralAttribute}
         */
        public Builder allowedValues(final Map<String, String> allowedValues) {
            if (allowedValues != null && !allowedValues.isEmpty()) {
                displayMap.put("range", allowedValues);
            }
            return this;
        }

        @Override
        public LiteralAttribute build() throws AttributeException {
            return new LiteralAttribute(this);
        }

        @Override
        public Builder name(final String name) {
            super.name(name);
            return this;
        }

        @Override
        public Builder visible(final boolean visible) {
            super.visible(visible);
            return this;
        }

        @Override
        public Builder plottable(final boolean plottable) {
            super.plottable(plottable);
            return this;
        }

        @Override
        public Builder mappable(final boolean mappable) {
            super.mappable(mappable);
            return this;
        }

        @Override
        public Builder type(final String type) {
            super.type(type);
            return this;
        }

        @Override
        public Builder collectionType(final CollectionType collectionType) {
            super.collectionType(collectionType);
            return this;
        }
    }
}
