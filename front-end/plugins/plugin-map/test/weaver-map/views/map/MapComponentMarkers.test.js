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
  var Backbone = require('backbone');
  var Item = require('weft/models/Item');
  var Aggregation = require('weft/models/Aggregation');
  var Thread = require('weft/models/Thread');
  var MapViewElement = require('weaver-map/views/map/MapViewElementV2');
  var MapComponentMarkers = require('weaver-map/views/map/MapComponentMarkersV2');
  var MapDataManager = require('weaver-map/models/map/MapDataManager');
  var GeoAttributes = require('weaver-map/models/GeoAttributes');
  var DistortionController = require('weaver-map/views/map/DistortionController');
  var ConstantSizeCalculator = require('weaver-map/views/map/size/ConstantSizeCalculator');

  describe('MapComponentMarkers', function () {

    // To test the MapComponentMarkers, we need
    var beforeEachCus = function () {

      // ... a Thread ...
      this.thread = new Thread({
        id: 'thread',
        name: 'Thread',
        itemType: {
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
        }
      });

      // ... some elements ...
      var countries = this.countries = ['GB', 'TH', 'JP', 'FR'];
      this.elementIDs = _.range(0, 10);//['a1', 'a2', 'a3', 'b1', 'b2', 'b3', 'c1', 'd1', 'd2', 'e1', 'f1'];
      var elements = this.elements = {};
      _(this.elementIDs).forEach(function (elementID) {
        elements[elementID] = new Item({
          'l.logicalId': elementID,
          name: elementID,
          lon: (2 * Math.random() - 1) * 180,
          lat: (2 * Math.random() - 1) * 90,
          country: _.sample(countries),
        });
      }, this);

      var aggregations = this.aggregations = {};
      _(this.countries).forEach(function (country) {
        aggregations[country] = new Aggregation({
          'l.logicalId': country + "A",
          name: country,
          plottableAggregateStats: {
            'lon_avg': (2 * Math.random() - 1) * 180,
            'lat_avg': (2 * Math.random() - 1) * 90,
          }
        });

      }, this);

      // ... a MapViewElement ...
      //MapDataManager.clear();
      this.mapViewElement2 = new MapViewElement({
        model: this.thread,
      });

      this.mapViewElement2.render();

      // ... a MapData ...
      this.mapData2 = MapDataManager.get(this.mapViewElement2);

      this.distortion = new DistortionController();
      this.distortion.attach(this.mapViewElement2);

      // this.thread.initHiddenThread(this.mapViewElement);

      this.mapComponentMarkers = new MapComponentMarkers({
        map: this.mapViewElement2,
        thread: this.thread,
      });

      this.mapComponentMarkers.sizeCalculator = new ConstantSizeCalculator({
        value: 3
      });
    };

    describe('distortion related', function () {

      beforeEach(function () {
        beforeEachCus.apply(this, arguments);
      });

      describe('getPointsToMove()', function () {

        it('Should be called for computing a new distortion', function () {

          var spyGetPointsToMove = sinon.spy(this.mapComponentMarkers, 'getPointsToMove');
          this.mapData2.set('mapIsReady', true);

          this.mapData2.trigger('recomputeDistortion');

          expect(spyGetPointsToMove).to.have.been.called;
        });
      });

      describe('_updatePoints()', function () {

        it('Should be called when the map apply the distortion.', function () {

          var spyUpdatePoints = sinon.spy(this.mapComponentMarkers, '_updatePoints');
          this.mapData2.set('mapIsReady', true);

          // Fake a first distortion computation
          this.distortion.featuresDistortion = {}; // fake it
          this.mapComponentMarkers.markersList = [{
            marker: {lngLat: [0, 0]},
            model: new (Backbone.Model.extend({ alert: new Backbone.Model({}) }))
          }]; // fake some points
          this.mapComponentMarkers.markersRef = this.mapViewElement2.getD3Element()
            .append('circle')
            .data(this.mapComponentMarkers.markersList);
          this.mapComponentMarkers.markers = this.mapComponentMarkers.markersRef;
          this.mapData2.getPoints();
          // fake the result of the distortion
          this.distortion.pointsDistortion = [[1, 1]];
          this.distortion.extentsDistortion = {max: 3, min: 0};

          // Apply it
          this.mapData2.trigger('applyDistortion');

          expect(spyUpdatePoints).to.have.been.called;
        });

        it('Should not update the markers position if we have started a fibre update', function () {

          var spyUpdateSvgMarkers = sinon.spy(this.mapComponentMarkers, '_updateSvgMarkers');

          this.mapComponentMarkers.prepareFibreUpdate();
          this.mapComponentMarkers._updatePoints([]);

          expect(spyUpdateSvgMarkers).not.to.have.been.called;
        });
      });

      describe('_updateView()', function () {

        it('Should call setModel, setDatum and update the containerModel', function () {

          var fakeView = {
            setModel: sinon.spy(),
            setDatum: sinon.spy(),
          };

          this.mapComponentMarkers.containerModel = {
            addElementModel: _.noop
          };
          this.mapComponentMarkers._updateView(fakeView, {model: 'toto'});

          expect(fakeView.setModel).to.have.been.called;
          expect(fakeView.setModel.calledBefore(fakeView.setDatum)).to.be.true;
          expect(fakeView.setModel.calledWith('toto')).to.be.true;
        });
      });

      describe('_resetDistortion()', function () {

        it('Should be called when the map reset:distortion is triggered', function () {

          var spyResetDistortion = sinon.spy(this.mapComponentMarkers, '_resetDistortion');
          this.mapData2.set('mapIsReady', true);

          this.mapViewElement2.resetMap();

          expect(spyResetDistortion).to.have.been.called;
        });
      });
    });

  });


});