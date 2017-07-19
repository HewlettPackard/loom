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
  /** @type EditableOperationView */
  var EditableOperationView = require('./EditableOperationView');
  var PropertySelector = require('weaver/views/PropertySelector');

  /**
   * @class DefaultOperationView
   * @namespace  views.QueryEditor
   * @module  weaver
   * @submodule views.QueryEditor
   * @constructor
   * @extends EditableOperationView
   */
  var DefaultOperationView = EditableOperationView.extend({

    tagName: 'li',

    className: EditableOperationView.prototype.className + ' mas-operation mas-defaultOperation',

    events: _.defaults({
      'change [data-updateOn=change]': function (event) {
        event.preventDefault();
        this.updateOperation();
      }
    }, EditableOperationView.prototype.events),

    /**
     * @method renderEditionState
     */
    renderEditionState: function () {
      EditableOperationView.prototype.renderEditionState.apply(this, arguments);
      this.renderTitle();
      this.renderEditionForm();
    },

    /**
     * @method renderTitle
     * @param withParams
     */
    renderTitle: function (withParams) {
      var title = this.getOperationTitle();
      if (withParams && this.options.operationDescription && !_.isEmpty(this.options.operationDescription.displayParameters)) {
        title += ' (' + this.renderParams(this.options.operationDescription) + ')';
      }
      var className = this.options.operationDescription && this.options.operationDescription.icon;
      this.$('.mas-operation--title').text(title).addClass(className);
    },

    /**
     * @method getOperationTitle
     * @returns {*}
     */
    getOperationTitle: function () {
      var title = this.model && this.model.operator;
      if (this.options.operationDescription) {
        return this.options.operationDescription.name ||
          this.options.operationDescription.id ||
          title;
      }
      return title;
    },

    /**
     * @method renderParams
     * @param operationDescription
     * @returns {TResult|string}
     */
    renderParams: function (operationDescription) {
      return _(operationDescription.displayParameters).map(function (parameterId) {
        return _.find(operationDescription.params, {id: parameterId});
      }, this).map(function (parameter) {
        return {
          value: this.model.parameters[parameter.id],
          parameter: parameter
        };
      }, this).reduce(function (result, data) {
        if (result !== '') {
          result += ', ';
        }
        var value = data.value;
        if (data.parameter.type === 'ATTRIBUTE_LIST') {
          value = this.getAttributeName(value) || value;
        }
        result += value;
        return result;
      }, '', this);
    },

    /**
     * @method getAttributeName
     * @param attributeId
     * @returns {IAttributeProperties|any|*|string|Object}
     */
    getAttributeName: function (attributeId) {
      var attribute = this.options.thread.getAttribute(attributeId);
      return attribute && attribute.name;
    },

    /**
     * @method renderEditionForm
     */
    renderEditionForm: function () {
      this.$('.mas-operationEditor--parameter').remove();
      if (this.options.operationDescription &&
        this.options.operationDescription.params &&
        this.options.operationDescription.params.length) {
        var formContent = _.reduce(this.options.operationDescription.params, function (fragment, operationParameter) {
          var parameterInput = this.renderOperationParameter(operationParameter, this.options.operationDescription.id);
          if (parameterInput) {
            fragment.appendChild(parameterInput);
          }
          return fragment;
        }, document.createDocumentFragment(), this);
        this.$('.mas-operation--form').prepend(formContent);
        if (this.isOperationUpdatingAutomatically()) {
          this.$el.addClass('is-updatingAutomatically');
        }
      } else {
        this.$el.addClass('has-noParameters');
      }
    },

    /**
     * @method renderOperationParameter
     * @param operationParameter
     * @param operator
     * @returns {*}
     */
    renderOperationParameter: function (operationParameter, operator) {
      var $result;
      switch (operationParameter.type) {
        case 'ATTRIBUTE_LIST':
          $result = this.renderAttributeList(operationParameter, operator);
          break;
        case 'INT':
        case 'FLOAT':
          $result = $('<input/>', {
            type: 'number',
            'class': 'mas-operationEditor--parameter',
            name: operationParameter.id,
            value: this.getParameterValue(operationParameter)
          });
          break;
        default:
          $result = $('<input/>', {
            type: 'text',
            'class': 'mas-operationEditor--parameter',
            name: operationParameter.id,
            value: this.getParameterValue(operationParameter)
          });
      }
      if (operationParameter.update) {
        $result.attr('data-updateOn', operationParameter.update);
      }
      return $result.get(0);
    },

    /**
     * @method getParameterValue
     * @param operationParameter
     * @returns {TModel|*|number|string}
     */
    getParameterValue: function (operationParameter) {
      var modelValue = this.model && this.model.parameters && this.model.parameters[operationParameter.id];
      return modelValue || operationParameter.defaultValue;
    },

    /**
     * @method isOperationUpdatingAutomatically
     * @returns {boolean}
     */
    isOperationUpdatingAutomatically: function () {
      return !_.find(this.options.operationDescription.params, function (parameter) {
        return !parameter.update;
      });
    },

    /**
     * @method renderAttributeList
     * @param operationParameter
     * @param operator
     * @returns {*}
     */
    renderAttributeList: function (operationParameter, operator) {
      var availableAttributes = this.options.thread.getAttributesForOperation(operator);
      if (_.size(availableAttributes) === 1) {
        var attributeId = _.keys(availableAttributes)[0];
        var attribute = availableAttributes[attributeId];
        this.model.parameters[operationParameter.id] = attributeId;
        var DOMAttributes = {
          'name': operationParameter.id,
          'data-value': attributeId
        };
        return $('<div>').addClass('mas-operationEditor--parameter mas-operationEditor--frozenParameter')
        .text(attribute.name)
        .attr(DOMAttributes);
      } else {
        var propertySelector = new PropertySelector({
          model: availableAttributes,
          selection: this.model.parameters[operationParameter.id]
        });
        propertySelector.$el.addClass('mas-operationEditor--parameter')
        .attr('name', operationParameter.id);
        return propertySelector.$el;
      }
    },

    /**
     * @method renderDisplayState
     */
    renderDisplayState: function () {
      EditableOperationView.prototype.renderDisplayState.apply(this, arguments);
      this.renderTitle(!this.model.suggested);
    },

    /**
     * @method updateOperation
     */
    updateOperation: function () {
      this.updateParameters();
      this.model['w.origin'] = DefaultOperationView.OPERATION_ORIGIN;
      EditableOperationView.prototype.updateOperation.apply(this, arguments);
    },

    /**
     * @method updateParameters
     */
    updateParameters: function () {
      this.model.parameters = {};
      if (this.options.operationDescription &&
        this.options.operationDescription.params) {
        _.reduce(this.options.operationDescription.params, function (parameterValues, operationParameter) {
          parameterValues[operationParameter.id] = this.readParameterValue(operationParameter);
          return parameterValues;
        }, this.model.parameters, this);
      }
    },

    /**
     * @method readParameterValue
     * @param operationParameter
     * @returns {*}
     */
    readParameterValue: function (operationParameter) {
      var $input = this.$('[name=' + operationParameter.id + ']');
      if ($input.hasClass('mas-operationEditor--frozenParameter')) {
        return $input.data('value');
      }
      switch (operationParameter.type) {
        case 'ATTRIBUTE_LIST':
          return $input.data('view').getSelection();
        case 'FLOAT':
          return parseFloat($input.val(), 10);
        case 'INT':
          return parseInt($input.val(), 10);
        default:
          return $input.val();
      }
    }
  });

  DefaultOperationView.OPERATION_ORIGIN = 'DefaultOperationView';

  return DefaultOperationView;
});
