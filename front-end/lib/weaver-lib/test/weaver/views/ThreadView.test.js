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
  var Query = require('weft/models/Query');
  var ItemType = require('weft/models/ItemType');
  var Thread = require('weft/models/Thread');
  var Aggregation = require('weft/models/Aggregation');
  var AggregatorClient = require('weft/services/AggregatorClient');
  var ThreadMonitorService = require('weft/services/ThreadMonitorService');
  var ProviderLegendService = require('weft/services/ProvidersLegendService');
  var ThreadView = require('weaver/views/ThreadView/ThreadView');

  describe('weaver/views/ThreadView/ThreadView.js', function () {

    // To test the ThreadView, we need
    beforeEach(function () {

      var itemType = {
        attributes: {
          name: {
            'name': 'Some name',
            'unit': 'Kb',
            'min': 0,
            'max': 10,
            plottable: true
          }
        }
      };

      // ... a Thread....
      this.thread = new Thread({
        id: 'thread',
        name: 'Thread',
        itemType: new ItemType(itemType)
      });

      // ... and some nested elements ...
      this.nestedThreads = [];
      _.times(5, function (index) {
        this.nestedThreads.push(new Aggregation({
          'l.logicalId': 'thread-' + index
        }, {
          itemType: itemType
        }));
      }, this);

      // ... and a ThreadView ...
      this.view = new ThreadView({
        model: this.thread,
        threadMonitorService: new ThreadMonitorService({
          aggregator: new AggregatorClient()
        }),
        providerLegendService: new ProviderLegendService()
      });
      this.thread.resetElements(this.nestedThreads);

      // We also need like an identified nested thread ...
      this.nestedThread = this.thread.get('elements').at(0);

      // ... with some nested elements too ...
      /*this.nestedThreadElements = [];
      _.times(5, function () {
        this.nestedThreadElements.push(new Thread());
      }, this);*/
      //this.nestedThread.resetElements(this.nestedThreadElements);

      this.view.$el.appendTo(document.body);
    });

    afterEach(function () {
      this.view.$el.remove();
    });

    it('Should display a headed and the thread elements', function () {

      expect(this.view.$el).to.have.descendants('.mas-threadHeader');
      expect(this.view.$el).to.have.descendants('.mas-elements');
    });

    describe('Reaction to reset:elements', function () {

      beforeEach(function () {

        this.view.displayNestedThread(this.nestedThread);
      });

      // Not sure it's the right place to do this :s
      // This might more be the responsibility of the ThreadViewElements
      it('Should unselect element if it has been removed', function (done) {

        var $div = $('<div>');
        $div.appendTo(document.body);
        $div.append(this.view.$el);
        $div.on('action:unselectElement', _.bind(function (event) {

          expect(event.originalEvent.thread).to.equal(this.nestedThread);
          $div.remove();
          done();
        }, this));

        this.nestedThread.set('selected', true);
        this.thread.resetElements([]);
      });

      it('Should remove nestedThread it\'s one of the elements removed', function () {

        this.thread.resetElements([]);

        expect(this.view.$el).not.to.have.descendants('.mas-thread-' + this.nestedThread.id);
      });
    });

    describe('displayNestedThread()', function () {

      it('Should display the nested thread', function (done) {

        this.view.displayNestedThread(this.nestedThread);
        this.view.$el.on('didDisplayThread', _.bind(function () {

          expect(this.view.subThread.model.get('aggregation')).to.equal(this.nestedThread);
          done();
        }, this));
      });

      it('Should dispatch a `didDisplayThread` event to notify it displayed the Thread', function (done) {


        var aggregation = this.nestedThread;
        this.view.$el.on('didDisplayThread', function (event) {

          expect(event.originalEvent.thread.get('aggregation')).to.equal(aggregation);
          done();
        });
        this.view.displayNestedThread(this.nestedThread);
      });
    });

    describe('removeNestedThread()', function () {

      // As we're testing nested thread removal, we need ...
      beforeEach(function (done) {

        // ... a nested Thread do be displayed
        this.view.$el.on('didDisplayThread', _.once(_.bind(function () {
          this.nestedView = this.view.subThread;
          done();
        }, this)));
        this.view.displayNestedThread(this.nestedThread);
      });

      it('Should remove the elements from nested Thread', function () {

        this.view.removeNestedThread();

        expect(this.nestedView.model.get('elements').size()).to.equal(0);
      });

      it('Should dispatch a `didRemoveThread` event', function (done) {

        var thread = this.nestedView.model;
        this.view.$el.on('didRemoveThread', function (event) {

          expect(event.originalEvent.thread).to.equal(thread);
          done();
        });

        this.view.removeNestedThread();
      });
    });

    describe('remove()', function () {

      beforeEach(function () {
        this.div = document.createElement('div');
        document.body.appendChild(this.div);
      });

      afterEach(function () {
        document.body.removeChild(this.div);
      });

      it('Should remove the views it delegates its rendering to', function () {

        var headerViewSpy = sinon.spy(this.view.headerView, 'remove');
        var elementsViewSpy = sinon.spy(this.view.elementsView, 'remove');
        this.view.remove();

        expect(headerViewSpy).to.have.been.called;
        expect(elementsViewSpy).to.have.been.called;
      });

      it('Should unselect currently selected elements', function (done) {

        // Say an element is selected
        var element = this.view.model.get('elements').at(2);
        element.set('selected', true);

        // We need to check that the event bubbles up the DOM
        this.view.$el.appendTo(this.div);

        $(this.div).one('action:unselectElement', function () {

          done();
        });

        this.view.remove();
      });
    });

    describe('.has-removedElements', function () {

      it('Should get the class when there are removed elements', sinon.test(function () {

        var counter = this.thread.getRemovedElementsCounter();

        counter.increment(5, 1000);

        expect(this.view.$mainThread).to.have.class('has-removedElements', 'After first increment');

        counter.reset();

        expect(this.view.$mainThread).not.to.have.class('has-removedElements', 'After reset');

        counter.increment(5, 1000);
        expect(this.view.$mainThread).to.have.class('has-removedElements', 'After second increment');

        this.clock.tick(1000);

        expect(this.view.$mainThread).not.to.have.class('has-removedElements', 'After time elapsed');
      }));
    });

    // events moved to tooltip.. come back and fix this..
    describe.skip('Events', function () {

      describe('Displaying nested Threads (`click .mas-action--view`)', function () {

        beforeEach(function () {

          this.aggregation = this.thread.get('elements').at(0);
          this.eventTarget = document.createElement('div');
          this.eventTarget.classList.add('mas-action--view');
          $(this.eventTarget).data('aggregation', this.aggregation);

          this.newAggregation = this.thread.get('elements').at(1);
          this.newTarget = document.createElement('div');
          this.newTarget.classList.add('mas-action--view');
          $(this.newTarget).data('aggregation', this.newAggregation);

          this.view.el.appendChild(this.eventTarget);
          this.view.el.appendChild(this.newTarget);
        });

        it('Should display the content of a the thread in a nested thread', function (done) {

          this.view.$el.on('didDisplayThread', _.bind(function () {
            expect(this.view.subThread.model.get('aggregation')).to.equal(this.aggregation);
            done();
          }, this));

          var event = document.createEvent('Event');
          event.initEvent('click', true, true);
          this.eventTarget.dispatchEvent(event);
        });

        it('Should close the nested thread if it is already displayed', function (done) {

          this.view.$el.on('didDisplayThread', _.bind(function () {

            var event = document.createEvent('Event');
            event.initEvent('click', true, true);
            this.eventTarget.dispatchEvent(event);

            expect(this.view.subThread).to.be.undefined;

            done();
          }, this));

          this.view.displayNestedThread(this.aggregation);
        });

        it('Should remove nested Thread when the main Thread\'s Query changes', function () {

          this.thread.set('query', new Query());

          expect(this.view.$el).not.to.have.descendants('.mas-threadView-nested');
        });

        it('Should replace nested thread when a thread is already displayed', function (done) {

          this.view.$el.one('didDisplayThread', _.bind(function () {
            expect(this.view.subThread.model.get('aggregation')).to.equal(this.aggregation);

            this.view.$el.one('didDisplayThread', _.bind(function () {
              expect(this.view.subThread.model.get('aggregation')).to.equal(this.newAggregation);
              done();

            }, this));

            var event = document.createEvent('Event');
            event.initEvent('click', true, true);
            this.newTarget.dispatchEvent(event);

          }, this));

          this.view.displayNestedThread(this.aggregation);
        });
      });
    });     

    describe('Content update notification', function () {

      it('Should change state when Thread query changes content will be updated', sinon.test(function () {

        this.thread.set('query', new Query());

        expect(this.view.$el).to.have.class('mas-threadView-beingUpdated');

        this.thread.refreshResult();
        this.requests[0].respond(200, {'Content-Type': 'application/json'}, JSON.stringify({}));

        expect(this.view.$el).not.to.have.class('mas-threadView-beingUpdated');

      }));
    });

    // if (!window.PHANTOMJS) {
    //   describe('hasElementsOrMetricsCoveredBy()', function () {

    //     it('Should return whether an element of given dimensions overlaps the Thread elements', function () {

    //       // Depends on CSS to be loaded :s
    //       // TODO: Make it independent from that :)
    //       var elementsDimensions = this.view.elementsView.el.getBoundingClientRect();

    //       var overlapingDimensions = {
    //         top: elementsDimensions.top + elementsDimensions.height / 4,
    //         bottom: elementsDimensions.bottom - elementsDimensions.height / 4
    //       };

    //       var nonOverlapingDimensions = {
    //         top: elementsDimensions.top - elementsDimensions.height / 4,
    //         bottom: elementsDimensions.top - elementsDimensions.height / 5
    //       };

    //       expect(this.view.hasElementsOrMetricsCoveredBy(overlapingDimensions)).to.be.true;
    //       expect(this.view.hasElementsOrMetricsCoveredBy(nonOverlapingDimensions)).to.be.false;
    //     });
    //   });
    // }
  });
});
