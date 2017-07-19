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
  var template = require('./TapestryTabsView.html');

  /**
   * TapestryTabsView displays the fiber tabs in the side menu
   * @class TapestryTabsView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var TapestryTabsView = BaseView.extend({

    TAB_TAPESTRY_PROVIDERS: 'providers',
    TAB_TAPESTRY_PATTERNS: 'patterns',
    TAB_TAPESTRY_RELATIONS: 'relations',

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_TapestryTabsView
     * @final
     */
    constructorName: 'LOOM_TapestryTabsView',

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
    className: 'mas-tapestryTabsView',

    /**
     * @property events
     * @type {Object}
     */
    events: {
      'click .mas-tapestryTabs--providers': function() {
        this.selectTab(this.TAB_TAPESTRY_PROVIDERS);
      },
      'click .mas-tapestryTabs--patterns': function() {
        this.selectTab(this.TAB_TAPESTRY_PATTERNS);
      },
      'click .mas-tapestryTabs--relations': function() {
        this.selectTab(this.TAB_TAPESTRY_RELATIONS);
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
      this.listenTo(this.EventBus, 'side-menu:tapestry:tab:selected', function(event){
        $('.mas-tapestryTabs--'+event.tab).addClass('is-selected');
      });
      this.selectedTab = this.TAB_TAPESTRY_PATTERNS;
      this.render();
    },

    /**
     * Selects a tab and renders the view
     * - no state of the selected tab or thread should be held in this view.
     * @param tab
     */
    selectTab: function(tab) {
      if (this.selectedTab !== null) {
        $('.mas-tapestryTabs--'+this.selectedTab).removeClass('is-selected');
      }
      this.selectedTab = tab;
      this.EventBus.trigger('side-menu:tapestry:tab:selected', {tab: tab});
      this.render();
    },

    unselectTab: function(tab) {
      if (this.selectedTab === tab) {
        this.EventBus.trigger('side-menu:tapestry:tab:unselected', {tab: tab});
      }
      this.selectedTab = null;
      this.render();
    }

  });

  return TapestryTabsView;
});
