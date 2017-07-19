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
  var Thread = require('weft/models/Thread');
  var ThreadAvailabilityMonitor = require('weft/services/ThreadAvailabilityMonitor');

  describe('weft/services/ThreadAvailabilityMonitor.js', function () {

    before(function () {
      var threads = this.threads = _.times(2, function (index) {
        return new Thread({
          id: index,
          itemType: {
            id: 'it_' + index
          }
        });
      });

      threads.push(new Thread({
        id: 3,
        itemType: {
          id: 'it_1'
        }
      }));

      // Mock of the available itemtype collection
      var itemTypes = this.itemTypes = Object.create(Backbone.Events);

      this.monitor = new ThreadAvailabilityMonitor(new Backbone.Collection(threads), itemTypes);
    });

    it('Should mark Thread as not available when its item type is no longer available', function () {

      this.itemTypes.trigger('remove', 'it_1');

      expect(this.threads[1].get('unavailable')).to.be.true;
      expect(this.threads[2].get('unavailable')).to.be.true;
    });

    it('Should mark Thread as available again when its item type is available again', function () {

      this.itemTypes.trigger('add', 'it_1');

      expect(this.threads[1].get('unavailable')).not.to.be.true;
      expect(this.threads[2].get('unavailable')).not.to.be.true;
    });
  });
});