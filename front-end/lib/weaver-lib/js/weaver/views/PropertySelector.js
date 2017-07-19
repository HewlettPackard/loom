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
  var $ = require('jquery');
    /** @type BaseView */
  var BaseView = require('./BaseView');

  var template = require('weaver/views/PropertySelector.html');

  /**
   * PropertySelector allows the user to pick properties
   * among a set of properties it is given as model
   * @class PropertySelector
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var PropertySelector = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_PropertySelector
     * @final
     */
    constructorName: 'LOOM_PropertySelector',

    /**
     * A ID to name or Metric index of the properties to chose from
     * @property model
     * @type {Object}
     * @default {}
     */

    /**
     * The title for this property selector
     * @property options.title
     * @type {String}
     * @default undefined
     */

    /**
     * The property or properties currently selected
     * @property options.selection
     * @type {String|Array}
     * @default undefined
     */

    /**
     * Flag preventing deletion of an item when an item already selected
     * gets clicked
     * @property options.preventDeselectionOnSameClick
     * @type {Boolean}
     * @default  undefined
     */

    /**
     * The label for the blank option. If not set, no blank option will be displayed
     * @property options.blankLabel
     * @type {String}
     * @default undefined
     */

    /**
     * When set to true, allows for multiple properties to be selected
     * @property options.multiple
     * @type {Boolean}
     */

    /**
     * @property tagName
     * @type {String}
     */
    tagName: 'ul',

    /**
     * @property className
     * @type {String}
     */
    className: 'mas-propertySelector',

    /**
     * @property events
     * @type {Object}
     */
    events: {
      'click .mas-propertySelector--property': '_handleClick',
      'click .mas-propertySelector-sort-refresh': '_setSortPersistency'
    },

    template: template,

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this._initializeDOM();
      this._initializeSelection();
      this.model = this.model || {};
      this.propertiesElements = {};
      this.$el.data('view', this);
      this.render();
    },

    /**
     * @method _initializeDOM
     * @private
     */
    _initializeDOM: function () {
      this.titleElement = this.el.querySelector('.mas-propertySelector--title');
      this.propertiesElement = this.el.querySelector('.mas-propertySelector--properties');
    },

    /**
     * @method _initializeSelection
     * @private
     */
    _initializeSelection: function () {
      if (this.options.multiple && !this.options.selection) {
        this.options.selection = [];
      }
    },

    /**
     * Sets the title of the property selector
     * @method setTitle
     * @param {String} title
     */
    setTitle: function (title) {
      this.options.title = title;
      this._updateTitle();
    },

    /**
     * Updates the properties available to choose from
     * @method setProperties
     * @param properties {Model}
     */
    setProperties: function (properties) {
      this.model = properties;
      this._removePropertyElements();
      this._updateProperties();
      this._updateSelection();
      this._highlightSelectedProperty(this.options.selection);
    },

    /**
     * Adds given property to the selection
     * @method select
     * @param property {String}
     * @param silent {Boolean} If true, the method won't trigger any event
     */
    select: function (property, silent) {
      if (this.options.multiple) {
        if (!_.isArray(this.options.selection)) {
          this.options.selection = [];
        }
        this.options.selection.push(property);
        this.$('.mas-property-blank').removeClass('mas-property-selected');
        if (!silent) {
          this.trigger('add:selection', property, this.options.selection);
        }
      } else {
        this.options.selection = property;
      }
      if (!silent) {
        this.trigger('change:selection', this.options.selection);
        BaseView.prototype.dispatchCustomEvent.call(this, 'change');
      }
      this._highlightSelectedProperty(property);
    },

    /**
     * Removes given property from the selection
     * @method unselect
     * @param property {String} The property to remove from selection
     */
    unselect: function (property) {
      if (this.options.multiple) {
        var removed = _.remove(this.options.selection, function (selection) {
          return selection === property;
        });
        if (removed.length) {
          this.trigger('remove:selection', removed, this.options.selection);
        }
        this._clearHighlighting(property);
        if (!(this.options.selection && this.options.selection.length)) {
          this._highlightBlankProperty();
        }
      } else {
        this.select();
      }
      this.trigger('change:selection', this.options.selection);
      BaseView.prototype.dispatchCustomEvent.call(this, 'change');
    },

    /**
     * Returns the current selection
     * @method getSelection
     * @return {String|Array}
     */
    getSelection: function () {
      return this.options.selection;
    },

    /**
     * Clears current selection, removing all selected properties
     * @method clearSelection
     */
    clearSelection: function () {
      if (this.options.multiple) {
        while (this.options.selection.length) {
          this.unselect(this.options.selection[0]);
        }
      } else {
        this.select();
      }
    },

    /**
     * @method isSelected
     * @param property {String}
     * @return {Boolean}
     */
    isSelected: function (property) {
      if (this.options.multiple) {
        return _.contains(this.options.selection, property);
      } else {
        return this.options.selection === property;
      }
    },

    /**
     * @method _handleClick
     * @param event
     * @private
     */
    _handleClick: function (event) {
      event.preventDefault();
      var property = $(event.target).data('property');
      if (_.isUndefined(property)) {
        this.clearSelection();
        return;
      }
      if (this.isSelected(property)) {
        if (!this.options.preventDeselectionOnSameClick) {
          this.unselect(property);
        }
      } else {
        this.select(property);
      }
    },

    /**
     * @method render
     */
    render: function () {
      this._updateTitle();
      this._updateProperties();
      this._highlightSelectedProperty(this.options.selection);
    },

    /**
     * @method _updateTitle
     * @private
     */
    _updateTitle: function () {
      this.titleElement.textContent = this.options.title || '';
    },

    /**
     * @method _updateProperties
     * @private
     */
    _updateProperties: function () {
      var fragment = document.createDocumentFragment();
      if (this.options.blankLabel) {
        fragment.appendChild(this._createBlankElement());
      }
      _.forEach(this.model, function (property, propertyID) {
        fragment.appendChild(this._createPropertyElement(property, propertyID));
      }, this);
      this.propertiesElement.appendChild(fragment);
    },

    /**
     * @method _clearHighlighting
     * @param property
     * @private
     */
    _clearHighlighting: function (property) {
      $(this.propertiesElements[property]).removeClass('mas-property-selected');
      BaseView.prototype.dispatchCustomEvent.call(this, 'didClearHighlighting');
    },

    /**
     * @method _highlightSelectedProperty
     * @param selectedProperty
     * @private
     */
    _highlightSelectedProperty: function (selectedProperty) {
      if (!this.options.multiple) {
        this.$('.mas-property-selected').removeClass('mas-property-selected');
        BaseView.prototype.dispatchCustomEvent.call(this, 'didClearHighlighting');
      }
      if (_.isUndefined(selectedProperty)) {
        this._highlightBlankProperty();
      } else {
        this._clearBlankPropertyHighlighting();
        $(this.propertiesElements[selectedProperty]).addClass('mas-property-selected');
      }
    },

    /**
     * @method _highlightBlankProperty
     * @private
     */
    _highlightBlankProperty: function () {
      this.$('.mas-property-blank').addClass('mas-property-selected');
    },

    /**
     * @method _clearBlankPropertyHighlighting
     * @private
     */
    _clearBlankPropertyHighlighting: function () {
      this.$('.mas-property-blank').removeClass('mas-property-selected');
    },

    /**
     * @method _removePropertyElements
     * @private
     */
    _removePropertyElements: function () {
      this.propertiesElements = {};
      $(this.propertiesElement).empty();
    },

    /**
     * @method _createBlankElement
     * @returns {Element}
     * @private
     */
    _createBlankElement: function () {
      var li = document.createElement('li');
      li.className = 'mas-propertySelector--property mas-property mas-property-blank mas-property-selected';
      li.textContent = this.options.blankLabel;
      return li;
    },

    /**
     * @method _createPropertyElement
     * @param property
     * @param propertyID
     * @returns {*}
     * @private
     */
    _createPropertyElement: function (property, propertyID) {
      if (property) {
        if (this.options.propertyView) {
          var view = new this.options.propertyView({
            model: property
          });
          this.propertiesElements[propertyID] = view.el;
          view.$el.addClass('mas-propertySelector--property mas-propertySelector--' + propertyID);
          view.$el.data('property', propertyID);
          return view.el;
        }
        var li = document.createElement('li');
        li.className = 'mas-propertySelector--property mas-property mas-propertySelector--' + propertyID;
        li.textContent = _.isString(property) ? property : (!!property.get) ? property.get('name') : property.name;
        $(li).data('property', propertyID);
        this.propertiesElements[propertyID] = li;
        return li;
      }
    },

    /**
     * @method _updateSelection
     * @private
     */
    _updateSelection: function () {
      if (this.options.multiple) {
        // Two step operation, otherwise, we'd be removing elements
        // from options.selection while iterating on it.
        _.filter(this.options.selection, function (selection) {
          return _.isUndefined(this.model[selection]);
        }, this).forEach(this.unselect, this);
      } else {
        if (_.isUndefined(this.model[this.options.selection])) {
          this.unselect(this.options.selection);
        }
      }
    }

  });

  return PropertySelector;
});
