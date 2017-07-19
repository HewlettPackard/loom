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
(define(function (require) {

  "use strict";

  var _ = require('lodash');
  var Operation = require('weft/models/Operation');
  var ActionDefinition = require('weft/models/ActionDefinition');
  var Metric = require('weft/models/Metric');

  var operations = {};
  operations[Operation.SORT_BY_ID] = ['numberOfItems'];

  var aggregationItemType = {
    id: 'weaver.aggregationItemType',
    attributes: {
      'name': {
        'name': 'Name',
        visible: false
      },
      'minIndex': {
        'name': 'First item',
        visible: true
      },
      'maxIndex': {
        'name': 'Last item',
        visible: true
      },
      'numberOfItems': {
        'name': 'Number of items',
        plottable: true,
        visible: true,
        min: 0,
        max: 'Inf'
      },
      'addedCount': {
        'name': 'New items',
        visible: true
      },
      'deletedCount': {
        'name': 'Deleted items',
        visible: true
      },
      'updatedCount': {
        'name': 'Updated items',
        visible: true
      },
      'alertCount': {
        'name': 'Number of alerts',
        plottable: true,
        visible: true
      },
      'highestAlertLevel': {
        'name': 'Highest alert level',
        visible: true
      },
      'highestAlertDescription': {
        'name': 'Highest alert description',
        visible: true
      }
    },
    operations: operations
  };

  /**
   * @class AggregationItemType
   * @module weft
   * @namespace  models
   * @constructor
   * @param {Object} properties The ItemType properties, as provided by the server
   */
  function AggregationItemType(properties) {
    _.merge(this, properties);
    //this.id = this.id;
  }

  _.extend(AggregationItemType.prototype, {

    getAttribute: function (attributeId) {
      return aggregationItemType.attributes[attributeId] || this.attributes[attributeId];
    },

    /**
     * Returns the list of visible attributes on this ItemType
     * @method  getVisibleAttributes
     * @return {Array} The list of visible attributes
     */
    getVisibleAttributes: function () {

      return _.reduce(aggregationItemType.attributes, function (result, attribute, attributeId) {

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

      // TODO: Propose a "aggregatable" property on the attributes
      // to define if an attribute will be present when aggregated
      // and can be used for client side sorting (or other)
      var attributesAvailableOnItem = _.reduce(this.operations[operationId], function (attributeList, attributeId) {

        var attribute = this.attributes[attributeId];
        attribute.id = attributeId;

        if (operationId === Operation.SORT_BY_ID) {

          if (attribute.plottable) {
            attributeList.push(attribute);
          }
        } else {
          attributeList.push(attribute);
        }

        return attributeList;
      }, [], this);

      var aggregationSpecificAttributes = _.map(aggregationItemType.operations[operationId], function (attributeId) {
        var attribute = aggregationItemType.attributes[attributeId];
        attribute.id = attributeId;
        return attribute;
      });

      return attributesAvailableOnItem.concat(aggregationSpecificAttributes);
    },

    /**
     * Returns the list of actions available for this ItemType
     * @method getActions
     * @return {Array} The list of actions
     */
    getActions: function () {
      return _.map(this.actions && this.actions.aggregation, function (action, actionId) {
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
      var itemMetrics = this.getItemMetrics();
      itemMetrics.push(this.createMetric(aggregationItemType.attributes.numberOfItems, 'numberOfItems'));
      return itemMetrics;
    },

    getItemMetrics: function () {
      return _.reduce(this.attributes, function (result, attribute, attributeId) {

        if (attribute.plottable) {

          result.push(this.createMetric(attribute, attributeId));
        }

        return result;
      }, [], this);
    },

    createMetric: function (attribute, attributeId) {
      var min = parseInt(attribute.min, 10);
      var max = parseInt(attribute.max, 10);

      return new Metric({
        id: attributeId,
        name: attribute.name,
        unit: attribute.unit,
        min: isNaN(min) ? 0 : min,
        max: isNaN(max) ? 'Inf' : max
      });
    }
  });

  AggregationItemType.prototype.constructor = AggregationItemType;

  return AggregationItemType;

}));
