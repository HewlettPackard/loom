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
  var MapDataManager = require('weaver-map/models/map/MapDataManager');

  describe('MapData', function () {

    // To test the MapData, we need
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


      // ... a MapViewElement ...
      this.mapViewElement = new MapViewElement({
        model: this.thread,
      });

      this.mapViewElement.render();

      // ... and a MapData.
      this.mapData = MapDataManager.get(this.mapViewElement);
    });

    afterEach(function () {
    });

    describe('Management of points (used for distortion)', function () {
      
      beforeEach(function () {

        var points = this.points = [];
        _(this.elements).forEach(function (element) {
          var value = element.get('attributes');
          points.push([value.lon, value.lat]);
        });

      });

      afterEach(function () {

      });

      it('Should unregister points', function () {

        var points = this.points;
        var res;

        this.mapData.registerPoints('id', points, points);
        
        res = this.mapData.getPoints();
        expect(res.length > 0).to.equal(true);

        this.mapData.unregisterPoints('id');
        res = this.mapData.getPoints();
        expect(res.length > 0).to.equal(false);
      });

      it('Should register points with callback mechanism', function () {

        var isCalled = false;
        var points = this.points;
        var newPoints = _.times(points.length, function (n) { return [n, n]; });

        this.mapData.registerPoints('id', function () {
          isCalled = true;
          return this.points;
        }, function (newPoints) {
          isCalled = true;
          expect(points.length).to.equal(newPoints.length);
        }, this);

        var res = this.mapData.getPoints();
        expect(isCalled).to.equal(true);
        expect(res).to.eql(points);

        isCalled = false;
        
        this.mapData.setPoints(newPoints);
        expect(isCalled).to.equal(true);

        this.mapData.unregisterPoints('id');
      });

      it('Should register points with raw access mechanism', function () {

        var points = this.points;
        var newPoints = _.times(points.length, function (n) { return [n, n]; });

        this.mapData.registerPoints('id', points, points);

        var res = this.mapData.getPoints();
        expect(res).to.eql(points);
                
        this.mapData.setPoints(newPoints);
        expect(points).to.eql(newPoints);

        this.mapData.unregisterPoints('id');
      });

      it('Should be possible to register points multiple times', function () {

        var points = this.points;
        var otherPoints = [[0, 1], [0, 2]];
        var n = otherPoints.length + points.length;
        var newPoints = _.times(n, function (i) { return [i, i]; });

        this.mapData.registerPoints('id', otherPoints, otherPoints);

        this.mapData.registerPoints('id2', function () {
          return this.points;
        }, function (value) {
          this.points = value;
        }, this);

        var res = this.mapData.getPoints();
        
        expect(
          _.isEqual(_.first(res, otherPoints.length), otherPoints) ||
          _.isEqual(_.last(res, otherPoints.length), otherPoints)
          ).to.equal(true);

        expect(
          _.isEqual(_.first(res, points.length), points) ||
          _.isEqual(_.last(res, points.length), points)
          ).to.equal(true);

        this.mapData.setPoints(newPoints);

        expect(
          _.isEqual(otherPoints, [[0, 0], [1, 1]]) ||
          _.isEqual(otherPoints, [[n - 1, n - 1], [n, n]])
          ).to.equal(true);

        this.mapData.unregisterPoints('id');
        this.mapData.unregisterPoints('id2');
      });
    });

  });
});