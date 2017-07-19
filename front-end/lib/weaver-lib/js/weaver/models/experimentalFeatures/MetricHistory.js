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
define([
  'lodash',
  'jquery',
  'weaver/models/ExperimentalFeature',
  'weaver/views/MetricValueView'
], function (_, $, ExperimentalFeature, MetricValueView) {

  "use strict";
  /**
   * @class  MetricHistory
   * @module  weaver
   * @submodule models.experimentalFeatures
   * @namespace  models.experimentalFeatures
   */
  var MetricHistory = new ExperimentalFeature({
    id: 'metric-history',
    name: 'Metric history'
  });

  /**
   * Enables the metric history feature.
   * todo: how is this different from the ExperimentalFeature.enabled property? Should it be renamed OnEnableFeature?
   * @method enableFeature
   */
  MetricHistory.enableFeature = function () {
    MetricValueView.prototype.metricHistoryFeature = true;
    this.refreshMetricViews();
  };

  /**
   * Enables the metric history feature.
   * todo: how is this different from the ExperimentalFeature.enabled property? Should it be renamed OnDisableFeature?
   * @method disableFeature
   */
  MetricHistory.disableFeature = function () {
    MetricValueView.prototype.metricHistoryFeature = false;
    this.refreshMetricViews();
  };

  /**
   * todo: I wonder if this function should be private? Is it ever called externally? or does it just react to change:enabled events
   * @method refreshMetricViews
   */
  MetricHistory.refreshMetricViews = function () {
    var views = this.weaver.getMetricViews();
    _.forEach(views, this.refreshMetricView);
  };

  /**
   * todo: I wonder if this function should be private? Is it ever called externally? or does it just react to change:enabled events
   * @method refreshMetricView
   * @param view
   */
  MetricHistory.refreshMetricView = function (view) {
    view._updateMetrics();
  };

  /**
   * Listens to the change events on the {ExperimentalFeature.enabled} property and enables/disables the feature accordingly
   */
  MetricHistory.on('change:enabled', function (feature, enabled) {
    if (enabled) {
      feature.enableFeature();
    } else {
      feature.disableFeature();
    }
  });

  return MetricHistory;
});
