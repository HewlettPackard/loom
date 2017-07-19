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
define([], function () {

  "use strict";

  /** @type BaseView */
  var BaseView = require('../BaseView');

  /**
   * A helper controlling the visibility of a group of menus, ensuring that only one of them is visible at a time.
   * Menu groups get defined by a custom class common to all the menus of the group.
   *
   * It gets passed a {{#crossLink "views.ThreadListView"}}{{/crossLink}} and listens
   * to the willExpand and didCollapse events which get sent out via {{#crossLink "views.Menu:expand"}}{{/crossLink}}
   *
   * @todo Take a closer look at this,
   *
   * @class MenuGroupController
   * @namespace  views.helpers
   * @module weaver
   * @submodule views.helpers
   * @constructor
   * @extends BaseView
   */
  var MenuGroupController = BaseView.extend({

    /**
     * @property constructorName
     * @final
     */
    constructorName: 'LOOM_MenuGroupController',

    /**
     * @property {String} options.groupClass The common className shared by the menus of the group
     */

    /**
     * @property {undefined|String} options.openedMenu The menu currently opened
     */

    /**
     * @property events
     * @type {Object}
     */
    events: {
      'willExpand': function (event) {
        if (this.belongsToGroup(event.target)) {
          this.closeOpenedMenu();
          this.options.openedMenu = event.view;
        }
      },
      'didCollapse': function (event) {
        if (this.belongsToGroup(event.target)) {
          this.options.openedMenu = undefined;
        }
      }
    },

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
    },

    /**
     * Closes the menu currently opened
     * @method closeOpenedMenu
     */
    closeOpenedMenu: function () {
      if (this.options.openedMenu) {
        this.options.openedMenu.collapse();
      }
    },

    /**
     * Checks if given element belongs to the group of menu
     * @method belongsToGroup
     * @param {DOMElement} menuElement
     */
    belongsToGroup: function (menuElement) {
      return menuElement.classList.contains(this.options.groupClass);
    }
  });

  return MenuGroupController;

});
