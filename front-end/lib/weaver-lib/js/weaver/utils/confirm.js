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

  var Q = require('q');

  return {
    /**
     * Because Windows 8 apps don't have window.confirm, we need an asynchronous wrapper that matches
     * @module weaver
     * @submodule utils
     * @namespace utils
     * @method confirm
     * @param {String} message The confirmation message sent to the user
     */
    confirm: function (message) {
      var deferred = Q.defer();
      var result = confirm(message);
      deferred.resolve(result);
      return deferred.promise;
    }
  };
});
