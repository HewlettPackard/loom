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
  var Backbone = require('backbone');
  var SharedQueryResultsManager = require('weft/models/Tapestry/SharedQueryResultsManager');
  var Thread = require('weft/models/Thread');

  describe('weft/models/Tapestry/SharedQueryResultsManager.js', function () {

    var threads = _.times(3, function (index) {
      return new Thread({
        'l.logicalId': index,
        'result': null
      });
    });

    var originalQuery = threads[1].get('query').clone();

    var differentQuery = threads[1].get('query').clone();
    differentQuery.set('inputs', ['/other-aggergation']);

    var thirdDifferentQuery = threads[1].get('query').clone();
    thirdDifferentQuery.set('inputs', ['/third-aggregation']);

    var threadCollection = new Backbone.Collection();

    var queryResultManager = new SharedQueryResultsManager({
      threads: threadCollection
    });

    it('Should assign a QueryResult to a Thread that gets added', function () {

      threadCollection.add(threads[0]);
      expect(threads[0].get('result')).not.to.be.undefined;
    });

    it('Should assign the same QueryResult if the Queries are the same', function () {

      threadCollection.add(threads[1]);
      expect(threads[0].get('query').isSameAs(threads[1].get('query'))).to.be.true;
      expect(threads[1].get('result')).to.equal(threads[0].get('result'));
    });


    it('Should provide a new QueryResult when one of the Query changes', function () {

      threads[1].set('query', differentQuery);
      expect(threads[1].get('result')).not.to.equal(threads[0].get('result'));
    });

    it('Should not removeQueryResult from cache as long as a Thread with the query is there', function () {
      expect(queryResultManager.get('results').size()).to.equal(2);
    });

    it('Should not remove QueryResult from cache as long as the last query result is pending', function () {

      // Create a third QR to check the proper behavior of chaining.
      threads[1].set('query', thirdDifferentQuery);
      expect(queryResultManager.get('results').size()).to.equal(3);
    });

    it('Should provide the same QueryResult when a Query changes to a Query already used', function () {

      threads[1].set('query', originalQuery);
      expect(threads[1].get('result')).to.equal(threads[0].get('result'));
    });

    it('Should remove QueryResult from cache when no Threads have the corresponding Query', function () {

      // Emulate the update of the query result from the server.
      // This call will remove old result that will in turn
      // removed elements that are not used anymore.
      threads[0].get('result').set('pending', false); // <-- Here chaining occurs.
      // Old QR are now deleted.

      expect(queryResultManager.get('results').size()).to.equal(1);
      threadCollection.remove(threads[1]);
      threadCollection.remove(threads[0]);
      expect(queryResultManager.get('results').size()).to.equal(0);
    });
  });
});
