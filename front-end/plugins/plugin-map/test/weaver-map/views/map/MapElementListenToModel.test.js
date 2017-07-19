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
  var Thread = require('weft/models/Thread');

  var MapViewElement = require('weaver-map/views/map/MapViewElementV2');
  var MapElementListenToModel = require('weaver-map/views/map/MapElementListenToModelV2');

  describe('MapElementListenToModel', function () {

    // To test the MapElementListenToModel, we need
    beforeEach(function () {

      // ... a Thread ...
      this.thread = new Thread({
        id: 'thread',
        name: 'Thread',
      });

      // ... an item ...
      var elementID = this.elementID = 'toto';
      this.element = new Item({
        'l.logicalId': elementID,
        name: elementID,
        attributes: {
          lon: (2 * Math.random() - 1) * 180,
          lat: (2 * Math.random() - 1) * 90,
        },
      });

      // a map view
      this.mapView = new MapViewElement({
        model: this.thread,
      });

      this.mapView.render();

      // ... and a MapElementListenToModel.
      this.view = new MapElementListenToModel({
        d3map: this.mapView.getD3Element(),
        thread: this.thread,
      });

      document.body.appendChild(this.view.el);
    });

    afterEach(function () {
      document.body.removeChild(this.view.el);
    });

    describe('setModel()', function () {

      it('Should listen to related event and change the css.', sinon.test(function () {

        var _updateRelatedStateSpy = this.spy(this.view, '_updateRelatedState');
        var _addClass = this.spy(this.view, '_addClass');

        this.view.setModel(this.element);
        this.element.set('related', true);

        expect(_updateRelatedStateSpy).to.have.been.called;
        expect(_updateRelatedStateSpy.args.length).to.equal(2);
        expect(_addClass).to.have.been.calledWith('is-related');
      }));

      it('Should listen to level change event to update the css', sinon.test(function () {

        var _updateRelatedStateSpy = this.spy(this.view, '_updateAlertState');
        var _setClass = this.spy(this.view, '_setClass');

        this.view.setModel(this.element);
        this.element.alert.set('level', 1);

        expect(_updateRelatedStateSpy).to.have.been.called;
        expect(_updateRelatedStateSpy.args.length).to.equal(2);
        expect(_setClass).to.have.been.calledWith('alertLevel', 'mas-alertNotification-1');
      }));

      it('Should listen to change:isMatchingFilter to update the css', sinon.test(function () {

        var _updateFilterState = this.spy(this.view, '_updateFilterState');
        var _addClass = this.spy(this.view, '_addClass');

        this.view.setModel(this.element);
        this.element.set('isMatchingFilter', true);

        expect(_updateFilterState).to.have.been.called;
        expect(_updateFilterState.args.length).to.equal(2);
        expect(_addClass).to.have.been.calledWith('is-matchingFilter');
      }));
    });

  });

});
