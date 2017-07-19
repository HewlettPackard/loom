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
  var MapElementListenToModel = require('./MapElementListenToModelV2');
  var BBox = require('./BBox');

  var MapMarker = MapElementListenToModel.extend({

    events: _.defaults({
      'mousemove': '_handleOverMarker',
    }, MapElementListenToModel.prototype.events),

    initialize: function () {
      MapElementListenToModel.prototype.initialize.apply(this, arguments);
    },

    _handleOverMarker: function (event) {
      d3.event = event;
      var markerData = this.model.attributes;
      this.__dispatchEvent(event, 'info:element', markerData);
    },

    setDatum: function (d) {
      this.localBBox = {
        'x': d.dx,
        'y': d.dy,
        'width': d.r * 2,
        'height': d.r * 2
      };
    },

    getLocalBBox: function () {
      return this.localBBox;
    },

  });

  BBox.assignTo(MapMarker);

  return MapMarker;
});