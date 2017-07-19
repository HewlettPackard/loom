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
  var ItemType = require('weft/models/ItemType');
  var Thread = require('weft/models/Thread');
  var GroupByDMAStatusController = require('weaver-dma/views/GroupByDMAStatusController');

  describe('weaver-dma/views/GroupByDMAStatusController', function () {

    function simulateClick(element) {

      var event = document.createEvent('MouseEvent');
      event.initEvent('click', true, true);
      element.dispatchEvent(event);
    }

    before(function () {

      // We need a few Threads with the core.dmaStatus attribute
      this.threads = new Backbone.Collection(_.times(2, function (index) {
        return new Thread({
          id: index,
          itemType: new ItemType({
            attributes: {
              'core.dmaStatus': {
                name: 'AA Status'
              }
            }
          })
        });
      }));

      // And one which doesn't have it
      this.threads.add(new Thread({
        id: 'non-dma-thread',
        itemType: new ItemType({
          attributes: {}
        })
      }));

      this.controller = new GroupByDMAStatusController({
        model: this.threads
      });
      document.body.appendChild(this.controller.el);

      this.toggle = document.createElement('button');
      this.toggle.setAttribute('data-action', 'addGroupByDMAStatus');
      this.controller.$el.append(this.toggle);
    });

    after(function () {
      this.controller.remove();
    });

    // Helper method to check how many "GROUB_BY core.dmaStatus" a Thread has
    function countOperations(thread) {

      var groupByOperationCount = _.reduce(thread.get('query').get('operationPipeline'),
        function (result, operation) {
          if (operation.operator === Operation.GROUP_BY_ID &&
                 operation.parameters.property === 'core.dmaStatus') {
                   result++;
                 }
          return result;
        }, 0);

      return groupByOperationCount;
    }

    it('Applies a GroupBy operation to all visible Threads with core.dmaStatus attribute', function () {

      simulateClick(this.toggle);

      expect(countOperations(this.threads.at(0))).to.equal(1);
      expect(countOperations(this.threads.at(1))).to.equal(1);
      expect(countOperations(this.threads.at(2))).to.equal(0);
    });

    it('Updates the toggle action to remove the operation when some threads have the operation', function () {
      expect(this.toggle.dataset.action).to.equal('removeGroupByDMAStatus');
    });

    it('Removes the operation when the toggle is set to remove', function () {
      simulateClick(this.toggle);
      this.threads.forEach(function (thread) {

        var hasGroupByOperation = _.any(thread.get('query').get('operationPipeline'),
          function (operation) {
            return operation.operator === Operation.GROUP_BY_ID &&
                   operation.parameters.property === 'core.dmaStatus' &&
                   operation['w.origin'] === GroupByDMAStatusController.OPERATION_ORIGIN;
          });

        expect(hasGroupByOperation).to.be.false;
      });
    });

    it('Updates the toggle action to add operation when all threads have lost the operation', function () {
      expect(this.toggle.dataset.action).to.equal('addGroupByDMAStatus');
    });

    it('Does not apply the operation if already present', function () {

      this.threads.at(0).pushOperation({
        operator: Operation.GROUP_BY_ID,
        parameters: {
          property: 'core.dmaStatus'
        }
      });

      simulateClick(this.toggle);

      expect(countOperations(this.threads.at(0))).to.equal(1);
      expect(countOperations(this.threads.at(1))).to.equal(1);
    });

    it('Does not remove GroupBy opearation is did not add', function () {

      simulateClick(this.toggle);

      expect(countOperations(this.threads.at(0))).to.equal(1);
      expect(countOperations(this.threads.at(1))).to.equal(0);
    });


  });
});
