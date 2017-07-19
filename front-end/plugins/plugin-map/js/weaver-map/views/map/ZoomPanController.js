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

  var LAYER_SCENE = 'layer-scene';

  var ZoomPanController = MapComponent.extend({

    events: {
      'update:layer-scene': function (layer) {
        this.sceneLayer = this.d3map.selectAll('#' + ZoomPanController.LAYER_SCENE);
        if (layer) {
          this._updateTransform(layer);
        }
      }
    },
    
    initialize: function () {

      MapComponent.prototype.initialize.apply(this, arguments);

      this.panIsPossible = {
        up: true,
        down: true,
        left: true,
        right: true
      };
    },

    initializeWhenAttached: function () {

      var self = this;

      // Init zoom

      this._initZoomPanSettings();

      // We set a fix extent, that work for the world map
      // but this should be more map dependent.(fetching extent for instance).
      this.zoom = d3.behavior.zoom()
        .scale(this.currentScale)
        .translate(this.currentTranslate)
        .scaleExtent([this.zoomMin, this.zoomMax])
        .on("zoom", _.bind(this._updateZoom, this));


      this.d3map.call(this.zoom);

      this.d3map.on('dblclick.zoom', null);
      this.d3map.on('mousemove', function () {
        var focus = d3.mouse(this);
        focus[0] -= self.offsetX;
        focus[1] -= self.offsetY;
        self.zoom.center(focus);
      });

      // Other needed settings:
      this.extents = this.mapData.extents();
      this.width = this.mapData.mapWidth();
      this.height = this.mapData.mapHeight();
      
      this.listenTo(this.map, 'change:extents', function (extents) {
        this.extents = extents;
      });

      this.listenTo(this.map, 'change:viewport', this._onViewPortChange);

      // We look for the layer on which the zoom act, to update them.
      this.sceneLayer = this.d3map.selectAll('#' + ZoomPanController.LAYER_SCENE);

      // Set initial transforms values.
      this._updateTransform(this.sceneLayer);
    },

    _onViewPortChange: function (width, height, extents) {

      var scale = this.width / width;

      this.width = width;
      this.height = height;
      this.extents = extents;

      // Fake a translate to reposition the map where it was looking at.
      var translate = [this.currentTranslate[0] / scale, this.currentTranslate[1] / scale];

      this.currentTranslate = translate;
      this.zoom.translate(this.currentTranslate);
      //this.zoom.scale(this.currentScale / scale);

      this._updateTransform(this.sceneLayer);

      // Update MapData:
      this.mapData.setZoom({
        currentTranslate: this.currentTranslate,
        currentScale: this.currentScale,
      });
    },

    _initZoomPanSettings: function () {

      this.currentScale = this.mapData.getZoomCurrentScale();
      this.currentTranslate = this.mapData.getZoomCurrentTranslate();
      this.zoomMax = Infinity;
      this.zoomMin = 1;

      this.offsetX = 0;
      this.offsetY = 0;
      this.δxMaxOld = 0;
      this.δxMinOld = 0;
      this.δyMaxOld = 0;
      this.δyMinOld = 0;
    },

    _updateZoom: function () {

      var translate = d3.event.translate;

      var zoomed = this.currentScale !== d3.event.scale;
      
      if (zoomed) {
        this.currentScale = d3.event.scale;
        this._computeDeltas(d3.event.translate);
        translate[0] += this.offsetX;
        translate[1] += this.offsetY;
      } else {
        this._updatePan(d3.event.translate);
        translate[0] += this.offsetX;
        translate[1] += this.offsetY;
      }

      this.currentTranslate = translate;
      this.zoom.translate(translate);
      this.offsetY = 0;
      this.offsetX = 0;

      this._updateTransform(this.sceneLayer);

      // Update MapData:
      this.mapData.setZoom({
        currentTranslate: this.currentTranslate,
        currentScale: this.currentScale,
      });

      // Dispatch instant event.
      this.dispatchInstantEvent(this.d3map, 'change:pan:instant', this.currentTranslate);

      // Dispatch throttled events.
      if (zoomed) {
      
        this.dispatchEvent(this.d3map, 'change:zoom', this.currentScale);
      }

      this.dispatchEvent(this.d3map, 'change:pan', this.currentTranslate);

      // Dispatch really slow event.
      this.dispatchEvent(this.d3map, 'change:pan:slow', this.currentTranslate, 1000);

      this.dispatchEvent(this.d3map, 'change:panup', this.panIsPossible.up, 300);
      this.dispatchEvent(this.d3map, 'change:pandown', this.panIsPossible.down, 300);
      this.dispatchEvent(this.d3map, 'change:panleft', this.panIsPossible.left, 300);
      this.dispatchEvent(this.d3map, 'change:panright', this.panIsPossible.right, 300);

      if (zoomed) {

        this.dispatchEvent(this.d3map, 'change:zoomminus', this.currentScale > this.zoomMin, 300);
        this.dispatchEvent(this.d3map, 'change:zoomplus', this.currentScale < this.zoomMax, 300);
      }
    },

    _updatePan: function (translate) {
      
      var δx = (translate[0] + this.offsetX - this.currentTranslate[0]);
      var δy = (translate[1] + this.offsetY - this.currentTranslate[1]);
      var δxMax = (this.extents.max[0] * this.currentScale + translate[0] + this.offsetX) - this.width;
      var δxMin = - (this.extents.min[0] * this.currentScale + translate[0] + this.offsetX);
      var δyMax = (this.extents.max[1] * this.currentScale + translate[1] + this.offsetY) - this.height;
      var δyMin = - (this.extents.min[1] * this.currentScale + translate[1] + this.offsetY);

      this._updatePanIsPossible(δxMax, δxMin, δyMax, δyMin);

      if (δxMax >= 0 || δxMin >= 0) {
        if (δxMax < 0) {
          if (this.δxMaxOld < 0) this.offsetX -= δx < 0 ? δx : 0;//Min;
          else this.offsetX -= δxMax;
        }
        if (δxMin < 0)  {
          if (this.δxMinOld < 0) this.offsetX -= δx > 0 ? δx : 0;//Max;
          else this.offsetX += δxMin;
        }
      }

      if (δyMax >= 0 || δyMin >= 0) {
        if (δyMax < 0) {
          if (this.δyMaxOld < 0) this.offsetY -= δy < 0 ? δy : 0;//Min;
          else this.offsetY -= δyMax;
        }
        if (δyMin < 0) {
          if (this.δyMinOld < 0) this.offsetY -= δy > 0 ? δy : 0;//Min;
          else this.offsetY += δyMin;
        }
      }

      this.δxMaxOld = δxMax;
      this.δxMinOld = δxMin;
      this.δyMaxOld = δyMax;
      this.δyMinOld = δyMin;
    },

    _computeDeltas: function (translate) {

      var δxMax = (this.extents.max[0] * this.currentScale + translate[0] + this.offsetX) - this.width;
      var δxMin = - (this.extents.min[0] * this.currentScale + translate[0] + this.offsetX);
      var δyMax = (this.extents.max[1] * this.currentScale + translate[1] + this.offsetY) - this.height;
      var δyMin = - (this.extents.min[1] * this.currentScale + translate[1] + this.offsetY);

      this._updatePanIsPossible(δxMax, δxMin, δyMax, δyMin);

      this.δxMaxOld = δxMax;
      this.δxMinOld = δxMin;
      this.δyMaxOld = δyMax;
      this.δyMinOld = δyMin;
    },

    _updatePanIsPossible: function (δxMax, δxMin, δyMax, δyMin) {

      var epsilon = 1e-3;

      this.panIsPossible.right = δxMin > epsilon;
      this.panIsPossible.left = δxMax > epsilon;
      this.panIsPossible.down = δyMin > epsilon;
      this.panIsPossible.up = δyMax > epsilon;
    },

    _updateTransform: function (sceneLayer) {

      sceneLayer.attr("transform",
        "translate(" + this.currentTranslate + ") " +
        "scale(" + [this.currentScale, this.currentScale] + ")");

      this.triggerMapEvent('change:transform', {
        translate: this.currentTranslate,
        scale: this.currentScale,
      });
    },

  });

  ZoomPanController.LAYER_SCENE = LAYER_SCENE;

  return ZoomPanController;
});