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
define(['weaver/utils/EventBus', 'lodash'], function (EventBus, _) {

  "use strict";

  /**
   * Feature is a base class for all available features in the system.
   * Features can be enabled/disabled and when enabled they listen to and transform events
   * It's a very simple system whereby responsibility is taken away from the UI code, click events etc and sent out
   * on to the EventBus as messages. Features listen to these messages (or not if disabled) and then send out other
   * messages which the app will respond too.
   *
   * Each UI element will now be a self contained sandbox where communication is done through input and output messages.
   * This allows us to get in between cause an effect, and change the reaction.
   *
   * Features can be VERY simple. The idea is that we can dynamically change what happens, even when a button is clicked
   * There should be no JS code attached to it. The click simply sends a message that says a click happened. This is
   * how we separate UI from Logic.
   *
   * @type {{}}
   */

  return function() {

    /**
     * @private
     * @type {boolean}
     */
    var enabled = false;

    return _.extend({

      /**
       * The unique name of the feature
       * @property name
       */
      name: 'you-must-name-your-feature',

      /**
       * Enable this feature
       * @method enable
       */
      enable: function () {
        enabled = true;
        EventBus.trigger(EventBus.createEventName(['feature', 'enabled', this.name]));
        this.onEnable();
        return this;
      },

      /**
       * Disable this feature
       * @method disable
       */
      disable: function () {
        enabled = false;
        EventBus.trigger(EventBus.createEventName(['feature', 'disabled', this.name]));
        this.stopListening();
        this.onDisable();
        return this;
      },

      /**
       * @method isEnabled
       * @returns {Boolean}
       */
      isEnabled: function() {
        return enabled === true;
      },

      /**
       * Called when the feature is enabled. Override in child classes to provide functionality
       * @method onEnable
       */
      onEnable: function() {},

      /**
       * Called when the feature is disabled. Override in child classes to provide functionality
       * @method onDisable
       */
      onDisable: function() {}
    }, EventBus);
  };
});
