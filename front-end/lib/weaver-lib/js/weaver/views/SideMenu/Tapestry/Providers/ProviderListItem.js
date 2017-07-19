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
  require('backbone.stickit');
  var confirm = require('weaver/utils/confirm');
  var BaseView = require('../../../BaseView');
  var ProviderLoginForm = require('./ProviderLoginForm');
  var ProviderLogoutForm = require('./ProviderLogoutForm');
  var template = $(require('./ProviderListItem.html')).get(0);

  /**
   * @class ProviderSelectionMenuItem
   * @module weaver
   * @submodule views.providers
   * @namespace  views.providers
   * @constructor
   * @extends BaseView
   */
  var ProviderSelectionMenuItem = BaseView.extend({

    /**
     * @property template
     * @type {String}
     * @final
     */
    template: template,

    isShowingForm: false,

    loginInOutForm: null,

    events: {
      // 'click .mas-action-signInOrOut': 'signInOrOut',
      'click .mas-providerLoginForm--cancel': 'hideForm',
      'click .mas-providerLogoutForm--logout': 'signOut',
      'click .mas-action-toggleProvider, .mas-provider--name': function() {
        this.isShowingForm = !this.isShowingForm;
        if (this.isShowingForm === true) {
          this.showForm();
        } else {
          this.hideForm();
        }
      }
    },

    bindings: {
      ':el': {
        classes: {
          'is-loggedIn': 'loggedIn',
          'is-locked': 'locked',
          'is-highlighted': 'highlighted'
        }
      }
    },

    showForm: function() {
      this.performShowFormLogic();
      this.$('.mas-action-toggleProvider').addClass('is-showingProviderForm');
      this.$el.find('.mas-itemForm').removeClass('hideFlex');
      this.EventBus.trigger('provider:show:form', this);
    },

    hideForm: function() {
      this.$el.find('.mas-itemForm').addClass('hideFlex');
      this.$('.mas-action-toggleProvider').removeClass('is-showingProviderForm');
      this.cleanLoginForm();
    },

    cleanLoginForm: function() {
      if (this.loginInOutForm !== null) {
        this.loginInOutForm.stopListening();
        this.loginInOutForm.remove();
        this.loginInOutForm = null;
      }
    },

    performShowFormLogic: function() {
      this.cleanLoginForm();

      if (this.model.get('locked')) {
        return;
      }

      if (this.model.get('loggedIn')) {
        this.loginInOutForm = new ProviderLogoutForm({
          model: this.model
        });
        this.$el.find('.mas-itemForm').append(this.loginInOutForm.el);
        this.$('.mas-action-highlightProvider').data('provider', this.model);
      } else {
        this.loginInOutForm = new ProviderLoginForm({
          model: this.model
        });
        this.$el.find('.mas-itemForm').append(this.loginInOutForm.el);
      }
    },

    /**
     * @method signInWhenLoggedOut
     */
    signInWhenLoggedOut: function () {
      if (!this.model.get('loggedIn')) {
        this.signInOrOut();
      }
    },


    /**
     * @method signIn
     */
    signIn: function () {
      if (!this.loginForm) {
        this.loginForm = new ProviderLoginForm({
          model: this.model
        });
        this.loginForm.$el.addClass('mas-provider--form');
        this.listenToOnce(this.loginForm, 'didLogin', this.removeLoginForm);
        this.$el.append(this.loginForm.el);
      } else {
        this.removeLoginForm();
      }
      return false;
    },

    /**
     * @method signOut
     */
    signOut: function (event) {
      event.preventDefault();
      var provider = this.model;
      var message = "You are about to logout from the '" + provider.get('name') + "' provider. You'll no longer have access to the data associated with this provider. Are you sure you want to proceed?";
      confirm.confirm(message)
        .then(function (confirmed) {
          if (confirmed) {
            provider.logout();
          }
        })
        .done();

      return false;
    },

    /**
     * @method removeLoginForm
     */
    removeLoginForm: function () {
      if (this.loginForm) {
        this.loginForm.remove();
        this.loginForm = undefined;
      }
    },

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.render();
      this.listenTo(this.EventBus, 'provider:highlight', function(event) {
        if (event.provider === this.model) {
          this.$el.find('.mas-action-highlightProvider').addClass('has-highlight');
          this.timeout = setTimeout(this.clearHighlight.bind(this), event.duration);
        }
      });
    },

    clearHighlight: function() {
      this.$el.find('.mas-action-highlightProvider').removeClass('has-highlight');
    },

    /**
     * @method render
     */
    render: function () {
      // legend removed from design. discuss.
      // if (this.options.legend) {
      //   this.$('.mas-provider--legend').text(this.options.legend);
      // }
      this.$('.mas-action-toggleProvider').data('provider', this.model);
      this.$('.mas-provider--name').text(this.model.get('name'));
      this.stickit();
    }
  });

  return ProviderSelectionMenuItem;
});