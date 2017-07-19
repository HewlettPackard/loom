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

  var BaseView = require('weaver/views/BaseView');
  var template = require('./EditableOperationView.html');

  /**
   * EditableOperationView provides the base mechanisms to create views rendering Query operations and switch
   * between displaying the operation and editing the operation's parameters.
   * @class EditableOperationView
   * @namespace views.QueryEditor
   * @module  weaver
   * @submodule views.QueryEditor
   * @constructor
   * @extends BaseView
   */
  return BaseView.extend({

    tagName: 'li',

    className: 'mas-operation',

    template: template,

    events: {
      'submit': function (event) {
        event.preventDefault();
        this.updateOperation();
      }
    },

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.render();
    },

    /**
     * Switches the view into its edition state
     * @method edit
     */
    edit: function () {
      if (this.$el.hasClass('is-disabled')) {
        return;
      }
      /**
       * Notifies that the view is going to enter its edition state
       * @event willEdit
       */
      this.dispatchCustomEvent('willEdit');
      this.options.editing = true;
      this.render();
    },

    /**
     * Switches the view to its display state
     * @method  stopEditing
     */
    stopEditing: function () {
      this.options.editing = false;
      this.render();
    },

    /**
     * @method render
     */
    render: function () {
      this.$el.attr('data-operator', this.model.operator);
      this.renderSuggestedState();
      this.renderMoveButtons();
      if (this.options.editing) {
        this.renderEditionState();
      } else {
        this.renderDisplayState();
      }
    },

    /**
     * @method renderSuggestedState
     */
    renderSuggestedState: function () {
      if (this.model.suggested) {
        this.$el.addClass('is-suggested');
        this.$('[data-action=removeOperation],[data-action=moveAfter],[data-action=moveBefore]').attr('disabled', 'disabled');
      }
    },

    /**
     * @method renderMoveButtons
     */
    renderMoveButtons: function () {
      if (this.model.first) {
        this.$('[data-action=moveBefore]').attr('disabled', 'disabled');
      }
      if (this.model.last) {
        this.$('[data-action=moveAfter]').attr('disabled', 'disabled');
      }
    },

    /**
     * Gives the focus to the view, or more precisely to the element with a `mas-operation--input` class
     * inside the view
     * @method focus
     */
    focus: function () {
      // Separate from edit, as it needs to be called once the view
      // is actually in the DOM for the elements to be focused
      this.$('.mas-operation--input').focus()
        .select();
    },

    /**
     * Renders the edition state of the view. Provides a base behaviour to be completed by child classes implementation
     * @method renderEditionState
     */
    renderEditionState: function () {
      this.$el.addClass('is-editing');
      this.dispatchCustomEvent('didRenderEditionState');
    },

    /**
     * Renders the display state of the view. Provides a base behaviour to be completed by child classes implementation
     * @method renderDisplayState
     */
    renderDisplayState: function () {
      this.$el.removeClass('is-editing');
      this.dispatchCustomEvent('didRenderDisplayState');
    },

    /**
     * Enable this view
     * @method enable
     */
    enable: function () {
      this.$el.removeClass('is-disabled');
    },

    /**
     * Disable the view.
     * @method disable
     */
    disable: function () {
      this.$el.addClass('is-disabled');
    },

    /**
     * Updates the operation with the values filled in by users
     * Provides a base behaviour to be completed by child classes implementation
     * @param {Boolean} hasNoParameters A flag showing if the operations parameter are filled in or not
     * @method updateOperation
     */
    updateOperation: function (hasNoParameters) {
      this.stopEditing();
      /**
       * Notifies that the operation has been updated with the values filled in by the users
       * @event didUpdateOperation
       */
      this.model.edited = true;
      this.dispatchCustomEvent('didUpdateOperation', {
        hasNoParameters: hasNoParameters
      });
    }
  });
});
