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

  var GroupByDMAStatusController = Backbone.View.extend({

    events: {
      'click [data-action=addGroupByDMAStatus]': function() {
        event.preventDefault();
        this.addOperation();
      },
      'click [data-action=removeGroupByDMAStatus]': function () {
        event.preventDefault();
        this.removeOperation();
      }
    },

    initialize: function () {

      this.listenTo(this.model, 'change:query', this.updateToggleAction);
      this.updateToggleAction();
    },

    updateToggleAction: function () {

      var updatedAction = this.model.any(this.doesThreadHaveOperation) ?
        'removeGroupByDMAStatus' : 'addGroupByDMAStatus';

      this.$('[data-action=addGroupByDMAStatus],[data-action=removeGroupByDMAStatus]')
        .attr('data-action', updatedAction);
    },

    doesThreadHaveOperation: function (thread) {
      return _.any(thread.get('query').get('operationPipeline'), function (operation) {
        return operation['w.origin'] === GroupByDMAStatusController.OPERATION_ORIGIN;
      });
    },

    addOperation: function () {

      this.model.forEach(function (thread) {

        if (this.hasDMAStatusAttribute(thread) && !this.hasGroupByDMAStatusOperation(thread)) {
          thread.pushOperation({
            operator: Operation.GROUP_BY_ID,
            parameters: {
              property: GroupByDMAStatusController.DMA_STATUS_ATTRIBUTE_ID
            },
            'w.origin': GroupByDMAStatusController.OPERATION_ORIGIN
          });
        }
      }, this);
    },

    removeOperation: function () {

      this.model.forEach(function (thread) {
        if (this.doesThreadHaveOperation(thread)) {

          var pipeline = thread.get('query').get('operationPipeline');
          thread.setPipeline(_.reduce(pipeline, function (result, operation) {

            if (operation['w.origin'] !== GroupByDMAStatusController.OPERATION_ORIGIN) {
              result.push(operation);
            }

            return result;
          }, []));
        }
      }, this);
    },

    hasDMAStatusAttribute: function (thread) {

      return !!thread.get('itemType').getAttribute(GroupByDMAStatusController.DMA_STATUS_ATTRIBUTE_ID);
    },

    hasGroupByDMAStatusOperation: function (thread) {

      return _.any(thread.get('query').get('operationPipeline'), function (operation) {
        return operation.operator === Operation.GROUP_BY_ID &&
               operation.parameters.property === GroupByDMAStatusController.DMA_STATUS_ATTRIBUTE_ID;
      });
    }
  }, {
    OPERATION_ORIGIN: 'GroupByDMAStatusController',
    DMA_STATUS_ATTRIBUTE_ID: 'core.dmaStatus'
  });

  return GroupByDMAStatusController;
});
