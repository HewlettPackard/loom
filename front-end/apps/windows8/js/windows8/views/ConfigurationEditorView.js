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

  var Backbone = require('backbone');
  var LabeledInputView = require('weaver/views/LabeledInputView');

  /**
   * Simple editor for startup configuration, allowing the user to select
   * which aggregator to target and which pattern to load
   * @class ConfigurationEditorView
   * @namespace views
   * @module ui
   * @constructor
   */
  var ConfigurationEditor = Backbone.View.extend({

    constructorName: 'LOOM_ConfigurationEditor',

    tagName: 'form',
    className: 'mas-configurationEditor',

    // CONSTRUCTOR
    initialize: function () {
      this.errorElement = document.createElement('p');
      this.errorElement.classList.add('mas-error');
      this.render();
    },

    // PUBLIC API
    showError: function (errorMessage) {

      this.errorElement.textContent = errorMessage;
      this.$el.addClass('has-error');
    },

    clearError: function () {
      this.$el.removeClass('has-error');
    },

    // RENDERING
    render: function () {

      this.aggregatorURLInput = new LabeledInputView({
        name: 'loom-url',
        label: 'Loom URL',
        model: this.model,
        property: 'loom-url'
      });

      this.el.appendChild(this.aggregatorURLInput.el);
      this.el.appendChild(this.errorElement);

      var submit = document.createElement('button');
      submit.textContent = 'Start';
      submit.classList.add('mas-button');
      submit.classList.add('mas-configurationEditor--submit'); // Thanks IE for the two classList calls!
      this.el.appendChild(submit);
    }
  });

  return ConfigurationEditor;
});
