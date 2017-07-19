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
define(['weaver/features/Feature', 'weaver/utils/EventBus'], function(Feature, EventBus) {
  "use strict";

  describe('weaver/features/Feature.js', function() {
    beforeEach(function() {
      this.feature = new Feature();
    });
    describe('Enabling a feature', function() {
      it('Should enable the feature', function() {
        expect(this.feature.isEnabled()).to.be.false;
        this.feature.enable();
        expect(this.feature.isEnabled()).to.be.true;
      });
      it('Should send a message when enabling a feature', function() {
        var spy = sinon.spy();
        EventBus.once(EventBus.createEventName(['feature', 'enabled', this.feature.name]), spy);
        this.feature.enable();
        expect(spy).to.have.been.called;
      });
      it('Should call onEnabled', function() {
        sinon.spy(this.feature, 'onEnable');
        this.feature.enable();
        expect(this.feature.onEnable).to.have.been.called;
        this.feature.onEnable.restore();
      });
    });
    describe('Disabling a feature', function() {
      it('Should disable the feature', function() {
        this.feature.enable();
        expect(this.feature.isEnabled()).to.be.true;
        this.feature.disable();
        expect(this.feature.isEnabled()).to.be.false;
      });
      it('Should send a message when disabling a feature', function() {
        var spy = sinon.spy();
        EventBus.once(EventBus.createEventName(['feature', 'disabled', this.feature.name]), spy);
        this.feature.disable();
        expect(spy).to.have.been.called;
      });
      it('Should call onDisabled', function() {
        sinon.spy(this.feature, 'onDisable');
        this.feature.disable();
        expect(this.feature.onDisable).to.have.been.called;
        this.feature.onDisable.restore();
      });
    });
  });
});
