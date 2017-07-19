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
  var Item = require('weft/models/Item');
  var Aggregation = require('weft/models/Aggregation');
  var ThreadView = require('weaver/views/ThreadView');
  var MapViewElement = require('weaver-map/views/map/MapViewElementV2');

  var ThreadViewAdapted = require('weaver-map/views/map/ThreadViewAdapted');

  describe('ThreadViewAdapted', function () {

    // To test the MapData, we need
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
        }, {});
      }, this);


      // ... a MapViewElement ...
      this.mapViewElement = new MapViewElement({
        model: this.thread,
      });

      this.mapViewElement.render();

      // ... and a ThreadViewAdapted.
      this.threadViewAdapted = new ThreadViewAdapted({
        model: this.thread,
        map: this.mapViewElement
      });

      document.body.appendChild(this.threadViewAdapted.el);
    });

    afterEach(function () {
      this.threadViewAdapted.remove();
    });

    describe('Transmission of focus, a thread..', function () {

      it('Should be the only one to have the focus when acquiring it', function () {
        // Let's create a chain of three thread view
        var parent = this.threadViewAdapted;

        parent._toggleThreadDisplay(new Aggregation({ displayMode: 'classic' }));

        var child  = parent.subThread;

        child._toggleThreadDisplay(new Aggregation({ displayMode: 'map' }));

        var lchild = child.subThread;

        // Set focus to child:
        child.model.set('focus', true);

        expect(parent.model.get('focus')).to.be.false;
        expect(child.model.get('focus')).to.be.true;
        expect(lchild.model.get('focus')).to.be.false;

        // Set focus to parent:
        parent.model.set('focus', true);

        expect(parent.model.get('focus')).to.be.true;
        expect(child.model.get('focus')).to.be.false;
        expect(lchild.model.get('focus')).to.be.false;

        // Set focus to lchild:
        lchild.model.set('focus', true);

        expect(parent.model.get('focus')).to.be.false;
        expect(child.model.get('focus')).to.be.false;
        expect(lchild.model.get('focus')).to.be.true;
      });

      it('Should transfer focus to its parent when being removed.', function () {

        var parent = this.threadViewAdapted;

        parent._toggleThreadDisplay(new Aggregation({ displayMode: 'classic' }));

        var child  = parent.subThread;

        // Set focus to child:
        child.model.set('focus', true);

        // Remove child
        parent.removeNestedThread();

        expect(parent.model.get('focus')).to.be.true;
      });
    });

    describe('_toggleThreadDisplay', function () {

      it('Should display a subThread when called for the first time', sinon.test(function () {
        var aggregation = new Aggregation({
          displayMode: 'classic',
        });

        var _createSubThreadView = this.spy(this.threadViewAdapted, '_createSubThreadView');

        this.threadViewAdapted._toggleThreadDisplay(aggregation);

        expect(_createSubThreadView.args.length).to.equal(1);
        expect(this.threadViewAdapted.subThread).not.to.be.undefined;
        expect(this.threadViewAdapted.subThread.model.get('displayMode')).to.equal('classic');
      }));

      it('Should switch of displayMode when called twice with a changed in the displayMode', sinon.test(function () {

        var aggregation = new Aggregation({
          displayMode: 'classic',
        });

        var _createSubThreadView = this.spy(this.threadViewAdapted, '_createSubThreadView');

        this.threadViewAdapted._toggleThreadDisplay(aggregation, 'classic');

        this.threadViewAdapted._toggleThreadDisplay(aggregation, 'map');

        expect(_createSubThreadView.args.length).to.equal(2);
        expect(this.threadViewAdapted.subThread).not.to.be.undefined;
        expect(aggregation.get('displayMode')).to.equal('map');
        expect(this.threadViewAdapted.subThread.model.get('displayMode')).to.equal('map');
      }));
    });

    describe('_createSubThreadView', function () {

      it('Should return ThreadView when called on an undefined display mode', function () {

        var res = this.threadViewAdapted._createSubThreadView(this.thread);

        expect(this.thread.get('displayMode')).to.equal('classic');
        expect(res instanceof ThreadView).to.be.true;
      });
    });
  });

});
