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
  var Menu = require('weaver/views/Menu');
  var PropertySelector = require('weaver/views/PropertySelector');

  /**
   * The ThreadActionsMenu lets users chose an action to execute against the Thread it holds as model
   * @class ThreadActionsMenu
   * @namespace views.ThreadViewHeader
   * @module  weaver
   * @submodule views.ThreadViewHeader
   * @extends Menu
   * @constructor
   */
  var ThreadActionsMenu = Menu.extend({

    /**
     * @property className
     * @type {String}
     */
    className: Menu.prototype.className + ' mas-actionsMenu is-collapsed',

    /**
     * @method initialize
     */
    initialize: function () {
      Menu.prototype.initialize.apply(this, arguments);
      this.updateDisabledState();
    },

    /**
     * @method expand
     */
    expand: function () {
      Menu.prototype.expand.apply(this, arguments);
      this.propertySelector = new PropertySelector({
        title: 'Actions',
        model: this.model.get('itemType').getThreadActions()
      });
      this.listenTo(this.propertySelector, 'change:selection', this.notifySelection);
      this.propertySelector.$el.addClass('mas-menu--content');
      this.el.appendChild(this.propertySelector.el);
    },

    /**
     * @method collapse
     */
    collapse: function () {
      Menu.prototype.collapse.apply(this, arguments);
      this.propertySelector.remove();
    },

    /**
     * @method enable
     */
    enable: function () {
      var actions = this.model.get('itemType').getThreadActions();
      if (!_.isEmpty(actions)) {
        Menu.prototype.enable.apply(this, arguments);
      }
    },

    /**
     * @method remove
     */
    remove: function () {
      Menu.prototype.remove.apply(this, arguments);
      if (this.propertySelector) {
        this.propertySelector.remove();
      }
    },

    /**
     * @method notifySelection
     */
    notifySelection: function (actionID) {
      //console.log(arguments, actionID);
      var action = this.model.get('itemType').getThreadActions()[actionID];
      this.dispatchCustomEvent('didSelectAction', {
        thread: this.model,
        action: action
      });
      this.collapse();
    },

    /**
     * @method updateDisabledState
     */
    updateDisabledState: function () {
      if (!this.model) {
        this.disable();
        return;
      }
      var actions = this.model.get('itemType').getThreadActions();
      if (_.isEmpty(actions)) {
        this.disable();
      }
    }
  });

  return ThreadActionsMenu;
});
