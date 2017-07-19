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

  var Backbone = require('backbone');
  var FilterService = require('weft/services/FilterService');
  var RelationshipHighlightFilterService = require('weaver/services/RelationshipHighlightFilterService');
  var EventBus = require('weaver/utils/EventBus');

  describe('weaver/services/RelationshipHighlightFilterService.js', function () {
    describe('defaults', function () {
      it('Should contain overridden default properties', function () {
        var service = new RelationshipHighlightFilterService();
        expect(service.attributes).to.have.property('filterFlag');
        expect(service.attributes).to.have.property('filteredElementsFlag');
        expect(service.attributes).to.have.property('active');
        expect(service.attributes.filterFlag).to.equal(null);
        expect(service.attributes.filteredElementsFlag).to.equal('highlighted');
        expect(service.attributes.active).to.equal(false);
      });
      it('Should contain FilterService default properties', function () {
        var service = new RelationshipHighlightFilterService();
        for (var item in FilterService.prototype.defaults()) {
          expect(service.attributes).to.have.property(item);
        }
      });
    });
    describe('EventBus', function () {
      beforeEach(function() {
        this.service = new RelationshipHighlightFilterService();
        this.model = new Backbone.Model({selected: true});
        this.view = new Backbone.View({model: this.model});
        this.service.setFilter = sinon.spy();
      });
      describe('element:will:remove', function() {
        it('Should reset filters if the item is highlighted', function () {
          EventBus.trigger('element:will:remove', {view: this.view});
          expect(this.service.setFilter).to.have.been.calledWith([]);
        });
        it('Should NOT reset filters if the item is highlighted', function () {
          this.model.set('selected', false);
          EventBus.trigger('element:will:remove', {view: this.view});
          expect(this.service.setFilter.called).to.be.false;
        });
      });
      describe('fiber:unselected', function() {
        beforeEach(function() {
          this.service = new RelationshipHighlightFilterService();
          this.model = new Backbone.Model({selected: true});
          this.view = new Backbone.View({model: this.model});
          this.service.setFilter = sinon.spy();
        });
        it('Should reset filters if the item is highlighted', function () {
          EventBus.trigger('fiber:unselected', {fiberView: this.view});
          expect(this.service.setFilter).to.have.been.calledWith([]);
        });
        it('Should reset filters if the item is not highlighted', function () {
          this.model.set('selected', false);
          EventBus.trigger('fiber:unselected', {view: this.view});
          expect(this.service.setFilter.called).to.be.true;
        });
      });
      describe.skip('fiber:selected', function() {
        it('Should filter the model passed', function () {
          var setFilter = sinon.stub(FilterService.prototype, 'setFilter');
          EventBus.trigger('fiber:selected', {fiberView: this.view});
          expect(this.service.setFilter).to.have.been.calledWith([this.model]);
          setFilter.restore();
        });
      });
    });
    describe('relationType', function() {
      it('Should send a RelationTypeChangeEvent on relationType change', function() {
        var spy = sinon.spy();
        var relationType = 'SOME NEW VALUE';
        var service = new RelationshipHighlightFilterService();
        EventBus.once(RelationshipHighlightFilterService.RelationTypeChangeEvent, spy);
        service.set('relationType', relationType);
        var spyCall = spy.getCall(0);
        expect(spyCall.args[0]).to.equal(relationType);
      });
    });
  });
});
