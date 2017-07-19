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
define(function (require) {
  "use strict";

  var _ = require('lodash');

  /**
   * GeoAttributes store meta data for geographical location of a particular thread.
   * It allows to query the set of attributes that constitue a tuple (longitude, latitude, country)
   * and keep track of the current index being looked at.
   *
   * @class  GeoAttributes
   * @namespace models
   * @constructor
   */
  var GeoAttributes = function GeoAttributes(values) {
    this.geolocations = [];
    this.geoMeaningTable = {};
    this.currentIndex = 0;

    if (values && _.isArray(values)) {
      for (var i = 0; i < values.length; ++i) {
        var value = values[i];
        this.addMappingFor(value.longitude, value.latitude);
      }
    }
  };

  /**
   * Add a mapping for the given attributes names.
   * @param {String} longitudeAttrName is the name of the attribute that can be use as a longitude.
   * @param {String} latitudeAttrName  is the name of the attribute that can be use as a latitude.
   * @param {String} countryAttrName   is the name of the attribute that refer to a country.
   */
  GeoAttributes.prototype.addMappingFor = function (longitudeAttrName, latitudeAttrName, countryAttrName) {

    this.geolocations.push({
      longitude: longitudeAttrName,
      latitude: latitudeAttrName,
      country: countryAttrName,
    });

    // To speed up look up
    this.geoMeaningTable[longitudeAttrName] = 'longitude';
    this.geoMeaningTable[latitudeAttrName] = 'latitude';
  };

  /**
   * Retrieve the name of the attribute for a given geographical attribute name.
   * @param  {String} geoMeaning can be longitude, latitude or country.
   * @param  {Integer} index     [optional] is the index in the list of tuples.
   *                             By default will be the current index.
   * @return {String}            Returns the corresponding loom attribute name.
   */
  GeoAttributes.prototype.getAttributeName = function (geoMeaning, index) {
    index = index || this.currentIndex;
    return this.geolocations[index][geoMeaning];
  };

  /**
   * Retrieve the geographical attribute name for the given loom attribute.
   * @param  {String} attributeName is the name of the loom attribute.
   * @return {String}               Returns 'longitude', 'latitude' or 'country'.
   */
  GeoAttributes.prototype.getGeoMeaning = function (attributeName) {
    return this.geoMeaningTable[attributeName];
  };

  /**
   * Returns the country for a given fibre.
   * @param  {Fiber}   fibre is the fibre.
   * @param  {Integer} index [optional] is the index in the list of tuples.
   *                         By default will be the current index.
   * @return {String}        Returns the country where that fibre is.
   */
  GeoAttributes.prototype.getCountry = function (fibre, index) {
    index = index || this.currentIndex;

    return fibre.get(this.geolocations[index].country);
  };

  /**
   * Returns the location of the given fibre.
   * @param  {Fiber}   fibre is the fibre.
   * @param  {Integer} index [optional] is the index in the list of tuples.
   *                         By default will be the current index.
   * @return {Array}         Returns the array [float(longitude), float(latitude)].
   */
  GeoAttributes.prototype.getLngLat = function (fibre, index) {
    index = index || this.currentIndex;

    return [fibre.get(this.geolocations[index].longitude), fibre.get(this.geolocations[index].latitude)];
  };

  return GeoAttributes;
});
