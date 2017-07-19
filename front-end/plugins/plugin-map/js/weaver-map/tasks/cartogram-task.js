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
"use strict";
define(function (require) {

  var d3 = require('cartogram-standalone');

  var cartogramTask = {

    init: function () {

      this.math = (function (d3) {

        var π = Math.PI;

        function miller(λ, φ) {
          return [
            λ,
            1.25 * Math.log(Math.tan(π / 4 + 0.4 * φ))
          ];
        }

        miller.invert = function (x, y) {
          return [
            x,
            2.5 * Math.atan(Math.exp(0.8 * y)) - 0.625 * π
          ];
        };

        var millerProjection;
        (millerProjection = function () { return d3.geo.projection(miller); }).raw = miller;

        var math = {
          d3Miller: millerProjection
        };

        return math;
      })(d3);

      this.carto = d3.cartogram();
    },

    run: function () {
      if (arguments.length > 1) {
        return this._classicRun.apply(this, arguments);
      } else {
        return this._pointsRun.apply(this, arguments);
      }
    },

    _classicRun: function (config, topology, geometries, points, values) {

      this._setProjection(config);
      this.carto.projection(this.proj);

      this.carto.properties(function (d) {
        return {
          countryName: d.name,
          nbItems: values[d.name]
        };
      });
      this.carto.value(function (d) {
        var value = values[d.properties.countryName];
        return value ? value : 1;
      });
      var result = this.carto(topology, geometries, points);
      return result;

    },

    _pointsRun: function (points) {

    },

    _setProjection: function (config) {
      var width  = config.width;              // width of the map.
      var height = config.height;             // height of the map.
      var offset = -height / 7;

      this.proj = this.math.d3Miller()
        .rotate([-11, 0])
        .scale((width + 1) / 2 / Math.PI)
        .translate([width / 2, height / 2 - offset])
        .precision(0.1);
    }
  };

  return cartogramTask;
});