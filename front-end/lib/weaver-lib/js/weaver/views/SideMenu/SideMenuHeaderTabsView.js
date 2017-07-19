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
  var $ = require('jquery');
  var _ = require('lodash');
  var BaseView = require('./../BaseView');
  var template = require('./SideMenuHeaderTabsView.html');

  /**
   * SideMenuHeaderTabsView displays the header tabs in the side menu
   * @class SideMenuHeaderTabsView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var SideMenuHeaderTabsView = BaseView.extend({

    TAB_FIBER: 'fiber',
    TAB_THREAD: 'thread',
    TAB_TAPESTRY: 'tapestry',

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_SideMenuHeaderTabsView
     * @final
     */
    constructorName: 'LOOM_SideMenuHeaderTabsView',

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
    className: 'mas-sideMenuHeaderTabs',

    /**
     * Should be injected via options to enable Selection services for tabs
     * @property {ServiceManager|null} serviceManager
     */
    serviceManager: null,

    /**
     * @property events
     * @type {Object}
     */
    events: {
      'click .mas-sideMenuHeaderTabs--fiber': function() {
        this.selectTab(this.TAB_FIBER);
      },
      'click .mas-sideMenuHeaderTabs--thread': function() {
        this.selectTab(this.TAB_THREAD);
      },
      'click .mas-sideMenuHeaderTabs--tapestry': function() {
        this.selectTab(this.TAB_TAPESTRY);
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
      this.listenTo(this.EventBus, 'fiber:selected', function(){this.selectTab(this.TAB_FIBER);});
      this.listenTo(this.EventBus, 'fiber:unselected', this.doNothingOnUnselect);
      this.listenTo(this.EventBus, 'thread:selected', function(){this.selectTab(this.TAB_THREAD);});
      this.listenTo(this.EventBus, 'thread:unselected', this.doNothingOnUnselect);
      this.listenTo(this.EventBus, 'side-menu:tab:selected', function(event){
        $('.mas-sideMenuHeaderTabs--'+event.tab).addClass('is-selected');
      });
      this.render();
    },

    /**
     * Selects a tab and renders the view
     * - no state of the selected tab or thread should be held in this view.
     * @param tab
     */
    selectTab: function(tab) {
      $('.mas-sideMenuHeaderTabs li').each(function() {
        $( this ).removeClass( "is-selected" );
      });
      this.selectedTab = tab;
      this.EventBus.trigger('side-menu:tab:selected', {tab: tab});
      this.render();
    },

    unselectTab: function(tab) {
      if (this.selectedTab === tab) {
        this.EventBus.trigger('side-menu:tab:unselected', {tab: tab});
      }
      this.selectedTab = null;
      this.render();
    },

    /**
     * This function is used to explicitly highlight the fact we should do nothing when a fiber or a thread is
     * unselected
     */
    doNothingOnUnselect: function(event) {
      _.noop(event);
    }

  });

  return SideMenuHeaderTabsView;
});
