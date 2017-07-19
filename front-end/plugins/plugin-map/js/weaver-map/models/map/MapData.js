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

  var assert = require('assert');
  var _ = require('lodash');

  var MatAff2x3 = require('./MatAff2x3');
  var Backbone = require('backbone');

  var MapData = Backbone.Model.extend({

    defaults: function () {

      return {
        points: {},

        zoom: {
          currentScale: 1,
          currentTranslate: [0, 0],
        },

        transitionDuration: 0,

        mapIsReady: false,

        filterRegion: {

          minimums: [-180, -90],

          maximums: [+180, +90],

        },

        gridCluster: {

          gridOffset: [169, -84],

          gridDelta: [36, 36],

        },

        stepCameraLng: 10,
        stepCameraLat: 5,

        projection: undefined,
      };
    },

    initialize: function (options) {

      // Arguments checking
      if (options.map) {
        this.set('transitionDuration', this.get('map').transitionDuration);
      }
    },

    clone: function () {
      return new MapData({
        points: {},
        zoom: _.cloneDeep(this.get('zoom')),
        transitionDuration: this.get('transitionDuration'),
        mapIsReady: false,
        filterRegion: _.cloneDeep(this.get('filterRegion')),
        gridCluster: _.cloneDeep(this.get('gridCluster')),
        stepCameraLng: this.get('stepCameraLng'),
        stepCameraLat: this.get('stepCameraLat'),
      });
    },

    remove: function () {

      this.set('points', {});
    },

    ///////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ////////////////////////////////////////////////////////////////////////////////
    
    registerPoints: function (id, callbackGet, callbackSet, thisArg) {
      var get, set;
      if (thisArg) {
        get = _.bind(callbackGet, thisArg);
        set = _.bind(toSetFunction(callbackSet), thisArg);
      } else {
        get = callbackGet;
        set = toSetFunction(callbackSet);
      }
      this.get('points')[id] = {
        get: get,
        set: set,
      };
    },

    unregisterPoints: function (id) {
      delete this.get('points')[id];
    },

    setPoints: function (points) {
      _.forEach(this.get('points'), function (value) {
        if (value.isArrayOfPoints) {
          value.set(range(points, value.firstIndex, value.lastIndex));
        } else if (value.isPoint) {
          value.set(points[value.firstIndex]);
        }
      });
    },

    getPoints: function () {
      var points = [];
      _.forEach(this.get('points'), function (value) {
        
        var res;
        value.firstIndex = points.length;

        if (_.isFunction(value.get)) {
          res = value.get();
        } else {
          res = value.get;
        }

        if (_.isArray(res) && _.isArray(res[0])) {
          pushAll(points, res);
          value.isArrayOfPoints = true;
        } else if (_.isArray(res) && res.length === 2) {
          points.push(res);
          value.isPoint = true;
        }
      
        value.lastIndex = points.length;
      });

      return points;
    },

    extents: function () {
      return this.get('map').getExtents();
    },

    mapWidth: function () {
      return this.get('map').width;
    },

    mapHeight: function () {
      return this.get('map').height;
    },

    mapSizes: function () {
      return [this.get('map').width, this.get('map').height];
    },

    getZoomCurrentScale: function () {
      return this.get('zoom').currentScale;
    },

    getZoomCurrentTranslate: function () {
      return this.get('zoom').currentTranslate;
    },

    setZoom: function (zoom) {
      this.get('zoom').currentScale = zoom.currentScale;
      this.get('zoom').currentTranslate = zoom.currentTranslate;
    },

    setProjection: function (projection) {
      this.set('projection', projection);
      invalidCacheResults(this);
    },

    convertFromPxSpaceToSVGSpace: function (coords /** [px,py] */) {
      var zoom = this.get('zoom');
      return [coords[0] / zoom.currentScale - zoom.currentTranslate[0] / zoom.currentScale,
              coords[1] / zoom.currentScale - zoom.currentTranslate[1] / zoom.currentScale];
    },

    convertFromSVGSpaceToPxSpace: function (coords /** [sx,sy] */) {
      var zoom = this.get('zoom');
      return [coords[0] * zoom.currentScale + zoom.currentTranslate[0],
              coords[1] * zoom.currentScale + zoom.currentTranslate[1]];
    },

    convertFromSVGSpaceToLngLatSpace: function (coords /** [sx, sy] */) {
      return this.get('projection').invert(coords);
    },

    convertFromLngLatSpaceToSVGSpace: function (coords /** [lng, lat] */) {
      return this.get('projection')(coords);
    },

    getCTM: function () {
      var s = this.getZoomCurrentScale();
      var t = this.getZoomCurrentTranslate();
      return new MatAff2x3(s, 0, 0, s, t[0], t[1]);
    },

    getCameraCenter: function (index) {
      if (index) {
        var coord = convertToCoord(index);
        return this.mapSizes()[coord] / 2.0;
      }
      var pos = [0, 0];
      pos[0] = this.get('map').width / 2.0;
      pos[1] = this.get('map').height / 2.0;
      return pos;
    },

    getCameraSize: function (index) {
      var coord = convertToCoord(index);
      return this.mapSizes()[coord];
    },

    getCameraSizes: function () {
      return this.mapSizes();
    },

    getLoomMaximums: function (index) {
      var coord = convertToCoord(index);
      return this.get('filterRegion').maximums[coord];
    },

    getLoomMinimums: function (index) {
      var coord = convertToCoord(index);
      return this.get('filterRegion').minimums[coord];
    },

    getLoomOffset: function (index) {
      var coord = convertToCoord(index);
      return this.get('gridCluster').gridOffset[coord];
    },

    getLoomDelta: function (index) {
      var coord = convertToCoord(index);
      return this.get('gridCluster').gridDelta[coord];
    },

    getMapBoundarySVGSpace: function () {
      return cacheResult('boundary', this._getMapBoundarySVGSpace, this);
    },

    ///////////////////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    // ////////////////////////////////////////////////////////////////////////////////
     
    _getMapBoundarySVGSpace: function () {
      var projection = this.get('projection');
      var center = projection.center();
      return {
        topleft:  projection([center[0] - 180, center[1] + 90]),
        botRight: projection([center[0] + 180, center[1] - 90])
      };
    },

  });

  function pushAll(array, values) {

    for (var i = 0; i < values.length; ++i) {
      array.push(values[i]);
    }
  }
  
  function range(array, firstIndex, lastIndex) {
    var res = [];
    _.forOwn(array, function(val, key) {
      if(isNaN(key)) {
        res[key] = val;
      }
    });
    for (var i = firstIndex; i < lastIndex; ++i) {
      res.push(array[i]);
    }
    return res;
  }

  function convertLatLngToIndex(name) {
    if (name === 'latitude') {
      return 1;
    } else if (name === 'longitude') {
      return 0;
    }
    assert(false, 'name is not valid: ' + name);
  }

  function convertToCoord(index) {
    var coord;
    if (_.isString(index)) {
      coord = convertLatLngToIndex(index);
    } else {
      coord = index;
    }

    return coord;
  }

  function invalidCacheResults(thisArg) {
    delete thisArg.__idCacheResults;
  }

  function toSetFunction(funcOrArray) {
    if (_.isFunction(funcOrArray)) {
      return funcOrArray;
    }

    return function (result) {
      for (var i = funcOrArray.length - 1; i >= 0; --i) {
        funcOrArray[i] = result[i];
      }
    };
  }

  function cacheResult(id, func, thisArg) {
    if (thisArg.__idCacheResults) {
      
      if (thisArg.__idCacheResults[id]) {
        return thisArg.__idCacheResults[id];
      }

    } else {
      
      thisArg.__idCacheResults = {};
    }

    thisArg.__idCacheResults[id] = func.apply(thisArg);
    return thisArg.__idCacheResults[id];
  }


  return MapData;
});