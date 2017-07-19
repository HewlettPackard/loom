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

  /** @type BaseView */
  var BaseView = require('../../../BaseView');
  var template = require('./ProviderLogoutForm.html');

  /**
   * @class ProviderLogoutForm
   * @module weaver
   * @submodule views.providers
   * @namespace  views.providers
   * @constructor
   * @extends BaseView
   */
  var ProviderLogoutForm = BaseView.extend({

    /**
     * Tell Backbone to create a form
     * @property tagName
     * @type {String}
     * @final
     * @default form
     */
    tagName: 'form',

    /**
     * Tell Backbone to create a form with this class name
     * @property className
     * @type {String}
     * @final
     * @default mas-providerLogoutForm
     */
    className: 'mas-providerLogoutForm',

    /**
     * Notify Backbone of events
     * @property events
     * @type {Object}
     * @final
     */
    events: {

    },

    template: template,

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      //this.$el.attr('method', 'post');
      this.$error = this.$('.mas-error');
      this.render();
    },

    /**
     * @method reset
     */
    reset: function () {
      this.el.reset();
      this.$error.text('');
    },

    /**
     * @method render
     */
    render: function () {
      this.$el.addClass(this.className);
    },

    /**
     * @method _displayError
     * @private
     */
    _displayError: function () {
      this.$error.text('Logout failed.');
    }
  });

  return ProviderLogoutForm;

});
