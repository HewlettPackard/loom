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
  var BaseView = require('../../../BaseView');
  var template = require('./ProviderLoginForm.html');

  /**
   * @class ProviderLoginForm
   * @module weaver
   * @submodule views.providers
   * @namespace  views.providers
   * @constructor
   * @extends BaseView
   */
  var ProviderLoginForm = BaseView.extend({

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
     * @default mas-providerLoginForm
     */
    className: 'mas-providerLoginForm',

    /**
     * Notify Backbone of events
     * @property events
     * @type {Object}
     * @final
     */
    events: {
      'submit': '_login'
    },

    template: template,

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.$el.attr('method', 'post');
      this.$username = this.$('[name=username]');
      this.$password = this.$('[name=password]');
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
     * @method _login
     * @param event
     * @private
     */
    _login: function (event) {
      event.preventDefault();
      var username = this.$username.val();
      var password = this.$password.val();
      this.$error.text('');
      var promise = this.model.login(username, password);
      promise.then(_.bind(function (patterns) {
          this.trigger('didLogin', patterns);
        }, this))
        .fail(_.bind(this._displayError, this))
        .done();
      promise.always(_.bind(this.$el.removeClass, this.$el, 'is-loggingIn'));
      this.$el.addClass('is-loggingIn');
    },

    /**
     * @method _displayError
     * @private
     */
    _displayError: function () {
      this.$error.text('Login failed. Please verify the credentials you used match the provider you are trying to login to');
    }
  });

  return ProviderLoginForm;

});
