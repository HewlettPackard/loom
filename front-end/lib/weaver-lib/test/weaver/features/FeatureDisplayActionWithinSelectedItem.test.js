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
  'weaver/features/FeatureDisplayActionWithinSelectedItem',
  'weaver/utils/EventBus'
], function (FeatureDisplayActionWithinSelectedItem, EventBus) {
  "use strict";

  describe('weaver/features/FeatureDisplayActionWithinSelectedItem.js', function () {
    beforeEach(function () {
      this.feature = FeatureDisplayActionWithinSelectedItem;
      this.inputEvent = 'fiber:action:display';
      this.payload = {fixture: 'payload'};
    });
    describe('When enabled', function () {
      it('Should react to the input event', function () {
        var spy = sinon.spy();
        EventBus.once(this.feature.name, spy);
        this.feature.enable();
        EventBus.trigger(this.inputEvent, this.payload);
        expect(spy).to.have.been.calledWith(this.payload);
      });
    });
    describe('When disabled', function () {
      it('Should NOT react to the input event', function () {
        var spy = sinon.spy();
        EventBus.once(this.feature.name, spy);
        this.feature.disable();
        EventBus.trigger(this.inputEvent, this.payload);
        expect(spy).not.to.have.been.called;
      });
    });
  });
});
