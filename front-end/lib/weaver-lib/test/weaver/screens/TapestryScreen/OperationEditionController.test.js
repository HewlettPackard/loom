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

  var OperationEditionController = require('weaver/screens/TapestryScreen/OperationEditionController');
  var EditableOperationView = require('weaver/views/QueryEditor/EditableOperationView');

  describe('weaver/screens/TapestryScreen/OperationEditionController.js', function() {

    function simulateClick(element) {

      var event = document.createEvent('MouseEvent');
      event.initEvent('click', true, true);
      element.dispatchEvent(event);
    }

    beforeEach(function() {

      this.controller = new OperationEditionController();

      this.views = [
        new EditableOperationView({
          model: {
            operator: 'braid',
            parameters: {
              maxFibres: 25
            }
          }
        }),
        new EditableOperationView({
          model: {
            operator: 'braid',
            parameters: {
              maxFibres: 25
            }
          }
        })

      ];

      this.views.forEach(function(view) {
        this.controller.el.appendChild(view.el);
      }, this);

      this.outsideElement = document.createElement('div');
      this.controller.el.appendChild(this.outsideElement);

      document.body.appendChild(this.controller.el);
    });

    afterEach(function() {

      this.controller.remove();
    });

    it('Allows only one operation to be edited at a time', function() {

      this.views[0].edit();
      expect(this.views[0].$el).to.have.class('is-editing');
      expect(this.views[1].$el).not.to.have.class('is-editing');

      this.views[1].edit();
      expect(this.views[0].$el).not.to.have.class('is-editing');
      expect(this.views[1].$el).to.have.class('is-editing');
    });

    it('Cancels edition when clicking outside of the edited operation', function() {

      this.views[0].edit();
      expect(this.views[0].$el).to.have.class('is-editing');

      simulateClick(this.outsideElement);
      expect(this.views[0].$el).not.to.have.class('is-editing');
    });

    it('Does not cancel edition when clicking an element inside the edited operation', function() {

      this.views[0].edit();
      expect(this.views[0].$el).to.have.class('is-editing');

      var insideElement = document.createElement('div');
      this.views[0].el.appendChild(insideElement);

      simulateClick(insideElement);
      expect(this.views[0].$el).to.have.class('is-editing');
    });
  });
});
