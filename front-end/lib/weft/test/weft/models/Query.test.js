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
/* global describe, it, expect, beforeEach */
/* jshint expr: true */
define(function(require) {

  "use strict";

  var _ = require('lodash');
  var Query = require('weft/models/Query');
  var Operation = require('weft/models/Operation');

  describe('weft/models/Query.js', function() {

    beforeEach(function() {

      this.query = new Query({
        inputs: ['/aggregation/id'],
        operationPipeline: [{
          operator: Operation.BRAID_ID,
          parameters: {
            maxFibres: 25
          }
        }, {
          operator: Operation.FILTER_STRING_ID,
          parameters: {
            pattern: 'vm-1*'
          }
        }],
        limit: {
          operator: Operation.BRAID_ID,
          parameters: {
            maxFibres: 10
          }
        }
      });
    });

    describe('isSameAs()', function() {

      before(function() {

        this.sameQuery = new Query({
          inputs: ['/aggregation/id'],
          operationPipeline: [{
            operator: Operation.BRAID_ID,
            parameters: {
              maxFibres: 25
            }
          }, {
            operator: Operation.FILTER_STRING_ID,
            parameters: {
              pattern: 'vm-1*'
            }
          }],
          limit: {
            operator: Operation.BRAID_ID,
            parameters: {
              maxFibres: 10
            }
          }
        });

        this.differentInputs = new Query({
          inputs: ['/aggregation/other-id'],
          operationPipeline: [{
            operator: Operation.BRAID_ID,
            parameters: {
              maxFibres: 25
            }
          }, {
            operator: Operation.FILTER_STRING_ID,
            parameters: {
              pattern: 'vm-1*'
            }
          }],
          limit: {
            operator: Operation.BRAID_ID,
            parameters: {
              maxFibres: 10
            }
          }
        });

        this.differentPipeline = new Query({
          inputs: ['/aggregation/id'],
          operationPipeline: [{
            operator: Operation.BRAID_ID,
            parameters: {
              maxFibres: 5
            }
          }, {
            operator: Operation.FILTER_STRING_ID,
            parameters: {
              pattern: 'vm-1*'
            }
          }],
          limit: {
            operator: Operation.BRAID_ID,
            parameters: {
              maxFibres: 10
            }
          }
        });

        this.differentLimit = new Query({
          inputs: ['/aggregation/id'],
          operationPipeline: [{
            operator: Operation.BRAID_ID,
            parameters: {
              maxFibres: 25
            }
          }, {
            operator: Operation.FILTER_STRING_ID,
            parameters: {
              pattern: 'vm-1*'
            }
          }],
          limit: {
            operator: Operation.BRAID_ID,
            parameters: {
              maxFibres: 1
            }
          }
        });
      });

      it('Shoudl consider it different to null or undefined', function() {

        expect(this.query.isSameAs(null)).to.be.false;
        expect(this.query.isSameAs()).to.be.false;
      });

      it('Should consider it the same as itself', function() {

        expect(this.query.isSameAs(this.query)).to.be.true;
      });

      it('Should consider queries the same if they run the same pipeline over the same inputs', function() {

        expect(this.query.isSameAs(this.sameQuery)).to.be.true;
      });

      it('Should not consider them the same if inputs are different', function() {

        expect(this.query.isSameAs(this.differentInputs)).to.be.false;
      });

      it('Should not consider them the same if pipeline is different', function() {

        expect(this.query.isSameAs(this.differentPipeline)).to.be.false;
      });

      it('Should not consider them the same if limit is different', function() {
        expect(this.query.isSameAs(this.differentLimit)).to.be.false;
      });

    });

    describe('hasLimit()', function () {

      it('Should return true when called with no arguments', function () {
        expect(this.query.hasLimit()).to.be.true;
      });

      it('Should return false if the limit is undefined or {}', function () {
        this.query.set('limit', undefined);
        expect(this.query.hasLimit()).to.be.false;
        this.query.set('limit', {});
        expect(this.query.hasLimit()).to.be.false;
      });

      it('Should return false when called with undefined', function () {
        expect(this.query.hasLimit(undefined)).to.be.false;
      });

      it('Should return true when called with the limit name', function () {
        expect(this.query.hasLimit(Operation.BRAID_ID)).to.be.true;
      });
    });

    describe('hasOperation()', function () {

      it('Should return true when called with no arguments', function () {
        expect(this.query.hasOperation()).to.be.true;
      });

      it('Should return true when called with an operation that is present in the pipeline', function () {
        expect(this.query.hasOperation(Operation.BRAID_ID)).to.be.true;
      });

      it('Should return false when called with an operation not present', function () {
        expect(this.query.hasOperation('hello world!')).to.be.false;
      });
    });

    describe('removeOperations()', function () {

      it('Should not modify the current query', function () {
        var operationPipeline = _.cloneDeep(this.query.get('operationPipeline'));

        this.query.removeOperations(Operation.BRAID_ID);

        expect(this.query.get('operationPipeline')).to.eql(operationPipeline);
      });

      it('Should remove all operations with the same name as the one given', function () {

        var query = this.query.pushOperation({
          operator: Operation.BRAID_ID,
          parameters: {
            maxFibres: 1234
          }
        });

        // Check we have at two braid otherwise the test is not worth it.
        expect(_.where(query.get('operationPipeline'), {operator: Operation.BRAID_ID}).length).to.equal(2);

        // Generate the new query
        var newQuery = query.removeOperations(Operation.BRAID_ID);

        var res = _.where(newQuery.get('operationPipeline'), { operator: Operation.BRAID_ID});

        expect(res.length).to.equal(0);
        expect(res).to.eql([]);
      });

      it('Should only remove the given specific operation', function () {

        // Add an other braid to perform the check.
        var query = this.query.pushOperation({
          operator: Operation.BRAID_ID,
          parameters: {
            maxFibres: 1234
          }
        });

        // Check we have at two braid otherwise the test is not worth it.
        expect(_.where(query.get('operationPipeline'), {operator: Operation.BRAID_ID}).length).to.equal(2);

        // Generate the new query
        var newQuery = query.removeOperations({operator: Operation.BRAID_ID, parameters: { maxFibres: 1234 }});

        var res = _.where(newQuery.get('operationPipeline'), { operator: Operation.BRAID_ID});

        expect(res.length).to.equal(1);
        expect(res).to.eql([{
          operator: Operation.BRAID_ID,
          parameters: {
            maxFibres: 25
          }
        }]);
      });
    });

    describe('getOperation()', function () {

      it('Should return the matched Operation when called with an operation that is present in the pipeline', function () {
        expect(this.query.getOperation(Operation.BRAID_ID)).to.eql([{
          operator: Operation.BRAID_ID,
          parameters: {
            maxFibres: 25
          }
        }]);
      });

      it('Should return an empty list when called with no arguments', function () {
        expect(this.query.getOperation()).to.eql([]);
      });

      it('Should return an empty list when called with an operation that does not exists', function () {
        expect(this.query.getOperation('foo bar')).to.eql([]);
      });
    });

    describe('updateOperations()', function () {

      it('Should not modify the current query', function () {
        var operationPipeline = _.cloneDeep(this.query.get('operationPipeline'));
        this.query.updateOperations({
          operator: Operation.BRAID_ID,
          parameters: {
            maxFibres: 30,
            newParameter: "bonjour",
          }
        });

        expect(this.query.get('operationPipeline')).to.eql(operationPipeline);
      });

      it('Should return a new query with the updated operations', function () {
        var clone = this.query.updateOperations({
          operator: Operation.BRAID_ID,
          parameters: {
            maxFibres: 30,
            newParameter: "bonjour",
          }
        });

        expect(clone.getOperation(Operation.BRAID_ID)).to.eql([{
          operator: Operation.BRAID_ID,
          parameters: {
            maxFibres: 30,
            newParameter: "bonjour",
          }
        }]);
      });
    });

    describe('pushOperation()', function () {

      it('Should return a new query with at the end, the new operation', function () {
        var pipeline = this.query.get('operationPipeline');
        var oldSize = pipeline.length;

        var clone = this.query.pushOperation({
          operator: Operation.BRAID_ID,
          parameters: {
            maxFibres: 30,
            newParameter: "bonjour",
          }
        });

        pipeline = clone.get('operationPipeline');

        expect(pipeline.length).to.equal(oldSize + 1);

        expect(pipeline[pipeline.length - 1]).to.eql({
          operator: Operation.BRAID_ID,
          parameters: {
            maxFibres: 30,
            newParameter: "bonjour",
          }
        });
      });
    });

    describe('toJSON()', function() {

      it('Should append the limit operation to the pipeline', function() {

        var json = JSON.stringify(this.query.toJSON());

        var expected = JSON.stringify({
          inputs: ['/aggregation/id'],
          operationPipeline: [{
            operator: Operation.BRAID_ID,
            parameters: {
              maxFibres: 25
            }
          }, {
            operator: Operation.FILTER_STRING_ID,
            parameters: {
              pattern: 'vm-1*'
            }
          }, {
            operator: Operation.SORT_BY_ID,
            parameters: {
              property: 'fullyQualifiedName',
              order: 'ASC'
            }
          }, {
            operator: Operation.BRAID_ID,
            parameters: {
              maxFibres: 10
            }
          }],
          includeExcludedItems: true
        });

        expect(json).to.equal(expected);
      });

      // TODO: Revisit when implementing SORT BY operation in the pipeline
      // It probably needs to be added only if there is no SORT BY already
      it('Should add a "name" sort if there is a groupBy operation', function() {

        var queryWithGroupBy = new Query({
          inputs: ['/aggregation/id'],
          operationPipeline: [{
            operator: Operation.GROUP_BY_ID,
            parameters: {
              property: 'propertyId'
            }
          }]
        });

        var json = JSON.stringify(queryWithGroupBy.toJSON());

        var expected = JSON.stringify({
          inputs: ['/aggregation/id'],
          operationPipeline: [{
            operator: Operation.GROUP_BY_ID,
            parameters: {
              property: 'propertyId'
            }
          }, {
            operator: Operation.SORT_BY_ID,
            parameters: {
              property: 'name',
              order: 'ASC'
            }
          }],
          includeExcludedItems: true
        });

        expect(json).to.equal(expected);
      });
    });
  });
});
