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
/* global describe, it, sinon, expect, beforeEach, after, afterEach */
/* jshint expr: true */
define(function (require) {
  "use strict";

  var _ = require('lodash');
  var GeoAttributes = require('weaver-map/models/GeoAttributes');

  describe('GeoAttributes', function () {

    beforeEach(function () {

      this.geoAttributes = new GeoAttributes([{
        longitude: 'core.lo',
        latitude: 'core.la',
        country: 'country'
      }, {
        longitude: 'longitude',
        latitude: 'latitude',
        country: 'core.country'
      }]);
    });

    describe('constructor', function () {

      it('Should have two geographical mapping with the current index being 0', function () {
        expect(_.size(this.geoAttributes.geolocations)).to.equal(2);
        expect(_.size(this.geoAttributes.geoMeaningTable)).to.equal(4);
        expect(this.geoAttributes.currentIndex).to.equal(0);
      });
    });

    describe('addMappingFor()', function () {

      it('Should accept the last parameter to be missing', function () {
        var index = this.geoAttributes.geolocations.length;
        this.geoAttributes.addMappingFor('long', 'lat');
        expect(this.geoAttributes.geolocations[index]).to.eql({
          longitude: 'long',
          latitude: 'lat',
          country: 'country',
        });
      });
    });

    describe('getLngLat()', function () {

      it('Should return an array of float when given a fibre', function () {
        var fakeFibre = {
          get: function () { return 0; }
        };
        expect(this.geoAttributes.getLngLat(fakeFibre)).to.eql([0, 0]);
      });

      it('Should return the appropriate array based on the index given', function () {
        var fakeFibre = {
          get: function (attr) {
            switch (attr) {
            case 'core.lo':
              return 0;
            case 'core.la':
              return 1;
            case 'longitude':
              return 2;
            case 'latitude':
              return 3;
            }
          }
        };

        expect(this.geoAttributes.getLngLat(fakeFibre)).to.eql([0, 1]);
        expect(this.geoAttributes.getLngLat(fakeFibre, 1)).to.eql([2, 3]);
      });
    });

    describe('getGeoMeaning()', function () {

      it('Should return the appropriate geographical attribute name when given a loom attribute', function () {
        expect(this.geoAttributes.getGeoMeaning('core.lo')).to.equal('longitude');
        expect(this.geoAttributes.getGeoMeaning('core.la')).to.equal('latitude');
        expect(this.geoAttributes.getGeoMeaning('longitude')).to.equal('longitude');
        expect(this.geoAttributes.getGeoMeaning('latitude')).to.equal('latitude');
      });
    });

  });

});