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

  /** @type BaseView */
  var BaseView = require('./BaseView');

  /**
   * @backbone no-initialize
   * @class CustomEventDispatcher
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  return BaseView.extend({

    /**
     * Allow sub classes to dispatch a custom dom event that contains the original event.
     * @todo Remove DOM events, its messy and harder to test. Use the Backbone.events bus
     * @private
     * @method __dispatchEvent
     * @param {Event|CustomEvent} oldEvent is the previous event (null if not relevant)
     * @param {String} name     is the name of the event to fire
     * @param {Object} args     arguments stored inside the event that can be used by any handler.
     * @param {Thread} thread
     * @private
     */
    __dispatchEvent: function (oldEvent, name, args, thread) {
      var evt = document.createEvent('Event');
      evt.initEvent(name, true, true);
      evt.thread = thread || this.model;
      evt.model = this.model;
      var root = oldEvent;
      if (root && root.originalEvent) {
        root = root.originalEvent;
      }
      if (root && root.rootEvent) {
        root = root.rootEvent;
      }
      evt.rootEvent = root;
      evt.view = this;
      evt.args = args;
      if (this.el) {
        this.el.dispatchEvent(evt);
      }
    }
  });

});
