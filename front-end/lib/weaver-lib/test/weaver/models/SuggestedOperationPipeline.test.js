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
  var Operation = require('weft/models/Operation');
  var SuggestedOperationPipeline = require('weaver/models/SuggestedOperationPipeline');

  describe('weaver/models/SuggestedOperationPipeline.js', function () {

    var suggestedPipeline = new SuggestedOperationPipeline([
      Operation.FILTER_STRING_ID,
      Operation.GROUP_BY_ID,
      Operation.SORT_BY_ID
    ]);

    function createPipeline (operators) {
      return operators.map(function (operator) {
        return {
          operator: operator
        };
      });
    }

    describe('isCompatible()', function () {

      var tests = [
        [[], true, 'pipeline is empty'],
        [[Operation.FILTER_STRING_ID], true, 'pipeline has one element'],
        [[Operation.FILTER_STRING_ID, Operation.SORT_BY_ID], true, 'pipeline has multiple elements'],
        [[Operation.FILTER_STRING_ID, Operation.GROUP_BY_ID, Operation.SORT_BY_ID], true, 'pipeline has the same elements'],
        [[Operation.BRAID_ID], false, 'pipeline contains an operation that is not suggested'],
        [[Operation.SORT_BY_ID, Operation.FILTER_STRING_ID], false, 'operations in the pipeline are not in the suggested order'],
        [[Operation.FILTER_STRING_ID, Operation.FILTER_STRING_ID, Operation.GROUP_BY_ID, Operation.SORT_BY_ID], false, 'pipeline contains more operations than suggested']
      ];

      tests.forEach(function (test) {

        it('Returns ' + test[1] + ' when ' + test[2], function () {

          var pipeline = createPipeline(test[0]);
          expect(suggestedPipeline.isCompatible(pipeline)).to.equal(test[1]);
        });
      });
    });

    describe('addSuggestedOperations()', function () {

      var tests = [
        [[], [0,1,2], 'when there is no'],
        [[Operation.FILTER_STRING_ID], [1, 2], 'after'],
        [[Operation.SORT_BY_ID], [0, 1], 'before'],
        [[Operation.GROUP_BY_ID], [0, 2], 'around'],
        [[Operation.FILTER_STRING_ID, Operation.SORT_BY_ID], [1], 'between']
      ];

      tests.forEach(function (test) {

        it('Adds suggested operations ' + test[2] + ' existing operations', function () {

          var pipeline = createPipeline(test[0]);
          var result = suggestedPipeline.addSuggestedOperations(pipeline);

          // Check pipeline has appropriate operations
          expect(_.pluck(result, 'operator')).to.deep.equal(suggestedPipeline.operators);

          // TODO: Check suggested operations are flagged properly
        });
      });
    });
  });
});
