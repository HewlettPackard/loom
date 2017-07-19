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
  var template = require('./message.html');

  /**
   * A simple labeled input
   * @class MessageNotificationView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var MessageNotificationView = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_MessageNotificationView
     * @final
     */
    constructorName: 'LOOM_MessageNotificationView',

    /**
     * @property {String} className
     * @final
     * @default mas-messageNotification
     */
    className: 'mas-messageNotification',

    template: template,

    message: null,

    /**
     * @property {Object} events
     */
    events: {
      'click .mas-action-dismiss': function () {
        this.EventBus.trigger('notification:cancel');
      }
    },

    /**
     * @method initialize
     */
    initialize: function (options) {
      this.message = options.message;
      this.event = options.event;
      BaseView.prototype.initialize.apply(this, arguments);
      this.render();
    },

    /**
     * @method render
     */
    render: function () {
      var status = this.message;
      if (this.event &&
          this.event.response &&
          this.event.response.overallStatus === 'aborted' &&
          this.event.response.hasOwnProperty('errorMessage') &&
          this.event.response.errorMessage !== undefined &&
          this.event.response.errorMessage !== ''
      ) {
          status = 'Server Error: '+this.event.action.get('name')+', '+this.event.response.errorMessage+'.';
      }
      this.$el.find('.mas-notificationMessage').html(status);
      return this;
    }
  });

  return MessageNotificationView;
});
