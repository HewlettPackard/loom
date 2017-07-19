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
  var Alert = require('weft/models/Alert');
  var Relations = require('./Element/Relations');
  var AttributeUpdatesMonitor = require('./Element/AttributeUpdatesMonitor');
  var UpdatedAttributesMonitor = require('./Element/UpdatedAttributesMonitor');
  
  /**
   * Base class for the elements displayed inside a Thread.
   * It maintains the links between related elements, making sure both elements in a relationship
   * know about each other
   *
   * @class Element
   * @module weft
   * @submodule models
   * @namespace models
   * @extends Backbone.Model
   */
  var Element = Backbone.Model.extend({

    constructorName: 'LOOM_Element',

    idAttribute: 'l.logicalId',

    /**
     * @property {Alert} alert Description of the alert happening on this element
     */

    /**
     * @property {Object} itemType Description of the itemType of this element
     */

    // PROPERTIES
    defaults: function () {

      return _.extend({

        /**
         * Flag marking that the element is matching the current filter
         * @attribute matchFilter
         * @type Boolean
         * @private
         */
        isMatchingFilter: false,

        /**
         * Flag set when the element is part of the filter
         * @property isPartOfFilter
         * @type Boolean
         * @private
         */
        isPartOfFilter: false,

        /**
         * Timeouts after which state changes stop being displayed
         * @property stateChangeTimeouts
         * @type {Object}
         * @default undefined
         */
        stateChangeTimeouts: undefined,

        /**
         * Flag set to tell the element no longer corresponds to the query of its parent
         * @property outdated
         * @type {Boolean}
         * @default false
         */
        outdated: false,

        /**
         * Flag set to tell the element has been selected by the user
         * @property selected
         * @type {Boolean}
         * @default false
         */
        selected: false,

        /**
         * The values of the metrics for that element, indexed by the ID of the metric
         * @property metricsValues
         * @type {Object}
         */
        metricsValues: {},

        /**
         * Index of the histories of the metric metric
         * @property metricHistories
         * @type {Object}
         */
        metricHistories: {}
      }, Object.getPrototypeOf(Relations).defaults());
    },

    metricsHistorySize: 1,

    // CONSTRUCTOR
    initialize: function (attributes, options) {

      options = options || {};
      this.itemType = options.itemType || {};
      this.alert = new Alert();

      Object.getPrototypeOf(Relations).initialize.apply(this, arguments);

      this.listenTo(this, 'change:metricsValues', this._updateMetricHistories);
      this.listenTo(this, 'sync', function() {
        this._updateMetricHistories(this, this.get('metricsValues') || {});
      });
      this._updateMetricHistories(this, this.get('metricsValues') || {});

      this._initializeAttributeMonitoring();
    },

    _initializeAttributeMonitoring: function () {

      this.attributeUpdatesMonitor = new AttributeUpdatesMonitor(this);
      this.updatedAttributesMonitor = new UpdatedAttributesMonitor({
        element: this
      });
    },

    // PUBLIC API

    /**
     * Returns the ItemType of the element (for TS compilation purpose)
     * @method getItemType
     * @return {Object} The ItemType of the element
     */
    getItemType: function () {
      return this.itemType;
    },

    /**
     * @method isFromProvider
     * @method isFromProvider
     * @param {String} providerId The ID of the provider
     * return {boolean} True if the Element comes from given provider, false otherwise
     */
    isFromProvider: function (providerId) {
      return _.contains(this.get('l.providerId'), providerId);
    },

    /**
     * Get the logical Id for this item
     * @method getActionInputsId
     * @returns {*|any}
     */
    getActionInputsId: function () {
      return this.get('l.logicalId');
    },

    /**
     * Get the aggregator associated with this Element
     * @method getAggregator
     * @return {models.AggregatorClient} The aggregator this element comes from
     */
    getAggregator: function () {
      if (!this.get('parent') || !this.get('parent').get('tapestry') || !this.get('parent').get('tapestry').aggregator) {
        return undefined;
      }
      return this.get('parent').get('tapestry').aggregator;
    },

    /**
     * Sets the state of this element for given duration
     * @method setState
     * @param {String} state    The new state for the element
     * @param {Number} duration The duration for which the element is marked with the state, in ms
     */
    setState: function (state, duration) {
      clearTimeout(this.stateTimeout);
      // Avoid using Backbone attributes to prevent risk of collisions
      // Triggers a custom event at the end to notify change
      this._doSetState(state);
      this.stateTimeout = setTimeout(_.bind(this.clearState, this), duration);
    },

    /**
     * Responsible for setting the state of the Element
     * Triggers the didSetState event
     * @method _doSetState
     * @param state
     * @private
     */
    _doSetState: function (state) {
      var previousState = this.state;
      this.state = state;
      if (previousState !== state) {
        /**
         * Sends a didSetState event on the object (not on Backbone.Events bus)
         * @event didSetState state
         * @param Element.STATE_ADDED|STATE_UPDATED The new state
         * @param Element this Object
         * @param Element.STATE_ADDED|STATE_UPDATED The previous state
         */
        this.trigger('didSetState', state, this, previousState);
      }
    },

    /**
     * Sets the element state to STATE_ADDED for the default duration
     * @method  setAddedState
     */
    setAddedState: function () {
      var duration = this.getStateChangeTimeout(Element.STATE_ADDED);
      this.setState(Element.STATE_ADDED, duration);
    },

    /**
     * Sets the element state to STATE_UPDATED for the default duration
     * @method  setUpdatedState
     */
    setUpdatedState: function () {
      var duration = this.getStateChangeTimeout(Element.STATE_UPDATED);
      this.setState(Element.STATE_UPDATED, duration);
    },

    /**
     * Clears current state
     */
    clearState: function () {
      clearTimeout(this.stateTimeout);
      this._doSetState();
    },

    /**
     * Returns the list of ActionDefinition available for this element
     * @return {Array}
     */
    getActionDefinitions: function () {
      return this.itemType.getActions();
    },

    /**
     * Returns the ActionDefinition with given id
     * @param  {String} actionId The id of the ActionDefinition
     * @return {models.ActionDefinition} Corresponding ActionDefinition
     */
    getActionDefinition: function (actionId) {

      return _.find(this.getActionDefinitions(), function (actionDefinition) {
        return actionDefinition.id === actionId;
      });
    },

    /**
     * Tells if the element has alerts
     * @method hasAlerts
     * @return {Boolean}
     */
    hasAlerts: function () {
      return !!this.alert.get('level');
    },

    /**
     * Returns the timeout after which the UI should stop displaying the notification
     * that the element changed
     * @method getStateChangeTimeout
     * @param state {String} The name of the state ('added', 'updated', 'removed')
     * @return {Number}
     */
    getStateChangeTimeout: function (state) {
      var timeouts = this.get('stateChangeTimeouts');
      if (!timeouts && this.get('parent')) {
        timeouts = this.get('parent').get('stateChangeTimeouts');
      }
      return timeouts ? timeouts[state] : 60000;
    },

    /**
     * Check if one of the displayable properties have changed
     * @method hasAnyDisplayablePropertyChanged
     * @return {Boolean}
     */
    hasAnyDisplayablePropertyChanged: function () {
      //todo: scary looking function, is it tested? refactor?
      // Not using the where syntax of filter because ignoreUpdate doesn't equal false
      var displayableProperties = _.indexBy(_.filter(this.itemType.getVisibleAttributes(), function (attribute) {
        return !attribute.ignoreUpdate;
      }), 'id');

      return !!_(this.changed).keys().find(function (key) {
        return displayableProperties.hasOwnProperty(key);
      });
    },

    /**
     * Returns list of the displayable properties that have changed
     * @method getDisplayablePropertiesThatHasChanged
     * @return {Array}
     */
    getDisplayablePropertiesThatHasChanged: function () {
      var displayableProperties = _.indexBy(this.itemType.getVisibleAttributes(), 'id');
      var changedPropertiesList = [];
      _(this.changed).keys().find(function (key) {
        if (displayableProperties.hasOwnProperty(key)) {
          changedPropertiesList.push(key);
        }
      });
      return changedPropertiesList;
    },

    // - Metrics
    /**
     * todo: Metrics should be in its own module. The concepts are currently spread out amongst the app
     * @method getAvailableMetric
     * @param metricId
     * @returns {T|*}
     */
    getAvailableMetric: function (metricId) {
      return _.find(this.itemType.getMetrics(), metricId);
    },

    /**
     * Returns the value for given metric
     * @method getMetricValue
     * @param metric {weft.models.Metric} The metric to retrieve the value of
     * @return {Number}
     */
    getMetricValue: function (metric) {
      return this.get(metric.id);
    },

    /**
     * Returns the history of values for the given metric
     * @method getMetricHistory
     * @param metric {models.Metric} The metric to retrieve the history of values
     * @return {Array}
     */
    getMetricHistory: function (metric) {

      return this.get('metricHistories')[metric.id];
    },

    // - Utilities
    /**
     * Checks if this element is part of given Collection of QueryResults
     * @param  {Backbone.Collection}  queryResults The Collection of QueryResults that might contain this element
     * @return {Boolean}              true if the element is in one of the QueryResults, false otherwise
     */
    isPartOf: function (queryResults) {

      return queryResults.any(function (queryResult) {

        return queryResult.hasElement(this);
      }, this);
    },

    // PRIVATE HELPERS
    _updateMetricHistories: function (element, metricValues) {
      var histories = this.get('metricHistories');
      this._removeLegacyValuesFromHistory(histories, metricValues);
      this._updateExistingValues(histories, metricValues);
      this.trigger('refresh', this);
    },

    // WONDER: Maybe having the parent clear the history for a given metric would be clearer
    _removeLegacyValuesFromHistory: function (histories, metricValues) {

      var metricsWithHistory = _.keys(histories);
      var metricsWithValues = _.keys(metricValues);
      _.forEach(metricsWithHistory, function (metricId) {

        if (metricsWithValues.indexOf(metricId) === -1) {
          delete histories[metricId];
        }
      });
    },

    _updateExistingValues: function (histories, metricValues) {

      _.forEach(metricValues, function (value, metricId) {

        var history = histories[metricId];
        if (!history) {
          history = [];
          histories[metricId] = history;
        }

        history.push(value);
        if (history.length > this.metricsHistorySize) {
          history.shift();
        }
      }, this);
    }
  });

  _.extend(Element.prototype, Relations);

  Element.STATE_ADDED = 'added';
  Element.STATE_UPDATED = 'updated';

  return Element;
});
