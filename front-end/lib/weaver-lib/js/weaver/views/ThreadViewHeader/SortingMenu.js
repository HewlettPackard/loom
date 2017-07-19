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
  var BaseView = require('weaver/views/BaseView');
  var PropertySelectorSideMenu = require('weaver/views/PropertySelectorSideMenu');
  var SortView = require('weaver/views/SortView');

  /**
   * SortingMenu provides a menu for selecting which attribute to use for sorting its model Thread
   * @class SortingMenu
   * @namespace  views.ThreadViewHeader
   * @module weaver
   * @submodule  views.ThreadViewHeader
   * @constructor
   */
  var SortingMenu = BaseView.extend({

    /**
     * @property {models.Thread} model
     */

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_SortingMenu
     * @final
     */
    constructorName: 'LOOM_SortingMenu',

    /**
     * @property className
     * @type {String}
     */
    className: 'mas-sortingMenu',

    /**
     * @property bindings
     * @type {Object}
     */
    bindings: {
      ':el': {
        classes: {
          'is-active': 'sort'
        }
      }
    },

    // events: {
    //   'click .mas-threadConfiguration--quicksort': function(event) {
    //     console.log(event);
    //   }
    // },

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this);
      // Need to use capture phase here so no fancy jQuery or Backbone event listening
      this.el.addEventListener('click', _.bind(this._preventOrderReversingOnSelection, this), true);
      this.propertySelector = new PropertySelectorSideMenu({
        title: 'Sorting',
        model: this._getModelAvailableSorts(),
        blankLabel: 'Default order',
        preventDeselectionOnSameClick: true,
        propertyView: SortView,
        thread: this.model,
        additionalItemClass: 'mas-threadConfiguration--quicksort'
      });

      this.propertySelector.$el.addClass('mas-menu--content');
      this._updateSelection();
      this.listenTo(this.propertySelector, 'change:selection', this._updateSort);
      this.el.appendChild(this.propertySelector.el);
      this._initializeModelListeners();

      this.render();
      if (this.model) {
        this.listenTo(this.model, 'change:outdated', this._updateDisabledState);
      }
    },

    /**
     * @method _initializeModelListeners
     * @private
     */
    _initializeModelListeners: function () {
      if (this.model) {
        this.listenTo(this.model.availableSorts, 'add remove', this._updateAvailableProperties);
        this.listenTo(this.model.availableSorts, 'add remove', this._updateDisabledState);
        this.listenTo(this.model, 'change:sort', this._updateSelection);
      }
    },

    /**
     * @method remove
     */
    remove: function () {
      BaseView.prototype.remove.apply(this, arguments);
      if (this.propertySelector) {
        this.propertySelector.remove();
      }
    },

    /**
     * @method render
     */
    render: function () {
      BaseView.prototype.render.apply(this);
      if (this.model) {
        this.stickit();
      }
      this.$el.addClass(this.className);
      //this._updateDisabledState();
    },

    /**
     * @method _preventOrderReversingOnSelection
     * @param event
     * @private
     */
    _preventOrderReversingOnSelection: function (event) {
      if (!event.target.classList.contains('mas-property-selected')) {
        event.preventOrderReversing = true;
      }
    },

    // /**
    //  * @method _updateDisabledState
    //  * @private
    //  */
    // _updateDisabledState: function () {
    //   if (this.model && this.model.availableSorts.size() && !this.model.get('outdated')) {
    //     this.enable();
    //   } else {
    //     this.disable();
    //   }
    // },

    /**
     * @method _updateSort
     * @param propertyID
     * @private
     */
    _updateSort: function (propertyID) {
      // Prevents sorting cancellations when the list of available sorts is not set yet
      if (this.model.get('sort') && this.model.get('sort').id === propertyID) {
        return;
      }
      var sort = this.model.getSort(propertyID);
      this.model.sortBy(sort);
    },

    /**
     * @method _updateAvailableProperties
     * @private
     */
    _updateAvailableProperties: function () {
      this.propertySelector.setProperties(this._getModelAvailableSorts());
    },

    /**
     * @method _updateSelection
     * @private
     */
    _updateSelection: function () {
      var currentSort = this.model && this.model.get('sort');
      this.propertySelector.select(currentSort ? currentSort.id : undefined);
    },

    /**
     * @method _getModelAvailableSorts
     * @returns {*}
     * @private
     */
    _getModelAvailableSorts: function () {
      if (this.model) {
        return _.indexBy(this.model.availableSorts.models, 'id');
      }
      return {};
    }
  });

  return SortingMenu;
});
