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
  var BaseView = require('weaver/views/BaseView');
  var SideMenuHeaderTabsView = require('weaver/views/SideMenu/SideMenuHeaderTabsView');
  var FiberTabPanel = require('weaver/views/SideMenu/Fiber/FiberTabPanel');
  var ThreadTabPanel = require('weaver/views/SideMenu/Thread/ThreadTabPanel');
  var TapestryTabPanel = require('weaver/views/SideMenu/Tapestry/TapestryTabPanel');
  var template = require('weaver/views/SideMenu/SideMenuTabbedView.html');

  /**
   * SideMenuTabbedView displays the components required to render a tabbed side menu view. It is a composite wrapper
   * around other component views.
   *
   * @class SideMenuTabbedView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var SideMenuTabbedView = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_SideMenuTabbedView
     * @final
     */
    constructorName: 'LOOM_SideMenuTabbedView',

    className: "mas-sideMenuTabbedView",

    /**
     * The template used to help structuring the view
     * @property template
     * @type {String}
     */
    template: template,

    sideMenuHeaderTabsView: null,
    fiberTabPanel: null,
    threadTabPanel: null,
    tapestryTabPanel: null,

    /**
     * @type undefined|ServiceManager
     */
    serviceManager: undefined,

    events: {
      'click .mas-sideMenuListItemValue': function(event) {
        var element = this.$el.find(event.currentTarget);
        if (element.hasClass('mas-sideMenuListItemValue')) {
          element.toggleClass('mas-hasEllipsesExpanded');
        }
      }
    },

    /**
     * @method initialize
     */
    initialize: function (options) {
      BaseView.prototype.initialize.apply(this, arguments);
      this.serviceManager = options.serviceManager;
      this.sideMenuHeaderTabsView = new SideMenuHeaderTabsView();
      this.fiberTabPanel = new FiberTabPanel({serviceManager: this.serviceManager});
      this.threadTabPanel = new ThreadTabPanel({serviceManager: this.serviceManager});
      this.tapestryTabPanel = new TapestryTabPanel({serviceManager: this.serviceManager});
      this.listenTo(this.EventBus, 'side-menu:tab:selected', function(event){
        $('.mas-sideMenuInfoPanel').each(function() {
          $( this ).removeClass( "is-selected" );
        });
        if (event.tab === this.sideMenuHeaderTabsView.TAB_FIBER) {
          $('.mas-fiberTabPanel').addClass('is-selected');
        } else if (event.tab === this.sideMenuHeaderTabsView.TAB_THREAD) {
          $('.mas-threadTabPanel').addClass('is-selected');
        } else if (event.tab === this.sideMenuHeaderTabsView.TAB_TAPESTRY) {
          $('.mas-tapestryTabPanel').addClass('is-selected');
        }
      });
      this.render();
    },

    /**
     * Renders the various components into the tabbed side menu view
     * @returns {SideMenuTabbedView}
     * @chainable
     */
    render: function() {
      this.$('.mas-sideMenuTabbed--tabs').replaceWith(this.sideMenuHeaderTabsView.el);
      this.$('.mas-sideMenuTabbed--tabs-panel')
        .append(this.fiberTabPanel.$el)
        .append(this.threadTabPanel.$el)
        .append(this.tapestryTabPanel.$el);
      return this;
    },

    /**
     * @warning make sure to add other classes once added
     */
    remove:function() {
      this.sideMenuHeaderTabsView.remove();
      this.fiberTabPanel.remove();
      this.threadTabPanel.remove();
      this.tapestryTabPanel.remove();
      BaseView.prototype.remove.apply(this);
    }

  });

  return SideMenuTabbedView;
});
