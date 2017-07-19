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
  /** @type BaseView */
  var BaseView = require('./BaseView');

  var BarGraphView = require('weaver/views/Element/BarGraphView');
  var MetricValueLabelView = require('weaver/views/MetricValueLabelView');

  /**
   * MetricValueViews display the value of a specific metric
   * for the element they have as model
   * @class MetricValueView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */

  var MetricValueView = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_MetricValueView
     * @final
     */
    constructorName: 'LOOM_MetricValueView',

    /**
     * @property className
     * @type {String}
     */
    className: 'mas-metricValue',

    /**
     * The model whose metric is displayed
     * @property {models.Element} model
     */

    /**
     * The metric being displayed
     * @property {models.Metric} options.metric
     */

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      /**
       * The BarGraphView displaying the metric value
       * Todo: hard coded number of values, pass this into the view and pass it on through
       * @property {views.BarGraphView} graph
       * @todo move this to a property
       */
      this.graph = new BarGraphView({
        maximumNumberOfValues: 30
      });
      this.$el.append(this.graph.$el);
      this.graph.$el.addClass('mas-metricValue--graph');

      /**
       * The graph label
       * @property {views.MetricValueLabelView} label
       * @todo move this to a property
       */
      this.label = new MetricValueLabelView({
        metric: this.options.metric
      });
      this.$el.append(this.label.el);
      this.label.$el.addClass('mas-metricValue--label');
      this.listenTo(this.model, 'change:' + this.options.metric.id, this.render);
      this.listenTo(this.model, 'refresh', this.render);
      this.metricHistoryFeature = false;
      this.render();
    },

    /**
     * @method render
     */
    render: function () {
      var metricValue = this.model.getMetricValue(this.options.metric);
      if (this.model.get('selected') && this.metricHistoryFeature) {
        this._displayMetricHistory();
      } else {
        this._displayMetricValue(metricValue);
      }
      this._updateClass(metricValue);
      this._updateLabel(metricValue);
    },

    /**
     * @method _updateClass
     * @param metricValue
     * @private
     */
    _updateClass: function (metricValue) {
      if (_.isUndefined(metricValue)) {
        this.$el.addClass('mas-metricValue-noValue');
      } else {
        this.$el.removeClass('mas-metricValue-noValue');
      }
    },

    /**
     * @method _updateLabel
     * @param metricValue
     * @private
     */
    _updateLabel: function (metricValue) {
      this.label.render(metricValue);
    },

    /**
     * @method _displayMetricHistory
     * @private
     */
    _displayMetricHistory: function () {
      var metricHistory = this.model.getMetricHistory(this.options.metric);
      var normalisedHistory = this.options.metric.normalise(metricHistory);
      this.graph.render(normalisedHistory);
    },

    /**
     * @method _displayMetricValue
     * @param metricValue
     * @private
     */
    _displayMetricValue: function (metricValue) {
      var normalisedValue = this.options.metric.normalise(metricValue);
      var origin = this.options.metric.normalise(0);
      this.graph.render(normalisedValue, origin);
      if (this.options.metric.isInRange(0)) {
        this.graph.renderOriginAxis(origin);
      } else {
        this.graph.removeOriginAxis();
      }
    }
  });

  return MetricValueView;
});
