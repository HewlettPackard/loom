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
  var BaseView = require('weaver/views/BaseView');
  var template = require('./screenWider.html');

  /**
   * A simple labeled input
   * @class ResizeNotificationWiderView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var ResizeNotificationWiderView = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_ResizeNotificationWiderView
     * @final
     */
    constructorName: 'LOOM_ResizeNotificationWiderView',

    /**
     * @property {String} className
     * @final
     * @default mas-labeledInput-top
     */
    className: 'mas-resizeNotification',

    template: template,

    /**
     * @property {Object} events
     */
    events: {
      'click .mas-action--rebraid': function() {
        this.EventBus.trigger('screen:notify:braiding:update');
        this.EventBus.trigger('notification:cancel');
      },
      'click .mas-action-cancelRebraid': function () {
        this.EventBus.trigger('notification:cancel');
      }
    },

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.render();
    },

    /**
     * @method render
     */
    render: function () {
      return this;
    }
  });

  return ResizeNotificationWiderView;
});
