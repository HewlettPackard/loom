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
  var ClusterGrid = require('./ClusterGrid');
  var ZoomPanController = require('./ZoomPanController');

  // We block the update of the pan/view.
  ZoomPanController.LAYER_SCENE = 'layer-scene-disabled';

  ClusterGrid.prototype.events = {
    'change:zoom': 'DEBUGrenderGrid',
    'change:pan': 'DEBUGrenderGrid',
  };

  ClusterGrid.prototype.initializeWhenAttached = function () {
    this.camera = this.d3map
      .append('rect')
        .attr('id', 'camera');
    this.queryRegion = this.d3map
      .append('rect')
        .attr('id', 'filteredRegion');
    this.gridParentNode = this.d3map
      .append('g')
        .attr('id', 'layer-scene');
    this.horizontalLinesNode = this.gridParentNode
      .append('g')
        .attr('id', 'horizontal-lines');
    this.verticalLinesNode = this.gridParentNode
      .append('g')
        .attr('id', 'vertical-lines');

    this.horizontalLinesRegionNode = this.gridParentNode
      .append('g')
        .attr('id', 'horizontalRegion-lines');

    this.verticalLinesRegionNode = this.gridParentNode
      .append('g')
        .attr('id', 'verticalRegion-lines');
    this.DEBUGrenderGrid();
  };


  ClusterGrid.prototype.DEBUGrenderGrid = function () {

    var tmp;
    var u = this.mapData;

    var extents = u.extents();

    var scale = u.getZoomCurrentScale();

    var cameraLngWidth  = 358 / scale;
    var cameraLatHeight = 178 / scale;

    var delta = [extents.max[0] - extents.min[0], extents.max[1] - extents.min[1]];

    var lngExtent = 360;
    var latExtent = 180;

    this.nbLng = u.get('stepCameraLng') * (Math.abs(Math.round(lngExtent / cameraLngWidth)) || 1);
    this.nbLat = u.get('stepCameraLat') * (Math.abs(Math.round(latExtent / cameraLatHeight)) || 1);

    var stepLng = lngExtent / this.nbLng;
    var stepLat = latExtent / this.nbLat;
    
    var listX = _.range(this.nbLng);
    var listY = _.range(this.nbLat);

    tmp = u.convertFromSVGSpaceToLngLatSpace([0, 0]);
    var offsetLng = tmp[0];
    var offsetLat = tmp[1];

    listX = _.map(listX, function (index) {
      return u.convertFromLngLatSpaceToSVGSpace([index * stepLng + offsetLng, 0])[0];
    });
    listY = _.map(listY, function (index) {
      return u.convertFromLngLatSpaceToSVGSpace([0, offsetLat - index * stepLat])[1];
    });

    ///////////////////////////////////////////////////////////////////////////
    /// Camera
    this.camera
        .attr('transform', "matrix(" + u.getCTM().invert() + ")")
        .attr('x', 0)
        .attr('y', 0)
        .attr('width', u.mapWidth())
        .attr('height', u.mapHeight())
        .style({'stroke': 'red', 'opacity': 0.5, 'stroke-width': 1, 'pointer-events': 'none'});

    ///////////////////////////////////////////////////////////////////////////
    /// Region     
    this.computeFilterRegion(stepLng, stepLat, offsetLng, offsetLat);

    ///////////////////////////////////////////////////////////////////////////
    /// Now we can send the data to Loom..      
    this.updateGrid(offsetLng, offsetLat, stepLng, stepLat);
    
    // Then we show what loom will process: (debug feature)
    // 
    
    var cx     = u.get('filterRegion').minimums[0];
    var width  = u.get('filterRegion').maximums[0] - cx;
    var cy     = u.get('filterRegion').maximums[1];
    var height = cy - u.get('filterRegion').minimums[1];

    var xy = u.convertFromLngLatSpaceToSVGSpace([cx, cy]);
    var wh = u.convertFromLngLatSpaceToSVGSpace([width + cx, - height + cy]);

    // _.remove(listX, function (value) {
    //   return value < xy[0] || value > wh[0];
    // });

    // _.remove(listY, function (value) {
    //   return value < xy[1] || value > wh[1];
    // });
    
    wh = [wh[0] - xy[0], wh[1] - xy[1]];

    

    var regionRightTop = [0, 0];//xy;
    var extendLines = delta;//[regionRightTop[0] + wh[0], regionRightTop[1] + wh[1]];

    ///////////////////////////////////////////////////////////////////////////
    /// Grid
    this.DEBUGrenderHorizontalLines(this.horizontalLinesNode, listY, regionRightTop[0], extendLines[0]);
    this.DEBUGrenderVerticalLines(this.verticalLinesNode, listX, regionRightTop[1], extendLines[1]);

    this.queryRegion
        .attr('x', xy[0])
        .attr('y', xy[1])
        .attr('width', wh[0])
        .attr('height', wh[1])
        .style({'fill': 'none', 'stroke-width': 2, 'stroke': 'yellow'});

    var nbRegionLng = (Math.abs(Math.round(lngExtent / width)) || 1);
    var nbRegionLat = (Math.abs(Math.round(latExtent / height)) || 1);

    var listRegionX = _.range(nbRegionLng);
    var listRegionY = _.range(nbRegionLat);

    var stepLngRegion = lngExtent / nbRegionLng;
    var stepLatRegion = latExtent / nbRegionLat;

    listRegionX = _.map(listRegionX, function (index) {
      return u.convertFromLngLatSpaceToSVGSpace([index * stepLngRegion + offsetLng + cx, 0])[0] || 0;
    });

    listRegionY = _.map(listRegionY, function (index) {
      return u.convertFromLngLatSpaceToSVGSpace([0, offsetLat - index * stepLatRegion + cy - height])[1] || 0;
    });

    //this.DEBUGrenderHorizontalLines(this.horizontalLinesRegionNode, listRegionY, 0, delta[0], 'green');
    //this.DEBUGrenderVerticalLines(this.verticalLinesRegionNode, listRegionX, 0, delta[1], 'green');
  };

  ClusterGrid.prototype.DEBUGrenderHorizontalLines = function (node, lines, xmin, xmax, stroke) {
    stroke = stroke || 'blue';
    var linesSVG = node.selectAll('line');

    var linesSVGinner = linesSVG.data(lines, function (datum, index) {
      return index;
    });

    linesSVGinner
      .enter().append('line');

    linesSVGinner
        .attr('x1', xmin)
        .attr('x2', xmax)
        .style({'stroke': stroke, 'opacity': 0.5, 'stroke-width': 1})
        .attr('y1', function (datum) {
          return datum;
        })
        .attr('y2', function (datum) {
          return datum;
        });

    linesSVGinner.exit().remove();
  };

  ClusterGrid.prototype.DEBUGrenderVerticalLines = function (node, lines, ymin, ymax, stroke) {
    stroke = stroke || 'blue';
    var linesSVG = node.selectAll('line');
    
    var linesSVGinner = linesSVG.data(lines, function (datum, index) {
      return index;
    });

    linesSVGinner
      .enter().append('line');
    
    linesSVGinner
        .attr('y1', ymin)
        .attr('y2', ymax)
        .style({'stroke': stroke, 'opacity': 0.5, 'stroke-width': 1})
        .attr('x1', function (datum) {
          return datum;
        })
        .attr('x2', function (datum) {
          return datum;
        });

    linesSVGinner.exit().remove();
  };

  return ClusterGrid;
});