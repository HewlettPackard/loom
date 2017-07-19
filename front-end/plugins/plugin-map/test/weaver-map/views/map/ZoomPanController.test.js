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
  var d3 = require('d3');

  var Thread = require('weft/models/Thread');
  var MapViewElement = require('weaver-map/views/map/MapViewElementV2');
  var ZoomPanController = require('weaver-map/views/map/ZoomPanController');
  var MapComponent = require('weaver-map/views/map/MapComponentV2');
  var MapDataManager = require('weaver-map/models/map/MapDataManager');

  describe('ZoomPanController', function () {


    /*jshint validthis: true */
    function fakeUpdateZoom(zoom) {
      d3.event = {};
      d3.event.translate = [+1, -1];
      d3.event.scale = zoom || 1;
      this.zoomPanController._updateZoom();
    }

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

      this.zoomPanController = new ZoomPanController();

      this.zoomPanController.attach(this.mapViewElement);

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
        
      this.fakeUpdateZoom = _.bind(fakeUpdateZoom, this);

      this.fakeTarget.el =  this.fakeTarget.$el.get(0);

      this.eventBus = new MapComponent({
        map: this.mapViewElement,
      });
    });

    afterEach(function () {
      document.body.removeChild(this.mapViewElement.el);
    });

    describe('_updateZoom()', function () {

      // Not sure this test is really usefull.. I'll come back later with
      // better test when we'll fix the first bugs.
      it('Should trigger un bunch of events', function () {

        var spy0 = sinon.spy(this.zoomPanController, 'dispatchInstantEvent');
        var spy1 = sinon.spy(this.zoomPanController, 'dispatchEvent');

        this.fakeUpdateZoom();
        this.fakeUpdateZoom(2);

        expect(spy0.calledWith(this.zoomPanController.d3map, 'change:pan:instant')).to.be.true;
        expect(spy1.calledWith(this.zoomPanController.d3map, 'change:pan')).to.be.true;
        expect(spy1.calledWith(this.zoomPanController.d3map, 'change:zoom')).to.be.true;
        expect(spy1.calledWith(this.zoomPanController.d3map, 'change:pan:slow')).to.be.true;
        expect(spy1.calledWith(this.zoomPanController.d3map, 'change:panup')).to.be.true;
        expect(spy1.calledWith(this.zoomPanController.d3map, 'change:pandown')).to.be.true;
        expect(spy1.calledWith(this.zoomPanController.d3map, 'change:panleft')).to.be.true;
        expect(spy1.calledWith(this.zoomPanController.d3map, 'change:panright')).to.be.true;
        expect(spy1.calledWith(this.zoomPanController.d3map, 'change:zoomminus')).to.be.true;
        expect(spy1.calledWith(this.zoomPanController.d3map, 'change:zoomplus')).to.be.true;
      });
    });
  });

});