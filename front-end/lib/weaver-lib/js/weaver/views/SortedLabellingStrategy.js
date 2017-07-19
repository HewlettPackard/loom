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
  var DefaultLabellingStrategy = require('./DefaultLabellingStrategy');

  /**
   * Strategy for displaying the labels when the fibers are sorted.
   * @class SortedLabellingStrategy
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends DefaultLabellingStrategy
   * @constructor
   */
  function SortedLabellingStrategy() {
    DefaultLabellingStrategy.prototype.constructor.apply(this, arguments);
  }

  SortedLabellingStrategy.prototype = Object.create(DefaultLabellingStrategy.prototype);

  _.merge(SortedLabellingStrategy.prototype, {

    /**
     * @method updateLabelsContent
     * @param views
     */
    updateLabelsContent: function (views) {
      var parts;
      _.forEach(views, function (view) {
        parts = [];
        if (!this.isContainingClusters && this.attribute !== 'name') {
          parts.push('<span class="mas-titleInContext--context">' + formattedAttributeValue(view.model, 'name') + '</span>');
        }
        var sortValue = view.model.get(this.attribute);
        if (sortValue) {
          parts.push('<span class="mas-titleInContext--title">' + formattedAttributeValue(view.model, this.attribute, {short: true}) + '</span>');
        }
        var viewLabel;
        if (parts.length > 1) {
          viewLabel = parts[0] +
                      '<span class="mas-titleInContext--separator">&nbsp;(</span>' +
                      parts[1] +
                      '<span class="mas-titleInContext--separator">)</span>';
        } else {
          viewLabel = parts[0];
        }
        view.updateLabel(viewLabel);
      }, this);
    }
  });

  return SortedLabellingStrategy;
});
