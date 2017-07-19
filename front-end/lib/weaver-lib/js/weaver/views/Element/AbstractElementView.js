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

  var Aggregation = require('weft/models/Aggregation');
  /** @type BaseView */
  var BaseView = require('weaver/views/BaseView');

  /**
   * AbstractElementView is the base class for ElementView & MetricValueView
   * @class AbstractElementView
   * @namespace views
   * @module weaver
   * @submodule views
   * @constructor
   * @extends BaseView
   */
  var AbstractElementView = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @final
     * @default LOOM_AbstractElementView
     */
    constructorName: 'LOOM_AbstractElementView',

    /**
     * @property tagName
     * @type {String}
     * @default li
     */
    tagName: 'li',

    /**
     * @property className
     * @type {String}
     * @default mas-element
     */
    className: 'mas-element',

    /**
     * @property className
     * @type {Object}
     */
    events: {
      'click': 'selectElement'
    },

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.listenTo(this.model, 'change:displayed', function (model, displayed) {
        this._updateDisplayedState(displayed);
      });
      this.listenTo(this.model, 'change:isMatchingFilter', function (model, matchesFilter) {
        this._updateFilterState(matchesFilter);
      });
      // TODO: Move up to ThreadViewElements
      this.listenTo(this.model, 'change:numberOfItems', function () {
        this._updateWidth();
      });
      this.render();
    },

    /**
     * @method showLabel
     */
    showLabel: function () {
      this.$el.removeClass('mas-element-noLabel');
    },

    /**
     * @method showLabel
     */
    hideLabel: function () {
      this.$el.addClass('mas-element-noLabel');
    },

    /**
     * @method showLabel
     */
    render: function () {
      this.$el.addClass('mas-element-' + this.model.cid);
      this._updateFilterState(this.model.get('isMatchingFilter'));
      this._updateDisplayedState(this.model.get('displayed'));
      this._updateFiberTypeState();
      this._updateWidth();
    },

    /**
     * @method _updateDisplayedState
     * @param displayed
     * @private
     */
    _updateDisplayedState: function (displayed) {
      if (displayed) {
        this.$el.addClass('is-displayed');
      } else {
        this.$el.removeClass('is-displayed');
      }
    },

    /**
     * @method _updateFiberTypeState
     * @private
     */
    _updateFiberTypeState: function () {
      if (this.model instanceof Aggregation) {
        this.$el.addClass('is-aggregation');
      }
    },

    /**
     * Updates the width of the view so it matches
     * @method _updateWidth
     */
    _updateWidth: function () {
      var flex = this._getFlexValue();
      this.el.style.msFlexPositive = flex;
      this.el.style.flexGrow = flex;
      this.el.style.webkitFlexGrow = flex;
    },

    /**
     * @method _getFlexValue
     * @returns {*}
     * @private
     */
    _getFlexValue: function () {
      if (this.model instanceof Aggregation) {
        return this.model.get('numberOfItems');
      }
      return 1;
    },

    /**
     * @method _dispatchEvent
     * @param name
     * @param args
     * @private
     */
    _dispatchEvent: function (name, args) {
      var evt = document.createEvent('Event');
      evt.initEvent(name, true, true);
      evt.thread = this.model;
      evt.view = this;
      evt.args = args;
      this.el.dispatchEvent(evt);
    },

    /**
     * @method _updateFilterState
     * @param match
     * @private
     */
    _updateFilterState: function (match) {
      if (match) {
        this.$el.addClass('is-matchingFilter');
      } else {
        this.$el.removeClass('is-matchingFilter');
      }
    }
  });

  return AbstractElementView;
});
