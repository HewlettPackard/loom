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
(define(function (require){

  "use strict";

  var _ = require('lodash');
  var Operation = require('weft/models/Operation');
  var AggregationItemType = require('weft/models/AggregationItemType');
  var Metric = require('weft/models/Metric');
  var ActionDefinition = require('weft/models/ActionDefinition');

  describe('weft/models/AggregationItemType.js', function () {

    beforeEach(function () {

      var operations = {};
      operations[Operation.SORT_BY_ID] = ['group-by-attribute-1', 'group-by-attribute-2'];
      operations[Operation.GROUP_BY_ID] = ['attribute'];

      this.properties = {
        id: 'itemType-id',
        attributes: {
          'attribute': {
            name: 'Attribute',
            visible: true
          },
          'hidden-attribute': {
            name: "Hidden attribute",
            visible: false
          },
          'metric-attribute': {
            name: 'Metric attribute',
            plottable: true
          },
          'other-attribute': {
            name: 'Other attribute',
            visible: true
          },
          'group-by-attribute-1': {
            name: 'Group by attribute 1',
            plottable: true,
          },
          'group-by-attribute-2': {
            name: 'Group by attribute 2'
          }
        },
        actions: {
          aggregation: {
            'action-id': {
              name: 'Human readable action name',
              // Other properties ommited
            }
          }
        },
        operations: operations
      };

      this.itemType = new AggregationItemType(this.properties);
    });

    describe('getVisibleAttributes()', function () {

      it('Returns the attributes for the aggregation, rather than the contained Items item type', function () {
        expect(_.pluck(this.itemType.getVisibleAttributes(), 'id')).to.all.satisfy(function (attributeId) {
            return _.contains([
              'name',
              'minIndex',
              'maxIndex',
              'numberOfItems',
              'addedCount',
              'deletedCount',
              'updatedCount',
              'alertCount',
              'highestAlertLevel',
              'highestAlertDescription'
            ], attributeId);
        });
      });
    });

    describe('getAttributesForOperation()', function () {

      it('Returns same attributes as the contained Items item type', function () {

        expect(_.pluck(this.itemType.getAttributesForOperation(Operation.GROUP_BY_ID), 'id')).to.deep.equal(['attribute']);
      });

      it('Returns additional attributes for the SORT_BY operation', function () {
        expect(_.pluck(this.itemType.getAttributesForOperation(Operation.SORT_BY_ID), 'id')).to.deep.equal(['group-by-attribute-1','numberOfItems']);
      });
    });

    describe('getMetrics()', function () {

      it('Returns the list of metrics from the original item type, as well as Number Of Items', function () {
        var metrics = this.itemType.getMetrics();
        expect(metrics.length).to.equal(3);
        expect(metrics).to.all.be.instanceOf(Metric);
        expect(metrics[0].id).to.equal('metric-attribute');
        expect(metrics[1].id).to.equal('group-by-attribute-1');
        expect(metrics[2].id).to.equal('numberOfItems');
      });
    });

    describe('getActions()', function () {

      it('Returns actions available for aggregations', function () {
        var actions = this.itemType.getActions();
        expect(actions.length).to.equal(1);
        expect(actions[0]).to.be.instanceOf(ActionDefinition);
        expect(actions[0].id).to.equal('action-id');
      });
    });
  });
}));
