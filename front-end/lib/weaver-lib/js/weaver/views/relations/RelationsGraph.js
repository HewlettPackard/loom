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
  var Q = require('q');
  var _ = require('lodash');
  var BaseView = require('../BaseView');
  var cytoscape = require('cytoscape');

  /**
   * @class  RelationsGraph
   * @module weaver
   * @submodule views.relations
   * @namespace  views.relations
   * @constructor
   * @extends BaseView
   */
  var RelationsGraph = BaseView.extend({

    /**
     * @property template
     * @type {String}
     */
    template: require('./RelationsGraph.html'),

    /**
     * @property manualRender
     * @type {Boolean}
     * @default true
     */
    manualRender: true,

    /**
     * @property events
     * @type {Object}
     */
    events: {
      'click [data-action=fitViewport]': function (event) {
        event.preventDefault();
        this.cy.fit([], RelationsGraph.FIT_PADDING);
      },
      'click [data-action=zoomIn]': function (event) {
        event.preventDefault();
        var extent = this.cy.extent();
        var position = {
          x: (extent.x2 - extent.x1) / 2,
          y: (extent.y2 - extent.y1) / 2
        };
        this.cy.animate({
          zoom: this.cy.zoom() * RelationsGraph.ZOOM_FACTOR,
          center: position
        });
      },
      'click [data-action=zoomOut]': function (event) {
        event.preventDefault();
        var extent = this.cy.extent();
        var position = {
          x: (extent.x2 - extent.x1) / 2,
          y: (extent.y2 - extent.y1) / 2
        };
        this.cy.animate({
          zoom: this.cy.zoom() / RelationsGraph.ZOOM_FACTOR,
          center: position
        });
      }
    },

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.elements = require('./lighthouse.json');
      this.boundRenderGraph = _.bind(this.renderGraph, this);
    },

    /**
     * @method render
     */
    render: function () {
      Q.all([
        RelationsGraph.loadStylesheet(),
        this.options.aggregatorClient.getSchema()
      ]).then(this.boundRenderGraph);
    },

    /**
     * @method renderGraph
     * @param styleAndContent
     */
    renderGraph: function (styleAndContent) {
      var stylesheet = styleAndContent[0];
      var graph = styleAndContent[1];
      this.$el.addClass('mas-relationsGraph-loaded');
      this.prepareGraph(graph);
      this.cy = cytoscape({
        container: this.el.querySelector('.mas-relationsGraph--graphContainer'),
        elements: graph,
        layout: {
          name: 'cose',
          circle: true,
          animate: false,
          root: this.getRootNodesIds(graph),
          nodeRepulsion: 7000000,
          padding: RelationsGraph.FIT_PADDING
        },
        style: stylesheet,
        userZoomingEnabled: true
      });
      this.cy.on('tap', '.mas-providerType', function (event) {
        var target = event.cyTarget;
        event.cy.fit(target, RelationsGraph.FIT_PADDING);
      });
      this.cy.on('tap', '.mas-relation', function (event) {
        var  labelledRelation = event.cy.$('.mas-relation-labelled');
        event.cy.startBatch();
        labelledRelation.removeClass('mas-relation-labelled');
        if (!labelledRelation.length || labelledRelation[0] !== event.cyTarget) {
          event.cyTarget.addClass('mas-relation-labelled');
        }
        event.cy.endBatch();
      });
    },

    /**
     * @method getRootNodesIds
     * @param graph
     * @returns {*}
     */
    getRootNodesIds: function (graph) {
      var rootNodeIds = _(graph.nodes).filter(function (node){
        return node.data.root;
      }).map(function (node) {
        return node.data.id;
      }).value();
      if (!rootNodeIds.length) {
        rootNodeIds = [graph.nodes[0].data.id];
      }
      return rootNodeIds;
    },

    /**
     * @method prepareGraph
     * @param graph
     */
    prepareGraph: function (graph) {
      this.prepareNodes(graph, this.getDisplayedItemTypeIds());
      this.prepareEdges(graph, this.getDisplayedRelationTypes());
    },

    /**
     * @method prepareNodes
     * @param graph
     * @param displayedItemTypes
     */
    prepareNodes: function (graph, displayedItemTypes) {
      var parents = {};
      _.forEach(graph.nodes, function (node) {
        node.data.name = node.data.name || node.data.id;
        node.classes = node.classes || '';
        node.classes += ' mas-itemType ';
        if (_.any(displayedItemTypes, function (itemType) {
          return itemType === node.data.id;
        })) {
          node.classes += ' mas-itemType-displayed ';
        }
        if (node.data.root) {
          node.classes += ' mas-itemType-root ';
        }
        node.x = node.y = 0;
        node.data.parent = node.data.id.split('-')[0];
        parents[node.data.parent] = true;
        return node;
      });
      _.forEach(parents, function (parent, parentId) {
        graph.nodes.push({
          data: {
            id: parentId,
            type: 'provider'
          },
          classes: 'mas-providerType'
        });
      });
    },

    /**
     * @method prepareEdges
     * @param graph
     * @param displayedRelationTypes
     */
    prepareEdges: function (graph, displayedRelationTypes) {
      _.forEach(graph.edges, function (edge) {
        edge.data.name = edge.data.name || "Standard";
        edge.classes = edge.classes || '';
        edge.classes += ' mas-relation ';
        if (_.any(displayedRelationTypes, function (relationType) {
          var parts = relationType.split(':');
          // TODO: Add relation type if it exists? Check File adapter
          var e = edge.data.source + ':' + edge.data.target;
          return _.every(parts, function (part) {
            return e.indexOf(part) !== -1;
          });
        })) {
          edge.classes += ' mas-relation-traversed ';
        } else {
          edge.classes += ' mas-relation-notTraversed ';
        }
        if (edge.data.stitch) {
          edge.classes += ' mas-relation-stitched';
        }
      });
    },

    /**
     * @method getDisplayedItemTypeIds
     * @returns {*}
     */
    getDisplayedItemTypeIds: function () {
      return _.uniq(this.options.displayedThreads.map(function (thread) {
        return thread.get('itemType').id;
      }));
    },

    /**
     * @method getDisplayedRelationTypes
     * @returns {*}
     */
    getDisplayedRelationTypes: function () {
      return _.uniq(_.flatten(this.options.displayedThreads.map(function (thread) {
        return thread.getRelationPaths();
      }), true));
    },

    /**
     * @method getLayoutRoot
     * @returns {*}
     */
    getLayoutRoot: function () {
      var selectedFiber = this.options.service.get('filters').at(0);
      if (selectedFiber) {
        // Need to account for aggregations itemType being aggregation:<ItemType ID>
        return selectedFiber.itemType.id.substring(selectedFiber.itemType.id.indexOf(':') + 1);
      } else {
        return this.elements.nodes[0].data.id;
      }
    }
  }, {
    FIT_PADDING: 30,
    ZOOM_FACTOR: 1.75,
    loadStylesheet: _.memoize(function () {
      return $.ajax('theme/css/theme-graph.css');
    })
  });

  return RelationsGraph;
});
