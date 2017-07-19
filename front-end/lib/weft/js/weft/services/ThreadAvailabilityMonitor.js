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
   * ThreadAvailabilityMonitor monitors a list of Threads to mark if their data
   * is available or not according to given list of available item types
   * @class ThreadAvailabilityMonitor
   * @namespace services
   * @module  weft
   * @submodule services
   * @constructor
   * @param {Backbone.Collection} threads The list of Threads to monitor
   * @param {services.AvailableItemTypesCollection} availableItemTypes The list of available item types
   */
  function ThreadAvailabilityMonitor(threads, availableItemTypes) {
    this.threads = threads;
    this.listenTo(availableItemTypes, 'add', this._unmarkAvailableThreads, this);
    this.listenTo(availableItemTypes, 'remove', this._markUnavailableThreads, this);
  }

  _.extend(ThreadAvailabilityMonitor.prototype, Backbone.Events, {

    /**
     * @method _markUnavailableThreads
     * @param itemType
     * @private
     */
    _markUnavailableThreads: function (itemType) {
      var threadWithRemovedItemType = this.threads.filter(function (thread) {
        return thread.get('itemType').id === itemType;
      });
      _.forEach(threadWithRemovedItemType, function (thread) {
        thread.set('unavailable', true);
      });
    },

    /**
     * @method _unmarkAvailableThreads
     * @param itemType
     * @private
     */
    _unmarkAvailableThreads: function (itemType) {
      var threadWithAddedItemType = this.threads.filter(function (thread) {
        return thread.get('itemType').id === itemType;
      });
      _.forEach(threadWithAddedItemType, function (thread) {
        thread.set('unavailable', false);
      });
    }
  });

  return ThreadAvailabilityMonitor;

});
