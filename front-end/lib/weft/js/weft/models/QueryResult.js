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
  var Item = require('weft/models/Item');
  var Aggregation = require('weft/models/Aggregation');
  var NewElementsMarker = require('./QueryResult/NewElementsMarker');
  var EventBus = require('weaver/utils/EventBus');

  /**
   * Representation of the result of a Query
   * @class QueryResult
   * @namespace models
   * @module weft
   * @constructor
   * @type {[type]}
   */
  var QueryResult = Backbone.Model.extend({

    urlRoot: '/results',

    /**
     * @property ENTITY_TYPE_AGGREGATION
     * @type {String}
     * @default Aggregation
     * @final
     */
    ENTITY_TYPE_AGGREGATION: 'Aggregation',

    defaults: function () {

      return {
        /**
         * The item type of the elements of the QueryResult
         * @attribute  itemType
         * @type {ItemType|AggregationItemType}
         */

        /**
         *
         * @attribute pending
         * @type {Boolean}
         * @default  true
         */
        pending: true,

        /**
         * Flag marking if the result do not correspond
         * to the query of their Thread
         * @type {Boolean}
         */
        outdated: false,

        /**
         * The elements
         * @attribute elements
         * @type {Backbone.Collection}
         */
        elements: undefined, // Makes sure an event gets fired when results come back empty

        /**
         * The list of Threads using this result
         * @attribute  threads
         * @type {Backbone.Collection}
         */
        threads: new Backbone.Collection()

        /**
         * An Aggregation representing items that were excluded from the results
         * due to some filtering happening in the query
         * @attribute excludedItems
         * @type {models.Aggregation}
         */
      };
    },

    initialize: function () {
      /**
       * @todo This should be made clearer that it provides new abilities via event listeners
       * I'm not sure about this pattern, it happens a lot in the codebase and seems to make things more complicated
       * than they need to be. The ability isn't injected in, its hard coded.. so why not have it directly on the class?
       */
      new NewElementsMarker(this);
    },

    /**
     * Checks if this QueryResult contains given element
     * @method  hasElement
     * @param  {models.Element}  element The element that might be contained in this QueryResult
     * @return {Boolean}
     */
    hasElement: function (element) {
      return this.get('elements') ? this.get('elements').contains(element) : false;
    },

    /**
     * Refreshes the result. Differs from Backbone.Model.fetch by processing the resulting elements asynchronously
     * @method  refresh
     * @param fibersIndex {Backbone.Collection}  The list of fibers into which Items and Aggregations
     *                                          should be looked up when parsing the response
     * @return {jqXHR} The jQuery XHR object used to request the results
     */
    refresh: function (fibersIndex) {
      this.cancelRefresh();
      // IMPROVE: A bit clunky to delegate back to standard Backbone `sync`
      if (this.get('aggregator')) {
        this._xhr = this.get('aggregator').send({
          type: 'GET',
          url: this.url()
        });
      } else {
        this._xhr = this.sync('read', this);
      }

      var self = this;
      this._xhr.then(function _handleRefreshResponse(response) {
        var parsedResponse = self.parse(response, fibersIndex);
        self.set(parsedResponse);
        // Refresh the sort on each threads (LOOM-1619)
        // todo: Why is it there and not at the Thread level, listening for 'reset:elements'?
        self.get('threads').forEach(function (thread) {
          thread.sortBy(thread.get('sort'));
        });

        var aggregator = self.get('aggregator');
        if (aggregator) {
          var selectedFiber = aggregator.get('selectedFiber');
          if (selectedFiber) {
            EventBus.trigger('fiber:custom:reselect', {fiberView: selectedFiber});
          }
        }

      });
      this._xhr.always(_.bind(function () {
        this._xhr = null;
      }, this));
      return this._xhr;
    },

    /**
     * @method  isRefreshing
     * @return {Boolean} `true` if this QueryResult is currently being refreshed, `false` otherwise
     */
    isRefreshing: function () {
      return !!this._xhr;
    },

    /**
     * Cancels current refresh if there is one
     * @method  cancelRefresh
     */
    cancelRefresh: function () {
      if (this._xhr) {
        this._xhr.abort();
      }
    },

    /**
     * Override Backbone's sync method to allow CORS requests
     * @param method
     * @param model
     * @param options
     * @returns {*|JQueryXHR}
     */
    sync: function (method, model, options) {
      options = options || {};
      options.crossDomain = true;
      options.xhrFields = options.xhrFields || {};
      options.xhrFields.withCredentials = true;
      return Backbone.Model.prototype.sync.call(this, method, model, options);
    },

    /**
     * Adds an index attribute to elements so they can be sorted back to their original order
     * @method  _addOriginalSortIndexAttribute
     * @param json
     * @private
     */
    _addOriginalSortIndexAttribute: function (json) {
      _.forEach(json.elements, function (element, index) {
        element.entity['l.index'] = index;
      });
    },

    /**
     * Parse the JSON result
     * @method parse
     * @param json
     * @param fibersIndex
     * @returns {{pending: boolean, contentType: (Array|*)}}
     */
    parse: function (json, fibersIndex) {
      var result = {
        pending: json.status === 'PENDING',
        contentType: json.elements && json.elements[0] && json.elements[0].entity['l.entityType']
      };
      if (json.excludedItems) {
        result.excludedItems = this._parseElement(json.excludedItems, json.itemType, fibersIndex);
      }
      this._addOriginalSortIndexAttribute(json);
      var parsedElements = this._parseElements(json, fibersIndex);
      result.elements = new Backbone.Collection(parsedElements);
      result.itemType = result.elements.at(0) ? result.elements.at(0).itemType : undefined;
      return result;
    },

    /**
     * Parse the elements from the JSON response
     * @param json
     * @param fibersIndex
     * @private
     */
    _parseElements: function (json, fibersIndex) {
      return _.map(json.elements, function (element) {
        return this._parseElement(element, json.itemType, fibersIndex);
      }, this);
    },

    /**
     * Parse an individual element from the JSON response
     * @param element
     * @param itemType
     * @param fibersIndex
     * @returns {*}
     * @private
     */
    _parseElement: function (element, itemType, fibersIndex) {
      _.merge(element.entity, _.omit(element, 'entity'));
      if (element.entity['l.entityType'] === this.ENTITY_TYPE_AGGREGATION) {
        this._parseAggregatedAttributes(element.entity);
      }
      var instance = this._getElementInstance(element.entity, itemType, fibersIndex);
      var alertProperties = this._parseAlert(element.entity);
      instance.alert.set(alertProperties);
      // let the model know we have just synced values into it
      instance.trigger('sync', element.entity);
      return instance;
    },

    /**
     * Parse alerts out of the JSON response
     * @param json
     * @returns {*}
     * @private
     */
    _parseAlert: function (json) {
      if (json['l.entityType'] === this.ENTITY_TYPE_AGGREGATION) {
        return {
          level: json.highestAlertLevel || 0,
          description: json.highestAlertDescription,
          count: json.alertCount || 0
        };
      } else {
        return {
          level: json['l.alertLevel'] || 0,
          description: json['l.alertDescription'] || '',
          count: 0
        };
      }
    },

    /**
     * This method is responsible for extracting the plottable aggregate stats from the response. It is where we
     * currently filter out everything apart from the avg value.
     *
     * It takes the matched plottable items, transforms the name, and maps the values directly onto the JSON
     * object passed. so JSON.<plottableX>.<Name>_<Variant> becomes JSON.<Name>, meaning the variant part is lost.
     * todo: All Weaver components expect this stripped named value to be directly on the object without the variant.
     *
     * @method _parseAggregatedAttributes
     * @param json
     * @private
     */
    _parseAggregatedAttributes: function (json) {
      var values = _.reduce(json.plottableAggregateStats, function (values, value, id) {
        if (id.indexOf('_avg') !== -1) {
          values[id.substr(0, id.length - 4)] = value;
        }
        return values;
      }, {});
      _.merge(json, values);
      // values written into metricsValues are consumed by the metrics history
      json.metricsValues = values;
    },

    /**
     * @method _getElementInstance
     * @param entity
     * @param itemType
     * @param fibersIndex
     * @returns {Aggregation|Item}
     * @private
     */
    _getElementInstance: function (entity, itemType, fibersIndex) {
      var currentInstance = fibersIndex.get(this._extractEntityId(entity));
      if (currentInstance) {
        currentInstance.set(entity);
        return currentInstance;
      }
      return this._getNewInstance(entity, itemType);
    },

    /**
     * Extracts the correct id for either an Aggregation or a Logical Item
     * @method _extractEntityId
     * @param entity
     * @returns {*}
     * @private
     */
    _extractEntityId: function (entity) {
      return entity['l.entityType'] === this.ENTITY_TYPE_AGGREGATION ? entity['l.semanticId'] : entity['l.logicalId'];
    },

    /**
     * @method _getNewInstance
     * @param entity
     * @param itemType
     * @returns {Aggregation|Item}
     * @private
     */
    _getNewInstance: function (entity, itemType) {
      if (entity['l.entityType'] === this.ENTITY_TYPE_AGGREGATION) {
        return new Aggregation(entity, {itemType: itemType});
      }
      return new Item(entity, {itemType: itemType});
    },

    /**
     * @method url
     * @returns {string}
     */
    url: function () {
      var thread = this.get('threads').at(0);
      var context = thread ? thread.url() : '';
      return context + this.urlRoot;
    }
  });

  return QueryResult;
});
