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
define([
  'jquery',
  'lodash',
  'backbone',
  'weft/models/Element',
  'weft/services/FilterService',
  'weaver/views/FilterView'
], function ($, _, Backbone, Element, FilterService, FilterView) {

  "use strict";

  describe('weaver/views/FilterView.js', function () {

    // To test the FilterView, we need
    beforeEach(function () {

      this.element = new Element({
        'l.logicalId': 'an-element',
        name: 'Name of the element'
      });

      this.element.itemType = {
        getVisibleAttributes: function () {
          return [];
        }
      };

      // A FilterView with FilterService
      this.view = new FilterView({
        service: new FilterService()
      });

    });

    it('Should add filter tag when element is added to filter', function (done) {

      this.view.service.get('filters').on('add', function () {

        expect(this.view.$el.find('.mas-filterElement')).to.contain('Name of the element');

        done();
      }, this);

      this.view.service.get('filters').add(this.element);

    });

    it('Should remove filter tag when element is removed from filter', function (done) {

      this.view.service.get('filters').add(this.element);

      this.view.service.get('filters').on('remove', function () {
        expect(this.view.$el.find('.mas-filterElement')).not.to.contain('Name of the element');

        done();
      }, this);

      this.view.service.get('filters').remove(this.element);

    });

    it('Should remove filter tag when clicked, and update filters', function () {

      this.view.service.get('filters').add(this.element);

      this.view.$el.find('li:contains(Name of the element)').click();
      expect(this.view.service.get('filters').size()).to.equal(0);

    });

  });

});
