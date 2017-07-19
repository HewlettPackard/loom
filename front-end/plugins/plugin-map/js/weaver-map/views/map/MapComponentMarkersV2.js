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
  var MapAggregationItemUpdateComponent = require('./MapAggregationItemUpdateComponentV2');
  var MapMarker = require('./MapMarkerV2');

  var CacheHelper = require('./size/CacheHelper');
  var SizeCalculator = require('./size/SizeCalculator');
  var ConstantSizeCalculator = require('./size/ConstantSizeCalculator');
  var LinearSizeCalculator = require('./size/LinearSizeCalculator');

  var markersTemplate = require('weaver-map/views/map/elements/templates/markers/markers.svg');
  var markersDefsTemplate = require('weaver-map/views/map/elements/templates/markers/markers-defs.svg');

  var MapComponentMarkers =  MapAggregationItemUpdateComponent.extend({

    events: {
      'change:zoom': function (event) {
        this._updateZoom(event.originalEvent.args);
      },
    },

    initialize: function (options) {
      MapAggregationItemUpdateComponent.prototype.initialize.apply(this, arguments);

      this.markersList = [];
      this.markersViews = {};

      // Marker size configuration
      this.minMarkerSize = 8;
      this.maxMarkerSize = 35;
      this.sizeCalculator = undefined;

      this.transitionDuration = 800;

      this.onSortCallback = _.noop;

      this.listenTo(this.thread, 'change:outdated', function (model, areOutdated) {
        if (areOutdated) {
          this.addOutdatedState();
        } else {
          this.removeOutdatedState();
        }
      });
    },

    initializeWhenAttached: function () {
      MapAggregationItemUpdateComponent.prototype.initializeWhenAttached.apply(this, arguments);

      this.idMarkerGroup = this.containerModel.getContext().idGroup;

      this.templateMarkers = this.mapRenderer.add({
        template: markersTemplate,
        modelContext: this.containerModel,
        refresh: true
      });

      this.templateDefMarkers = this.mapRenderer.addDefs({
        template: markersDefsTemplate,
        processorContext: this.containerModel.getContext()
      });

      this.defsMarkers = this.d3map.select('#' +
        this.mapRenderer.getRendered(this.templateDefMarkers).id
      );

      this.tooltip = this.map.$(".mas-mapTooltip");

      var self = this;
      this.map.mapSvgElement
        .on('mouseover', function () {
          if (!d3.event.defaultPrevented) {
            self.tooltip.css('opacity', '0.0');
          }
        });

      this.listenTo(this.map, 'reset:distortion', _.bind(this._resetDistortion, this));

      this.mapData.registerPoints(this.cid, this.getPointsToMove, this._updatePoints, this);
    },

    // OUTDATED STATE
    addOutdatedState: function () {
      this.containerModel.addClass("is-outdated");
      this.refresh.refresh();

      // Stop the current transition and prevent subsequent transitions.
      this.isOutdated = true;
      if (this.markersRef) {
        this.markersRef.transition().duration(0);
      }
    },

    removeOutdatedState: function () {
      this.containerModel.removeClass("is-outdated");
      this.refresh.refresh();

      // Re-allow transition on zoom (the query might not be changed).
      this.isOutdated = false;
    },

    // DISTORTION RELATED
    getPointsToMove: function () {
      return _.map(this.markersList, function (e) { return e.marker.lngLat; });
    },

    _updatePoints: function (points) {

      if (this.force) {
        this.force.stop();
      }

      // update of the position of the markers.
      var scale = this.mapData.getZoomCurrentScale();
      var i;

      var markersList = this.markersList;
      for (i = 0; i < points.length; ++i) {
        markersList[i].dx = points[i][0];
        markersList[i].dy = points[i][1];

        // Add a random bias to avoid zero distance with the force layout
        // causing the computation to be stopped
        markersList[i].x = points[i][0] + Math.random() / scale;
        markersList[i].y = points[i][1] + Math.random() / scale;

        if (points.isChangeInstant) {

          markersList[i].px = markersList[i].x;
          markersList[i].py = markersList[i].y;
        }
      }

      // If no fibre update is running we update the markers position now
      if (!this.isFibreUpdateRunning) {
        this._updateSvgMarkersSoft(markersList);
      // Otherwise we just update the data for the next markers creations.
      } else {
        this.proj = this.mapData.get('projection');
      }

    },

    _resetDistortion: function () {
      var proj = this.mapData.get('projection');

      for (var i = 0; i < this.markersList.length; ++i) {
        var datum = this.markersList[i];
        var marker = datum.marker;
        var projCoord = proj(marker.lngLat);
        datum.dx = projCoord[0];
        datum.dy = projCoord[1];
      }
      this._updateSvgMarkers(this.markersList);
    },

    // REMOVAL
    removeFromMap: function () {
      this._removeMarkers();
      this.mapRenderer.remove(this.templateMarkers);
      this.mapRenderer.remove(this.templateDefMarkers);
      this.defsMarkers.remove();
      this.mapData.unregisterPoints(this.cid);
      MapAggregationItemUpdateComponent.prototype.removeFromMap.apply(this, arguments);
    },

    // ----------------------------------------------------------------------
    // OVERRIDE      - MapAggregationItemUpdateComponent -
    //

    prepareFibreUpdate: function (/* numberOfFibres */) {
      MapAggregationItemUpdateComponent.prototype.prepareFibreUpdate.apply(this, arguments);

      if (this.force) {
        this.force.stop();
        this.force = undefined;
      }

      // Prepare helpers:
      var geoAttributes = this.thread.getGeoAttributes();
      this.attributesTranslator = _.bind(geoAttributes.getAttributeName, geoAttributes);
      this.proj = this.mapData.get('projection');

      // Prepare data set
      _.forEach(this.markersList, function (d) {
        this.stopListening(d.model);
      }, this);
      _.remove(this.markersList);

      // Alert this interface that change are being performed async
      this.isFibreUpdateRunning = true;

      // Double buffering here
      this.markersViewsNew = {};
    },

    putAggregation: function (id, aggregation) {
      MapAggregationItemUpdateComponent.prototype.putAggregation.apply(this, arguments);

      var plottableAggregateStats = aggregation.get('plottableAggregateStats');

      this._updateMarker(id, {
        lngLat: [
          plottableAggregateStats[this.attributesTranslator('longitude') + '_avg'],
          plottableAggregateStats[this.attributesTranslator('latitude') + '_avg'],
        ],
        count: aggregation.get('numberOfItems')
      }, aggregation);
    },

    putItem: function (id, item) {
      MapAggregationItemUpdateComponent.prototype.putItem.apply(this, arguments);

      this._updateMarker(id, {
        lngLat: [
          item.get(this.attributesTranslator('longitude')),
          item.get(this.attributesTranslator('latitude')),
        ],
        count: 1
      }, item);
    },

    cancelFibreUpdate: function () {
      MapAggregationItemUpdateComponent.prototype.cancelFibreUpdate.apply(this, arguments);

      this.attributesTranslator = undefined;
      this.proj = undefined;

      this._clearViews(this.markersViewsNew);
      this.markersViewsNew = undefined;
    },

    finishFibreUpdate: function () {
      MapAggregationItemUpdateComponent.prototype.finishFibreUpdate.apply(this, arguments);

      // Switch buffers:
      this._clearViews(this.markersViews);
      this.markersViews = this.markersViewsNew;
      this.markersViewsNew = {};

      // Update svg
      this.renderAndUpdate();

      // Add tracking:
      _.forEach(this.markersViews, function (view) {
        this.dispatchInstantEvent(this.d3map, 'addlazy:tracking', view);
      }, this);

      // Update refs
      this.triggerMapEvent('pointsUpdated');
      this._updateSvgMarkers(this.markersList);
      this.tooltip.css('opacity', '0.0');


      this.attributesTranslator = undefined;
      this.proj = undefined;
      this.isFibreUpdateRunning = false;
    },

    // ----------------------------------------------------------------------
    // PRIVATE INTERFACE
    //

    _scaleForMarker: function (scale, d) {
      if (d.hidden) return this.maxMarkerSize / scale;
      return this.sizeCalculator.applyWith(d.count) / scale;
    },
    /*_scaleForMarker: function (scale, d) {
      var x = d.count / this.minItemsCount + 1;
      if (x < 100) {
        return Math.pow(x, Math.log(12) / Math.log(100)) * this.minMarkerSize / scale;
      } else {
        return Math.log(x * Math.log(12) + 100 * (1 - Math.log(12))) * 12 / Math.log(100) * this.minMarkerSize / scale;
      }
    },*/

    _updateMarker: function (id, marker, model) {
      var scale = this.mapData.getZoomCurrentScale();
      var xy = this.proj(marker.lngLat);
      var markerOnPlane = {
        'id': id,
        'idRef': _.uniqueId('defcircle'),
        'x': xy[0] + Math.random() / scale,
        'y': xy[1] + Math.random() / scale,
        'dx': xy[0],
        'dy': xy[1],
        'r': 0,
        'count': marker.count,
        'marker': marker,
        'model': model,
        'hidden': model.get('l.tags') === 'polygon_clustering',
        'selected': model.get('selected') ? true: false,
      };

      var self = this;
      markerOnPlane.onSort = function (callback, thisArg) {
        self.onSortCallback = _.bind(callback, thisArg);
      };

      this.listenTo(model, 'change:selected', function (model, selected) {
        markerOnPlane.selected = selected;
        var sortCallback = _.bind(this._sortCallback, this);
        this.onSortCallback(sortCallback);
        //this.markers.sort(sortCallback);
      });

      this.markersList.push(markerOnPlane);
      var view = this._createMapMarker();
      this._updateView(view, markerOnPlane);
      this.markersViewsNew[id] = view;
    },

    _removeMarkers: function () {
      _.remove(this.markersList);
      this._clearViews(this.markersViews);
    },

    _clearViews: function (viewContainer) {
      _(viewContainer).forEach(_.bind(this._clearView, this));
    },

    _clearView: function (view) {
      this.dispatchInstantEvent(this.d3map, 'remove:tracking', view);
      view.detach();
      view.remove();
    },

    _updateZoom: function (currentScale) {

      this._updateRadiusForList(currentScale, this.markersList);
      this._updateMarkersShape(currentScale);

      if (this.markersRef && !this.isOutdated) {

        this.markersRef.transition()
          .duration(this.transitionDuration)
          .attr('r', function (d) { return d.r; })
          .call(endall, _.bind(function () {
            this._resumeForce(currentScale);
          }, this));
      }
    },

    _updateMarkersShape: function (scale) {
      scale = scale || this.mapData.getZoomCurrentScale();

      if (this.markers) {

        // this.markers.transition()
        //   .duration(this.transitionDuration)
        //   .ease("linear")
        //   .style("stroke-width", 1 / scale);
      }
    },

    _createMapMarker: function (id) {
      var view = new MapMarker({
        thread: this.thread,
        d3map: this.d3map,
      });

      return view;
    },

    _updateView: function (view, datum) {

      view.setModel(datum.model, datum.idRef);
      view.setDatum(datum);
      this.containerModel.addElementModel(view.templateModel);
    },

    _createForceLayout: function (list) {

      if (this.force) {
        this.force.stop();
        this.force = undefined;
      }

      this.force = d3.layout.force()
        .nodes(list)
        .gravity(0)
        .charge(0)
        .friction(0.9)
        .on("tick", _.bind(this._tickForce, this, list));
      // for (var i = 0; i < 8; ++i) {
      //   this.force.tick();
      // }
      this.force.start();
    },

    _resumeForce: function (scale) {

      if (this.force) {
        this.minRadius = this._scaleForMarker(scale, { count: this.minItemsCount });
        this.force.resume();
      }
    },

    _tickForce: function (list, e) {
      if (!this.force || !this.markersRef) {
        throw new Error("BUGGGG !! ");
      }
      var scale = this.mapData.getZoomCurrentScale();

      this.markersRef
        .each(this._attractByDestination(list, 10 * e.alpha * e.alpha))
        .each(this._collide(list, 0.5, scale))
        .attr("cx", function (d) {
          return d.x;
        })
        .attr("cy", function (d) {
          return d.y;
        });
    },

    _attractByDestination: function (list, alpha) {
      return function (d) {
        var k = 0.01;

        var x = d.x - d.dx,
            y = d.y - d.dy,
            l = Math.sqrt(x * x + y * y);

        if (l > 0) {
          l = l * alpha * k;
          d.x -= x *= l;
          d.y -= y *= l;
        }
      };
    },

    _collide: function (list, alpha, scale) {
      var quadtree = d3.geom.quadtree(list);
      var padding = 0.5 / scale;
      var self = this;
      return function (d) {
        var r = 2 * self.minRadius + padding,
            nx1 = d.x - r,
            nx2 = d.x + r,
            ny1 = d.y - r,
            ny2 = d.y + r;
        quadtree.visit(function (quad, x1, y1, x2, y2) {
          var epsilon = 0.0001;
          var k = 0.1;
          if (quad.point && (quad.point !== d)) {
            var x = d.x - quad.point.x,
                y = d.y - quad.point.y,
                l = Math.sqrt(x * x + y * y),
                r = 2 * self.minRadius + padding;
            if (l < r) {
              l = (l - r) / (l + epsilon) * alpha * alpha * k;
              d.x -= x *= l;
              d.y -= y *= l;
              quad.point.x += x;
              quad.point.y += y;
            }
          }
          return x1 > nx2 || x2 < nx1 || y1 > ny2 || y2 < ny1;
        });
      };
    },

    _sortCallback: function (d1, d2) {

      if (d1.selected === d2.selected) {
        return +d2.count - +d1.count;
      } else {
        return +d1.selected - +d2.selected;
      }
    },

    _updateSvgMarkers: function (list) {
      var self = this;

      var listVisible = _.filter(list, function (d) { return !d.hidden; });

      this.minItemsCount = _.min(list, function (d) { return d.count; }).count;

      if (listVisible.length > 5) {

        this.sizeCalculator = new CacheHelper(new SizeCalculator({
          n: 5,
          epsilon: 1,
          values: _.map(listVisible, function (d) { return d.count; }),
          sizeMin: this.minMarkerSize,
          sizeMax: this.maxMarkerSize,
        }));
      } else {

        this.sizeCalculator = new ConstantSizeCalculator({
          value: this.minMarkerSize
        });
      }

      var scale = this.mapData.getZoomCurrentScale();
      this.minRadius = this._scaleForMarker(scale, { count: this.minItemsCount });

      this.markersRef = this.defsMarkers.selectAll("circle").data(list, function (d) { return d.id; });
      this.markersRef.enter().append('circle');

      this.markersRef
        .attr('id', function (d) { return d.idRef; });

      this._updateRadiusForList(scale, list);

      this.markersRef.transition()
        .duration(0)
        .attr('r', function (d) { return d.r; })
        .attr('cx', function (d) { return d.x; })
        .attr('cy', function (d) { return d.y; });

      this.markersRef.exit()
        .remove();

      this._createForceLayout(listVisible);

      this._updateMarkersShape();

      this.dispatchInstantEvent(this.d3map, 'refresh:tracking');
    },

    _updateSvgMarkersSoft: function (list) {

      var scale = this.mapData.getZoomCurrentScale();

      var listVisible = _.filter(list, function (d) { return !d.hidden; });
      this._updateRadiusForList(scale, list);

      this.markersRef
        .attr('r', function (d) { return d.r; })
        .attr('cx', function (d) { return d.x; })
        .attr('cy', function (d) { return d.y; });

      this._createForceLayout(listVisible);

      this.dispatchInstantEvent(this.d3map, 'refresh:tracking');
    },

    _updateRadiusForList: function (scale, list) {

      _.forEach(list, function (value) {
        value.r = this._scaleForMarker(scale, value);
      }, this);
    },
  });

  // ---------------------------------------------------
  //  HELPERS

  /**
   * This function allow to listen to the end transition event.
   * @param  {d3.transition}   transition is the transition we want to listen to.
   * @param  {Function}        callback   is the function to call when the transition is over.
   */
  function endall(transition, callback) {
    var n = 0;
    transition
        .each(function () { ++n; })
        .each("end", function () {
          if (!--n) {
            callback.apply(this, arguments);
          }
        });
  }

  return MapComponentMarkers;
});