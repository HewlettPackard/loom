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

  /** @type BaseView */
  var BaseView = require('./BaseView');

  /**
   * SortView displays how a Thread is sorted and allows the user to adjust the order of the sort
   * @class SortView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var SortView = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_SortView
     * @final
     */
    constructorName: 'LOOM_SortView',

    /**
     * @property className
     * @type {String}
     */
    className: 'mas-sortView',

    tagName: "li",

    /**
     * @property events
     * @type {Object}
     */
    events: {
      'click': function ($event) {
        if (!$event.originalEvent.preventOrderReversing) {
          this.reverseOrder();
        }
      }
    },

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.render();
      this.listenTo(this.model, 'change:order', this._renderOrder);
    },

    /**
     * @method render
     */
    render: function () {
      this.$el.text(this.model.get('property').get('name'));
      this._renderOrder();
    },

    /**
     * @method reverseOrder
     */
    reverseOrder: function () {
      this.model.reverseOrder();
    },

    /**
     * @method _renderOrder
     * @private
     */
    _renderOrder: function () {
      this.$el.removeClass(this.currentOrderClass);
      this.currentOrderClass = 'mas-sortView-' + this.model.get('order');
      this.$el.addClass(this.currentOrderClass);
    }
  });

  return SortView;
});
