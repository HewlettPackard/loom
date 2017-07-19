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
/* global describe, it, sinon, expect, beforeEach, after, afterEach */
/* jshint expr: true */
define(function (require) {
  "use strict";

  var _ = require('lodash');
  var math = require('weaver/utils/math');
  
  var MapDataManager = require('weaver-map/models/map/MapDataManager');
  var ClusterGrid = require('weaver-map/views/map/ClusterGrid');

  describe('ClusterGrid', function () {

    // To test the ClusterGrid, we need
    beforeEach(function () {

      var fakeMapViewElement = {
        cid: 0,
        getD3Element: function () { return {}; },
        $el: {
          off: _.noop,
          on: _.noop,
        },
      };

      this.proj = math.d3Miller()
        .rotate([-11, 0])
        .scale((500) / 2 / Math.PI)
        .translate([250, 180])
        .precision(0.1);

      // ... a MapData ...
      MapDataManager.clear();
      this.mapData = MapDataManager.get(fakeMapViewElement);
      this.mapData.set('mapIsReady', true);
      this.mapData.setProjection(this.proj);

      // ... and a ClusterGrid.
      this.clusterGrid = new ClusterGrid({
        map: fakeMapViewElement,
      });
    });

    afterEach(function () {
      MapDataManager.clear();
    });

    describe('clamp()', function () {

      it('Should limit the value to the range [max, min]', function () {
        var value = -182;

        expect(this.clusterGrid.clamp(value, 180, -180)).to.equal(-180);
        expect(this.clusterGrid.clamp(value, -185, -360)).to.equal(-185);
      });

      it('Should limit the value to be lower than max if called with one argument', function () {

        var value = 254;

        expect(this.clusterGrid.clamp(value, 180)).to.equal(180);
        expect(this.clusterGrid.clamp(value, -180)).to.equal(-180);
        expect(this.clusterGrid.clamp(value, 300)).to.equal(254);
      });
    });

    describe('computeFilterRegion()', function () {

      it('Should update the filter region in the mapData', sinon.test(function () {

        var spyUpdateRegion = this.spy(this.clusterGrid, 'updateRegion');
        var spySet = this.spy(this.mapData, 'set');

        this.clusterGrid.computeFilterRegion(33, 34, 0, 0);

        expect(spySet).to.have.been.called;
        expect(spyUpdateRegion).to.have.been.called;
      }));

      it('Should call _doComputeFilterRegion when the grid is different from the default', sinon.test(function () {

        var spyDoComputeFilterRegion = this.spy(this.clusterGrid, '_doComputeFilterRegion');
        var spyUpdateRegion = this.spy(this.clusterGrid, 'updateRegion');

        this.mapData.setZoom({
          currentScale: 5,
          currentTranslate: [0, 0],
        });
        this.clusterGrid.computeFilterRegion(3, 3, 0, 0);

        expect(spyDoComputeFilterRegion).to.have.been.called;
        expect(spyUpdateRegion).to.have.been.called;
      }));
    });
  });
});