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
  var d3 = require('d3');
  var MapComponent = require('./MapComponentV2');
  var Aggregation = require('weft/models/Aggregation');

  // var taskRunner = require('task!weaver-map/tasks/cartogram-task');

  var DistortionController = MapComponent.extend({

    events: {
      'pointsUpdated': function () {
        clearTimeout(this.timeoutId);
        this.timeoutId = setTimeout(_.bind(this.recomputeDistortion, this), 100);
      },
      'recomputeDistortion': 'recomputeDistortion',
      'applyDistortion': 'applyDistortion',
      'revertDistortion': 'revertDistortion',
      'add:(thread,fibre)': '_addFibreToCountry',
      'removeAll:(thread)': '_removeAll',
    },

    initialize: function () {

      this.__nbItemsPerCountryPerThread = {};

      this.maxValue = 200;            // max value (scaled) for the deformation
      this.minValue = 1;              // min value (scaled) for the deformation

    },

    initializeWhenAttached: function () {

      // Points management:
      //
      //   When the projection change we need to either update the distortion
      //   or update the current position of markers by using the projection.
      //
      this.listenTo(this.mapData, 'change:projection', _.throttle(
        _.bind(this.refreshPoints, this),
        20
      ));
    },

    getNbItemsPerCountry: function () {

      return _.reduce(_.values(this.__nbItemsPerCountryPerThread), function (result, threadData) {
        _.forEach(threadData, function (value, country) {
          result[country] = result[country] || 0;
          result[country] += value;
        });
        return result;
      }, { 'default': 0 });
    },

    /**
     * This function force the update of points' positions.
     * It is particularly usefull when the map size has changed.
     */
    refreshPoints: function () {

      if (this.isDistortionApplied) {

        this.recomputeDistortion();
      } else {

        // We reproject all points
        var points = this.mapData.getPoints();
        var projection = this.mapData.get('projection');

        points = _.map(points, function (point) {
          return projection(point);
        });

        points.isChangeInstant = true;

        this.mapData.setPoints(points);
      }
    },


    // ----------------------------------------------------------------------
    // DISTORTION MANAGEMENT
    //

    recomputeDistortion: function () {

      this.computeNewDistortion(this.getNbItemsPerCountry());
    },

    computeNewDistortion: function (values) {

      var u = this.mapData;
      var topology = this.map.topology;
      var geometries = this.map.geometries;

      var lo = _.min(values);
      var hi = _.max(values);
      var scale = d3.scale.linear()
        .domain([lo, hi])
        .range([this.minValue, this.maxValue]);

      var scaledValues = {};

      _.forEach(geometries, function (country) {

        var value = values[country.name];
        scaledValues[country.name] = scale(value || 0);
      });

      var config = { width: u.mapWidth(), height: u.mapHeight() };
      var points = u.getPoints();

      taskRunner.runTaskWith(config, topology, geometries, points, scaledValues)
        .then(_.bind(this._cacheDistortion, this))
        .done();
    },

    _cacheDistortion: function (data) {

      var features = data.features;
      var points = data.points;
      var extents = data.extents;

      this.pointsDistortion = points;
      this.featuresDistortion = features;
      this.extentsDistortion = extents;

      if (this.isDistortionApplied) {

        this.applyDistortion(points, features, extents);
      }
    },

    applyDistortion: function (points, features, extents) {

      points = points || this.pointsDistortion;
      features = features || this.featuresDistortion;
      extents = extents || this.extentsDistortion;

      // We need a valid context to really modify the
      // environment (map + points)
      if (points && features && extents) {

        points.isChangeInstant = false;
        this.mapData.setPoints(points);
        this.map.applyDistortion(features, extents);
        this.hasDistortionComplete = true;
      }

      // Apply distortion has been called
      // so when the _cacheDistortion returns we will apply
      // the distortion
      this.isDistortionApplied = true;
    },

    revertDistortion: function () {

      if (this.hasDistortionComplete) {
        this.map.resetMap();
        this.hasDistortionComplete = false;
      }
      this.isDistortionApplied = false;
    },

    // ----------------------------------------------------------------------
    // FIBRES COUNTERS
    //

    _addFibreToCountry: function (thread, fibre) {

      var threadId = this._generateId(thread);

      var threadData = this.__nbItemsPerCountryPerThread[threadId] = this.__nbItemsPerCountryPerThread[threadId] || {};

      if (fibre instanceof Aggregation) {

        if (thread.isGrouped('country')) {
          this._addAggregationToCountry(threadData, fibre);
        }
      } else {

        this._addItemToCountry(threadData, fibre);
      }
    },

    _addItemToCountry: function (threadData, item) {

      if (!threadData[item.attributes.country]) {
        threadData[item.attributes.country] = 0;
      }
      threadData[item.attributes.country] += 1;
    },

    _addAggregationToCountry: function (threadData, aggregation) {

      if (!threadData[aggregation.attributes.name] &&
           this._isNotBraided(aggregation)) {
        threadData[aggregation.attributes.name] = aggregation.attributes.numberOfItems;
      } else {
        console.error("Aggregation is not a country !");
      }
    },

    _removeAll: function (thread) {

      var threadId = this._getId(thread);
      delete this.__nbItemsPerCountryPerThread[threadId];
    },

    _isNotBraided: function (aggregation) {

      return aggregation.attributes['l.tags'] !== 'braid';
    },


    _generateId: function (thread) {

      if (!thread.__idDistortionController) {
        thread.__idDistortionController = _.uniqueId('DistortionController');
      }
      return thread.__idDistortionController;
    },

    _getId: function (thread) {

      return thread.__idDistortionController;
    }

  });

  return DistortionController;
});
