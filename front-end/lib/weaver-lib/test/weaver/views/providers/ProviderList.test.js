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
  var Backbone = require('backbone');
  var AggregatorClient = require('weft/services/AggregatorClient');
  var Provider = require('weft/models/Provider');
  var ProviderList = require('weaver/views/SideMenu/Tapestry/Providers/ProviderList');

  describe('weaver/views/providers/ProviderList.js', function () {

    beforeEach(function () {
      // Required for providers to log in/log out
      var aggregator = this.aggregator = new AggregatorClient();
      var providersList = _.times(3, function (index) {
        return new Provider({
          name: 'Provider #' + index,
          aggregator: aggregator
        });
      });
      providersList[1].set('loggedIn', true);
      this.providers = new Backbone.Collection(providersList);
      this.menu = new ProviderList({
        model: this.providers
      });
      this.menu.$el.appendTo(document.body);
      sinon.stub(window, 'confirm').returns(true);
    });

    afterEach(function () {
      this.menu.remove();
      window.confirm.restore();
    });

    it('Should highlight which providers the user is logged in', function () {
      expect(this.menu.$('li:contains(Provider #0)')).not.to.have.class('is-loggedIn');
      expect(this.menu.$('li:contains(Provider #1)')).to.have.class('is-loggedIn');
      expect(this.menu.$('li:contains(Provider #1)')).to.match(':first-child');
      expect(this.menu.$('li:contains(Provider #2)')).not.to.have.class('is-loggedIn');
    });

    it('Should update which providers are displayed when providers are added or removed', function () {
      var newProvider = new Provider({
        id: 'provider-3',
        name: 'Provider #3',
        aggregator: this.aggregator
      });
      this.providers.set([
        this.providers.at(1),
        this.providers.at(2),
        newProvider
      ]);
      expect(this.menu.$('li:contains(Provider #1)')).to.have.class('is-loggedIn');
      expect(this.menu.$('li:contains(Provider #2)')).not.to.have.class('is-loggedIn');
      expect(this.menu.$('li:contains(Provider #3)')).not.to.have.class('is-loggedIn');
    });

    it.skip('Should allow the user to login when clicking on a logged out provider', sinon.test(function () {
      this.menu.$('li:contains(Provider #2) .mas-action-signInOrOut').click();
      this.menu.$('[name=username]').val('john.doe');
      this.menu.$('[name=password]').val('password');
      this.menu.$('.mas-providerLoginForm--submit').click();
      expect(JSON.parse(this.requests[0].requestBody)).to.deep.equal({
        username: 'john.doe',
        password: 'password'
      });
      this.requests[0].respond(200);
      expect(this.menu.$el).not.to.have.descendants('.mas-providerLoginForm');
    }));

    it('Should remove form when user clicks `Cancel`', function () {
      this.menu.$('li:contains(Provider #2) .mas-action-signInOrOut').click();
      this.menu.$('.mas-providerLoginForm--cancel').click();
      expect(this.menu.$el).not.to.have.descendants('.mas-providerLoginForm');
    });

    it.skip('Should display an error when user provides bad credentials', sinon.test(function () {
      this.menu.$('li:contains(Provider #2) .mas-action-signInOrOut').click();
      this.menu.$('[name=username]').val('john.doe');
      this.menu.$('[name=password]').val('password');
      this.menu.$('.mas-providerLoginForm--submit').click();
      this.requests[0].respond(401);
      expect(this.menu.$('.mas-error').text()).not.to.be.empty;
    }));
  });
});
