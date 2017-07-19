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
   * LockingManager
   * @class LockingManager
   * @namespace services
   * @module weft
   * @submodule services
   * @extends Backbone.Model
   */
  var LockingManager = Backbone.Model.extend({

    /**
     * The AggregatorClient the manager monitors
     * @attribute aggregator
     * @type {services.AggregatorClient}
     */

    /**
     * The Tapestry whose synchronisation is dependent
     * on the `aggregator` locking state
     * @attribute tapestry
     * @type {models.Tapestry}
     */

    /**
     * The ThreadMonitorService whose activity is dependent
     * on the `aggregator` locking state
     * @attribute threadMonitor
     * @type {services.ThreadMonitorService}
     */

    initialize: function () {
      this.listenTo(this.get('aggregator'), 'change:hasLockedProviders', this._handleLockingState);
      this.listenTo(this.get('aggregator'), 'loomSessionLost', this._handleLostOfLoomSession);
    },

    /**
     * @method _handleLostOfLoomSession
     * @private
     */
    _handleLostOfLoomSession: function () {
      // The new session won't have any Tapestry
      this.get('tapestry').set('id', null);
      this.get('aggregator').get('providers').forEach(function (provider) {
        if (provider.get('loggedIn')) {
          provider.lock();
        }
      });
    },

    /**
     * @method _handleLockingState
     * @private
     */
    _handleLockingState: function () {
      if (this.get('aggregator').get('hasLockedProviders')) {
        this.lock();
      } else {
        this.unlock();
      }
    },

    /**
     * @method lock
     */
    lock: function () {
      this.get('threadMonitor').pause();
    },

    /**
     * @method unlock
     */
    unlock: function () {
      this.get('tapestry').save().then(_.bind(function () {
        this.get('threadMonitor').resume();
      }, this)).done();
    }
  });

  return LockingManager;
});