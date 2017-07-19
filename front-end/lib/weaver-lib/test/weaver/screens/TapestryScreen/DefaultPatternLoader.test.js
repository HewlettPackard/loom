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
define(function have(require) {

  "use strict";

  var _ = require('lodash');
  var Backbone = require('backbone');
  var Q = require('q');
  var Provider = require('weft/models/Provider');
  var Pattern = require('weft/models/Pattern');
  var Thread = require('weft/models/Thread');
  var DefaultPatternLoader = require('weaver/screens/TapestryScreen/DefaultPatternLoader');
  var BraidingController = require('weft/models/Tapestry/BraidingController');

  describe('weaver/screens/TapestryScreen/DefaultPatternLoader.js', function () {

    beforeEach(function () {

      var threads = this.everyThreads = _.times(3, function (index) {

        return new Thread({
          name: 'Thread ' + index,
          itemType: {
            id: 'it_' + index
          }
        });
      });

      var pattern = this.pattern =  new Pattern({
        name: 'Pattern',
        threads: [threads[0], threads[1]],
        defaultPattern: true
      });

      this.provider = new Provider({
        patterns: [pattern]
      });

      this.threads = new Backbone.Collection();

      this.loader = new DefaultPatternLoader(new Backbone.Collection([this.provider]), this.threads, new BraidingController());
    });

    // Firefox fails those too for a timeout reason oO. IE and Chrome are fine.
    it('Should add Threads to managed list when users confirm the prompt', function (done) {

        this.threads.on('add', _.after(2, function () {

            expect(this.threads.pluck('name')).to.deep.equal(['Thread 0', 'Thread 1']);
            done();
        }), this);

        var deferred = Q.defer();
        deferred.resolve(true);
        sinon.stub(this.loader, '_promptConfirmation').returns(deferred.promise);

        this.provider.set('loggedIn', true);
    });

    it('Should only add Threads that are not in the list already', function (done) {

        this.threads.add([this.everyThreads[1]]);

        this.threads.once('add', function (thread) {

          expect(thread.get('name')).to.equal('Thread 0');
          done();
        }, this);

        var deferred = Q.defer();
        deferred.resolve(true);
        sinon.stub(this.loader, '_promptConfirmation').returns(deferred.promise);

        this.provider.set('loggedIn', true);
    });

    it('Should only prompt if Threads are not part of the managed list already', function () {

      this.threads.add([this.everyThreads[0], this.everyThreads[1]]);

      var deferred = Q.defer();
      deferred.resolve(true);
      sinon.stub(this.loader, '_promptConfirmation').returns(deferred.promise);

      this.provider.set('loggedIn', true);

      expect(this.loader._promptConfirmation).not.to.have.been.called;
    });

    it('Should list the Threads that will be added in the prompt', function () {

      this.threads.add[this.everyThreads[1]];

      var deferred = Q.defer();
      deferred.resolve(true);
      sinon.stub(this.loader, '_promptConfirmation').returns(deferred.promise);

      this.provider.set('loggedIn', true);

      expect(this.loader._promptConfirmation.args[0][0]).to.contain('Thread 0');
    });

    it('Should not prompt if the provider does not have a default pattern', function () {

      this.pattern.set('defaultPattern', false);

      var deferred = Q.defer();
      deferred.resolve(true);
      sinon.stub(this.loader, '_promptConfirmation').returns(deferred.promise);

      this.provider.set('loggedIn', true);

      expect(this.loader._promptConfirmation).not.to.have.been.called;
    });
  });
});
