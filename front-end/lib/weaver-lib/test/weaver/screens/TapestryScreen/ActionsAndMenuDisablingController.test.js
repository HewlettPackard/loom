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

  var ActionsAndMenuDisablingController = require('weaver/screens/TapestryScreen/ActionsAndMenuDisablingController');
  var Aggregation = require('weft/models/Aggregation');
  var Backbone = require('backbone');
  var Menu = require('weaver/views/Menu');
  var ElementView = require('weaver/views/Element/ElementView');

  describe('weaver/screens/TapestryScreen/ActionsAndMenuDisablingController.js', function () {
    
    beforeEach(function () {
      this.statusLoaderMock = new Backbone.Model({
        status: 1
      });
      this.elementMock = new Aggregation();
      this.controller = new ActionsAndMenuDisablingController({
        model: this.statusLoaderMock
      });
      this.menu = new Menu();
      this.menu.render();
      this.elementView = new ElementView({
        model: this.elementMock
      });
      this.controller.el.appendChild(this.menu.el);
      this.controller.el.appendChild(this.elementView.el);
      document.body.appendChild(this.controller.el);
    });

    afterEach(function () {
      document.body.removeChild(this.controller.el);
    });

    it('Should disable menus when the connection goes down and re-enable them when the connection gets back up', function () {
      var $toggle = this.menu.$('.mas-menu--toggle');
      this.statusLoaderMock.set('status', 0.1);
      expect($toggle).to.be.disabled;
      this.statusLoaderMock.set('status', 1);
      expect($toggle).not.to.be.disabled;
    });

    it.skip('Should disable actions (apart from filtering) of displayed ElementDetailsViews', function () {
      this.elementView.selectElement();
      this.statusLoaderMock.set('status', 0.1);
      expect(this.elementView.$('.mas-action--view')).to.be.disabled;
      expect(this.elementView.$('.mas-action--filter')).not.to.be.disabled;
      this.statusLoaderMock.set('status', 1);
      expect(this.elementView.$('.mas-action--view')).not.to.be.disabled;
    });

    it.skip('Should disable actions (apart from filtering) on new ElementDetailViews', function () {
      this.statusLoaderMock.set('status', 0.1);
      this.elementView.selectElement();
      expect(this.elementView.$('.mas-action--view')).to.be.disabled;
      expect(this.elementView.$('.mas-action--filter')).not.to.be.disabled;
    });
  });
});