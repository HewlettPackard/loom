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
  var Backbone = require('backbone');

  /**
   * Sorts represent ways of sorting a Thread
   * @class Sort
   * @module weft
   * @submodule models
   * @namespace models
   * @constructor
   * @extends Backbone.Model
   */
  var Sort = Backbone.Model.extend({

    constructorName: 'LOOM_Sort',

    defaults: {
      /**
       * The property used for sorting
       * @attribute {Backbone.Model} property
       */

      /**
       * The order of the sort
       * @attribute {String} order
       */
      order: 'ascending'
    },

    /**
     * Sets the sort order back to the default value
     * @method resetOrder
     */
    resetOrder: function () {
      this.set('order', this.defaults.order);
    },

    /**
     * Reverses the current sort order
     * @method reverseOrder
     */
    reverseOrder: function () {
      if (this.get('order') === Sort.ORDER_ASCENDING) {
        this.set('order', Sort.ORDER_DESCENDING);
      } else {
        this.set('order', Sort.ORDER_ASCENDING);
      }
    },

    /**
     * Returns the comparator function to be used for sorting
     * a Backbone.Collection
     * @method getComparator
     * @return {Function}
     */
    getComparator: function () {
      var orderModifier = this.get('order') === Sort.ORDER_DESCENDING ? -1 : 1;
      var attributeId = this.id;
      return function (a, b) {
        var aValue = a.get(attributeId);
        var bValue = b.get(attributeId);
        // Ideally, we'd have that info given by the server, rather than duck-typing
        if (_.isString(aValue)) {
          return orderModifier * (aValue.localeCompare(bValue));
        } else {
          return orderModifier * (aValue - bValue);
        }
      };
    },

    /**
     * @method isSameAs
     * @param sort
     * @returns {*|boolean}
     */
    isSameAs: function (sort) {
      return sort && sort.id === this.id;
    }
  });

  Sort.ORDER_ASCENDING = 'ascending';
  Sort.ORDER_DESCENDING = 'descending';

  return Sort;
});
