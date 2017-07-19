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
  require('jquery-ui/sortable');
  /** @type Menu */
  var Menu = require('./Menu');
  var Operation = require('weft/models/Operation');
  var Query = require('weft/models/Query');
  var SuggestedOperationPipeline = require('weaver/models/SuggestedOperationPipeline');
  var template = require('./QueryEditor.html');
  var SortByOperationView = require('./QueryEditor/SortByOperationView');
  var OperationPipelineView = require('./QueryEditor/OperationPipelineView');
  var FilterRelatedOperationView = require('./QueryEditor/FilterRelatedOperationView');
  var confirm = require('weaver/utils/confirm');
  var AddOperationMenu = require('./QueryEditor/AddOperationMenu');

  /**
   * @class operationViews
   * @namespace views
   * @module  weaver
   * @submodule views
   * @type {{}}
   */
  var operationViews = {};
  operationViews[Operation.SORT_BY_ID] = SortByOperationView;
  operationViews[Operation.FILTER_RELATED_ID] = FilterRelatedOperationView;

  /**
   * The QueryEditor allows users to configure the Query of a Thread.
   * It is divided into 3 parts:
   *  - an editor for the operations in the Query's pipeline
   *  - an editor for the 'limit' operation provided by the views
   *  - an editor for the client-side sorting to be applied to the Thread's elements
   * @class QueryEditor
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends Menu
   * @constructor
   */
  var QueryEditor = Menu.extend({

    /**
     * @property className
     * @type {String}
     */
    className: Menu.prototype.className + ' mas-queryEditor',

    /**
     * @property template
     * @type {String}
     */
    template: template,

    /**
     * @property operationViews
     * @type {Object}
     */
    operationViews: operationViews,

    /**
     * @property events
     * @type {Object}
     */
    events: {
      'click [data-action=toggleEdition]': 'toggleEdition',
      // TODO: If operations are being edited use new values when updating the query
      'click [data-action=updateQuery]': 'updateQuery',
      'change [name=includeExcludedItems]': function () {
        if (!this.$el.hasClass('is-editing')) {
          this.updateQuery();
        }
      },
      'willEdit': function () {
        this.addOperationMenu.collapse();
      },
      'choseOperation': function (event) {
        this.addOperation(event.originalEvent.operator);
        this.addOperationMenu.collapse();
      },
      'click .mas-operation:not(.is-editing)': function (event) {
        if (!event.isDefaultPrevented()) {
          event.preventDefault();
          // For some reason, when using event delegation,
          // jQuery stores the element matching the selector
          // in `currentTarget`, making it differ from the DOM event's `currentTarget`
          var operation = $(event.currentTarget).data('view');
          operation.edit();
          operation.focus();
        }
      },
      'didUpdateOperation': function (event) {
        if (event.originalEvent.hasNoParameters) {
          this._confirmOperationRemoval($(event.target));
        }
        this.edit();
      },
      'didUpdatePipeline': function () {
        this.edit();
      }
    },

    /**
     * @method initialize
     * @param options
     */
    initialize: function () {
      Menu.prototype.initialize.apply(this, arguments);
      this.$operationPipeline = this.$('.mas-queryEditor--operationPipeline');
      this.$addOperationMenu = this.$('.mas-queryEditor--addOperation');
    },

    /**
     * @method expand
     */
    expand: function () {
      if (!this.availableOperators) {
        this.model.getAvailableOperations().then(function (availableOperators) {
          this.availableOperators = availableOperators;
          this.renderContent();
        }.bind(this));
      }
      this.renderContent();
      Menu.prototype.expand.apply(this, arguments);
      this.listenTo(this.model, 'change', this.renderOperationPipeline);
      this.listenTo(this.model, 'change', this.updateHasOperationThatCanExcludeItems);
      this.listenTo(this.model, 'change', this.renderIncludeExcludedItemsButton);
    },

    /**
     * @menu collapse
     */
    collapse: function () {
      this.stopListening(this.model, 'change');
      Menu.prototype.collapse.apply(this, arguments);
    },

    /**
     * @menu renderContent
     */
    renderContent: function () {
      if (this.availableOperators) {
        this.$el.removeClass('is-loading');
        this.renderOperationPipeline();
        this.renderIncludeExcludedItemsButton();
        this.renderAddOperationButton();
        this.updateHasOperationThatCanExcludeItems();
      } else {
        this.$el.addClass('is-loading');
      }
    },

    /**
     * @method toggleEdition
     */
    toggleEdition: function () {
      if (this.$el.hasClass('is-editing')) {
        this.stopEditing();
      } else {
        this.edit();
      }
    },

    /**
     * @method edit
     */
    edit: function () {
      this.$el.addClass('is-editing');
      this.updateHasOperationThatCanExcludeItems();
    },

    /**
     * @method stopEditing
     */
    stopEditing: function () {
      this.stopEditionOfCurrentOperation();
      this.$el.removeClass('is-editing');
      this.renderOperationPipeline();
      this.updateHasOperationThatCanExcludeItems();
    },

    /**
     * HAS OPERATION THAT CAN EXCLUDE ITEMS
     * @method  updateHasOperationThatCanExcludeItems
     */
    updateHasOperationThatCanExcludeItems: function () {
      if (this.hasOperationThatCanExcludeItems()) {
        this.$el.addClass('has-operationThatCanExcludeItems');
      } else {
        this.$el.removeClass('has-operationThatCanExcludeItems');
      }
    },

    /**
     * @method hasOperationThatCanExcludeItems
     * @returns {*}
     */
    hasOperationThatCanExcludeItems: function () {
      // If the pipeline has been edited, we need to use the edited one,
      // If not, we use the query's
      var pipeline = this.operationPipeline && this.operationPipeline.model ? this.operationPipeline.model : this.model.get('query').get('operationPipeline');
      return _.any(pipeline, this.canExcludeItem, this);
    },

    /**
     * @method canExcludeItem
     * @param operation
     * @returns {T|boolean}
     */
    canExcludeItem: function (operation) {
      if (!operation.suggested) {
        var description = _.find(this.availableOperators, {id: operation.operator});
        return description && description.canExcludeItems;
      }
    },

    /**
     * @method updateQuery
     */
    updateQuery: function () {
      this.$('.mas-operation.is-editing').each(function (index, el) {
        var view = $(el).data('view');
        view.updateOperation();
        view.stopEditing();
      });
      // Because some operation being edited might have no parameters, the user might be being
      // prompted for confirmation, so  we need to wait for it to happen before actually
      // updating the query
      if (this.removalConfirmation) {
        this.removalConfirmation.then(_.bind(this._doUpdateQuery, this)).done();
      } else {
        this._doUpdateQuery();
      }
    },

    /**
     * @method _doUpdateQuery
     * @private
     */
    _doUpdateQuery: function () {
      var query = this.generateQueryFromPipeline();
      this.model.set('query', query);
      this.stopEditing();
    },

    /**
     * @method generateQueryFromPipeline
     * @returns {Query}
     */
    generateQueryFromPipeline: function () {
      var pipeline = _.map(this.operationPipeline.model, function (operation) {
        return _.pick(operation, 'operator', 'parameters', 'w.origin');
      });
      var includeExcludedItems = this.hasOperationThatCanExcludeItems() ? this.$('.mas-includeExcludedItemsToggle--checkbox').prop('checked') : true;
      var query = new Query({
        inputs: this.model.get('query').get('inputs'),
        operationPipeline: pipeline,
        limit: this.model.get('query').getLimit(),
        includeExcludedItems: includeExcludedItems
      });
      return query;
    },

    /**
     * @method addOperation
     * @param operator
     */
    addOperation: function (operator) {
      this.operationPipeline.addOperation(operator);
      this.edit();
    },

    /**
     * @method stopEditionOfCurrentOperation
     */
    stopEditionOfCurrentOperation: function () {
      this.$('.mas-operation.is-editing').each(function (index, el) {
        var view = $(el).data('view');
        if (view) {
          view.stopEditing();
        }
      });
    },

    /**
     * @method renderOperationPipeline
     */
    renderOperationPipeline: function () {
      if (this.model) {
        if (!this.operationPipeline) {
          this.operationPipeline = new OperationPipelineView({
            el: this.$operationPipeline[0],
            operationViews: this.operationViews,
            availableOperators: this.availableOperators,
            suggestedPipeline: QueryEditor.SUGGESTED_OPERATION_PIPELINE,
            thread: this.model
          });
        }
        this.updateHasOperationThatCanExcludeItems();
        this.operationPipeline.model = _.cloneDeep(this.model.get('query').get('operationPipeline'));
        this.operationPipeline.render();
      }
    },

    /**
     * @method renderIncludeExcludedItemsButton
     */
    renderIncludeExcludedItemsButton: function () {
      this.$('[name=includeExcludedItems]').attr('checked', this.model.get('query').get('includeExcludedItems'));
    },

    /**
     * @method renderAddOperationButton
     */
    renderAddOperationButton: function () {
      this.addOperationMenu = new AddOperationMenu({
        el: this.$addOperationMenu,
        model: this.availableOperators
      });
    },

    /**
     * @method _confirmOperationRemoval
     * @param $operationViewElement
     * @private
     */
    _confirmOperationRemoval: function ($operationViewElement) {
      var operation = $operationViewElement.data('view').model.operator;
      this.removalConfirmation = confirm.confirm("You've set no parameters for the '" + operation + "' operation, do you want to remove it?");
      this.removalConfirmation.then(_.bind(function (confirmation) {
        if (confirmation) {
          $operationViewElement.remove();
        }
        this.removalConfirmation = undefined;
      }, this)).done();
    }
  });

  /**
   * @property SUGGESTED_OPERATION_PIPELINE
   * @type SuggestedOperationPipeline
   */
  QueryEditor.SUGGESTED_OPERATION_PIPELINE = new SuggestedOperationPipeline([
    Operation.FILTER_STRING_ID,
    Operation.GROUP_BY_ID,
    Operation.FILTER_STRING_ID,
    Operation.SORT_BY_ID
  ]);

  return QueryEditor;
});
