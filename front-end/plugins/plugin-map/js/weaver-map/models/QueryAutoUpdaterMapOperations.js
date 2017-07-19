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
/*jshint -W064*/
define(function (require) {
  "use strict";

  var _ = require('lodash');
  var extend = require('backbone').View.extend;
  var Operation = require('weft/models/Operation');
  var QueryAutoUpdater = require('plugins/common/models/QueryAutoUpdater');
  var MapDataManager = require('weaver-map/models/map/MapDataManager');
  var AttributesOperationBuilder = require('weaver-map/models/AttributesOperationBuilder');

  /**
   * A QueryAutoUpdaterMapOperations as queryUpdater on a thread
   * automatically update the following map operations:
   *   - FILTER_BY_REGION
   *   - POLYGON_CLUSTERING
   *   - GRID_CLUSTERING
   *
   * @class QueryAutoUpdaterMapOperations
   * @namespace weaver-map.models
   * @module weaver-map
   */
  var QueryAutoUpdaterMapOperations = extend.call(QueryAutoUpdater, {

    creator: function (options) {

      QueryAutoUpdater.prototype.creator.apply(this, arguments);

      if (options.map) {

        this.mapData = MapDataManager.get(options.map);
      }

      if (options.mapData) {

        this.mapData = options.mapData;
      }

      this._attachEvents();
    },

    forceUpdate: function () {

      this._updateFilterByRegion(this.thread);
      this._updatePolygonClustering(this.thread);
      this._updateGridClustering(this.thread);
    },

    _attachEvents: function () {

      // ---------------------------------------------------------------------------
      // Pan changes:
      //

      this.listenTo(this.mapData, 'change:filterRegion', function () {

        // The update of the filter by region for the thread.
        this._updateFilterByRegion(this.thread);

        // The polygon clustering depend on the region in auto mode:
        this._updatePolygonClustering(this.thread);
      });

      // ---------------------------------------------------------------------------
      // Zoom changes:
      //

      // The update of the query when the scale has changed.
      this.listenTo(this.mapData, 'change:gridCluster', function () {

        // Update the grid clustering:
        this._updateGridClustering(this.thread);
      });
    },

    // ----------------------------------------------------------------------------------------
    // FILTER BY REGION -- auto
    _updateFilterByRegion: function (thread) {

      // update the filter region for the current thread.
      if (thread.hasOperation(Operation.FILTER_BY_REGION_ID)) {

        // Get a filterByRegion
        var filterByRegion = thread.getOperation(Operation.FILTER_BY_REGION_ID)[0];

        if (filterByRegion.parameters.complement) {
          // We update the filter in the case of a complement
          this._updateFilterByRegionComplementTrue(thread, filterByRegion);
        } else {
          // We update the filter in the case of the original region
          this._updateFilterByRegionComplementFalse(thread, filterByRegion);
        }
      }
    },

    _updateFilterByRegionComplementFalse: function (thread, filterByRegion) {

      // Recompute filter regions
      filterByRegion = AttributesOperationBuilder(filterByRegion)
        .createArrayFromAttributes('maximums', function (attributeName) {
          return this.mapData.getLoomMaximums(thread.getGeoAttributes().getGeoMeaning(attributeName));
        }, this)
        .createArrayFromAttributes('minimums', function (attributeName) {
          return this.mapData.getLoomMinimums(thread.getGeoAttributes().getGeoMeaning(attributeName));
        }, this)
        .build();

      thread.updateOperations(filterByRegion);
    },

    _updateFilterByRegionComplementTrue: function (thread, filterByRegion) {

      var complementFilterByRegion = AttributesOperationBuilder(filterByRegion)
        .createArrayFromAttributes('minimums', function (attributeName) {
          return this.mapData.getLoomMaximums(thread.getGeoAttributes().getGeoMeaning(attributeName));
        }, this)
        .createArrayFromAttributes('maximums', function (attributeName) {
          return this.mapData.getLoomMinimums(thread.getGeoAttributes().getGeoMeaning(attributeName));
        }, this)
        .setRawParameter('complement', true)
        .build();

      // TODO: updateOperations should accept a predicate that is passed to
      // test against to find the operation.
      thread.updateOperations(complementFilterByRegion);
    },

    // ----------------------------------------------------------------------------------------
    // GRID CLUSTERING -- auto
    _updateGridClustering: function (thread) {

      // Update the grid clustering if there's one.
      if (thread.hasLimit(Operation.GRID_CLUSTERING_ID)) {

        var gridclustering = thread.get('query').getLimit();

        // Recompute grid
        gridclustering = AttributesOperationBuilder(gridclustering)
          .createArrayFromAttributes('translations', function (attributeName) {
            return this.mapData.getLoomOffset(thread.getGeoAttributes().getGeoMeaning(attributeName));
          }, this)
          .createArrayFromAttributes('deltas', function (attributeName) {
            return this.mapData.getLoomDelta(thread.getGeoAttributes().getGeoMeaning(attributeName));
          }, this)
          .build();


        thread.limitWith(gridclustering);
      }
    },

    // ----------------------------------------------------------------------------------------
    // POLYGON CLUSTERING -- auto
    _updatePolygonClustering: function (thread) {

      // Update the polygon clustering if there's one.
      if (thread.hasLimit(Operation.POLYGON_CLUSTERING_ID)) {

        var cameraPolygonClustering = thread.get('query').getLimit();

        var geoAttributes = thread.getGeoAttributes();
        var lngLatOrder = _.map(cameraPolygonClustering.parameters.attributes,
          _.bind(geoAttributes.getGeoMeaning, geoAttributes)
        );

        var mapToAppropriate = function (obj) {
          return _.map(lngLatOrder, function (lngOrLat) { return obj[lngOrLat](lngOrLat); });
        };
        var camMin = _.bind(this.mapData.getLoomMinimums, this.mapData);
        var camMax = _.bind(this.mapData.getLoomMaximums, this.mapData);
        var mapbounds = {
          min: {
            longitude: -180,
            latitude: -90,
          },
          max: {
            longitude: 180,
            latitude: 90
          }
        };
        var mapMin = function (lngOrLat) { return mapbounds.min[lngOrLat]; };
        var mapMax = function (lngOrLat) { return mapbounds.max[lngOrLat]; };

        var mapTopLeft  = mapToAppropriate({
          longitude: mapMin,
          latitude:  mapMin,
        });
        var mapTopRight = mapToAppropriate({
          longitude: mapMax,
          latitude:  mapMin,
        });
        var mapBotLeft  = mapToAppropriate({
          longitude: mapMin,
          latitude:  mapMax,
        });
        var mapBotRight = mapToAppropriate({
          longitude: mapMax,
          latitude:  mapMax,
        });

        var camTopLeft  = mapToAppropriate({
          longitude: camMin,
          latitude:  camMin,
        });
        var camTopRight = mapToAppropriate({
          longitude: camMax,
          latitude:  camMin,
        });
        var camBotLeft  = mapToAppropriate({
          longitude: camMin,
          latitude:  camMax,
        });
        var camBotRight = mapToAppropriate({
          longitude: camMax,
          latitude:  camMax,
        });

        // Convention chosen:
        //
        //  1 *--------------------* 5
        //    |   2 *------* 6     |
        //    |     |Camera|       |
        //    |   3 *------* 8     |
        //    |        MAP         |
        //  4 *--------------------* 7
        //
        var _1, _2, _3, _4, _5, _6, _7, _8;

        /*jshint -W015 */
        _1 = mapTopLeft;  _4 = mapBotLeft;
        _2 = camTopLeft;  _3 = camBotLeft;

        _5 = mapTopRight;  _7 = mapBotRight;
        _6 = camTopRight;  _8 = camBotRight;
        /*jshint +W015 */

        cameraPolygonClustering = AttributesOperationBuilder(cameraPolygonClustering)
          .setRawParameter('polygons', [
            [_1, _2, _3, _4], [_1, _5, _6, _2],
            [_5, _7, _8, _6], [_3, _8, _7, _4],
          ])
          .build();

        thread.limitWith(cameraPolygonClustering);
      }
    },

  });

  return QueryAutoUpdaterMapOperations;

});
