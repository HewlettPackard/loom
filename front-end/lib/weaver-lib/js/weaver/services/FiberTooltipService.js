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
define(['jquery', 'position-calculator'], function ($, PositionCalculator) {

  "use strict";

  var FilterService = require('weft/services/FilterService');
  var EventBus = require('weaver/utils/EventBus');
  var Tooltip = require('weaver/views/Element/Tooltip');

  /**
   * FiberTooltipService is the main service that handles fiber tooltips.
   * @class FiberTooltipService
   * @namespace services
   * @module weaver
   * @submodule services
   * @extends Backbone.Model
   */
  var FiberTooltipService = FilterService.extend({

    constructorName: 'LOOM_FiberTooltipService',

    tooltip: null,

    tapestry: null,

    /**
     * Global event bus for inter module communication
     * @type Backbone.Events
     */
    EventBus: EventBus,

    initialize: function (tapestry) {
      FilterService.prototype.initialize.apply(this, arguments);
      this.tapestry = tapestry;
      this.listenTo(this.EventBus, 'fiber:selected', function (event) {
        this._cleanTooltip();
        this.tooltip = new Tooltip(event);
        this.tooltip.render();
        this.tapestry.$el.append(this.tooltip.el);
        this._positionTooltip();
      });
      this.listenTo(this.EventBus, 'fiber:unselected', function () {
        this._cleanTooltip();
      });
      this.listenTo(this.EventBus, 'element:will:remove', function(event) {
        if (this.tooltip !== null && event.view  === this.tooltip.options.fiberView) {
          this._cleanTooltip();
        }
      });
      this.listenTo(
        this.EventBus,
        [
          'change',
          'notification:cancel',
          'query:editor:shown',
          'query:editor:hidden',
          'screen:notify:braiding:narrower',
          'screen:notify:braiding:wider',
          'screen:notify:braiding:update',
          'thread-list:scrolled',
          'thread:close',
          'thread:list:remove',
          'thread-list:sortstart',
          'thread-list:sort',
          'thread-list:sortstop',
          'thread:metric:selected',
          'thread:metric:unselected',
          'thread:selected',
          'thread:unselected'
        ].join(' '),
        this._positionTooltip
      );
    },

    /**
     * Cleans and removes the current tooltip (if any)
     * @private
     */
    _cleanTooltip: function() {
      if (this.tooltip !== null) {
        this.tooltip.$el.remove();
        this.tooltip = null;
      }
    },

    /**
     * Position the tooltip relative to the fiber it is related too
     * Taking metrics into consideration..
     * @private
     */
    _positionTooltip: function() {
      if (this.tooltip !== null) {
        // always reset the position to 0,0 before calculating.. so offsets are positive.
        this.tooltip.$el.css({top: 0, left: 0 });
        var heightOffset = 72;
        var calculator = new PositionCalculator({
          item: this.tooltip.$el,
          itemAt: "top center",
          target: this.tooltip.options.fiberView.$el,
          targetAt: "bottom center"
        });
        var posResult = calculator.calculate();
        var fiberViewPosition = this.tooltip.options.fiberView.$el.offset();
        this.tooltip.$el.css({
          'top': (fiberViewPosition.top + heightOffset) + "px",
          'left': posResult.moveBy.x + "px"
        });
      }
    }
  });

  return FiberTooltipService;
});
