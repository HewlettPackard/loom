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
  var BaseView = require('./../../BaseView');
  var PatternList = require('weaver/views/SideMenu/Tapestry/Patterns/PatternList');
  var ProviderList = require('weaver/views/SideMenu/Tapestry/Providers/ProviderList');
  var TapestrySelectedInfoView = require('weaver/views/SideMenu/Tapestry/TapestrySelectedInfoView');
  var TapestryTabsView = require('weaver/views/SideMenu/Tapestry/TapestryTabsView');
  var TapestryRelationsTabPanel = require('weaver/views/SideMenu/Tapestry/TapestryRelationsTabPanel');
  var template = require('weaver/views/SideMenu/Tapestry/TapestryTabPanel.html');

  /**
   * TapestryTabPanel displays the tapestry tab including its info heading, menu and details panel
   *
   * @class TapestryTabPanel
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var TapestryTabPanel = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_TapestryTabPanel
     * @final
     */
    constructorName: 'LOOM_TapestryTabPanel',

    className: "mas-tapestryTabPanel",

    /**
     * The template used to help structuring the view
     * @property template
     * @type {String}
     */
    template: template,

    /**
     * @property {SideMenuSelectedInfoView} selectedInfoView
     */
    selectedInfoView: null,
    tapestryTabsView: null,
    relationsTab: null,
    selectedTab: null,
    providersTab: null,
    patternsTab: null,

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
      this.selectedInfoView = new TapestrySelectedInfoView({
        model: this.serviceManager.get('AggregatorClient').get('providers')
      });
      this.tapestryTabsView = new TapestryTabsView();
      this.providersTab = new ProviderList({
        serviceManager: this.serviceManager,
        model: this.serviceManager.get('AggregatorClient').get('providers')
      });
      this.patternsTab = new PatternList({
        model: this.serviceManager.get('AggregatorClient').get('availablePatterns')
      });
      this.relationsTab = new TapestryRelationsTabPanel();
      this.selectedTab = this.tapestryTabsView.TAB_TAPESTRY_PATTERNS;

      this.listenTo(this.EventBus, 'side-menu:tapestry:tab:selected', function(event) {
        this.selectedTab = event.tab;
        this.render();
      });
      this.render();
    },

    /**
     * Renders the various components into the tabbed side menu view
     * @returns {TapestryTabPanel}
     * @chainable
     */
    render: function() {
      this.$('.mas-tapestryTabPanel--info').append(this.selectedInfoView.$el);
      this.$('.mas-tapestryTabPanel--menu').append(this.tapestryTabsView.$el);
      this.$('.mas-tapestryTabPanel--panel')
        .append(this.providersTab.$el)
        .append(this.patternsTab.$el)
        .append(this.relationsTab.$el);

      this._toggleTab();
      return this;
    },

    _toggleTab: function() {
      if (this.selectedTab === this.tapestryTabsView.TAB_TAPESTRY_PROVIDERS) {
        this.providersTab.$el.removeClass('hideFlex');
        this.patternsTab.$el.addClass('hideFlex');
        this.relationsTab.$el.addClass('hideFlex');
      } else if (this.selectedTab === this.tapestryTabsView.TAB_TAPESTRY_PATTERNS) {
        this.providersTab.$el.addClass('hideFlex');
        this.patternsTab.$el.removeClass('hideFlex');
        this.relationsTab.$el.addClass('hideFlex');
      } else if (this.selectedTab === this.tapestryTabsView.TAB_TAPESTRY_RELATIONS) {
        this.providersTab.$el.addClass('hideFlex');
        this.patternsTab.$el.removeClass('hideFlex');
        this.relationsTab.$el.addClass('hideFlex');
      }
    },

    /**
     * be a good citizen and clean up after yourself
     * @method remove
     */
    remove: function() {
      this.selectedInfoView.remove();
      this.tapestryTabsView.remove();
      this.providersTab.remove();
      this.patternsTab.remove();
      this.relationsTab.remove();
      BaseView.prototype.remove.apply(this);
    }

  });

  return TapestryTabPanel;
});
