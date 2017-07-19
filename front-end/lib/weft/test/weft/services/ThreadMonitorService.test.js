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

  var Backbone = require('backbone');
  var QueryResult = require('weft/models/QueryResult');
  var ThreadMonitorService = require('weft/services/ThreadMonitorService');

  describe('weft/services/ThreadMonitorService.js', function () {

    describe('polling', function () {

      beforeEach(function () {

        this.queryResults = new Backbone.Collection([
          // A simple thread
          new QueryResult(),
          new QueryResult(),
          new QueryResult(),
          new QueryResult()
        ]);

        this.threadMonitor = new ThreadMonitorService({
          pollingInterval: 30000,
          queryResults: this.queryResults,
        });

        this.queryResult = new QueryResult({
          id: 'thread'
        });
      });

      describe('poll', function () {

        it('Should prevent scheduled polling if false', sinon.test(function () {

          this.threadMonitor.set('poll', false);
          this.threadMonitor.schedulePoll(this.queryResult);
          this.clock.tick(this.threadMonitor.get('pollingInterval'));
          expect(this.requests).to.have.length(0);
        }));

        it('Should resume polling when set to true', sinon.test(function () {

          this.threadMonitor.set('poll', false);
          this.threadMonitor.set('poll', true);
          expect(this.requests).to.have.length(this.threadMonitor.get('queryResults').size());
        }));
      });

      describe('pollingInterval', function () {

        // Not really a feature, but this will check updates to pollingInterval
        // for testing purposed (to prevent polling) will get noticed
        it('Should have a reasonable default value', function () {

          expect(new ThreadMonitorService().get('pollingInterval')).to.be.at.most(30000);
        });
      });

      describe('poll()', function () {

        beforeEach(function () {

          this.expectedRequestBody = {
            query: {},
            relationsWith: [{
              // Threads get identified by the ID sent back in the response
              // to the last query they sent. If a thread doesn't have such info
              // it will be omited from this list, hence only 3 objects here
              // while there are 4 threads monitored
              id: 'idOfThreadQueryResult',
              _meta: {
                type: 'thread'
              }
            }, {
              id: 'idOfClusterQueryResult',
              _meta: {
                type: 'cluster'
              }
            }, {
              id: 'idOfGroupQueryResult',
              _meta: {
                type: 'group'
              }
            }]
          };
        });

        it('Should query the aggregator for the thread\'s content', sinon.test(function () {

          var spy = sinon.spy(this.queryResult, 'refresh');

          this.threadMonitor.poll(this.queryResult);

          expect(spy).to.have.been.called;

        }));

        it('Should query thread at given index in the list of monitored thread', sinon.test(function () {

          var spy = sinon.spy(this.queryResults.at(2), 'refresh');

          var index = 2;
          this.threadMonitor.poll(index);

          expect(spy).to.have.been.called;
        }));

        it('Should schedule a poll after initial poll', sinon.test(function () {

          this.threadMonitor.poll(this.queryResult);
          this.requests[0].respond(200, {
            'Content-Type': 'application/json'
          }, '{}');
          this.clock.tick(this.threadMonitor.get('pollingInterval'));
          expect(this.requests).to.have.length(2);
        }));

        it('Should reset existing polling interval', sinon.test(function () {

          // Say we poll and receive a response
          this.threadMonitor.poll(this.queryResult);
          this.requests[0].respond(200, {
            'Content-Type': 'application/json'
          }, '{}');

          // The poll again a bit later, before scheduled poll is triggered
          this.clock.tick(this.threadMonitor.get('pollingInterval') / 2);
          this.threadMonitor.poll(this.queryResult);
          this.requests[1].respond(200, {
            'Content-Type': 'application/json'
          }, '{}');

          // No request should have been sent when the initial polling interval is over
          this.clock.tick(this.threadMonitor.get('pollingInterval') / 2);
          expect(this.requests).to.have.length(2);

          // Once we reach the end of new polling interval, a new request should have been sent
          this.clock.tick(this.threadMonitor.get('pollingInterval') / 2);
          expect(this.requests).to.have.length(3);

        }));

        it('Should cancel pending poll', sinon.test(function () {

          this.threadMonitor.poll(this.queryResult);
          this.threadMonitor.poll(this.queryResult);

          expect(this.requests[0].aborted).to.be.true;
        }));
      });

      describe('schedulePoll()', function () {

        it('Should schedule a poll after configured polling interval', sinon.test(function () {

          this.threadMonitor.schedulePoll(this.queryResult);
          this.clock.tick(this.threadMonitor.get('pollingInterval'));
          expect(this.requests).to.have.length(1);
        }));

        it('Should cancel currently scheduled poll', sinon.test(function () {

          this.threadMonitor.schedulePoll(this.queryResult);

          // Move up a bit in time, not long enough to trigger the poll
          // and schedule a new poll
          this.clock.tick(this.threadMonitor.get('pollingInterval') / 2);
          this.threadMonitor.schedulePoll(this.queryResult);

          // Move up in time to the point first poll should have happened
          this.clock.tick(this.threadMonitor.get('pollingInterval') / 2);
          expect(this.requests).to.have.length(0);

          // And move up to the point new poll should happen
          this.clock.tick(this.threadMonitor.get('pollingInterval') / 2);
          expect(this.requests).to.have.length(1);
        }));

        // TODO: Make a specific, testable function?
        describe('scheduled polls', function () {

          it('Should reschedule poll if previous polling still has not received a response', sinon.test(function () {

            this.threadMonitor.poll(this.queryResult);

            // Despite having reached the point a new request should have been sent
            // noting is sent as the response to previous request has not been received
            this.clock.tick(this.threadMonitor.get('pollingInterval'));
            expect(this.requests).to.have.length(1);

            // Now the request has been received, next poll triggers a new request
            this.requests[0].respond(200, {
              'Content-Type': 'application/json'
            }, '{}');
            this.clock.tick(this.threadMonitor.get('pollingInterval'));
            expect(this.requests).to.have.length(2);
          }));

          it('Should execute even if previous polling failed', sinon.test(function () {
            this.threadMonitor.poll(this.queryResult);
            this.requests[0].respond(404, {
              'Content-Type': 'application/json'
            }, '{}');
            this.clock.tick(this.threadMonitor.get('pollingInterval'));
            expect(this.requests).to.have.length(2);
          }));
        });
      });

      describe('cancelScheduledPoll()', function () {

        it('Should cancel currently scheduled poll', sinon.test(function () {

          this.threadMonitor.schedulePoll(this.queryResult);
          this.threadMonitor.cancelScheduledPoll(this.queryResult);
          this.clock.tick(this.threadMonitor.get('pollingInterval'));
          expect(this.requests).to.have.length(0);
        }));
      });

      describe('threads', function () {


        describe('remove()', function () {

          it('Should cancel polling and abort pending request', sinon.test(function () {

            this.threadMonitor.get('queryResults').add(this.queryResult);
            this.queryResult.set('queryable', true);
            var pendingRequest = this.requests[0];

            this.threadMonitor.get('queryResults').remove(this.queryResult);
            expect(pendingRequest.aborted).to.be.true;
            this.clock.tick(this.threadMonitor.get('pollingInterval'));
            expect(this.requests).to.have.length(1);
          }));
        });

        describe('Model events', function () {

          beforeEach(function () {

            this.threadToUpdate = this.threadMonitor.get('queryResults').first();
          });

          it('Should trigger a poll when `queryable` changes', sinon.test(function () {

            this.threadToUpdate.set('queryable', true);
            expect(this.requests).to.have.length(1);
          }));
        });
      });
    });
  });
});
