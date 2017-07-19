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
  var IsOutsideController = require('weaver-map/views/map/IsOutsideController');
  var MapMarker = require('weaver-map/views/map/MapMarkerV2');
  var MapCountry = require('weaver-map/views/map/MapCountryV2');
  var MapComponent = require('weaver-map/views/map/MapComponentV2');
  var MapDataManager = require('weaver-map/models/map/MapDataManager');

  describe('IsOutsideController fakeTarget', function () {

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

      this.mapViewElement.getBBox = function () {
        return {width: 10, height: 10, x: 0, y: 0};
      };

      this.isOutsideController = new IsOutsideController();

      this.isOutsideController.attach(this.mapViewElement);

      this.fakeTarget = {
        bbox: {
          width: 5,
          height: 5,
          x: 0,
          y: 0,
        },
        trigger: function () {},
        getBBox: function () {
          return this.bbox;
        },
        $el: this.mapViewElement.$el.append('<div></div>'),
      };
        

      this.fakeTarget.el =  this.fakeTarget.$el.get(0);

      this.eventBus = new MapComponent({
        map: this.mapViewElement,
      });
    });

    afterEach(function () {
      document.body.removeChild(this.mapViewElement.el);
    });

    describe('add:tracking', function () {

      it('Should trigger hasLeftContainer when the target is completely outside', function () {

        var spy = sinon.spy();
        var spy2 = sinon.spy(this.isOutsideController, 'dispatchInstantEvent');

        this.mapViewElement.$el.on('hasLeftContainer-top', spy);

        this.fakeTarget.bbox = {width: 4, height: 4, x: 0, y: -5};
        this.eventBus.dispatchInstantEvent(this.mapViewElement.getD3Element(), 'add:tracking', this.fakeTarget);

        expect(spy).to.have.been.called;
        expect(spy2).to.have.been.called;
      });

      it('Should do nothing when the target is partially inside', function () {

        var spy = sinon.spy(this.isOutsideController, 'dispatchInstantEvent');
        var spy1 = sinon.spy();

        this.mapViewElement.$el.on('hasLeftContainer-top', spy1);

        this.fakeTarget.bbox = {width: 4, height: 6, x: 0, y: -5};
        this.eventBus.dispatchInstantEvent(this.mapViewElement.getD3Element(), 'add:tracking', this.fakeTarget);

        expect(spy).not.to.have.been.called;
        expect(spy1).not.to.have.been.called;
      });

      it('Should do nothing when the target is inside', function () {

        var spy = sinon.spy(this.isOutsideController, 'dispatchInstantEvent');

        this.eventBus.dispatchInstantEvent(this.mapViewElement.getD3Element(), 'add:tracking', this.fakeTarget);

        expect(spy).not.to.have.been.called;
      });
    });

    describe('remove:tracking', function () {

      it('Should inform the MapOffscreenController that the target should be removed', function () {

        var spy = sinon.spy();

        this.mapViewElement.$el.on('removeFromOffscreenController', spy);

        this.eventBus.dispatchInstantEvent(this.mapViewElement.getD3Element(), 'remove:tracking', this.fakeTarget);

        expect(spy).to.have.been.called;
      });
    });

    describe('Multiples target', function () {

      beforeEach(function () {

        var marker = this.mapViewElement.getD3Element()
          .append('circle')
          .attr('id', 'toto')
          .attr('cx', 0)
          .attr('cy', +12)
          .attr('r', 1);

        var datum = {
          dx: 0,
          dy: 12,
          r: 1,
        };

        marker.data([datum]);

        var country = this.mapViewElement.getD3Element()
          .append('use')
          .attr('xlink:href', '#toto');

        this.marker = new MapMarker({
          thread: this.thread,
          d3map: this.mapViewElement.getD3Element(),
          el: marker[0][0],
        });

        this.marker.setDatum(datum);

        this.country = new MapCountry({
          thread: this.thread,
          d3map: this.mapViewElement.getD3Element(),
          el: country[0][0],
        });
      });

      it('add:tracking should update each target', function () {

        var spy = sinon.spy(this.isOutsideController, '_handleMoveEventForTarget');
        var spy3 = sinon.spy(this.isOutsideController, 'dispatchInstantEvent');

        this.eventBus.dispatchInstantEvent(this.mapViewElement.getD3Element(), 'add:tracking', this.fakeTarget);
        this.eventBus.dispatchInstantEvent(this.mapViewElement.getD3Element(), 'add:tracking', this.marker);
        this.eventBus.dispatchInstantEvent(this.mapViewElement.getD3Element(), 'add:tracking', this.country);

        expect(spy.callCount).to.equal(3);
        expect(spy3.callCount).to.equal(2);
        expect(spy3.calledWith(this.marker.el, 'hasLeftContainer-bottom', this.marker)).to.be.true;
        expect(spy3.calledWith(this.country.el, 'hasLeftContainer-bottom', this.country)).to.be.true;
      });

      it('change:pan should force the update of every target', function () {

        var spy = sinon.spy(this.isOutsideController, '_handleMoveEventForTarget');
        var spytargetIsOutside = sinon.spy(this.isOutsideController, '_targetIsOutside');

        var spyisInsideContainer = sinon.spy();

        this.mapViewElement.$el.on('isInsideContainer', spyisInsideContainer);

        this.fakeTarget.bbox = {width: 4, height: 4, x: -5, y: 0};

        this.eventBus.dispatchInstantEvent(this.mapViewElement.getD3Element(), 'add:tracking', this.fakeTarget);
        this.eventBus.dispatchInstantEvent(this.mapViewElement.getD3Element(), 'add:tracking', this.marker);
        this.eventBus.dispatchInstantEvent(this.mapViewElement.getD3Element(), 'add:tracking', this.country);
        
        spy.reset();
        spytargetIsOutside.reset();

        // Move the fake target inside
        this.fakeTarget.bbox = {width: 4, height: 6, x: 0, y: -5};

        this.eventBus.dispatchInstantEvent(this.mapViewElement.getD3Element(), 'change:pan');

        expect(spy.callCount).to.equal(3);
        expect(spytargetIsOutside.calledWith(this.fakeTarget)).to.be.true;
        expect(spytargetIsOutside.calledWith(this.marker)).to.be.true;
        expect(spytargetIsOutside.calledWith(this.country)).to.be.true;
        expect(spyisInsideContainer).to.have.been.called;
      });
    });
  });

});