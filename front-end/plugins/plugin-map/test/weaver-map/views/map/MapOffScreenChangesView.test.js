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
  var MapViewElement = require('weaver-map/views/map/MapViewElementV2');
  var MapOffScreenChangesView = require('weaver-map/views/map/MapOffScreenChangesView');
  var MapOffScreenChangesController = require('weaver-map/views/map/MapOffScreenChangesController');
  var MapFlexOverlay = require('weaver-map/views/map/MapFlexOverlay');
  var MapMarker = require('weaver-map/views/map/MapMarkerV2');
  var MapComponent = require('weaver-map/views/map/MapComponentV2');
  var MapDataManager = require('weaver-map/models/map/MapDataManager');

  describe('MapOffScreenChangesView', function () {

    // To test the MapOffScreenChangesView, we need
    beforeEach(function () {

      // ... a Thread ...
      this.thread = new Thread({
        id: 'thread',
        name: 'Thread',
      });

      // ... a MapViewElement ...
      this.mapViewElement = new MapViewElement({
        model: this.thread,
      });

      this.mapViewElement.render();
      document.body.appendChild(this.mapViewElement.el);

      this.mapData = MapDataManager.get(this.mapViewElement);
      this.mapData.set('mapIsReady', true);

      this.offScreenChanges = {
        top:    new MapOffScreenChangesView({ direction: 'horizontal' }),
        bottom: new MapOffScreenChangesView({ direction: 'horizontal' }),
        left:   new MapOffScreenChangesView({ direction: 'vertical' }),
        right:  new MapOffScreenChangesView({ direction: 'vertical' })
      };

      this.flexOverlay = new MapFlexOverlay();

      this.flexOverlay.attach(this.mapViewElement);

      // Attach the view to the overlay:
      _.forOwn(this.offScreenChanges, function (value, key) {
        this.flexOverlay.setChildView(key, value);
      }, this);
      
      this.offScreenController = new MapOffScreenChangesController({
        views: this.offScreenChanges
      });
      
      this.offScreenController.attach(this.mapViewElement);

      // Those are the view that we will test.
      this.viewTop = this.offScreenChanges.top;
      this.viewLeft = this.offScreenChanges.left;

      this.element = new MapMarker({
        thread: this.thread,
        el: this.mapViewElement.getD3Element().append('circle')[0][0],
        d3map: this.mapViewElement.getD3Element(),
      });

      this.model = new Aggregation({
        'l.logicalId': "A",
        name: "Marker",
        alertCount: 5,
      });

      this.element.setModel(this.model);

      this.element.__idMapOffScreenChangesController = 24;

      this.eventBus = new MapComponent({
        map: this.mapViewElement,
      });
    });

    afterEach(function () {
      document.body.removeChild(this.mapViewElement.el);
    });

    describe('add()', function () {

      it('Should do nothing if the state is not a valid state (`alert`, `updated` or `added`)', function () {

        var spy = sinon.spy(this.viewTop, '_updateBarSize');

        this.viewTop.add('state-not-appropriate', this.element);

        expect(spy).not.to.have.been.called;
      });

      it('Should increase total size by the alert count when called with `alert`', function () {

        var spy = sinon.spy(this.viewTop, '_updateBarSize');

        this.viewTop.add('alert', this.element);

        expect(spy).to.have.been.called;
        expect(this.viewTop.totalCount).to.equal(this.model.get('alertCount'));
      });

      it('Should increase total size by one when called with `updated` or `added`', function () {

        var spy = sinon.spy(this.viewLeft, '_updateBarSize');

        this.viewLeft.add('updated', this.element);
        expect(this.viewLeft.totalCount).to.equal(1);

        this.viewLeft.add('added', this.element);
        expect(this.viewLeft.totalCount).to.equal(2);

        expect(spy).to.have.been.called;
      });
    });

    describe('remove()', function () {

      it('Should remove from totalSize the count previously added', function () {

        var count = this.model.get('alertCount');
        this.viewLeft.add('alert', this.element);
        expect(this.viewLeft.totalCount).to.equal(count);

        // Change the alertCount without updating the view.
        this.model.set('alertCount', 234234);

        this.viewLeft.remove('alert', this.element);
        expect(this.viewLeft.totalCount).to.equal(0);
      });

      it('Should do nothing when called with bad state value', function () {

        var spy = sinon.spy(this.viewTop, '_updateBarSize');

        this.viewTop.remove(undefined, {});

        expect(spy).not.to.have.been.called;
      });
    });

    describe('clear()', function () {

      it('Should reset the view to its default value, and also update the view accordingly', function () {

        var spy = sinon.spy(this.viewTop, '_updateBarSize');

        this.viewTop.add('alert', this.element);
        this.viewTop.clear();

        expect(this.viewTop.totalCount).to.equal(0);
        expect(this.viewTop._setOfElements).to.eql({
          'added': {},
          'alert': {},
          'updated': {}
        });

        expect(spy).to.have.been.called;
      });
    });
  });

});