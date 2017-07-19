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
define(function(require) {
  "use strict";

  var $ = require('jquery');
  var ActionDialogView = require('weaver/views/Element/ActionDialogView');
  var BaseView = require('weaver/views/BaseView');
  var EventBus = require('weaver/utils/EventBus');

  /**
   * @class ThreadActionMenuController
   * @module weaver
   * @submodule views.actions
   * @namespace  views.actions
   * @constructor
   * @extends BaseView
   */
  var ThreadActionMenuController = BaseView.extend({

    /**
     * Enables the extended view
     * @param {Boolean} extendedView
     */
    extendedView: false,

    events: {
      'didSelectAction': function (event) {
        if (!event.isDefaultPrevented()) {
          event.preventDefault();
          //todo: send action event on eventbus and forget about it
          EventBus.trigger('thread:action:display', {
            thread: event.originalEvent.thread,
            action: event.originalEvent.action
          });
          this.showMenu(event.originalEvent.action, event.originalEvent.thread);
        }
      },
      // @todo: move this out of here..
      'click [data-action=closeActionsMenu], .mas-action-hideDialog': function (event) {
        event.preventDefault();
        this.hideMenu();
      }
    },

    /**
     * @method hideMenu
     */
    hideMenu: function () {
      this.model.hideMenu();
    },

   /**
     * @method showMenu
     * @param action
     * @param thread
     * @param {boolean} extendedView
     */
    showMenu: function (action, thread, extendedView) {
      this.extendedView = extendedView || false;
      var $dom = this._createMenuDom(action, thread);
      this._broadcastWillShowMenu();
      this.model.showMenu($dom);
    },

    /**
     * @method _broadcastWillShowMenu
     * @private
     */
    _broadcastWillShowMenu: function() {
      EventBus.trigger('willShowMenu', {controller: this});
    },

    /**
     * @method _createMenuDom
     * @param action
     * @param thread
     * @returns {*|jQuery|HTMLElement}
     * @private
     */
    _createMenuDom: function (action, thread) {
      var view = this._createActionDialogView(action, thread);
      var $dom = this._createActionDom(action);
      $dom.append(view.el);
      return $dom;
    },

    /**
     * Create the ActionDialogView from the action and thread passed
     * @method _createActionDialogView
     * @param action
     * @param thread
     * @private
     */
    _createActionDialogView: function (action, thread) {
      return new ActionDialogView({
        model: action,
        element: thread,
        extendedView: this.extendedView
      });
    },

    /**
     * Create the DOM for the action
     * @param action
     * @returns {jQuery|HTMLElement}
     * @private
     */
    _createActionDom: function (action) {
      return $('<div class="mas-threadAction"><h1 class="mas-propertySelector--title">' + action.get('name') + '</h1></div>');
    }
  });

  return ThreadActionMenuController;
});
