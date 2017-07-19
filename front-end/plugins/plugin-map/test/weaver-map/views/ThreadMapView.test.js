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
  var Aggregation = require('weft/models/Aggregation');
  var Thread = require('weft/models/Thread');
  var ItemType = require('weft/models/ItemType');
  var ThreadView = require('weaver/views/ThreadView');
  var ClusterOperation = require('weaver-map/models/ClusterOperation');
  var ThreadMapView = require('weaver-map/views/ThreadMapView');
  var GeoAttributes = require('weaver-map/models/GeoAttributes');
  var DisplayMode = require('plugins/common/utils/DisplayMode');

  describe('ThreadMapView', function () {

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


      // ... and a ThreadMapView.
      this.threadMapView = new ThreadMapView({
        model: this.thread,
      });

      document.body.appendChild(this.threadMapView.el);
    });

    afterEach(function () {
      document.body.removeChild(this.threadMapView.el);
    });

    describe('Focus', function () {


      it('Should set a default query auto updater when creating a sub thread', function () {

        var spy = sinon.spy(this.threadMapView, 'stopQueryMonitor');
        var fakeAggregation = new Aggregation({
          id: 'allo',
          displayMode: DisplayMode.MAP,
        });

        this.threadMapView.render();
        this.threadMapView.mapData.set('mapIsReady', true);

        this.threadMapView._toggleThreadDisplay(fakeAggregation);
        var subThread = this.threadMapView.subThread;
        subThread.model.set('focus', true);

        expect(spy).to.have.been.called;
      });
    });

    describe('Weaver "API" Changes', function () {

      it('Should only remove one binding from the original ThreadView', function () {

        var classesOrigina = ThreadView.prototype.bindings[':el'].classes;
        var classesMapView = _.clone(ThreadMapView.prototype.bindings[':el'].classes);
        classesMapView['mas-threadView-beingUpdated'] = classesOrigina['mas-threadView-beingUpdated'];

        expect(classesMapView).to.eql(classesOrigina);
      });

      describe('render()', function () {

        it('Should call the _renderElements, _renderMap methods.', function () {

          var _renderElementsSpy = sinon.spy(this.threadMapView, '_renderElements');
          var _renderMapSpy = sinon.spy(this.threadMapView, '_renderMap');

          this.threadMapView.render();

          expect(_renderElementsSpy).to.have.been.calledOnce;
          expect(_renderMapSpy).to.have.been.calledOnce;
        });
      });
    });

  });

});
