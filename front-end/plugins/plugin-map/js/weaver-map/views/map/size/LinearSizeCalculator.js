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
  
  var LinearSizeCalculator = (function () {
    
    function LinearSizeCalculator(values, sizeMin, sizeMax) {
      
      values = _.sortBy(values);
      
      this.x0 = values[0];
      this.xn = values[values.length - 1];
      this.Δ = this.xn - this.x0;
      
      this.sizeMax = sizeMax;
      this.sizeMin = sizeMin;
    }
    
    LinearSizeCalculator.prototype.applyWith = function (x) {
      return (x - this.x0) * (this.sizeMax - this.sizeMin) / this.Δ + this.sizeMin;
    };
    
    return LinearSizeCalculator;
  })();
  
  return function (options) {
    return new LinearSizeCalculator(options.values, options.sizeMin, options.sizeMax);
  }
});