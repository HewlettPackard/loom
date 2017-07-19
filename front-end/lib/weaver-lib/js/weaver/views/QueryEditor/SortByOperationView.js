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
  var Sort = require('weft/models/Sort');
  var SortView = require('weaver/views/SortView');
  var PropertySelector = require('weaver/views/PropertySelector');
  /** DefaultOperationView */
  var DefaultOperationView = require('./DefaultOperationView');

  /**
   * @class SortByOperationView
   * @namespace views.QueryEditor
   * @module  weaver
   * @submodule views.QueryEditor
   * @constructor
   * @extends DefaultOperationView
   */
  return DefaultOperationView.extend({

    /**
     * @method initialize
     */
    initialize: function () {
      DefaultOperationView.prototype.initialize.apply(this, arguments);
      // Need to use capture phase here so no fancy jQuery or Backbone event listening
      this.el.addEventListener('click', _.bind(this._preventOrderReversingOnSelection, this), true);
    },

    /**
     * @method renderOperationParameter
     * @param operationParameter
     * @returns {*|any}
     */
    renderOperationParameter: function (operationParameter) {
      if (operationParameter.id !== 'order') {
        return DefaultOperationView.prototype.renderOperationParameter.apply(this, arguments);
      }
    },

    /**
     * @method renderAttributeList
     * @param operationParameter
     * @param operator
     * @returns {JQuery}
     */
    renderAttributeList: function (operationParameter, operator) {
      var currentParameterValue = this.model.parameters[operationParameter.id];
      var availableAttributes = this.options.thread.getAttributesForOperation(operator);
      var availableSorts = this.availableSorts = _.reduce(availableAttributes, function (sortList, attribute, attributeId) {
        sortList[attributeId] = new Sort({
          id: attributeId,
          property: new Backbone.Model(attribute)
        });
        if (currentParameterValue === attributeId && this.model.parameters.order === 'DSC') {
          sortList[attributeId].set('order', Sort.ORDER_DESCENDING);
        }
        return sortList;
      }, {}, this);
      var propertySelector = new PropertySelector({
        model: availableSorts,
        selection: currentParameterValue,
        preventDeselectionOnSameClick: true,
        propertyView: SortView
      });
      propertySelector.$el.addClass('mas-operationEditor--parameter')
                          .attr('name', operationParameter.id)
                          .on('didClearHighlighting', _.bind(this._restoreSortOrderOnUnselectedElements, this));
      return propertySelector.$el;
    },

    /**
     * @method _restoreSortOrderOnUnselectedElements
     * @private
     */
    _restoreSortOrderOnUnselectedElements: function () {
      var $descendingUnselectedElement = this.$('.mas-sortView-descending:not(.mas-property-selected)');
      var sort = this.availableSorts[$descendingUnselectedElement.data('property')];
      if (sort) {
        sort.set('order', Sort.ORDER_ASCENDING);
      }
    },

    /**
     * @method _preventOrderReversingOnSelection
     * @param event
     * @private
     */
    _preventOrderReversingOnSelection: function (event) {
      if (!event.target.classList.contains('mas-property-selected')) {
        event.preventOrderReversing = true;
      }
    },

    /**
     * @method updateParameters
     */
    updateParameters: function () {
      this.model.parameters = {};
      var selectedSort = this.$('[name=property]').data('view').getSelection();
      if (selectedSort) {
        this.model.parameters.property = selectedSort;
        this.model.parameters.order = (this.availableSorts[selectedSort].get('order') === Sort.ORDER_ASCENDING) ? 'ASC' : 'DSC';
      }
    }
  });
});