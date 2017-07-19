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
/* global describe, it, expect, sinon */
/* jshint expr: true */
define(function (require) {

  "use strict";

  var _ = require('lodash');
  var Backbone = require('backbone');
  var Thread = require('weft/models/Thread');
  var Pattern = require('weft/models/Pattern');
  var ItemType = require('weft/models/ItemType');

  describe('weft/models/Pattern.js', function () {

    describe('parse()', function () {

      var json = {
        providerType: 'providerType',
        name: 'Pattern Name',
        threads: [{
          aggregation: '/os/instances',
          itemType: '/os/instance',
          query: {}
        }, {
          aggregation: '/os/private/instances',
          itemType: '/os/instance',
          query: {}
        }, {
          aggregation: '/os/regions',
          itemType: 'os/region'
        }, {
          aggregation: '/os/definitions',
          itemType: 'os/definition'
        }],
        _meta: {
          itemTypes: {
            '/os/instance': {
              id: '/os/instance',
              attributes: {
                powerStatus: {
                  name: "Power Status"
                },
                name: {
                  name: "Name"
                },
                flavour: {
                  name: "Flavour"
                },
                cpuLoad: {
                  name: "CPU load"
                },
                ipAddress: {
                  name: "IP Address"
                }
              }
            },
            '/os/definition': {

            }
          }
        }
      };

      it('Should create a Pattern with appropriate medatada', function () {

        var pattern = new Pattern(json, {
          parse: true
        });

        expect(_.omit(pattern.attributes, 'threads')).to.deep.equal({
          providerType: 'providerType',
          name: 'Pattern Name'
        });
      });

      it('Should delegate the parsing of the Threads to Thread, providing the appropriate information for the Item Type', sinon.test(function () {

        var spy = this.spy(Thread.prototype, 'parse');

        var pattern = new Pattern(json, {
          parse: true
        });

        expect(spy).to.have.been.called;
        expect(spy.args[0][0].itemType).to.be.an('object');
        expect(spy.args[1][0].itemType).to.be.an('object');
        expect(spy.args[2][0].itemType).to.be.undefined;
        expect(pattern.get('threads').length).to.equal(4);
      }));
    });

    describe('getMissingThreads()', function () {

      it('Returns the list of threads from the pattern that are not in the provided list', function () {

        var threads = _.times(3, function (index) {
          return new Thread({
            name: 'Thread ' + index,
            itemType: new ItemType({
              id: 'item-type-' + index
            })
          });
        });

        var pattern = new Pattern({
          threads: threads
        });

        var existingThreads = new Backbone.Collection(_.times(2, function (index) {
          return new Thread({
            name: 'Thread ' + index,
            itemType: new ItemType({
              id: 'item-type-' + index
            })
          });
        }));

        var missingThreads = pattern.getMissingThreads(existingThreads);

        expect(missingThreads.length).to.equal(1);
      });
    });

    describe('join()', function () {

      it('Should create a pattern combining the Threads of provided patterns', function () {

        var threads = _.times(4, function (index) {
          return new Thread({
            name: 'Thread ' + index
          });
        });

        var patternA = new Pattern({
          threads: [threads[0], threads[1]]
        });

        var patternB = new Pattern({
          threads: [threads[2]]
        });

        var patternC = new Pattern({
          threads: [threads[3]]
        });

        expect(Pattern.join(patternA, patternB, patternC).get('threads')).to.deep.equal(threads);

      });
    });
  });
});
