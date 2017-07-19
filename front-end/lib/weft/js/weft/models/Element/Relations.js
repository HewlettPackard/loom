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

  /**
   * Mixin providing the API for handling relations between elements
   * @mixin Relations
   * @class Relations
   * @module weft
   * @submodule models.element
   * @namespace models.element
   */
  var Relations = Object.create({

    defaults: function () {
      return {
        /**
         * An index by ID of the relations of this object
         *
         * @attribute relations
         * @type {Object}
         */
        relations: Object.create(null),

        /**
         * An index of the specific relation types this element has with other elements
         * Key is the type of relation and value is the list of the IDs of the other elements
         * part in this type of relation with this element
         *
         * @attribute relationTypes
         * @type {Object}
         */
        'l.relationTypes': {}
      };
    },

    initialize: function () {

      /**
       * todo: thi doesn't seem to ever get used? Can I delete it?
       * @property {Object} _relations An index by ID of the relations of this object
       * @private
       */
      this._relations = Object.create(null);
    }
  });


  var defaultMapper = function (fibre) {
    return fibre.get('l.logicalId');
  };
  defaultMapper.keyName = "logicalId";

  _.extend(Relations, {

    /**
     * Returns the elements this element is related to, including explicit relations
     * expressed in the `relations` properties and element's ancestors
     *
     * @method getRelatedElements
     * @param  [relationTypes] {String|Array} One or several specific type(s) of relations to filter related elements.
     *                                     If not provided, returns all related elements.
     * @param  [depth]         {number}       The depth limit for the relationships graph traversal.
     * @return {Array}
     */
    // TODO: Rename to getRelations
    getRelatedElements: function (relationTypes, depth) {

      if (!_.isEmpty(relationTypes)) {

        var relatedIDs = this._getIDsForRelationTypes(relationTypes, depth);
        return this._getRelatedElementInstances(relatedIDs);

      } else {
        return _.values(this.get('relations'));
      }
    },

    /**
     * Returns an expanded graph starting from this element.
     * The value returned is an Array that can be directly serialized in JSON without
     * any further transformation. It uses the following conventions:
     *
     *     - The first element of the list is equivalent to `this`
     *     - Each neighbors integer value correspond to a valid index in the array
     *     - Links are reported only once between items.
     *
     * The result has the following interface:
     *     Array<{
     *         neighbors: { [relationName: string]: Array<Number> };
     *         [mapper.keyName]: typeof(mapper());
     *     }>
     *
     * @method getRelationsGraph
     * @param [mapper] {Function} A function that takes a fibre and return a value mapped.
     */
    getRelationsGraph: function (mapper) {

      if (!mapper) {
        mapper = defaultMapper;
      }
      if (!mapper.keyName) {
        mapper.keyName = "fibre";
      }

      var graph = [];
      var indices = {};
      var nextNode = this._createGraphNode(this, 0);
      var fibers = this.get('relations');
      fibers = this._mapFibersToRelations(fibers);

      var root = { neighbors: {} };
      root[mapper.keyName] = mapper(this);

      graph.push(root);
      indices[this.get('l.logicalId')] = 0;

      this._traverseRelationsGraph(fibers, Infinity, nextNode, function () {
        return true;
      }, function (node, fromNode, edgeName) {

        var fromNodeIndex = indices[fromNode.fiber.get('l.logicalId')];
        var nodeIndex;

        var logicalId = node.fiber.get('l.logicalId');
        // Is the node already registered ?
        if (logicalId in indices) {
          nodeIndex = indices[logicalId];
        } else {
          nodeIndex = graph.length;
          indices[logicalId] = nodeIndex;

          var newNode = { neighbors: {} };
          newNode[mapper.keyName] = mapper(node.fiber);
          graph.push(newNode);
        }

        // Update the previous node:
        var neighbors = graph[fromNodeIndex].neighbors;
        neighbors[edgeName] = neighbors[edgeName] || [];
        neighbors[edgeName].push(nodeIndex);
      });

      return graph;
    },

    _reportPossibleNodes: function (fibers, depth, relationTypes) {
      // The next node to traverse
      var nextNode = this._createGraphNode(this, 0);
      var visitedNodes = {};

      // Traverse the sub-graph of relationships between nodes reachable from `this`.
      // The traversal will only keep nodes that can be reached by following edges
      // with a name that belong to `relationTypes`.
      // The edge name is currently of the form:
      //          <itemType>:<itemType>(:<relationType>)
      //
      this._traverseRelationsGraph(fibers, depth, nextNode, function (edgeName) {
        return _.intersection(edgeName.split(':'), relationTypes).length > 0;
      }, function (node) {
        visitedNodes[node.fiber.get('l.logicalId')] = true;
      });

      return _.keys(visitedNodes);
    },

    _mapFibersToRelations: function (fibers) {
      return _.mapValues(fibers, function (fiber) {
        return this._createGraphNode(fiber);
      }, this);
    },

    _createGraphNode: function (fiber, depthFromSelf) {
      // Note: Zero is false, so we have to make sure that depthFromSelf is
      //       undefined.
      if (_.isUndefined(depthFromSelf)) {
        depthFromSelf = Number.MAX_VALUE;
      }
      return {
        fiber: fiber,
        depthFromSelf: depthFromSelf,
        // Only keep the edges that starts from this node (omit indirect ones)
        edges: _.omit(fiber.get('l.relationTypes'), function (_, key) {
          return !(key in fiber.itemType.attributes);
        })
      };
    },

    _traverseRelationsGraph: function (fibers, depth, nextNode, isEdgeValid, reporter) {

      var stackElement;
      var possibleRelations = this.get('relations');
      var stack = [{
        node: nextNode,
        ignoreThoseEdges: {},
        ignoreThoseNodes: {}
      }];
      var globallyIgnoredIds = {};
      var tuplesAlreadySeen = {};

      function tuple(node, fromNode, edgeName) {
        return fromNode.fiber.get('l.logicalId') +
          ':' + edgeName + ':' +
          node.fiber.get('l.logicalId');
      }

      /*jshint -W083 */
      // Depth-first traversal. To switch to Breadth-first, simply change
      // the pop with a `shift()`.
      while ((stackElement = stack.pop())) {

        // If the node has a path which is to far from
        // the root don't visit it.
        if (stackElement.node.depthFromSelf < depth) {

          /*jshint -W083 */
          _.forEach(stackElement.node.edges, function (nodeLogicalIds, edgeName) {

            if (isEdgeValid(edgeName) && !(edgeName in stackElement.ignoreThoseEdges)) {

              _.forEach(nodeLogicalIds, function (nodeLogicalId) {

                if (!(nodeLogicalId in stackElement.ignoreThoseNodes) &&
                     (nodeLogicalId in possibleRelations))
                {
                  var node = fibers[nodeLogicalId];

                  // Update the depth if it is a shorter path.
                  node.depthFromSelf   = Math.min(node.depthFromSelf, stackElement.node.depthFromSelf + 1);

                  // Should we carry on the traversal or is it a node that we have already seen ?
                  if (!(nodeLogicalId in globallyIgnoredIds)) {

                    var ignoreThoseEdges = _.cloneDeep(stackElement.ignoreThoseEdges);
                    ignoreThoseEdges[edgeName] = true;
                    var ignoreThoseNodes = _.cloneDeep(stackElement.ignoreThoseNodes);
                    ignoreThoseNodes[stackElement.node.fiber.get('l.logicalId')] = true;

                    // Ignore the name in further depth traversals:
                    globallyIgnoredIds[nodeLogicalId] = true;

                    // Note that having ignoreThoseEdges as a stack element
                    // is different than having it as part of the node which is
                    // shared during the traversal.
                    stack.push({
                      node: node,
                      ignoreThoseEdges: ignoreThoseEdges,
                      ignoreThoseNodes: ignoreThoseNodes
                    });
                  }

                  // Always report the link even if the node won't be traversed
                  // because it has already be seen before. We only check for
                  // the tuple (node, fromNode, edgeName).
                  if (!(tuple(node, stackElement.node, edgeName) in tuplesAlreadySeen)) {

                    // Report the edge
                    reporter(node, stackElement.node, edgeName);

                    // Prevent seeing this edge again:
                    tuplesAlreadySeen[tuple(node, stackElement.node, edgeName)] = true;
                    tuplesAlreadySeen[tuple(stackElement.node, node, edgeName)] = true;
                  }
                }
              });
            }

          });
          /*jshint +W083 */
        }
      }
      /*jshint +W083 */
    },

    _getIDsForRelationTypes: function (relationTypes, depth) {

      if (_.isString(relationTypes)) {
        relationTypes = [relationTypes];
      }

      if (_.isUndefined(depth)) {
        // Accept all nodes
        depth = Infinity;
      }

      // Obtains the list of fibers.
      var fibers = this.get('relations');
      // Convert it in a more suitable format
      fibers = this._mapFibersToRelations(fibers);

      return this._reportPossibleNodes(fibers, depth, relationTypes);
    },

    _getRelatedElementInstances: function (relatedElementsIDList) {

      var everyRelations = this.get('relations');
      var instance;
      return _.reduce(relatedElementsIDList, function (result, relatedElementID) {
        instance = everyRelations[relatedElementID];
        if (instance) {
          result.push(instance);
        }
        return result;
      }, []);
    },

    /**
     * Checks if given `possibleRelation` element has a relationship of any of the `relationTypes` with this element with a path
     * which is no longer than depth.
     * If no `relationTypes` are provided, it checks if the possible relation has any relationship with this element at all.
     * @method isRelatedTo
     * @param {models.Element}  possibleRelation  The element which might be related to this element
     * @param {String|Array}    [relationTypes]   One or several relation types the possible relation might have with this fiber
     * @param {number}          [depth]           The depth for the graph traversal.
     * @return {Boolean}
     */
    isRelatedTo: function (possibleRelation, relationTypes, depth) {

      if (!_.isEmpty(relationTypes)) {
        var relatedIDs = this._getIDsForRelationTypes(relationTypes, depth);
        return _.contains(relatedIDs, possibleRelation.get('l.logicalId'));
      } else {
        return !!this.get('relations')[possibleRelation.get('l.logicalId')];
      }
    },

    /**
     * Adds a relation of given type
     * @param {[type]} relation     [description]
     */
    addRelation: function (relation) {

      if (!this.isRelatedTo(relation)) {
        this.get('relations')[relation.get('l.logicalId')] = relation;
        relation.addRelation(this);
        this.trigger('add:relations', relation, this);
      }
    },

    /**
     * Removes the relation of given `relationType` with given `relation`
     * @param  {models.Element}  relation
     * @param  {String}          relationType
     */
    removeRelation: function (relation, relationType) {

      if (this.isRelatedTo(relation, relationType)) {
        delete this.get('relations')[relation.get('l.logicalId')];
        relation.removeRelation(this);
        this.trigger('remove:relations', relation, this);
      }
    },

    /**
     * Updates the relations of this element to match given list of relations
     * @method updateRelations
     * @param relations {Array} The related elements
     */
    updateRelations: function (relations) {

      var relationIds = _.map(relations, function (relation) {
        return relation.get('l.logicalId');
      });

      var relationsToBreak = _.omit(this.get('relations'), relationIds);

      _.forEach(relationsToBreak, function (relation) {
        this.removeRelation(relation);
      }, this);

      var _relationTypesByRelationID = _.reduce(this.get('l.relationTypes'), function (result, relationIDs, relationTypeID) {

        _.forEach(relationIDs, function (relationID) {
          if (!result[relationID]) {
            result[relationID] = [];
          }

          result[relationID].push(relationTypeID);
        });

        return result;
      }, {});

      _.forEach(relations, function (relation) {
        this.addRelation(relation);
        relation.addRelationTypes(this, _relationTypesByRelationID[relation.get('l.logicalId')]);
      }, this);
    },

    /**
     *
     * @method addRelationTypes
     * @param relation
     * @param relationTypes
     */
    addRelationTypes: function (relation, relationTypes) {
      var types = this.get('l.relationTypes');
      var itemType = this.itemType.id.split('-')[1];
      var relationItemType = relation.itemType.id.split('-')[1];
      _.forEach(relationTypes, function (relationType) {

        // Only create links for direct relations
        // Ideally, this should come from pre-treatment on the l.relationTypes list
        // either on the server or just as it gets to the client side
        if (relationType.indexOf(itemType) !== -1 && relationType.indexOf(relationItemType) !== -1) {
          if (!types[relationType]) {
            types[relationType] = [];
          }

          if (!_.contains(types[relationType], relation.get('l.logicalId'))) {
            types[relationType].push(relation.get('l.logicalId'));
          }
        }
      }, this);
      this.set('l.relationTypes', types);
      /**
       * Send a change event for relationship types
       * Event is sent on the object this is mixed into to and not on the global Backbone bus
       * @event change:l.relationTypes
       */
      this.trigger('change:l.relationTypes');
    },

    /**
     * Breaks all the relations the element have
     * @method breakRelations
     */
    breakRelations: function () {
      _.forEach(this.get('relations'), function (relation) {
        relation.removeRelation(this);
      }, this);

      this.set('relations', Object.create(null));
      this._relations = Object.create(null);
    }
  });

  return Relations;
});
