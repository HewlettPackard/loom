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

  /**
   * FilterService handle which elements are currently filtered.
   * @class FilterService
   * @namespace services
   * @module weft
   * @submodule services
   * @extends Backbone.Model
   */
  var FilterService = Backbone.Model.extend({

    constructorName: 'LOOM_FilterService',

    defaults: function () {
      return {

        /**
         * A flag setting if the service is active or not
         * @property active
         * @type {boolean}
         */
        active: true,

        /**
         * The flag that will be set on elements part of the filter.
         * Set to undefined if you don't want these elements to be flagged.
         * @property filterFlag
         * @type {string}
         */
        filterFlag: 'isPartOfFilter',

        /**
         * The flag that will be set on elements matching the filter
         * @property filteredElementsFlag
         * @type {string}
         */
        filteredElementsFlag: 'isMatchingFilter',

        /**
         * The relations types to use for marking objects
         * @property relationTypes
         * @type {undefined|Array}
         */
        relationType: undefined,

        /**
         * How deep the traversal of the relation graph should
         * go when looking for related element
         * @type {integer}
         */
        depth: undefined,

        /**
         * @property filters
         * current filter collection
         * @type {Backbone.Collection}
         */
        filters: new Backbone.Collection(),

        /**
         * @property filteredElements
         * reference to Elements currently being filtered on.
         * @type {Backbone.Collection}
         */
        filteredElements: new Backbone.Collection()
      };
    },

    initialize: function () {
      if (this.get('active')) {
        this.activate();
      }
    },

    /**
     * @method activate
     */
    activate: function () {
      this.set('active', true);
      this.listenTo(this, 'change:relationType', this.filter, this);
      this.listenTo(this.get('filters'), 'remove', this.onRemoveFilter, this);
      this.listenTo(this.get('filters'), 'add', this.filter, this);
      this.listenTo(this.get('filters'), 'add', this._flagFilter, this);
      this.listenTo(this.get('filters'), 'add:relations', this._maybeAddMatchingFlag, this);
      this.listenTo(this.get('filters'), 'change:l.relationTypes', this.filter, this);
      this.listenTo(this, 'change:depth', this.filter);

      // No maybe here, the element just got unrelated to one of the filters
      this.listenTo(this.get('filters'), 'remove:relations', this._removeMatchingFlag, this);
      this.flagFilters();
      this.filter();
    },

    /**
     * @method deactivate
     */
    deactivate: function () {
      this.set('active', false);
      this.stopListening();
      this.clearFlags();
    },

    /**
     * @method flagFilters
     */
    flagFilters: function () {
      this.get('filters').forEach(this._flagFilter, this);
    },

    /**
     * @method clearFilterFlags
     * @param element
     */
    clearFilterFlags: function (element) {
      element.set(this.get('filterFlag'), false);
      element.set(this.get('filteredElementsFlag'), false);
    },

    /**
     * @method clearFlags
     */
    clearFlags: function () {
      this.get('filters').forEach(this.clearFilterFlags, this);
      this.get('filteredElements').forEach(function (element) {
        element.set(this.get('filteredElementsFlag'), false);
      }, this);
      this.get('filteredElements').reset();
    },

    /**
     * Adds element to the selection.
     * No-op if the element is currently filtered on
     * @method addFilter
     * @param element {Backbone.Model} The element to add to the filter
     */
    addFilter: function (element) {
      this.get('filters').add(element);
    },

    /**
     * Removes element from the filter
     * @method removeFilter
     * @param element {Backbone.Model} The element to remove from the filter
     */
    removeFilter: function (element) {
      this.get('filters').remove(element);
    },

    /**
     * @method setFilter
     * @param elements
     */
    setFilter: function (elements) {
      this.get('filters').set(elements);
    },

    /**
     * @method matchesFilter
     * @param element
     * @returns {*}
     */
    matchesFilter: function (element) {
      return this.get('filters').every(function (filter) {
        return filter.isRelatedTo(element, this.get('relationType'), this.get('depth'));
      }, this);
    },

    /**
     * Removes element from the filter
     * @method onRemoveFilter
     * @param element {Backbone.Model} The element to remove from the filter
     */
    onRemoveFilter: function (element) {
      this.clearFilterFlags(element);
      this.filter();
    },

    /**
     * @method _flagFilter
     * @param element
     * @private
     */
    _flagFilter: function (element) {
      element.set(this.get('filterFlag'), true);
      element.set(this.get('filteredElementsFlag'), true);
    },

    /**
     * @method _maybeAddMatchingFlag
     * @param element
     * @private
     */
    _maybeAddMatchingFlag: function (element) {
      if (this.matchesFilter(element)) {
        element.set(this.get('filteredElementsFlag'), true);
        this.get('filteredElements').add(element);
      }
    },

    /**
     * @method _removeMatchingFlag
     * @param element
     * @private
     */
    _removeMatchingFlag: function (element) {
      element.set(this.get('filteredElementsFlag'), false);
      this.get('filteredElements').remove(element);
    },

    /**
     * Update the filtered elements
     * @method filter
     */
    filter: function () {
      //get elements that match the filters
      var matchingElements = _.intersection.apply(_, this.getListsOfRelatedElements());
      var removedElements = _.difference(this.get('filteredElements').models, matchingElements);
      var newElements = _.difference(matchingElements, this.get('filteredElements').models);

      // Remove all removed elements
      removedElements.forEach(function (element) {
        element.set(this.get('filteredElementsFlag'), false);
      }, this);

      // then apply filter to new elements
      newElements.forEach(function (element) {
        element.set(this.get('filteredElementsFlag'), true);
      }, this);

      //keep reference to filtered elements
      this.set('filteredElements', new Backbone.Collection(matchingElements));
    },

    /**
     * @method getListsOfRelatedElements
     * @returns {*}
     */
    getListsOfRelatedElements: function () {
      return this.get('filters').map(function (filter) {
        return filter.getRelatedElements(this.get('relationType'), this.get('depth'));
      }, this);
    }
  });

  return FilterService;
});
