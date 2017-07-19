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
  /** @type BaseView */
  var BaseView = require('./BaseView');
  var MenuNotificationController = require('./MenuNotificationController');

  /**
   * A base view for creating a menu that collapses/expands its content when clicking on a toggle
   * @class  Menu
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var Menu = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_Menu
     * @final
     */
    constructorName: 'LOOM_Menu',

    /**
     * @property className
     * @type {String}
     */
    className: 'mas-menu',

    /**
     * @property events
     * @type {Object}
     */
    events: {
      'click .mas-menu--toggle': function ($event) {
        if (!$event.originalEvent.didToggle) {
          this.toggle();
          $event.originalEvent.didToggle = true;
        }
      }
    },

    /**
     * @method initialize
     * @param options
     */
    initialize: function (options) {
      options = options || {};
      BaseView.prototype.initialize.apply(this, arguments);
      this.$el.data('view', this);
      this.toggleElement = this.el.querySelector('.mas-menu--toggle');
      //todo: make this a private property
      this._menuNotificationController = new MenuNotificationController({
        el: this.el
      });
      if (options.collapsed) {
        this.collapse();
      }
    },

    /**
     * method showNotification
     */
    showNotification: function () {
      this._menuNotificationController.showNotification();
    },

    /**
     * @method render
     */
    render: function () {
      if (!this.toggleElement) {
        this._renderToggle();
      }
    },

    /**
     * @method _renderToggle
     * @private
     */
    _renderToggle: function () {
      var toggle = this.toggleElement = document.createElement('button');
      toggle.classList.add('mas-menu--toggle');
      this.el.appendChild(toggle);
    },

    /**
     * @method enable
     */
    enable: function () {
      this.$el.removeClass('is-disabled');
      this.toggleElement.removeAttribute('disabled');
    },

    /**
     * @method disable
     */
    disable: function () {
      this.$el.addClass('is-disabled');
      this.toggleElement.setAttribute('disabled', true);
    },

    /**
     * Toggles the menu, collapsing it if it is expanded and vice-versa
     * @method toggle
     */
    toggle: function () {
      if (!this.$el.hasClass('is-disabled')) {
        if (this.$el.hasClass('is-collapsed')) {
          this.expand();
        } else {
          this.collapse();
        }
      }
    },

    /**
     * @method collapse
     */
    collapse: function () {
      if (!this.$el.hasClass('is-collapsed')) {
        this.$el.addClass('is-collapsed');
        this._dispatchEvent('didCollapse');
      }
    },

    /**
     * @method expand
     */
    expand: function () {
      if (this.$el.hasClass('is-collapsed')) {
        this._dispatchEvent('willExpand');
        this.$el.removeClass('is-collapsed');
        this._dispatchEvent('didExpand');
      }
    },

    /**
     * @method _dispatchEvent
     * @param name
     * @param data
     * @private
     */
    _dispatchEvent: function (name, data) {
      var evt = document.createEvent('Event');
      evt.initEvent(name, true, true);
      _.extend(evt, data, {
        view: this
      });
      this.el.dispatchEvent(evt);
    }
  });

  return Menu;
});
