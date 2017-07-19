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
  var BaseView = require('weaver/views/BaseView');

  /**
   * The ThreadViewHeaderMetrics displays the list of metrics currently displayed on the Thread and allows the user
   * to remove them.
   *
   * @class ThreadViewHeaderMetrics
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var ThreadViewHeaderMetrics = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_ThreadMetricsView
     * @final
     */
    constructorName: 'LOOM_ThreadMetricsView',

    /**
     * @property tagName
     * @type {String}
     */
    tagName: 'ul',

    /**
     * @property className
     * @type {String}
     */
    className: 'mas-threadMetrics',

    /**
     * @property events
     * @type {Object}
     */
    // events: {
    //   'click .mas-action-removeMetric': function (event) {
    //     // we dont have the remove button any more..
    //     var metric = $(event.target).data('metric');
    //     this.model.get('metrics').remove(metric);
    //   }
    // },

    /**
     * @method initialize
     */
    initialize: function () {
      this.$HTMLElements = {};
      this.render();
      this.listenTo(this.model.get('metrics'), 'add', this._renderMetric);
      this.listenTo(this.model.get('metrics'), 'remove', this._removeMetric);

      //this.model.get('metrics').on('add', this._renderMetric, this);
      //this.model.get('metrics').on('remove', this._removeMetric, this);
    },

    /**
     * @method render
     */
    render: function () {
      this.model.get('metrics').forEach(this._renderMetric, this);
    },

    /**
     * @method _renderMetric
     * @param metric
     * @private
     */
    _renderMetric: function (metric) {
      var $HTMLElement = $('<li class="mas-threadMetrics--metric"></li>');
      $HTMLElement[0].innerHTML += metric.get('name');
      $HTMLElement.find('button').data('metric', metric);
      $HTMLElement.appendTo(this.el);
      this.$HTMLElements[metric.id] = $HTMLElement;
    },

    /**
     * @method _removeMetric
     * @param metric
     * @private
     */
    _removeMetric: function (metric) {
      var $HTMLElement = this.$HTMLElements[metric.id];
      if ($HTMLElement) {
        $HTMLElement.remove();
      }
    }
  });

  return ThreadViewHeaderMetrics;

});
