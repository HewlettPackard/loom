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
/* global describe, it, expect, beforeEach, sinon */
/* jshint expr: true, strict: false */
define(function (require) {

  var _ = require('lodash');
  var Backbone = require('backbone');
  var Element = require('weft/models/Element');
  var Metric = require('weft/models/Metric');
  var Thread = require('weft/models/Thread');
  var Tapestry = require('weft/models/Tapestry');
  var AggregatorClient  = require('weft/services/AggregatorClient');
  var QueryResult = require('weft/models/QueryResult');
  var ActionDefinition = require('weft/models/ActionDefinition');

  var mockItemType1 = {
    id: 't-1',
    getVisibleAttributes: function () {
      return [];
    },
    attributes: {
      '1:2:type-a': {},
      '1:2:type-b': {},
    }
  };

  var mockItemType2 = {
    id: 't-2',
    getVisibleAttributes: function () {
      return [];
    },
    attributes: {
      '1:2:type-a': {},
      '3:2:type-a': {},
      '1:2:type-b': {},
    }
  };

  var mockItemType3 = {
    id: 't-3',
    getVisibleAttributes: function () {
      return [];
    },
    attributes: {
      '3:2:type-a': {},
    }
  };


  describe('weft/models/Element.js', function () {

    function addRelatedElement() {

      this.element.addRelation(this.relatedElement);

      expect(this.relatedElement.isRelatedTo(this.element)).to.be.true;
    }

    function removeRelatedElement() {
      this.element.removeRelation(this.relatedElement);
      expect(this.relatedElement.isRelatedTo(this.element)).to.be.false;
    }

    // To test the Element, we need...
    beforeEach(function () {

      // ... a few elements ...
      this.relatedElement = new Element({
        'l.logicalId': 'relatedElement',
        name: 'Related Element',
      });
      this.relatedElement.itemType = mockItemType2;
      this.anotherRelatedElement = new Element({
        'l.logicalId': 'anotherRelatedElement'
      });
      this.anotherRelatedElement.itemType = mockItemType3;
      this.yetAnotherRelatedElement = new Element({
        'l.logicalId': 'yetAnotherRelatedElement'
      });
      this.yetAnotherRelatedElement.itemType = mockItemType2;
      this.oldRelation = new Element({
        'l.logicalId': 'oldRelatedElement'
      });
      this.oldRelation.itemType = mockItemType3;
      this.anotherOldRelation = new Element({
        'l.logicalId': 'anotherOldRelatedElement'
      });
      this.anotherOldRelation.itemType = mockItemType3;

      // ... an Element ...
      this.element = new Element({
        'l.logicalId': 'element'
      });
      this.element.itemType = mockItemType1;

      this.parentThread = new Thread({id: 'parent'});
      this.grandparentThread = new Thread({id: 'grandparent'});

      this.tapestry = new Tapestry([], {
        aggregator: new AggregatorClient()
      });

      this.tapestry.add(this.parentThread);
      this.tapestry.add(this.grandparentThread);

      var index = this.tapestry.get('fibersIndex').get('index');

      index.add(this.element);

      _.forEach([this.relatedElement, this.anotherRelatedElement,
                 this.yetAnotherRelatedElement, this.oldRelation, this.anotherOldRelation],
        function (el) {
          index.add(el);
          el.set('parent', new Thread({ id: _.unique() }));
          this.tapestry.add(el.get('parent'));
          el.get('parent').set('itemType', el.itemType);
      }, this);

      this.parentThread.set('itemType', mockItemType1);
      this.element.set('parent', this.parentThread);
      this.parentThread.set('parent', this.grandparentThread);
    });

    describe('setState()', function () {

      beforeEach(function () {

        this.element = new Element();
        this.element.itemType = mockItemType1;
      });

      it('Should set provided state for provided duration', sinon.test(function () {

        var duration = 1000;

        this.element.setState(Element.STATE_UPDATED, duration);
        this.clock.tick(duration / 2);
        expect(this.element.state).to.equal(Element.STATE_UPDATED);
        this.element.setState(Element.STATE_ADDED, duration);
        this.clock.tick(duration / 2);
        // Checks that the first duration expiring did not revert the state
        expect(this.element.state).to.equal(Element.STATE_ADDED);
        this.clock.tick(duration / 2);
        expect(this.element.state).to.be.undefined;
      }));

      it('Should trigger an event when state changes', sinon.test(function () {

        var spy = sinon.spy();
        this.element.on('didSetState', spy);

        this.element.setState(Element.STATE_ADDED, 1000);
        expect(spy).to.have.been.called;
        this.element.setState(Element.STATE_ADDED, 1000);
        expect(spy).to.have.been.called; //And not calledTwice
        this.element.setState(Element.STATE_UPDATED, 1000);
        expect(spy).to.have.been.calledTwice;
        this.clock.tick(1000);
        expect(spy).to.have.been.calledThrice;
      }));
    });

    describe('actions', function () {

      var actionDefinitions = _.times(5, function (index) {
        return new ActionDefinition({
          id: index
        });
      });

      var element = new Element();
      element.itemType = mockItemType1;
      element.itemType.getActions = function () {
        return actionDefinitions;
      };

      describe('getActionDefinitions()', function () {

        it('Should return the list of actions available for the Element', function () {

          var definitions = element.getActionDefinitions();

          expect(definitions).to.deep.equal(actionDefinitions);
        });
      });

      describe('getActionDefinition()', function () {

        it('Should return the action with given ID', function () {

          var actionDefinition = element.getActionDefinition(2);

          expect(actionDefinition).to.equal(actionDefinitions[2]);
        });

        it('Should return `undefined` if no action with such ID exist', function () {

          var actionDefinition = element.getActionDefinition('nonExistingId');

          expect(actionDefinition).to.be.undefined;
        });
      });
    });

    describe('Relations', function () {

      it('Should add element to the related element relations when a relation is added', addRelatedElement);

      it('Should trigger a `add:relations` event when a relation is added', function (done) {

        this.element.on('add:relations', function (relation, element) {

          expect(relation).to.equal(this.relatedElement);
          expect(element).to.equal(this.element);
          done();
        }, this);

        addRelatedElement.apply(this);
      });

      it('Should remove element from previously related element relations when a relation is removed', function () {

        addRelatedElement.apply(this);
        removeRelatedElement.apply(this);
      });

      it('Should trigger a `remove:relations` event when a relation is removed', function (done) {

        this.element.on('remove:relations', function (relation, element) {
          expect(relation).to.equal(this.relatedElement);
          expect(element).to.equal(this.element);
          done();
        }, this);

        addRelatedElement.apply(this);
        removeRelatedElement.apply(this);
      });

      describe('getRelatedElements()', function () {

        beforeEach(function () {

          this.element.addRelation(this.relatedElement);
          this.element.addRelation(this.anotherRelatedElement);
          this.element.addRelation(this.yetAnotherRelatedElement);

          this.element.set('l.relationTypes', {
            '1:2:type-a': [this.relatedElement.get('l.logicalId')],
            '3:2:type-a': [this.anotherRelatedElement.get('l.logicalId')],
            // Duplication of this.relatedElement to test that result doesn't contain
            // duplicated element when querying multiple relations
            '1:2:type-b': [this.relatedElement.get('l.logicalId'), this.yetAnotherRelatedElement.get('l.logicalId')]
          });

          // Last bit missing for the relationship traversing from this.element.
          this.yetAnotherRelatedElement.set('l.relationTypes', {
            '3:2:type-a': [this.anotherRelatedElement.get('l.logicalId')],
          });
        });

        it('Should return the list of explicitely defined relations', function () {

          var relations = this.element.getRelatedElements();

          var explicitRelations = this.element.get('relations');
          _.forEach(explicitRelations, function (relatedElement) {
            expect(_.contains(relations, relatedElement)).to.be.true;
          });
        });

        it('Should return the list of elements with specific relations', function () {

          var typeARelations = this.element.getRelatedElements('type-a');
          expect(typeARelations).to.have.length(1);
          expect(typeARelations[0]).to.equal(this.relatedElement);
        });

        it('Should return the list of elements with specific relations (2 here)', function () {

          var relations = this.element.getRelatedElements(['type-b']);
          expect(relations).to.have.length(2);
          _.forEach([this.yetAnotherRelatedElement, this.relatedElement],
            function (relatedElement) {
              expect(_.contains(relations, relatedElement)).to.be.true;
          });
        });

        it('Should return the list of elements with one of the specific relations when multiple relations are provided', function () {

          var relations = this.element.getRelatedElements(['type-a', 'type-b']);
          expect(relations).to.have.length(3);
          _.forEach([this.relatedElement, this.yetAnotherRelatedElement, this.anotherRelatedElement],
            function (relatedElement) {
              expect(_.contains(relations, relatedElement)).to.be.true;
          });
        });
      });

      describe('isRelatedTo()', function () {

        beforeEach(function () {

          this.element.addRelation(this.relatedElement);
          this.element.addRelation(this.anotherRelatedElement);
          this.element.addRelation(this.yetAnotherRelatedElement);

          this.element.set('l.relationTypes', {
            '1:2:type-a': [this.relatedElement.get('l.logicalId')],
            '3:2:type-a': [this.anotherRelatedElement.get('l.logicalId')],
            // Duplication of this.relatedElement to test that result doesn't contain
            // duplicated element when querying multiple relations
            '1:2:type-b': [this.relatedElement.get('l.logicalId'), this.yetAnotherRelatedElement.get('l.logicalId')]
          });

          // Last bit missing for the relationship traversing from this.element.
          this.yetAnotherRelatedElement.set('l.relationTypes', {
            '3:2:type-a': [this.anotherRelatedElement.get('l.logicalId')],
          });
        });

        it('Should return true if element is in the list of relations', function () {

          expect(this.element.isRelatedTo(this.relatedElement)).to.be.true;

          this.element.removeRelation(this.relatedElement);
          expect(this.element.isRelatedTo(this.relatedElement)).to.be.false;
        });

        it('Should account for specific types of relations', function () {
          expect(this.element.isRelatedTo(this.relatedElement, 'type-a')).to.be.true;
          expect(this.element.isRelatedTo(this.relatedElement, 'type-b')).to.be.true;
          expect(this.element.isRelatedTo(this.relatedElement, 'type-c')).to.be.false;
        });
      });

      describe('breakRelations()', function () {

        it('Should empty the list of elements relations', function () {

          addRelatedElement.apply(this);

          this.element.breakRelations();

          expect(_.keys(this.element.get('relations')).length).to.equal(0);
          expect(this.relatedElement.isRelatedTo(this.element)).to.be.false;
        });
      });

      describe('updateRelations()', function () {

        it('Should set the list of elements relations to the new list', function () {

          this.element.addRelation(this.relatedElement);
          this.element.addRelation(this.oldRelation);
          this.element.addRelation(this.anotherOldRelation);


          this.element.updateRelations([this.relatedElement, this.anotherRelatedElement]);

          expect(this.element.isRelatedTo(this.relatedElement)).to.be.true;
          expect(this.element.isRelatedTo(this.anotherRelatedElement)).to.be.true;
          expect(this.element.isRelatedTo(this.oldRelation)).to.be.false;
          expect(this.element.isRelatedTo(this.anotherOldRelation)).to.be.false;
          expect(this.anotherRelatedElement.isRelatedTo(this.element)).to.be.true;
          expect(this.relatedElement.isRelatedTo(this.element)).to.be.true;
          expect(this.oldRelation.isRelatedTo(this.element)).to.be.false;
          expect(this.anotherOldRelation.isRelatedTo(this.element)).to.be.false;
        });

        it('Should account for specific types of relations', function () {

          this.element.set('l.relationTypes', {
            '1:2:type-b': [this.relatedElement.get('l.logicalId')]
          });
          this.element.updateRelations([this.relatedElement]);
          expect(this.element.isRelatedTo(this.relatedElement, 'type-b')).to.be.true;
          expect(this.relatedElement.isRelatedTo(this.element, 'type-b')).to.be.true;
        });
      });
    });

    describe('getMetricValue()', function () {

      beforeEach(function () {
        this.metricWithValue = new Metric({
          id: 'metric-with-value'
        });

        this.metricWithNoValue = new Metric({
          id: 'metric-with-no-value'
        });

        this.element.set('metric-with-value', 10);
      });

      it('Should return the value of given metric', function () {

        var value = this.element.getMetricValue(this.metricWithValue);
        expect(value).to.equal(10);
      });

      it('Should return `undefined` if the metric has no value', function () {

        var value = this.element.getMetricValue(this.metricWithNoValue);
        expect(value).to.be.undefined;
      });
    });

    describe('Metrics history', function () {
      it('Should get updated when an element metrics get updated', function () {
        this.element.metricsHistorySize = 2;
        this.element.set('metricsValues', {'a-metric': 10, 'anotherMetric': 1});
        expect(this.element.getMetricHistory({id: 'a-metric'})).to.deep.equal([10]);
        expect(this.element.getMetricHistory({id: 'anotherMetric'})).to.deep.equal([1]);
        this.element.set('metricsValues', {'anotherMetric': 5});
        expect(this.element.getMetricHistory({id: 'anotherMetric'})).to.deep.equal([1, 5]);
        expect(this.element.getMetricHistory({id: 'a-metric'})).to.be.undefined;
        this.element.set('metricsValues', {'anotherMetric': 8});
        expect(this.element.getMetricHistory({id: 'anotherMetric'})).to.deep.equal([5, 8]);
      });
    });

    describe('getStateChangeTimeout()', function () {

      beforeEach(function () {
        this.element = new Element({
          stateChangeTimeouts: {
            'added': 1000,
            'removed': 2000,
            'updated': 3000,
            'nestedChange': 4000
          }
        });

        this.nestedElement = new Element({
          parent: this.element
        });
      });

      it('Should return the appropriate value', function () {

        expect(this.element.getStateChangeTimeout('added')).to.equal(1000);
        expect(this.element.getStateChangeTimeout('removed')).to.equal(2000);
        expect(this.element.getStateChangeTimeout('updated')).to.equal(3000);
        expect(this.element.getStateChangeTimeout('nestedChange')).to.equal(4000);
      });

      it('Should return the parent\'s values if the timeouts have not been configured for the element', function () {

        expect(this.nestedElement.getStateChangeTimeout('added')).to.equal(1000);
        expect(this.nestedElement.getStateChangeTimeout('removed')).to.equal(2000);
        expect(this.nestedElement.getStateChangeTimeout('updated')).to.equal(3000);
        expect(this.nestedElement.getStateChangeTimeout('nestedChange')).to.equal(4000);
      });
    });

    describe('isPartOf()', function () {

      beforeEach(function () {

        this.element = new Element();

        this.results = _.times(2, function () {
          return new QueryResult();
        });

        this.results = new Backbone.Collection(this.results);
      });

      it('Should return true if element is part of one of the QueryResults', function () {

        this.results.at(1).set('elements', new Backbone.Collection([this.element]));

        expect(this.element.isPartOf(this.results)).to.be.true;
      });

      it('Should return false if element is part of none of the QueryResults', function () {

        // Tests the case where the QueryResult actually has elements
        // (vs. current state where it has no elements)
        this.results.at(0).set('elements', new Backbone.Collection([new Element(), new Element()]));

        expect(this.element.isPartOf(this.results)).to.be.false;
      });
    });
    describe('getAggregator', function() {
      describe('parent check', function() {
        it ('Should return undefined if not exists', function() {
          this.element.set('parent', undefined);
          expect(this.element.getAggregator()).to.be.undefined;
        });
      });
      describe('parent tapestry check', function() {
        it ('Should return undefined if not exists', function() {
          this.element.set('parent', {get: function() { return null;}});
          expect(this.element.getAggregator()).to.be.undefined;
        });
      });
      describe('parent tapestry aggregator check', function() {
        it ('Should return undefined if not exists', function() {
          this.element.set('parent', {get: function() {return {aggregator: null};}});
          expect(this.element.getAggregator()).to.be.undefined;
        });
      });
      describe('parent tapestry aggregator', function() {
        it ('Should return the aggregator', function() {
          var aggregator = {test: 'fixture'};
          this.element.set('parent', {get: function() {return {aggregator: aggregator};}});
          expect(this.element.getAggregator()).to.equal(aggregator);
        });
      });
    });
  });
});
