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
  var BaseView = require('../../../BaseView');
  var ProviderListItem = require('./ProviderListItem');
  var template = $(require('./ProviderList.html')).eq(0);

  /**
   * ProviderListView renders the list of providers it gets passed as models
   * and allows users to sign in and sign out
   * @class  ProviderListView
   * @module weaver
   * @submodule views.providers
   * @namespace  views.providers
   * @constructor
   * @extends BaseView
   */
  var ProviderListView = BaseView.extend({

    HIGHLIGHT_DURATION: 4000,

    /**
     * @property template
     * @type {String}
     * @final
     */
    template: template,

    events: {
      'click .mas-provider.is-loggedIn:not(.is-locked) .mas-action-highlightProvider': function (event) {
        if (!event.isDefaultPrevented()) {
          event.preventDefault();
          var provider = $(event.target).parents('.mas-action-highlightProvider').data('provider');
          var providerHighlightService = this.serviceManager.get('ProviderHighlightService');
          if (provider === providerHighlightService.highlightedProvider) {
            providerHighlightService.highlight();
            this.EventBus.trigger('provider:highlight', {provider: provider, duration: 0});
          } else {
            providerHighlightService.highlight(provider, this.HIGHLIGHT_DURATION);
            this.EventBus.trigger('provider:highlight', {provider: provider, duration: this.HIGHLIGHT_DURATION});
          }
        }
      }
    },

    /**
     * @method initialize
     */
    initialize: function (options) {
      BaseView.prototype.initialize.apply(this, arguments);
      this.serviceManager = options.serviceManager;
      this.render();
      this.listenTo(this.model, 'add remove change:loggedIn change:locked', this.render);
    },

    /**
     * @method updateLockedWarningDisplay
     */
    updateLockedWarningDisplay: function () {
      var hasLockedProviders = this.model.findWhere({'locked': true});
      if (hasLockedProviders) {
        this.$el.addClass('has-lockedProviders');
      } else {
        this.$el.removeClass('has-lockedProviders');
      }
    },

    /**
     * @method render
     */
    render: function () {
      this.$('.mas-providerList--list').empty().append(this.renderListItems());
      this.updateLockedWarningDisplay();
    },

    /**
     * @method renderListItems
     * @returns {Array.<T>}
     */
    renderListItems: function () {
      var loggedInIndex = 0;
      var split = this.model.reduce(function (result, provider) {
        if (provider.get('loggedIn')) {
          result.loggedIn.push(provider);
        } else {
          result.loggedOut.push(provider);
        }
        return result;
      }, {
        loggedIn: [],
        loggedOut: []
      });
      var rendered = _.mapValues(split, function (providerList) {
        return _.map(providerList, function (provider) {
          var properties = {
            model: provider
          };
          if (provider.get('loggedIn')) {
            loggedInIndex ++;
            properties.legend = loggedInIndex;
          }
          var item = new ProviderListItem(properties);
          return item.$el;
        });
      });
      return rendered.loggedIn.concat(rendered.loggedOut);
    }

  });

  return ProviderListView;
});