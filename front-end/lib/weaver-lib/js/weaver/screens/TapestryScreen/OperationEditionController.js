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
  var BaseView = require('../../views/BaseView');

  /**
   * @backbone no-initialize
   * @class OperationEditionController
   * @module weaver
   * @submodule screens.TapestryScreen
   * @namespace screens.TapestryScreen
   * @constructor
   * @extends BaseView
   */
  return BaseView.extend({

    events: {
      willEdit: 'stopEditionOfCurrentOperation',
      'click': function (event) {
        if (!event.isDefaultPrevented() || $(event.target).parents('mas-operation').length) {
          if (this.isOutside(event.target, '.mas-operation.is-editing')) {
            this.stopEditionOfCurrentOperation();
          }
          if (this.isOutside(event.target, '.mas-queryEditor--addOperation:not(.is-collapsed)')) {
            this.collapseAddOperationForm();
          }
        }
      }
    },

    /**
     * @method isOutside
     * @param element
     * @param selector
     * @returns {boolean}
     */
    isOutside: function (element, selector) {
      var $element = $(element);
      return !($element.is(selector + ', ' + selector + ' *'));
    },

    /**
     * @method stopEditionOfCurrentOperation
     */
    stopEditionOfCurrentOperation: function () {
      var view = this.$('.mas-operation.is-editing').data('view');
      if (view) {
        view.stopEditing();
      }
    },

    /**
     * @method collapseAddOperationForm
     */
    collapseAddOperationForm: function () {
      var view = this.$('.mas-queryEditor--addOperation:not(.is-collapsed)').data('view');
      if (view) {
        view.collapse();
      }
    }
  });
});
