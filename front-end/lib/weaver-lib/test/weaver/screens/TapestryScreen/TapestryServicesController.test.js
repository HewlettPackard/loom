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
define(function() {
  "use strict";

  var ServiceManager = require('weaver/utils/ServiceManager');
  var controller = require('weaver/screens/TapestryScreen/TapestryServicesController');

  describe('weaver/screens/TapestryScreen/TapestryServicesController.js', function() {
    before(function() {
      this.services = [
        'FurtherRelationshipFilterService',
        'PrimaryFilterService',
        'RelationshipSelectedFilterService',
        'RelationshipHighlightFilterService',
        'ProvidersLegendService',
        'ProviderHighlightService',
        'StatusLoader',
        'PatternInTapestryMarker',
        'ThreadMonitorService',
        'LockingManager',
        'BraidingController',
        'RelationTypeUpdater',
        'ThreadAvailabilityMonitor'
      ];
    });
    describe('start', function() {
      describe('Should bring up the required services', function() {
        before(function(){
          this.services.forEach(function(service) {
            this[service] = sinon.stub(controller,'_create'+service);
          }, this);
          this.registerSpy = sinon.spy(ServiceManager, 'register');
        });
        after(function() {
          this.services.forEach(function(service) {
            delete this[service];
            controller['_create'+service].restore();
            ServiceManager.deregister(service);
          }, this);
          ServiceManager.register.restore();
        });
        it('Should create the services', function() {
          controller.start(ServiceManager, {});
          this.services.forEach(function(service) {
            expect(this[service].called).to.be.true;
          }, this);
        });
        it('Should register the created services', function() {
          this.services.forEach(function(service) {
            expect(this.registerSpy.calledWith(service));
          }, this);
        });
      });
    });
    describe('stop', function() {
      beforeEach(function() {
        this.fakeService = {stopListening: function(){}, get: function(){return {set: function(){}};}};
        this.services.forEach(function(service) {
          ServiceManager.register(service, this.fakeService);
        }, this);
      });
      afterEach(function() {
        this.services.forEach(function(service) {
          ServiceManager.deregister(service);
        }, this);
      });
      it('Should stop the services', function() {
        var spy = sinon.spy(ServiceManager, 'stopListening');
        controller.stop(ServiceManager, {});
        this.services.forEach(function(service) {
          expect(spy.calledWith(service));
        }, this);
        ServiceManager.stopListening.restore();
      });
    });
    describe('deregister', function() {
      beforeEach(function() {
        this.fakeService = {deregister: function(){}};
        this.services.forEach(function(service) {
          ServiceManager.register(service, this.fakeService);
        }, this);
      });
      it('Should deregister the services', function() {
        var spy = sinon.spy(ServiceManager, 'deregister');
        controller.deregister(ServiceManager, {});
        this.services.forEach(function(service) {
          expect(spy.calledWith(service));
        }, this);
        ServiceManager.deregister.restore();
      });
    });
  });
});
