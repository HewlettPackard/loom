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

  /**
   * Mixin providing the bounding box support for view attached to svg element
   * @mixin BBox
   */
  var BBox = {

    assignTo: function (Class) {
      _.assign(Class.prototype, BBox, function (a, b) {
        return _.isUndefined(a) ? b : a;
      });
    },

    getCenter: function () {
      var bbox = this.getBBox();
      return [bbox.x + bbox.width / 2, bbox.y + bbox.height / 2];
    },

    getBBox: function () {
      var bbox = this.getLocalBBox();
      var ctm = this.getCTM();
      var x = ctm.a * bbox.x + ctm.c * bbox.y + ctm.e * 1;
      var y = ctm.b * bbox.x + ctm.d * bbox.y + ctm.f * 1;
      var width  = ctm.a * bbox.width + ctm.c * bbox.height;
      var height = ctm.b * bbox.width + ctm.d * bbox.height;

      return {'x': x, 'y': y, 'width': width, 'height': height};
    },

    getLocalBBox: function () {
      return this.el.getBBox();
    },

    getCTM: function () {
      return this.el.getCTM();
    },
  };

  return BBox;
});