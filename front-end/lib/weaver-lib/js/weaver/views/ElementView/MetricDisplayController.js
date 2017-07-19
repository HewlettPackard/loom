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
  var MetricValueView = require('weaver/views/MetricValueView');

  /**
   * @class  MetricDisplayController
   * @module weaver
   * @submodule views.ElementView
   * @namespace  views.ElementView
   * @method MetricDisplayController
   * @param view
   * @param metrics
   * @constructor
   */
  function MetricDisplayController(view, metrics) {
    this.view = view;
    this.views = {};
    this.metrics = metrics;
    this.activate();
  }

  _.extend(MetricDisplayController.prototype, {
    /**
     * @method displayMetric
     * @param metric
     */
    displayMetric: function (metric) {
      var view = new MetricValueView({
        model: this.view.model,
        metric: metric
      });
      view.$el.addClass('mas-element--content');
      this.view.$el.append(view.el);
      this.views[metric.id] = view;
    },

    /**
     * @method removeMetric
     * @param metric
     */
    removeMetric: function (metric) {
      var view = this.views[metric.id];
      view.remove();
    },

    /**
     * @method activate
     */
    activate: function () {
      if (this.metrics) {
        this.metrics.on('add', this.displayMetric, this);
        this.metrics.on('remove', this.removeMetric, this);
        this.metrics.forEach(this.displayMetric, this);
      }
    },

    /**
     * @method deactivate
     */
    deactivate: function () {
      if (this.metrics) {
        this.metrics.off('add', this.displayMetric, this);
        this.metrics.off('remove', this.removeMetric, this);
      }
    }
  });

  return MetricDisplayController;

});
