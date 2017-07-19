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

  var Element = require('weft/models/Element');
  var FiberOverview = require('weaver/views/Element/FiberOverview');

  function simulatePointerEnteringElement(element, pointerType) {
    var event = document.createEvent('Event');
    event.initEvent('pointerover', true, true);
    event.pointerType = pointerType;
    element.dispatchEvent(event);
  }

  function simulatePointerLeavingElement(element) {
    var event = document.createEvent('Event');
    event.initEvent('pointerout', true, true);
    element.dispatchEvent(event);
  }

  // we are moving away from this tooltip controller, soon to be deprecated
  describe.skip('weaver/views/FiberOverview/TooltipController.js', function () {
    beforeEach(function () {
      this.view = new FiberOverview({
        model: new Element({
          name: 'element'
        })
      });
      document.body.appendChild(this.view.el);
    });
    afterEach(function () {
      document.body.removeChild(this.view.el);
    });
    it('Delays the apparition of the tooltip when using a mouse', sinon.test(function () {
      simulatePointerEnteringElement(this.view.el, 'mouse');
      expect(this.view.$el).not.to.have.class('has-visibleTooltip');
      this.clock.tick(500);
      expect(this.view.$el).to.have.class('has-visibleTooltip');
      simulatePointerLeavingElement(this.view.el); // Cleanup :)
    }));
    it('Cancels the apparition of the tooltip when the pointer leaves the element befor the tooltip appeared', sinon.test(function () {

      simulatePointerEnteringElement(this.view.el, 'mouse');
      expect(this.view.$el).not.to.have.class('has-visibleTooltip');
      this.clock.tick(200);
      simulatePointerLeavingElement(this.view.el);
      this.clock.tick(300);
      expect(this.view.$el).not.to.have.class('has-visibleTooltip');
    }));
  });
});
