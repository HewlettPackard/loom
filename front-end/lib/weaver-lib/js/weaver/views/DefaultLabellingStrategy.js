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
  var formattedAttributeValue = require('weaver/views/helpers/formattedAttributeValue');

  /**
   * Strategy for displaying the labels of that makes
   * it easy for a human to locate a specific items
   * @class DefaultLabellingStrategy
   * @namespace views
   * @module  weaver
   * @submodule views
   * @constructor
   */
  var DefaultLabellingStrategy = function DefaultLabellingStrategy() {
    /**
     * @property LABEL_MARGIN
     * @type {Number}
     * @default 10
     * @final
     */
    this.LABEL_MARGIN = 10;

    /**
     * @property unselectedItemWidth
     * @type {Number}
     * @default 1
     */
    this.unselectedItemWidth = 1;

    /**
     * @property threadWidth
     * @type {Number}
     * @default 1
     */
    this.threadWidth = 1;

  };

  _.extend(DefaultLabellingStrategy.prototype, {

    /**
     * Updates the label values and position of the given list of ElementViews
     * @method updateLabels
     * @param views {Array}
     */
    updateLabels: function (views) {
      this.updateLabelsContent(views);
      this.updateLabelsVisibility(views);
    },

    /**
     * @method updateLabelsContent
     * @param views
     */
    updateLabelsContent: function (views) {
      _.forEach(views, function (view) {
        view.updateLabel('<span class="mas-titleInContext--title">' + formattedAttributeValue(view.model, 'name') + '</span>');
      }, this);
    },

    /**
     * @method updateLabelsVisibility
     * @param views
     */
    updateLabelsVisibility: function (views) {
      this._updateLabelsMaxWidth(views);
      // Will force a layout, might be worth putting it in a rAF call
      requestAnimationFrame(_.bind(this._updateLabelsVisibility, this, views));
    },

    /**
     * @method _updateLabelsMaxWidth
     * @param views
     * @private
     */
    _updateLabelsMaxWidth: function (views) {
      _.forEach(views, function (view) {
        var maxWidth = 'none';
        if (this.unselectedItemWidth > 60) {
          maxWidth = this.unselectedItemWidth + 'px';
        }
        view.fiberOverview.$label[0].style.maxWidth = maxWidth;
        // Reset the position of the label
        view.fiberOverview.$label[0].style.left = '';
      }, this);
    },

    /**
     * @method _updateLabelsVisibility
     * @param views
     * @private
     */
    _updateLabelsVisibility: function (views) {
      var updates = this._computeUpdates(views, this.unselectedItemWidth);
      _.forEach(updates, function (update) {
        if (update.visible) {
          update.view.showLabel();
        } else {
          update.view.hideLabel();
        }
      });
    },

    /**
     * @method _computeUpdates
     * @param views
     * @returns {Array|TResult}
     * @private
     */
    _computeUpdates: function (views) {
      var nextAvailablePosition = 0;
      var lastVisibleLabel = '';
      var selectionPosition = this._getSelectionPosition(views);
      return _.reduce(views, function (result, view) {
        var leftEdge = view.el.offsetLeft;
        var width = view.fiberOverview.$label[0].scrollWidth;
        var rightEdge = leftEdge + width;
        var labelValue = view.fiberOverview.$label[0].innerHTML;
        var collidesWithPreviousLabel = nextAvailablePosition > leftEdge;
        var update = {
          view: view,
          visible: labelValue !== lastVisibleLabel
        };
        if (collidesWithPreviousLabel) {
          update.visible = false;
        } else {
          var collidesWithContainerRightEdge = rightEdge >= this.threadWidth;
          if (collidesWithContainerRightEdge) {
            update.visible = false;
          } else {
            update.visible = update.visible && true;
          }
        }
        if (update.visible) {
          nextAvailablePosition = rightEdge;
          lastVisibleLabel = labelValue;
        }
        if (this.unselectedItemWidth < 60) {
          nextAvailablePosition += this.LABEL_MARGIN / 2;
        }
        // Account for selected element
        if (nextAvailablePosition >= selectionPosition.left &&
            nextAvailablePosition <= selectionPosition.right) {
          update.visible = false;
          nextAvailablePosition = selectionPosition.right;
        }
        result.push(update);
        return result;
      }, [], this);
    },

    /**
     * @method _getSelectionPosition
     * @param views
     * @returns {{}|TResult}
     * @private
     */
    _getSelectionPosition: function (views) {
      return _.reduce(views, function (result, view) {
        if (view.model.get('selected')) {
          var left = view.el.offsetLeft;
          var width = view.el.clientWidth;
          return {
            left: left,
            right: left + width
          };
        }
        return result;
      }, {});
    }
  });

  DefaultLabellingStrategy.prototype.constructor = DefaultLabellingStrategy;

  return DefaultLabellingStrategy;

});
