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
"use strict";

var _ = require('lodash');
var ActionDefinition = require('weft/models/ActionDefinition');
var Metric = require('weft/models/Metric');

/**
 * @class ItemType
 * @module weft
 * @namespace  models
 * @constructor
 * @param {Object} properties The ItemType properties, as provided by the server
 */
function ItemType(properties) {
  _.merge(this, properties);
}

_.extend(ItemType.prototype, {

  /**
   * Returns the definition of attribute with given attributeId
   * @method getAttribute
   * @param {String} attributeId
   * @return {Object} The definition of the attribute if found
   */
  getAttribute: function (attributeId) {
    return this.attributes[attributeId];
  },

  /**
   * Returns the list of visible attributes on this ItemType
   * @method  getVisibleAttributes
   * @return {Array} The list of visible attributes
   */
  getVisibleAttributes: function () {

    if (this.orderedAttributes) {
      return this.orderedAttributes.map(function (attributeId) {
        var attribute = this.attributes[attributeId];
        attribute.id = attributeId;
        if (attribute.visible) {
          return attribute;
        }
      }.bind(this)).filter(function (attribute) {

        return attribute;
      });
    }

    return _.reduce(this.attributes, function (result, attribute, attributeId) {

      if (attribute.visible) {
        attribute.id = attributeId;
        result.push(attribute);
      }

      return result;
    }, []);
  },

  /**
   * Returns the list of attributes available for the operation with given `operationId`
   * @param  {string} operationId The ID of the operation
   * @return {Array}              The list of attributes
   */
  getAttributesForOperation: function (operationId) {

    if (!this.operations) {
      return [];
    }

    return _.map(this.operations[operationId], function (attributeId) {

      var attribute = this.attributes[attributeId];
      attribute.id = attributeId;
      return attribute;
    }, this);
  },

  /**
   * Returns the list of actions available for this ItemType
   * @method getActions
   * @return {Array} The list of actions
   */
  getActions: function () {

    return _.map(this.actions && this.actions.item, function (action, actionId) {
      var definition = new ActionDefinition(action);
      definition.id = actionId;
      return definition;
    });
  },

  getThreadActions: function () {
    return _.mapValues(this.actions && this.actions.thread, function (action, actionId) {
      var definition = new ActionDefinition(action);
      definition.id = actionId;
      return definition;
    });
  },

  /**
   * Returns the list of metrics available for this ItemType
   * @method  getMetrics
   * @return {Array} The list of metrics
   */
  getMetrics: function () {
    return _.reduce(this.attributes, function (result, attribute, attributeId) {

      if (attribute.plottable) {

        var min = parseInt(attribute.min, 10);
        var max = parseInt(attribute.max, 10);

        result.push(new Metric({
          id: attributeId,
          name: attribute.name,
          unit: attribute.unit,
          min: isNaN(min) ? 0 : min,
          max: isNaN(max) ? 'Inf' : max
        }));
      }

      return result;
    }, []);
  }
});

ItemType.prototype.constructor = ItemType;

module.exports = ItemType;
