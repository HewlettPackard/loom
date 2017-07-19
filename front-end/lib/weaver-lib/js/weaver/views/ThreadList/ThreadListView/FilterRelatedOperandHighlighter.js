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

  var $ = require('jquery');
  /** @type BaseView */
  var BaseView = require('weaver/views/BaseView');
  var Operation = require('weft/models/Operation');

  /**
   * @class  FilterRelatedOperandHighlighter
   * @module weaver
   * @submodule views.ThreadListView
   * @namespace  views.ThreadListView
   * @constructor
   * @extends BaseView
   */
  var FilterRelatedOperandHighlighter = BaseView.extend({

    /**
     * @property events
     * @type {Object}
     */
    events: {
      'didRenderEditionState': function (event) {
        var $target = $(event.target);
        if (this._isFilterRelatedOperationView($target)) {
          this.highlightFiber($target.data('view').getFiberId());
        }
      },
      'didRenderDisplayState': function (event) {
        if (this._isFilterRelatedOperationView($(event.target))) {
          this.clearHighlighting();
        }
      },
      'didCollapse .mas-queryEditor': function () {
        this.clearHighlighting();
      },
      'didRemoveOperation': function (event) {
        var operation = event.originalEvent.operation;
        if (this._isFilterRelatedOperationView(operation)) {
          this.clearHighlighting();
        }
      }
    },

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
    },

    /**
     * @method _isFilterRelatedOperationView
     * @param operation
     * @returns {boolean}
     * @private
     */
    _isFilterRelatedOperationView: function (operation) {
      return operation.operator === Operation.FILTER_RELATED_ID;
    },

    /**
     * @method highlightFiber
     * @param fiberId
     */
    highlightFiber: function (fiberId) {
      this.$('[data-id="' + fiberId + '"]').addClass(FilterRelatedOperandHighlighter.CLASSNAME);
    },

    /**
     * @method clearHighlighting
     */
    clearHighlighting: function () {
      this.$('.' + FilterRelatedOperandHighlighter.CLASSNAME).removeClass(FilterRelatedOperandHighlighter.CLASSNAME);
    }
  });

  FilterRelatedOperandHighlighter.CLASSNAME = 'is-operandOfFilterRelated';

  return FilterRelatedOperandHighlighter;
});
