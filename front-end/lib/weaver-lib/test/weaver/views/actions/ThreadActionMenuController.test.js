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

  //var ActionDefinition = require('weft/models/ActionDefinition');
  var SideMenuLayoutView = require('weaver/views/SideMenuLayoutView');
  var ThreadActionMenuController = require('weaver/views/actions/ThreadActionMenuController');
  //var EventBus = require('weaver/utils/EventBus');

  describe.skip('weaver/views/actions/ThreadActionMenuController.js', function () {
    before(function () {
      this.sideMenuLayoutView = new SideMenuLayoutView();
      this.controller = new ThreadActionMenuController({
        model: this.sideMenuLayoutView
      });
      this.controller.el.appendChild(this.sideMenuLayoutView.el);
      document.body.appendChild(this.controller.el);
    });
    after(function () {
      this.controller.remove();
    });
    describe('_createMenuDom', function() {
      it('should create the DOM for the menu', function() {
        this.action = {};
        this.thread = {};
        this.dom = {append: sinon.stub()};
        sinon.stub(this.controller, '_createActionDom');
        sinon.stub(this.controller, '_createActionDialogView');
        this.controller._createActionDialogView.returns({el: {}});
        this.controller._createActionDom.returns(this.dom);
        var dom = this.controller._createMenuDom(this.action, this.thread);
        expect(this.dom.append.called).to.be.true;
        expect(dom).to.equal(this.dom);
        this.controller._createActionDom.restore();
        this.controller._createActionDialogView.restore();
      });
    });
  });
});
