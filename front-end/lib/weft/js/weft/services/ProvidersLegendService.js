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

  var Backbone = require('backbone');

  /**
   * ProviderLegendService associate common legends to Providers
   * and Threads displaying ItemTypes offered by those providers
   * @class ProvidersLegendService
   * @namespace services
   * @module  weft
   * @submodule services
   * @constructor
   * @extends Backbone.Model
   */
  var ProvidersLegendService = Backbone.Model.extend({

    defaults: {
      providers: new Backbone.Collection()
    },

    /**
     * @method getLegendForThread
     * @param thread
     * @returns {*}
     */
    getLegendForThread: function (thread) {
      return this.get('providers').reduce(function (legend, provider, index) {
        if (provider.offersItemTypeOfThread(thread)) {
          legend.push(index + 1);
        }
        return legend;
      }, []);
    },

    /**
     * @method getLegendForProvider
     * @param provider
     * @returns {*}
     */
    getLegendForProvider: function (provider) {
      return this.get('providers').indexOf(provider) + 1;
    }
  });

  return ProvidersLegendService;
});