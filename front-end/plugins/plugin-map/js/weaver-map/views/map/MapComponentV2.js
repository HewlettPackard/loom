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
  var MapEvent = require('./MapEventV2');

  /**
   * (Implemenatation details of MapComponent)
   * Impose a throttle limit on the dispatchInstantEvent function.
   */
  var Dispatcher = function (mapComponent, name, timeout) {
    this.name = name;
    this.mapComponent = mapComponent;
    this.throttleFunc = _.throttle(_.bind(this._dispatchThrottled, this), timeout || 20);
  };

  Dispatcher.prototype.dispatch = function (svgElement, args) {
    this.svgElement = svgElement;
    this.args = args;
    this.throttleFunc();
  };

  Dispatcher.prototype._dispatchThrottled = function () {
    this.mapComponent.dispatchInstantEvent(this.svgElement, this.name, this.args);
  };

  /**
   * Class to easily add map component.
   * All sub class have a field 'map' to access the map they're attached to.
   * @param {Object} options list of arguments you can pass to your initialize function
   *                         plus you can specify here the 'map' the object should be attached too.
   */
  var MapComponent = MapEvent.extend({

    initialize: function () {
      this.__throttledEvents = {};
    },

    /**
     * This function is called whenever the component is attached to a new map.
     * When the map is provided at the object creation, the function is called
     * right after the initialize method.
     */
    initializeWhenAttached: _.noop,

    /**
     * Dispatch an event inside the DOM. The event is controlled with a throttle
     * to avoid firing the event too often. The throttling limit is 20 ms.
     * If you don't want this behavior see dispatchInstantEvent.
     * @param  {Object} svgElement Can be either a DOM element or a d3 selection
     * @param  {String} name       Name of the event to trigger.
     * @param  {Object} args       Any data that you want to pass to handlers.
     * @param  {Integer} timeout   Throttle limit.
     */
    dispatchEvent: function (svgElement, name, args, timeout) {
      var dispatcher = this.__throttledEvents[name];
      if (!dispatcher) {
        dispatcher = this.__throttledEvents[name] = new Dispatcher(this, name, timeout);
      }
      dispatcher.dispatch(svgElement, args);
    },

    /**
     * Dispatch intantly an event inside the DOM.
     * @param  {Object} svgElement Can be either a dom element or a d3 selection.
     * @param  {String} name       Name of the event to trigger.
     * @param  {Object} args       Any data that you want to pass to handlers.
     */
    dispatchInstantEvent: function (svgElement, name, args) {
      var evt = document.createEvent('Event');
      evt.initEvent(name, true, true);
      evt.source = this;
      evt.args = args;
      if (svgElement.dispatchEvent) {
        svgElement.dispatchEvent(evt);
      }
      // If it is a d3 selection, then all elements dispatch the event.
      else if (svgElement.datum) {
        svgElement.each(function () {
          this.dispatchEvent(evt);
        });
      }
    },
  });

  return MapComponent;
});