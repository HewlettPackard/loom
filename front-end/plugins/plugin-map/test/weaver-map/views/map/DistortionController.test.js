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
  var Item = require('weft/models/Item');
  var Thread = require('weft/models/Thread');
  var MapViewElement = require('weaver-map/views/map/MapViewElementV2');
  var MapComponent = require('weaver-map/views/map/MapComponentV2');
  var MapDataManager = require('weaver-map/models/map/MapDataManager');
  var DistortionController = require('weaver-map/views/map/DistortionController');

  describe('DistortionController', function () {

    // To test the DistortionController, we need
    beforeEach(function () {

      // ... a Thread ...
      this.thread = new Thread({
        id: 'thread',
        name: 'Thread',
        geoAttributes: [{
          latitude: "lat",
          longitude: "lon",
        }],
      });

      // ... some elements ...
      var countries = this.countries = ['GB', 'TH', 'JP', 'FR'];
      this.elementIDs = _.range(0, 10);//['a1', 'a2', 'a3', 'b1', 'b2', 'b3', 'c1', 'd1', 'd2', 'e1', 'f1'];
      var elements = this.elements = {};
      _(this.elementIDs).forEach(function (elementID) {
        elements[elementID] = new Item({
          'l.logicalId': elementID,
          name: elementID,
          //attributes: {
          lon: (2 * Math.random() - 1) * 180,
          lat: (2 * Math.random() - 1) * 90,
          country: _.sample(countries),
          //},
        });
      }, this);


      // ... a MapViewElement ...
      this.mapViewElement = new MapViewElement({
        model: this.thread,
      });

      this.mapViewElement.render();

      // ... a MapData ...
      this.mapData = MapDataManager.get(this.mapViewElement);
      this.mapData.set('mapIsReady', true);

      // ... and a DistortionController.
      this.distortionController = new DistortionController({
        map: this.mapViewElement,
      });
    });

    afterEach(function () {
      // TODO: allow those call to be made with phantomJS
      //this.distortionController.remove();
      //this.mapViewElement.remove();
    });

    describe('_addItemToCountry', function () {

      it('Should be called when add:(thread,fibre) is triggered', sinon.test(function () {

        var anEventDispatcher = new MapComponent({ map: this.mapViewElement });

        var spyAddFibreToCountry = this.spy(this.distortionController, '_addItemToCountry');

        anEventDispatcher.triggerMapEvent('add:(thread,fibre)', this.thread, this.elements[0]);

        expect(spyAddFibreToCountry).to.have.been.called;
      }));

    });

    describe('getNbItemsPerCountry', function () {

      it('Should return a map of country with the number of item in the country',  function () {

        // Add fibers:
        var anEventDispatcher = new MapComponent({ map: this.mapViewElement });

        var countries = _.reduce(this.countries, function (res, num) { res[num] = 0; return res; },  {});

        countries[this.elements[0].attributes.country] += 1;
        countries[this.elements[1].attributes.country] += 1;
        countries[this.elements[2].attributes.country] += 1;
        countries[this.elements[3].attributes.country] += 1;

        countries = _.omit(countries, function (value) { return value === 0; });
        countries['default'] = 0;

        anEventDispatcher.triggerMapEvent('add:(thread,fibre)', this.thread, this.elements[0]);
        anEventDispatcher.triggerMapEvent('add:(thread,fibre)', this.thread, this.elements[1]);
        anEventDispatcher.triggerMapEvent('add:(thread,fibre)', this.thread, this.elements[2]);
        anEventDispatcher.triggerMapEvent('add:(thread,fibre)', this.thread, this.elements[3]);

        expect(this.distortionController.getNbItemsPerCountry()).to.eql(countries);
      });
    });

  });

});