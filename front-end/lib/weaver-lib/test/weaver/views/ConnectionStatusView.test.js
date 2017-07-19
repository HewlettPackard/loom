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
  'backbone',
  'weaver/views/Toolbar/ConnectionStatusView'
], function (Backbone, ConnectionStatusView) {

  'use strict';

  describe('weaver/views/Toolbar/ConnectionStatusView.js', function () {

    // Just mocking the loader
    this.statusLoader = new Backbone.Model();
    this.statusLoader.poll = function (){};
    this.statusLoader.set('status', undefined);

    var view = new ConnectionStatusView({
      model: this.statusLoader,
      downThreshold: 0.4
    });

    it('Should have `.mas-connectionStatus-ok` class when status is 1', function () {

      view.model.set('status', 1);

      expect(view.$el).to.have.class('mas-connectionStatus-ok');
    });

    it('Should have `.mas-connectionStatus-unstable` class when status is below 1 but not under `downThreshold`', function () {

      view.model.set('status', 0.9);

      expect(view.$el).to.have.class('mas-connectionStatus-unstable');
      expect(view.$el).not.to.have.class('mas-connectionStatus-ok');
    });

    it('Should have `.mas-connectionStatus-down` class when status is below `downThreshold`', function () {

      view.model.set('status', 0.2);
      expect(view.$el).to.have.class('mas-connectionStatus-down');
      expect(view.$el).not.to.have.class('mas-connectionStatus-unstable');
    });
  });
});
