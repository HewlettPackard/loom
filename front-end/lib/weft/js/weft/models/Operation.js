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

  var ID_TOKEN_REGEX = /(.*)\/(.*)\/(.*)/;

  /**
   * Defines an Operation in the system
   * @class Operation
   * @module weft
   * @submodule models
   * @namespace models
   * @param options
   * @constructor
   */
  function Operation(options) {
    _.merge(this, options);
    this._idTokens = ID_TOKEN_REGEX.exec(this.id);
  }

  _.extend(Operation.prototype, {

    /**
     * Checks if this operation already exists in provided list,
     * @param  {Array}  list  The list to check if the
     * @return {Boolean}      [description]
     */
    isInList: function (list) {
      return _.find(list, function (existingOperation) {
        // Remove safeguard once server-side has correct IDs
        if (this._idTokens && existingOperation._idTokens) {
          return this._idTokens[1] === existingOperation._idTokens[1] &&
                 this._idTokens[3] === existingOperation._idTokens[3];
        }
      }, this);
    }
  });

  Operation.prototype.constructor = Operation;

  // IDs for well known operations
  /**
   *
   * @static
   * @property {String} GROUP_BY_ID Id of the GROUP_BY operation
   */
  Operation.GROUP_BY_ID = '/loom/loom/GROUP_BY';

  /**
   * @static
   * @property {String} SORT_BY_ID Id of the SORT_BY operation
   */
  Operation.SORT_BY_ID = '/loom/loom/SORT_BY';

  /**
   * @static
   * @property {String} BRAID_ID Id of the BRAID operation
   */
  Operation.BRAID_ID = '/loom/loom/BRAID';

  /**
   * @static
   * @property {String} KMEANS_ID Id of the KMEANS operation
   */
  Operation.KMEANS_ID = '/loom/loom/KMEANS';

  /**
   * @static
   * @property {String} FILTER_STRING_ID Id of the FILTER_STRING operation
   */
  Operation.FILTER_STRING_ID = '/loom/loom/FILTER_STRING';

  /**
   * @static
   * @property {String} FILTER_RELATED_ID Id of the FILTER_RELATED operation
   */
  Operation.FILTER_RELATED_ID = '/loom/loom/FILTER_RELATED';

  /**
   * @static
   * @property {String} FILTER_BY_REGION_ID Id of the FILTER_BY_REGION operation
   */
  Operation.FILTER_BY_REGION_ID = '/loom/loom/FILTER_BY_REGION';

  /**
   * @static
   * @property {String} GRID_CLUSTERING_ID Id of the GRID_CLUSTERING operation
   */
  Operation.GRID_CLUSTERING_ID = '/loom/loom/GRID_CLUSTERING';

  /**
   * @static
   * @property {String} POLYGON_CLUSTERING_ID Id of the POLYGON_CLUSTERING operation
   */
  Operation.POLYGON_CLUSTERING = '/loom/loom/POLYGON_CLUSTERING';

  /**
   * @static
   * @property {String} SUMMARY_ID Id of the SUMMARY operation
   */
  Operation.SUMMARY_ID = '/loom/loom/SUMMARY';

  return Operation;
});
