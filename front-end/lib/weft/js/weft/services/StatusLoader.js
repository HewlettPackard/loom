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
  var Adapter = require('weft/models/Adapter');
  var StatusEvent = require('weft/models/StatusEvent');

  /**
   * The StatusLoader is responsible for checking the status of the aggregator,
   *
   * @class StatusLoader
   * @namespace services
   * @module weft
   * @submodule services
   * @extends Backbone.Model
   */
  var StatusLoader = Backbone.Model.extend({

    constructorName: 'LOOM_StatusLoader',

    defaults: {
      /**
       * The result of the status call
       * @property status
       * @type {undefined|Number}
       * @default undefined
       */
      status: undefined,

      /**
       * Flag telling if the StatusLoader should poll at regular intervals
       * @property poll
       * @type {Boolean}
       * @default: true
       */
      poll: true,

      /**
       * The interval at which the status will be polled, in ms
       * this property will be overridden if polling interval if set in the config
       * @property pollingInterval
       * @type {Number}
       * @default 15000
       */
      pollingInterval: 15000,

      /**
       * The list of adapters available on the remote server
       * @type {Backbone.Collection}
       */
      adapters: new Backbone.Collection(),

      /**
       * The list of statusEvents
       * @type {Backbone.Collection}
       */
      statusEvents: new Backbone.Collection(),

      /**
       * @type {String}
       */
      serverVersion: 'not set',

      /**
       *  An index to store the setTimeout handles used for polling
       *  @type {null|Number}
       *  @default null
       */
      _timeoutHandle: null

      /**
       * @property {services.AggregatorClient} aggregator
       */
    },

    initialize: function () {
      this._timeoutHandle = null;
    },

    /**
     * Polls the status available at given statusURL
     * @method poll
     */
    poll: function () {
      this.get('aggregator').getStatus()
        .then(_.bind(this._updateStatus, this, 1))
        .fail(_.bind(this._updateStatus, this, 0))
        .done();
      this.schedulePoll();
    },

    /**
     * Schedules a poll for status
     * @method schedulePoll
     * @return {Number} The timeout reference
     */
    schedulePoll: function () {
      this.cancelScheduledPoll();
      this._timeoutHandle = setTimeout(_.bind(function () {
          if (this.get('poll')) {
            this.poll();
          } else {
            this.schedulePoll();
          }
        },
        this), this.get('pollingInterval'));
    },

    /**
     * Cancels poll currently scheduled
     * @method cancelScheduledPoll
     */
    cancelScheduledPoll: function () {
      clearTimeout(this._timeoutHandle);
    },

    /**
     * @method _updateStatus
     * @param status
     * @param json
     * @private
     */
    _updateStatus: function (status, json) {
      this.set('serverVersion', json.version);
      this.set('status', status);
      var adapterList = this.get('adapters');
      var adapters = [];
      _.each(json.adapters, function (adapt) {
        var adapter = new Adapter(adapt, {
          parse: true
        });
        adapters.push(adapter);
      });
      adapterList.set(adapters);

      var statusEvents = this.get('statusEvents');
      var events = [];
      _.each(json.statusEvents, function (event, key) {
        var evt = new StatusEvent(event, {
          parse: true
        });
        evt.name = key;
        events.push(evt);
      });
      statusEvents.set(events);
    }
  });

  return StatusLoader;

});
