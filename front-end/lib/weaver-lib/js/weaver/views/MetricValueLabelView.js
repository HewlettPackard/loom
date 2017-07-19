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
  var numeral = require('numeral');
  /** @type BaseView */
  var BaseView = require('./BaseView');

  /**
   * Renders the label of a metric, allowing the user to switch
   * between an abbreviated view and a full view
   * @class MetricValueLabelView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var MetricValueLabelView = BaseView.extend({

    /**
     * @property className
     * @type {String}
     */
    className: 'mas-valueLabel',

    /**
     * @property events
     * @type {Object}
     */
    events: {
      'click': function (event) {
        event.preventDefault();
        this.toggleAbbreviation();
        this._doRender();
      }
    },

    /**
     * @property abbreviated
     * @type {Boolean}
     */
    abbreviated: true,

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.abbreviated = true;
    },

    /**
     * @method toggleAbbreviation
     */
    toggleAbbreviation: function () {
      this.abbreviated = !this.abbreviated;
    },

    /**
     * @method render
     * @param value
     */
    render: function (value) {
      this.value = value;
      this._doRender();
    },

    /**
     * @method _doRender
     * @private
     */
    _doRender: function () {
      var label = '???';
      if (!_.isUndefined(this.value)) {
        if (this.abbreviated) {
          label = this._renderAbbreviatedValue(this.value);
        } else {
          label = this._renderFullValue(this.value);
        }
        label += this._renderUnit();
      }
      this.$el.html(label);
    },

    /**
     * @method _renderUnit
     * @returns {string}
     * @private
     */
    _renderUnit: function () {
      var unit = this.options.metric.get('unit');
      return unit ? ' ' + unit : '';
    },

    /**
     * @method _renderAbbreviatedValue
     * @param value
     * @returns {void|*|string|XML}
     * @private
     */
    _renderAbbreviatedValue: function (value) {
      return numeral(value).format('0.[00]a').replace('.', '&#8202;.');
    },

    /**
     * @method _renderFullValue
     * @param value
     * @returns {void|*|string|XML}
     * @private
     */
    _renderFullValue: function (value) {
      return numeral(value).format('0.[00]').replace('.', '&#8202;.');
    }

  });

  return MetricValueLabelView;
});