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
define(['lodash', 'jquery'], function (_, $) {

  "use strict";
  var FiberActionPanel = require('weaver/views/SideMenu/Fiber/FiberActionPanel');
  var FiberActionListPanel = require('weaver/views/SideMenu/Fiber/FiberActionListPanel');
  var template = require('./FiberActionControllerPanel.html');

  /** @type BaseView */
  var BaseView = require('weaver/views/BaseView');

  /**
   * FiberActionControllerPanel displays either the list of fiber actions, or the fiber action being processed,
   * or the after processing page
   *
   * @class FiberActionControllerPanel
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var FiberActionControllerPanel = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_FiberActionList
     * @final
     */
    constructorName: 'LOOM_FiberActionControllerPanel',

    className: "mas-fiberActionControllerPanel",

    template: template,

    fiberActionListPanel: null, //the action list

    fiberActionPanel: null,

    events: {
      'click .mas-fiber-action': function(event) {
        event.stopPropagation();
        if (event.originalEvent) {
          var x = $(event.originalEvent.target);
          x = x.hasClass('mas-fiberActionIcon') ? x.parent() : x;
          this.EventBus.trigger('fiber:action:selected', {
            id: x.data('fiber-action'),
            action: x.data('action'),
            fiber: this.model,
            thread: this.options.thread
          });
        }
      }
    },

    /**
     * @method initialize
     */
    initialize: function (options) {
      BaseView.prototype.initialize.apply(this, arguments);
      this.model = options.model;
      this.fiberActionListPanel = new FiberActionListPanel({
        model: this.model
      });
      this.listenTo(this.EventBus, 'fiber:action:selected', function(event) {
        this.fiberActionPanel = new FiberActionPanel(event);
        this.render();
      });
      this.listenTo(this.EventBus, 'fiber:action:cancel', this._removeFiberActionPanel);
      this.listenTo(this.EventBus, 'fiber:action:server:send:success', function() {
        this._removeFiberActionPanel();
      });
      this.listenTo(this.EventBus, 'fiber:action:server:send:fail', function() {
        this._removeFiberActionPanel();
      });
      this.render();
    },

    /**
     * Clean out the fiber action panel and reinstate the actions list view
     * @private
     */
    _removeFiberActionPanel: function() {
      this.fiberActionPanel.remove();
      this.fiberActionPanel = null;
      this.$el.find('.mas-fiberActionControllerPanel--list').removeClass('hideFlex');
      this.render();
    },

    /**
     * Renders the various components into the tabbed side menu view
     * @returns {FiberActionControllerPanel}
     * @chainable
     */
    render: function() {
      this.$el.find('.mas-fiberActionControllerPanel--list').append(this.fiberActionListPanel.el);
      if (this.fiberActionPanel) {
        this.$el.find('.mas-fiberActionControllerPanel--action').append(this.fiberActionPanel.el);
        this.$el.find('.mas-fiberActionControllerPanel--list').addClass('hideFlex');
      } else {
        this.$el.find('.mas-fiberActionControllerPanel--list').removeClass('hideFlex');
      }
      return this;
    }
  });

  return FiberActionControllerPanel;
});
