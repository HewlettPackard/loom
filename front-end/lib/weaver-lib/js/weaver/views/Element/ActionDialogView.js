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
  /** @type BaseView */
  var BaseView = require('weaver/views/BaseView');
  var $ = require('jquery');
  var Q = require('q');
  var template = require('./ActionDialogView.html');
  var LabeledInputView = require('weaver/views/LabeledInputView');
  var LabeledFileInputView = require('weaver/views/LabeledFileInputView');
  var confirm = require('weaver/utils/confirm');
  var FileResponse = require('weaver/utils/FileResponse');
  require('weaver/utils/jquery.serializeObject');

  /**
   * DialogViews displays an interface to to execute actions upon elements (items ?)
   * @class ActionDialogView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var ActionDialogView = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_ActionDialogView
     * @final
     */
    constructorName: 'LOOM_ActionDialogView',

    /**
     * @property tagName
     * @type {String}
     */
    tagName: 'div',

    /**
     * @property className
     * @type {String}
     */
    className: 'mas-dialog',

    /**
     * The template used to help structuring the view
     * @property template
     * @type {String}
     */
    template: template,

    /**
     * Whether this should display the extended view or not
     */
    extendedView: false,

    /**
     * @property events
     * @type {Object}
     */
    events: {
      'submit': 'submit',
      'click .mas-dialog--close': function() {
        this.EventBus.trigger('view:action:close', this);
        this.close();
      },
      'click .mas-dialog--cancel': function() {
        this.EventBus.trigger('view:action:cancel', this);
        //this.cancel();
      }
    },

    /**
     * @property TYPE_ENUMERATED
     * @final
     * @type {String}
     * @default ENUMERATED
     */
    TYPE_ENUMERATED: 'ENUMERATED',

    /**
     * @property TYPE_STRING
     * @final
     * @type {String}
     * @default STRING
     */
    TYPE_STRING: 'STRING',

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.extendedView = this.options.extendedView || false;
      this.$el.find('.mas-dialog--close').addClass('mas-isHidden');
      this.$el.find('.mas-dialog--loading').addClass('mas-isHidden');
      this.$('.mas-form').attr('method', 'post');
      this.listenTo(this.EventBus, 'action:server:send', this.updateUiOnSend);
      this.listenTo(this.EventBus, 'action:server:send:success', this.updateUiOnSuccess);
      this.listenTo(this.EventBus, 'action:server:send:fail', this.updateUiOnFailure);
      this.render();
    },

    /**
     * @method render
     */
    render: function () {
      this._updateContent();
    },

    /**
     * Reflow the submit action dialog process
     * @todo this is work in progress and should not be considered final
     * @todo A dialog should not know how to communicate with a server
     * @todo I would like to remove all of this out of the dialog and into actual processors
     *
     * @method submit
     * @public
     */
    submit: function (event) {
      event.preventDefault();


      this.promptForConfirmation().then(_.bind(function (confirmed) {
        if (confirmed) {
          var inputs = $(event.target).serializeObject();
          if (this._validate(inputs)) {
            var files = $(event.target).find('input[type=file]');
            if (files.length > 0) {
              FileResponse.readSingleFileContents(inputs, files[0].name, files[0].files)
              .then(function (inputs) {
                this.executeModelAction(inputs);
              }.bind(this));
            } else {
              this.executeModelAction(inputs);
            }
          }
        }
      }, this));
    },

    /**
     * Executes the action for a model
     * @method executeModelAction
     * @param inputs
     */
    executeModelAction: function(inputs) {
      this.EventBus.trigger('action:server:send', {action: this.model, values: inputs});
      this.model.execute(this.options.element, inputs).then(
        _.bind(this.onSuccess, this)
      ).fail(
        _.bind(this.onFail, this)
      );
    },

    /**
     * Update the ui when the action is sent to the server
     * Can accept option event param that contains the action and inputs
     * @method
     */
    updateUiOnSend: function() {
      // @todo: Create different states for the dialog view and handle the visibility
      // of the different parts via CSS only
      this.$el.find('.mas-dialog--content').addClass('mas-isInvisible');
      this.$el.find('.mas-dialog--submit').addClass('mas-isHidden');
      this.$el.find('.mas-dialog--cancel').addClass('mas-isHidden');
      this.$el.find('.mas-dialog--close').removeClass('mas-isHidden');
      this.$el.find('.mas-dialog--loading').removeClass('mas-isHidden');
    },

    /**
     * Hook to enable the API to define confirmation states on actions
     * @method promptForConfirmation
     * @returns {*}
     */
    promptForConfirmation: function () {
      if (this.model.get('confirm')) {
        return confirm.confirm(this.model.get('confirm'));
      } else {
        var deferred = Q.defer();
        deferred.resolve(true);
        return deferred.promise;
      }
    },

    /**
     * Separate the logic from the UI
     * @method onSuccess
     */
    onSuccess: function (response) {
      //this.targetElement.hideDialog();
      this.EventBus.trigger('action:server:send:success', {action: this.model, response: response});
    },

    /**
     * @method onFail
     * @param response
     */
    onFail: function (response) {
      this.EventBus.trigger('action:server:send:fail', {action: this.model, response: response});
    },

    /**
     * Updated the UI now that the action has succeeded.
     * Optional param 'event' contains action and server response
     */
    updateUiOnSuccess: function() {
      this.$el.find('.mas-dialog--loading').addClass('mas-isHidden');
      this.$el.append('<div class="mas-dialog--response mas-dialog--success">Action completed</div>');
    },

    /**
     * Updated the UI now that the action has failed.
     * @param event
     */
    updateUiOnFailure: function(event) {
      this.$el.find('.mas-dialog--loading').addClass('mas-isHidden');
      this.$el.append('<div class="mas-dialog--response mas-dialog--error">Action failed : <br> ' + event.response.message + '</div>');
    },

    /**
     * @method close
     * @public
     */
    close: function () {
      this.$el.remove();
    },

    /**
     * @method _updateContent
     * @private
     */
    _updateContent: function () {
      this.$('.mas-dialog--title').html(this.model.get('name'));
      this.$('.mas-dialog--body').html(this.model.get('description'));
      _.forEach(this.model.get('params'), function (param) {
        this.$('.mas-dialog--controls').append(this._createControls(param));
      }, this);
      if (this.extendedView === true) {
        this.$('.mas-dialogControl').addClass('mas-dialogControl--extended');
        this.$('.mas-dialog--identifier').html(this.options.element.get('name'));
      } else {
        this.$('.mas-dialog--identifier').remove();
      }
    },

    /**
     * todo: refactor these strings to use constants TYPE_ENUMERATED & TYPE_STRING
     * @method _createControls
     * @private
     */
    _createControls: function (param) {
      var fragment = document.createDocumentFragment();
      var controls = {
        'ENUMERATED': function () {
          var radioGroup = document.createElement('div');
          radioGroup.classList.add('mas-dialogControl');
          _.forEach(param.range, function (name, index) {
            radioGroup.appendChild(this._createRadioInput(param.id, name, index));
          }, this);
          fragment.appendChild(radioGroup);
          return fragment;
        },
        'STRING': function () {
          return fragment.appendChild(this._createTextInput(param.id, param.name, param.range.max));
        },
        'FILE': function () {
          return fragment.appendChild(this._createFileUploadInput(param.id, param.name, param.range.max));
        }
        //add other types as needed..
      };
      return controls[param.type].call(this);
    },

    /**
     * @method _createRadioInput
     * create html for radiogroup
     * windows 8 app restrictions workaround
     * @private
     */
    _createRadioInput: function (id, name, index) {
      // IMPROVE: Make its own component
      //windows 8 app restrictions workaround
      var label = document.createElement('label');
      label.classList.add('mas-fancyRadio');
      var input = document.createElement('input');
      input.type = 'radio';
      input.required = true;
      input.name = id;
      input.classList.add('mas-fancyRadio--input');
      input.value = index;
      var span = document.createElement('div');
      span.classList.add('mas-fancyRadio--radio');
      span.innerHTML = name;
      label.appendChild(input);
      label.appendChild(span);
      return label;
    },

    /**
     * @method _createTextInput
     * create html for text input field
     * windows 8 app restrictions workaround
     * @private
     */
    _createTextInput: function (name, label, max) {
      return new LabeledInputView({
        name: name,
        label: label,
        maxLength: max
      }).el;
    },

    /**
     * @method _createFileUploadInput
     * create html for file upload input field
     * @private
     */
    _createFileUploadInput: function (name, label, max) {
      return new LabeledFileInputView({
        name: name,
        label: label,
        maxLength: max
      }).el;
    },


    /**
     * @todo: This is broken and I cant see how it ever worked.
     * At some point this.model.params became this.model.get('params')
     * - Do the classes still work?
     * - Is error handling even required?
     * - Ignore for now and move onto FILE processing code but come back to this and investigate
     *
     * @method _validate
     * validate input
     * @private
     */
    _validate: function (inputs) {
      var valid = true;
      _.forEach(this.model.params, function (param) {
        this._clearError(param.id, param.type);
        if (inputs[param.id] !== undefined && inputs[param.id] !== "") {
          valid = valid ? true : false;
        } else {
          valid = false;
          this._showError(param.id, param.type);
        }
      }, this);
      return valid;
    },

    /**
     * @method _showError
     * highlight unvalidated inputs
     * @private
     */
    _showError: function (id, type) {
      // IMPROVE: Update the way errors are displayed! Maybe use parsley.js for this
      switch (type) {
        case 'ENUMERATED':
          this.$('.mas-dialog--error').html("none selected!");
          break;
        case 'STRING':
          $("input[name='" + id + "']").addClass('error');
          break;
        case 'FILE':
          $("input[name='" + id + "']").addClass('error');
          break;
      }
    },

    /**
     * @method _clearError
     * clear highlights
     * @private
     */
    _clearError: function (id, type) {
      switch (type) {
        case 'ENUMERATED':
          this.$('.mas-dialog--error').empty();
          break;
        case 'STRING':
          $("input[name='" + id + "']").removeClass('error');
          break;
        case 'FILE':
          $("input[name='" + id + "']").removeClass('error');
          break;
      }
    }
  });

  return ActionDialogView;
});