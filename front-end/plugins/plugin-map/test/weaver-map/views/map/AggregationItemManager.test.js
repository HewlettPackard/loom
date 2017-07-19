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
  var ItemType = require('weft/models/ItemType');
  var Aggregation = require('weft/models/Aggregation');
  var Thread = require('weft/models/Thread');
  var MapViewElement = require('weaver-map/views/map/MapViewElementV2');
  var MapComponentMarkers = require('weaver-map/views/map/MapComponentMarkersV2');
  var MapComponentCountries = require('weaver-map/views/map/MapComponentCountriesV2');
  var MapDataManager = require('weaver-map/models/map/MapDataManager');
  var AggregationItemManager = require('weaver-map/views/map/AggregationItemManager');
  var GeoAttributes = require('weaver-map/models/GeoAttributes');


  describe('AggregationItemManager', function () {

    // To test the AggregationItemManager, we need
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
            '/loom/loom/GRID_CLUSTERING': ['lon', 'lat'],
            '/loom/loom/FILTER_BY_REGION': ['lon', 'lat'],
            '/loom/loom/KMEANS': ['lon', 'lat'],
            '/loom/loom/POLYGON_CLUSTERING': ['lon', 'lat']
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

      // this.thread = new MetaThread({
      //   thread: this.thread
      // });

      // ... a MapViewElement ...
      this.mapViewElement = new MapViewElement({
        model: this.thread,
      });

      this.mapViewElement.render();

      // ... a MapData ...
      this.mapData = MapDataManager.get(this.mapViewElement);

      // this.thread.initHiddenThread(this.mapViewElement);

      this.itemsManager = new AggregationItemManager({
        map: this.mapViewElement,
        model: this.thread,
      });

      this.spyActiveComponent = sinon.spy(this.itemsManager, '_updateActiveComponent');
      this.mapData.set('mapIsReady', true);
    });

    afterEach(function () {

      this.spyActiveComponent.restore();
    });

    describe('_updateActiveComponent()', function () {

      it('Should switch to country display from markers when grouping by country', sinon.test(function () {

        expect(this.itemsManager.activeComponent instanceof MapComponentMarkers).to.be.true;

        this.thread.pushOperation({
          operator: '/loom/loom/GROUP_BY',
          parameters: {
            property: "country",
          }
        });

        expect(this.itemsManager.activeComponent instanceof MapComponentCountries).to.be.true;
        expect(this.spyActiveComponent).to.have.been.called;
      }));

      it('Should switch back to markers if grouping by country is removed', sinon.test(function () {

        expect(this.itemsManager.activeComponent instanceof MapComponentMarkers).to.be.true;

        this.thread.pushOperation({
          operator: '/loom/loom/GROUP_BY',
          parameters: {
            property: "country",
          }
        });

        expect(this.itemsManager.activeComponent instanceof MapComponentCountries).to.be.true;

        this.thread.removeOperations({
          operator: '/loom/loom/GROUP_BY',
          parameters: {
            property: "country",
          }
        });

        expect(this.itemsManager.activeComponent instanceof MapComponentMarkers).to.be.true;
      }));
    });

    describe('fillFromModel()', function () {

      it('Should make the proper calls on MapComponentMarkers when passed items', function (done) {

        var spyPutItem = sinon.spy(this.itemsManager.activeComponent, 'putItem');
        var spyPutAggregation = sinon.spy(this.itemsManager.activeComponent, 'putAggregation');
        var spyPrepareFibreUpdate = sinon.spy(this.itemsManager.activeComponent, 'prepareFibreUpdate');
        var spyFinishFibreUpdate = sinon.spy(this.itemsManager.activeComponent, 'finishFibreUpdate');
        var spyCancelFibreUpdate = sinon.spy(this.itemsManager.activeComponent, 'cancelFibreUpdate');

        var elements = _.toArray(this.elements);
        this.itemsManager.fillFromModel(elements)
        .then(function () {

          expect(spyPutItem).to.have.been.called;
          expect(spyPutItem.args.length).to.equal(elements.length);
          expect(spyPutAggregation).not.to.have.been.called;
          expect(spyFinishFibreUpdate).to.have.been.called;
          expect(spyFinishFibreUpdate.args.length).to.equal(1);
          expect(spyPrepareFibreUpdate).to.have.been.called;
          expect(spyPrepareFibreUpdate.args.length).to.equal(1);
          expect(spyCancelFibreUpdate).not.to.have.been.called;
          done();
        });
      });

      it('Should call cancelFibreUpdate when a second fill is started', function (done) {

        var spyCancelFibreUpdate = sinon.spy(this.itemsManager.activeComponent, 'cancelFibreUpdate');

        var aggregations = _.toArray(this.aggregations);
        this.itemsManager.fillFromModel(aggregations).then(function () {
          throw "This code should never be reached. cf DoNothingPromise";
        });
        this.itemsManager.fillFromModel(aggregations).then(function () {
          expect(spyCancelFibreUpdate).to.have.been.called;
          done();
        });

      });

      it('Should call cancelFibreUpdate when a second fill is started (MapComponentCountries case)', function (done) {

        this.thread.pushOperation({
          operator: '/loom/loom/GROUP_BY',
          parameters: {
            property: "country",
          }
        });

        var spyCancelFibreUpdate = sinon.spy(this.itemsManager.activeComponent, 'cancelFibreUpdate');

        var aggregations = _.toArray(this.aggregations);
        this.itemsManager.fillFromModel(aggregations).then(function () {
          throw "This code should never be reached. cf DoNothingPromise";
        });
        this.itemsManager.fillFromModel(aggregations).then(function () {
          expect(spyCancelFibreUpdate).to.have.been.called;
          done();
        });

      });

      it('Should make the proper calls on MapComponentMarkers when passed aggregations', function (done) {

        var spyPutItem = sinon.spy(this.itemsManager.activeComponent, 'putItem');
        var spyPutAggregation = sinon.spy(this.itemsManager.activeComponent, 'putAggregation');
        var spyPrepareFibreUpdate = sinon.spy(this.itemsManager.activeComponent, 'prepareFibreUpdate');
        var spyFinishFibreUpdate = sinon.spy(this.itemsManager.activeComponent, 'finishFibreUpdate');
        var spyCancelFibreUpdate = sinon.spy(this.itemsManager.activeComponent, 'cancelFibreUpdate');

        var aggregations = _.toArray(this.aggregations);
        this.itemsManager.fillFromModel(aggregations)
        .then(function () {

          expect(spyPutItem).not.to.have.been.called;
          expect(spyPutAggregation).to.have.been.called;
          expect(spyPutAggregation.args.length).to.equal(aggregations.length);
          expect(spyFinishFibreUpdate).to.have.been.called;
          expect(spyFinishFibreUpdate.args.length).to.equal(1);
          expect(spyPrepareFibreUpdate).to.have.been.called;
          expect(spyPrepareFibreUpdate.args.length).to.equal(1);
          expect(spyCancelFibreUpdate).not.to.have.been.called;
          done();
        });

        expect(this.itemsManager.activeComponent instanceof MapComponentMarkers).to.be.true;
      });

      it('Should make the proper calls on MapComponentCountries', function (done) {

        this.thread.pushOperation({
          operator: '/loom/loom/GROUP_BY',
          parameters: {
            property: "country",
          }
        });

        var spyPutItem = sinon.spy(this.itemsManager.activeComponent, 'putItem');
        var spyPutAggregation = sinon.spy(this.itemsManager.activeComponent, 'putAggregation');
        var spyPrepareFibreUpdate = sinon.spy(this.itemsManager.activeComponent, 'prepareFibreUpdate');
        var spyFinishFibreUpdate = sinon.spy(this.itemsManager.activeComponent, 'finishFibreUpdate');
        var spyCancelFibreUpdate = sinon.spy(this.itemsManager.activeComponent, 'cancelFibreUpdate');

        var aggregations = _.toArray(this.aggregations);
        this.itemsManager.fillFromModel(aggregations)
        .then(function () {

          expect(spyPutItem).not.to.have.been.called;
          expect(spyPutAggregation).to.have.been.called;
          expect(spyPutAggregation.args.length).to.equal(aggregations.length);
          expect(spyFinishFibreUpdate).to.have.been.called;
          expect(spyFinishFibreUpdate.args.length).to.equal(1);
          expect(spyPrepareFibreUpdate).to.have.been.called;
          expect(spyPrepareFibreUpdate.args.length).to.equal(1);
          expect(spyCancelFibreUpdate).not.to.have.been.called;
          done();
        });
      });
    });

  });

});

