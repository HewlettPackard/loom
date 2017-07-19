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
  var template = require('./ThreadMetricsConfigurationPanel.html');
  var MetricsMenu = require('weaver/views/ThreadViewHeader/MetricsMenu');

  /**
   * ThreadMetricsConfigurationPanel displays the thread overview panel when selected
   *
   * @class ThreadMetricsConfigurationPanel
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var ThreadMetricsConfigurationPanel = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_ThreadMetricsConfigurationPanel
     * @final
     */
    constructorName: 'LOOM_ThreadMetricsConfigurationPanel',

    className: "mas-threadMetricsConfiguration mas-sideMenuContentPanel",

    template: template,

    events: {
      'click .mas-dialog--back': function(event) {
        event.preventDefault();
        this.EventBus.trigger('thread:configuration:cancel');
      },
      'click .mas-dialog--clear-all': function(event) {
        event.preventDefault();
        this.EventBus.trigger('thread:configuration:metrics:clear-all');
      }
    },

    /**
     * options.model = LOOM_Thread
     * @method initialize
     */
    initialize: function (options) {
      BaseView.prototype.initialize.apply(this, arguments);
      this.model = options.model;
      this.render();
    },

    /**
     * Renders the metrics list into this component
     * @returns {ThreadMetricsConfigurationPanel}
     * @chainable
     */
    render: function() {
      this.metricsMenu = new MetricsMenu({
        el: this.$('.mas-threadOverviewPanel--properties'),
        model: this.model
      });
      return this;
    },

    /**
     * @method remove
     */
    remove: function () {
      BaseView.prototype.remove.apply(this, arguments);
      this.metricsMenu.remove();
    }
  });

  return ThreadMetricsConfigurationPanel;
});
