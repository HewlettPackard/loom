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
define(['jquery'], function ($) {

  "use strict";

  /** @type BaseView */
  var BaseView = require('./../BaseView');

  /**
   * View displaying the details of an alert (count, level and textual description)
   * @class  AlertDetails
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var AlertDetails = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_AlertDetails
     * @final
     */
    constructorName: 'LOOM_AlertDetails',

    /**
     * @property className
     * @type {String}
     */
    className: 'mas-alertDetails',

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.$alertCount = $('<div class="mas-alertDetails--count"></div>').appendTo(this.el);
      this.$alertLevel = $('<div class="mas-alertDetails--level"></div>').appendTo(this.el);
      this.$alertDescription = $('<div class="mas-alertDetails--description"></div>').appendTo(this.el);
      this.listenTo(this.model, 'change', this.render);
      this.render();
    },

    /**
     * @method render
     */
    render: function () {
      this.$alertCount.html(this.model.get('count'));
      this.$alertLevel.html(this.model.get('level'));
      this.$alertDescription.html(this.model.get('description') || '???');
      this._updateCountDisplay();
    },

    /**
     * @method _updateCountDisplay
     * @private
     */
    _updateCountDisplay: function () {
      if (this.model.get('count')) {
        this.$el.removeClass('mas-alertDetails-noCount');
      } else {
        this.$el.addClass('mas-alertDetails-noCount');
      }
    }
  });

  return AlertDetails;
});
