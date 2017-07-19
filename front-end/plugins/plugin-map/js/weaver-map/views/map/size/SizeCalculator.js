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
define (function (require) {
  "use strict";
  
  var _ = require('lodash');
  var ConstantSizeCalculator = require('./ConstantSizeCalculator');
  
  var SizeCalculator = (function () {
    
    function SizeCalculator(n, values, sizeMin, sizeMax, epsilon) {
      
      values = _.sortBy(values);
      this.x0 = values[0];
      this.xn = values[values.length - 1];
      this.Δ = (this.xn - this.x0) / n;

      // If this.Δ is too small we switch to
      // ConstantSizeCalculator.
      if (this.Δ < epsilon) {
        return new ConstantSizeCalculator({
          value: sizeMin
        });
      }
      
      this.H = new Array(n + 1);
      this.C = new Array(n);
      this.sizeMin = sizeMin;
      this.sizeMax = sizeMax;
      
      // Compute histogram
      this._computeHistogram(values);
      this._computeConstants();
    }
    
    SizeCalculator.prototype.applyWith = function (x) {
      var i = Math.min(Math.floor((x - this.x0) / this.Δ), this.C.length - 1);
      var xi = this.x0 + i * this.Δ;
      
      return (this.H[i+1] - this.H[i]) / ( 2 * this.Δ ) * ( x - xi ) * ( x - xi ) +
        this.H[i] * ( x - xi ) + this.C[i];
    };
    
    SizeCalculator.prototype._computeHistogram = function (values) {
      
      // Initialize histogram.
      for (var i = this.H.length - 1; i >= 0; --i) {
        this.H[i] = 0;
      }
      
      // Fill it.
      _.forEach(values.slice(0, values.length - 1), function (x) {
        var i = Math.floor((x - this.x0) / this.Δ);
        this.H[i] += 1;
      }, this);
      
      this.H[this.H.length - 2] += 1; // last value in values

      // Normalize it.
      var factor = _.reduce(this.H.slice(1, this.H.length - 1), function (sum, value) {
        return sum + value;
      }, 0, this)
      factor += (this.H[0]) / 2;
      
      factor = (this.sizeMax - this.sizeMin) / (this.Δ * factor);
      
      this.H = this.H.map(function (value) { return value * factor; });
    };
    
    SizeCalculator.prototype._computeConstants = function ()  {
      
      this.C[0] = this.sizeMin;
      
      for (var i = 1; i < this.C.length; ++i) {
        this.C[i] = this.C[i-1] + this.Δ * (this.H[i] + this.H[i-1]) / 2;
      }
    };
    
    return SizeCalculator;
  })();
  
  return function (options) {
    return new SizeCalculator(options.n, options.values, options.sizeMin, options.sizeMax, options.epsilon);
  }
});