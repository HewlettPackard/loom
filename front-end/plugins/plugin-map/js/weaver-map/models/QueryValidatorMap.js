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
  var QueryValidator = require('plugins/common/models/QueryValidator');
  var DisplayMode = require('plugins/common/utils/DisplayMode');

  var MapDataManager = require('weaver-map/models/map/MapDataManager');
  var AttributesOperationBuilder = require('weaver-map/models/AttributesOperationBuilder');

  /**
   * This is the QueryValidator dedicated for the map.
   * On creation, it replace the query with one appropriate for map visualization.
   * Then it is called whenever the query is being modified, to avoid having problematic
   * queries.
   *
   * @param {MapViewElement} options.map is a mandatory options needed to access map related information.
   */
  var QueryValidatorMap = extend.call(QueryValidator, {

    initialize: function (options) {

      this.mapData = MapDataManager.get(options.map);

      this._initFactoryOperations();
      this._configThread();
    },

    _initFactoryOperations: function () {

      this.filterByRegion = !_.isEmpty(this.thread.getAttributesForOperation(Operation.FILTER_BY_REGION_ID));
      this.kmeans = !_.isEmpty(this.thread.getAttributesForOperation(Operation.KMEANS_ID));
      this.gridclustering = !_.isEmpty(this.thread.getAttributesForOperation(Operation.GRID_CLUSTERING_ID));
    },

    _configThread: function () {

      var query = this.thread.get('query');

      // We add a group by if there's no operation.
      if (!this.thread.hasOperation()) {
        query = this._addGroupByCountry(query);
      }

      // We explicitly remove the limit,
      // The validation will take care of adding the appropriate limit.
      query = query.limitWith(undefined);

      // The modification we've made is equivalent to
      // a change within the editor. So we need to
      // perform a query validation before setting the new
      // query to the thread.
      query = this.validateQuery(query);

      this.thread.set('query', query);
    },

    _addGroupByCountry: function (query) {

      // Add the group by country
      query = query.pushOperation({
        operator: Operation.GROUP_BY_ID,
        parameters: {
          property: 'country',
        }
      });

      return query;
    },

    _addLimitToQuery: function (query) {

      if (this.filterByRegion && this.gridclustering) {

        // Attach new operator:
        if (!query.hasOperation(Operation.FILTER_BY_REGION_ID)) {
          query = query.unshiftOperation(this._generateFilterByRegionOperation());
        }

        if (!query.hasLimit(Operation.GRID_CLUSTERING_ID) || !this._isValidGridClustering(query.getLimit())) {
          query = query.limitWith(this._generateGridClusteringOperation());
        }

      } else if (this.kmeans) {

        if (!query.hasLimit(Operation.KMEANS_ID)) {

          query = query.limitWith(this._generateKmeansOperation());
        }

      } else {

        // We make it clear that something is missing from the itemType to be cleanly
        // shown on the map
        query = query.limitWith({
          operator: Operation.SUMMARY_ID,
          parameters: {}
        });
      }

      return query;
    },

    _validateMapQuery: function (query) {

      // Check that the user didn't set a grid clustering without at least a filter by region.
      if (this.filterByRegion && this.gridclustering) {

        // Do we have a grid clustering ?
        if (!query.hasLimit() || query.hasLimit(Operation.GRID_CLUSTERING_ID)) {

          if (!query.hasOperation(Operation.FILTER_BY_REGION_ID)) {
            query = query.unshiftOperation(this._generateFilterByRegionOperation());
          }


          // As grid clustering can be only used in auto mode,
          // and it can be a fresh new operation switch we set the parameters
          // for that limit.
          query = query.limitWith(this._generateGridClusteringOperation());
        }
      }

      // If the user has chosen a kmeans operation
      // we check it has set a proper maxFibre
      if (this.kmeans) {

        if (!query.hasLimit() || query.hasLimit(Operation.KMEANS_ID)) {

          var kmeans = query.getLimit() || this._generateKmeansOperation();

          if (!kmeans.parameters.maxFibres) {

            query = query.limitWith(this._generateKmeansOperation());
          }
        }
      }

      // If the query has still no limit
      query = QueryValidator.prototype.validateQuery(query);

      return query;
    },

    _generateFilterByRegionOperation: function () {

      var filterByRegionAttributes = _.keys(this.thread.getAttributesForOperation(Operation.FILTER_BY_REGION_ID));
      var filterByRegion = AttributesOperationBuilder({
        operator: Operation.FILTER_BY_REGION_ID,
        attributes: filterByRegionAttributes,
        parameters: {
          attributes: filterByRegionAttributes
        }
      })
        .createArrayFromAttributes('maximums', function (attributeName) {
          return this.mapData.getLoomMaximums(this.thread.getGeoAttributes().getGeoMeaning(attributeName));
        }, this)
        .createArrayFromAttributes('minimums', function (attributeName) {
          return this.mapData.getLoomMinimums(this.thread.getGeoAttributes().getGeoMeaning(attributeName));
        }, this)
        .build();

      return filterByRegion;
    },

    _isValidGridClustering: function (operation) {
      return operation.operator === Operation.GRID_CLUSTERING_ID &&
             _.isArray(operation.parameters.translations) &&
             _.isArray(operation.parameters.deltas) &&
             _.isArray(operation.parameters.attributes) &&
             operation.parameters.translations.length === operation.parameters.deltas.length;
    },

    _generateGridClusteringOperation: function () {

      var gridClusteringAttributes = _.keys(this.thread.getAttributesForOperation(Operation.GRID_CLUSTERING_ID));
      var gridclustering = AttributesOperationBuilder({
        operator: Operation.GRID_CLUSTERING_ID,
        attributes: gridClusteringAttributes,
        parameters: {
          attributes: gridClusteringAttributes
        }
      })
        .createArrayFromAttributes('translations', function (attributeName) {
          return this.mapData.getLoomOffset(this.thread.getGeoAttributes().getGeoMeaning(attributeName));
        }, this)
        .createArrayFromAttributes('deltas', function (attributeName) {
          return this.mapData.getLoomDelta(this.thread.getGeoAttributes().getGeoMeaning(attributeName));
        }, this)
        .build();

      return gridclustering;
    },

    _generateKmeansOperation: function () {
      var kmeansAttributes = _.keys(this.thread.getAttributesForOperation(Operation.KMEANS_ID));
      var kmeans = AttributesOperationBuilder({
        operator: Operation.KMEANS_ID,
        attributes: kmeansAttributes,
        parameters: {
          attributes: kmeansAttributes
        }
      })
        .setRawParameter('maxFibres', 20)
        .build();

      return kmeans;
    },

    validateQuery: function (query) {

      var groupByCountry = query.getOperation(Operation.GROUP_BY_ID);

      if (this.thread.get('displayMode') === DisplayMode.MAP &&
          groupByCountry.length > 0 &&
          groupByCountry[groupByCountry.length - 1].parameters.property === 'country')
      {
        if (query.hasLimit()) {
          query = query.limitWith(undefined);
        }

        if (query.hasOperation(Operation.FILTER_BY_REGION_ID)) {
          query = query.removeOperations(Operation.FILTER_BY_REGION_ID);
        }

        // Stop the thread merging:
        this.thread.stopThreadResultMerging();

      } else {

        query = this._validateMapQuery(query);

        // Restart merging
        this.thread.startThreadResultMerging();
      }

      return query;
    },
  });


  return QueryValidatorMap;
});
