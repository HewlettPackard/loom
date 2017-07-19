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

  /**
   * AvailableItemTypesCollection maintains a list of available item types
   * offered by given list of provider
   * @class  AvailableItemTypesCollection
   * @constructor
   * @module weft
   * @submodule models
   * @namespace models
   * @param {Backbone.Collection} providers The list of providers monitored by this collection
   */
  function AvailableItemTypesCollection(providers) {

    /**
     * @property {Array} itemTypes The list of item types offered by the list of providers
     */
    this.itemTypes = [];

    /**
     * @property {Backbone.Collection} providers The list of providers monitored by this collection
     */
    this.providers = providers;

    this.listenTo(this.providers, 'change:loggedIn', this._updateAvailableItemTypes, this);
  }

  _.merge(AvailableItemTypesCollection.prototype, Backbone.Events, {

    _updateAvailableItemTypes: function (provider) {

      if (provider.get('loggedIn')) {
        this._addNewItemTypes(provider);
      } else {
        this._removeUnavailableItemTypes(provider);
      }
    },

    /**
     * Add a new Item Type to the Collection
     * @param provider
     * @private
     */
    _addNewItemTypes: function (provider) {

      var offeredItemTypes = provider.getAvailableItemTypes();
      _.forEach(offeredItemTypes, function (itemType) {

        // IMPROVE: If performance suffers, index itemTypes to make the check faster
        if (!_.contains(this.itemTypes, itemType)) {
          this.itemTypes.push(itemType);
          /**
           * Send an add event when a new item is added to the collection
           * Event is sent on the object and not on the global Backbone bus
           * @event add
           * @param ItemType
           */
          this.trigger('add', itemType);
        }
      }, this);
    },

    _removeUnavailableItemTypes: function (provider) {

      var unavailableItemTypes = this._getUnavailableItemTypes(provider.getAvailableItemTypes());
      _.forEach(unavailableItemTypes, function (itemType) {

        _.pull(this.itemTypes, itemType);
        this.trigger('remove', itemType);
      }, this);
    },

    _getUnavailableItemTypes: function (itemTypes) {

      return _.filter(itemTypes, function (itemType) {
        return !this.providers.find(function (provider) {
          return provider.get('loggedIn') && _.contains(provider.getAvailableItemTypes(), itemType);
        });
      }, this);
    }
  });

  return AvailableItemTypesCollection;
});
