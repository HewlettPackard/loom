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
  var $ = require('jquery');
  var Item = require('weft/models/Item');
  var ItemType = require('weft/models/ItemType');
  var Operation = require('weft/models/Operation');
  var uuid = require('uuid');
  var Thread = require('weft/models/Thread');
  var ThreadView = require('weaver/views/ThreadView/ThreadView');
  var MassRelationFilterController = require('weaver/views/ThreadList/ThreadListView/MassRelationFilterController');

  function simulateClick(element) {

    var event = document.createEvent('MouseEvent');
    event.initEvent('click', true, true);
    element.dispatchEvent(event);
  }

  describe('weaver/views/ThreadList/ThreadListView/MassRelationFilterController.js', function () {

    beforeEach(function () {

      this.threads = _.times(3, function (index) {
        return new Thread({
          id: index,
          itemType: new ItemType()
        });
      });

      this.threads[2].unshiftOperation({
        operator: 'SOME_OPERATOR',
        parameters: {}
      });

      this.views = this.threads.map(function (thread) {
        return new ThreadView({
          model: thread
        });
      });

      var div = this.div = document.createElement('div');

      this.views.forEach(function (view) {
        div.appendChild(view.el);
      });

      document.body.appendChild(this.div);

      new MassRelationFilterController({
        el: this.div
      });

      this.fiberId = uuid.v4();

      this.$fiber = $('<div>').addClass('mas-action-filterRelated').data('fiber', new Item({
        'l.logicalId': this.fiberId
      })).appendTo(this.views[0].el);


    });

    afterEach(function () {
      document.body.removeChild(this.div);
    });

    // this is now done via EventBus events.. come back and fix these tests.
    describe.skip('click .mas-action-filterRelated', function () {
      it('Prepends a FILTER_RELATED operation on each Thread, apart from the one with the fiber used as operand', function () {
        simulateClick(this.$fiber[0]);

        var expectedResults = [
          [], // First thread is the one with the fiber, query stays empty
          [{  // Second thread had an empty query, so gets only the FILTER_RELATED op
            operator: Operation.FILTER_RELATED_ID,
            parameters: {
              id: this.fiberId
            },
            'w.origin': MassRelationFilterController.OPERATION_ORIGIN
          }],
          [{  // Third thread had an operation already, the FILTER_RELATED should be first in pipeline
            operator: Operation.FILTER_RELATED_ID,
            parameters: {
              id: this.fiberId
            },
            'w.origin': MassRelationFilterController.OPERATION_ORIGIN
          }, {
            operator: 'SOME_OPERATOR',
            parameters: {}
          }]
        ];

        expect(this.threads[0].get('query').get('operationPipeline')).to.deep.equal(expectedResults[0]);
        expect(this.threads[1].get('query').get('operationPipeline')).to.deep.equal(expectedResults[1]);
        expect(this.threads[2].get('query').get('operationPipeline')).to.deep.equal(expectedResults[2]);
      });

      it('Updates existing mass FILTER_RELATED operation if the Thread already had one', function () {
        this.threads[1].unshiftOperation({
          operator: Operation.FILTER_RELATED_ID,
          parameters: {
            id: uuid.v4()
          },
          'w.origin': MassRelationFilterController.OPERATION_ORIGIN
        });

        simulateClick(this.$fiber[0]);

        expect(this.threads[1].get('query').get('operationPipeline')).to.deep.equal([{
          operator: Operation.FILTER_RELATED_ID,
          parameters: {
            id: this.fiberId
          },
          'w.origin': MassRelationFilterController.OPERATION_ORIGIN
        }]);
      });
    });

    describe('click .mas-action-clearFilterRelated', function () {
      beforeEach(function () {

        // Add the button to the DOM
        this.$clear = $('<div>').addClass('mas-action-clearFilterRelated').appendTo(this.div);

        this.otherFiberId = uuid.v4();

        // Second thread will have a FILTER_RELATED operation
        // that doesn't come from the controller
        this.threads[1].unshiftOperation({
          operator: Operation.FILTER_RELATED_ID,
          parameters: {
            id: this.otherFiberId
          }
        });

        simulateClick(this.$fiber[0]);
      });

      it('Clears threads from their FILTER_RELATED operations introduced by the MassRelationFilterController', function () {
        simulateClick(this.$clear[0]);

        var expectedResults = [
          [], // First thread still empty
          [{  // Second thread should keep its original FILTER RELATED operation
            operator: Operation.FILTER_RELATED_ID,
            parameters: {
              id: this.otherFiberId
            }
          }],
          [{ // Third thread should be back to having only its originl operation
            operator: 'SOME_OPERATOR',
            parameters: {}
          }]
        ];

        expect(this.threads[0].get('query').get('operationPipeline')).to.deep.equal(expectedResults[0]);
        expect(this.threads[1].get('query').get('operationPipeline')).to.deep.equal(expectedResults[1]);
        expect(this.threads[2].get('query').get('operationPipeline')).to.deep.equal(expectedResults[2]);
      });
    });
  });

});
