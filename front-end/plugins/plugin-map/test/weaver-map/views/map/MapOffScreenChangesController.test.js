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

  describe('MapOffScreenChangesController', function () {

    // To test the MapOffScreenChangesController, we need
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

      this.element = new MapMarker({
        thread: this.thread,
        el: this.mapViewElement.getD3Element().append('circle')[0][0],
        d3map: this.mapViewElement.getD3Element(),
      });

      this.element.setModel(new Aggregation({
        'l.logicalId': "oui-oui et ses amis",
        name: "toto va à la ferme",
      }));

      this.eventBus = new MapComponent({
        map: this.mapViewElement,
      });
    });

    afterEach(function () {
      document.body.removeChild(this.mapViewElement.el);
    });

    describe('hasLeftContainer-* events', function () {

      // 4 test.
      
      it('Should insert the element to the bottom view', function () {

        // Bottom
        this.eventBus.dispatchInstantEvent(this.element.el, 'hasLeftContainer-bottom', this.element);
        
        // Check state:
        var id = this.element.__idMapOffScreenChangesController;
        expect(id).not.to.be.undefined;
        expect(this.offScreenController.targetInView[id]).to.equal(this.offScreenController.views.bottomView);
        expect(_.size(this.offScreenController.targetInView)).to.equal(1);

      });

      it('Should insert the element to the top view', function () {
        
        // Top
        this.eventBus.dispatchInstantEvent(this.element.el, 'hasLeftContainer-top', this.element);
        
        // Check state:
        var id = this.element.__idMapOffScreenChangesController;
        expect(id).not.to.be.undefined;
        expect(this.offScreenController.targetInView[id]).to.equal(this.offScreenController.views.topView);
        expect(_.size(this.offScreenController.targetInView)).to.equal(1);
      });

      it('Should insert the element to the left view', function () {
        // Left
        this.eventBus.dispatchInstantEvent(this.element.el, 'hasLeftContainer-left', this.element);
        
        // Check state:
        var id = this.element.__idMapOffScreenChangesController;
        expect(id).not.to.be.undefined;
        expect(this.offScreenController.targetInView[id]).to.equal(this.offScreenController.views.leftView);
        expect(_.size(this.offScreenController.targetInView)).to.equal(1);
      });

      it('Should insert the element to the right view', function () {
        // Right
        this.eventBus.dispatchInstantEvent(this.element.el, 'hasLeftContainer-right', this.element);
        
        // Check state:
        var id = this.element.__idMapOffScreenChangesController;
        expect(id).not.to.be.undefined;
        expect(this.offScreenController.targetInView[id]).to.equal(this.offScreenController.views.rightView);
        expect(_.size(this.offScreenController.targetInView)).to.equal(1);
      });
    });

    describe('hasMovedTo-* events', function () {

      it('Should switch the element of view when view are different', function () {

        var spyRemoveFromView = sinon.spy(this.offScreenController, '_removeFromView');

        // Bottom
        this.eventBus.dispatchInstantEvent(this.element.el, 'hasLeftContainer-bottom', this.element);
        this.eventBus.dispatchInstantEvent(this.element.el, 'hasLeftContainer-left', this.element);
        
        // Check state:
        var id = this.element.__idMapOffScreenChangesController;
        expect(id).not.to.be.undefined;
        expect(this.offScreenController.targetInView[id]).to.equal(this.offScreenController.views.leftView);
        expect(_.size(this.offScreenController.targetInView)).to.equal(1);
        expect(spyRemoveFromView).to.have.been.called;
        expect(spyRemoveFromView.callCount).to.equal(2);
      });

      it('Should not switch the element if it is already in the correct view', function () {

        var spyRemoveFromView = sinon.spy(this.offScreenController, '_removeFromView');

        // Bottom
        this.eventBus.dispatchInstantEvent(this.element.el, 'hasLeftContainer-bottom', this.element);
        this.eventBus.dispatchInstantEvent(this.element.el, 'hasLeftContainer-bottom', this.element);
        
        // Check state:
        var id = this.element.__idMapOffScreenChangesController;
        expect(id).not.to.be.undefined;
        expect(this.offScreenController.targetInView[id]).to.equal(this.offScreenController.views.bottomView);
        expect(_.size(this.offScreenController.targetInView)).to.equal(1);
        expect(spyRemoveFromView).to.have.been.called;
        expect(spyRemoveFromView.callCount).to.equal(1);
      });
    });

  });
});