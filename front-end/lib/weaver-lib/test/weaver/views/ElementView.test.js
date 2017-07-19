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
  var Aggregation = require('weft/models/Aggregation');
  var Thread = require('weft/models/Thread');
  var ElementView = require('weaver/views/Element/ElementView');
  //var EventBus = require('weaver/utils/EventBus');

  describe('weaver/views/Element/ElementView.js', function () {
    beforeEach(function () {
      this.parent = new Thread({
        name: 'parentThread',
        itemProperties: {
          'a-property': 'A property',
          'another-property': 'Another property'
        },
        stateChangeTimeouts: {
          updated: 4000,
          nestedAdd: 6000,
          nestedUpdate: 5000,
          nestedDelete: 7000
        }
      });
      this.element = new Aggregation({
        name: 'testElement',
        parent: this.parent
      });
      this.view = new ElementView({
        model: this.element
      });
      document.body.appendChild(this.view.el);
    });
    afterEach(function () {
      document.body.removeChild(this.view.el);
    });
    describe('.is-related', function () {
      var RELATED_CLASS = 'is-related';
      it('Should have a `.is-related` class if the element is flagged as related', function () {
        this.element.set('related', true);
        expect(this.view.$el).to.have.class(RELATED_CLASS);
      });
      it('Should have a `.is-related` class when the element gets flagged as related', function () {
        this.element.set('related', true);
        expect(this.view.$el).to.have.class(RELATED_CLASS);
      });
      it('Should not have a `.is-related` class when the element stops being related', function () {
        this.element.set('related', true);
        expect(this.view.$el).to.have.class(RELATED_CLASS);
        this.element.unset('related');
        expect(this.view.$el).not.to.have.class(RELATED_CLASS);
      });
    });
    describe('.is-matchingFilter', function () {
      it('Should have a `.is-matchingFilter` class if the element is matching the current filter', function () {
        this.element.set('isMatchingFilter', true);
        expect(this.view.$el).to.have.class('is-matchingFilter');
      });
      it('Should have not a `.is-matchingFilter` class if the element is not matching the current filter', function () {
        this.element.set('isMatchingFilter', false);
        expect(this.view.$el).not.to.have.class('is-matchingFilter');
      });
    });
    // Actions have been moved to the tooltip
    describe.skip('.is-partOfFilter', function () {
      it('Should have a `.is-partOfFilter` when filter action button is clicked and filter is not selected', function () {
        this.view._updateElementDetails(true);
        this.view.$el.find('.mas-action--filter').click();
        expect(this.view.$el).to.have.class('is-partOfFilter');
      });
      it('Should have not a `.is-partOfFilter` when filter action button is clicked and filter is previously selected', function () {
        this.element.set('isPartOfFilter', true);
        this.view._updateElementDetails(true);
        this.view.$el.find('.mas-action--filter').click();
        expect(this.view.$el).not.to.have.class('is-partOfFilter  ');
      });
    });
    // filters currently removed frm element view.. tests maybe deprecatd soon
    describe.skip('updates to filters', function () {
      it('Should fire event when filter is activated', function (done) {
        this.view._updateElementDetails(true);
        this.view.$el.on('action:addFilterElement', _.bind(function (event) {
          expect(event.originalEvent.thread).to.equal(this.element);
          done();
        }, this));
        this.view.$el.find('.mas-action--filter').click();
      });
      it('Should fire event when filter is deactivated', function (done) {
        this.element.set('isPartOfFilter', true);
        this.view._updateElementDetails(true);
        this.view.$el.on('action:removeFilterElement', _.bind(function (event) {
          expect(event.originalEvent.thread).to.equal(this.element);
          done();
        }, this));
        this.view.$el.find('.mas-action--filter').click();
      });
    });
  });
});
