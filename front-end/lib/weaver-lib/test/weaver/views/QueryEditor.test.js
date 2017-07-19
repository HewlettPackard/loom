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
  var Thread = require('weft/models/Thread');
  var Query = require('weft/models/Query');
  var QueryEditor = require('weaver/views/QueryEditor');
  var availableOperators = require('../../fixtures/availableOperators');

  describe('weaver/views/QueryEditor.js', function () {

    function simulateClick(element) {
      var event = document.createEvent('MouseEvent');
      event.initEvent('click', true, true);
      element.dispatchEvent(event);
    }

    function simulateCheck(checkboxElement) {
      checkboxElement.checked = !checkboxElement.checked;
      var event = document.createEvent('Event');
      event.initEvent('change', true, true);
      checkboxElement.dispatchEvent(event);
    }

    beforeEach(function () {
      this.query = new Query({
        operationPipeline: [{
          operator: 'BRAID',
          parameters: {
            maxFibres: 25
          }
        }, {
          operator: 'UNKNOWN_OPERATOR',
          parameters: {}
        }],
        limit: {
          operator: 'BRAID',
          parameters: {
            maxFibres: 10
          }
        }
      });
      this.thread = new Thread({
        query: this.query
      });
      this.thread.getAvailableOperations = function () {
        return {
          then: function (callback) {
            callback.call(null, availableOperators);
          }
        };
      };
      this.editor = new QueryEditor({
        model: this.thread
      });
      this.editor.expand();
      this.editor.$el.appendTo(document.body);
    });

    afterEach(function () {
      this.editor.remove();
    });

    describe("Empty pipeline", function () {

      it('Displays a pre-set pipeline when the query pipeline is empty', function () {
        this.thread.set('query', new Query({}));
        this.editor.expand();
        var operations = _.map(this.editor.$('.mas-operation'), function (operation) {
          return $(operation).data('view').model.operator;
        });
        expect(operations).to.deep.equal(QueryEditor.SUGGESTED_OPERATION_PIPELINE.operators);
      });
    });

    describe("Displays Query's operation", function () {
      it('Displays the list of operations', function () {
        var $operations = this.editor.$('.mas-operationPipeline--operation');
        expect($operations.length).to.equal(2);
        expect($operations.eq(0)).to.have.attr('data-operator', 'BRAID');
        expect($operations.eq(1)).to.have.attr('data-operator', 'UNKNOWN_OPERATOR');
      });
    });

    describe('Operation editions', function () {
      var UPDATED_PARAM = 18;
      beforeEach(function () {
        this.editor.edit();
      });
      it('Allows update of existing operations', function () {
        var $braidOperation = this.editor.$('[data-operator=BRAID]');
        simulateClick($braidOperation[0]);
        expect($braidOperation).to.have.class('is-editing');
        $braidOperation.find('[name=maxFibres]').val(UPDATED_PARAM);
        simulateClick($braidOperation.find('.mas-operationEditor--save')[0]);
        simulateClick(this.editor.$('[data-action=updateQuery]')[0]);
        expect(this.thread.get('query').get('operationPipeline')).to.deep.equal([{
          operator: 'BRAID',
          parameters: {
            maxFibres: UPDATED_PARAM
          },
          'w.origin': 'DefaultOperationView'
        }, {
          operator: 'UNKNOWN_OPERATOR',
          parameters: {}
        }]);
      });

      it('Does not change the query if canceled', function () {
        var $braidOperation = this.editor.$('[data-operator=BRAID]');
        simulateClick($braidOperation[0]);
        expect($braidOperation).to.have.class('is-editing');
        $braidOperation.find('[name=maxFibres]').val(UPDATED_PARAM);
        simulateClick($braidOperation.find('.mas-operationEditor--save')[0]);
        simulateClick(this.editor.$('[data-action=toggleEdition]')[0]);
        expect(this.thread.get('query').get('operationPipeline')).to.deep.equal([{
          operator: 'BRAID',
          parameters: {
            maxFibres: 25
          }
        }, {
            operator: 'UNKNOWN_OPERATOR',
            parameters: {}
        }]);
      });

      it('Cancels current edition when canceled', function () {
        var $braidOperation = this.editor.$('[data-operator=BRAID]');
        simulateClick($braidOperation[0]);

        expect($braidOperation).to.have.class('is-editing');

        $braidOperation.find('[name=maxFibres]').val(UPDATED_PARAM);

        simulateClick(this.editor.$('[data-action=toggleEdition]')[0]);

        expect($braidOperation).not.to.have.class('is-editing');
        expect(this.thread.get('query').get('operationPipeline')).to.deep.equal([{
          operator: 'BRAID',
          parameters: {
            maxFibres: 25
          }
        }, {
          operator: 'UNKNOWN_OPERATOR',
          parameters: {}
        }]);
      });

      it('Saves update when saving update and operations are being edited', function () {

        var $braidOperation = this.editor.$('[data-operator=BRAID]');
        simulateClick($braidOperation[0]);

        expect($braidOperation).to.have.class('is-editing');

        $braidOperation.find('[name=maxFibres]').val(UPDATED_PARAM);

        simulateClick(this.editor.$('[data-action=updateQuery]')[0]);

        expect(this.thread.get('query').get('operationPipeline')).to.deep.equal([{
          operator: 'BRAID',
          parameters: {
            maxFibres: UPDATED_PARAM
          },
          'w.origin': 'DefaultOperationView'
        }, {
          operator: 'UNKNOWN_OPERATOR',
          parameters: {}
        }]);
      });

      it('Allows to move an operation later in the pipeline', function () {
        var $braidOperation = this.editor.$('[data-operator=BRAID]');
        simulateClick($braidOperation[0]);

        simulateClick($braidOperation.find('[data-action=moveAfter]')[0]);

        expect(this.editor.$el).to.have.class('is-editing');
        var $operations = this.editor.$('.mas-operation');
        expect($operations.eq(0)).to.have.attr('data-operator', 'UNKNOWN_OPERATOR');
        expect($operations.eq(1)).to.have.attr('data-operator', 'BRAID');

        simulateClick(this.editor.$('[data-action=updateQuery]')[0]);

        expect(this.thread.get('query').get('operationPipeline')).to.deep.equal([{
          operator: 'UNKNOWN_OPERATOR',
          parameters: {}
        }, {
          operator: 'BRAID',
          parameters: {
            maxFibres: 25
          }
        }]);
      });

      it('Allows to move an operation earlier in the pipeline', function () {
        var $operation = this.editor.$('[data-operator=UNKNOWN_OPERATOR]');
        simulateClick($operation[0]);

        simulateClick($operation.find('[data-action=moveBefore]')[0]);

        expect(this.editor.$el).to.have.class('is-editing');
        var $operations = this.editor.$('.mas-operation');
        expect($operations.eq(0)).to.have.attr('data-operator', 'UNKNOWN_OPERATOR');
        expect($operations.eq(1)).to.have.attr('data-operator', 'BRAID');

        simulateClick(this.editor.$('[data-action=updateQuery]')[0]);

        expect(this.thread.get('query').get('operationPipeline')).to.deep.equal([{
          operator: 'UNKNOWN_OPERATOR',
          parameters: {}
        }, {
          operator: 'BRAID',
          parameters: {
            maxFibres: 25
          }
        }]);
      });

      it('Allows removal of existing operations', function (){

        var $originalOperations = this.editor.$('.mas-operationPipeline--operation');
        var $braidOperation = this.editor.$('[data-operator=BRAID]');
        simulateClick($braidOperation[0]);

        simulateClick($braidOperation.find('[data-action=removeOperation]')[0]);

        expect(this.editor.$el).to.have.class('is-editing');

        var $newOperations = this.editor.$('.mas-operationPipeline--operation');
        expect($newOperations.length - $originalOperations.length).to.equal(-1);
        expect($newOperations.find('[data-operator=BRAID]')).not.to.exist;

        simulateClick(this.editor.$('[data-action=updateQuery]')[0]);

        expect(this.thread.get('query').get('operationPipeline')).to.deep.equal([{
          operator: 'UNKNOWN_OPERATOR',
          parameters: {}
        }]);
      });

      if (!window.PHANTOMJS) {
        it('Allows addition of new operations', function (done) {

          var $originalOperations = this.editor.$('.mas-operationPipeline--operation');

          this.editor.$el.one('didExpand', '.mas-queryEditor--addOperation', _.bind(function () {
            simulateClick(this.editor.$('.mas-property:contains(Braid)')[0]);

            var $newOperations = this.editor.$('.mas-operationPipeline--operation');
            $newOperations.last().find('[name=maxFibres]').val(5);

            expect($newOperations.length - $originalOperations.length).to.equal(1);
            expect($newOperations.last()).to.have.attr('data-operator', 'BRAID');

            simulateClick(this.editor.$('[data-action=updateQuery]')[0]);

            expect(this.thread.get('query').get('operationPipeline')).to.deep.equal([{
              operator: 'BRAID',
              parameters: {
                maxFibres: 25
              }
            }, {
              operator: 'UNKNOWN_OPERATOR',
              parameters: {}
            }, {
              operator: 'BRAID',
              parameters: {
                maxFibres: 5
              },
              "w.origin":"DefaultOperationView"
            }]);

            done();
          }, this));

          simulateClick(this.editor.$('.mas-queryEditor--addOperation>.mas-menu--toggle')[0]);
        });

        it('Displays previous operations after cancelation', function (done) {

          var $braidOperation = this.editor.$('[data-operator=BRAID]');
          simulateClick($braidOperation[0]);
          $braidOperation.find('[name=maxFibres]').val(10);
          simulateClick($braidOperation.find('.mas-operationEditor--save')[0]);

          var $unknowOperation = this.editor.$('[data-operator="UNKNOWN_OPERATOR"]');
          simulateClick($unknowOperation[0]);
          simulateClick($unknowOperation.find('[data-action=removeOperation]')[0]);

          simulateClick(this.editor.$('.mas-queryEditor--addOperation>.mas-menu--toggle')[0]);

          this.editor.$el.one('didExpand', _.bind(function () {

            simulateClick(this.editor.$('.mas-property:contains(Filter)')[0]);

            var $filterOperation = this.editor.$('[data-operator=FILTER_STRING]');
            $filterOperation.find('[name=pattern]').val('foo');
            simulateClick($filterOperation.find('.mas-operationEditor--save')[0]);

            // Quick check that the display gets updated
            expect(this.editor.$('[data-operator=BRAID] .mas-operation--display')).to.contain(10);
            expect(this.editor.$('[data-operator=UNKNOWN_OPERATOR]')).not.to.exist;
            expect(this.editor.$('[data-operator=FILTER_STRING] .mas-operation--display')).to.contain('foo');

            // Then cancelation!!!
            simulateClick(this.editor.$('[data-action=toggleEdition]')[0]);

            expect(this.editor.$('[data-operator=BRAID] .mas-operation--display')).to.contain(25);
            expect(this.editor.$('[data-operator=UNKNOWN_OPERATOR] .mas-operation--display')).to.contain('UNKNOWN_OPERATOR');
            expect(this.editor.$('[data-operator=FILTER_STRING] .mas-operation--display')).not.to.exist;

            done();
          }, this));
        });
      }

      it('Displays toggle for requesting excluded results when the pipeline contains operation that might exclude items', function (done) {
        this.editor.$el.one('didExpand', _.bind(function () {

          // Add a Filter operation
          simulateClick(this.editor.$('.mas-property:contains(Filter)')[0]);

          expect(this.editor.$el).to.have.class('has-operationThatCanExcludeItems');
          expect(this.editor.$('.mas-includeExcludedItemsToggle--checkbox')).to.be.checked;

          // Setup the operation
          var $filterOperation = this.editor.$('[data-operator=FILTER_STRING]');
          $filterOperation.find('[name=pattern]').val('foo');
          simulateClick($filterOperation.find('.mas-operationEditor--save')[0]);

          simulateClick(this.editor.$('[data-action=updateQuery]')[0]);

          expect(this.editor.$el).to.have.class('has-operationThatCanExcludeItems');
          done();
        }, this));

        simulateClick(this.editor.$('.mas-queryEditor--addOperation>.mas-menu--toggle')[0]);
      });

      it('Updates the query when clicking on the toggle and the pipeline was not being edited', function () {

        this.editor.stopEditing();
        simulateCheck(this.editor.$('.mas-includeExcludedItemsToggle--checkbox')[0]);
        expect(this.thread.get('query').get('includeExcludedItems')).to.be.true;
      });
    });
  });
});
