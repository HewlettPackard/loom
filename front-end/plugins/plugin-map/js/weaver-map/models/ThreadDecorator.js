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

  var Thread = require('weft/models/Thread');
  var Query = require('weft/models/Query');


  var DisplayMode = require('plugins/common/utils/DisplayMode');
  var ClusterOperation = require('weaver-map/models/ClusterOperation');
  var GeoAttributes = require('weaver-map/models/GeoAttributes');
  var Operation = require('weft/models/Operation');

  var originalInitialize = Thread.prototype.initialize;
  var originalDefaults = Thread.prototype.defaults;

  var _parseCluster = function (itemType, operation) {

    return new ClusterOperation({
      operator: operation,
      attributes: _.reduce(itemType.operations[operation], function (itemClusterBy, attributeId) {

        var attributeDefinition = itemType.attributes[attributeId];
        itemClusterBy[attributeId] = attributeDefinition ? attributeDefinition.name || attributeId : attributeId;
        return itemClusterBy;
      }, {}),
    });
  };

  var _parseGeoAttributes = function (itemType) {

    return _.reduce(itemType.geoAttributes, function (geolocations, couple) {
      var lat = itemType.attributes[couple.latitude];
      var lon = itemType.attributes[couple.longitude];
      if (lat && lon && lat.plottable && lon.plottable) {
        geolocations.addMappingFor(couple.longitude, couple.latitude);
      }
      return geolocations;
    }, new GeoAttributes());
  };

  Thread.prototype.initialize = function () {
    originalInitialize.apply(this, arguments);
    this.getGeoAttributes = _.memoize(Thread.prototype.getGeoAttributes)
  }

  Thread.prototype.getGeoAttributes = function () {

    return _parseGeoAttributes(this.get('itemType'));
  };

  Thread.prototype.createNestedThreadQuery = function (aggregation) {
    var limit = {
        operator: Operation.BRAID_ID,
        parameters: {
          maxFibres: 30
        }
    };

    if (aggregation.get('displayMode') === DisplayMode.MAP) {

      var kmeansAttributes = this.getAttributesForOperation('KMEANS');
      if (!_.isEmpty(kmeansAttributes)) {
        limit.operator = Operation.KMEANS_ID;
        limit.parameters.attributes = _.keys(kmeansAttributes);
        limit.parameters.k = 15;
      }
    }

    return new Query({
      inputs: [aggregation.get('l.logicalId')],
      limit: limit
    });
  };

});
