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
  var BaseView = require('../../views/BaseView');
  var template = require('./BraidingUpdater.css');

  /**
   * BraidingUpdater adjusts the braiding of a BraidingController according to the width of the screen, ensuring
   * elements are wide enough (`minimumFibersWidth` option) but that there aren't too many on screen
   * @class  BraidingUpdate
   * @namespace  screens.TapestryScreen
   * @module weaver
   * @submodule screens.TapestryScreen
   * @constructor
   * @extends BaseView
   */
  var BraidingUpdater = BaseView.extend({

    el: document.body,


    config: {
      /**
       * The width of the viewport containing the fibers in percent of the screen size
       * @property {Number} fibersViewportWidth
       */
      fibersViewportWidth: 83,

      /**
       * The horizontal padding of the viewport displaying the fibers, in pixels
       * @property {Number} fibersViewportPadding
       */
      fibersViewportPadding: 10,

      /**
       * The minimum width of fibers, in pixels
       * @property {Number} minimumFibersWidth
       */
      minimumFibersWidth: 30,

      /**
       * The maximum number of fibers
       * @property {Number} maximumNumberOfFibers
       */
      maximumNumberOfFibers: 45,

      /**
       * The size taken by the excludedFibers element when displayed includes padding and margin
       * @property {Number} excludedFibersWidth
       */
      excludedFibersSize: 62,

      /**
       * The minimum width of selected element (in percent)
       * @property {Number} minimumSelectedElementWidth
       */
      minimumSelectedElementWidth: 25,

      /**
       * The minimum width of selected element (in pixels)
       * @type {Number}
       */
      minimumSelectedElementWidthPx: 200,

      /**
       * The width, in pixels, before which the minimumSelectedElementWidth is larger
       * @property {Number} breakpoint
       */
      breakpoint: 964, // 200px / (25% * 83%), rough computation

      /**
       * The width of the side menu that is going to reduce the viewport (in pixels)
       * @property {Number} sideMenuWidthPx
       */
      sideMenuWidthPx: 287
    },

    initialize: function (options) {
      this.options = _.defaults(options, this.config);
      BaseView.prototype.initialize.apply(this, this.options);

      this._resizeListener = _.debounce(_.bind(function () {
        this.measureViewport();
        this.recommendNewBraiding();
      }, this), 300);

      $(window).on('resize', this._resizeListener);

      this.$css = $('<style></style>')
        .addClass('mas-braidingUpdater--css', 'mas-braidingUpdater-' + this.cid)
        .appendTo(document.head);

      this.measureViewport();
      this.updateBraiding();

      this.listenTo(this.EventBus, 'screen:notify:braiding:update', this.updateBraiding);
    },

    /**
     * @method remove
     */
    remove: function () {
      BaseView.View.prototype.remove.apply(this, arguments);
      this.$css.remove();
      $(window).off('resize', this._resizeListener);
    },

    /**
     * @method getOptimalBraiding
     * @returns {Number}
     */
    getOptimalBraiding: function () {
      var fibersViewportWidth = this.getFibersViewportWidth();
      var selectedElementWidth = this.getSelectedElementWidth(fibersViewportWidth);
      var availableWidth = fibersViewportWidth - selectedElementWidth;
      var fibersFittingAvailableWidth = Math.floor(availableWidth / this.options.minimumFibersWidth) + 1; // +1 for the selected one
      return Math.min(fibersFittingAvailableWidth, this.options.maximumNumberOfFibers);
    },

    /**
     * @method getFibersViewportWidth
     * @returns {Number}
     */
    getFibersViewportWidth: function () {
      //todo: add bracketing to make math clearer. outcome is operator precedent dependant
      return (this.viewportWidth - this.options.sideMenuWidthPx) * this.options.fibersViewportWidth / 100 - this.options.fibersViewportPadding - this.options.excludedFibersSize;
    },

    /**
     * @method getSelectedElementWidth
     * @param availableWidth
     * @returns {*}
     */
    getSelectedElementWidth: function (availableWidth) {
      // Test out when to use minimum width in percent or px
      if (availableWidth < (100 * this.options.minimumSelectedElementWidthPx / this.options.minimumSelectedElementWidth)) {
        return this.options.minimumSelectedElementWidthPx;
      } else {
        return availableWidth * this.options.minimumSelectedElementWidth / 100;
      }
    },

    /**
     * @method measureViewport
     */
    measureViewport: function () {
      this.viewportWidth = document.body.clientWidth;
    },

    /**
     * @method recommendNewBraiding
     */
    recommendNewBraiding: function () {
      var optimalBraiding = this.getOptimalBraiding();
      if (optimalBraiding < this.model.get('braiding')) {
        this.EventBus.trigger('screen:notify:braiding:narrower', {state: 'narrower', optimalBraiding: optimalBraiding});
      } else if (optimalBraiding > this.model.get('braiding')) {
        this.EventBus.trigger('screen:notify:braiding:wider', {state: 'wider', optimalBraiding: optimalBraiding});
      } else {
        this.options.toolbar.hideNotification();
      }
    },

    /**
     * @method updateBraiding
     */
    updateBraiding: function () {
      var optimalBraiding = this.getOptimalBraiding();
      this.model.set('braiding', optimalBraiding);
      this.updateCSS(optimalBraiding);
      this.options.toolbar.hideNotification();
    },

    /**
     * @method updateCSS
     * @param braiding
     */
    updateCSS: function (braiding) {
      var updatedCSS = template
        .replace('{{noSelectionElementsWidth}}', (100 / braiding) / 1.5);

      this.$css.text(updatedCSS);
    }
  });

  return BraidingUpdater;
});
