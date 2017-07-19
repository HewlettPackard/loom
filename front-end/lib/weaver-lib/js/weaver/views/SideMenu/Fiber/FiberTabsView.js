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

  var $ = require('jquery');
  /** @type BaseView */
  var BaseView = require('weaver/views/BaseView');
  var template = require('./FiberTabsView.html');

  /**
   * FiberTabsView displays the fiber tabs in the side menu
   * @class FiberTabsView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var FiberTabsView = BaseView.extend({

    TAB_FIBER_OVERVIEW: 'overview',
    TAB_FIBER_ACTIONS: 'actions',
    TAB_FIBER_ALERTS: 'alerts',

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_SideMenuHeaderTabsView
     * @final
     */
    constructorName: 'LOOM_FiberTabsView',

    /**
     * The template used to help structuring the view
     * @property template
     * @type {String}
     */
    template: template,

    /**
     * @property className
     * @type {String}
     */
    className: 'mas-fiberTabs',

    /**
     * @property events
     * @type {Object}
     */
    events: {
      'click .mas-fiberTabs--overview': function() {
        this.selectTab(this.TAB_FIBER_OVERVIEW);
      },
      'click .mas-fiberTabs--actions': function() {
        this.selectTab(this.TAB_FIBER_ACTIONS);
      },
      'click .mas-fiberTabs--alerts': function() {
        this.selectTab(this.TAB_FIBER_ALERTS);
      }
    },

    /**
     * Tells us which tab should be displayed when rendering
     * @property {String} tab
     */
    selectedTab: null,

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.listenTo(this.EventBus, 'fiber:selected', function(event){
        this.selectTab(this.TAB_FIBER_OVERVIEW);
        this._updateAlertTabNotification(event);
      });
      this.listenTo(this.EventBus, 'fiber:unselected', function(){
        this.selectTab(this.TAB_FIBER_OVERVIEW);
        this._hideAlertTabNotification();
      });
      this.listenTo(this.EventBus, 'side-menu:fiber:tab:selected', function(event){
        $('.mas-fiberTabs--'+event.tab).addClass('is-selected');
      });
      this.listenTo(this.EventBus, 'fiber:selected:alert:change', this._updateAlertTabNotification);
      this._hideAlertTabNotification();
      this.render();
    },

    /**
     * Selects a tab and renders the view
     * - no state of the selected tab or thread should be held in this view.
     * @param tab
     */
    selectTab: function(tab) {
      $('.mas-fiberTabs li').each(function() {
        $( this ).removeClass( "is-selected" );
      });
      this.selectedTab = tab;
      this.EventBus.trigger('side-menu:fiber:tab:selected', {tab: tab});
      this.render();
    },

    /**
     * Toggle the alert notification depending on the current alert status
     * @param event
     * @private
     */
    _updateAlertTabNotification: function(event) {
      if (event.fiber.alert.hasAlert()) {
        this._showAlertTabNotification(event.fiber.alert);
      } else {
        this._hideAlertTabNotification();
      }
    },

    /**
     * Utility function to hide the alert
     * @private
     */
    _hideAlertTabNotification: function() {
      this.$el.find('.mas-alertNotification').hide();
    },

    /**
     * Utility function to show the alert
     * - clears out any previous alert classes from the element to avoid level conflicts
     * @private
     */
    _showAlertTabNotification: function(alert) {
      this.$el
        .find('.mas-alertNotification')
        .removeClass (function (index, css) { // clear out current notification level classname
          return (css.match (/\mas-alertNotification\S+/g) || []).join(' ');
        })
        .addClass('mas-alertNotification-'+alert.get('level'))
        .show();
    }
  });

  return FiberTabsView;
});
