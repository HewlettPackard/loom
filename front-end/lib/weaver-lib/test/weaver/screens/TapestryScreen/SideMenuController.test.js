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
  var fastdom = require('fastdom');
  var FilterService = require('weft/services/FilterService');
  var AggregatorClient = require('weft/services/AggregatorClient');
  var SideMenuLayoutView = require('weaver/views/SideMenuLayoutView');
  var SideMenuController = require('weaver/views/SideMenu/SideMenuController');
  var EventBus = require('weaver/utils/EventBus');

  /**
   * We no longer use this, but I have kept the code for historical reference when we implement
   * Relations and relation graph
   */
  describe.skip('weaver/screens/TapestryScreen/SideMenuController.js', function () {
    before(function () {
      this.sideMenuLayoutView = new SideMenuLayoutView();
      this.toggles = ['providers','patterns','relations'].map(function (menu) {
        return $('<div class="mas-action-toggleSideMenu">').attr('data-menu', menu);
      });
      this.sideMenuController = new SideMenuController({
        model: this.sideMenuLayoutView,
        aggregatorClient: new AggregatorClient(),
        relationshipHighlightService: new FilterService(),
        relationTypeList: new Backbone.Collection(),
        relationshipService: new FilterService()
      });
      this.sideMenuController.$el.append(this.toggles).appendTo(document.body);
    });
    after(function () {
      this.sideMenuController.remove();
    });
    it('Displays the providers menu', function () {
      this.toggles[0].click();
      expect(this.sideMenuLayoutView.$el).not.to.have.class('has-hiddenMenu');
      expect(this.sideMenuLayoutView.$('.mas-providerList')).to.exist;
    });
    it('Displays the patterns menu', function () {
      this.toggles[1].click();
      expect(this.sideMenuLayoutView.$el).not.to.have.class('has-hiddenMenu');
      expect(this.sideMenuLayoutView.$('.mas-patternList')).to.exist;
    });
    it('Displays the relations menu', function () {
      this.toggles[2].click();
      expect(this.sideMenuLayoutView.$el).not.to.have.class('has-hiddenMenu');
      expect(this.sideMenuLayoutView.$('.mas-relationTypeList')).to.exist;
    });
    it('Closes the menu', function () {
      this.toggles[2].click();
      expect(this.sideMenuLayoutView.$el).to.have.class('has-hiddenMenu');
      expect(this.sideMenuLayoutView.$('.mas-relationTypeList')).to.exist;
    });
    describe('showMenu', function() {
      beforeEach(function() {
        this.menu = {};
        this.overlay = {};
        this.content = {};
        this._stopListeningToContent = sinon.stub(this.sideMenuController, '_stopListeningToContent');
        this._buildMenuContent = sinon.stub(this.sideMenuController, '_buildMenuContent');
        this._buildMenuContent.returns(this.content);
        this._broadcastWillShowMenu = sinon.stub(this.sideMenuController, '_broadcastWillShowMenu');
        this._showModelMenu = sinon.stub(this.sideMenuController, '_showModelMenu');
        this._manuallyRenderContent = sinon.stub(this.sideMenuController, '_manuallyRenderContent');
      });
      afterEach(function() {
        this.sideMenuController._stopListeningToContent.restore();
        this.sideMenuController._buildMenuContent.restore();
        this.sideMenuController._broadcastWillShowMenu.restore();
        this.sideMenuController._showModelMenu.restore();
        this.sideMenuController._manuallyRenderContent.restore();
      });
      it('Should safely show the menu' , function() {
        this.sideMenuController.content = {manualRender: true};
        this.sideMenuController.showMenu(this.menu, this.overlay);
        expect(this.sideMenuController._stopListeningToContent.called).to.be.true;
        expect(this.sideMenuController._buildMenuContent).to.have.been.calledWith(this.menu);
        expect(this.sideMenuController._broadcastWillShowMenu.called).to.be.true;
        expect(this.sideMenuController._showModelMenu).to.have.been.calledWith(this.overlay);
        expect(this.sideMenuController._manuallyRenderContent).to.have.been.calledWith(true, this.content);
      });
    });
    describe('_broadcastWillShowMenu', function() {
      it('Should send the willShowMenu event', function() {
        var spy = sinon.spy();
        EventBus.once('willShowMenu', spy);
        this.sideMenuController._broadcastWillShowMenu();
        var spyCall = spy.getCall(0);
        expect(spyCall.args[0].controller).to.equal(this.sideMenuController);
      });
    });
    describe('_stopListeningToContent', function() {
      it('Should safely stop listening to content', function() {
        this.sideMenuController.content = {stopListening: sinon.spy()};
        this.sideMenuController._stopListeningToContent();
        expect(this.sideMenuController.content.stopListening.called).to.be.true;
      });
    });
    describe('_buildMenuContent', function() {
      it('Should render the menu content', function() {
        this.menu = 'fake_menu_option';
        this.content = {fake: 'content'};
        // mock this line: this.renderers[menu](this.options.aggregatorClient, this.options);
        this.sideMenuController.renderers = [];
        this.sideMenuController.renderers[this.menu] = sinon.stub();
        this.sideMenuController.renderers[this.menu].returns(this.content);
        expect(this.sideMenuController.menu).to.be.undefined;
        var content = this.sideMenuController._buildMenuContent(this.menu);
        expect(content).to.equal(this.content);
        expect(this.sideMenuController.content).to.equal(this.content);
        expect(this.sideMenuController.menu).to.equal(this.menu);
      });
    });
    describe('_showModelMenu', function() {
      it('Should show the model menu', function() {
        this.overlay = {test: 'fixture'};
        this.sideMenuController.content = {el: 'fixture'};
        this.sideMenuController.model = {showMenu: sinon.spy()};
        this.sideMenuController._showModelMenu(this.overlay);
        expect(this.sideMenuController.model.showMenu).to.have.been.calledWith(
          this.sideMenuController.content.el,
          this.overlay
        );
      });
    });
    describe('_manuallyRenderContent', function() {
      it('Should not render the content if manual render is disabled', function() {
        this.content = {render: sinon.spy()};
        sinon.stub(fastdom, 'write');
        this.sideMenuController._manuallyRenderContent(false, this.content);
        expect(this.content.render.called).to.be.false;
        expect(fastdom.write.called).to.be.false;
        fastdom.write.restore();
      });
      it('Should render the content if manual render is enabled', function() {
        this.content = {render: sinon.spy()};
        sinon.stub(fastdom, 'write');
        this.sideMenuController._manuallyRenderContent(true, this.content);
        expect(fastdom.write.called).to.be.true;
        fastdom.write.restore();
      });
    });
    describe('hideMenu', function() {
      it('Should hide and reset the menu', function() {
        this.sideMenuController.menu = 'menu';
        this.sideMenuController.content = 'content';
        this.sideMenuController.model = {hideMenu: sinon.stub()};
        sinon.stub(this.sideMenuController, '_stopListeningToContent');
        this.sideMenuController.hideMenu();
        expect(this.sideMenuController.menu).to.be.undefined;
        expect(this.sideMenuController.content).to.be.undefined;
        expect(this.sideMenuController.model.hideMenu.called).to.be.true;
        expect(this.sideMenuController._stopListeningToContent.called).to.be.true;
        this.sideMenuController._stopListeningToContent.restore();
      });
    });
  });
});
