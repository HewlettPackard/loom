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
  "use strcit";
  
  var CacheHelper = (function () {
    
    function CacheHelper(cachedComputer) {
      
      this._cachedValues = {};
      this._cachedComputer = cachedComputer;
    };
    
    CacheHelper.prototype.applyWith = function (x) {
      if (!_.has(this._cachedValues, x)) {
        this._cachedValues[x] = this._cachedComputer.applyWith(x);
      }
      return this._cachedValues[x];
    };
    
    CacheHelper.prototype.clearCache = function () {
      this._cachedValues = {};
    };
    
    return CacheHelper;
  })();
  
  return CacheHelper;
});