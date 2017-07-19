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

  var _ = require('lodash');
  var $ = require('jquery');
  var fastdom = require('fastdom');
  var BaseView = require('weaver/views/BaseView');
  var ElementDetailsView = require('weaver/views/Element/ElementDetailsView');
  var RelationTypeMenu = require('weaver/views/relations/RelationTypeMenu');
  var RelationsGraph = require('weaver/views/relations/RelationsGraph');
 
  /**
   * SideMenuController controls which screen is displayed in the SideMenu. It has a known set of renders which are
   * able to handle providers, patterns, relations and the relation graph.
   *
   * @backbone  no-initialize
   * @class     SideMenuController
   * @namespace screens.TapestryScreen
   * @module    weaver
   * @submodule screens.TapestryScreen
   * @constructor
   * @extends   BaseView
   */
  var SideMenuController = BaseView.extend({

    /**
     * A set of menu options that can be displayed using the "primary-menu:" messages
     * @property {Array} renderers
     */
    renderers: {
      'relations': function (aggregatorClient, options) {
        return new RelationTypeMenu(_.omit(options, 'el'));
      },
      'relationsGraph': function (aggregatorClient, options) {
        return new RelationsGraph({
          service: options.relationshipService,
          displayedThreads: options.displayedThreads,
          aggregatorClient: aggregatorClient
        });
      }
    },

    events: {
      'click .mas-action-toggleSideMenu': function (event) {
        var menu = this.$(event.currentTarget).data('menu');
        var overlay = this.$(event.currentTarget).data('overlay');
        var message = 'primary-menu:item:' + ((this.menu === menu) ? 'hide': 'show');
        this.EventBus.trigger(message, {
          menu: menu,
          overlay: overlay
        });
      },
      //@todo: convert this to event bus message..
      'click ': function (event) {
        var outsideMenuSelector = '.mas-sideMenuLayout-withOverlay, .mas-sideMenuLayout-withOverlay :not(.mas-sideMenuLayout--menu *, .mas-sideMenuLayout--menu)' ;
        if ($(event.target).is(outsideMenuSelector)) {
          this.hideMenu();
        }
      }
    },

    /**
     * The model passed in is a SideMenuLayoutView
     */
    initialize: function(options) {
      this.options = options || {};
      BaseView.prototype.initialize.apply(this, arguments);
      this.serviceManager = options.serviceManager;
      this.listenTo(this.EventBus, 'fiber:selected', function(event) {
        this.selectFiber(event.fiberView);
      });
    },

     /**
     * @method showMenu
     * @param menu
     * @param overlay
     */
    showMenu: function (menu, overlay) {
      this._stopListeningToContent();
      var content = this._buildMenuContent(menu);
      this._broadcastWillShowMenu();
      this._showModelMenu(overlay);
      this._manuallyRenderContent(this.content.manualRender, content);
    },

    /**
     * Renders a selected fiber into the side menu
     * @method selectFiber
     * @param {Backbone.View} view
     */
    selectFiber: function (view) {
      this._stopListeningToContent();
      var content = this.content = new ElementDetailsView({
        model: view.model
      });
      this._broadcastWillShowMenu();
      this._showModelMenu(false);
      this._manuallyRenderContent(this.content.manualRender, content);
    },

    /**
     * @method _broadcastWillShowMenu
     * @private
     */
    _broadcastWillShowMenu: function() {
     // EventBus.trigger('willShowMenu', {controller: this});
    },

    /**
     * Helper to safely stop listening to content
     * @method _stopListeningToContent
     * @private
     */
    _stopListeningToContent: function () {
      // Make sure we discard the menu properly
      if (this.content) {
        this.content.stopListening();
      }
    },

    /**
     * Build the menu content
     * @method _buildMenuContent
     * @param menu
     * @returns {*}
     * @private
     */
    _buildMenuContent: function (menu) {
      var content = this.content = this.renderers[menu](this.options.aggregatorClient, this.options);
      this.menu = menu;
      return content;
    },

    /**
     * this.model is a SideMenuLayoutView, so calling showMenu on it with this.content.el actually renders the content
     * directly into the div 9with an overlay if required)
     *
     * @method _showModelMenu
     * @param overlay
     * @private
     */
    _showModelMenu: function (overlay) {
      this.model.showMenu(this.content.el, overlay);
    },

    /**
     * @method _manuallyRenderContent
     * @param manualRender
     * @param content
     * @private
     */
    _manuallyRenderContent: function (manualRender, content) {
      if (manualRender) {
        fastdom.write(function () {
          content.render();
        });
      }
    },

    /**
     * @method hideMenu
     */
    hideMenu: function () {
      this.model.hideMenu();
      this._stopListeningToContent();
      this.content = undefined;
      this.menu = undefined;
    }
  });

  return SideMenuController;
});
