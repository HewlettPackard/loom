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
define(['jquery', 'lodash', 'backbone'], function ($, _, Backbone) {

  "use strict";

  /** @type BaseView */
  var BaseView = require('./../BaseView');

  /**
   * View displaying a value or list of values as a bar graph
   * @class BarGraphView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var BarGraphView = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_ActionDialogView
     * @final
     */
    constructorName: 'LOOM_BarGraphView',

    /**
     * @property className
     * @type {String}
     */
    className: 'mas-graph',

    /**
     * @property MINIMUM_SIZE
     * @final
     * @type {Number}
     * @default 3.5
     */
    MINIMUM_SIZE: 3.5,

    /**
     * @property DEFAULT_MAXIMUM_NUMBER_OF_VALUES
     * @final
     * @type {Number}
     * @default 10
     */
    DEFAULT_MAXIMUM_NUMBER_OF_VALUES: 10,

    

    /**
     * @method initialize
     */
    initialize: function (options) {
      this.options = options || {};
      BaseView.prototype.initialize.apply(this, arguments);
      this.options.maximumNumberOfValues = this.options.maximumNumberOfValues || this.DEFAULT_MAXIMUM_NUMBER_OF_VALUES;
    },

    /**
     * @method _ensureElement
     * @private
     */
    _ensureElement: function () {
      if (!this.el) {
        var attrs = _.extend({}, _.result(this, 'attributes'));
        if (this.id) {
          attrs.id = _.result(this, 'id');
        }
        if (this.className) {
          attrs['class'] = _.result(this, 'className');
        }
        // todo: possibly remove Backbone here and use jQuery directly..
        var $el = Backbone.$(this._createElement()).attr(attrs);
        this.setElement($el, false);
      } else {
        this.setElement(_.result(this, 'el'), false);
      }
    },

    /**
     * @method _createElement
     * @returns {Element}
     * @private
     */
    _createElement: function () {
      return document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    },

    /**
     * Renders provided value or list of value from given origin (bottom of the graph if no origin is provided)
     * @param  {Number|Array} value    The value(s) to render
     * @param  {Number}       [origin] The origin of the graph
     */
    render: function (value, origin) {
      this.$el.empty();
      origin = origin || 0;
      if (_.isArray(value)) {
        var x = 0;
        var barWidth = 100 / this.options.maximumNumberOfValues;
        if (value.length < this.options.maximumNumberOfValues) {
          x = (this.options.maximumNumberOfValues - value.length) * barWidth;
        }
        _.forEach(value, function (value, index) {
          var $bar = this._renderBar(value, origin);
          $bar.attr({
            width: barWidth + '%',
            'x': (x + index * barWidth) + '%'
          });
        }, this);
        return;
      }
      this._renderBar(value, origin);
    },

    /**
     * @method renderOriginAxis
     * @param origin
     */
    renderOriginAxis: function (origin) {
      if (!this.$axis) {
        this.$axis = $(document.createElementNS('http://www.w3.org/2000/svg', 'line'));
      }
      this.$axis.appendTo(this.$el);
      this.$axis.attr({
        class: 'mas-graph--axis',
        x1: 0,
        x2: '100%',
        // `1 - origin` because graph is from the bottom
        y1: (1 - origin) * 100 + '%',
        y2: (1 - origin) * 100 + '%'
      }).css({
        'shape-rendering': 'crispEdges'
      });
      /*if (origin === 0) {
        // Without it the line doesn't get shown at the bottom :(
        this.$axis.css({
          'transform': 'translateY(-1px)'
        });
      }*/
    },

    /**
     * @method removeOriginAxis
     */
    removeOriginAxis: function () {
      if (this.$axis) {
        this.$axis.remove();
      }
    },

    /**
     * @method _renderBar
     * @param value
     * @param origin
     * @returns {JQuery|jQuery|HTMLElement}
     * @private
     */
    _renderBar: function (value, origin) {
      var top = Math.max(value, origin);
      var bottom = Math.min(value, origin);
      var $bar = $(document.createElementNS('http://www.w3.org/2000/svg', 'rect'));
      var height = ((top - bottom) * 100);
      var attributes;
      if ((value === origin || height > this.MINIMUM_SIZE) && value !== 0) {
        attributes = {
          height: height + '%'
        };
      } else {
        attributes = {
          height: '1px'
        };
        if (top === 0) {
          attributes.style = 'transform: translateY(-1px)';
        }
      }
      $bar.attr({
        'class': 'mas-graph--value',
        width: '100%',
        y: (1 - top) * 100 + '%'
      }).attr(attributes);
      this.el.appendChild($bar[0]);
      return $bar;
    }
  });

  return BarGraphView;

});
