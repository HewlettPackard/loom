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
  var Sort = require('weft/models/Sort');

  /**
   * AvailableSortsUpdater is a helper maintaining the list
   * of sorts available for the Thread it is responsible for
   * @class AvailableSortsUpdater
   * @namespace models.thread
   * @module weft
   * @submodule models.thread
   * @constructor
   */
  function AvailableSortsUpdater(thread) {
    this.thread = thread;
    this.thread.get('metrics').on('add', this._addSortOption, this);
    this.thread.get('metrics').on('remove', this._removeSortOption, this);
  }

  _.extend(AvailableSortsUpdater.prototype, {

    /**
     * Adds an option to sort for a given metric
     * @method _addSortOption
     * @param metric {models.metric}
     * @private
     */
    _addSortOption: function (metric) {
      var sortOption = new Sort({
        id: metric.id,
        property: metric,
        order: 'ascending'
      });
      this.thread.availableSorts.add(sortOption);
    },

    /**
     * Remove the sort option for a thread
     * @method _removeSortOption
     * @param metric
     * @private
     */
    _removeSortOption: function (metric) {
      this.thread.availableSorts.remove(metric.id);
    }
  });

  return AvailableSortsUpdater;
});
