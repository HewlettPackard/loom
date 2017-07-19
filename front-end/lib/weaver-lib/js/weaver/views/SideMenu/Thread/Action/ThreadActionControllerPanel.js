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
  var ThreadActionPanel = require('weaver/views/SideMenu/Thread/Action/ThreadActionPanel');
  var ThreadActionListPanel = require('weaver/views/SideMenu/Thread/Action/ThreadActionListPanel');
  var template = require('./ThreadActionControllerPanel.html');


  /** @type BaseView */
  var BaseView = require('weaver/views/BaseView');

  /**
   * ThreadActionControllerPanel displays either the list of thread actions, or the thread action being processed,
   * or the after processing page
   *
   * @class ThreadActionControllerPanel
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var ThreadActionControllerPanel = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_ThreadActionList
     * @final
     */
    constructorName: 'LOOM_ThreadActionControllerPanel',

    className: "mas-threadActionControllerPanel",

    template: template,

    threadActionListPanel: null, //the action list

    threadActionPanel: null,

    events: {
      'click .mas-thread-action': function(event) {
        event.stopPropagation();
        if (event.originalEvent) {
          var x = $(event.originalEvent.target);
          this.EventBus.trigger('thread:action:selected', {
            id: x.data('thread-action'),
            action: x.data('action'),
            thread: this.model
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
      this.threadActionListPanel = new ThreadActionListPanel({
        model: this.model
      });
      this.listenTo(this.EventBus, 'thread:action:selected', function(event) {
        this.threadActionPanel = new ThreadActionPanel(event);
        this.render();
      });
      this.listenTo(this.EventBus, 'thread:action:cancel', this._removeThreadActionPanel);
      // Receives event in function
      this.listenTo(this.EventBus, 'thread:action:server:send:success', function() {
        //console.log('todo: send success notification via service', event);
        this._removeThreadActionPanel();
      });
      // Receives event in function
      this.listenTo(this.EventBus, 'thread:action:server:send:fail', function() {
        //console.log('todo: send fail notification via service', event);
        this._removeThreadActionPanel();
      });
      this.render();
    },

    /**
     * Clean out the thread action panel and reinstate the actions list view
     * @private
     */
    _removeThreadActionPanel: function() {
      this.threadActionPanel.remove();
      this.threadActionPanel = null;
      this.$el.find('.mas-threadActionControllerPanel--list').removeClass('hideFlex');
      this.render();
    },

    /**
     * Renders the various components into the tabbed side menu view
     * @returns {ThreadActionControllerPanel}
     * @chainable
     */
    render: function() {
      this.$el.find('.mas-threadActionControllerPanel--list').append(this.threadActionListPanel.el);
      if (this.threadActionPanel) {
        this.$el.find('.mas-threadActionControllerPanel--action').append(this.threadActionPanel.el);
        this.$el.find('.mas-threadActionControllerPanel--list').addClass('hideFlex');
      } else {
        this.$el.find('.mas-threadActionControllerPanel--list').removeClass('hideFlex');
      }
      return this;
    }
  });

  return ThreadActionControllerPanel;
});
