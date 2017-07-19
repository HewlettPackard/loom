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

  var _ = require('lodash');
  //var Menu = require('weaver/views/Menu');
  var BaseView = require('weaver/views/BaseView');
  var PropertySelectorSideMenu = require('weaver/views/PropertySelectorSideMenu');

  /**
   * Menu for configuring which metrics are displayed for a given Thread
   * @class MetricsMenu
   * @namespace views.ThreadViewHeader
   * @module  weaver
   * @submodule views.ThreadViewHeader
   * @extends Menu
   * @constructor
   */
  var MetricsMenu = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_MetricsMenu
     * @final
     */
    constructorName: 'LOOM_MetricsMenu',

    /**
     * @property className
     * @type {String}
     */
    className: 'mas-metricsMenu',

    /**
     * Dynamic interpretation of Backbone events
     * @method events
     * @returns {function(): any}
     */
    events: {

    },

    /**
     * model: {LOOM_Thread}
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this._initializePropertySelector();
      if (this.model) {
        // Listen to reset:elements in case the Thread goes from having aggregations
        // to having items, or vice-versa
        this.listenTo(this.model, 'reset:elements', this._updateAvailableMetrics);
        this.listenTo(this.model.get('metrics'), 'remove', this._removeMetricFromSelection);
      }

      this.listenTo(this.EventBus, 'thread:configuration:metrics:clear-all', function() {
        this.propertySelector.clearSelection();
      });
      this.render();
    },

    /**
     * @method _initializePropertySelector
     * @private
     */
    _initializePropertySelector: function () {
      var propertySelector = this.propertySelector = new PropertySelectorSideMenu({
        title: 'Metrics',
        multiple: true,
        model: this.model ? _.indexBy(this.model.getAvailableMetrics(), 'id') : {},
        thread: this.model,
        additionalItemClass: 'mas-threadConfiguration--metric'
      });
      if (this.model) {
        this.model.get('metrics').forEach(function (metric) {
          propertySelector.select(metric.id, true);
        });
      }
      this.listenTo(this.propertySelector, 'change:selection', this._updateMetrics);
      // we could also let the thread know at this point that the metrics have 'changed'
    },

    /**
     * @method render
     */
    render: function () {
      BaseView.prototype.render.apply(this, arguments);
      this.$el.addClass(this.className);
      this.propertySelector.$el.addClass('mas-menu--content');
      this.el.appendChild(this.propertySelector.el);
    },

    /**
     * @method _updateMetrics
     * @param selection
     * @private
     */
    _updateMetrics: function (selection) {
      // IMPROVE: Maybe encapsulate that in the setDisplayedMetrics()
      // method and let it take a list of IDs as a parameter ?
      var selectedMetrics = _(this.model.getAvailableMetrics()).indexBy('id').pick(selection)
        .values()
        .value();
      this.model.setDisplayedMetrics(selectedMetrics);
    },

    /**
     * @method _updateAvailableMetrics
     * @private
     */
    _updateAvailableMetrics: function () {
      this.propertySelector.setProperties(_.indexBy(this.model.getAvailableMetrics(), 'id'));
    },

    /**
     * @method  _removeMetricFromSelection
     * @param metric
     * @private
     */
    _removeMetricFromSelection: function (metric) {
      this.propertySelector.unselect(metric.id);
    },

    /**
     * @method remove
     */
    remove: function () {
      BaseView.prototype.remove.apply(this, arguments);
      this.propertySelector.remove();
    }
  });

  return MetricsMenu;
});