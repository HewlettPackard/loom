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
  /** @type BaseView */
  var BaseView = require('./BaseView');
  var SideMenuTabbedView = require('weaver/views/SideMenu/SideMenuTabbedView');

  /**
   * @class SideMenuLayoutView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var SideMenuLayoutView = BaseView.extend({

    /**
     * @property className
     * @type {String}
     */
    className: 'mas-sideMenuLayout',

    /**
     * @method initialize
     */
    initialize: function (options) {
      this.options = options || {};
      BaseView.prototype.initialize.apply(this, arguments);
      this.listenTo(this.EventBus, 'view:action:close', function() {
        this.hideMenu();
      });
      this.listenTo(this.EventBus, 'view:action:cancel', function() {
        this.hideMenu();
      });
      this.sideMenuTabbed = new SideMenuTabbedView({
        serviceManager: options.serviceManager,
        controller: this
      });
      this.render();
    },

    /**
     * @method render
     */
    render: function () {
      if (this.options.content) {
        // @todo: this is how the threadlist is added to the page. Its actually passed as content into the side view!
        this.options.content.$el.addClass('mas-sideMenuLayout--content').appendTo(this.$el);
      }
      this.$menu = $('<div class="mas-sideMenuLayout--menu">').appendTo(this.$el);
      this.$menu.append(this.sideMenuTabbed.render().el);
    },

    /**
     * @method showMenu
     * @param menuContent
     * @param overlay
     */
    showMenu: function (menuContent, overlay) {
      this.$el.removeClass('has-hiddenMenu');
      if (overlay) {
        this.$el.addClass('mas-sideMenuLayout-withOverlay');
      } else {
        this.$el.removeClass('mas-sideMenuLayout-withOverlay');
      }
    },

    /**
     * @method hideMenu
     */
    hideMenu: function () {
      this.$el.addClass('has-hiddenMenu');
      this.$el.removeClass('mas-sideMenuLayout-withOverlay');
    },

    /**
     * @method remove
     */
    remove: function () {
      this.sideMenuTabbed.remove();
      this.sideMenuTabbed = null;
      BaseView.prototype.remove.apply(this);
    }
  });

  return SideMenuLayoutView;
});
