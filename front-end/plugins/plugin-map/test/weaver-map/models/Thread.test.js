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
  var ClusterOperation = require('weaver-map/models/ClusterOperation');
  var GeoAttributes = require('weaver-map/models/GeoAttributes');

  describe('Thread (Decorated)', function () {

    beforeEach(function () {

    });

    afterEach(function () {

    });

    describe('getGeoAttributes()', function () {

      beforeEach(function () {
        this.thread = new Thread({
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
        })
      });

      it('Should return the geographic attributes for this Thread', function () {
        expect(this.thread.getGeoAttributes()).to.eql(new GeoAttributes([{
          latitude: "lat",
          longitude: "lon",
        }]));

      });
    });
  });
});
