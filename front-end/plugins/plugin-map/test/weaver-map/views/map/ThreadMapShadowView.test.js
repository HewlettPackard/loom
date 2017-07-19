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
  var ItemType = require('weft/models/ItemType');
  var Item = require('weft/models/Item');
  var Thread = require('weft/models/Thread');

  var MapViewElement = require('weaver-map/views/map/MapViewElementV2');
  var ThreadMapShadowView = require('weaver-map/views/map/ThreadMapShadowView');

  describe('ThreadMapShadowView', function () {

    // To test the ThreadMapShadowView, we need
    beforeEach(function () {

      // ... a Thread ...
      this.thread = new Thread({
        id: 'thread',
        name: 'Thread',
        itemType: new ItemType({
          attributes: {
            'name': {
              'name': 'Name',
              visible: true
            },
            'lon': {
              'name': 'Coord Longitude',
              visible: true,
              plottable: true
            },
            'lat': {
              'name': 'Coord Latitude',
              visible: true,
              plottable: true
            },
          },
          operations: {
            'GRID_CLUSTERING': ['lon', 'lat'],
            'FILTER_BY_REGION': ['lon', 'lat'],
            'KMEANS': ['lon', 'lat'],
            'POLYGON_CLUSTERING': ['lon', 'lat']
          },
          orderedAttributes: [
            'name',
            'lon',
            'lat'
          ],
          geoAttributes: [{
            latitude: "lat",
            longitude: "lon",
          }],
        })
      });

      // ... some elements ...
      this.elementIDs = ['a1', 'a2', 'a3', 'b1', 'b2', 'b3', 'c1', 'd1', 'd2', 'e1', 'f1'];
      var elements = this.elements = {};
      _(this.elementIDs).forEach(function (elementID) {
        elements[elementID] = new Item({
          'l.logicalId': elementID,
          name: elementID,
          attributes: {
            lon: (2 * Math.random() - 1) * 180,
            lat: (2 * Math.random() - 1) * 90,
          },
        });
      }, this);

      this.mapView = new MapViewElement({
        model: this.thread,
      });

      this.mapView.render();

      // ... and a ThreadMapShadowView.
      this.threadMapView = new ThreadMapShadowView({
        model: this.thread,
        map: this.mapView,
      });
    });

    describe('initialize', function () {

      it('Should have focus on creation.', function () {

        expect(this.thread.get('focus')).to.equal(true);
      });
    });

    describe('_actionsAfterRenderNewElements', function () {

      it('Should be called after a reset:elements on the thread.', function () {

        var spy = sinon.spy(this.threadMapView, '_actionsAfterRenderNewElements');

        this.thread.resetElements([]);

        expect(spy).to.have.been.called;
      });
    });
  });

});