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
  var Provider = require('weft/models/Provider');
  var AvailableItemTypesCollection = require('weft/models/AvailableItemTypesCollection');

  describe('weft/models/AvailableItemTypesCollection.js', function () {

    before(function () {

      var providers = this.providers = _.times(2, function (i) {
        var provider = new Provider({
          id: i
        });

        sinon.stub(provider, 'getAvailableItemTypes').returns(_.times(2, function (j) {

          // Make sure a little collision in the item types provided happens
          if (i === 1 && j === 0) {
            return 'it_0_0';
          }
          return 'it_' + i + '_' + j;
        }));

        return provider;
      });

      this.collection = new AvailableItemTypesCollection(new Backbone.Collection(providers));
    });

    it('Should add the item types available on the provider when user logs into a new Provider', function () {

      var eventsValues = [];
      this.collection.on('add', function (itemType) {
        eventsValues.push(itemType);
      });

      this.providers[0].set('loggedIn', true);

      expect(this.collection.itemTypes).to.deep.equal(['it_0_0', 'it_0_1']);
      expect(eventsValues).to.deep.equal(['it_0_0', 'it_0_1'], 'Events');
    });

    it("Should not add the same item type twice", function () {

      var eventsValues = [];
      this.collection.on('add', function (itemType) {
        eventsValues.push(itemType);
      });

      this.providers[1].set('loggedIn', true);

      expect(this.collection.itemTypes).to.deep.equal(['it_0_0', 'it_0_1', 'it_1_1']);
      expect(eventsValues).to.deep.equal(['it_1_1']);
    });

    it('Should remove item types no longer available after user logs out from a Provider', function () {

      this.providers[1].set('loggedIn', false);

      expect(this.collection.itemTypes).to.deep.equal(['it_0_0', 'it_0_1']);

      this.providers[0].set('loggedIn', false);

      expect(this.collection.itemTypes).to.deep.equal([]);
    });

  });
});
