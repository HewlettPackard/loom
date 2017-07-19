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
  var template = require('./ThreadTabsView.html');

  /**
   * ThreadTabsView displays the thread tabs in the side menu
   * @class ThreadTabsView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var ThreadTabsView = BaseView.extend({

    TAB_THREAD_CONFIGURATION: 'configuration',
    TAB_THREAD_ACTIONS: 'actions',

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_SideMenuHeaderTabsView
     * @final
     */
    constructorName: 'LOOM_ThreadTabsView',

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
    className: 'mas-threadTabs',

    /**
     * @property events
     * @type {Object}
     */
    events: {
      'click .mas-threadTabs--configuration': function() {
        this.selectTab(this.TAB_THREAD_CONFIGURATION);
      },
      'click .mas-threadTabs--actions': function() {
        this.selectTab(this.TAB_THREAD_ACTIONS);
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
      this.listenTo(this.EventBus, 'thread:selected', function(){this.selectTab(this.TAB_THREAD_CONFIGURATION);});
      this.listenTo(this.EventBus, 'thread:unselected', function(){this.selectTab(this.TAB_THREAD_CONFIGURATION);});
      this.listenTo(this.EventBus, 'side-menu:thread:tab:selected', function(event){
        $('.mas-threadTabs--'+event.tab).addClass('is-selected');
      });
      this.render();
    },

    /**
     * Selects a tab and renders the view
     * - no state of the selected tab or thread should be held in this view.
     * @param tab
     */
    selectTab: function(tab) {
      $('.mas-threadTabs li').each(function() {
        $( this ).removeClass( "is-selected" );
      });
      this.selectedTab = tab;
      this.EventBus.trigger('side-menu:thread:tab:selected', {tab: tab});
      this.render();
    }
  });

  return ThreadTabsView;
});
