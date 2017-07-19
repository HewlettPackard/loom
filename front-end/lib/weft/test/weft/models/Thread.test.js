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
  var Thread = require('weft/models/Thread');
  var Aggregation = require('weft/models/Aggregation');
  var Item = require('weft/models/Item');
  var Metric = require('weft/models/Metric');
  var Query = require('weft/models/Query');
  var QueryResult = require('weft/models/QueryResult');
  var ItemType = require('weft/models/ItemType');
  var Operation = require('weft/models/Operation');

  describe('weft/models/Thread.js', function () {

    beforeEach(function () {

      this.metrics = [
        new Metric({
          'id': 'fixedRangeMetric',
          'name': 'Fixed range metric',
          'min': 10,
          'max': 50
        }),
        new Metric({
          'id': 'dynamicRangeMetric-current',
          'name': 'Dynamic range metric (current)',
          'dynamicRange': 'current'
        }),
        new Metric({
          'id': 'dynamicRangeMetric-history',
          'name': 'Dynamic range metric (history)',
          'dynamicRange': 'history'
        })
      ];

      this.itemElements = new Backbone.Collection([
        new Item({
          'dynamicRangeMetric-current': 15,
          'dynamicRangeMetric-history': 20
        }),
        new Item({
          'dynamicRangeMetric-current': 210,
          'dynamicRangeMetric-history': 75
        }),
        new Item({
          'dynamicRangeMetric-current': 31,
          'dynamicRangeMetric-history': 765
        })
      ]);

      this.threadElements = new Backbone.Collection([
        new Aggregation({
          'dynamicRangeMetric-current': 30,
          'dynamicRangeMetric-history': 25,
          numberOfItems: 20,
          'l.tags': Operation.BRAID_ID
        }),
        new Aggregation({
          'dynamicRangeMetric-current': 30,
          'dynamicRangeMetric-history': 25,
          numberOfItems: 30,
          'l.tags': Operation.BRAID_ID
        }),
        new Aggregation({
          'dynamicRangeMetric-current': 30,
          'dynamicRangeMetric-history': 25,
          numberOfItems: 50,
          'l.tags': Operation.BRAID_ID
        })
      ]);

      this.groupElements = new Backbone.Collection([
        new Aggregation({
          'type': 'group',
          'dynamicRangeMetric-current': 8,
          'dynamicRangeMetric-history': 25,
          'l.tags': Operation.GROUP_BY_ID
        }),
        new Aggregation({
          'type': 'group',
          'dynamicRangeMetric-current': 365,
          'dynamicRangeMetric-history': 38,
          'l.tags': Operation.GROUP_BY_ID
        }),
        new Aggregation({
          'type': 'group',
          'dynamicRangeMetric-current': 145,
          'dynamicRangeMetric-history': 510,
          'l.tags': Operation.GROUP_BY_ID
        })
      ]);

      this.clusterElements = new Backbone.Collection([
        new Aggregation({'l.tags': Operation.BRAID_ID}),
        new Aggregation({'l.tags': Operation.BRAID_ID})
      ]);
    });

    describe('isSameAs()', function () {

      it('Should return true if compared Thread is the same object', function () {

        var thread = new Thread();
        expect(thread.isSameAs(thread)).to.be.true;
      });

      // TODO: Add more tests maybe?
      // Like actually testing if it compares different Threads properly
    });

    describe('refreshMetricsRanges()', function () {

      beforeEach(function () {

        this.thread = new Thread({
          metrics: new Backbone.Collection(this.metrics)
        });

        this.thread.resetElements(this.itemElements.toArray());
      });

      it('Should update the range of dynamic metrics when Thread elements are reset', function () {

        expect(this.metrics[0].get('min')).to.equal(10);
        expect(this.metrics[0].get('max')).to.equal(50);

        expect(this.metrics[1].get('min')).to.equal(15);
        expect(this.metrics[1].get('max')).to.equal(210);

        expect(this.metrics[2].get('min')).to.equal(20);
        expect(this.metrics[2].get('max')).to.equal(765);
      });

      it('Should set minimum as 0 if all values are the same', function () {

        var elements = [
          new Item({
            'dynamicRangeMetric-current': 30,
            'dynamicRangeMetric-history': 25,
            numberOfItems: 20
          }),
          new Item({
            'dynamicRangeMetric-current': 30,
            'dynamicRangeMetric-history': 25,
            numberOfItems: 30
          }),
          new Item({
            'dynamicRangeMetric-current': 30,
            'dynamicRangeMetric-history': 25,
            numberOfItems: 50
          })
        ];

        this.thread.resetElements(elements);

        expect(this.metrics[0].get('min')).to.equal(10);
        expect(this.metrics[0].get('max')).to.equal(50);

        expect(this.metrics[1].get('min')).to.equal(0);

        // 'history'-based dynamicRange won't have the same value
        expect(this.metrics[2].get('min')).to.equal(20);
      });
    });

    describe('getAttributesForOperation()', function () {

      var operations = {};
      operations[Operation.GROUP_BY_ID] = ['attribute-a', 'attribute-c'];

      var thread = new Thread({
        itemType: {
          id: 'some-item-type',
          attributes: {
            'attribute-a': {
              id: 'attribute-a',
              name: 'Attribute A'
            },
            'attribute-b': {
              id: 'attribute-b',
              name: 'Attribute B'
            },
            'attribute-c': {
              id: 'attribute-c',
              name: 'Attribute C'
            }
          },
          operations: operations
        }
      });

      it('Returns the list of attributes available for the operation', function () {
        expect(thread.getAttributesForOperation(Operation.GROUP_BY_ID)).to.deep.equal({
          'attribute-a': {
            id: 'attribute-a',
            name: 'Attribute A'
          },
          'attribute-c': {
            id: 'attribute-c',
            name: 'Attribute C'
          }
        });
      });
    });

    describe('isContainingItems()', function () {

      it('Should return true if Thread elements are Items', function () {

        var thread = new Thread({
          elements: this.itemElements
        });

        expect(thread.isContainingItems()).to.be.true;
      });

      it('Should return false if Thread elements are not Items', function () {

        var thread = new Thread({
          elements: this.threadElements
        });

        expect(thread.isContainingItems()).to.be.false;
      });

      it('Should return false if Thread contains no elements', function () {

        var thread = new Thread();

        expect(thread.isContainingItems()).to.be.false;
      });
    });

    describe('isContainingClusters()', function () {

      it('Should return false if Thread contains no elements', function () {

        var thread = new Thread();
        expect(thread.isContainingClusters()).to.be.false;
      });

      it('Should return true if the Thread contains clusters', function () {

        var thread = new Thread({
          elements: this.clusterElements
        });
        expect(thread.isContainingClusters()).to.be.true;
      });

      it('Should return false if the Thread contains Items', function () {

        var thread = new Thread({
          elements: this.itemElements
        });
        expect(thread.isContainingClusters()).to.be.false;
      });

      it('Should return false if the Thread contains groups', function () {

        var thread = new Thread({
          elements: this.groupElements
        });
        expect(thread.isContainingClusters()).to.be.false;
      });
    });

    describe('elements', function () {

      beforeEach(function () {
        this.thread = new Thread();
        this.thread.get('result').set('elements', this.itemElements);

        this.itemType = new ItemType({
          operations: {}
        });
      });

      it('Should get updated when the QueryResult gets new elements', function () {

        expect(this.thread.get('elements').models).to.have.members(this.itemElements.models);
      });

      it('Should get updated when a non-pending QueryResult is set', function () {

        var newResult = new QueryResult({
          pending: false,
          elements: this.groupElements,
          itemType: this.itemType
        });

        this.thread.set('result', newResult);

        expect(this.thread.get('elements').models).not.to.have.members(this.itemElements.models);
        expect(this.thread.get('elements').models).to.have.members(this.groupElements.models);
      });

      it('Should get updated when a non-pending empty QueryResult is set', function () {

        var newResult = new QueryResult({
          pending: false,
          elements: new Backbone.Collection([]),
          itemType: this.itemType
        });

        this.thread.set('result', newResult);

        expect(this.thread.get('elements').models).not.to.have.members(this.itemElements.models);
        expect(this.thread.get('elements').size()).to.equal(0);
      });

      it('Should not get updated when a pending QueryResult is set', function () {
        var newResult = new QueryResult({
          pending: true,
          itemType: this.itemType
        });

        this.thread.set('result', newResult);
        expect(this.thread.get('elements').models).to.have.members(this.itemElements.models);
      });

      it('Should get updated after new QueryResult gets new elements', function () {
        var newResult = new QueryResult({
          pending: true,
          itemType: this.itemType
        });

        this.thread.set('result', newResult);

        newResult.set({
          pending: false,
          elements: this.groupElements
        });

        expect(this.thread.get('elements').models).not.to.have.members(this.itemElements.models);
        expect(this.thread.get('elements').models).to.have.members(this.groupElements.models);
      });

      it('Should get updated after new QueryResult gets empty list of elements', function () {
        var newResult = new QueryResult({
          pending: true,
          itemType: this.itemType
        });

        this.thread.set('result', newResult);

        newResult.set({
          pending: false,
          elements: []
        });

        expect(this.thread.get('elements').models).not.to.have.members(this.itemElements.models);
        expect(this.thread.get('elements').size()).to.equal(0);
      });
    });

    describe('getSummary()', function () {

      beforeEach(function () {
        this.thread = new Thread();

        function addAlerts(element, index) {
          element.alert.set('level', index);
          if (element instanceof Aggregation) {
            element.alert.set('count', 10);
          }
        }

        this.itemElements.forEach(addAlerts);

        this.threadElements.forEach(addAlerts);
      });

      it('Should return the number of items displayed in the Thread', function () {

        this.thread.resetElements(this.itemElements.toArray());
        expect(this.thread.getSummary().numberOfItems).to.equal(3);
      });

      it('Should aggregate the number of items when Thread contains groups or clusters', function () {

        this.thread.resetElements(this.threadElements.toArray());
        expect(this.thread.getSummary().numberOfItems).to.equal(100);
      });

      it('Should sum the number of items with alerts in the Thread', function () {

        this.thread.resetElements(this.itemElements.toArray());
        expect(this.thread.getSummary().numberOfAlerts).to.equal(2);

        this.thread.resetElements(this.threadElements.toArray());
        expect(this.thread.getSummary().numberOfAlerts).to.equal(20);
      });
    });

    describe('outdated', function () {

      beforeEach(function () {

        var elements = [];
        _.times(5, function (index) {

          elements[index] = new Item({
            id: 'element-' + index
          });
        });

        this.originalElements = _.values(_.pick(elements, [0, 1, 2, 3]));
        this.newElements = _.values(_.pick(elements, [0, 2, 3, 4]));

        this.thread = new Thread();
        this.thread.resetElements(this.originalElements);
      });

      it('Should mark Thread as outdated when the query changes, and clear it when results are received', sinon.test(function () {

        // Elements get marked as outdated when the thread get grouped
        this.thread.set('query', new Query());

        expect(this.thread.get('outdated')).to.be.true;

        this.thread.get('result').set('pending', false);
      }));
    });

    // No longer needed with loom v2
    describe('sortBy()', function () {

      beforeEach(function () {

        this.initialElements = _.times(5, function (index) {
          return new Item({
            'l.logicalId': index,
            'l.entityType': 'Item',
            stringProperty: '' + (index % 2 ? index : -index),
            numberProperty: index % 3 ? index : -index
          });
        });

        this.thread = new Thread();

        this.availableSorts = [new Sort({
          id: 'stringProperty'
        }), new Sort({
          id: 'numberProperty'
        })];

        this.thread.availableSorts.reset(this.availableSorts);

        this.thread.resetElements(this.initialElements);
      });

      it('Should sort the Threads element', function () {

        this.thread.sortBy(this.availableSorts[0]);

        // '-2', '-4', '0', '1'. '3'
        var expectedOrder = [2, 4, 0, 1, 3];
        this.thread.get('elements').forEach(function (element, index) {

          expect(element.id).to.equal(expectedOrder[index]);
        });

        this.thread.sortBy(this.availableSorts[1]);

        // -3, 0, 1, 2, 4
        expectedOrder = [3, 0, 1, 2, 4];
        this.thread.get('elements').forEach(function (element, index) {

          expect(element.id).to.equal(expectedOrder[index]);
        });
      });

      it('Should set the Thread in the specified order', function () {

        this.availableSorts[0].set('order', Sort.ORDER_DESCENDING);

        this.thread.sortBy(this.availableSorts[0]);

        var expectedOrder = [3, 1, 0, 4, 2];
        this.thread.get('elements').forEach(function (element, index) {

          expect(element.id).to.equal(expectedOrder[index]);
        });
      });

      it('Should adjust sorting when Sort order changes', function () {

        this.availableSorts[0].set('order', Sort.ORDER_DESCENDING);

        this.thread.sortBy(this.availableSorts[0]);

        this.thread.get('sort').set('order', Sort.ORDER_ASCENDING);

        var expectedOrder = [2, 4, 0, 1, 3];
        this.thread.get('elements').forEach(function (element, index) {

          expect(element.id).to.equal(expectedOrder[index]);
        });
      });

      it('Should keep elements sorted', function () {

        this.thread.sortBy(this.availableSorts[0]);

        _.forEach(this.initialElements, function (element, index) {

          element.set('stringProperty', '' + (index % 3 ? index : -index));
        });

        this.thread.resetElements(this.initialElements);

        var expectedOrder = [3, 0, 1, 2, 4];
        this.thread.get('elements').forEach(function (element, index) {

          expect(element.id).to.equal(expectedOrder[index]);
        });
      });
    });

    describe('Events', function () {

      describe('reset:elements', function () {

        beforeEach(function () {

          this.originalElements = [
            new Item(),
            new Item(),
            new Item()
          ];

          this.thread = new Thread({
            elements: new Backbone.Collection(this.originalElements)
          });

          this.newElements = [
            this.originalElements[1],
            new Item()
          ];
        });

        it('Should provide a delta with the current elements', function (done) {

          this.thread.on('reset:elements', function (collection, options) {

            expect(options.previousModels).to.have.length(3);

            // Check delta show which ements are added
            expect(options.delta.added).to.have.length(1);
            expect(options.delta.added).to.contain(this.newElements[1]);

            // Check delta show which elements are removed
            expect(options.delta.removed).to.have.length(2);
            expect(options.delta.removed).to.contain(this.originalElements[0]);
            expect(options.delta.removed).to.contain(this.originalElements[2]);
            done();
          }, this);

          this.thread.resetElements(this.newElements);
        });
      });
    });

    describe('toJSON()', function () {

      it('Should encode the Thread according to the API format', function () {

        var thread = new Thread({
          id: 'thread-id',
          itemType: {
            id: 'itemType-id'
          },
          query: {
            inputs: ['aggregation-id'],
            operationPipeline: []
          }
        }, {parse: true});

        var expectedJSON = {
          id: 'thread-id',
          itemType: 'itemType-id',
          query: {
            inputs: ['aggregation-id'],
            operationPipeline: [{
              operator: Operation.SORT_BY_ID,
              parameters: {
                property: 'fullyQualifiedName',
                order: 'ASC'
              }
            }],
            includeExcludedItems: true
          }
        };

        expect(thread.toJSON()).to.deep.equal(expectedJSON);
      });
    });

    describe.skip('parse()', function () {

      it('Should parse the query', function () {

        var spy = sinon.spy(Query.prototype, 'parse');

        var thread = new Thread({
          query: {
            operationPipeline: []
          }
        }, {
          parse: true
        });

        expect(thread.get('query')).to.be.an.instanceof(Query);
        expect(spy).to.have.been.called;
      });
    });

    describe('refreshResult()', function () {

      beforeEach(function () {
        this.thread = new Thread();
      });

      it('Should set a flag to mark ongoing polling and success', sinon.test(function () {

        var spy = this.spy();
        this.thread.on('change:polled', spy);

        this.thread.refreshResult();

        expect(this.thread.get('polled')).to.equal('polling');
        expect(spy).to.have.been.called;

        this.requests[0].respond(200, {'Content-Type': 'application/json'}, JSON.stringify({}));

        expect(this.thread.get('polled')).to.equal('success');
        expect(spy).to.have.been.calledTwice;
      }));

      it('Should set a flag to mark ongoing polling and failure', sinon.test(function () {

        var spy = this.spy();
        this.thread.on('change:polled', spy);

        this.thread.refreshResult();

        expect(this.thread.get('polled')).to.equal('polling');
        expect(spy).to.have.been.called;

        this.requests[0].respond(404);

        expect(this.thread.get('polled')).to.equal('failed');
        expect(spy).to.have.been.calledTwice;
      }));
    });
  });

});
