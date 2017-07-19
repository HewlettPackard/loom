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
/* global describe, it, sinon, expect, beforeEach, afterEach */
/* jshint expr: true */
define(function (require) {
  "use strict";

  //var d3 = require('d3');
  var _ = require('lodash');
  var Item = require('weft/models/Item');
  var Operation = require('weft/models/Operation');
  var ItemType = require('weft/models/ItemType');
  var Thread = require('weft/models/Thread');
  var ThreadMapView = require('weaver-map/views/ThreadMapView');
  var MapDataManager = require('weaver-map/models/map/MapDataManager');
  var ClusterOperation = require('weaver-map/models/ClusterOperation');

  var QueryAutoUpdaterMapOperations = require('weaver-map/models/QueryAutoUpdaterMapOperations');

  describe('QueryAutoUpdaterMapOperations', function () {

    // To test the QueryAutoUpdaterMapOperations, we need
    beforeEach(function () {

      var operations = {};
      operations[Operation.GRID_CLUSTERING_ID] = ['lon', 'lat'];
      operations[Operation.FILTER_BY_REGION_ID] =  ['lon', 'lat'];
      operations[Operation.KMEANS_ID] = ['lon', 'lat'];
      operations[Operation.POLYGON_CLUSTERING_ID] = ['lon', 'lat'];

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
          operations: operations,
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

      this.thread.resetElements(_.toArray(this.elements));

      this.thread.set('itemClusterBy', {
        FILTER_BY_REGION: new ClusterOperation({
          operator: Operation.FILTER_BY_REGION_ID,
          attributes: {latitude: '', longitude: '' },
        }),
        GRID_CLUSTERING: new ClusterOperation({
          operator: Operation.GRID_CLUSTERING_ID,
          attributes: {latitude: '', longitude: '' },
        }),
        POLYGON_CLUSTERING: new ClusterOperation({
          operator: Operation.POLYGON_CLUSTERING_ID,
          attributes: {latitude: '', longitude: ''},
        })
      });

      this.thread.pushOperation({
        operator: 'FAKE_OP'
      });


      // ... a ThreadMapView ...
      this.threadView = new ThreadMapView({
        model: this.thread,
      });

      this.threadView.render();
      this.threadView.$el.appendTo(document.body);

      MapDataManager.get(this.threadView.map).set('mapIsReady', true);

      // ... a MapData ...
      this.mapData = MapDataManager.get(this.threadView.map);

      // ... and a QueryAutoUpdaterMapOperations.
      this.queryUpdater = this.thread.get('queryUpdater');
    });

    afterEach(function () {
      this.threadView.remove();
    });

    describe('The query updater', function () {

      it('Should be of type QueryAutoUpdaterMapOperations for the ThreadMapView', function () {

        expect(this.queryUpdater instanceof QueryAutoUpdaterMapOperations).to.be.true;
      });
    });

    describe('Query update', function () {

      it('Should not be done when the query has no FILTER_BY_REGION operations and only filterRegion has changed.',
        function () {

        var callback = function () {
          // This code is not supposed to be reachable
          expect(true).to.be.false;
        };

        var spycallback = sinon.spy(callback);

        var newQuery = this.thread.get('query').removeOperations(Operation.FILTER_BY_REGION_ID);
        this.thread.set('query', newQuery);
        this.thread.on('change:query', spycallback);

        //this.mapData.trigger('change:gridCluster', this.mapData.get('gridCluster'));
        this.mapData.trigger('change:filterRegion', this.mapData.get('filterRegion'));

        expect(this.mapData.get('gridCluster')).not.to.be.undefined;
        expect(this.mapData.get('filterRegion')).not.to.be.undefined;

        expect(spycallback).not.to.have.been.called;
      });

      it('Should not be done when the query has GRID_CLUSTERING disabled and only gridCluster has changed.',
        function () {

        var callback = function () {
          // This code is not supposed to be reachable
          expect(true).to.be.false;
        };

        var spycallback = sinon.spy(callback);

        var newQuery = this.thread.get('query').limitWith(undefined);
        this.thread.set('query', newQuery);
        this.thread.on('change:query', spycallback);

        this.mapData.trigger('change:gridCluster', this.mapData.get('gridCluster'));
        //this.mapData.trigger('change:filterRegion', this.mapData.get('filterRegion'));

        expect(this.mapData.get('gridCluster')).not.to.be.undefined;
        expect(this.mapData.get('filterRegion')).not.to.be.undefined;

        expect(spycallback).not.to.have.been.called;
      });

      it('Should be done when changing the gridCluster while the query has an active GRID_CLUSTERING',
       function () {
        var self = this;
        var counter = 0;
        var callback = function (thread, query) {
          expect(query).to.equal(self.thread.get('query'));
          counter++;
        };

        this.thread.on('change:query', callback);

        this.mapData.trigger('change:gridCluster', this.mapData.get('gridCluster'));

        expect(counter).to.equal(1);
        expect(this.thread.hasLimit(Operation.GRID_CLUSTERING_ID)).to.be.true;
        this.thread.off('change:query');
      });


      it('Should be done when changing the filterRegion while the query has at least one FILTER_BY_REGION',
        function () {
        var self = this;
        var counter = 0;
        var callback = function (thread, query) {
          expect(query).to.equal(self.thread.get('query'));
          counter++;
        };

        this.thread.on('change:query', callback);

        this.mapData.trigger('change:filterRegion', this.mapData.get('filterRegion'));

        expect(counter).to.equal(1);
        expect(this.thread.hasOperation(Operation.FILTER_BY_REGION_ID)).to.be.true;
        this.thread.off('change:query');
      });
    });

    // Not appening anymore as now Loom is handling the low number of aggregation
    // it('Should update the Query when reset:Elements is called.', function (done) {
    //   var self = this;
    //   var counter = 0;
    //   var callback = function (thread, query) {
    //     expect(query).to.equal(self.thread.get('query'));
    //     counter++;
    //   };

    //   this.thread.on('change:query', callback);
    //   this.thread.resetElements(_.times(200, function () { return this.elements.a1; }, this));

    //   setTimeout(_.bind(function () {
    //     expect(counter).to.equal(1);
    //     this.thread.off('change:query');
    //     done();
    //   }, this), 600);
    // });

  });
});