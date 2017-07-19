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
define(function (require) {

  "use strict";

  var $ = require('jquery');
  /** @type BaseView */
  var BaseView = require('./../BaseView');
  var template = require('./ToolbarView.html');
  var ConnectionStatusView = require('weaver/views/Toolbar/ConnectionStatusView');
  var FilterView = require('weaver/views/FilterView');
  var Menu = require('weaver/views/Menu');
  var MenuGroupController = require('weaver/views/helpers/MenuGroupController');
  var AboutView = require('weaver/views/Toolbar/AboutView');
  
  /**
   * The ToolbarView displays the toolbar used at the bottom of the TapestryScreen
   * @class      ToolbarView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var ToolbarView = BaseView.extend({

    /**
     * @property className
     * @type {undefined|String}
     */
    classname: undefined,

    /**
     * The template used to help structuring the view
     * @property template
     * @type {String}
     */
    template: template,

    /**
     * @property filterView
     * @type views.FilterView
     */
    filterView: undefined,

    /**
     * @property connectionStatusView
     * @type views.ConnectionStatusView
     */
    connectionStatusView: undefined,

    /**
     * @property aboutView
     * @type views.AboutView
     */
    aboutView: undefined,

    /**
     * @property events
     * @type {Object}
     */
    events: {
      'click .mas-action-clearFilterRelated': function() {
        this.EventBus.trigger('thread:fiber:action:clear-filter-related');
      }
    },

    /**
     * @method initialize
     */
    initialize: function (options) {
      this.options = options || {};
      BaseView.prototype.initialize.apply(this, arguments);
      this.serviceManager = options.serviceManager;
      if (this.options.services) {
        this._initializeToolbarMenus();
        this.filterView = new FilterView({
          el: this.$('.mas-filter'),
          service: this.serviceManager.get('PrimaryFilterService')
        });
        this.connectionStatusView = new ConnectionStatusView({
          el: this.$('.mas-connectionStatus'),
          model: this.serviceManager.get('StatusLoader')
        });
        this.aboutView = new AboutView({
          el: this.$('.mas-about'),
          service: this.serviceManager.get('StatusLoader')
        });
      }
      /**
       * The functionality to hide/show the clear related filter is currently under discussion, I have commented out
       * the listeners and default hiding behavior for now while simon decides on a suitable UX alternative.
       *
      this.listenTo(this.EventBus, 'thread:fiber:filter:enable', this.showClearFilterRelated);
      this.listenTo(this.EventBus, 'thread:fiber:action:clear-filter-related', this.hideClearFilterRelated);
      this.hideClearFilterRelated();
       */
    },

    /**
     * Shows the clear related filter icon / text block
     * @method showClearFilterRelated
     */
    showClearFilterRelated: function() {
      this.$el.find('.mas-clearFilterRelated').css('display', 'block');
    },

    /**
     * Hides the clear related filter icon / text block
     * @method hideClearFilterRelated
     */
    hideClearFilterRelated: function() {
      this.$el.find('.mas-clearFilterRelated').css('display', 'none');
    },

    /**
     * @method _initializeToolbarMenus
     * @private
     */
    _initializeToolbarMenus: function () {
      //this._initializePatternSelectionMenu(this.serviceManager.get('AggregatorClient').get('availablePatterns'));
      //this._initializeProviderSelectionMenu(this.serviceManager.get('AggregatorClient').get('providers'));
      //new Menu({el: this.$('.mas-relationTypeMenu')});
      //new Menu({el: this.$('.mas-relationsGraphMenu')});
      new Menu({el: this.$('.mas-aboutMenu')});
      new MenuGroupController({el: this.el, groupClass: 'mas-toolbarMenus'});
    },

    /**
     * @method _initializeProviderSelectionMenu
     * @param providers
     * @private
     */
    // _initializeProviderSelectionMenu: function (providers) {
    //   new CollectionNotificationMenu({
    //     el: this.$('.mas-providerSelectionMenu'),
    //     model: providers
    //   });
    // },

    /**
     * @method _initializePatternSelectionMenu
     * @param availablePatterns
     * @private
     */
    // _initializePatternSelectionMenu: function (availablePatterns) {
    //   new CollectionNotificationMenu({
    //     el: this.$('.mas-patternSelectionMenu'),
    //     model: availablePatterns
    //   });
    // },

    /**
     * @method showNotification
     * @param $notification
     */
    showNotification: function ($notification) {
      this.hideNotification();
      this.$notification = $notification.addClass('mas-toolbar--notification').prependTo(this.$el);
      this.dispatchCustomEvent('change:height', {
        toolbarHeight: this.$el.height()
      });
    },

    /**
     * @method hideNotification
     */
    hideNotification: function () {
      if (this.$notification) {
        this.$notification.remove().removeClass('mas-toolbar--notification');
        this.dispatchCustomEvent('change:height', {
          toolbarHeight: this.$el.height()
        });
      }
    },

    /**
     * @method remove
     */
    remove: function () {
      BaseView.prototype.remove.apply(this, arguments);
      //this.experimentalFeatureSelector.remove();
    },

    /**
     * Looks up in the DOM for all the ElementsView displayed by the Weaver
     * @method getElementViews
     * @return {Array}
     */
    getElementViews: function () {
      return $('.mas-element:not(.mas-element-empty)').map(function (index, element) {
        return $(element).data('view');
      }).toArray();
    },

    /**
     * Looks up in the DOM for all the ThreadElementsView displayed by the Weaver
     * @method  getTreadElementsViews
     * @return {Array}
     */
    getThreadElementsViews: function () {
      return $('.mas-elements').map(function (index, element) {
        return $(element).data('view');
      }).toArray();
    },

    /**
     * Looks up in the DOM for all the MetricValueViews displayed by the Weaver
     * @method getMetricViews
     * @return {Array}
     */
    getMetricViews: function () {
      return $('.mas-element-metric').map(function (index, element) {
        return $(element).data('view');
      }).toArray();
    }
  });

  return ToolbarView;
});
