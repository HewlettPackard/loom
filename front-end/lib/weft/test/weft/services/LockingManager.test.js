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
  var Q = require('q');
  var Backbone = require('backbone');
  var Provider = require('weft/models/Provider');
  var QueryResult = require('weft/models/QueryResult');
  var AggregatorClient = require('weft/services/AggregatorClient');
  var ThreadMonitorService = require('weft/services/ThreadMonitorService');
  var LockingManager = require('weft/services/LockingManager');

  describe('weft/services/LockingManager.js', function () {

    before(function () {

      this.providers = _.times(3, function (index) {
        return new Provider(index);
      });

      this.queryResults = _.times(3, function (index) {
        return new QueryResult(index);
      });

      this.aggregator = new AggregatorClient();
      this.aggregator.get('providers').set(this.providers);

      this.tapestry = this.aggregator.getEmptyTapestry();
      this.deferred = Q.defer();
      sinon.stub(this.tapestry, 'save').returns(this.deferred.promise);

      this.threadMonitorService = new ThreadMonitorService({
        'queryResults': new Backbone.Collection(this.queryResults)
      });

      this.refreshStub = sinon.stub(QueryResult.prototype, 'refresh').returns({
        always: function () {},
        abort: function () {}
      });

      this.service = new LockingManager({
        aggregator: this.aggregator,
        threadMonitor: this.threadMonitorService,
        tapestry: this.tapestry
      });
    });

    after(function () {

      this.refreshStub.restore();
    });

    it('Should stop polling when providers get locked', sinon.test(function () {
      this.providers[0].lock();

      this.clock.tick(this.threadMonitorService.get('pollingInterval'));

      expect(this.refreshStub).not.to.have.been.called;
    }));

    it('Should re-sync tapestry and resume polling after providers get unlocked', function (done) {

      sinon.test(function () {

        this.providers[0].unlock();
        expect(this.tapestry.save).to.have.been.called;
        this.deferred.promise.then(_.bind(function () {
          expect(this.refreshStub).to.have.been.calledThrice;
          done();
        }, this)).done();
        this.deferred.resolve();

      }).apply(this);
    });

    it('Should lock all logged in providers when the client loses the session with the Loom server', sinon.test(function () {

      this.providers[0].set('loggedIn', true);
      this.providers[1].set('loggedIn', true);

      this.aggregator.trigger('loomSessionLost');

      expect(this.providers[0].get('locked')).to.be.true;
      expect(this.providers[1].get('locked')).to.be.true;
      expect(this.providers[2].get('locked')).not.to.be.true;
    }));

    it('Should re-create the Tapestry when the client re-authenticates after the session is lost', sinon.test(function () {

      this.tapestry.save.restore();

      this.providers[0].unlock();
      this.providers[1].unlock();

      expect(this.requests[0].method).to.equal('POST');
    }));
  });
});
