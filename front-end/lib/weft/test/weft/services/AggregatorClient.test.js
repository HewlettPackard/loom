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

  'use strict';

  var _ = require('lodash');

  var Cookies = require('cookies-js');
  var Tapestry = require('weft/models/Tapestry');
  var Pattern = require('weft/models/Pattern');
  var Provider = require('weft/models/Provider');
  var AggregatorClient = require('weft/services/AggregatorClient');

  describe('weft/services/AggregatorClient.js', function () {

    beforeEach(function () {

      this.client = new AggregatorClient({
        url: 'http://somewhere.in.the.cloud',
      });
    });

    describe('login/logout', function () {

      beforeEach(sinon.test(function () {

        // Let's say the client
        this.providers = [new Provider({
          aggregator: this.client
        }), new Provider({
          aggregator: this.client
        })];
        this.client.get('providers').set(this.providers);

        this.providers[0].login();
        this.providers[1].login();

        this.requests[0].respond(200);
        this.requests[1].respond(200);
      }));

      describe('loggedIn', function () {

        it('Should get set when a provider logs in successfully', sinon.test(function () {

          expect(this.providers[0].get('loggedIn')).to.be.true;
          expect(this.client.get('loggedIn')).to.be.true;
        }));

        it('Should get unset when all providers have logged out', sinon.test(function () {

          this.providers[0].logout();
          this.requests[0].respond(200);
          expect(this.providers[0].get('loggedIn')).to.be.false;
          expect(this.client.get('loggedIn')).to.be.true;

          this.providers[1].logout();
          this.requests[1].respond(200);
          expect(this.providers[0].get('loggedIn')).to.be.false;
          expect(this.client.get('loggedIn')).to.be.false;
        }));
      });

      describe('expireAuthenticationCookie()', function () {

        it ('Should expire the authentication cookie', function () {

          Cookies.set('loom', 'some-value');

          this.client.expireAuthenticationCookie();

          expect(Cookies.get('loom')).to.be.undefined;
        });
      });

      describe('logout()', function () {

        it('Should send a request to the aggregator to logout', sinon.test(function () {

          this.client.logout();
          expect(this.requests[0].url).to.equal('http://somewhere.in.the.cloud/providers?operation=logout');
          expect(this.requests[0].method).to.equal('POST');
        }));

        it('Should unset `loggedIn`', sinon.test(function () {

          this.client.logout();
          this.requests[0].respond(200);
          expect(this.client.get('loggedIn')).to.be.false;
        }));

        it('Should logout any provider already logged in to', sinon.test(function () {

          this.client.logout();

          expect(this.providers[0].get('loggedIn')).to.be.false;
          expect(this.providers[1].get('loggedIn')).to.be.false;
          expect(this.requests).to.have.length(1, 'Only one request should have been sent');
        }));
      });
    });

    describe('availablePatterns', function () {

      beforeEach(function () {

        var patterns = this.patterns = _.times(4, function (index) {
          return new Pattern({
            id: index
          });
        });

        var providers = this.providers =  [
          new Provider({
            patterns: [patterns[0], patterns[1]]
          }),
          new Provider({
            patterns: [patterns[1], patterns[2]]
          }),
          new Provider({
            patterns: [patterns[3]]
          })
        ];

        this.client.get('providers').set(providers);

      });

      it('Should contain the list of Patterns after login to a Provider', function () {
        this.providers[0].set('loggedIn', true);

        expect(this.client.get('availablePatterns').pluck('id')).to.deep.equal([0, 1]);
      });

      it('Should not duplicate patterns when two providers offer the same Patterns', function () {

        this.providers[0].set('loggedIn', true);
        this.providers[1].set('loggedIn', true);

        expect(this.client.get('availablePatterns').pluck('id')).to.deep.equal([0, 1, 2]);

      });

      it('Should no longer contain Pattern when user logs out of all Providers offering it', function () {

        this.providers[0].set('loggedIn', true);
        this.providers[1].set('loggedIn', true);
        this.providers[2].set('loggedIn', true);

        expect(this.client.get('availablePatterns').pluck('id')).to.deep.equal([0, 1, 2, 3]);

        this.providers[2].set('loggedIn', false);

        expect(this.client.get('availablePatterns').pluck('id')).to.deep.equal([0, 1, 2], 'Should have removed 3rd provider pattern');

        this.providers[0].set('loggedIn', false);

        expect(this.client.get('availablePatterns').pluck('id')).to.deep.equal([1, 2], 'Should have removed 1st provider pattern');

        this.providers[1].set('loggedIn', false);

        expect(this.client.get('availablePatterns').pluck('id')).to.be.empty;
      });
    });

    describe('Providers', function () {

      describe('providers', function () {

        beforeEach(function () {

          this.providers  = [new Provider({
            aggregator: this.client
          }), new Provider({
            aggregator: this.client
          })];
          this.providers[0].set('loggedIn', true);

          this.client.get('providers').set(this.providers);
        });

        it('Should logout from provider when a provider gets removed', sinon.test(function () {

          this.client.get('providers').remove(this.providers[0]);
          expect(this.providers[0].get('loggedIn')).to.be.false;
        }));
      });

      describe('getProviders()', function () {

        it('Should request the list of providers', sinon.test(function () {

          var promise = this.client.getProviders();

          promise.then(_.bind(function (providers) {

            expect(providers[0].get('name')).to.equal('OpenStack - Private');
            expect(providers[1].get('name')).to.equal('OpenStack - Public');
            expect(providers[2].get('name')).to.equal('moonshot - all');

            expect(this.client.get('providers').size()).to.equal(3);
          }, this)).done();

          this.requests[0].respond(200, {'Content-Type': 'application/json'}, JSON.stringify({
            providers: [{
              name: 'OpenStack - Private',
              providerType: 'os',
              providerId: 'private'
            }, {
              name: 'OpenStack - Public',
              providerType: 'os',
              providerId: 'public'
            }, {
              providerType: 'moonshot',
              providerId: 'all'
            }]
          }));
        }));
      });
    });

    describe('Tapestries', function () {

      describe('getEmptyTapestry()', function () {

        it('Should create an empty tapestry linked to the AggregatorClient', function () {

          var tapestry = this.client.getEmptyTapestry();
          expect(tapestry.get('threads').size()).to.equal(0);
          expect(tapestry.aggregator).to.equal(this.client);
        });
      });

      describe('syncTapestry', sinon.test(function () {

        it('Should create a new Tapestry if the Tapestry is new', sinon.test(function () {

          var tapestry = new Tapestry();
          var spy = this.spy(tapestry, "toJSON");
          this.client.syncTapestry(tapestry);

          expect(this.requests[0].url).to.equal('http://somewhere.in.the.cloud/tapestries');
          expect(this.requests[0].method).to.equal('POST');
          expect(spy).to.have.been.called;
        }));

        it('Should update the Tapestry ID when response comes back', sinon.test(function () {

          var tapestry = new Tapestry();
          var promise = this.client.syncTapestry(tapestry);

          promise.then(function (tapestry) {
            expect(tapestry.id).to.equal('server-generated-id');
          }).done();

          this.requests[0].respond(200, {'Content-Type': 'application/json'}, JSON.stringify({
            id: 'server-generated-id'
          }));
        }));
      }));
    });

    describe('getStatus()', function () {

      it('Should send a request for the aggregator status', sinon.test(function () {

        this.client.getStatus();
        expect(this.requests[0].url).to.equal('http://somewhere.in.the.cloud/status');
      }));
    });

    describe('getPatterns()', function () {

      it('Should send a request for the list of patterns available on the aggregator', sinon.test(function () {
        this.client.getPatterns();

        expect(this.requests[0].url).to.equal('http://somewhere.in.the.cloud/patterns');
      }));

      it('Should parse the patterns', function (done) {

        sinon.test(function () {

          var spy = this.spy(Pattern.prototype, 'parse');

          var patternsJSON = JSON.stringify({
            patterns: _.times(3, function (index) {

              return {
                id: index,
                name: 'Pattern #' + index
              };
            })
          });

          var promise = this.client.getPatterns();
          promise.then(function (patterns) {

            _.forEach(patterns, function (pattern) {

              expect(pattern).to.be.an.instanceof(Pattern);
            });
            expect(spy).to.have.been.called;
            done();
          });

          this.requests[0].respond(200, {'Content-Type': 'application/json'}, patternsJSON);
        }).apply(this);
      });
    });

    describe('getPattern()', function () {

      it('Should send a reuqest for the pattern with provided ID', sinon.test(function () {

        this.client.getPattern('some-pattern');

        expect(this.requests[0].url).to.equal('http://somewhere.in.the.cloud/patterns/some-pattern');
      }));

      it('Should parse resulting pattern', sinon.test(function () {

        var spy = this.spy(Pattern.prototype, 'parse');

        var patternJSON = JSON.stringify({
          providerType: 'providerType',
          name: 'Pattern name',
          threads: []
        });

        var promise = this.client.getPattern('some-pattern');
        promise.then(function (pattern) {
          expect(pattern).to.be.an.instanceof(Pattern);
          expect(spy).to.have.been.called;
        }).done();

        this.requests[0].respond(200, {'Content-Type': 'application/json'}, patternJSON);
      }));
    });

    describe('lockedProviders', function () {

      beforeEach(function () {

        this.providersJSON = [{
              name: 'OpenStack - Private',
              providerType: 'os',
              providerId: 'private'
            }, {
              name: 'OpenStack - Public',
              providerType: 'os',
              providerId: 'public'
            }];

        this.providers = _.map(this.providersJSON, function (providerJSON) {
          return new Provider(providerJSON);
        });

        this.client.get('providers').set(this.providers);
        _.forEach(this.providers, function (provider) {
          provider.set('aggregator', this.client);
        }, this);
      });

      it('Should get updated when a `423 Locked` error arrise from the server', sinon.test(function () {

        var response = {
          status: 423,
          message: JSON.stringify({
            providers: [this.providersJSON[0]]
          })
        };

        this.client.send({
          url: '/a/request'
        });

        this.requests[0].respond(423, {'Content-Type': 'application/json'}, JSON.stringify(response));

        expect(this.client.get('lockedProviders').models).to.have.members([this.providers[0]]);
        expect(this.providers[0].get('locked')).to.be.true;

        response = {
          status: 423,
          message: JSON.stringify({
            providers: this.providersJSON
          })
        };

        this.client.send({
          url: '/a/request'
        });

        this.requests[1].respond(423, {'Content-Type': 'application/json'}, JSON.stringify(response));

        expect(this.client.get('lockedProviders').models).to.have.members(this.providers);
        expect(this.providers[0].get('locked')).to.be.true;
        expect(this.providers[1].get('locked')).to.be.true;

        this.providers[0].login();

        this.requests[2].respond(200);

        expect(this.client.get('lockedProviders').models).not.to.have.members([this.providers[0]]);
        expect(this.providers[0].get('locked')).to.be.false;
        expect(this.providers[1].get('locked')).to.be.true;

        this.providers[1].login();

        this.requests[3].respond(403);

        expect(this.client.get('lockedProviders').models).to.have.members([this.providers[1]]);
        expect(this.providers[1].get('locked')).to.be.true;

        this.providers[1].login();

        this.requests[4].respond(200);

        expect(this.client.get('lockedProviders').size()).to.equal(0);
        expect(this.providers[1].get('locked')).to.be.false;
      }));
    });

    describe('send()', function () {

      it('Should prepend aggregator URL to the request\'s URL', sinon.test(function () {

        this.client.send({
          url: '/a/service'
        });

        expect(this.requests[0].url).to.equal('http://somewhere.in.the.cloud/a/service');
      }));

      it('Should not prepend aggregator URL if requests URL specifies a protocol', sinon.test(function () {

        this.client.send({
          url: 'http://somewhere.else'
        });

        expect(this.requests[0].url).to.equal('http://somewhere.else');
      }));
    });

    describe('Operations', function () {

      describe('getOperationsForItemType()', function () {

        it('Should the properly configured request', sinon.test(function () {

          this.client.getOperationsForItemType('itemType-id');

          expect(this.requests[0].url).to.equal('http://somewhere.in.the.cloud/operations?itemType=itemType-id');
        }));

        it('Should provide the list of operation, without duplicates', function (done) {

          sinon.test(function () {

            this.client.getOperationsForItemType('itemType-id').then(function (operations) {

              //console.log(operations);

              expect(_.pluck(operations, 'id')).to.deep.equal([
                'providerType-1/providerId-1/operationId-1',
                'providerType-1/providerId-2/operationId-3'
              ]);

              done();
            });

            this.requests[0].respond(200, {'Content-Type': 'application/json'}, JSON.stringify({
              operations: [{
                id: 'providerType-1/providerId-1/operationId-1'
              }, {
                id: 'providerType-1/providerId-2/operationId-1'
              }, {
                id: 'providerType-1/providerId-2/operationId-3'
              }]
            }));


          }).call(this);
        });
      });
    });
  });
});
