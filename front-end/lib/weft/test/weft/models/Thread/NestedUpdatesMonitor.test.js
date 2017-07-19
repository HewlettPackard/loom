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

  var Thread = require('weft/models/Thread');
  var Aggregation = require('weft/models/Aggregation');

  describe.skip('NestedUpdatesMonitor', function () {

    beforeEach(function () {

      var parent = new Thread({
        elementProperties: {
          'createdCount': 'Recently created',
          'updatedCount': 'Recently updated',
          'deletedCount': 'Recently deleted'
        },
        stateChangeTimeouts: {
          nestedAdd: 6000,
          nestedUpdate: 5000,
          nestedDelete: 7000
        }
      });

      this.aggregation = new Aggregation({
        parent: parent
      });
    });

    it('Should update the Threads state to STATE_NESTED_UPDATE when a nested update is detected', sinon.test(function () {

      this.aggregation.set('createdCount', 10);

      expect(this.aggregation.state).to.equal(Thread.STATE_NESTED_UPDATE, '1');

      this.clock.tick(60001);

      expect(this.aggregation.state).to.be.undefined;

      this.aggregation.set('updatedCount', 20);

      expect(this.aggregation.state).to.equal(Thread.STATE_NESTED_UPDATE, '2');

      this.clock.tick(60001);

      expect(this.aggregation.state).to.be.undefined;

      this.aggregation.set('deletedCount', 20);

      expect(this.aggregation.state).to.equal(Thread.STATE_NESTED_UPDATE, '3');

      this.clock.tick(60001);

      expect(this.aggregation.state).to.be.undefined;
    }));

    // All the timeouts are the same at the moment
    it('Should use the longest timeout if multiple properties change at the same time', sinon.test(function () {

      this.aggregation.set({
        'createdCount': 10,
        'deletedCount': 2
      });

      this.clock.tick(5000);

      expect(this.aggregation.state).to.equal(Thread.STATE_NESTED_UPDATE);

      this.clock.tick(2000);

      expect(this.aggregation.state).to.be.undefined;
    }));
  });
});
