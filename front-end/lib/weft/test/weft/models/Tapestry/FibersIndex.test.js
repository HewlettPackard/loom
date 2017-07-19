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
/* global describe, it, expect, beforeEach, sinon */
/* jshint expr: true, strict: false */
define(function (require) {


  var _ = require('lodash');
  var Backbone = require('backbone');
  var Item = require('weft/models/Item');
  var QueryResult = require('weft/models/QueryResult');
  var FibersIndex = require('weft/models/Tapestry/FibersIndex');

  describe('weft/models/Tapestry/FibersIndex.js', function () {

    beforeEach(function () {

      this.results = new Backbone.Collection([new QueryResult(), new QueryResult()]);

      this.fibersIndex = new FibersIndex({
        queryResults: this.results
      });

      var items = this.items = _.times(7, function (index) {

        return new Item({
          'l.logicalId': index
        });
      });

      this.getItems = function (start, end) {
        return new Backbone.Collection(_.at(items, _.range(start, end)));
      };

      this.index = this.fibersIndex.get('index');
    });

    it('Should get updated when the content of a QueryResult change', sinon.test(function () {

      this.results.at(0).set('elements', this.getItems(0, 4));
      this.results.at(1).set('elements', this.getItems(4, 7));

      expect(this.index.size()).to.equal(7);
      // Update first results, with some elements remove and a few shared with the other result
      this.results.at(0).set('elements', this.getItems(2, 6));

      expect(this.index.size()).to.equal(5);
      expect(this.index.models.indexOf(this.items[0])).to.equal(-1);
      expect(this.index.models.indexOf(this.items[1])).to.equal(-1);
    }));

    it('Should get updated when a QueryResult gets removed from the results being indexed', sinon.test(function () {

      this.results.at(0).set('elements', this.getItems(4));
      this.results.at(1).set('elements', this.getItems(4, 7));

      var removedQueryResult = this.results.at(1);
      this.results.remove(removedQueryResult);

      expect(this.index.size()).to.equal(4);
      removedQueryResult.get('elements').forEach(function (fiber) {
        expect(this.index.models.indexOf(fiber)).to.equal(-1);
      }, this);
    }));
  });
});