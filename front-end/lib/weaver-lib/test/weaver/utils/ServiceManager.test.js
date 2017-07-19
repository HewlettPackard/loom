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
  
  describe('weaver/utils/ServiceManager.js', function() {
    it('Should register a service', function() {
      expect(ServiceManager.register('new_service', {})).to.be.true;
    });
    it('Should throw when registering a service twice', function() {
      expect(ServiceManager.register('twice', {})).to.be.true;
      expect(function() {ServiceManager.register('twice', {});}
        .bind(this))
        .to.throw(Error, new RegExp(ServiceManager.ERROR_SERVICE_EXISTS));
    });
    it('Should get a registered service', function() {
      var name = 'registered_service', service = {};
      ServiceManager.register(name, service);
      expect(ServiceManager.get(name)).to.equal(service);
    });
    it('Should throw when getting a service that does not exist', function() {
      expect(function(){ServiceManager.get('unknown');})
        .to.throw(Error, new RegExp(ServiceManager.ERROR_SERVICE_UNKNOWN));
    });
    it('Should deregister a service', function() {
      var name = 'test_deregister', service = {};
      ServiceManager.register(name, service);
      expect(ServiceManager.deregister(name)).to.be.true;
    });
    it('Should throw when deregistering a service that does not exist', function() {
      expect(function(){ServiceManager.deregister('marzipan');})
      .to.throw(Error, new RegExp(ServiceManager.ERROR_SERVICE_UNKNOWN));
    });
    it('Should stop listening to a service', function() {
      expect(ServiceManager.register('listener', {stopListening: function() {}})).to.be.true;
      expect(ServiceManager.stopListening('listener')).to.be.true;
    });
    it('Should throw when stop listening is not supported on a service', function() {
      expect(ServiceManager.register('listener2', {})).to.be.true;
      expect(function(){ServiceManager.stopListening('listener2');})
      .to.throw(Error, new RegExp(ServiceManager.ERROR_STOP_LISTENING_NOT_SUPPORTED));
    });
    it('Should throw when stop listening to a service that does not exist', function() {
      expect(function(){ServiceManager.stopListening('another-one');})
      .to.throw(Error, new RegExp(ServiceManager.ERROR_SERVICE_UNKNOWN));
    });
  });
});
