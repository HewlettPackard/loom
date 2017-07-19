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
define(['lodash', 'numeral'], function (_, numeral) {

  "use strict";

  /**
   * Strategy for displaying the labels of clusters that makes it easy for a human to locate a specific cluster
   * @class ClusterLabellingStrategy
   * @namespace views
   * @module  weaver
   * @submodule views
   * @constructor
   * @param options {Object}
   * @param options.maxNumberOfLabels {Number} The maximum number of labels
   */
  var ClusterLabellingStrategy = function ClusterLabellingStrategy(options) {
    this.options = _.defaults(options || {}, {
      maxNumberOfLabels: 7
    });
  };

  _.extend(ClusterLabellingStrategy.prototype, {

    /**
     * The maximum number of labels that will appear
     * @property maxNumberOfLabels
     * @type {Number}
     * @default 7
     */
    //todo: investigate this pattern. I would prefer to explicitly state the property here instead of above

    /**
     * Updates the label values and position of the given list of ElementViews
     * @method updateLabels
     * @param views {Array}
     */
    updateLabels: function (views) {
      var numberOfItems = this._getNumberOfItems(views);
      var numberOfLabels = this._getNumberOfLabels(views);
      var itemsPerCluster = this._getNumberOfItemsPerCluster(views);
      var numberOfItemsPerLabel = this._roundUp(numberOfItems / numberOfLabels);
      var labelValue = 0;
      if (views.length > 2) {
        _.forEach(views, function (view) {
          if (view.model.get('maxIndex') >= labelValue) {
            view.updateLabel(numeral(labelValue).format('0.[0]a'));
            view.showLabel();
            this._updateLabelPosition(view, labelValue, itemsPerCluster);
            labelValue += numberOfItemsPerLabel;
          } else {
            view.hideLabel();
          }
        }, this);
      } else {
        _.forEach(views, function (view) {
          var labelValue = view.model.get('minIndex');
          view.updateLabel(numeral(labelValue).format('0.[0]a'));
          view.showLabel();
        });
      }
    },

    /**
     * @method hideLabels
     * @param views
     * @todo: can we deprecate?
     */
    hideLabels: function (views) {
      _.forEach(views, function (view) {
        view.hideLabel();
      });
    },

    /**
     * @method _roundup
     * @param number
     * @returns {number|*}
     * @private
     */
    _roundUp: function (number) {
      var orderOfMagnitude = Math.floor(Math.log(number) / Math.log(5));
      var power = Math.pow(5, orderOfMagnitude);
      number = (number / power);
      number = Math.ceil(number);
      number = number * power;
      return number;
    },

    /**
     * @method _reducePrecision
     * @param number
     * @returns {number}
     * @private
     * @todo: can we deprecate?
     */
    _reducePrecision: function (number) {
      var orderOfMagnitude = Math.floor(Math.log(number) / Math.log(10));
      var reducedOrder = orderOfMagnitude - 1;
      return Math.floor(number / Math.pow(10, reducedOrder)) * Math.pow(10, reducedOrder);
    },

    /**
     * @method _getNumberOfItems
     * @param views
     * @returns {number}
     * @private
     */
    _getNumberOfItems: function (views) {
      var firstIndex = _.first(views).model.get('minIndex');
      var lastIndex = _.last(views).model.get('maxIndex');
      return lastIndex - firstIndex;
    },

    /**
     * @method _getNumberOfLabels
     * @param views
     * @returns {number}
     * @private
     */
    _getNumberOfLabels: function (views) {
      return views.length < this.options.maxNumberOfLabels ? views.length : this.options.maxNumberOfLabels;
    },

    /**
     * @method _getNumberOfItemsPerCluster
     * @param views
     * @returns {number}
     * @private
     */
    _getNumberOfItemsPerCluster: function (views) {
      return views[0].model.get('maxIndex') - views[0].model.get('minIndex');
    },

    /**
     * @method _updateLabelPosition
     * @param view
     * @param labelValue
     * @param itemsPerCluster
     * @private
     */
    _updateLabelPosition: function (view, labelValue, itemsPerCluster) {
      var firstIndex = view.model.get('minIndex');
      var shifting = (labelValue - firstIndex) / itemsPerCluster;
      view.fiberOverview.$label.css('left', numeral(shifting).format(100 * shifting + '%'));
    }
  });

  return ClusterLabellingStrategy;

});
