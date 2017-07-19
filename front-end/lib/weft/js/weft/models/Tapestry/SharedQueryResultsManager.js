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

  var Backbone = require('backbone');
  var QueryResult = require('weft/models/QueryResult');

  /**
   * @class SharedQueryResultsManager
   * @namespace models.tapestry
   * @module weft
   * @submodule models.tapestry
   * @constructor
   * @extends Backbone.Model
   */
  var SharedQueryResultsManager = Backbone.Model.extend({

    defaults: function () {
      return {
        /**
         * Current list of QueryResults
         * @type {Backbone.Collection}
         */
        results: new Backbone.Collection()
      };
    },

    initialize: function () {
      if (this.get('threads')) {
        this.listenTo(this.get('threads'), 'add', this.provideQueryResult);
        this.listenTo(this.get('threads'), 'change:query', this.provideQueryResult);
        this.listenTo(this.get('threads'), 'remove', this.removeResult);
      }
    },

    /**
     * @method provideQueryResult
     * @param thread
     */
    provideQueryResult: function (thread) {
      var result = this.getQueryResultFor(thread.get('query'));
      this._updateQueryResult(thread, result);
    },

    /**
     * @method getQueryResultFor
     * @param query
     * @returns {*}
     */
    getQueryResultFor: function (query) {
      var result = this.findQueryResultFor(query);
      if (!result) {
        result = new QueryResult({
          query: query,
          aggregator: this.get('aggregator')
        });
        this.get('results').add(result);
      }
      return result;
    },

    /**
     * @method findQueryResultFor
     * @param query
     * @returns {*}
     */
    findQueryResultFor: function (query) {
      // Could be speeded up by indexing the QueryResults
      // using the hash of their Query
      return this.get('results').find(function (result) {
        return result.get('query').isSameAs(query);
      });
    },

    /**
     * remove the query result for a thread
     * @method removeResult
     * @param thread
     */
    removeResult: function (thread) {
      this._updateQueryResult(thread);
    },

    /**
     * @method _updateQueryResult
     * @param thread
     * @param queryResult
     * @private
     */
    _updateQueryResult: function (thread, queryResult) {
      var previousResult = thread.get('result');
      thread.set('result', queryResult);
      if (previousResult && previousResult.get('threads').isEmpty()) {
        if (queryResult && queryResult.get('pending')) {
          previousResult.set('queryable', false);
          this.listenToOnce(queryResult, 'change:pending', function () {
            // Chaining is occurring here if you call this method several times:
            // That's why there's this call to tell other change to be updated
            previousResult.set('pending', false);
            this.get('results').remove(previousResult);
          });
        } else {
          this.get('results').remove(previousResult);
        }
      }
    }
  });

  return SharedQueryResultsManager;

});
