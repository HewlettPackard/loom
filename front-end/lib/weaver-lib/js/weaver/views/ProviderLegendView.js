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
  var _ = require('lodash');
  /** @type BaseView */
  var BaseView = require('./BaseView');
  var $template = $('<span class="mas-providerLegend--provider">');

  /**
   * @class ProviderLegendView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var ProviderLegendView = BaseView.extend({

    /**
     * @property tagName
     * @type {String}
     */
    tagName: 'ul',

    /**
     * @property className
     * @type {String}
     */
    className: 'mas-providerLegend',

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      if (this.options.providerLegendService) {
        this.render();
        this.listenTo(this.options.providerLegendService.get('providers'), 'add remove', this.render);
      }
    },

    /**
     * @method render
     */
    render: function () {
      var legends = this.options.providerLegendService.getLegendForThread(this.model);
      this.$el.empty().append(_.map(legends, this.renderLegend));
    },

    /**
     * @method renderLegend
     * @param legend
     * @returns {JQuery|string}
     */
    renderLegend: function (legend) {
      return $template.clone().text(legend);
    }
  });

  return ProviderLegendView;
});