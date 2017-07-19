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
  var Query = require('weft/models/Query');
  var Thread = require('weft/models/Thread');
  var Pattern = require('weft/models/Pattern');
  var Tapestry = require('weft/models/Tapestry');
  var AggregatorClient = require('weft/services/AggregatorClient');

  describe('weft/models/Tapestry.js', function () {

    beforeEach(function () {

      this.tapestry = new Tapestry([], {
        aggregator: new AggregatorClient()
      });
      this.tapestry.attachAutoSyncOnEvents();

      sinon.spy(this.tapestry.aggregator, 'syncTapestry');
    });

    describe('load()', function () {

      it('Should add the Threads from the pattern', sinon.test(function () {

        var threads = _.times(3, function (id) {
          return new Thread({
            query: {
              inputs: ['/da/' + id]
            }
          }, {parse: true});
        });

        var pattern = new Pattern({
          threads: threads
        });

        this.tapestry.load(pattern);
        this.tapestry.get('threads').forEach(function (thread, index) {
          expect(thread.isSameAs(threads[index])).to.be.true;
        });
      }));
    });

    describe('add()', function () {

      it('Should assign an ID to the Threads being added', sinon.test(function () {

        var threads = _.times(3, function (id) {
          return new Thread({
            aggregation: {
              id: 'a' + id
            }
          });
        });

        this.tapestry.add(threads[0]);
        this.tapestry.add([threads[1], threads[2]]);

        this.tapestry.get('threads').forEach(function (thread, index) {

          expect(thread.id).not.to.be.undefined;
          expect(thread.get('aggregation').id).to.equal('a' + index);
        });
      }));
    });

    describe('save()', function () {

      it('Should use the aggregator attached to the Tapestry', sinon.test(function () {

        this.tapestry.save();
        expect(this.tapestry.aggregator.syncTapestry).to.have.been.calledWith(this.tapestry);
      }));

      it('Should mark Threads as queryable once the Tapestry has been synced', sinon.test(function () {

        var thread = new Thread();
        this.tapestry.add(thread).save();

        expect(thread.get('result').get('queryable')).to.be.false;

        this.requests[0].respond(200, {'Content-Type': 'application/json'}, JSON.stringify({id: '1'}));

        expect(thread.get('result').get('queryable')).to.be.true;
      }));
    });

    describe('toJSON()', function () {

      it('Should include an empty list of Threads if the Tapestry has no Threads', function () {
        var tapestry = new Tapestry();
        expect(tapestry.toJSON()).to.deep.equal({
          threads: []
        });
      });

      it('Should include the ID of the tapestry if it has one', function () {

        var tapestry = new Tapestry({
          id: 'id'
        });

        expect(tapestry.toJSON()).to.deep.equal({
          threads: [],
          id: 'id'
        });
      });

      it('Should encode each Thread in the Tapestry', sinon.test(function () {

        var tapestry = new Tapestry();
        var threads = _.times(3, function () {
          return new Thread();
        });

        tapestry.add(threads);

        var spy = this.spy(Thread.prototype, 'toJSON');

        // Delegate the exact encoding testing to the Thread
        expect(tapestry.toJSON().threads).to.have.length(3);
        expect(spy).to.have.been.calledThrice;
      }));
    });

    describe('Automatic sync with server', function () {

      it('Should get automatically synced when a Thread gets added to the Tapestry', sinon.test(function () {

        var thread = new Thread();

        this.tapestry.add(thread);

        this.clock.tick();

        expect(this.tapestry.aggregator.syncTapestry).to.have.been.calledWith(this.tapestry);
      }));

      it('Should get automatically synced when a Thread gets removed from the Tapestry', sinon.test(function () {

        var thread = new Thread();

        this.tapestry.add(thread);

        this.clock.tick();

        this.tapestry.remove(thread);

        this.clock.tick();

        expect(this.tapestry.aggregator.syncTapestry).to.have.been.calledTwice;
      }));

      it('Should get automatically synced when Thread Query changes', sinon.test(function () {

        var thread = new Thread();

        this.tapestry.add(thread);

        this.clock.tick();

        thread.set('query', new Query());

        this.clock.tick();

        expect(this.tapestry.aggregator.syncTapestry).to.have.been.calledTwice;
      }));

      it('Should only sync once when multiple threads are added synchronously', sinon.test(function () {

        this.tapestry.add(new Thread());
        this.tapestry.add(new Thread());

        this.clock.tick();

        expect(this.tapestry.aggregator.syncTapestry).to.have.been.calledOnce;
      }));
    });
  });
});
