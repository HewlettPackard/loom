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
define(function(require) {

    var _ = require('lodash');
    var math = require('weaver/utils/math');

    var ClusterContainer = function ClusterContainer(options) {
        this.options = _.defaults(options || {}, {
            //default value.
            tolerance: 10,
            distance: math.distance
        });
        this._clusters = [];
        this._clusterTolerance = this.options.tolerance;
        this._clusterData = [];
        this._clusterResolution = 1;
        this._addToData = true;
        this._distance = this.options.distance;
        this._mergeAttributes = this.options.mergeAttributes;
    };

    _.extend(ClusterContainer.prototype, {
        add: function(point) {

            if (this._addToData)
                this._clusterData.push(point);

            // look for an existing cluster for the new point
            cluster = this._clusterFindAndMerge(point, _.bind(this._clusterTest, this),
                _.bind(this._clusterAddPoint, this));

            if (!_.isUndefined(cluster)) {
                return cluster;
            }

            // otherwise create a new one with our point.
            if (_.isUndefined(cluster)) {
                return this._clusterCreate(point);
            }
        },

        clear: function() {
            this._clusters.length = 0;
        },

        onViewportChange: function(scale) {
            this._clusterResolution = 1 / scale;
            this.clear();
            this._rebuildClusters();
        },

        get: function(index) {
            return this._clusters[index];
        },

        getClusters: function() {
            return this._clusters;
        },

        _clusterFindAndMerge: function(element, testFunc, mergeFunc) {
            for (var i = 0; i < this._clusters.length; ++i) {
                var cluster = this._clusters[i];
                if (testFunc(element, cluster)) {
                    mergeFunc(element, cluster);
                    return cluster;
                }
            }
        },

        _clusterAddPoint: function(p, cluster) {

            var count, x, y;
            count = cluster.clusterCount;
            x = (p.x + (cluster.x * count)) / (count + 1);
            y = (p.y + (cluster.y * count)) / (count + 1);
            cluster.x = x;
            cluster.y = y;

            cluster.attribute = this._mergeAttributes(cluster, p);

            // increment the count
            cluster.clusterCount++;
        },

        _clusterTest: function(point, cluster) {
            return this._distance(point, cluster) <= this._clusterTolerance * this._clusterResolution;
        },

        _clusterCreate: function(point) {
            var clusterId = this._clusters.length;

            var cluster = {
                "x": point.x,
                "y": point.y,
                "clusterCount": 1,
                "clusterId": clusterId,
                attribute: _.cloneDeep(point.attribute)
            };
            this._clusters.push(cluster);
            return cluster;
        },

        _rebuildClusters: function() {
            this._addToData = false;
            for (var j = 0; j < this._clusterData.length; ++j) {

                var point = this._clusterData[j];

                this.add(point)
            }
            this._addToData = true;
        }
    });

    return ClusterContainer;
});