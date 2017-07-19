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
/* global describe, it, sinon, expect, beforeEach, afterEach */
/* jshint expr: true */
define(function (require) {

  "use strict";

  var $ = require('jquery');
  var _ = require('lodash');
  var Metric = require('weft/models/Metric');
  var Item = require('weft/models/Item');
  var Thread = require('weft/models/Thread');
  var ThreadViewElements = require('weaver/views/ThreadView/ThreadViewElements');
  var ElementView = require('weaver/views/Element/ElementView');
  var MetricValueView = require('weaver/views/MetricValueView');


  // Helper functions
  /**
   * Finds the DOM element corresponding to given element
   * @method findCorrespondingDOMElement
   * @param element {Element}
   * @param $DOMElements {Array}
   * @return {jQuery Object}
   */
  function findCorredpondingDOMElement(element, $DOMElements) {
    var result = $DOMElements.filter('.mas-element-' + element.cid);
    return result;
  }

  describe('weaver/views/ThreadViewElements.js', function () {

    // To test the ThreadViewElements we need
    beforeEach(function () {

      // ... a Thread...
      this.thread = new Thread({
        id: 'thread',
        name: 'thread',
        itemMetrics: {},
        stateChangeTimeouts: {
          'added': 4000
        }
      });

      // ... a few elements that will get in the thread ...
      this.elementIDs = ['a1', 'a2', 'a3', 'b1', 'b2', 'b3', 'c1', 'd1', 'd2', 'e1', 'f1'];
      var elements = this.elements = {};
      _.forEach(this.elementIDs, function (elementID) {
        elements[elementID] = new Item({
          'l.logicalId': elementID,
          name: elementID,
          metricsValues: {
            'a-metric': 19
          }
        },{
          itemType: {
            attributes: {
              'name': {
                type: 'literal'
              }
            }
          }
        });
      });

      // ... and a ThreadViewElements...
      this.view = new ThreadViewElements({
        model: this.thread
      });

      // ... and a few elements in the Thread ...
      this.initialElements = [
        this.elements.a1,
        this.elements.a2,
        this.elements.a3,
        this.elements.b1,
        this.elements.b2,
        this.elements.c1,
        this.elements.d1,
        this.elements.d2
      ];

      this.updatedElements = [
        this.elements.a2,
        this.elements.a1, // Inverted position to check the elements will actually be displayed at a new position
        this.elements.b1,
        this.elements.b2,
        this.elements.b3, // New `b3` element to check elements get added
        // `c1` element isn't there anymore
        this.elements.d1,
        this.elements.d2,
        this.elements.e1, // Additional new elements
        this.elements.f1
      ];

      document.body.appendChild(this.view.el);
    });

    afterEach(function () {
      try {
        this.view.remove();
      } catch (e) {
        console.log('After each', e);
        throw e;
      }
    });

    describe('options.metric', function () {

      beforeEach(function (done) {

        this.metric = new Metric({
          id: 'a-metric',
          name: 'A metric',
          unit: 'Mb',
          min: 0,
          max: 20
        });

        this.viewWithMetrics = new ThreadViewElements({
          model: this.thread,
          metric: this.metric
        });

        this.viewWithMetrics.$el.one('didRender', _.bind(function () {
          done();
        }, this));

        this.thread.resetElements(this.initialElements);
      });


      it('Should make the view display MetricValueViews', function displayMetricValueViews() {

        var firstElement = _.first(_.values(this.viewWithMetrics.elementViews));
        expect(firstElement).to.be.an.instanceof(MetricValueView);
        expect(firstElement.options.metric).to.equal(this.metric);
      });

      it('Should not optimize label placement when diplaying metrics', function doesntOptimizeLabels() {

        var view = new ThreadViewElements({
          model: this.thread,
          metric: this.metric
        });

        var spy = sinon.spy(view.defaultLabellingStrategy, 'updateLabels');

        this.thread.resetElements(this.initialElements);

        expect(spy).not.to.have.been.called;
      });
    });

    describe('Initial rendering', function () {

      it('Shoud render the Thread elements', function (done) {

        this.thread.resetElements(this.initialElements);

        this.view = new ThreadViewElements({
          model: this.thread
        });

        this.view.$el.one('didRender', _.bind(function () {

          var $elements = this.view.$el.find('.mas-element');

          this.thread.get('elements').each(function (element, index) {

            expect($elements.eq(index)).to.have.class('mas-element-' + element.cid);
          });

          done();
        }, this));
      });
    });

    describe('Reaction to model\'s reset:elements', function () {

      beforeEach(function (done) {

        this.view.$el.one('didRender', _.bind(function () {

          this.$initialDOMElements = this.view.$el.find('.mas-element');
          done();
        }, this));

        this.thread.resetElements(this.initialElements);
      });

      it('Should display the elements in the thread', function displayElements() {

        var $elements = this.$initialDOMElements;
        expect($elements.length).to.equal(this.thread.get('elements').size());
        this.thread.get('elements').each(function (element, index) {

          expect($elements.eq(index)).to.have.class('mas-element-' + element.cid);
        });
      });

      it('Should remove elements that are no longer in the Thread', function removeOldElements() {


        this.thread.resetElements(this.updatedElements);
        this.$updatedDOMElements = this.view.$el.find('.mas-element');

        var removedElements = _.difference(this.initialElements, this.updatedElements);

        _.forEach(removedElements, _.bind(function (element) {

          var $initialDOMElement = findCorredpondingDOMElement(element, this.$initialDOMElements);
          expect(this.$updatedDOMElements).not.to.contain($initialDOMElement);
        }, this));
      });

      // # Skipped when run in
      // PhantomJS. PhantomJS implementation only contains legacy `-webkit-ordinal-group`
      // to set flexbox ordering :(
      if (!window.PHANTOMJS) {

        it('Should display the updated elements, in the order they appear in the Thread', function displayUpdatedElements(done) {

          // Account for the animations happening when rendering the elements
          this.slow(1100);

          this.view.el.addEventListener('didRender', _.bind(function a() {
            this.$updatedDOMElements = this.view.$el.find('.mas-element');

            _.forEach(this.updatedElements, function (element, index) {

              var $updatedDOMElement = findCorredpondingDOMElement(element, this.$updatedDOMElements);
              expect(parseInt($updatedDOMElement.css('order') || $updatedDOMElement.css('webkitOrder') || $updatedDOMElement.css('webkitBoxOrdinalGroup'))).to.equal(index);
            }, this);

            done();
          }, this));


          this.thread.resetElements(this.updatedElements);

        });

        it('Should keep DOM element for elements that are still there', function keepDOMElements(done) {

          // Account for the animations happening when rendering the elements
          this.slow(1100);

          this.view.el.addEventListener('didRender', _.bind(function () {

            var $updatedDOMElements = this.view.$el.find('.mas-element');
            expect($updatedDOMElements.length).to.equal(9);

            _.forEach(this.updatedElements, _.bind(function (element) {

              if (this.initialElements.indexOf(element) !== -1) {

                var $initialDOMElement = findCorredpondingDOMElement(element, this.$initialDOMElements);
                var $updatedDOMElement = findCorredpondingDOMElement(element, $updatedDOMElements);
                expect($initialDOMElement[0]).to.equal($updatedDOMElement[0]);
              }
            }, this));

            done();
          }, this));

          this.thread.resetElements(this.updatedElements);
        });

      }

      it('Should display an empty indicator if thread has no element after it has received the result to a query', function emptyIndicator(done) {


        this.view.$el.one('didRender', _.bind(function () {

          var $updatedDOMElements = this.view.$el.find('.mas-elements--emptyMessage');

          expect($updatedDOMElements.length).to.equal(1);
          expect($updatedDOMElements).to.have.class('mas-emptyMessage');
          expect($updatedDOMElements.find('.mas-emptyMessage--message')).to.contain('This thread has no elements to display');

          this.view.$el.one('didRender', _.bind(function () {

            $updatedDOMElements = this.view.$el.find('.mas-element');

            expect($updatedDOMElements.length).to.equal(9);
            expect(this.view.$el.find('.mas-elements--emptyMessage')).not.to.have.descendants('.mas-emptyMessage');
            done();
          }, this));

          this.thread.resetElements(this.updatedElements);
        }, this));

        this.thread.set('resultId', new Date().getTime());
        this.thread.resetElements();
      });

      it('Should highlight new elements for the duration configured in the Thread', sinon.test(function () {

        this.thread.resetElements(this.updatedElements);

        var $newElements = this.view.$('.mas-element-b3, .mas-element-e1, .mas-element-f1');
        $newElements.each(function (index, element) {
          expect($(element)).to.have.class('mas-element-added');
        });

        var duration = this.thread.getStateChangeTimeout('added');
        this.clock.tick(duration);

        $newElements.each(function (index, element) {
          expect($(element)).not.to.have.class('mas-element-added');
        });
      }));
    });

    // deprecate?
    describe.skip('.has-selection', function () {

      beforeEach(function (done) {

        this.view.$el.one('didRender', function () {
          done();
        });
        this.thread.resetElements(this.initialElements);
      });

      it('Should get the class when the Thread contains selected elements', function () {

        var firstElementView = this.view.$('.mas-element').eq(0).data('view');

        expect(this.view.$el).not.to.have.class('has-selection');

        firstElementView.selectElement();

        expect(this.view.$el).to.have.class('has-selection');

        firstElementView.unselectElement();

        expect(this.view.$el).not.to.have.class('has-selection');
      });
    });

    describe('remove()', function () {

      it('Should stop the view from listening to model events', function () {

        this.view.remove();

        var spy = sinon.spy(this.view, '_renderElements');

        this.thread.resetElements(this.initialElements);

        expect(spy).not.to.have.been.called;
      });

      it('Should remove elements displayed in the view', function (done) {

        var spy = sinon.spy(ElementView.prototype, 'remove');

        this.view.el.addEventListener('didRender', _.bind(function () {

          this.view.remove();

          var elements = this.view.$('.mas-element');

          expect(elements.length).to.equal(0); // Yeah jQuery kind of array that doesn't play well with chai :(
          expect(spy.args).to.have.length(8);

          spy.restore();
          done();
        }, this));

        this.thread.resetElements(this.initialElements);
      });
    });
  });


});
