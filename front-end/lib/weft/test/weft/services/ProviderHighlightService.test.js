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
  var Provider = require('weft/models/Provider');
  var Element = require('weft/models/Element');
  var ProviderHighlightService = require('weft/services/ProviderHighlightService');

  // REFACTOR: A mock itemtype shouldn't be necessary for this
  var mockItemType = {
    getVisibleAttributes: function () {
      return [];
    }
  };

  describe('weft/models/ProviderHighlightService.js', function () {

    before(function () {

      this.providers = _.times(5, function (index) {

        return new Provider({
          id: 'provider-' + index
        });
      });

      this.elementFromProvider1 = new Element();
      this.elementFromProvider1.itemType = mockItemType;
      this.elementFromProvider1.set('l.providerId', [this.providers[1].id]);

      this.elementFromProvider2 = new Element();
      this.elementFromProvider2.itemType = mockItemType;
      this.elementFromProvider2.set('l.providerId', [this.providers[2].id]);

      this.fibersList = new Backbone.Collection([this.elementFromProvider1, this.elementFromProvider2]);

      this.service = new ProviderHighlightService(this.fibersList);
    });

    describe('highlight()/clearHighlight()', function () {

      it('Highlights elements related to given provider', function () {

        this.service.highlight(this.providers[1]);
        expect(this.providers[1].get('highlighted')).to.be.true;
        expect(this.elementFromProvider1.get('isFromHighlightedProvider')).to.be.true;
      });

      it('Clears highlighting on previously highlighted elements when highlighting a new element', function () {

        this.service.highlight(this.providers[2]);
        expect(this.providers[1].get('highlighted')).to.be.false;
        expect(this.providers[2].get('highlighted')).to.be.true;
        expect(this.elementFromProvider1.get('isFromHighlightedProvider')).to.be.false;
        expect(this.elementFromProvider2.get('isFromHighlightedProvider')).to.be.true;
      });

      it('Clears highlighting when called with no arguments', function () {

        this.service.highlight();
        expect(this.providers[2].get('highlighted')).to.be.false;
        expect(this.elementFromProvider2.get('isFromHighlightedProvider')).to.be.false;
      });

      it('Highlights the provider for given amout of time', sinon.test(function () {

        this.service.highlight(this.providers[1], 10000);
        expect(this.providers[1].get('highlighted')).to.be.true;
        expect(this.elementFromProvider1.get('isFromHighlightedProvider')).to.be.true;

        this.clock.tick(10000);
        expect(this.providers[1].get('highlighted')).to.be.false;
        expect(this.elementFromProvider1.get('isFromHighlightedProvider')).to.be.false;
      }));
    });

    describe('Fibers updates', function () {

      it('Should mark new elements if they are from highlighted provider', function () {

        this.service.highlight(this.providers[1]);

        var newElement = new Element();
        newElement.itemType = mockItemType;
        newElement.set('l.providerId', [this.providers[1].id]);

        this.fibersList.add(newElement);

        expect(newElement.get('isFromHighlightedProvider')).to.be.true;
      });

      it('Should mark elements that become from highlighted provider', function () {

        this.service.highlight(this.providers[1]);

        this.elementFromProvider2.set('l.providerId', [
          this.providers[1].id,
          this.providers[2].id
        ]);

        expect(this.elementFromProvider2.get('isFromHighlightedProvider')).to.be.true;
      });

      it('Should unmark elements that are no longer from highlighted provider', function () {

        this.service.highlight(this.providers[1]);

        this.elementFromProvider1.set('l.providerId', [
          this.providers[2].id
        ]);

        expect(this.elementFromProvider1.get('isFromHighlightedProvider')).to.be.false;
      });
    });
  });
});
