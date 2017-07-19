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
  var ItemType = require('weft/models/ItemType');
  var ActionDefinition = require('weft/models/ActionDefinition');
  var Metric = require('weft/models/Metric');

  describe('weft/models/ItemType.js', function () {

    beforeEach(function () {

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
          },
          'group-by-attribute-2': {
            name: 'Group by attribute 2'
          }
        },
        actions: {
          item: {
            'action-id': {
              name: 'Human readable action name',
              // Other properties ommited
            }
          }
        },
        operations: {
          'GROUP_BY': ['group-by-attribute-1', 'group-by-attribute-2']
        },
        orderedAttributes: ['other-attribute', 'attribute', 'hidden-attribute']
      };

      this.itemType = new ItemType(this.properties);
    });

    describe('getVisibleAttributes()', function () {
      it('Should return only attributes that are visible, in the ordered declared by the properties', function () {

        console.log(this.itemType.getVisibleAttributes());

        expect(_.pluck(this.itemType.getVisibleAttributes(), 'id')).to.deep.equal(['other-attribute', 'attribute',]);
      });
    });

    describe('getAttributesForOperation()', function () {
      it('Should return the list of attributes available for provided operation', function () {

        expect(_.pluck(this.itemType.getAttributesForOperation('GROUP_BY'), 'id')).to.deep.equal(['group-by-attribute-1','group-by-attribute-2']);
      });
    });

    describe('getActions()', function () {
      it('Should return the list of actions available for this itemType', function () {
        var actions = this.itemType.getActions();
        expect(actions.length).to.equal(1);
        expect(actions[0]).to.be.instanceOf(ActionDefinition);
        expect(actions[0].id).to.equal('action-id');
      });
    });

    describe('getMetrics()', function () {

      it('Should return the list of metrics', function () {
        var metrics = this.itemType.getMetrics();
        expect(metrics.length).to.equal(1);
        expect(metrics[0]).to.be.instanceOf(Metric);
        expect(metrics[0].id).to.equal('metric-attribute');
      });
    });
  });

}));
