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
  var AggregatorClient = require('weft/services/AggregatorClient');
  var Provider = require('weft/models/Provider');
  var LoginScreen = require('weaver/screens/LoginScreen');

  describe('weaver/screens/LoginScreen.js', function () {

    function simulateClick(element) {

      var event = document.createEvent('MouseEvent');
      event.initEvent('click', true, true);
      element.dispatchEvent(event);
    }

    beforeEach(function () {

      var aggregatorClient = new AggregatorClient();

      this.providers = _.times(3, function (index) {
        return new Provider({
          aggregator: aggregatorClient,
          name: 'Provider ' + index
        });
      });

      this.screen = new LoginScreen({
        model: this.providers
      });

      this.$providers = this.screen.$('[name=provider] option');
      this.submit = this.screen.el.querySelector('button');

      this.screen.$el.appendTo(document.body);
    });

    afterEach(function () {
      this.screen.remove();
    });

    it('Should allow users to choose which provider to connect to', function () {

      var models = this.providers;
      this.$providers.each(function (index, element) {
        expect(element.text).to.equal(models[index].get('name'));
      });
    });

    it('Should send the request to the selected provider', sinon.test(function () {

      var spy = this.spy(this.providers[1], 'login');

      this.$providers.eq(1).attr('selected', 'selected');

      simulateClick(this.submit);

      expect(spy).to.have.been.called;
    }));

    it('Should send the username and password to connect with', sinon.test(function () {

      this.screen.$('[name=username]').val('john.doe');
      this.screen.$('[name=password]').val('5eCr3t!');
      simulateClick(this.submit);

      var body = JSON.parse(this.requests[0].requestBody);
      expect(body).to.deep.equal({
        username: 'john.doe',
        password: '5eCr3t!'
      });
    }));

    it('Should trigger a `didLogin` event upon successful login', function (done) {

      sinon.test(function () {

        simulateClick(this.submit);

        this.screen.once('didLogin', function (patterns) {

          expect(patterns).to.have.length(2);
          done();
        });

        this.requests[0].respond(200, {'Content-Type': 'application/json'}, JSON.stringify({
          patterns: [{}, {}]
        }));
      }).apply(this);
    });

    it('Should display an error message upon login failure', sinon.test(function () {

      simulateClick(this.submit);

      this.requests[0].respond(401);

      expect(this.screen.$('.mas-error')).not.to.be.empty;
    }));

  });
});
