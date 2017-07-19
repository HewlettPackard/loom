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

  var $ = require('jquery');
  var _ = require('lodash');

  var math = require('weaver/utils/math');
  var BaseView = require('weaver/views/BaseView');
  var d3 = require('d3');
  require('cartogram');

  var template = require('./MapViewElement.html');
  var mapTopoJSON = require('weaver-map/data/world-with-country-codes.json');
  var MapDataManager = require('weaver-map/models/map/MapDataManager');

  var D3Adapter = require('./elements/proc/D3Adapter');
  var TemplateSVGRenderer = require('./elements/TemplateSVGRenderer');
  var BaseModel = require('./elements/models/BaseModel');
  var countryDefTemplate = require('weaver-map/views/map/elements/templates/countries/countries-defs.svg');
  var insetEffectDefsTemplate = require('weaver-map/views/map/elements/templates/effects/inset-effect-defs.svg');
  var hatchEffectDefsTemplate = require('weaver-map/views/map/elements/templates/effects/hatch-effect-defs.svg');


  /**
   * MapViewElement represent a static world map with distort feature
   * plus component support to represent data on the map.
   */
  var MapViewElement = BaseView.extend({

    constructorName: "LOOM_MapViewElement",

    events: {
      'change:pan': function (event) {
        this.mapRenderer.refresh(this.templateHatch);
      }
    },

    initialize: function () {

      this.isloaded = false;
      this.percentOfParent = 1;//0.75;

      var $dom = $(template);
      this.setElement($dom[0]);

      this.$el.data('view', this);

      // SETTINGS

      this.transitionDuration = 800;  // Duration of the transition in (?)
      MapDataManager.get(this, this.model.get('viewData'));

      // END SETTINGS

      // SVG settings
      this.$mapSvg = this.$('.mas-mapSvg');
      this.mapSvgElement = d3.selectAll(this.$mapSvg.toArray());

      this.mapRenderModel = new BaseModel({
        mapData: MapDataManager.get(this)
      });
      this.mapRenderer = new TemplateSVGRenderer(this.mapSvgElement[0][0]);

      // Thread settings
      var translator = this.model.get('translator');

      // Default tooltip for country:
      this.defaultTooltip = function (d) {
        return translator.translate(d.properties.countryName);
      };
      // Tooltip for country with number of Items:
      this.advancedTooltip = function (d) {
        return [translator.translate(d.properties.countryName), d.properties.nbItems].join(": ");
      };
    },

    // ----------------------------------------------------------------------
    // RENDERING
    //

    render: function () {

      this.uuid = _.uniqueId();

      // Default projection for the path.
      this.defaultPath = d3.geo.path();
      this.currentPath = this.defaultPath;

      this._computeMapSize();
      this._updateMapProjection();

      this.carto = d3.cartogram()
        .projection(MapDataManager.get(this).get('projection'))
        .properties(_.bind(this._getPropertyForCountry, this));

      // Creating the layer for the countries (translated & scaled on zoom/pan)
      this.realCountries = d3.select('#' + this.mapRenderer.getSVGElement().id)
        .append('g')
        .attr('id', 'countries' + this.uuid)
        .append('g')
          .attr("id", "layer-scene")
          .selectAll("g");

      var lazyUpdate = _.debounce(_.bind(this._updateOnResize, this), 60);
      $(window).on('resize', lazyUpdate);

      MapDataManager.get(this).on('map:shrink', _.bind(function () {
        this.__newState = 'small';
        this.percentOfParent = 0.75;
        this._scheduleSizeRefresh();
      }, this));

      MapDataManager.get(this).on('map:grow', _.bind(function () {
        this.__newState = 'big';
        this.percentOfParent = 1;
        this._scheduleSizeRefresh();
      }, this));

      this._init(mapTopoJSON);
    },

    _scheduleSizeRefresh: function () {
      clearTimeout(this.__sizeTimeoutId);
      this.__sizeTimeoutId = setTimeout(_.bind(function () {

        if (!_.isEqual(this.__curState, this.__newState)) {

          this.__curState = this.__newState;
          this._updateOnResize();
        }
      }, this), 20);
    },

    _init: function (topo) {
      this.topology = topo;
      this.geometries = topo.objects.countries.geometries;
      this._parseFeatures();
      this._addEffectDefs();
      this._initSettings();
      this._setMapAsReady();
    },

    _parseFeatures: function () {

      this.defaultFeatures = this.carto.features(this.topology, this.geometries);

      var extents = { min: [Number.MAX_VALUE, Number.MAX_VALUE], max: [-Number.MAX_VALUE, -Number.MAX_VALUE] };
      var self = this;
      this.realCountries = this.realCountries.data(this.defaultFeatures)
        .enter()
        .append("use")
          .attr('id', function (d) {return self.convertToRefId(self.convertToId(d.properties.countryName)); })
          .attr("xlink:href", function (d) { return '#' + self.convertToId(d.properties.countryName); })
          .attr('class', 'mas-mapCountry do-not-translate-touch-events');

      this.countryDefs = this.mapRenderer.addDefs({
        template: countryDefTemplate,
        processorContext: this._convertTopoJson(this.defaultFeatures),
        adapter: new D3Adapter(_.indexBy(this.defaultFeatures, function (value) {
          return value.properties.countryName + this.uuid;
        }, this)),
      });

      // Compute the extents for the default features.
      _.forEach(this.defaultFeatures, this._computeExtents(extents, this.defaultPath));

      this.defaultExtents = extents;

      this.realCountries.append("title")
        .text(this.defaultTooltip);
    },

    _addEffectDefs: function () {

      // Hatch
      this.templateHatch = this.mapRenderer.addDefs({
        template: hatchEffectDefsTemplate,
        modelContext: this.mapRenderModel,
        refresh: true
      });

      // Inset
      this.mapRenderer.addDefs({
        template: insetEffectDefsTemplate,
        modelContext: this.mapRenderModel,
        refresh: true
      });
    },

    _convertTopoJson: function (features) {
      return {
        paths: _.map(features, function (feature) {
          return {
            d: this.defaultPath(feature),
            id: feature.properties.countryName + this.uuid
          }
        }, this)
      };
    },

    _initSettings: function () {

      // Final initialization step
      this.extents = this.defaultExtents;
    },

    _setMapAsReady: function () {
      if (this.width > 0 && this.height > 0) {
        // Initialization complete. Map is ready.
        MapDataManager.get(this).set('mapIsReady', true);
      }
    },

    _updateCountriesPath: function (pathProjection, shouldComputeExtents) {

      var extents = { min: [Number.MAX_VALUE, Number.MAX_VALUE], max: [-Number.MAX_VALUE, -Number.MAX_VALUE] };

      var selection = this.getDefsCountries()
        .attr("d", pathProjection);

      if (shouldComputeExtents) {
        this.extents = extents;
        return selection.each(this._computeExtents(extents, pathProjection));
      }

      return selection;
    },

    _updateCountriesPathWithTransition: function (pathProjection, shouldComputeExtents) {

      var extents = { min: [Number.MAX_VALUE, Number.MAX_VALUE], max: [-Number.MAX_VALUE, -Number.MAX_VALUE] };

      var transition = this.getDefsCountries().transition()
        .duration(this.transitionDuration)
        .ease("linear")
        .attr("d", pathProjection);

      if (shouldComputeExtents) {
        this.extents = extents;
        return transition.transition()
          .duration(0)
          .each(this._computeExtents(extents, pathProjection));
      }

      return transition;
    },


    _computeExtents: function (extents, path) {
      return function (d) {
        var bounds = path.bounds(d);
        extents.min = math.min(bounds[0], extents.min);
        extents.max = math.max(bounds[1], extents.max);
      };
    },

    _update: function (pathProjection, features, tooltip) {
      this.currentPath = pathProjection;

      this.getDefsCountries().data(features)
        .select("title")
          .text(tooltip);

      this._updateCountriesPathWithTransition(pathProjection);
    },

    // ----------------------------------------------------------------------
    // DISTORTION
    //

    applyDistortion: function (features, extents) {

      this._update(this.carto.path, features, this.advancedTooltip);
      this.mapWasDistorted = true;
      this.extents = extents;
      this.trigger('change:extents', this.extents);
    },

    resetMap: function () {

      this._update(this.defaultPath, this.defaultFeatures, this.defaultTooltip);
      this.trigger('reset:distortion', this.mapWasDistorted);
      this.mapWasDistorted = false;
      this.extents = this.defaultExtents;
      this.trigger('change:extents', this.extents);
    },

    // ----------------------------------------------------------------------
    // DIMENSIONS
    //

    _updateOnResize: function () {

      var oldSize = {
        width: this.width,
        height: this.height
      };

      // Recompute the map dimensions
      this._computeMapSize();

      // Do we have a change ?
      if (oldSize.width !== this.width) {

        // Trigger the change
        this.trigger('change:viewport', this.width, this.height, this.extents);

        // Recompute the map projection
        // (and trigger the change)
        this._updateMapProjection();

        // Synchronously update the paths
        this._updateCountriesPath(this.currentPath, true);
      }
    },

    _computeMapSize: function () {
      this.width = this._width();              // width of the map.
      // Height computed for a world map with an adjustment factor to hide the antartic by default.
      this.height = 1.35 * 5 / 4 * Math.log(Math.tan(9 * Math.PI / 20)) * this.width / 2 / Math.PI;
      this.height = Math.ceil(this.height);

      // We constrain the svg with the new value computed.
      this.$mapSvg.width(this.width);
      this.$mapSvg.height(this.height);
      // We constrain the other flex element.
      this.$el.height(this.height);
    },

    _updateMapProjection: function () {
      var offset = -this.height / 7;

      var proj = math.d3Miller()
        .rotate([-11, 0])
        .scale((this.width + 1) / 2 / Math.PI)
        .translate([this.width / 2, this.height / 2 - offset])
        .precision(0.1);

      MapDataManager.get(this).setProjection(proj);

      this.defaultPath.projection(proj);
    },


    getD3Element: function () {
      return this.mapSvgElement;
    },

    // We still need d3 for the transition and to update the path
    // when using the distortion.
    getDefsCountries: function () {
      return d3.select("#defs-countries").selectAll("path");
    },

    getExtents: function () {
      return this.extents;
    },

    getCenter: function () {
      this._getCenterValue = [this._width() / 2, this._height() / 2];
      return this._getCenterValue;
    },

    getBBox: function () {
      return { width: this.width, height: this.height, x: 0, y: 0};
    },

    getMapRatio: function () {
      return this.width / this.height;
    },

    // ----------------------------------------------------------------------
    // OTHERS
    //

    remove: function () {

      BaseView.prototype.remove.apply(this);
      this.mapSvgElement.remove();
      this.mapRenderer.clear();
    },

    refreshElementsLabels: _.noop,

    convertToId: function (str) {
      return str.replace(/[ '(){}#,]/g, '') + this.uuid;
    },

    convertToRefId: function (str) {
      return 'r-' + str.replace(/[ '(){}#,]/g, '');
    },

    _width: function () {
      return this.$el.width() * 0.70; //* this.percentOfParent;
    },

    _height: function () {
      return this.$el.height();
    },

    _getPropertyForCountry: function (d) {
      // TODO
      return {
        countryName: d.name,
      };
    }

  });

  return MapViewElement;
});
