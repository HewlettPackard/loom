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
  var ThreadMetricsConfigurationPanel = require('weaver/views/SideMenu/Thread/Configuration/ThreadMetricsConfigurationPanel');
  var ThreadConfigurationQuickSortPanel = require('weaver/views/SideMenu/Thread/Configuration/ThreadConfigurationQuickSortPanel');
  var ThreadConfigurationListPanel = require('weaver/views/SideMenu/Thread/Configuration/ThreadConfigurationListPanel');
  var template = require('./ThreadConfigurationControllerPanel.html');


  /** @type BaseView */
  var BaseView = require('weaver/views/BaseView');

  /**
   * ThreadConfigurationControllerPanel displays either the list of thread configurations, or the thread configuration
   * being processed, or the after processing page
   *
   * @class ThreadConfigurationControllerPanel
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var ThreadConfigurationControllerPanel = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_ThreadActionList
     * @final
     */
    constructorName: 'LOOM_ThreadConfigurationControllerPanel',

    className: "mas-threadConfigurationControllerPanel",

    template: template,

    threadConfigurationControllerPanel: null, //the action list

    threadConfigurationListPanel: null,

    events: {
      'click .mas-action--closeThread': function() {
        this.EventBus.trigger(
          'thread:close:request',
          this.serviceManager.get('ThreadSelectionService').selectedThreadEvent
        );
      },
      'click .mas-action--cloneThread': function() {
        this.EventBus.trigger(
          'thread:clone',
          this.serviceManager.get('ThreadSelectionService').selectedThreadEvent
        );
      },
      'click .mas-thread-configuration': function(event) {
        event.stopPropagation();
        if (event.originalEvent) {
          var x = $(event.originalEvent.target);
          if (x.hasClass('mas-threadConfigurationIcon')) {
            x = x.parent();
          }
          this.EventBus.trigger('thread:configuration:selected', {
            id: x.data('id'),
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
      this.serviceManager = options.serviceManager;
      this.threadConfigurationListPanel = new ThreadConfigurationListPanel({
        model: this.model
      });
      this.listenTo(this.EventBus, 'thread:configuration:selected', function(event) {
        if (event && event.id) {
          if (event.id === 'metrics') {
            this.threadConfigurationPanel = new ThreadMetricsConfigurationPanel({
              model: this.model
            });
          } else if (event.id === 'quicksort') {
            this.threadConfigurationPanel = new ThreadConfigurationQuickSortPanel({
              model: this.model
            });
          }
          this.render();
        }

      });
      this.listenTo(this.EventBus, 'thread:configuration:cancel', this._removeThreadConfigurationPanel);
      this.render();
    },

    /**
     * Clean out the thread configuration panel and reinstate the configurations list view
     * @private
     */
    _removeThreadConfigurationPanel: function() {
      this.threadConfigurationPanel.remove();
      this.threadConfigurationPanel = null;
      this.$el.find('.mas-threadConfigurationControllerPanel--list').removeClass('hideFlex');
      this.render();
    },

    /**
     * Renders the various components into the tabbed side menu view
     * @returns {ThreadConfigurationControllerPanel}
     * @chainable
     */
    render: function() {
      this.$el.find('.mas-threadConfigurationControllerPanel--list').append(this.threadConfigurationListPanel.el);
      if (this.threadConfigurationPanel) {
        this.$el.find('.mas-threadConfigurationControllerPanel--configuration').append(this.threadConfigurationPanel.el);
        this.$el.find('.mas-threadConfigurationControllerPanel--list').addClass('hideFlex');
        this.$el.find('.mas-threadConfigurationControllerPanel--configuration').removeClass('hideFlex');
      } else {
        this.$el.find('.mas-threadConfigurationControllerPanel--list').removeClass('hideFlex');
        this.$el.find('.mas-threadConfigurationControllerPanel--configuration').addClass('hideFlex');
      }
      return this;
    }
  });

  return ThreadConfigurationControllerPanel;
});
