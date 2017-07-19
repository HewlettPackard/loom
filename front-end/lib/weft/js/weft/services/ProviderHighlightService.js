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
   * The ProviderHighlightService helps highlighting Fibers which come from a specific provider
   * for a given duration
   * @class ProviderHighlightService
   * @namespace services
   * @module  weft
   * @submodule services
   * @constructor
   * @param {Backbone.Collection} fibersList The list of fibers
   */
  function ProviderHighlightService(fibersList) {
    this.fibersList = fibersList;
    this.listenTo(this.fibersList, 'add change:l.providerId', this._maybeHighlightProvider);
    this.listenTo(this.fibersList, 'remove', this._removeHighlightedFiber);
  }

  _.extend(ProviderHighlightService.prototype, Backbone.Events, {

    /**
     * @method highlight
     * @param provider
     * @param duration
     */
    highlight: function (provider, duration) {
      this.clearHighlight();
      if (provider) {
        this.highlightedProvider = provider;
        this.highlightedProvider.set('highlighted', true);
        this.highlightedFibers = this.findFibersFromProvider(provider.id);
        this.flagHighlightedFibers(true);
        if (duration) {
          this.timeout = setTimeout(this.clearHighlight.bind(this), duration);
        }
      }
    },

    /**
     * @method _maybeHighlightProvider
     * @param fiber
     * @private
     */
    _maybeHighlightProvider: function (fiber) {
      if (this.highlightedProvider) {
        if (fiber.isFromProvider(this.highlightedProvider.id)) {
          fiber.set('isFromHighlightedProvider', true);
          if (!_.contains(this.highlightedFibers, fiber)) {
            this.highlightedFibers.push(fiber);
          }
        } else {
          fiber.set('isFromHighlightedProvider', false);
          this.highlightedFibers = _.without(this.highlightedFibers, fiber);
        }
      }
    },

    /**
     * @method findFibersFromProvider
     * @param providerId
     * @returns {TModel[]}
     */
    findFibersFromProvider: function (providerId) {
      return this.fibersList.filter(function (fiber) {
        return fiber.isFromProvider(providerId);
      });
    },

    /**
     * @method flagHighlightedFibers
     * @param flag
     */
    flagHighlightedFibers: function (flag) {
      _.forEach(this.highlightedFibers, function (fiber) {
        fiber.set('isFromHighlightedProvider', flag);
      });
    },

    /**
     * @method clearHighlight
     */
    clearHighlight: function () {
      if (this.highlightedProvider) {
        this.highlightedProvider.set('highlighted', false);
      }
      this.flagHighlightedFibers(false);
      this.highlightedProvider = undefined;
      this.highlightedFibers = [];
      if (this.timeout) {
        clearTimeout(this.timeout);
      }
    }
  });

  return ProviderHighlightService;
});