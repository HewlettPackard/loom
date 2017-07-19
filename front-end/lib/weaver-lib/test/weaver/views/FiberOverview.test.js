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

  var FiberOverview = require('weaver/views/Element/FiberOverview');

  describe('weaver/views/FiberOverview.js', function () {

    beforeEach(function () {
      this.view = new FiberOverview();
    });

    // tooltips have moved for now but might be coming back into this element...
    describe.skip('showTooltip()', function () {
      it('Shows tooltip at given offset', function () {
        this.view.showTooltip(10);
        expect(this.view.$el).to.have.class('has-visibleTooltip');
        expect(this.view.$tooltip).to.have.css('bottom', '10px');
      });
    });

    describe.skip('hideTooltip()', function () {
      it('Should hide the tooltip', function () {
        this.view.showTooltip();
        this.view.hideTooltip();
        expect(this.view.$el).not.to.have.class('has-visibleTooltip');
      });
    });
  });
});
