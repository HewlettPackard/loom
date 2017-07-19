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
  /** @type BaseView */
  var BaseView = require('../BaseView');

  /**
   * A controller for displaying and hiding tooltips over elements
   * @class  TooltipController
   * @module  weaver
   * @submodule views.FiberOverview
   * @namespace  views.FiberOverview
   * @constructor
   * @extends BaseView
   */
  var TooltipController = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @final
     */
    constructorName: 'LOOM_TooltipController',

    /**
     * @property TOOLTIP_DELAY
     * @type {Number}
     * @final
     * @default 500
     */
    TOOLTIP_DELAY: 500,

    /**
     * @property FINGER_SIZE
     * @type {Number}
     * @final
     * @default 40
     */
    FINGER_SIZE: 40,

    /**
     * @property {views/ElementView} options.view The view controlled by this object
     */
    events: {
      // 'pointerenter': function (event) {
      //   var offset = this._getTooltipOffset(event);
      //   var delay = this._getTooltipDelay(event);
      //   this.showTooltip(delay, offset);
      // },
      // 'pointerleave': function () {
      //   this.hideTooltip();
      // }
    },

    /**
     * gets passed:
     * options.view {FiberOverview}
     * options.model {FiberOverview.model} which is what exactly? the fiber?
     * options.el {FiberOverview.el}
     * @method initialize
     */
    initialize: function (options) {
      BaseView.prototype.initialize.apply(this, arguments);
      this.listenTo(this.EventBus, 'fiber:selected', function(event) {
        if (event.fiberView === this.$el.parents('.mas-element').data('view')) {
          this.showTooltip(0,-54, 0);
        }
      });
      this.listenTo(this.EventBus, 'fiber:unselected', function(event) {
        if (event.fiberView === this.$el.parents('.mas-element').data('view')) {
          this.hideTooltip();
        }
      })
    },

    /**
     * @method showTooltip
     * @param delay
     * @param vertical
     * @param horizontal
     */
    showTooltip: function (delay, vertical, horizontal) {
      this.timeout = setTimeout(_.bind(this._doShowTooltip, this, vertical, horizontal), delay);
    },

    /**
     * @method hideTooltip
     */
    hideTooltip: function () {
      clearTimeout(this.timeout);
      this.options.view.hideTooltip();
    },

    /**
     * @method _doShowTooltip
     * @param vertical
     * @param horizontal
     * @private
     */
    _doShowTooltip: function (vertical, horizontal) {
      this.options.view.showTooltip(vertical, horizontal);
    },

    /**
     * @method _getTooltipOffset
     * @param event
     * @returns {number}
     * @private
     */
    _getTooltipOffset: function (event) {
      var viewElement = this.options.view.el;
      var viewPosition = viewElement.getBoundingClientRect();
      var tooltipPosition = viewElement.querySelector('.mas-fiberOverview--tooltip').getBoundingClientRect();

      // Let's get the position of the interaction, relative to the bottom of the element:
      var eventPosition = viewPosition.height - (viewPosition.top - event.originalEvent.clientY);
      // Then add some space for the finger of the user
      var offset = eventPosition + this.FINGER_SIZE;

      // We then want to check that that offset still leaves the tooltip visible
      if (offset + tooltipPosition.height > viewPosition.height) {
        offset = viewPosition - tooltipPosition;
      }
      return offset;
    },

    /**
     * @method _getTooltipDelay
     * @param event
     * @returns {number}
     * @private
     */
    _getTooltipDelay: function (event) {
      return event.originalEvent.pointerType === 'mouse' ? this.TOOLTIP_DELAY : 0;
    }
  });

  return TooltipController;
});
