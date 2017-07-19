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

  var Backbone = require('backbone');
  var ElementStateController = require('weaver/views/ElementView/ElementStateController');

  describe('weaaver/views/ElementView/ElementStateController.js', function () {

    beforeEach(function () {

      this.model = new Backbone.Model();
      this.model.state = 'added';
      this.controller = new ElementStateController({
        model: this.model
      });
    });

    it('Should display the state of its model', function () {

      expect(this.controller.$el).to.have.class('mas-element-added');
    });

    it('Should update the state when the model state changes', function () {

      this.model.trigger('didSetState', 'updated');
      expect(this.controller.$el).not.to.have.class('mas-element-added');
      expect(this.controller.$el).to.have.class('mas-element-updated');

      this.model.trigger('didSetState');
      expect(this.controller.$el).not.to.have.class('mas-element-updated');
    });
  });
});
