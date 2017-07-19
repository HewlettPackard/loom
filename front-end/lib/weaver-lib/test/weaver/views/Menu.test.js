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

  var Menu = require('weaver/views/Menu');

  describe('weaver/views/Menu.js', function () {

    var element = document.createElement('div');
    element.innerHTML = '<div class="mas-menu--toggle"></div>';

    var menu = new Menu({
      el: element
    });

    before(function () {
      document.body.appendChild(menu.el);
    });

    after(function () {
      document.body.removeChild(menu.el);
    });

    function clickOnToggle(menu) {

      var toggle = menu.el.querySelector('.mas-menu--toggle');
      var event = document.createEvent('MouseEvent');
      event.initEvent('click', true, true);
      toggle.dispatchEvent(event);
    }

    it('Should allow the user to collapse/expand the menu by clicking the toggle', function () {

      clickOnToggle(menu);
      expect(menu.$el).to.have.class('is-collapsed');

      clickOnToggle(menu);
      expect(menu.$el).not.to.have.class('is-collapsed');
    });

    it('Should trigger events when collapsing/expanding', function () {

      var willExpandSpy = sinon.spy();
      var didCollapseSpy = sinon.spy();
      var didExpandSpy = sinon.spy();

      menu.el.addEventListener('willExpand', willExpandSpy);
      menu.el.addEventListener('didCollapse', didCollapseSpy);
      menu.el.addEventListener('didExpand', didExpandSpy);

      // Menu is left open from previous test,
      // first click will collapse it
      clickOnToggle(menu);
      expect(didCollapseSpy).to.have.been.called;

      // Shouldn't trigger events again, as menu is already collapsed
      menu.collapse();
      expect(didCollapseSpy).not.to.have.been.calledTwice;

      // Not his expands the menu
      clickOnToggle(menu);
      expect(willExpandSpy).to.have.been.called;
      expect(didExpandSpy).to.have.been.called;

      // But this shouldn't trigger the events again
      menu.expand();
      expect(willExpandSpy).not.to.have.been.calledTwice;
      expect(didExpandSpy).not.to.have.been.calledTwice;
    });

    it('Should not collapse/expand containing menu when nested', function () {
      var element = document.createElement('div');
      element.innerHTML = '<div class="mas-menu--toggle"></div>';
      var nestedMenu = new Menu({
        el: element
      });
      menu.el.appendChild(nestedMenu.el);
      clickOnToggle(nestedMenu);
      expect(menu.$el).not.to.have.class('is-collapsed');
    });


    describe('showNotification()', function () {
      expect(menu.$el).not.to.have.class('has-notification');
      menu.collapse();
      menu.showNotification();
      expect(menu.$el).to.have.class('has-notification');
      menu.expand();
      expect(menu.$el).not.to.have.class('has-notification');
      menu.showNotification();
      expect(menu.$el).not.to.have.class('has-notification');
    });

  });
});
