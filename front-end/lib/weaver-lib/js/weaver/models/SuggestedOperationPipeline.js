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
  var escapeRegExp = require('weaver/utils/lodash.escapeRegExp');
  
  /**
   * SuggestedOperationPipeline helps completing existing pipelines so it's easier to configure them with
   * suggested operators
   * @class SuggestedOperationPipeline
   * @namespace  models.experimentalFeatures
   * @module weaver
   * @submodule models.experimentalFeatures
   * @constructor
   * @param {models.Operation[]} operators The list of suggested operators
   */
  function SuggestedOperationPipeline(operators) {
    this.operators = operators;
    this._operatorsStringList = this.operators.join(',');
  }

  _.extend(SuggestedOperationPipeline.prototype, {
    operators: [],

    /**
     * @method isCompatible
     * @param operationPipeline
     * @returns {*}
     */
    isCompatible: function (operationPipeline) {
      var reOperations = this.createRegExp(operationPipeline);
      return reOperations.test(this._operatorsStringList);
    },

    /**
     * Create a RegExp from the Operation Pipeline
     * @method createRegExp
     * @param operationPipeline
     * @returns {RegExp}
     */
    createRegExp: function (operationPipeline) {
      return new RegExp(_.pluck(operationPipeline, 'operator').map(escapeRegExp).join(',?.*,'));
    },

    /**
     * @method addSuggestedOperations
     * @param operationPipeline
     * @returns {TResult|Array}
     */
    addSuggestedOperations: function (operationPipeline) {
      return _.reduce(this.operators, function (result, operator) {
        if (operationPipeline[0] && operationPipeline[0].operator === operator) {
          result.push(operationPipeline[0]);
          operationPipeline = _.rest(operationPipeline);
        } else {
          result.push({
            operator: operator,
            parameters: {},
            suggested: true
          });
        }
        return result;
      }, []);
    }
  });

  return SuggestedOperationPipeline;
});