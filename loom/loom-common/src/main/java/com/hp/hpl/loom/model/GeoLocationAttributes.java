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
package com.hp.hpl.loom.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Class to hold the mappings for geo positions.
 */
public class GeoLocationAttributes {

    /**
     * Class to hold the mapping of which Item attribute holds the long / lat pair.
     *
     */
    private class GeoLocationAttribute {
        private String longitude;
        private String latitude;
        private String countryAttributeName;

        public GeoLocationAttribute(final String longitude, final String latitude, final String countryAttributeName) {
            this.longitude = longitude;
            this.latitude = latitude;
            this.countryAttributeName = countryAttributeName;
        }

        public void setLatitude(final String latitude) {
            this.latitude = latitude;
        }

        public void setLongitude(final String longitude) {
            this.longitude = longitude;
        }

        public String getLatitude() {
            return latitude;
        }

        public String getLongitude() {
            return longitude;
        }

        /**
         * @return the countryAttributeName
         */
        public String getCountryAttributeName() {
            return countryAttributeName;
        }

        /**
         * @param countryAttributeName the countryAttributeName to set
         */
        public void setCountryAttributeName(final String countryAttributeName) {
            this.countryAttributeName = countryAttributeName;
        }
    }

    @JsonInclude
    private List<GeoLocationAttribute> geoAttributes;

    /**
     * Init constructor.
     */
    public GeoLocationAttributes() {
        geoAttributes = new ArrayList<GeoLocationAttributes.GeoLocationAttribute>();
    }

    /**
     * Add the geo attributes.
     *
     * @param latitudeAttributeName the latitude name
     * @param longitudeAttributeName the longitude name
     * @param countryAttributeName the country name
     */
    public void addGeoAttributes(final String latitudeAttributeName, final String longitudeAttributeName,
            final String countryAttributeName) {
        geoAttributes
                .add(new GeoLocationAttribute(longitudeAttributeName, latitudeAttributeName, countryAttributeName));
    }

    /**
     * Gets the geo attributes.
     *
     * @return the geo attributes.
     */
    public List<GeoLocationAttribute> getGeoAttributes() {
        return geoAttributes;
    }
}
