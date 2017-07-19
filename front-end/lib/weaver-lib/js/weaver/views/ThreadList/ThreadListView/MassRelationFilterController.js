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

  var $ = require('jquery');
  var _ = require('lodash');
  /** @type BaseView */
  var BaseView = require('weaver/views/BaseView');
  var Operation = require('weft/models/Operation');
  var confirm = require('weaver/utils/confirm').confirm;

  /**
   * @class  MassRelationFilterController
   * @module weaver
   * @submodule views.ThreadListView
   * @namespace  views.ThreadListView
   * @constructor
   * @extends BaseView
   */
  var MassRelationFilterController = BaseView.extend({

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.listenTo(this.EventBus, 'thread:fiber:action:filter' , this.filterClickEventListener);
      this.listenTo(this.EventBus, 'thread:fiber:action:clear-filter-related' , this.clearFilter);
    },

    /**
     * @method getEditingQueryEditor
     * @returns {JQuery|jQuery|HTMLElement}
     */
    getEditingQueryEditor: function () {
      return $('.mas-queryEditor.is-editing');
    },

    filterClickEventListener: function (event) {
      var $editingQueryEditor = this.getEditingQueryEditor();
      if ($editingQueryEditor.length) {
        confirm("You are currently editing one of the Threads' query. Do you want to apply your changes before filtering related items?")
        .then(function (confirmation) {
          if (confirmation) {
            $editingQueryEditor.data('view').updateQuery();
            this.applyFilter(event.fiber, event.fiberView);
            this.updateTapestryFilteredStatus(true);
          }
        }.bind(this))
        .done();
      } else {
        this.applyFilter(event.fiber, event.fiberView);
        this.updateTapestryFilteredStatus(true);
      }
    },

    /**
     * @method applyFilter
     * @param fiber
     * @param sourceElement
     */
    applyFilter: function (fiber, sourceElement) {
      var operation;
      this.relationSource = fiber;
      $('.mas-threadView-main').filter(function (index, mainThread) {
        return !mainThread.contains(sourceElement.el);
      }).map(function (index, mainThread) {
        return $(mainThread).data('view').model;
      }).each(function (index, thread) {

        if (!this.hasMassFilterOperation(thread)) {
          operation = this.createOperation(fiber.get('l.logicalId'));
          thread.unshiftOperation(operation);
        } else {
          this.updateOperation(thread, fiber.get('l.logicalId'));
        }
      }.bind(this));
    },

    /**
     * @method createOperation
     * @param fiberId
     * @returns {{operator: string, parameters: {id: *}, [w.origin]: string}}
     */
    createOperation: function (fiberId) {
      return {
        operator: Operation.FILTER_RELATED_ID,
        parameters: {
          id: fiberId
        },
        'w.origin': MassRelationFilterController.OPERATION_ORIGIN
      };
    },

    /**
     * @method isMassFilterOperation
     * @param operation
     * @returns {boolean}
     */
    isMassFilterOperation: function (operation) {
      return operation['w.origin'] === MassRelationFilterController.OPERATION_ORIGIN;
    },

    /**
     * @method updateOperation
     * @param thread
     * @param fiberId
     */
    updateOperation: function (thread, fiberId) {
      var pipeline = thread.get('query').get('operationPipeline');
      thread.setPipeline(_.reduce(pipeline, function (result, operation) {
        if (this.isMassFilterOperation(operation)) {
          operation = this.createOperation(fiberId);
        }
        result.push(operation);
        return result;
      }, [], this));
    },

    /**
     * @method clearFilter
     */
    clearFilter: function () {
      $('.mas-threadView-main').map(function (index, mainThread) {
        return $(mainThread).data('view').model;
      }).each(this.removeOperation.bind(this));
      this.updateTapestryFilteredStatus(false);
    },

    /**
     * @method removeOperation
     * @param index
     * @param thread
     */
    removeOperation: function (index, thread) {
      if (this.hasMassFilterOperation(thread)) {
        this.doRemoveOperation(thread);
      }
    },

    /**
     * @method hasMassFilterOperation
     * @param thread
     * @returns {boolean}
     */
    hasMassFilterOperation: function (thread) {
      return !!_.find(
        thread.get('query').get('operationPipeline'),
        {'w.origin': MassRelationFilterController.OPERATION_ORIGIN}
      );
    },

    /**
     * @method doRemoveOperation
     * @param thread
     */
    doRemoveOperation: function (thread) {
      var pipeline = thread.get('query').get('operationPipeline');
      thread.setPipeline(_.reduce(pipeline, function (result, operation) {
        if (!this.isMassFilterOperation(operation)) {
          result.push(operation);
        }
        return result;
      }, [], this));
    },

    updateTapestryFilteredStatus: function(filtered) {
      $('.mas-tapestryScreen').toggleClass('is-filtered', filtered);
    }
  });

  /**
   * @property OPERATION_ORIGIN
   * @type {String}
   * @final
   * @default MassRelationFilterController
   */
  MassRelationFilterController.OPERATION_ORIGIN = 'MassRelationFilterController';

  return MassRelationFilterController;

});
