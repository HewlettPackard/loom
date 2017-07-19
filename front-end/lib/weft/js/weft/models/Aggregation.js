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
  var Element = require('weft/models/Element');
  var NestedUpdatesMonitor = require('weft/models/Thread/NestedUpdatesMonitor');
  var AggregationItemType = require('weft/models/AggregationItemType');

  /**
   * Aggregation
   * @class Aggregation
   * @namespace models
   * @module weft
   * @type {[type]}
   */
  var Aggregation = Element.extend({

    constructorName: 'LOOM_Aggregation',

    idAttribute: 'l.semanticId',

    defaults: function () {

      var parentDefaults = Element.prototype.defaults;

      if (_.isFunction(parentDefaults)) {
        parentDefaults = parentDefaults.call(this);
      } else {
        parentDefaults = _.clone(parentDefaults);
      }

      return _.extend(parentDefaults, {

        /**
         * Flag marking if the aggregation corresponds
         * to the current query of the Thread
         * @attribute outdated
         * @type {Boolean}
         */
        outdated: false,

        /**
         * The number of items represented by this aggregation
         * @attribute numberOfItems
         * @type {Number}
         */
        numberOfItems: 0,

        /**
         * Number of items in the aggregation that were created recently
         * @attribute createdCount
         * @type {Number}
         */
        createdCount: 0,

        /**
         * Number of items in the aggregation that were deleted recently
         * @attribute deletedCount
         * @type {Number}
         */
        deletedCount: 0,

        /**
         * Number of items in the aggregation that were updated recently
         * @type {Number}
         */
        updatedCount: 0,

        /**
         * The index of the first item
         * @attribute minIndex
         * @type {Number}
         */
        minIndex: 0,

        /**
         * The index of the last index
         * @attribute maxIndex
         * @type {Number}
         */
        maxIndex: 0
      });
    },

    initialize: function (attributes, options) {

      options = options || {};
      options.itemType = options.itemType || {};

      options.itemType = new AggregationItemType(options.itemType);
      Element.prototype.initialize.call(this, attributes, options);
      this.nestedUpdatesMonitor = new NestedUpdatesMonitor(this);
    },

    // NESTED STATE CHANGES

    NESTED_CHANGE_MAPPINGS: {
      'createdCount': 'nestedAdd',
      'deletedCount': 'nestedDelete',
      'updatedCount': 'nestedUpdate'
    },

    getNestedStateChanges: function () {

      var changes = [];

      _(this.NESTED_CHANGE_MAPPINGS).forEach(function (state, property) {

        if (this.changed.hasOwnProperty(property)) {
          changes.push(state);
        }
      }, this);

      return changes;
    },

    getStateChangeTimeout: function () {
      return 60000;
    },

    /**
     * Sets the state of the Thread to STATE_NESTED_UPDATE for given duration
     * @param {Number} duration The duration the Thread should keep the state
     */
    setNestedUpdateState: function (duration) {
      this.setState('nestedStateChanges', duration);
    },

    getLongestStateChangeTimeout: function (states) {

      var timeout = 0;
      _.forEach(states, function (state) {

        var stateTimeout = this.getStateChangeTimeout(state);
        if (stateTimeout > timeout) {
          timeout = stateTimeout;
        }
      }, this);

      return timeout;
    }

  });

  return Aggregation;
});
