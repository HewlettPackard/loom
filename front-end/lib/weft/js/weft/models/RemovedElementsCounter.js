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
  var Backbone = require('backbone');

  /**
   * A counter for elements that get removed from a Thread.
   * It gets automatically decreased after a certain duration
   * @class RemovedElementsCounter
   * @namespace models
   * @module weft
   * @constructor
   */
  var RemovedElementsCounter = Backbone.Model.extend({

    constructorName: 'LOOM_RemovedElementsCounter',

    // PROPERTIES
    defaults: {

      /**
       * The Thread this counter counts the removed elements
       * @property propertyName
       * @type {propertyType}
       * @default defaultValue
       */
      thread: undefined,

      /**
       * The number of removed elements
       * @property numberOfRemovedElements
       * @type {Number}
       * @default 0
       */
      numberOfRemovedElements: 0
    },

    // CONSTRUCTOR
    initialize: function () {

      this._counterId = 0;

      if (this.get('thread')) {

        // TODO: Make this behaviour external and bind it to the Thread's polling
        // Maybe in a `Thread.refresh()` method that would trigger `refresh` events
        this.listenTo(this.get('thread'), 'reset:elements', function () {

          if (this._ignore) {
            this._ignore = false;
          } else {
            var removed = arguments[1].delta.removed;
            this.increment(removed.length, this.get('thread').getStateChangeTimeout('deleted') || 5000);
          }
        }, this);

        this.listenTo(this.get('thread'), 'change:sortedBy', function () {
          this._ignore = true;
          this.reset();
        }, this);

        this.listenTo(this.get('thread'), 'change:query', function () {
          this._ignore = true;
          this.reset();
        }, this);
      }
    },

    // PUBLIC API
    /**
     * Increments the counter by given count for given duration
     * @method increment
     * @param count {Number}
     * @param duration {Number}
     */
    increment: function (count, duration) {

      this.set('numberOfRemovedElements', this.get('numberOfRemovedElements') + count);
      var currentCounterId = this._counterId;
      setTimeout(_.bind(function () {
        if (currentCounterId === this._counterId) {
          this.set('numberOfRemovedElements', this.get('numberOfRemovedElements') - count);
        }
      }, this), duration);
    },

    /**
     * Resets the counter to 0
     * @method reset
     */
    reset: function () {
      this.set('numberOfRemovedElements', 0);
      this._counterId++;
    }
  });

  return RemovedElementsCounter;
});
