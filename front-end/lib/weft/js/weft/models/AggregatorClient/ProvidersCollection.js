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
   * The definition of an action that can be executed on an element
   * @class ProvidersCollection
   * @module weft
   * @submodule models.aggregatorClient
   * @namespace models.aggregatorClient
   * @extends Backbone.Collection
   */
  return Backbone.Collection.extend({

    /**
     * Remove a provider
     * @method remove
     * @param models
     */
    remove: function (models) {
      this.logout(models);
      Backbone.Collection.prototype.remove.apply(this, arguments);
    },

    /**
     * Log out of a provider
     * @method logout
     * @param models
     */
    logout: function (models) {
      if (!_.isArray(models)) {
        models = [models];
      }
      _.forEach(models, function (model) {
        // Client-only logout first so logout propagates quickly while still in the collection
        // Then fire and forget logout, the provider is getting removed anyways
        model.logout(true);
        model.logout();
      });
    }
  });
});
