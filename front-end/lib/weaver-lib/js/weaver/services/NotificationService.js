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
  var _ = require('lodash');
  var FilterService = require('weft/services/FilterService');
  var EventBus = require('weaver/utils/EventBus');
  var MessageNotificationView = require('weaver/views/Notifications/MessageNotificationView');
  var ResizeNotificationWiderView = require('weaver/views/Notifications/ResizeNotificationWiderView');
  var ResizeNotificationNarrowerView = require('weaver/views/Notifications/ResizeNotificationNarrowerView');

  /**
   * NotificationService is the main service that handles tapestry notifications. it received messages and determines
   * which notifications to display and for how long. Do not call the functions in this service directly, you should
   * communicate with it via the message bus.
   *
   * @class NotificationService
   * @namespace services
   * @module weaver
   * @submodule services
   * @extends FilterService
   */
  var NotificationService = FilterService.extend({

    /**
     * @property {String} constructorName
     */
    constructorName: 'LOOM_NotificationService',

    /**
     * @property {Backbone.View} tapestry
     */
    tapestry: null,

    /**
     * The timeout ID for clearing the notification
     * @property {Number} timeout
     */
    timeout: null,

    /**
     * The number of milliseconds to display the Notification
     * @property {Number} notificationTimeMs
     */
    notificationTimeMs: 1500000,

    /**
     * Current notification, only one can be allowed at once, and for X seconds
     */
    notification: null,

    /**
     * Global event bus for inter module communication
     * @type Backbone.Events
     */
    EventBus: EventBus,

    initialize: function (tapestry) {
      FilterService.prototype.initialize.apply(this, arguments);
      this.tapestry = tapestry;
      this.listenTo(this.EventBus, 'screen:notify:braiding:narrower', function(event) {
        this.showNotification(new ResizeNotificationNarrowerView(event));
      });
      this.listenTo(this.EventBus, 'screen:notify:braiding:wider', function(event) {
        this.showNotification(new ResizeNotificationWiderView(event));
      });
      this.listenTo(
        this.EventBus, 'fiber:action:server:send thread:action:server:send', function(event) {
          this.showNotification(new MessageNotificationView({message: "Sending action to server", event: event}));
        }
      );
      this.listenTo(
        this.EventBus, 'fiber:action:server:send:success thread:action:server:send:success', function(e) {
          this.showNotification(new MessageNotificationView({message: "Action successfully sent to server", event: e}));
        }
      );
      this.listenTo(
        this.EventBus, 'fiber:action:server:send:fail thread:action:server:send:fail', function(event) {
          this.showNotification(new MessageNotificationView({
            message: "There was a problem sending the action to the server",
            event: event
          }));
        }
      );
      this.listenTo(this.EventBus, 'notification:cancel', function(event) {
        this.clearNotification(event);
      });
    },

    /**
     * Show the notification on the screen
     * @method showNotification
     * @param notification
     */
    showNotification: function(notification) {
      this.clearNotification();
      this.el = this.tapestry.$el.find('.mas-notification');
      this.notification = notification;
      this.el.append(notification.el);
      $(this.tapestry.el).addClass('has-notification');
      this.timeout = setTimeout(_.bind(this.clearNotification, this), this.notificationTimeMs);
    },

    /**
     * Clear the notification from the screen
     * @method clearNotification
     */
    clearNotification: function() {
      if (this.notification) {
        clearTimeout(this.timeout);
        this.notification.stopListening();
        this.notification.remove();
        this.notification = null;
        $(this.tapestry.el).removeClass('has-notification');
      }
    }
  });

  return NotificationService;
});
