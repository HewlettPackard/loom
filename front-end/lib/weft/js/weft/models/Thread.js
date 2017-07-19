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
  var Backbone = require('backbone');
  var Operation = require('weft/models/Operation');
  var Element = require('weft/models/Element');
  var ItemType = require('weft/models/ItemType');
  var Metric = require('weft/models/Metric');
  var Item = require('weft/models/Item');
  var Aggregation = require('weft/models/Aggregation');
  var RemovedElementsCounter = require('weft/models/RemovedElementsCounter');
  var Query = require('weft/models/Query');
  var QueryResult = require('weft/models/QueryResult');
  var Sort = require('weft/models/Sort');

  /**
   * A Thread represents a group of items the user want to visualise (eg. servers, vms, applications)
   * @class Thread
   * @module weft
   * @namespace models
   * @submodule models
   * @extends Backbone.Model
   * @constructor
   */
  var Thread = Backbone.Model.extend({

    constructorName: 'LOOM_Thread',

    urlRoot: '/threads',

    // PROPERTIES
    defaults: function () {

      var parentDefaults = Element.prototype.defaults.call(this);
      var queryResult = new QueryResult();
      queryResult.get('threads').add(this);

      return _.extend(parentDefaults, {

        /**
         * The tapestry this Thread is displayed in
         * @attribute tapestry
         * @type {models.Tapestry}
         */
        tapestry: undefined,

        /**
         * Flag marking if the Thread can be queried or not
         * @attribute queryable
         * @type {Boolean}
         */
        queryable: false,

        /**
         * Flag marking if the Thread elements match current query
         * @attribute  outdated
         * @type {Boolean}
         */
        outdated: false,

        /**
         * Flag marking if the Thread's item type is available
         * @attribute unavailable
         * @type {Boolean}
         */
        unavailable: false,

        /**
         * The Aggregation displayed by this thread
         * @attribute  aggregation
         * @type {models.Aggregation}
         * @default  undefined
         */

        /**
         * The Query to run on the Thread's Aggregation
         * @attribute  query
         * @type {Query}
         */
        query: new Query({
          sortBy: new Sort({
            id: 'name',
            property: new Backbone.Model({
              id: 'name',
              name: 'Name'
            })
          }),
          aggregate: 45
        }),

        /**
         * The result of the Query
         * @attribute result
         * @type {QueryResult}
         */
        result: queryResult,

        /**
         * The Thread containing this Thread
         * @property parent
         * @type {models.Thread}
         * @default undefined
         */
        parent: undefined,

        /**
         * A human readable name for the Thread
         * @property name
         * @type {String}
         */
        name: 'Thread',

        /**
         * The list of elements displayed on the screen (could be items or threads aggregating/grouping items)
         * @property elements
         * @type {Backbone.Collection}
         */
        elements: new Backbone.Collection([], {
          comparator: 'l.index'
        }),

        /**
         * The itemType of this Thread
         * @attribute itemType
         * @type {ItemType|AggregationItemType}
         */

        /**
         * The Sort used to order the Thread's elements after poll
         * @attribute sort
         * @type {models.Sort}
         */
        sort: undefined,

        /**
         * Collection of metrics threads displayed on the thread
         * @property metrics
         * @type Backbone.Collection
         */
        metrics: new Backbone.Collection([], {
          model: Metric
        })
      });
    },

    // CONSTRUCTOR
    initialize: function () {

      this.listenTo(this, 'change:result', this._updateAvailableSorts);
      this.listenTo(this, 'change:result', this._initializeRelationsToResult);

      /**
       * The list of sort that can be performed on the Thread
       * @property {Backbone.Collection} availableSorts
       */
      this.availableSorts = new Backbone.Collection();
      this.listenTo(this.availableSorts, 'remove', this._maybeCancelSorting, this);

      this.on('change:query', function () {
        this.set('outdated', true);
      }, this);

      this.listenTo(this.get('elements'), 'reset', this.refreshMetricsRanges, this);
      this.listenTo(this.get('metrics'), 'add', this.refreshMetricRange, this);

      this.listenTo(this, 'change:sort', this._resetOrderOfPreviousSort, this);
      this.listenTo(this, 'change:sort', this._updateSortOrderListener, this);


      this.removedElementsCounter = new RemovedElementsCounter({
        thread: this
      });

      if (this.get('aggregation')) {
        this.get('query').set('inputs', [this.get('aggregation').id]);
      }

      this._initializeRelationsToResult(this, this.get('result'));
    },

    _initializeRelationsToResult: function (thread, result) {

      this._breakRelationsToPreviousResult();

      if (!result) {
        return;
      }

      if (!result.get('pending')) {
        this.resetElements(result.get('elements').models);
        this.set('outdated', false);
      }

      result.get('threads').add(this);

      this.listenTo(result, 'change:elements', function (result, elements) {

        if (elements) {
          this.resetElements(elements.models);
        }
      }, this);

      this.listenTo(result, 'change:pending', function (result, pending) {
        if (!pending) {
          this.set('outdated', false);
        }
      }, this);

      this.listenTo(result, 'change:contentType', this._updateAvailableSorts, this);
    },

    _updateAvailableSorts: function (queryResult) {

      var itemType = queryResult.get('itemType');
      if (itemType) {
        var attributes = itemType.getAttributesForOperation(Operation.SORT_BY_ID);
        var sorts = _.map(attributes, function (attribute) {
          return this._createNewSort(attribute.name, attribute.id);
        }, this);

        this.availableSorts.set(sorts);
      }
    },

    _createNewSort: function (name, id) {

      var sort = this.availableSorts.get(id);
      if (!sort) {
        sort = new Sort({
          id: id,
          property: new Backbone.Model({
            id: id,
            name: name
          })
        });
      }

      return sort;
    },

    _breakRelationsToPreviousResult: function () {

      var previousResult = this.previous('result');
      if (previousResult) {
        previousResult.get('threads').remove(this);
        this.stopListening(previousResult);
      }
    },

    // PUBLIC API
    findFiber: function (predicate) {

      return this.get('elements').find(predicate);
    },

    /**
     * Resets the elements of the Thread to given list of Elements
     * @param  {Array} elements The list of Elements to set as the new elements of the Thread
     * @return {Object}          The delta between the previous elements list and the new ones
     */
    resetElements: function resetElements(elements) {

      var previousElements = this.get('elements').toArray();
      this._maybeUpdateComparator(elements);
      this.get('elements').reset(elements);
      return this._notifyElementsReset(this.get('elements'), previousElements);
    },

    _maybeUpdateComparator: function (elements) {

      if (elements && elements[0] && elements[0].get('l.entityType') === 'Item') {
        if (!this.get('sort')) {
          this.sortBy(this.getSort('name'), true);
        }
      } else {
        this.sortBy(undefined, true);
      }
    },

    getAggregator: function () {
      return this.get('tapestry').aggregator;
    },

    getActionInputsId: function () {
      return this.get('itemType').id;
    },

    // OPERATIONS
    /**
     * Fetches the list of operations available for this Thread
     * @return {Promise} A promise for the list of operations
     */
    getAvailableOperations: function () {
      return this.get('tapestry').aggregator.getOperationsForItemType(this.get('itemType').id);
    },

    setPipeline: function (pipeline) {
      this.set('query', this.get('query').setPipeline(pipeline));
      return this;
    },

    limitWith: function (operation) {
      if (operation && _.isFunction(operation.toJSON)) {
        this.set('query', this.get('query').limitWith(operation.toJSON()));
      } else {
        this.set('query', this.get('query').limitWith(operation));
      }
      return this;
    },

    pushOperation: function (operation) {
      if (_.isFunction(operation.toJSON)) {
        this.set('query', this.get('query').pushOperation(operation.toJSON()));
      } else {
        this.set('query', this.get('query').pushOperation(operation));
      }
      return this;
    },

    unshiftOperation: function (operation) {
      if (_.isFunction(operation.toJSON)) {
        this.set('query', this.get('query').unshiftOperation(operation.toJSON()));
      } else {
        this.set('query', this.get('query').unshiftOperation(operation));
      }
      return this;
    },

    updateOperations: function (operation) {
      if (_.isFunction(operation.toJSON)) {
        this.set('query', this.get('query').updateOperations(operation.toJSON()));
      } else {
        this.set('query', this.get('query').updateOperations(operation));
      }
      return this;
    },

    removeOperations: function (operation) {
      if (_.isFunction(operation.toJSON)) {
        this.set('query', this.get('query').removeOperations(operation.toJSON()));
      } else {
        this.set('query', this.get('query').removeOperations(operation));
      }
      return this;
    },

    hasOperation: function () {
      return this.get('query').hasOperation.apply(this.get('query'), arguments);
    },

    getAttribute: function (attributeId) {
      var itemType = this.get('itemType');

      return itemType && itemType.attributes && itemType.attributes[attributeId];
    },

    /**
     * Returns an index of the attributes available for the operation with given ID
     * @param  {String} operationId The ID of the operation whose attributes to retrieve
     * @return {Array} The list of attributes
     */
    getAttributesForOperation: function (operationId) {

      var itemType = this.get('itemType');
      var attributeIds = itemType && itemType.operations && itemType.operations[operationId];
      return _.pick(itemType.attributes, attributeIds);
    },

    getOperation: function () {
      return this.get('query').getOperation.apply(this.get('query'), arguments);
    },

    /**
     * Simple forward call to hasLimit on the underlying query.
     * @param {any} !operation [optional] is the operation.
     */
    hasLimit: function () {
      return this.get('query').hasLimit.apply(this.get('query'), arguments);
    },

    // GROUPING
    getLastGroupByOperation: function () {

      var groupByOperationList = this.get('query').getOperation(Operation.GROUP_BY_ID);

      if (groupByOperationList.length > 0) {
        return _.last(groupByOperationList);
      }
    },

    /**
     * Returns a list of the properties this Thread's parent is grouped by,
     * in the order they appear in the query pipeline
     * @return {Array} The list of properties, empty if the parent has no GROUP_BY operation in its pipeline
     */
    getParentGrouping: function () {
      if (this.get('parent')) {
        var groupByOperations = this.get('parent').get('query').getOperation(Operation.GROUP_BY_ID);
        return _.map(groupByOperations, function (operation) {
          return operation.parameters.property;
        });
      }

      return [];
    },

    /**
     * Traverses the parent's chain to find the last attribute
     * the Thread chain was grouped by
     * @return {Object} The definition of found attribute
     */
    getLastParentGrouping: function () {

      var parent = this.get('parent');
      if (parent) {
        var groupByOperations = parent.get('query').getOperation(Operation.GROUP_BY_ID);
        if (groupByOperations.length > 0) {
          var attributeId = _.last(groupByOperations).parameters.property;
          return parent.get('itemType').attributes[attributeId];
        }
        return parent.getLastParentGrouping();
      }
    },

    /**
     * Returns true if the query contains a GROUP_BY operation.
     * If no argument is given, then it returns true only, if there's
     * at least one GROUP_BY operation.
     * @param  {String}  property [optional] is the name of the property.
     * @return {Boolean}          true if the query contains the given
     *                            grouping operation.
     */
    isGrouped: function (property) {

      var groupBy = this.get('query').getOperation(Operation.GROUP_BY_ID);

      if (arguments.length === 0) {
        return groupBy.length > 0;
      }

      var res = _.where(groupBy, { parameters: { property: property } });

      return res.length > 0;
    },

    _getSortCorrespondingToGrouping: function (propertyId) {

      if (propertyId) {
        return new Sort({
          id: 'name'
        });
      } else {
        new Sort({
          id: 'fullyQualifiedName'
        });
      }
    },

    // SORTING
    /**
     * Sorts the elements of the thread
     * @param  {models.Sort} sort           The sort settings (if undefined, will cancel the sorting)
     * @param  {Boolean}     comparatorOnly A flag to have the sortBy operation only update the element comparator and not actually perform a sort
     */
    sortBy: function (sort, comparatorOnly) {

      this.set('sort', sort);
      this._updateComparator(sort);
      if (!comparatorOnly) {
        this._doSort();
      }
    },

    getSort: function (sortId) {

      return this.availableSorts.get(sortId);
    },

    _resetOrderOfPreviousSort: function (thread) {

      var previousSort = thread.previous('sort');
      if (previousSort) {
        previousSort.resetOrder();
      }
    },

    _updateSortOrderListener: function (thread, sort) {

      var previousSort = thread.previous('sort');
      if (previousSort) { // Otherwise, we'd stop listening altogether
        thread.stopListening(thread.previous('sort'));
      }
      if (sort) {
        thread.listenTo(sort, 'change:order', function () {
          this._updateComparator(sort);
          this._doSort();
          this.trigger('change:sort.order', this, sort);
        }, this);
      }
    },

    _updateComparator: function (sort) {

      if (sort) {
        this.get('elements').comparator = sort.getComparator();
      } else {
        this.get('elements').comparator = 'l.index';
      }
    },

    _doSort: function () {
      this.get('elements').sort();

      this.trigger('sort:elements', this.get('elements'), {

        previousElements: this.get('elements').models,
        delta: {
          added: [],
          removed: []
        }
      });
    },

    _maybeCancelSorting: function (availableSorts, removedSort) {

      var currentSort = this.get('sort');
      if (currentSort && currentSort.id === removedSort) {
        this.sortBy();
      }
    },

    /**
     * Clears the Thread, removing all its `elements`, resetting its `resultId`
     * and the counter of its removed elements
     * @method clear
     */
    clear: function clear() {

      this.unset('resultId');
      //this.clearElementsRelations();
      //this.newElementsMarker.ignoreNextReset();
      this.resetElements();
      this.getRemovedElementsCounter().reset();
    },

    clearElementsRelations: function () {

      this.get('elements').forEach(function (element) {
        element.breakRelations();
      });
    },

    /**
     * Gets a counter for the number of elements being removed
     * from the Thread
     * @method getRemovedElementCounter
     * @return {models.RemovedElementCounter}
     */
    getRemovedElementsCounter: function () {

      return this.removedElementsCounter;
    },

    /**
     * Provides a summary of the number of items and alerts
     * contained in the Thread
     * @method getSummary
     * @return {Object} An object containing `numberOfItems` and  `numberOfAlerts`
     */
    getSummary: function () {

      return this.get('elements').reduce(function (summary, element) {

        if (element instanceof Item) {
          summary.numberOfItems += 1;
        } else {
          summary.numberOfItems += element.get('numberOfItems') || 0;
        }

        if (element.hasAlerts()) {
          summary.numberOfAlerts += element.alert.get('count') || 1;
        }

        return summary;
      }, {
        numberOfItems: 0,
        numberOfAlerts: 0
      });
    },

    // -- EXCLUDED ITEMS
    hasExcludedItems: function () {
      return this.get('result') && this.get('result').get('excludedItems') && this.get('result').get('excludedItems').get('numberOfItems');
    },

    // -- RELATION PATHS
    getRelationPaths: function () {
      return _.keys(this.get('elements').reduce(function (result, fiber) {

        // Hopefully not too slow
        var paths = _.flatten(_.values(fiber.get('l.relationPaths'), true));

        _.forEach(paths, function (path) {
          if (!result[path]) {
            result[path] = true;
          }
        });

        return result;
      }, {}));
    },

    // -- METRICS
    getAvailableMetrics: function () {
      var resultsMetrics;
      if (this.get('result') && this.get('result').get('itemType')) {
        resultsMetrics = this.get('result').get('itemType').getMetrics();
      }
      var selfMetrics = this.get('itemType').getMetrics();
      return _.merge({}, resultsMetrics, selfMetrics);
    },

    getAvailableMetric: function (metricId) {
      return this.getAvailableMetrics()[metricId];
    },

    getUnit: function (propertyId) {

      var definition = this.getAvailableMetric(propertyId);
      return definition && definition.get('unit');
    },

    refreshMetricsRanges: function () {

      this.get('metrics').forEach(this.refreshMetricRange, this);
    },

    refreshMetricRange: function (metric) {

      if (metric.get('dynamicRange')) {

        var values = this._extractMetricsValues(metric);
        metric.updateRange(values);
      }
    },

    _extractMetricsValues: function (metric) {

      return this.get('elements').map(function (element) {

        return element.getMetricValue(metric);
      });
    },

    getStateChangeTimeout: function () {
      return 60000;
    },

    /**
     * Sets the list of selected metrics.
     * Metrics will be cloned in the process so their range computation goes OK
     * @method setDisplayedMetrics
     * @param metrics {Array}
     */
    setDisplayedMetrics: function (metrics) {

      var clonedMetrics = this._getMetricsClone(metrics);
      this.get('metrics').set(clonedMetrics);
    },

    /**
     * Removes given metric from the displayed metrics
     * @method  removeDisplayedMetric
     * @param  {models.Metric} metric The metric to remove
     */
    removeDisplayedMetric: function (metric) {

      var displayedMetric = this.get('metrics').get(metric.id);
      this.get('metrics').remove(displayedMetric);
    },

    _getMetricsClone: function (metrics) {

      return _.map(metrics, this._getMetricClone, this);
    },

    _getMetricClone: function (metric) {

      var existingClone = this.get('metrics').get(metric.id);

      if (!existingClone) {
        existingClone = metric.clone();
      }

      return existingClone;
    },

    /**
     * Checks if the Thread contains items as its elements
     * @method isContainingItems
     * @return {Boolean}
     */
    isContainingItems: function () {

      var firstElement = this.get('elements').first();
      return firstElement instanceof Item;
    },

    /**
     * Checks if the Thread contains clusters as its elements
     * @method isContainingClusters
     * @return {Boolean}
     */
    isContainingClusters: function () {

      var firstElement = this.get('elements').first();
      return firstElement instanceof Aggregation && firstElement.get('l.tags') === Operation.BRAID_ID;
    },

    /**
     * Checks if the Thread contains groups as its elements
     * @method isContainingGroups
     * @return {Boolean}
     */
    isContainingGroups: function () {

      var firstElement = this.get('elements').first();
      return firstElement instanceof Aggregation && firstElement.get('l.tags') !== Operation.BRAID_ID;
    },

    // PRIVATE HELPERS
    _clearMetrics: function () {

      var metrics = this.get('metrics');
      while (!metrics.isEmpty()) {
        metrics.pop();
      }
    },

    /**
     * Propagates the reset event on the thread elements
     * @method _notifyElementsReset
     * @param elements {Backbone.Collection}
     * @param previousElements {Object}
     * @private
     */
    _notifyElementsReset: function (elements, previousElements) {

      var options = {};
      options.previousModels = previousElements;
      options.delta = this._computeDeltaWithPreviousElements(elements.toArray(), options.previousModels);
      _.forEach(options.delta.added, _.bind(function (element) {

        element.set('parent', this);
      }, this));
      this.trigger('reset:elements', elements, options);
      return options.delta;
    },

    _computeDeltaWithPreviousElements: function (newElements, previousElements) {

      return {
        added: _.difference(newElements, previousElements),
        removed: _.difference(previousElements, newElements)
      };
    },

    refreshResult: function () {

      this.set('polled', 'polling');
      var xhr = this.get('result').refresh({
        success: _.bind(function () {
          this.set('outdated', false);
        }, this),
        fibersIndex: this.get('tapestry') ? this.get('tapestry').get('fibersIndex') : undefined
      });

      xhr.then(_.bind(this._notifyPollingSuccess, this))
        .fail(_.bind(this._notifyPollingError, this))
        .done();

      return xhr;
    },

    _notifyPollingSuccess: function () {

      this.set('polled', 'success');
    },

    _notifyPollingError: function () {

      this.set('polled', 'failed');
    },

    url: function () {

      var context = this.get('tapestry') ? this.get('tapestry').url() : '';

      return context + this.urlRoot + '/' + this.id;
    },

    toJSON: function () {

      var result = this.pick('id', 'itemType', 'query');

      result.id = "" + result.id;

      var aggregationId;
      if (this.get('aggregation')) {
        aggregationId = this.get('aggregation').id;
      }

      if (result.itemType) {
        result.itemType = result.itemType.id;
      }

      if (result.query) {
        result.query = result.query.toJSON() || {};
      }

      if (aggregationId) {
        result.aggregation = aggregationId;
      }
      return result;
    },

    parse: function (json) {

      json = _.clone(json, true);

      if (json && json.itemType) {

        json.itemType = new ItemType(json.itemType);
      }

      if (json.query) {
        json.query = new Query(json.query, {
          parse: true
        });
      }

      return json;
    },

    isSameAs: function (thread) {

      if (thread) {

        if (this === thread) {
          return true;
        }

        return (this.get('itemType') && this.get('itemType').id) === (thread.get('itemType') && thread.get('itemType').id);
      }
    },

    clone: function () {

      var result = new Thread({
        name: this.get('name'),
        itemType: this.get('itemType'),
        query: this.get('query').clone()
      });

      return result;
    },

    createNestedThread: function (aggregation) {

      var nestedThread = this.clone();
      nestedThread.set({
        parent: this,
        aggregation: aggregation,
        name: aggregation.get('name'),
        query: this.createNestedThreadQuery(aggregation)
      });

      return nestedThread;
    },

    createNestedThreadQuery: function (aggregation) {
      return new Query({
        inputs: [aggregation.get('l.logicalId')],
        limit: this.get('query').getLimit()
      });
    }

  });



  return Thread;
});
