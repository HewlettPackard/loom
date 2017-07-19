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
/**
 * @module weaver
 * @submodule utils
 * @namespace utils
 */
define(function (require) {
  "use strict";
  var d3 = require('d3');
  var π = Math.PI;

  /**
   * @class miller
   * @param λ
   * @param φ
   * @returns {*[]}
   */
  function miller(λ, φ) {
    return [
        λ,
        1.25 * Math.log(Math.tan(π / 4 + 0.4 * φ))
      ];
  }

  /**
   * @method invert
   * @param x
   * @param y
   * @returns {*[]}
   */
  miller.invert = function (x, y) {
    return [
        x,
        2.5 * Math.atan(Math.exp(0.8 * y)) - 0.625 * π
      ];
  };

  /**
   * Uses the d3.geo.projection method
   * @class millerProjection
   */
  var millerProjection;
  (millerProjection = function () {
    return d3.geo.projection(miller);
  }).raw = miller;

  /**
   * @class math
   * @type {{distance: math.distance, distanceA: math.distanceA, transform: math.transform, min: math.min, max: math.max, d3Miller: *}}
   */
  var math = {
    /**
     * @method distance
     * @param a
     * @param b
     * @returns {number}
     */
    distance: function (a, b) {
      var dxSquared = (a.x - b.x) * (a.x - b.x);
      var dySquared = (a.y - b.y) * (a.y - b.y);
      return Math.sqrt(dxSquared + dySquared);
    },

    /**
     * @method distanceA
     * @param a
     * @param b
     * @returns {number}
     */
    distanceA: function (a, b) {
      var dx = a[0] - b[0];
      var dy = a[1] - b[1];
      return Math.sqrt(dx * dx + dy * dy);
    },

    /**
     * @method transform
     * @param matrix
     * @param point
     * @returns {*[]}
     */
    transform: function (matrix, point) {
      var x = matrix.a * point[0] + matrix.c * point[1] + matrix.e * 1;
      var y = matrix.b * point[0] + matrix.d * point[1] + matrix.f * 1;
      return [x, y];
    },

    /**
     * @method min
     * @param v1
     * @param v2
     * @returns {*[]}
     */
    min: function (v1, v2) {
      return [Math.min(v1[0], v2[0]), Math.min(v1[1], v2[1])];
    },

    /**
     * @method max
     * @param v1
     * @param v2
     * @returns {*[]}
     */
    max: function (v1, v2) {
      return [Math.max(v1[0], v2[0]), Math.max(v1[1], v2[1])];
    },

    /**
     * @attribute d3Miller
     */
    d3Miller: millerProjection
  };

  return math;
});
