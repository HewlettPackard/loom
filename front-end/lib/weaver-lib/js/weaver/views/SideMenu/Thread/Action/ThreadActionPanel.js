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
define(['lodash', 'jquery', 'q'], function (_, $, Q) {

  "use strict";

  /** @type BaseView */
  var BaseView = require('weaver/views/BaseView');
  var template = require('./ThreadActionPanel.html');
  var LabeledInputView = require('weaver/views/LabeledInputView');
  var LabeledFileInputView = require('weaver/views/LabeledFileInputView');
  var confirm = require('weaver/utils/confirm');
  var FileResponse = require('weaver/utils/FileResponse');
  require('weaver/utils/jquery.serializeObject');

  /**
   * ThreadActionPanel displays the thread action form to allow the user to perform the action
   *
   * @class ThreadActionPanel
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var ThreadActionPanel = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_ThreadActionList
     * @final
     */
    constructorName: 'LOOM_ThreadActionPanel',

    className: "mas-threadActionPanel",

    template: template,

    events: {
      'click .mas-dialog--submit': function(event) {
        this.submit(event);
      },
      'click .mas-dialog--back': function(event) {
        event.preventDefault();
        this.EventBus.trigger('thread:action:cancel');
      }
    },

    /**
     * Options contains:
     * options.id - the id of the action, as provided by loom such as 'power'
     * options.action - the action object from loom including name, descriptions, arguments etc
     * options.thread - the thread which the action is performing on
     * @method initialize
     */
    initialize: function (options) {
      BaseView.prototype.initialize.apply(this, arguments);
      this.model = options.thread;
      this.render();
    },

    /**
     * Renders the various components into the tabbed side menu view
     * @returns {ThreadActionPanel}
     * @chainable
     */
    render: function() {
      this.$('.mas-threadActionPanel--title').html(this.options.action.get('name'));
      this.$('.mas-threadActionPanel--description').html(this.options.action.get('description'));
      _.forEach(this.options.action.get('params'), function (param) {
        this.$('.mas-threadActionPanel--controls').append(this._createControls(param));
      }, this);
      return this;
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
          var inputs = $(event.target).parents('.mas-form').serializeObject();
          if (this._validate(inputs)) {
            var files = $(event.target).parents('.mas-form').find('input[type=file]');
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
      this.EventBus.trigger('thread:action:server:send', {action: this.options.action, values: inputs});
      this.options.action.execute(this.options.thread, inputs).then(
        _.bind(this.onSuccess, this)
      ).fail(
        _.bind(this.onFail, this)
      );
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
      this.EventBus.trigger('thread:action:server:send:success', {action: this.model, response: response});
    },

    /**
     * @method onFail
     * @param response
     */
    onFail: function (response) {
      this.EventBus.trigger('thread:action:server:send:fail', {action: this.model, response: response});
    }

  });

  return ThreadActionPanel;
});
