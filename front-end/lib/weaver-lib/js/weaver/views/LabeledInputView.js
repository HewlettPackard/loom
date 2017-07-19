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
define([], function () {

  "use strict";

  /** @type BaseView */
  var BaseView = require('./BaseView');

  /**
   * A simple labeled input
   * @class LabeledInputView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var LabeledInputView = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_LabeledInputView
     * @final
     */
    constructorName: 'LOOM_LabeledInputView',

    /**
     * @property {String} tagName
     * @final
     * @default label
     */
    tagName: 'label',

    /**
     * @property {String} className
     * @final
     * @default mas-labeledInput-top
     */
    className: 'mas-labeledInput-top',

    /**
     * @property {Object} events
     */
    events: {
      'change .mas-input': 'updateModelProperty'
    },

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.render();
    },

    /**
     * @method updateModelProperty
     */
    updateModelProperty: function () {
      if (this.model && this.options.property) {
        this.model[this.options.property] = this.input.value;
      }
    },

    /**
     * @method render
     */
    render: function () {
      // IMPROVE: Unnecessary span!
      var span = document.createElement('span');
      span.classList.add('mas-formLabel');
      span.innerHTML = this.options.label || '';
      var input = this.input = document.createElement('input');
      input.type = 'text';
      input.name = this.options.name || '';
      input.classList.add('mas-input');
      if (this.options.maxLength) {
        input.maxlength = this.options.maxLength;
      }
      if (this.model && this.options.property) {
        input.value = this.model[this.options.property] || '';
      }
      this.el.appendChild(span);
      this.el.appendChild(input);
    }
  });

  return LabeledInputView;
});
