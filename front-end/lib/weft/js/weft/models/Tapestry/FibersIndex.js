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
   * FibersIndex is a helper keeping an index of all the Items
   * and Aggregations displayed in a list of QueryResults
   * @class FibersIndex
   * @namespace models.tapestry
   * @module weft
   * @submodule models.tapestry
   * @constructor
   * @extends Backbone.Model
   * @type {[type]}
   */
  var FibersIndex = Backbone.Model.extend({

    defaults: function () {
      return {
        /**
         * A collection of QueryResults containing the Fibers that need indexing
         * @attribute {Backbone.Collection} queryResults
         */

        /**
         * The actual index
         * @attribute {Backbone.Collection} index
         */
        index: new Backbone.Collection()
      };
    },

    initialize: function () {
      if (this.get('queryResults')) {
        this.listenTo(this.get('queryResults'), 'change:elements', this._updateIndex);
        this.listenTo(this.get('queryResults'), 'remove', this._maybeRemoveFibers);
      }
    },

    /**
     * Get the fibers from the index by ids
     * @method getFibers
     * @param ids
     * @returns {*}
     */
    getFibers: function (ids) {
      if (_.isArray(ids)) {
        return this._bulkGet(ids);
      } else {
        return this.get('index').get(ids);
      }
    },

    /**
     * Get an array of fibers by their ids
     * @param ids Number|Array
     * @returns {Array}
     * @private
     */
    _bulkGet: function (ids) {
      var result = [];
      _.forEach(ids, function (id) {
        var fiber = this.get('index').get(id);
        if (fiber) {
          result.push(fiber);
        }
      }, this);
      return result;
    },

    /**
     * Add a fiber(s) to the index
     * @method add
     * @param fibers
     */
    add: function (fibers) {
      this.get('index').add(fibers);
    },

    /**
     * Remove a fiber(s) from the index
     * @method remove
     * @param fibers
     */
    remove: function (fibers) {
      this.get('index').remove(fibers);
    },

    /**
     * Logic to determine if fibers should be removed from the index, ignored undefined elements
     * @method _maybeRemoveFibers
     * @param queryResult
     * @private
     */
    _maybeRemoveFibers: function (queryResult) {
      // QueryResults might have undefined elements, so a bit of check is needed
      if (queryResult.get('elements')) {
        queryResult.get('elements').forEach(function (element) {
          if (!element.isPartOf(this.get('queryResults'))) {
            this.remove(element);
          }
        }, this);
      }
      // Also need to remove excluded items
      this.remove(queryResult.get('excludedItems'));
    },

    /**
     * Calculates the difference between the current and desired indexes and triggers the relevant
     * add/remove actions (and events on the index).
     * Events do not appear to bubble.
     * @method _updateIndex
     * @param queryResult
     * @param elements
     * @private
     */
    _updateIndex: function (queryResult, elements) {
      var previousElements = queryResult.previous('elements');
      if (previousElements) {
        var removed = _.difference(previousElements.models, elements.models);
        var added = _.difference(elements.models, previousElements.models);
        this.remove(removed);
        this.add(added);
      } else {
        this.add(elements.models);
      }
      this._updateExcludedItemsIndex(queryResult);
    },

    /**
     * Update the excluded items index
     * @method _updateExcludedItemsIndex
     * @param queryResult
     * @private
     */
    _updateExcludedItemsIndex: function (queryResult) {
      if (queryResult.changed.excludedItems) {
        this.remove(queryResult.previous('excludedItems'));
      }
      this.add(queryResult.get('excludedItems'));
    }
  });

  return FibersIndex;
});
