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
define(function (require) {
  "use strict";

  var _ = require('lodash');
  var MapComponent = require('./MapComponentV2');
  
  var IsOutsideController = MapComponent.extend({
    
    events: {
      'change:pan': function () {
        this._handleMoveEvent();
      },
      'add:tracking': function (event) {
        var elementWithBBox = event.originalEvent.args;
        this.trackTarget(elementWithBBox);
      },
      'addlazy:tracking': function (event) {
        var elementWithBBox = event.originalEvent.args;
        this.trackTargetLazy(elementWithBBox);
      },
      'remove:tracking': function (event) {
        var elementWithBBox = event.originalEvent.args;
        this.stopTracking(elementWithBBox);
      },
      'refresh:tracking': function () {
        this._handleMoveEvent();
      },
    },

    initialize: function () {
      MapComponent.prototype.initialize.apply(this, arguments);

      this.targetList = {};
    },

    initializeWhenAttached: function () {

    },

    stopTracking: function (target) {
      if (target) {
        if (target.el) {
          this.dispatchInstantEvent(target.el, 'removeFromOffscreenController', target);
        }
        delete this.targetList[target.__idIsOutsideController];
      }
    },
 
    trackTarget: function (target) {
      
      this.trackTargetLazy(target);
      this._handleMoveEvent(this.targetList[target.__idIsOutsideController]);
    },

    trackTargetLazy: function (target) {

      var id = this._generateId(target);

      var targetWrapper = {
        t: target,
        wasOutside: false,
      };
  
      this.targetList[id] = targetWrapper;
    },

    _generateId: function (target) {
      if (!target.__idIsOutsideController) {
        target.__idIsOutsideController = _.uniqueId("IsOutsideController");
      }
      return target.__idIsOutsideController;
    },

    _handleMoveEvent: function () {
      if (arguments.length === 1) {
        this._handleMoveEventForTarget.apply(this, arguments);
      } else {
        _.forEach(this.targetList, this._handleMoveEventForTarget, this);
      }
    },

    _handleMoveEventForTarget: function (targetWrapper) {

      var res = this._targetIsOutside(targetWrapper.t);
      var target = targetWrapper.t;

      if (res) {

        if (!targetWrapper.wasOutside) {
          target.trigger('hasLeftContainer');
        }
        
        this.dispatchInstantEvent(target.el, 'hasLeftContainer-' + this._matchResWithBorder(res), target);

        targetWrapper.wasOutside = true;

      } else if (targetWrapper.wasOutside) {

        targetWrapper.wasOutside = false;

        target.trigger('isInsideContainer');
        this.dispatchInstantEvent(target.el, 'isInsideContainer', target);
      }
    },

    _targetIsOutside: function (target) {
      var bboxTarget = target.getBBox();
      var bboxContainer = this.map.getBBox();
      var isOutSide = 0;
      isOutSide |= (bboxTarget.x + bboxTarget.width < bboxContainer.x) << 0;
      isOutSide |= (bboxTarget.x > bboxContainer.x + bboxContainer.width) << 1;
      isOutSide |= (bboxTarget.y + bboxTarget.height < bboxContainer.y) << 2;
      isOutSide |= (bboxTarget.y > bboxContainer.y + bboxContainer.height) << 3;

      return isOutSide;
    },

    _matchResWithBorder: function (res) {
      if (res & (1 << 3)) {
        return 'bottom';
      }
      if (res & (1 << 2)) {
        return 'top';
      }
      if (res & (1 << 1)) {
        return 'right';
      }
      if (res & (1 << 0)) {
        return 'left';
      }
    },

  });

  return IsOutsideController;
});