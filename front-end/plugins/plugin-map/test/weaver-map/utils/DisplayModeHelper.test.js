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
  var Thread = require('weft/models/Thread');
  var DisplayMode = require('plugins/common/utils/DisplayMode');

  describe('DisplayMode', function () {

    // To test the DisplayMode, we need
    beforeEach(function () {

      // ... a Thread ...
      this.thread = new Thread({
        id: 'thread',
        name: 'Thread',
        itemType: {
          attributes: {
            'lat': {
              name: 'Latitude',
              plottable: true
            },
            'lon': {
              name: 'Longitude',
              plottable: true
            }
          },
          geoAttributes: [{
            'latitude': 'lat',
            'longitude': 'lon'
          }]
        }
      });

      // ... and a DisplayMode object.
      this.displayMode = new DisplayMode({
        thread: this.thread,
      });

      this.possibleMode = [DisplayMode.CLASSIC, DisplayMode.MAP];
    });

    it('Should contains CLASSIC and MAP constants', function () {
      expect(DisplayMode.CLASSIC).to.equal('classic');
      expect(DisplayMode.MAP).to.equal('map');
    });

    describe('hasManyDisplayMode()', function () {

      it('Should return true', function () {
        expect(this.displayMode.hasManyDisplayMode()).to.equal(true);
      });
    });

    describe('getPossibleDisplayModeArray()', function () {

      it('Should contains all display modes supported by the model', function () {

        _.forEach(this.possibleMode, function (mode) {
          expect(this.displayMode.getPossibleDisplayModeArray()).to.contain(mode);
        }, this);
      });
    });

  });

});
