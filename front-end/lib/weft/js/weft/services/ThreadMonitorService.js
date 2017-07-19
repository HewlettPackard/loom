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
   * The ThreadMonitorService monitors queryResults for changes (in grouping, sorting...)
   * and queries the aggregator accordingly to update the queryResult's data. It is also responsible for
   * polling the aggregator at regular intervals.
   * @class ThreadMonitorService
   * @module weft
   * @submodule services
   * @namespace services
   * @extends Backbone.Model
   * @constructor
   */
  var ThreadMonitorService = Backbone.Model.extend({

    constructorName: 'LOOM_ThreadMonitorService',

    config: {
      pollingInterval: 15000
    },

    defaults: function () {
      return {

        /**
         * Flag telling if the ThreadMonitorService should poll at regular intervals
         * @attribute poll
         * @type {Boolean}
         * @default: true
         */
        poll: true,

        /**
         * The interval at which the queryResults will be polled, in ms
         * this property will be overridden if polling interval if set in the queryResult description in pattern
         * @attribute pollingInterval
         * @type {Number}
         * @default 15000
         */
        // Extra level of indirection to allow the configuration of the polling interval
        // by updating ThreadMonitorService.prototype.config.pollingInterval
        pollingInterval: ThreadMonitorService.prototype.config.pollingInterval,

        /**
         * @attribute queryResults
         * @type Backbone.Collection
         */
        queryResults: new Backbone.Collection(),

        /**
         * Collection of the fibers for QueryResults to look up their items into
         * when parsing the polling results
         * @attribute  fibersIndex
         * @type {Backbone.Collection}
         */
        fibersIndex: new Backbone.Collection()
      };
    },

    // CONSTRUCTOR
    initialize: function () {

      /**
       * todo: why here and not in defaults?
       * An index to store the setTimeout handles used for polling
       * @attribute _timeoutHandles
       * @type {{}}
       * @private
       */
      this._timeoutHandles = {};

      /**
       * todo: why here and not in defaults?
       * An index to store the polling requests
       * @attribute _requests
       * @type {{}}
       * @private
       */
      this._requests = {};

      this.listenTo(this, 'change:poll', function (service, poll) {
        if (poll) {
          this.get('queryResults').each(_.bind(function (queryResult) {
            this.poll(queryResult);
          }, this));
        }
      });

      this._listenToThreadsEvents(this.get('queryResults'));
    },

    /**
     * @method _listenToThreadsEvents
     * @param queryResults
     * @private
     */
    _listenToThreadsEvents: function (queryResults) {
      this.listenTo(queryResults, 'remove', function (queryResult) {
        this._abortPendingPoll(queryResult);
        this.cancelScheduledPoll(queryResult);
      }, this);

      this.listenTo(queryResults, 'change:queryable', function (queryResult, queryable) {
        if (queryable) {
          this.poll(queryResult);
        } else {
          this._abortPendingPoll(queryResult);
          this.cancelScheduledPoll(queryResult);
        }
      }, this);
    },

    /**
     * Add a query result
     * @method add
     * @param queryResult
     */
    add: function (queryResult) {
      this.get('queryResults').add(queryResult);
    },

    /**
     * remove a query result
     * @method remove
     * @param queryResult
     */
    remove: function (queryResult) {
      this.get('queryResults').remove(queryResult);
    },

    /**
     * Pause polling for new results
     * @method pause
     */
    pause: function () {
      this.set('poll', false);
    },

    /**
     * Resume polling for new results
     * @method resume
     */
    resume: function () {
      this.set('poll', true);
    },

    /**
     * Polls the content of given queryResult
     * @method poll
     * @param queryResult {models.Thread|Number} The queryResult to poll.
     * If passed a String, the service will look for a queryResult with given ID in the list of queryResult it monitors.
     * If passed a Number, the service will query the queryResult at given index in the list of queryResult it monitors.
     */
    poll: function (queryResult) {
      if (!_.isObject(queryResult)) {
        queryResult = this._findThread(queryResult);
      }
      if (!queryResult) {
        return;
      }
      queryResult.refresh(this.get('fibersIndex'));
      this.schedulePoll(queryResult);
    },

    /**
     * Poll all the QueryResults monitored by this ThreadMonitorService
     * @method pollAll
     */
    pollAll: function () {
      this.get('queryResults').forEach(this.poll, this);
    },

    /**
     * Cancel and abort all the polling query results
     * @method abortAll
     */
    abortAll: function () {
      this.queryResults.forEach(function (queryResult) {
        queryResult.cancelRefresh();
      });
    },

    /**
     * Schedules a poll for given queryResult
     * @method schedulePoll
     * @param queryResult {models.Thread}
     * @return {Number} The timeout reference
     */
    schedulePoll: function (queryResult) {
      this.cancelScheduledPoll(queryResult);
      /* set interval value */
      this._timeoutHandles[queryResult.cid] = setTimeout(_.bind(function () {
        if (this._isLastPollComplete(queryResult) && this.get('poll')) {
          this.poll(queryResult);
        } else {
          this.schedulePoll(queryResult);
        }
      }, this), this.get('pollingInterval'));
    },

    /**
     * @method delayedPoll
     * @param queryResult
     * @param delay
     */
    delayedPoll: function (queryResult, delay) {
      this._timeoutHandles[queryResult.cid] = setTimeout(_.bind(function () {
        this.poll(queryResult);
      }, this), delay);
    },

    /**
     * Cancels poll currently scheduled for given queryResult
     * @method cancelScheduledPoll
     * @param queryResult {models.Thread}
     */
    cancelScheduledPoll: function (queryResult) {
      clearTimeout(this._timeoutHandles[queryResult.cid]);
    },

    /**
     * @method _notifyFailure
     * @param queryResult
     * @private
     */
    _notifyFailure: function (queryResult) {
      console.warn('Polling failed', queryResult);
    },

    /**
     * @method _findThread
     * @param index
     * @returns {*}
     * @private
     */
    _findThread: function (index) {
      return this.get('queryResults').at(index);
    },

    /**
     * @method _isLastPollComplete
     * @param queryResult
     * @returns {boolean}
     * @private
     */
    _isLastPollComplete: function (queryResult) {
      return !queryResult.isRefreshing();
    },

    /**
     * @method _abortPendingPoll
     * @param queryResult
     * @private
     */
    _abortPendingPoll: function (queryResult) {
      queryResult.cancelRefresh();
    }

  });

  return ThreadMonitorService;
});
