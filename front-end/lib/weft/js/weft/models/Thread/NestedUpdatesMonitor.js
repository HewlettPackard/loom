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

  /**
   * NestedUpdatesMonitor is a helper that updates the state of a Thread
   * when nested updates are detected
   * @class       NestedUpdatesMonitor
   * @namespace   models.thread
   * @module      weft
   * @submodule   models.thread
   * @constructor
   * @param {models.Thread} thread The Thread being monitored
   */
  function NestedUpdatesMonitor(thread) {
    this.thread = thread;
    this.thread.on('change', this._markNestedUpdates, this);
  }

  _.extend(NestedUpdatesMonitor.prototype, {
    /**
     * todo: This could actually be moved to the thread. there is no need for this class
     * @private
     */
    _markNestedUpdates: function () {
      var updates = this.thread.getNestedStateChanges();
      if (updates.length) {
        var duration = this.thread.getLongestStateChangeTimeout(updates);
        this.thread.setNestedUpdateState(duration);
      }
    }
  });

  return NestedUpdatesMonitor;
});
