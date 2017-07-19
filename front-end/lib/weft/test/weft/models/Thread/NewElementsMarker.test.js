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
/*global describe, it, expect, beforeEach, sinon */
/*jshint expr: true */
define(function (require) {

  "use strict";

  var _ = require('lodash');
  var Backbone = require('backbone');
  var Element = require('weft/models/Element');
  var QueryResult = require('weft/models/QueryResult');

  describe('weft/models/NewElementMarker.js', function () {

    beforeEach(function () {

      this.queryResult = new QueryResult({
        pending: false,
      });

      this.elements = _.times(3, function (index) {

        return new Element({
          id: index
        });
      });
    });

    // Makes sure sinon configuration is reset after tests
    // so that specific tests can alter it any way they want
    afterEach(function () {
      sinon.config = undefined;
    });

    it('Should ignore the first poll', function () {

      var elements = [this.elements[0], this.elements[1], this.elements[2]];

      this.queryResult.set('elements', new Backbone.Collection(elements));

      elements.forEach(function (element) {

        expect(element.state).to.be.undefined;
      });
    });

    it('Should ignore the first poll, even in case of poor query timing', function (done) {

      sinon.test(function () {

        console.log('Testing!');
        this.queryResult.on('change:elements', function (result, elements) {
          expect(elements.at(1).state).not.to.equal('added');
          done();
        });

        this.queryResult.refresh(new Backbone.Collection());
        this.queryResult.refresh(new Backbone.Collection());

        // First request should be aborted to avoid parsing
        expect(this.requests[0].aborted).to.be.true;


        // Second response comes in
        this.requests[1].respond(200, {
          'Content-Type': 'application/json'
        }, JSON.stringify({
          elements: [{
            'entity': {
              'l.logicalId': 'a'
            }
          }, {
            'entity': {
              'l.logicalId': 'b'
            }
          }]
        }));

       }).apply(this);
    });

    it('Should not mark new elements while the query is pending', function () {

      this.queryResult.set({
        pending:  true,
        elements: new Backbone.Collection()
      });

      this.queryResult.set({
        pending: false,
        elements: new Backbone.Collection([this.elements[0], this.elements[1]])
      });

      expect(this.elements[0].state).not.to.equal(Element.STATE_ADDED);
      expect(this.elements[1].state).not.to.equal(Element.STATE_ADDED);

    });

    it('Should mark the new elements of the QueryResult', sinon.test(function () {

      this.queryResult.set('elements', new Backbone.Collection(this.elements[2]));

      var elements = [this.elements[0], this.elements[1], this.elements[2]];

      this.queryResult.set('elements', new Backbone.Collection(elements));


      expect(this.elements[0].state).to.equal(Element.STATE_ADDED);
      expect(this.elements[1].state).to.equal(Element.STATE_ADDED);

      this.clock.tick(60000);

      elements.forEach(function (element) {

        expect(element.state).to.be.undefined;
      });
    }));
  });
});
