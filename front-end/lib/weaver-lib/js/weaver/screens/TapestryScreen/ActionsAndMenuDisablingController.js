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
  var BaseView = require('../../views/BaseView');

  /**
   * ActionsAndMenuDisablingController enable and disable actions according to the status of the connection to the
   * Loom server. When the connection goes down, it prevents the user from executing any action that would trigger
   * a request to the Loom server.
   * @class ActionsAndMenuDisablingController
   * @namespace screens.TapestryScreen
   * @module weaver
   * @submodule screens.TapestryScreen
   * @constructor
   * @extends BaseView
   */
  var ActionsAndMenuDisablingController = BaseView.extend({

    events: {
      /**
       * @param event
       */
      'didSelectElement': function (event) {
        //todo: move this to an EventBus listener
        var selectedElement = event.originalEvent.view;
        if (this.preventActionsOnElement) {
          selectedElement.$('.mas-action:not(.mas-action--filter)').prop('disabled', true);
        }
      }
    },

    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.listenTo(this.model, 'change:status', function (statusLoader, status) {
        this.updateActionsAndMenuAvailability(status);
      });
    },

    /**
     * Menus are enabled/disabled depending on the current state value passed
     * @method updateActionsAndMenuAvailability
     * @param status
     */
    updateActionsAndMenuAvailability: function (status) {
      //todo: What does 0.2 mean? Where does this number come from?
      if (status > 0.2) {
        this.enableMenus();
      } else {
        this.disableMenus();
      }
    },

    /**
     * @method enableMenus
     */
    enableMenus: function () {
      // IMPROVE: Use a set class on the HTML buttons to disable, so that a unique jQuery call can be made
      this.$('.mas-menu').each(function (index, menu) {
        $(menu).data('view').enable();
      });
      this.$('.mas-elementDetails .mas-action:not(.mas-action--filter)').prop('disabled', false);
      this.preventActionsOnElement = false;
    },

    /**
     * @method disableMenus
     */
    disableMenus: function () {
      this.$('.mas-menu').each(function (index, menu) {
        $(menu).data('view').disable();
      });
      $('.mas-elementDetails .mas-action:not(.mas-action--filter)').prop('disabled', true);
      this.preventActionsOnElement = true;
    }
  });

  return ActionsAndMenuDisablingController;
});