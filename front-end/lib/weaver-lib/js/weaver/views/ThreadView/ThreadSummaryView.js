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
define(['jquery'], function ($) {

  "use strict";

  /** @type BaseView */
  var BaseView = require('./../BaseView');

  /**
   * It is used by the {{#crossLink "views.ThreadViewHeader"}}{{/crossLink}} to render the number of items and number
   * of alerts
   *
   * @class ThreadSummaryView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var ThreadSummaryView = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_ThreadSummaryView
     * @final
     */
    constructorName: 'LOOM_ThreadSummaryView',

    /**
     * @property className
     * @type {String}
     */
    className: 'mas-threadSummary',

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.$numberOfItems = $('<div class="mas-threadSummary--numberOfItems">').appendTo(this.$el);
      this.$numberOfAlerts = $('<div class="mas-threadSummary--numberOfAlerts">').appendTo(this.$el);
      if (this.model) {
        //this.listenTo(this.model, 'reset:elements', this.render, this);
        this.listenTo(this.model.get('result'), 'change:pending', this.render);
        this.listenTo(this.model, 'change:result', this.render);
        this.listenTo(this.model.get('elements'), 'change:numberOfItems', this.render);
        this.render();
      }
    },

    /**
     * @method render
     */
    render: function () {
      var summary = this.model.getSummary();
      this.$numberOfItems.html(summary.numberOfItems > 1 ? summary.numberOfItems + ' items' : summary.numberOfItems + ' item');
      this.$numberOfAlerts.html(summary.numberOfAlerts);
      this._updateAlertsState(summary);
      var result = this.model.get('result');
      this._updatePendingState(result === undefined || result.get('pending'));
    },

    /**
     * @method _updateAlertsState
     * @param summary
     * @private
     */
    _updateAlertsState: function (summary) {
      if (summary.numberOfAlerts) {
        this.$el.removeClass('mas-threadSummary-noAlerts');
      } else {
        this.$el.addClass('mas-threadSummary-noAlerts');
      }
    },

    /**
     * @method _updatePendingState
     * @param pending
     * @private
     */
    _updatePendingState: function (pending) {
      if (pending) {
        this.$el.addClass('mas-threadSummary-pending');
      } else {
        this.$el.removeClass('mas-threadSummary-pending');
      }
    }
  });

  return ThreadSummaryView;
});
