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
define([], function () {

  "use strict";

  /** @type BaseView */
  var BaseView = require('weaver/views/BaseView');
  var ThreadConfigurationControllerPanel = require('weaver/views/SideMenu/Thread/Configuration/ThreadConfigurationControllerPanel');
  var ThreadActionControllerPanel = require('weaver/views/SideMenu/Thread/Action/ThreadActionControllerPanel');
  var ThreadSelectedInfoView = require('weaver/views/SideMenu/Thread/ThreadSelectedInfoView');
  var ThreadTabsView = require('weaver/views/SideMenu/Thread/ThreadTabsView');
  var template = require('weaver/views/SideMenu/Thread/ThreadTabPanel.html');

  /**
   * ThreadTabPanel displays the thread tab including its info heading, menu and details panel
   *
   * @class ThreadTabPanel
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var ThreadTabPanel = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_SideMenuHeaderTabsView
     * @final
     */
    constructorName: 'LOOM_ThreadTabPanel',

    className: "mas-threadTabPanel",

    /**
     * The template used to help structuring the view
     * @property template
     * @type {String}
     */
    template: template,

    /**
     * @type undefined|ServiceManager
     */
    serviceManager: undefined,

    /**
     * keeps track of the selected thread
     */
    selectedThread: null,

    /**
     * keeps track of the currently selected thread tab
     */
    /**
     * @property {SideMenuSelectedInfoView} selectedInfoView
     */
    selectedInfoView: null,
    threadTabsView: null,
    threadConfigurationControllerPanel: null,
    noThreadSelectedPanel: null,

    /**
     * @method initialize
     */
    initialize: function (options) {
      BaseView.prototype.initialize.apply(this, arguments);
      this.serviceManager = options.serviceManager;
      this.selectedInfoView = new ThreadSelectedInfoView({model: this});
      this.threadTabsView = new ThreadTabsView({model: this});

      /**
       * When a thread is selected, we create all its views and hide everything except the overview
       */
      this.listenTo(this.EventBus, 'thread:selected', function(event) {
        this.selectedThread = event.threadView;
        this.selectedThreadTab = this.threadTabsView.TAB_THREAD_CONFIGURATION;
        this.$('.mas-threadTabPanel--panel').empty();
        this.threadConfigurationControllerPanel = new ThreadConfigurationControllerPanel({
          model: this.selectedThread.model,
          serviceManager: this.serviceManager
      });
      this.threadActionControllerPanel = new ThreadActionControllerPanel({
        model: this.selectedThread.model,
        thread: event.thread,
        threadView: event.threadView
      });
      this.$('.mas-threadTabPanel--panel')
        .append(this.threadConfigurationControllerPanel.$el)
        .append(this.threadActionControllerPanel.$el);
        this.threadConfigurationControllerPanel.$el.hide();
        this.render();
      });

      /**
       * when a thread is unselected, we remove all its views and replace with an unselected one
       */
      this.listenTo(this.EventBus, 'thread:unselected', function() {
        this.selectedThread = null;
        this.threadConfigurationControllerPanel.remove();
        this.threadConfigurationControllerPanel = null;
        this.threadActionControllerPanel.remove();
        this.threadActionControllerPanel = null;
        this.renderNoThreadSelected();
        this.render();
      });

      this.listenTo(this.EventBus, 'side-menu:thread:tab:selected', function(event) {
        this.selectedThreadTab = event.tab;
        this.render();
      });

      this.render();
    },

    /**
     * Renders the various components into the tabbed side menu view
     * @returns {ThreadTabPanel}
     * @chainable
     */
    render: function() {
      this.$('.mas-threadTabPanel--info').replaceWith(this.selectedInfoView.el);
      this.$('.mas-threadTabPanel--menu').append(this.threadTabsView.$el);
      this.renderThreadTabPanels();
      return this;
    },

    renderThreadTabPanels: function() {
      if (this.selectedThread) {
        this.toggleThreadPanels();
      } else {
        this.renderNoThreadSelected();
      }
    },

    toggleThreadPanels: function() {
      if (this.selectedThreadTab === this.threadTabsView.TAB_THREAD_CONFIGURATION) {
        this.threadConfigurationControllerPanel.$el.show();
        this.threadActionControllerPanel.$el.hide();
      } else {
        this.threadConfigurationControllerPanel.$el.hide();
        this.threadActionControllerPanel.$el.show();
      }
    },

    /**
     * be a good citizen and clean up after yourself
     * @method remove
     */
    remove: function() {
      this.selectedInfoView.remove();
      this.threadTabsView.remove();
      this.noThreadSelectedPanel.remove();
      this.threadConfigurationControllerPanel.remove();
      this.threadActionControllerPanel.remove();
      BaseView.prototype.remove.apply(this);
    },

    renderNoThreadSelected: function() {
      this.$('.mas-threadTabPanel--panel').empty().append('<div class="mas-noThreadSelectedPanel">Please select a thread</div>');
    }

  });

  return ThreadTabPanel;
});
