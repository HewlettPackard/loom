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
   * Reloads providers on an adapter change
   * @class ProvidersReloader
   * @namespace services
   * @module  weft
   * @submodule services
   * @extends Backbone.Events
   * @param adapters
   * @param aggregator
   * @constructor
   */
  function ProvidersReloader(adapters, aggregator) {
    this.aggregator = aggregator;
    this.listenTo(adapters, 'add remove', this._reloadProviders);
  }

  _.extend(ProvidersReloader.prototype, Backbone.Events, {

    /**
     * triggered on add/remove event fired on adapters
     * @method _reloadProviders
     * @private
     */
    _reloadProviders: function () {
      this.aggregator.getProviders();
    }
  });

  return ProvidersReloader;
});
