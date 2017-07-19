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
  var MapComponent = require('./MapComponentV2');

  var ClusterGrid = MapComponent.extend({

    events: {
      'change:zoom': 'zoomHasChanged',
      'change:pan': 'panHasChanged',
    },

    initialize: function () {

      this.__ratiosValues = {};
    },

    computeFilterRegion: function (stepLng, stepLat, offsetLng, offsetLat) {

      var u = this.mapData;
      
      if (_.isUndefined(this.nbLng) || _.isUndefined(this.nbLat)) {
        this.computeGrid();
      }

      if (this.nbLng === u.get('stepCameraLng') && this.nbLat === u.get('stepCameraLat')) {
        this.updateRegion(-180, 90, 360, 180);
      } else {
        this._doComputeFilterRegion(stepLng, stepLat, offsetLng, offsetLat);
      }
    },

    _doComputeFilterRegion: function (stepLng, stepLat, offsetLng, offsetLat) {

      var u = this.mapData;

      var cameraTopLeft     = [0, 0];
      var cameraBottomRight = [u.mapWidth(), u.mapHeight()];

      cameraTopLeft     = u.convertFromSVGSpaceToLngLatSpace(u.convertFromPxSpaceToSVGSpace(cameraTopLeft));
      cameraBottomRight = u.convertFromSVGSpaceToLngLatSpace(u.convertFromPxSpaceToSVGSpace(cameraBottomRight));

      var cx = + (this.computeCellIndex('cx', (cameraTopLeft[0] - offsetLng) / stepLng, Math.floor) - 1) * stepLng + offsetLng;
      var cy = - (this.computeCellIndex('cy', (offsetLat - cameraTopLeft[1]) / stepLat, Math.floor) - 1) * stepLat + offsetLat;

      cx = this.clamp(cx, 179, -179);
      cy = this.clamp(cy,  89,  -89);

      var width  = (Math.ceil((cameraBottomRight[0] - cameraTopLeft[0]) / stepLng) + 3) * stepLng;
      var height = (Math.ceil((cameraTopLeft[1] - cameraBottomRight[1]) / stepLat) + 3) * stepLat;

      width  = this.clamp(width, 180 - cx);
      height = this.clamp(height, 90 + cy);

      this.updateRegion(cx, cy, width, height);
    },

    updateRegion: function (cx, cy, width, height) {
      this.mapData.set('filterRegion', {
        'maximums': [cx + width, cy],
        'minimums': [cx, cy - height],
      });
    },

    clamp: function (value, max, min) {
      if (!_.isUndefined(max)) {
        value = value > max ? max: value;
      }
      if (!_.isUndefined(min)) {
        value = value < min ? min: value;
      }
      return value;
    },

    computeCellIndex: function (id, ratio, closeInteger) {

      var index = closeInteger(ratio);
      if (id in this.__ratiosValues) {
        
        if (this.__ratiosValues[id] > index || this.__ratiosValues[id] + 1 < index) {
          this.__ratiosValues[id] = index;
          return index;
        } else {
          return this.__ratiosValues[id];
        }
      }

      this.__ratiosValues[id] = index;
    
      return index;
    },

    computeGrid: function () {
      var u = this.mapData;

      var scale = u.getZoomCurrentScale();

      var cameraLngWidth  = 358 / scale;
      var cameraLatHeight = 178 / scale;

      var lngExtent = 360;
      var latExtent = 180;

      this.nbLng = u.get('stepCameraLng') * (Math.abs(Math.round(lngExtent / cameraLngWidth)) || 1);
      this.nbLat = u.get('stepCameraLat') * (Math.abs(Math.round(latExtent / cameraLatHeight)) || 1);

      var stepLng = lngExtent / this.nbLng;
      var stepLat = latExtent / this.nbLat;

      var tmp = u.convertFromSVGSpaceToLngLatSpace([0, 0]);
      var offsetLng = tmp[0];
      var offsetLat = tmp[1];

      this.updateGrid(offsetLng, offsetLat, stepLng, stepLat);
    },

    zoomHasChanged: function () {
      
      // Recompute the grid:
      this.computeGrid();

      // Recompute the filter region:
      var grid = this.mapData.get('gridCluster');
      this.computeFilterRegion(grid.gridDelta[0], grid.gridDelta[1], -grid.gridOffset[0], -grid.gridOffset[1]);
    },

    panHasChanged: function () {

      // Recompute the region if needed.
      var grid = this.mapData.get('gridCluster');
      this.computeFilterRegion(grid.gridDelta[0], grid.gridDelta[1], -grid.gridOffset[0], -grid.gridOffset[1]);
    },

    updateGrid: function (offsetLng, offsetLat, stepLng, stepLat) {
      this.mapData.set('gridCluster', {
        'gridOffset': [-offsetLng, -offsetLat],
        'gridDelta': [stepLng, stepLat],
      });
    },
  });

  return ClusterGrid;
});