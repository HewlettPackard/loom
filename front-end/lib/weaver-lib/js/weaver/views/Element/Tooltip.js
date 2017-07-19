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

  var $ = require('jquery');
  /** @type BaseView */
  var BaseView = require('./../BaseView');
  var template = require('./Tooltip.html');
  var ItemType = require('weft/models/ItemType');

  /**
   * @class Tooltip
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var Tooltip = BaseView.extend({

    template: template,

    events: {
      "click .mas-tooltip-action": function(event) {
        event.preventDefault();
        event.stopPropagation();
        if ($(event.target)) {
          var action = $(event.target).data('action');
          this.EventBus.trigger('thread:fiber:action:' + action, this.options);
        }
      }
    },

    /**
     * options.event will be the fiber:selected event data giving
     * options.event.fiber
     * options.event.fiberView
     * options.event.thread
     * options.event.threadView
     *  @method initialize
     */
    initialize: function (options) {
      BaseView.prototype.initialize.apply(this, arguments);
      if (options.fiber.itemType instanceof ItemType) {
        this.$el.find('.mas-tooltip--action-display').remove();
      }
      //this.setTooltipText(options.fiber.get('name'));
    },

    /**
     * Shows a tooltip over the element
     * @method showTooltip
     * @param  {Object} vertical       The vertical offset of the tooltip
     * @param  {Object} horizontal     The horizontal offset of the tooltip (0)
     */
    // showTooltip: function (vertical, horizontal) {
    //   this.$('.mas-fiberOverview--tooltip').css({
    //     bottom: vertical || 0,
    //     right: horizontal || 0
    //   });
    //   this.$el.addClass('has-visibleTooltip');
    // },   

    /**
     * @method showTooltipText
     * @param value
     */
    setTooltipText: function (value) {
      this.$el.find('.mas-tooltip--text').text(value);
    }

  });

  return Tooltip;
});
