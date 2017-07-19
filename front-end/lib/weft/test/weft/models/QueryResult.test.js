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
/*global describe, it, expect, beforeEach, sinon */
/*jshint expr: true */
define(function (require) {

  "use strict";

  var _ = require('lodash');
  var Backbone = require('backbone');
  var QueryResult = require('weft/models/QueryResult');
  var Item = require('weft/models/Item');
  var Aggregation = require('weft/models/Aggregation');
  var ItemType = require('weft/models/ItemType');
  var AggregationItemType = require('weft/models/AggregationItemType');

  describe('weft/models/QueryResult.js', function () {

    describe('refresh()', function () {
      it('Sends a request for the results', sinon.test(function () {
        var result = new QueryResult();
        result.refresh();
        expect(this.requests[0]).not.to.be.undefined;
      }));

      it('Should cancel existing request', sinon.test(function () {
        var result = new QueryResult();
        result.refresh();
        result.refresh();
        expect(this.requests[0].aborted).to.be.true;
      }));
    });

    describe('cancelRefresh()', function () {
      it('Should cancel current refresh, if there is one', sinon.test(function () {
        var result = new QueryResult();
        result.refresh();
        result.cancelRefresh();
        expect(this.requests[0].aborted).to.be.true;
      }));
    });

    describe('isRefreshing()', function () {
      it('Should return true when the query result is being refreshed', sinon.test(function () {
        var result = new QueryResult();
        result.refresh();
        expect(result.isRefreshing()).to.be.true;
      }));

      it('Should return false when the query result has been refreshed', sinon.test(function () {
        var result = new QueryResult();
        result.refresh();
        this.requests[0].respond(200, {
          'Content-Type': 'application/json'
        }, JSON.stringify({
          elements: []
        }));
        // Might need to update the meaning to "after parsing is done"
        expect(result.isRefreshing()).to.be.false;
      }));

      it('Should return false if the refresh fails', sinon.test(function () {
        var result = new QueryResult();
        result.refresh();
        this.requests[0].respond(500, {
          'Content-Type': 'application/json'
        }, JSON.stringify({}));
        // Might need to update the meaning to "after parsing is done"
        expect(result.isRefreshing()).to.be.false;
      }));

      it('Should return false after refresh is canceled', sinon.test(function () {
        var result = new QueryResult();
        result.refresh();
        result.cancelRefresh();
        expect(result.isRefreshing()).to.be.false;
      }));
    });

    describe('parse()', function () {
      beforeEach(function () {
        this.itemType = {
          id: '/some/itemType'
        };
        this.existingItems = new Backbone.Collection(_.times(5, function (index) {
          return new Item({
            'l.logicalId': '/da/' + index,
          }, {
            itemType: this.itemType
          });
        }, this));
        var json = {
          id: '/da/result',
          status: 'complete',
          itemType: this.itemType,
          elements: [{
            entity: {
              // Ha, Java ;)
              '@class': '.Item',
              // Loom metadata, flattened in a `l.` namespace
              // so there's no collision with the item attributes
              'l.entityType': 'Item',
              'l.logicalId': '/providers/test/items/item0',
              'l.typeId': '/typeids/TestType',
              'l.created': '1398358970512',
              // Anything else is considered an attribute of the element
              name: 'Name of the item',
              ip: '127.0.0.1',
              os: 'Ubuntu'
            },
            // Tags will help the client distinguish between different flavours
            // of a given entityType (eg. aggregations resulting from grouping
            // vs. aggregations resulting from other operations)
            'l.tags': '',
            'l.relations': ['/da/2', '/da/4']
          }, {
            entity: {
            // Ha, Java ;)
              '@class': '.Aggregation',
              // Loom metadata, flattened in a `l.` namespace
              // so there's no collision with the item attributes
              'l.entityType': 'Aggregation',
              'l.logicalId': '/da/result/0',
              'l.typeId': '/typeids/TestType',
              'l.created': '1398358970512',
              numberOfItems: 138,
              createdCount: 1,
              deletedCount: 4,
              updatedCount: 8
            },
            // Tags will help the client distinguish between different flavours
            // of a given entityType (eg. aggregations resulting from grouping
            // vs. aggregations resulting from other operations)
            'l.tags': 'group_by',
            'l.relations': ['/da/1']
          }]
        };
        this.result = new QueryResult();
        this.result.set(this.result.parse(json, this.existingItems));
      });

      it('Should create the appropriate instance', function () {
        expect(this.result.get('elements')).to.have.length(2);
        expect(this.result.get('elements').at(0)).to.be.an.instanceof(Item, 'Item');
        expect(this.result.get('elements').at(1)).to.be.an.instanceof(Aggregation, 'Aggregation');
      });

      it('Should set the appropriate itemType on the instances', function () {
        expect(this.result.get('elements').at(0).itemType.id).to.equal('/some/itemType');
        expect(this.result.get('elements').at(0).itemType).to.be.an.instanceof(ItemType);
        expect(this.result.get('elements').at(1).itemType.id).to.equal('/some/itemType');
        expect(this.result.get('elements').at(1).itemType).to.be.an.instanceof(AggregationItemType);
      });

      it('Should reuse the instance when an element has the same ID', sinon.test(function () {
        var original = this.result.get('elements').at(0);
        // Make sure the original element is going to be in the list of elements that are indexed
        this.existingItems.add(original);
        var newResponse = {
          id: '/da/result',
          status: 'complete',
          elements: [{
            entity: {
              // Ha, Java ;)
              '@class': '.Item',
              // Loom metadata, flattened in a `l.` namespace
              // so there's no collision with the item attributes
              'l.entityType': 'Item',
              'l.logicalId': '/providers/test/items/item0',
              'l.typeId': '/typeids/TestType',
              'l.created': '1398358970512',
              // Anything else is considered an attribute of the element
              name: 'Name of the item',
              ip: '127.0.0.2',
              os: 'Ubuntu'
            },
            // Tags will help the client distinguish between different flavours
            // of a given entityType (eg. aggregations resulting from grouping
            // vs. aggregations resulting from other operations)
            'l.tags': '',
            'l.relations': []
          }]
        };
        var element = this.result._parseElement(newResponse.elements[0], {}, this.existingItems);
        expect(element).to.equal(original);
        expect(original.get('ip')).to.equal('127.0.0.2');
      }));

      it('Should merge the attributes', function () {
        expect(_.pick(this.result.get('elements').at(0).attributes, 'name', 'ip', 'os')).to.deep.equal({
          name: 'Name of the item',
          ip: '127.0.0.1',
          os: 'Ubuntu'
        });
        expect(_.pick(this.result.get('elements').at(1).attributes, 'numberOfItems', 'createdCount', 'deletedCount', 'updatedCount', 'l.tags')).to.deep.equal({
          numberOfItems: 138,
          createdCount: 1,
          deletedCount: 4,
          updatedCount: 8,
          'l.tags': 'group_by'
        });
      });

      it('Should update its `pending` status according to the results', function (done) {
        sinon.test(function () {
          var result = new QueryResult({
            elements: [],
            status: 'PENDING'
          }, {
            parse: true,
            fibersIndex: this.fibersIndex
          });
          expect(result.get('pending')).to.be.true;
          result.fetch();
          result.once('change:pending', function () {
            expect(result.get('pending')).to.be.false;
            done();
          });
          this.requests[0].respond(200, {'Content-Type': 'application/json'}, JSON.stringify({
            elements: [],
            status: 'COMPLETE'
          }));
        }).apply(this);
      });

      it('Should add an l.index attribute to elements so they can be sorted back to their original order', function() {
        expect(this.result.attributes.elements.models[0].get('l.index')).to.equal(0);
        expect(this.result.attributes.elements.models[1].get('l.index')).to.equal(1);
      });
    });

    describe('_getNewInstance', function() {
      var itemType = new ItemType({});
      it('Should create an Aggregation for an Aggregation entity type', function() {
        var queryResult = new QueryResult();
        var item = queryResult._getNewInstance({'l.entityType': 'Aggregation'}, itemType);
        expect(item).to.be.an.instanceof(Aggregation);
      });
      it('Should create an Item when not an Aggregation entity type', function() {
        var queryResult = new QueryResult();
        var item = queryResult._getNewInstance({'l.entityType': 'NotAnAggregation'}, itemType);
        expect(item).to.be.an.instanceof(Item);
      });
    });

    describe('_extractEntityId', function() {
      it('Should correctly extract the entity ID from an Aggregation', function() {
        var queryResult = new QueryResult();
        var entityId = queryResult._extractEntityId({
          'l.entityType': 'Aggregation',
          'l.semanticId': 5,
          'l.logicalId': 10
        });
        expect(entityId).to.equal(5);
      });
      it('Should correctly extract the entity ID from a logical item', function() {
        var queryResult = new QueryResult();
        var entityId = queryResult._extractEntityId({
          'l.entityType': 'Item',
          'l.semanticId': 5,
          'l.logicalId': 10
        });
        expect(entityId).to.equal(10);
      });
    });

    describe('_parseAlert', function() {
      it('Should correctly extract the highest alert level for an Aggregation', function () {
        var queryResult = new QueryResult(),
          level = 6,
          description = 'something bad',
          count = 360,
          result = queryResult._parseAlert({
            'l.entityType': 'Aggregation',
            'highestAlertLevel': level,
            'highestAlertDescription': description,
            'alertCount': count
          });
        expect(result.level).to.equal(level);
        expect(result.description).to.equal(description);
        expect(result.count).to.equal(count);
      });
      it('Should have correct default values for an Aggregation', function () {
        var queryResult = new QueryResult();
        var result = queryResult._parseAlert({
          'l.entityType': 'Aggregation'
        });
        expect(result.level).to.equal(0);
        expect(result.description).to.be.undefined;
        expect(result.count).to.equal(0);
      });
      it('Should correctly extract the highest alert level for an Item', function () {
        var queryResult = new QueryResult(),
          level = 6,
          description = 'something bad',
          result = queryResult._parseAlert({
            'l.entityType': 'NotAnAggregation',
            'l.alertLevel': level,
            'l.alertDescription': description
          });
        expect(result.level).to.equal(level);
        expect(result.description).to.equal(description);
        expect(result.count).to.equal(0);
      });
      it('Should have correct default values for an Item', function () {
        var queryResult = new QueryResult();
        var result = queryResult._parseAlert({
          'l.entityType': 'NotAnAggregation'
        });
        expect(result.level).to.equal(0);
        expect(result.description).to.equal('');
        expect(result.count).to.equal(0);
      });
    });
    
    describe('hasElement', function() {
      beforeEach(function() {
        this.itemType = {id: '/some/itemType'};
        this.itemA =  new Item({'l.logicalId': '/da/0'}, {itemType: this.itemType});
        this.itemB =  new Item({'l.logicalId': '/da/1'}, {itemType: this.itemType});
        this.queryResult = new QueryResult();
        this.queryResult.set('elements', new Backbone.Collection());
      });
      it('Should be false if there are no elements', function() {
        expect(this.queryResult.get('elements').length).to.equal(0);
        expect(this.queryResult.hasElement({})).to.be.false;
      });
      it('Should be false if the passed element is not in the collection', function() {
        this.queryResult.get('elements').add(this.itemA);
        expect(this.queryResult.hasElement({})).to.be.false;
        expect(this.queryResult.hasElement(this.itemB)).to.be.false;
      });
      it('Should be true if the passed element is in the collection', function() {
        this.queryResult.get('elements').add(this.itemA);
        expect(this.queryResult.hasElement(this.itemA)).to.be.true;
      });
    });
  });
});
