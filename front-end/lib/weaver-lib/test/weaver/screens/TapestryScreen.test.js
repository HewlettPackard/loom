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
define(function(require) {
  "use strict";

  var EventBusMixin = require('weaver/mixins/EventBusMixin');
  var TapestryScreen = require('weaver/screens/TapestryScreen');
  var ServiceManager = require('weaver/utils/ServiceManager');
  var TapestryServicesController = require('weaver/screens/TapestryScreen/TapestryServicesController');

  describe('weaver/screens/TapestryScreen.js', function () {
    describe.skip('on EventBus:willSelectElement', function() {
      before(function() {
        this.ServiceManager = sinon.stub(ServiceManager);
        this.TapestryServicesController = sinon.stub(TapestryServicesController);
      });
      after(function() {
        this.ServiceManager.restore();
        this.TapestryServicesController.restore();
      });
      beforeEach(function() {
        this.eventName = 'willSelectElement';
        this.screen = new TapestryScreen({
          ServiceManager: this.ServiceManager,
          TapestryServicesController: this.TapestryServicesController
        });
        this.view = {unselectElement: function(){}};
        this.payload = {view: this.view};
      });
      // there is no way to bring up the tapestryScreen on its own without creating a huge mock.
      // it currently creates 26 objects and needs to be broken down into a maintainable structure
      // update 03/08/16, TapestryScreen is getting smaller but still requires lots000.. keep working on this..
      it('Should unselect the current view', function() {
        var spy = sinon.spy(this.view, 'unselectElement');
        this.screen.selectedElement = this.view;
        EventBusMixin.broadcast(this.eventName, this.payload);
        expect(spy.called).to.be.false;
      });
    });
  });
});
