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
  var Operation = require('weft/models/Operation');

  /**
   * Query store the configuration of the operations, used to analyse the contents of an aggregation
   * @class Query
   * @namespace models
   * @module weft
   * @submodule models
   * @constructor
   * @extends Backbone.Model
   */
  var Query = Backbone.Model.extend({

    constructorName: 'LOOM_Query',

    defaults: function () {

      return {
        /**
         * A list of the aggregation IDs the query runs on
         * @attribute inputs
         * @type {Array}
         */
        inputs: [],

        /**
         * A list of operations to be applied to the Query's inputs
         * by the server
         * @attribute  operationPipeline
         * @type {Array}
         */
        operationPipeline: [],

        /**
         * An operation that will be applied at the end of the pipeline,
         * whose role is to limit the number of fibers returned by the query
         * @attribute limit
         * @type {Object}
         */
        limit: undefined,

        /**
         * A flag letting Loom know if it should include in the results
         * an aggregation of the items that might get excluded due to filters
         * @attribute includeExcludedItems
         * @type {Boolean}
         * @default false
         */
        includeExcludedItems: true
      };
    },

    /**
     *
     * @method limitWith
     * @param operation
     * @returns {Query}
     */
    limitWith: function (operation) {
      operation = _.clone(operation);
      return (operation !== this.get('limit')) ? this.clone().set('limit', operation) : this;
    },

    /**
     * @method setPipeline
     * @param pipeline
     * @returns {*}
     */
    setPipeline: function (pipeline) {
      var clone = this.clone();
      clone.set('operationPipeline', pipeline);
      return clone;
    },

    /**
     * Checks if given `query` is the same as this Query,
     * ie. that they run on the same inputs with the same parameters
     * @method isSameAs
     * @param  {Query}  query
     * @return {Boolean}       [description]
     */
    isSameAs: function (query) {
      if (!query) {
        return false;
      }
      return query === this || (_.isEqual(query.get('inputs'), this.get('inputs')) &&
        _.isEqual(query.get('operationPipeline'), this.get('operationPipeline')) &&
        _.isEqual(query.get('limit'), this.get('limit'))) &&
        _.isEqual(query.get('includeExcludedItems'), this.get('includeExcludedItems'));
    },

    /**
     * @method clearOperationPipeline
     * @returns {*}
     */
    clearOperationPipeline: function () {
      var query = this.clone();
      query.set('operationPipeline', []);
      query.set('limit', undefined);
      return query;
    },

    /**
     * @method hasOperations
     * @returns {boolean}
     */
    hasOperations: function () {
      return !!this.get('operationPipeline').length;
    },

    /**
     * Returns true if this query has the given limit. If given
     * a string, it test against the name of the operation. If given
     * an operation it test against equality. If nothing is given,
     * it returns true only if there's a limit operation.
     * @method hasLimit
     * @param  {any}      operation [optional] the operation to test.
     * @return {Boolean}  true if one of the described rule is true.
     */
    hasLimit: function (operation) {
      // Do we have a limit for this query ?
      if (arguments.length === 0) {
        return this.get('limit') !== undefined && !_.isEqual(this.get('limit'), {});
      }
      // Do we have the given operation as limit for the query ?
      if (_.isString(operation)) {
        return this.get('limit') !== undefined && this.get('limit').operator === operation;
      } else {
        return this.get('limit') === operation;
      }
    },

    /**
     * @method pushOperation
     * @param operation
     * @returns {*}
     */
    pushOperation: function (operation) {
      var clone = this.clone();
      operation = _.clone(operation);
      clone.get('operationPipeline').push(operation);
      return clone;
    },

    /**
     * @method unshiftOperation
     * @param operation
     * @returns {*}
     */
    unshiftOperation: function (operation) {
      var clone = this.clone();
      operation = _.clone(operation);
      clone.get('operationPipeline').unshift(operation);
      return clone;
    },

    /**
     * @method hasOperation
     * @param operation
     * @returns {boolean}
     */
    hasOperation: function (operation) {
      var pipeline = this.get('operationPipeline');
      var res;
      // No arguments, do we have at least one operations ?
      if (arguments.length === 0) {
        return _.size(pipeline) > 0;
      }
      res = this.getOperation(operation);
      return res.length > 0;
    },

    /**
     * @method removeOperations
     * @param operation
     * @returns {*}
     */
    removeOperations: function (operation) {
      var clone = this.clone();
      var pipeline = clone.get('operationPipeline');
      if (_.isString(operation)) {
        _.remove(pipeline, function (op) {
          return op.operator === operation;
        });
      } else {
        _.remove(pipeline, function (op) {
          return _.isEqual(operation, op);
        });
      }
      return clone;
    },

    /**
     * @method getOperation
     * @param operation
     * @returns {*|Array}
     */
    getOperation: function (operation) {
      var pipeline = this.get('operationPipeline');
      var res;
      // If we are given the operator name
      if (_.isString(operation)) {
        res = _.where(pipeline, {
          operator: operation
        });
        // If we are given a real operator
      } else if (operation && !_.isUndefined(operation.operator)) {
        res = _.where(pipeline, {
          operator: operation.operator
        });
      }
      return _.cloneDeep(res) || [];
    },

    /**
     * @method getLimit
     * @returns {any|*}
     */
    getLimit: function () {
      return _.cloneDeep(this.get('limit'));
    },

    /**
     * @method updateOperations
     * @param operation
     * @returns {*}
     */
    updateOperations: function (operation) {
      var clone = this.clone();
      var pipeline = clone.get('operationPipeline');
      _.where(pipeline, {
        operator: operation.operator
      }).map(
        function (oldOperation) {
          oldOperation.parameters = operation.parameters;
        }
      );
      return clone;
    },

    /**
     * To avoid generating fully connected graphs,
     * queries need to append some sort to the pipeline,
     * dependent on whether the Thread is sorted or not
     * @method _getSortByOperation
     * @param  {[type]} pipeline [description]
     * @return {[type]}          [description]
     * @private
     */
    _getSortByOperation: function (pipeline) {
      var property = this._hasGroupByOperation(pipeline) ? 'name' : 'fullyQualifiedName';
      return {
        operator: Operation.SORT_BY_ID,
        parameters: {
          property: property,
          order: 'ASC'
        }
      };
    },

    /**
     * @method _hasGroupByOperation
     * @param pipeline
     * @returns {boolean}
     * @private
     */
    _hasGroupByOperation: function (pipeline) {
      return _.where(pipeline, {
        operator: Operation.GROUP_BY_ID
      }).length > 0;
    },

    /**
     * @method toJSON
     * @returns {{inputs: (any|*), operationPipeline: TResult[], includeExcludedItems: (any|*)}}
     */
    toJSON: function () {
      var pipeline = _.clone(this.get('operationPipeline'));
      var limit = this.get('limit');
      if (!this.hasOperation(Operation.SORT_BY_ID)) {
        pipeline.push(this._getSortByOperation(pipeline));
      }
      if (limit) {
        pipeline.push(limit);
      }
      var result = {
        inputs: this.get('inputs'),
        operationPipeline: _.map(pipeline, function (operation) {
          return _.pick(_.cloneDeep(operation), ['operator', 'parameters']);
        }),
        includeExcludedItems: this.get('includeExcludedItems')
      };
      return result;
    },

    /**
     * @method clone
     * @returns {*}
     */
    clone: function () {
      return new Query({
        inputs: _.cloneDeep(this.get('inputs')),
        operationPipeline: _.cloneDeep(this.get('operationPipeline')),
        limit: _.cloneDeep(this.get('limit'))
      });
    }
  });

  return Query;
});
