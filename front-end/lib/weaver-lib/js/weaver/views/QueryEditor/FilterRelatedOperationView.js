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
  var _ = require('lodash');
  /** DefaultOperationView */
  var DefaultOperationView = require('./DefaultOperationView');
  var $buttons = $(require('./FilterRelatedOperationView.buttons.html'));

  /**
   * @class FilterRelatedOperationView
   * @namespace views.QueryEditor
   * @module  weaver
   * @submodule views.QueryEditor
   * @constructor
   * @extends DefaultOperationView
   */
  var FilterRelatedOperationView = DefaultOperationView.extend({

    events: _.defaults({
      'click [data-action=use-selection]': function (event) {
        event.preventDefault();
        var $selection = $('.mas-element.is-selected');
        if ($selection.length) {
          this.fill($selection);
          this.updateOperation();
        }
      },
      'click [data-action=select-fiber]': function (event) {
        event.preventDefault();
        if (this.isSelecting) {
          this.endSelection();
        } else {
          this.startSelection();
        }
      },
      'click [data-action=cancel-selection]': function (event) {
        event.preventDefault();
        this.endSelection();
      }
    }, DefaultOperationView.prototype.events),

    /**
     * @method initialize
     */
    initialize: function () {
      DefaultOperationView.prototype.initialize.apply(this, arguments);
      this.$el.addClass('mas-filterRelatedOperation');
      this.$('.mas-operation--form').prepend($buttons.clone());
      this._boundSelectElement = _.bind(this.selectElement, this);
    },

    /**
     * @method getFiberId
     * @returns {*}
     */
    getFiberId: function () {
      return this.model.parameters.id;
    },

    /**
     * @method selectElement
     * @param event
     */
    selectElement: function (event) {
      event.preventDefault();
      var $target = $(event.target).closest('.mas-element');
      if ($target.length) {
        this.fill($target);
        this.endSelection();
        this.updateOperation();
      }
    },

    /**
     * @method startSelection
     */
    startSelection: function () {
      this.isSelecting = true;
      this.$el.addClass('is-selecting');
      document.body.addEventListener('click', this._boundSelectElement, true);
      var $title = this.$('.mas-operation--title.mas-operation--edit');
      this.title = $title.text();
      $title.html('Pick a fiber (<a data-action="cancel-selection">cancel</a>)');
    },

    /**
     * @method endSelection
     */
    endSelection: function () {
      this.isSelecting = false;
      document.body.removeEventListener('click', this._boundSelectElement, true);
      this.$el.removeClass('is-selecting');
      this.$('.mas-operation--title.mas-operation--edit').text(this.title);
    },

    /**
     * @method fill
     * @param $elementView
     */
    fill: function ($elementView) {
      var selectionId = $elementView.data('view').model.get('l.logicalId');
      this.$('[name=id]').val(selectionId);
    },

    /**
     * @method renderTitle
     */
    renderTitle: function () {
      var title = this.getOperationTitle();
      var fiberTitle = this.getFiberTitle();
      if (fiberTitle) {
        title += ' (<span class="mas-filterRelatedOperation--fiberTitle">' + fiberTitle + '</span>)';
      }
      this.$('.mas-operation--title').html(title);
    },

    /**
     * @method getFiberTitle
     * @returns {*}
     */
    getFiberTitle: function () {
      if (this.model.parameters.id) {
        // Not great to use the HTML to fetch a model, but there's no
        // list of all the fibers :(
        var $fiber = $('[data-id="' + this.model.parameters.id + '"]');
        if ($fiber.length) {
          return $fiber.data('view').model.get('name');
        }
      }
    },

    /**
     * @method renderEditionForm
     */
    renderEditionForm: function () {
      DefaultOperationView.prototype.renderEditionForm.apply(this, arguments);
      this._updateUseSelectionAvailability();
    },

    /**
     * @method _updateUseSelectionAvailability
     * @private
     */
    _updateUseSelectionAvailability: function () {
      if ($('.mas-element.is-selected').length) {
        this.$('[data-action=use-selection]').removeAttr('disabled');
      } else {
        this.$('[data-action=use-selection]').attr('disabled', true);
      }
    }
  });

  return FilterRelatedOperationView;
});
