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

  var Aggregation = require('weft/models/Aggregation');

  Aggregation.prototype.hasAnyDisplayablePropertyChanged = function () {
    var displayableProperties = {
      'numberOfItems' : 'Number of items',
      'alertCount': 'Number of alerts',
      'deletedCount': 'Deleted items',
      'updatedCount': 'Updated items',
      'addedCount': 'New items',
    };
    return !!_(this.changed).keys().find(function (key) {

      return displayableProperties.hasOwnProperty(key);
    });
  };
});