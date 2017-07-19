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

  var ThreadMapView = require('weaver-map/views/ThreadMapView');
  var ClusterOperation = require('weaver-map/models/ClusterOperation');
  // var _ = require('lodash');
  // var Item = require('weft/models/Item');
  var ItemType = require('weft/models/ItemType');
  var Aggregation = require('weft/models/Aggregation');
  var Thread = require('weft/models/Thread');
  var MapDataManager = require('weaver-map/models/map/MapDataManager');
  var GeoAttributes = require('weaver-map/models/GeoAttributes');
  var DisplayMode = require('plugins/common/utils/DisplayMode');

  describe('MapViewElement', function () {

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
        }),
        displayMode: DisplayMode.MAP,
      });

      this.thread.set('itemClusterBy', {
        FILTER_BY_REGION: new ClusterOperation({
          operator: 'FILTER_BY_REGION',
          attributes: {lat: '', lon: '' },
        }),
        GRID_CLUSTERING: new ClusterOperation({
          operator: 'GRID_CLUSTERING',
          attributes: {lat: '', lon: '' },
        }),
        POLYGON_CLUSTERING: new ClusterOperation({
          operator: 'POLYGON_CLUSTERING',
          attributes: {lat: '', lon: ''},
        })
      });

      this.thread.pushOperation({
        operator: 'FAKE_OP'
      });

      // ... and a ThreadMapView.
      this.threadMapView = new ThreadMapView({
        model: this.thread,
      });

      this.threadMapView.render();

      document.body.appendChild(this.threadMapView.el);

      this.mapViewElement2 = this.threadMapView.map;

      this.mapData = MapDataManager.get(this.mapViewElement2);
      this.mapData.set('mapIsReady', true);

      this.thread.resetElements([new Aggregation({
        id: 'agg-' + 213132131,
        plottableAggregateStats: {
          'lat_avg': 0,
          'lon_avg': 0,
        }
      })]);

      this.clock = sinon.useFakeTimers();
    });

    afterEach(function () {
      document.body.removeChild(this.threadMapView.el);
      this.clock.restore();
    });

    describe('map:grow, map:shrink events', function () {

      it('Should change the size of the viewport', function () {

        var spyUpdateOnResize = sinon.spy(this.mapViewElement2, '_updateOnResize');

        this.mapData.trigger('map:shrink');
        this.clock.tick(1000);

        expect(spyUpdateOnResize).to.have.been.called;
      });

      it('Should do nothing if both are called', function () {

        var spyUpdateOnResize = sinon.spy(this.mapViewElement2, '_updateOnResize');

        this.mapData.trigger('map:shrink');
        this.mapData.trigger('map:grow');
        this.clock.tick(500);

        expect(spyUpdateOnResize).to.have.been.called;
      });

      it('Should trigger first change:transform when refresh:tracking is sent', function (done) {

        var spyChangeTransform = sinon.spy();

        this.mapData.on('change:transform', spyChangeTransform);
        this.mapViewElement2.getD3Element().on('refresh:tracking', function () {
          expect(spyChangeTransform).to.have.been.called;
          done();
        });
        this.mapData.trigger('map:shrink');
        this.clock.tick(1000);
      });
    });

  });

});
