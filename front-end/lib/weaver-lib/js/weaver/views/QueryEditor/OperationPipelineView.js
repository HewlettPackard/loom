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
  var BaseView = require('weaver/views/BaseView');
  var DefaultOperationView = require('./DefaultOperationView');

  /**
   * @class OperationPipelineView
   * @namespace views.QueryEditor
   * @module  weaver
   * @submodule views.QueryEditor
   * @constructor
   * @extends BaseView
   */
  var OperationPipelineView = BaseView.extend({

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.$el.sortable({
        axis: 'x',
        items: '.mas-operationPipeline--operation'
      });
    },

    /**
     * @property
     * @type {Object}
     */
    events: {
      'click [data-action=removeOperation]': function (event) {
        event.preventDefault();
        this.removeOperation(this.getOperationFromDOM(event.target));
      },
      'click [data-action=moveAfter]': function (event) {
        event.preventDefault();
        this.moveAfter(this.getOperationFromDOM(event.target));
      },
      'click [data-action=moveBefore]': function (event) {
        event.preventDefault();
        this.moveBefore(this.getOperationFromDOM(event.target));
      },
      'didUpdateOperation': function () {
        this.model = this.regeneratePipeline();
        this.dispatchCustomEvent('didUpdatePipeline');
        this.render();
      },
      // Ensure the editor only displays the save and cancel buttons
      // if the item have actually been moved when using drag'n'drop
      // for sorting
      sortstart: function (event, ui) {
        this.sortIndex = ui.item.index();
      },
      sortstop: function (event, ui) {

        if (ui.item.index() !== this.sortIndex) {
          this.moveAt(this.getOperationFromDOM(ui.item), ui.item.index());
        }
      }
    },

    /**
     * @method getOperationFromDOM
     * @param DOMElement
     * @returns {*|jQuery}
     */
    getOperationFromDOM: function (DOMElement) {
      return $(DOMElement).closest('.mas-operation').data('view').model;
    },

    /**
     * @method addOperation
     * @param operator
     */
    addOperation: function (operator) {
      var operation = {
        operator: operator,
        parameters: {}
      };
      this.editedOperation = operation;
      this.model.push(operation);
      this.dispatchCustomEvent('didUpdatePipeline');
      this.render();
    },

    /**
     * @method removeOperation
     * @param operation
     */
    removeOperation: function (operation) {
      this.model = _.without(this.model, operation);
      // this._updateSortableActivation();
      this.dispatchCustomEvent('didRemoveOperation', {
        operation: operation
      });
      this.dispatchCustomEvent('didUpdatePipeline');
      this.render();
    },

    /**
     * @method moveBefore
     * @param operation
     */
    moveBefore: function (operation) {
      var indexOfOperation = this.model.indexOf(operation);
      if (indexOfOperation !== 0) {
        this.model.splice(indexOfOperation, 1);
        this.model.splice(indexOfOperation - 1, 0, operation);
        this.dispatchCustomEvent('didUpdatePipeline');
        this.render();
      }
    },

    /**
     * @method moveAt
     * @param operation
     * @param index
     */
    moveAt: function (operation, index) {
      var indexOfOperation = this.model.indexOf(operation);
      if (index > 0 && index < this.model.length) {
        this.model.splice(indexOfOperation, 1);
        this.model.splice(index, 0, operation);
        this.dispatchCustomEvent('didUpdatePipeline');
        this.render();
      }
    },

    /**
     * @method moveAfter
     * @param operation
     */
    moveAfter: function (operation) {
      var indexOfOperation = this.model.indexOf(operation);
      if (indexOfOperation !== this.model.length - 1) {
        this.model.splice(indexOfOperation, 1);
        this.model.splice(indexOfOperation + 1, 0, operation);
        this.dispatchCustomEvent('didUpdatePipeline');
        this.render();
      }
    },

    /**
     * @method regeneratePipeline
     * @returns {Array|TResult}
     */
    regeneratePipeline: function () {
      return _.reduce(this.$('.mas-operation'), function (result, operationView) {
        var operation = $(operationView).data('view').model;
        if (operation.suggested) {
          if (operation.edited) {
            operation.suggested = false;
            result.push(operation);
          }
        } else {
          result.push(operation);
        }
        return result;
      }, []);
    },

    /**
     * @method render
     */
    render: function () {
      this.$el.empty();
      var renderedPipeline = this.model;
      // Before merging suggested operations, mark which ones are first and last
      // so arrows for moving them are displayed properly
      this.flagFirstAndLastOperation(renderedPipeline);
      if (this.options.suggestedPipeline && this.options.suggestedPipeline.isCompatible(this.model)) {
        renderedPipeline = this.options.suggestedPipeline.addSuggestedOperations(this.model);
      }
      if (renderedPipeline.length) {
        _.forEach(renderedPipeline, this.renderOperation, this);
      } else {
        this.renderEmptyMessage();
      }
      this.updateSortableActivation();
    },

    /**
     * @method flagFirstAndLastOperation
     * @param operations
     */
    flagFirstAndLastOperation: function (operations) {
      var lastIndex = operations.length - 1;
      _.forEach(operations, function (operation, index) {
        operation.first = index === 0;
        operation.last = index === lastIndex;
      });
    },

    /**
     * @method updateSortableActivation
     */
    updateSortableActivation: function () {
      if (this.model.length > 1) {
        this.$el.sortable('enable');
      } else {
        this.$el.sortable('disable');
      }
    },

    /**
     * @method renderOperation
     * @param operation
     */
    renderOperation: function (operation) {
      var ViewClass = this.getView(operation.operator);
      var view = new ViewClass({
        // Deep clone the operation so the editors don't update the current query
        model: operation,
        operationDescription: _.find(this.options.availableOperators, {id: operation.operator}) || {},
        thread: this.options.thread
      });
      if (operation === this.editedOperation) {
        view.edit();
        view.focus();
        this.editedOperation = undefined;
      }
      this.removeEmptyMessage();
      view.$el.addClass('mas-operationPipeline--operation').appendTo(this.$el);
      return view;
    },

    /**
     * @method getView
     * @param operator
     * @returns {*}
     */
    getView: function (operator) {
      return this.options.operationViews[operator] || DefaultOperationView;
    },

    /**
     * @method renderEmptyMessage
     */
    renderEmptyMessage: function () {
      this.$emptyMessage = this.$emptyMessage || $('<li class="mas-operationPipeline--emptyMessage">No operation applied to the data.</li>');
      this.$emptyMessage.appendTo(this.$el);
    },

    /**
     * @method removeEmptyMessage
     */
    removeEmptyMessage: function () {
      if (this.$emptyMessage) {
        this.$emptyMessage.remove();
      }
    }
  });

  return OperationPipelineView;
});
