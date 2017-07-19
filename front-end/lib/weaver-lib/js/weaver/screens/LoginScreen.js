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
  var $ = require('jquery');
  var BaseView = require('../views/BaseView');
  var template = require('./LoginScreen.html');

  /**
   * Login screen allowing the user to authenticate with a provider of his choice
   * @class        LoginScreen
   * @namespace    screens
   * @module       weaver
   * @submodule    screens
   * @constructor
   * @extends     BaseView
   */
  var LoginScreen = BaseView.extend({

    /**
     * @property {Array} model The list of providers to choose from
     */

    /**
     * @type {String}
     */
    className: 'mas mas-loginScreen',

    events: {
      'submit': '_login'
    },

    /**
     * @property {String} template
     */
    template: template,

    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.$provider = this.$('[name=provider]');
      this.$form = this.$('.mas-providerLoginForm');
      this.$username = this.$('[name=username]');
      this.$password = this.$('[name=password]');
      this.$error = this.$('.mas-error');
      this.$form.attr('method', 'post');
      this.render();
    },

    /**
     * Render the current view
     * @method render
     */
    render: function () {
      this.$el.addClass(this.className);
      this._renderProviderOptions();
    },

    /**
     * Builds the login form
     * @method _login
     * @param event
     * @private
     */
    _login: function (event) {
      event.preventDefault();
      var provider = this.$provider.find(':selected').data('provider');
      var username = this.$username.val();
      var password = this.$password.val();
      this.$error.text('');
      var promise = provider.login(username, password);

      promise.then(_.bind(function (patterns) {
          this.trigger('didLogin', patterns);
        }, this))
        .fail(_.bind(this._displayError, this))
        .done();

      promise.always(_.bind(this.$form.removeClass, this.$form, 'is-loggingIn')).done();
      this.$form.addClass('is-loggingIn');
    },

    /**
     * Display the error text. Used when a login fails.
     * @method _displayError
     * @private
     */
    _displayError: function () {
      this.$error.text('Login failed. Please verify the credentials you used match the provider you are trying to login to');
    },

    /**
     * Builds the provider options list
     * @method _renderProviderOptions
     * @private
     */
    _renderProviderOptions: function () {
      var $select = this.$provider;
      $select.append(this.model.map(this._renderProviderOption, this));
    },

    /**
     * Builds a provider option
     * @method _renderProviderOption
     * @param provider
     * @returns {*|JQuery|any|jQuery}
     * @private
     */
    _renderProviderOption: function (provider) {
      return $('<option>' + provider.get('name') + '</option>').data('provider', provider);
    }
  });

  return LoginScreen;

});
