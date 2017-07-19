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

  /**
   * Stores the definition of a metric
   * @class Metric
   * @namespace models
   * @module weft
   * @submodule models
   * @constructor
   * @extends Backbone.Model
   */
  var Backbone = require('backbone');
  var Metric = Backbone.Model.extend({

    constructorName: 'LOOM_Metric',

    // PROPERTIES
    defaults: function () {
      return {

        /**
         * ID of the metric (should be unique within a given Thread)
         * @property id
         * @type {String}
         */

        /**
         * The name of the metric
         * @property name
         * @type {String}
         */
        name: null,

        /**
         * The minimum value of this metric
         * @property min
         * @type {Number}
         * @default Infinity
         */
        min: Infinity,

        /**
         * The maximum value of this metric
         * @property max
         * @type {Number}
         * @default -Infinity
         */
        max: -Infinity

        /**
         * Flag telling if the ranges of values for this metric have to
         * be dynamically computed for each response.
         * @property dynamicRange
         * @type {Boolean}
         */
      };
    },

    initialize: function () {
      if (this.get('max') === 'Inf') {
        this.set('max', -Infinity);
        this.set('dynamicRange', 'history');
      }
    },

    /**
     * Determines if a given value is within the current min and max range
     * @method isInRange
     * @param value
     * @returns {boolean}
     */
    isInRange: function (value) {
      return value >= this.get('min') && value <= this.get('max');
    },

    /**
     * Updates the range of the metric so it encompasses given values.
     * If the Metric's `dynamicRange` is set to 'history', it will account
     * for the highest and lowest values the Metric's ever had, expanding it to match
     * the set of values. Otherwise the range will be based on the set of values only.
     * @method updateRange
     * @param values {Array}
     */
    updateRange: function (values) {
      var initialRange = {
        min: this.get('dynamicRange') === 'history' ? this.get('min') : Infinity,
        max: this.get('dynamicRange') === 'history' ? this.get('max') : -Infinity
      };
      var range = _.reduce(values, function (result, value) {
        if (value < result.min) {
          result.min = value;
        }
        if (value > result.max) {
          result.max = value;
        }
        return result;
      }, initialRange);
      if (range.min === range.max) {
        range.min = 0;
      }
      this.set(range);
    },

    /**
     * Normalises given value between 0 and 1
     * according to the range of values this metric can have
     * @method normalise
     * @param value {Number|Array}
     * @return {Number|Array}
     */
    normalise: function (value) {
      if (_.isArray(value)) {
        return _.map(value, function (val) {
          return this.normalise(val);
        }, this);
      }
      if (this.get('min') === this.get('max')) {
        return this.get('min') === 0 ? 0 : 1;
      }
      if (_.isNull(value) || _.isUndefined(value) || value < this.get('min')) {
        return 0;
      }
      if (value > this.get('max')) {
        return 1;
      }
      return (value - this.get('min')) / (this.get('max') - this.get('min'));
    }
  });

  return Metric;
});
