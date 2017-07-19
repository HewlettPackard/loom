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

  var $ = require('jquery');
  var Backbone = require('backbone');
  var AboutView = require('weaver/views/Toolbar/AboutView');
  var StatusLoader = require('weft/services/StatusLoader');
  var adapterCollection = new Backbone.Collection();

  describe('weaver/views/AboutView.js', function () {

    beforeEach(function () {
 	  var service =  new StatusLoader();
 	  service.set('adapters', adapterCollection);

      this.view = new AboutView({service: service});
      document.body.appendChild(this.view.el);
    });

    afterEach(function () {
      this.view.remove();
    });

    describe('render()', function () {

      beforeEach(function () {
        this.$notification = $('<p class="my-notification">Notification</p>');
      });

      it('Should render AboutView', function () {
    		adapterCollection.add({
    		  "id": "private/os",
    		  "providerId": "private",
    		  "providerType": "os",
    		  "build": "loom-adapter-os",
    		  "version": "1.0-SNAPSHOT (bfbe35aa)",
    		  "name": "Private",
    		  "className": "class com.hp.hpl.loom.adapter.os.fake.FakeAdapter",
    		  "loadedTime": 1412070991021
    		});
    		adapterCollection.add({
    		  "id": "public/os",
    		  "providerId": "private",
    		  "providerType": "os",
    		  "build": "loom-adapter-os",
    		  "version": "1.0-SNAPSHOT (bfbe35aa)",
    		  "name": "Public",
    		  "className": "class com.hp.hpl.loom.adapter.os.fake.FakeAdapter",
    		  "loadedTime": 1412070991029
    		});
        expect(this.view.$el).to.have.descendants(':contains(private/os).mas-about--adapter');
        expect(this.view.$el).to.have.descendants(':contains(public/os).mas-about--adapter');
      });

    });
  });
});
