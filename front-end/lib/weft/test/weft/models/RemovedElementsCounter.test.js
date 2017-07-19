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
/* global describe, it, sinon, expect, beforeEach */
/* jshint expr: true */
define(function (require) {

  'use strict';

  var _ = require('lodash');
  var Aggregation = require('weft/models/Aggregation');
  var Thread = require('weft/models/Thread');
  var Query = require('weft/models/Query');
  var RemovedElementsCounter = require('weft/models/RemovedElementsCounter');

  describe('weft/models/RemovedElementsCounter.js', function () {

    beforeEach(function () {

      this.elements = _.times(4, function (index) {
        return new Aggregation({
          id: 'thread-' + index
        });
      });

      this.thread = new Thread({
        stateChangeTimeouts: {
          deleted: 3000
        }
      });

      this.thread.resetElements(this.elements);

      this.counter = new RemovedElementsCounter({
        thread: this.thread
      });
    });

    describe('Model events', function () {

      it('Should get updated elements get removed from the model', sinon.test(function () {

        var spy = this.spy(this.counter, 'increment');
        this.thread.resetElements([this.elements[1]]);

        expect(spy.args[0][0]).to.equal(3); // 3 items were removed
        expect(spy.args[0][1]).to.equal(this.thread.getStateChangeTimeout('deleted'));
      }));

      // IMPROVE: Make it "when the query changes"?
      it('Should get reset when grouping changed', sinon.test(function () {

        var spy = this.spy(this.counter, 'reset');

        this.thread.set('query', new Query());

        expect(spy).to.have.been.called;
      }));

      it('Should ignore the first update after thread got grouped or sorted', sinon.test(function () {

        var spy = this.spy(this.counter, 'increment');
        this.thread.set('query', new Query());

        this.thread.resetElements(_.without(this.elements, this.elements[0]));

        expect(spy).not.to.have.been.called;

        this.thread.resetElements(_.without(this.elements, this.elements[3]));

        expect(spy.args[0][0]).to.equal(1);
        expect(spy.args[0][1]).to.equal(this.thread.getStateChangeTimeout('deleted'));
      }));
    });

    describe('increment()', function () {

      it('Should display provided count during provided time', sinon.test(function () {

        var duration = 5000;

        this.counter.increment(5, duration);

        expect(this.counter.get('numberOfRemovedElements')).to.equal(5);

        this.clock.tick(duration / 2);

        this.counter.increment(3, duration);

        expect(this.counter.get('numberOfRemovedElements')).to.equal(8);

        this.clock.tick(duration / 2);

        expect(this.counter.get('numberOfRemovedElements')).to.equal(3);

        this.clock.tick(duration / 2);

        expect(this.counter.get('numberOfRemovedElements')).to.equal(0);
      }));
    });

    describe('reset()', function () {

      it('Should reset the count', sinon.test(function () {

        var duration = 5000;

        this.counter.increment(5, 5000);

        this.clock.tick(duration / 2);

        this.counter.reset();

        expect(this.counter.get('numberOfRemovedElements')).to.equal(0);

        // Check that the number of element does not get decreased anymore
        // after initial duration has elapsed
        this.clock.tick(duration / 2);

        expect(this.counter.get('numberOfRemovedElements')).to.equal(0);
      }));
    });
  });
});