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

import com.hp.hpl.loom.exceptions.AttributeException;


/**
 * Class to model the Time attributes. It is assumed that time is on time ISO format
 * (2016-01-07T21:40:20+00:00)
 */
public class TimeAttribute extends Attribute {
    protected TimeAttribute(final Builder builder) {
        super(builder);
    }

    /**
     * The format for this attribute.
     *
     * @return the format
     */
    public String getFormat() {
        return (String) displayMap.get("format");
    }

    /**
     * The short format for this attribute.
     *
     * @return the short format
     */
    public String getShortFormat() {
        return (String) displayMap.get("shortFormat");
    }

    /**
     * A builder for the {@link TimeAttribute}.
     */
    public static class Builder extends Attribute.Builder {
        private String fieldName;

        /**
         * Constructor for the {@link TimeAttribute} using the fieldName.
         *
         * @param fieldName the field to construct for
         */
        @SuppressWarnings("checkstyle:magicnumber")
        public Builder(final String fieldName) {
            super(fieldName);
            this.fieldName = fieldName;
            displayMap.put("type", Attribute.TYPE_TIME);
            displayMap.put("format", "");
            displayMap.put("shortFormat", "");

        }

        @Override
        public TimeAttribute build() throws AttributeException {
            return new TimeAttribute(this);
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
        public Builder type(final String type) {
            super.type(type);
            return this;
        }

        @Override
        public Builder mappable(final boolean mappable) {
            super.mappable(mappable);
            return this;
        }

        @Override
        public Builder collectionType(final CollectionType collectionType) {
            super.collectionType(collectionType);
            return this;
        }

        /**
         * Set the format.
         *
         * @param format the format
         * @return the builder
         */
        public Builder format(final String format) {
            displayMap.put("format", format);
            return this;
        }

        /**
         * Set the shortFormat.
         *
         * @param shortFormat the shortFormat
         * @return the builder
         */
        public Builder shortFormat(final String shortFormat) {
            displayMap.put("shortFormat", shortFormat);
            return this;
        }
    }
}
