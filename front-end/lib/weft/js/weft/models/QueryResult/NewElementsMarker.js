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
   * NewElementsMarker is a helper flagging new Elements in a QueryResult
   * when its list of Elements change
   *
   * @class NewElementsMarker
   * @module weft
   * @submodule models.queryResult
   * @namespace models.queryResult
   * @constructor
   * @param {models.QueryResult} queryResult The QueryResult whose elements are monitored
   */
  function NewElementsMarker(queryResult) {
    this.queryResult = queryResult;
    this.queryResult.on('change:elements', this._markNewElements, this);
    this.queryResult.on('change:query', this.ignoreNextReset, this);
  }

  _.extend(NewElementsMarker.prototype, {

    /**
     * Provides ability to ignore the next reset
     * @method ignoreNextReset
     */
    ignoreNextReset: function () {
      this.markElements = false;
    },

    /**
     * Marks new elements and resets the ignore next reset flag
     * @method _markNewElements
     * @private
     */
    _markNewElements: function () {
      if (!this.queryResult.get('pending')) {
        if (this.markElements) {
          this._doMarkNewElements();
        } else {
          this.markElements = true;
        }
      }
    },

    /**
     * @method _doMarkNewElements
     * @private
     */
    _doMarkNewElements: function () {
      var previousElements = this.queryResult.previous('elements');
      var addedElements = _.difference(this.queryResult.get('elements').models, previousElements ? previousElements.models : undefined);
      _.forEach(addedElements, function (element) {
        element.setAddedState();
      });
    }
  });

  return NewElementsMarker;
});
