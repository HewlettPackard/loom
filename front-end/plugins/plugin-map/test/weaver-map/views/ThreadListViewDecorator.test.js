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
  var ItemType = require('weft/models/ItemType');
  var DisplayMode = require('plugins/common/utils/DisplayMode');
  var FilterService = require('weft/services/FilterService');
  var ThreadListView = require('weaver/views/ThreadListView');
  var ClusterOperation = require('weaver-map/models/ClusterOperation');
  var ThreadView = require('weaver/views/ThreadView');
  var GeoAttributes = require('weaver-map/models/GeoAttributes');

  describe('ThreadListViewDecorator (regression test)', function () {

    // To test the ThreadMapView, we need
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

      this.filterService = new FilterService();

      // ... and a ThreadMapView.
      this.threadListView = new ThreadListView({
        filterService: this.filterService,
      });
      this.threadListView.stopListening(this.threadListView.model);
      this.spyDisplay = sinon.spy(this.threadListView, 'displayThread');
      this.threadListView._registerModelListeners();
    });

    describe('API changes', function () {

      it('Should create a ThreadView even when we add a Thread with geoAttributes', function () {

        var spy = sinon.spy(this.threadListView, 'createView');

        this.threadListView.model.add(this.thread);

        expect(spy).to.have.been.called;
        expect(spy.returnValues[0] instanceof ThreadView).to.be.true;
        expect(this.spyDisplay).to.have.been.called;
        expect(this.spyDisplay.callCount).to.equal(1);
        expect(this.thread.get('displayMode')).to.equal(DisplayMode.CLASSIC);
      });
    });
  });

});
