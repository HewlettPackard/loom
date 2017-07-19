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

  var DisplayMode = require('plugins/common/utils/DisplayMode');
  var Operation = require('weft/models/Operation');

  function MapQueryCleaner() {

  }

  MapQueryCleaner.prototype.transition = function (query, to_display_mode) {
    if (to_display_mode !== DisplayMode.MAP) {

      // Clear the pipeline of the query
      query = query.removeOperations(Operation.FILTER_BY_REGION_ID);

      // We set a braid limit, then the QueryAutoUpdaterBraiding
      // will update the braid value.
      query = query.limitWith({
        'operator': Operation.BRAID_ID
      });
    }

    return query;
  };

  return MapQueryCleaner;
});
