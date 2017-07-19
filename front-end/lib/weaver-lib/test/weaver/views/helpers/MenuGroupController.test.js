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
  'weaver/views/Menu',
  'weaver/views/helpers/MenuGroupController'
], function (Menu, MenuGroupController) {

  "use strict";

  describe('weaver/views/helpers/MenuGroupController.js', function () {

      var controller = new MenuGroupController({
        groupClass: 'targetMenus'
      });

      var menus = [
        new Menu({
          className: 'targetMenus is-collapsed'
        }),
        new Menu(),
        new Menu({
          className: 'targetMenus is-collapsed'
        })
      ];

      menus.forEach(function(menu) {

        controller.el.appendChild(menu.el);
      });

      before(function () {
        document.body.appendChild(controller.el);
      });

      after(function () {
        document.body.removeChild(controller.el);
      });

      it('Should collapse current menu when another menu from the group gets expanded', function () {

        menus[0].expand();
        expect(menus[0].$el).not.to.have.class('is-collapsed');

        menus[2].expand();
        expect(menus[0].$el).to.have.class('is-collapsed');
        expect(menus[2].$el).not.to.have.class('is-collapsed');
      });

      it('Should not collapse current menu when a menu from outside the group gets expanded', function () {

        menus[1].expand();
        expect(menus[2].$el).not.to.have.class('is-collapsed');
      });

      it('Should not collapse menu from outside the group', function () {

        menus[0].expand();
        expect(menus[1].$el).not.to.have.class('is-collapsed');
        expect(menus[2].$el).to.have.class('is-collapsed');
      });
  });
});
