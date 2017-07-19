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

  _.dirty = function (f, thisArg) {
    var value = f.call(thisArg);

    var dirtyF = function () {
      if (thisArg.___lodashCache[dirtyF.toString()]) {
        value = f.call(thisArg);
      }
      return value;
    };

    if (!thisArg.___lodashCache) {
      thisArg.___lodashCache = {};
    }

    thisArg.___lodashCache[dirtyF.toString()] = false;

    return dirtyF;
  };

  _.setDirty = function (dirtyF, thisArg) {
    thisArg.___lodashCache[dirtyF.toString()] = true;
  };

  _.safeCall = function (f, thisArg) {
    _.noop(f && _.bind(f, thisArg)());
  };

  _.fwdEventOnPreventDefault = function (f, thisArg) {
    return function (event) {
      if (!event.isDefaultPrevented()) {
        event.preventDefault();
        if (thisArg) {
          f.apply(thisArg, arguments);
        } else {
          f.apply(this, arguments);
        }
      }
    };
  };
});