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
    'weaver/services/FeatureSwitcherService',
    'weaver/utils/EventBus',
    'weaver/features/Feature'
  ],
  function (FeatureSwitcherService, EventBus, Feature) {
    "use strict";

    describe('weaver/services/FeatureSwitcherService.js', function () {
      describe('Add and Get', function () {
        var feature = new Feature();
        FeatureSwitcherService.add(feature);
        expect(FeatureSwitcherService.get(feature.name)).to.eql(feature);
      });
      describe('Ping', function () {
        it('Should do nothing if the feature is unknown', function () {
          var spy = sinon.spy();
          var feature = {
            name: 'my-brand-new-disabled-feature', isEnabled: function () {
              return true;
            }
          };
          EventBus.once(EventBus.createEventName(['feature', 'ping', feature.name]), spy);
          FeatureSwitcherService.ping(feature);
          expect(spy.called).to.be.false;
        });
        it('Should send the current state when known', function () {
          var spy = sinon.spy();
          var feature = {
            name: 'my-brand-new-enabled-feature', isEnabled: function () {
              return true;
            }
          };
          FeatureSwitcherService.add(feature);
          EventBus.once(EventBus.createEventName(['feature', 'ping', feature.name]), spy);
          FeatureSwitcherService.ping(feature);
          expect(spy).to.have.been.calledWith({enabled: true});
        });
      });
    });
  });
