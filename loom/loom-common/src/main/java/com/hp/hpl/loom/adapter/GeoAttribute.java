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
 * Class to model the Geo attributes.
 */
public class GeoAttribute extends Attribute {
    protected GeoAttribute(final Builder builder) {
        super(builder);
    }

    /**
     * The grouping for this attribute.
     *
     * @return the group
     */
    public String getGroup() {
        return (String) displayMap.get("group");
    }

    /**
     * Is it a latitude.
     *
     * @return is latitude
     */
    public boolean isLatitude() {
        return (boolean) displayMap.get("latitude");
    }

    /**
     * Is it a longitude.
     *
     * @return is longitude
     */
    public boolean isLongitude() {
        return (boolean) displayMap.get("longitude");
    }

    /**
     * Is it a country.
     *
     * @return is country
     */
    public boolean isCountry() {
        return (boolean) displayMap.get("country");
    }

    /**
     * A builder for the {@link GeoAttribute}.
     */
    public static class Builder extends Attribute.Builder {
        private String fieldName;

        /**
         * Constructor for the {@link GeoAttribute} using the fieldName.
         *
         * @param fieldName the field to construct for
         */
        @SuppressWarnings("checkstyle:magicnumber")
        public Builder(final String fieldName) {
            super(fieldName);
            this.fieldName = fieldName;
            displayMap.put("type", Attribute.TYPE_GEO);
            if (displayMap.get("country") != null && (boolean) displayMap.get("country")) {
                displayMap.put("plottable", false);
            } else {
                displayMap.put("plottable", true);
            }
            displayMap.put("min", -180);
            displayMap.put("max", 180);
            displayMap.put("unit", "°");
        }

        @Override
        public GeoAttribute build() throws AttributeException {
            return new GeoAttribute(this);
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
         * Set the group.
         *
         * @param group the group
         * @return the builder
         */
        public Builder group(final String group) {
            displayMap.put("group", group);
            return this;
        }

        /**
         * Set the longitude.
         *
         * @param longitude the longitude
         * @return the builder
         */
        public Builder longitude(final boolean longitude) {
            displayMap.put("longitude", longitude);
            return this;
        }

        /**
         * Set the latitude.
         *
         * @param latitude the latitude
         * @return the builder
         */
        public Builder latitude(final boolean latitude) {
            displayMap.put("latitude", latitude);
            return this;
        }

        /**
         * Set the country.
         *
         * @param country the country
         * @return the builder
         */
        public Builder country(final boolean country) {
            displayMap.put("country", country);
            return this;
        }
    }


}
