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
  var FurtherRelationshipFilterService = require('weaver/services/FurtherRelationshipFilterService');
  var RelationshipSelectedFilterService = require('weaver/services/RelationshipSelectedFilterService');
  var RelationshipHighlightFilterService = require('weaver/services/RelationshipHighlightFilterService');
  var EventBus = require('weaver/utils/EventBus');
  var AggregatorClient = require('weft/services/AggregatorClient');

  describe('weaver/services/FurtherRelationshipFilterService.js', function () {
    beforeEach(function() {
      this.service = new FurtherRelationshipFilterService({
        'AggregatorClient': new AggregatorClient(),
        'RelationshipSelectedFilterService': new RelationshipSelectedFilterService(),
        'RelationshipHighlightFilterService': new RelationshipHighlightFilterService()
      });
    });
    describe('defaults', function () {
      it('Should contain overridden default properties', function () {
        expect(this.service.attributes).to.have.property('filterFlag');
        expect(this.service.attributes).to.have.property('filteredElementsFlag');
        expect(this.service.attributes).to.have.property('active');
        expect(this.service.attributes.filterFlag).to.equal('selected');
        expect(this.service.attributes.filteredElementsFlag).to.equal('furtherRelated');
        expect(this.service.attributes.active).to.equal(true);
      });
      it('Should contain FilterService default properties', function () {
        for (var item in FilterService.prototype.defaults()) {
          expect(this.service.attributes).to.have.property(item);
        }
      });
    });
    describe('EventBus', function () {
      beforeEach(function() {
        this.model = new Backbone.Model({selected: true});
        this.view = new Backbone.View({model: this.model});
        this.service.setFilter = sinon.spy();
      });
      describe.skip('willRemoveElement', function() {
        it('Should reset filters if the item is selected', function () {
          EventBus.trigger('willRemoveElement', {view: this.view});
          expect(this.service.setFilter).to.have.been.calledWith([]);
        });
        it('Should NOT reset filters if the item is not selected', function () {
          this.model.set('selected', false);
          EventBus.trigger('willRemoveElement', {view: this.view});
          expect(this.service.setFilter.called).to.be.false;
        });
      });
      describe.skip('fiber:unselected', function() {
        beforeEach(function() {
          this.model = new Backbone.Model({selected: true});
          this.view = new Backbone.View({model: this.model});
          this.service.setFilter = sinon.spy();
        });
        it('Should reset filters if the item is not selected', function () {
          this.model.set('selected', false);
          EventBus.trigger('fiber:unselected', {fiberView: this.view});
          expect(this.service.setFilter.called).to.be.true;
        });
      });
      describe.skip('fiber:selected', function() {
        it('Should filter the model passed', function () {
          var setFilter = sinon.stub(FilterService.prototype, 'setFilter');
          var calcFurtherRelations = sinon.stub(FurtherRelationshipFilterService.prototype, 'calcFurtherRelations');
          calcFurtherRelations.returns([]);
          EventBus.trigger('fiber:selected', {fiberView: this.view});
          expect(this.service.setFilter.called).to.be.true;
          setFilter.restore();
          calcFurtherRelations.restore();
        });
      });
    });
  });
});
