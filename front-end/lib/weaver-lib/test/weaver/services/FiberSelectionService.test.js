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
    'weaver/services/FiberSelectionService',
    'weaver/utils/EventBus'
  ],
  function (FiberSelectionService, EventBus) {
    "use strict";

    describe('weaver/services/FiberSelectionService.js', function () {
      beforeEach(function() {
        this.service = new FiberSelectionService();
        this.fiber = {};
        this.service.setFilter = sinon.spy();
      });
      describe('fiber:click', function() {
        it.skip('Should reacts to fiber:click events', function () {
          var payload = {fiber: {}};
          sinon.stub(FiberSelectionService.prototype, 'onFiberClick');
          EventBus.trigger('fiber:click', payload);
          expect(this.service.onFiberClick).to.have.been.calledWith(payload);
          FiberSelectionService.prototype.restore();
        });
      });
      describe('onFiberClick', function() {
        it('should unselect the selected fiber when called with a match', function() {
          var spy = sinon.spy();
          var event = {fiberView: this.fiber};
          this.service.selectedFiber = this.fiber;
          EventBus.once('fiber:unselected', spy);
          this.service.onFiberClick(event);
          expect(this.service.selectedFiber).to.be.null;
          var spyCall = spy.getCall(0);
          expect(spyCall.args[0]).to.equal(event);
        });
        it('should unselect the selected fiber when called without a match', function() {
          var spy = sinon.spy();
          var event = {fiberView: 'new'};
          var selectedFiber = {fiber: 'old'};
          this.service.selectedFiber = selectedFiber;
          this.service.selectedFiberEvent.fiberView = selectedFiber;
          EventBus.once('fiber:unselected', spy);
          this.service.onFiberClick(event);
          expect(this.service.selectedFiber).to.be.eql('new');
          var spyCall = spy.getCall(0);
          expect(spyCall.args[0].fiberView).to.equal(selectedFiber);
        });
        it('should select the selected fiber when called without a match', function() {
          var spy = sinon.spy();
          var event = {fiberView: 'new'};
          EventBus.once('fiber:selected', spy);
          this.service.onFiberClick(event);
          expect(this.service.selectedFiber).to.be.eql('new');
          expect(this.service.selectedFiberEvent).to.be.eql(event);
          var spyCall = spy.getCall(0);
          expect(spyCall.args[0]).to.equal(event);
        });
      });
    });
  });
