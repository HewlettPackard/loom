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
  var FiberOverviewPanel = require('weaver/views/SideMenu/Fiber/FiberOverviewPanel');
  var FiberActionControllerPanel = require('weaver/views/SideMenu/Fiber/FiberActionControllerPanel');
  var FiberAlertsControllerPanel = require('weaver/views/SideMenu/Fiber/FiberAlertsControllerPanel');
  var FiberSelectedInfoView = require('weaver/views/SideMenu/Fiber/FiberSelectedInfoView');
  var FiberTabsView = require('weaver/views/SideMenu/Fiber/FiberTabsView');
  var template = require('weaver/views/SideMenu/Fiber/FiberTabPanel.html');

  /**
   * FiberTabPanel displays the fiber tab including its info heading, menu and details panel
   *
   * @class FiberTabPanel
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var FiberTabPanel = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_FiberTabPanel
     * @final
     */
    constructorName: 'LOOM_FiberTabPanel',

    className: "mas-fiberTabPanel",

    /**
     * The template used to help structuring the view
     * @property template
     * @type {String}
     */
    template: template,

    /**
     * keeps track of the selected fiber
     */
    selectedFiber: null,
    /**
     * keeps track of the currently selected fiber tab
     */
    /**
     * @property {SideMenuSelectedInfoView} selectedInfoView
     */
    selectedInfoView: null,
    fiberTabsView: null,
    fiberOverviewPanel: null,
    noFiberSelectedPanel: null,

    /**
     * @type undefined|ServiceManager
     */
    serviceManager: undefined,
    /**
     * @method initialize
     */
    initialize: function (options) {
      BaseView.prototype.initialize.apply(this, arguments);
      this.serviceManager = options.serviceManager;
      this.selectedInfoView = new FiberSelectedInfoView({model: this});
      this.fiberTabsView = new FiberTabsView({model: this});

      /**
       * When a fiber is selected, we create all its views and hide everything except the overview
       */
      this.listenTo(this.EventBus, 'fiber:selected', function(event) {
        this.selectedFiber = event.fiberView;
        this.selectedFiberTab = this.fiberTabsView.TAB_FIBER_OVERVIEW;
        this.$('.mas-fiberTabPanel--panel').empty();
        this.fiberOverviewPanel = new FiberOverviewPanel({
          model: this.selectedFiber.model
        });
        this.fiberActionControllerPanel = new FiberActionControllerPanel({
          model: this.selectedFiber.model,
          thread: event.thread,
          threadView: event.threadView
        });
        this.fiberAlertsControllerPanel = new FiberAlertsControllerPanel({
          model: this.selectedFiber.model,
          thread: event.thread,
          threadView: event.threadView
        });
        this.$('.mas-fiberTabPanel--panel')
          .append(this.fiberOverviewPanel.$el)
          .append(this.fiberActionControllerPanel.$el)
          .append(this.fiberAlertsControllerPanel.$el);
        this.fiberOverviewPanel.$el.hide();
        this.render();
      });

      /**
       * when a fiber is unselected, we remove all its views and replace with an unselected one
       */
      this.listenTo(this.EventBus, 'fiber:unselected', function() {
        this.selectedFiber = null;
        this.fiberOverviewPanel.stopListening();
        this.fiberOverviewPanel.remove();
        this.fiberOverviewPanel = null;
        this.fiberActionControllerPanel.stopListening();
        this.fiberActionControllerPanel.remove();
        this.fiberActionControllerPanel = null;
        this.fiberAlertsControllerPanel.stopListening();
        this.fiberAlertsControllerPanel.remove();
        this.fiberAlertsControllerPanel = null;
        this.renderNoFiberSelected();
        this.render();
      });

      this.listenTo(this.EventBus, 'side-menu:fiber:tab:selected', function(event) {
        this.selectedFiberTab = event.tab;
        this.render();
      });

      this.render();
    },

    /**
     * Renders the various components into the tabbed side menu view
     * @returns {FiberTabPanel}
     * @chainable
     */
    render: function() {
      this.$('.mas-fiberTabPanel--info').replaceWith(this.selectedInfoView.el);
      this.$('.mas-fiberTabPanel--menu').append(this.fiberTabsView.$el);
      this.renderFiberTabPanels();
      return this;
    },

    renderFiberTabPanels: function() {
      if (this.selectedFiber) {
        this.toggleFiberPanels();
      } else {
        this.renderNoFiberSelected();
      }
    },

    toggleFiberPanels: function() {
      this.fiberOverviewPanel.$el.hide();
      this.fiberActionControllerPanel.$el.hide();
      this.fiberAlertsControllerPanel.$el.hide();

      if (this.selectedFiberTab === this.fiberTabsView.TAB_FIBER_OVERVIEW) {
        this.fiberOverviewPanel.$el.show();
      } else if (this.selectedFiberTab === this.fiberTabsView.TAB_FIBER_ACTIONS)  {
        this.fiberActionControllerPanel.$el.show();
      } else if (this.selectedFiberTab === this.fiberTabsView.TAB_FIBER_ALERTS)  {
        this.fiberAlertsControllerPanel.$el.show();
      }
    },

    /**
     * be a good citizen and clean up after yourself
     * @method remove
     */
    remove: function() {
      this.selectedInfoView.remove();
      this.fiberTabsView.remove();
      this.noFiberSelectedPanel.remove();
      this.fiberOverviewPanel.remove();
      this.fiberActionControllerPanel.remove();
      this.fiberAlertsControllerPanel.remove();
      BaseView.prototype.remove.apply(this);
    },

    renderNoFiberSelected: function() {
      this.$('.mas-fiberTabPanel--panel').empty().append('<div class="mas-noFiberSelectedPanel">Please select a fiber</div>');
    }

  });

  return FiberTabPanel;
});
