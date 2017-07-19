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

  var Backbone = require('backbone');
  var Operation = require('weft/models/Operation');

  /**
   * A BraidingController enforce a given value for braiding
   * in the Queries of the Threads in the list it manages
   * 
   * @class BraidingController
   * @namespace models.tapestry
   * @module weft
   * @submodule models.tapestry
   * @constructor
   * @param {Number}              options.braiding  The braiding value
   * @param {Backbone.Collection} options.threads   The list of Threads controlled by the BraidingController
   * @extends Backbone.Model
   */
  var BraidingController = Backbone.Model.extend({

    defaults: function () {
      return {
        /**
         * The braiding value
         * @type {Number}
         * @attribute  braiding
         */
        braiding: 45,

        /**
         * The list of Threads
         * @type {Backbone.Collection}
         * @attribute  threads
         */
        threads: new Backbone.Collection()
      };
    },

    initialize: function () {
      this.listenTo(this, 'change:braiding', this.updateAllThreadsBraiding);
      this.listenTo(this.get('threads'), 'add', this.updateThreadBraiding);
      this.updateAllThreadsBraiding(this);
    },

    /**
     * Updates the braiding value for a thread
     * @method updateThreadBraiding
     * @param thread
     */
    updateThreadBraiding: function (thread) {
      thread.limitWith({
        operator: Operation.BRAID_ID,
        parameters: {
          maxFibres: this.get('braiding')
        }
      });
    },

    /**
     * Update the braiding value on all threads. Called when self.braiding changes
     * @method updateAllThreadsBraiding
     * @param controller
     */
    updateAllThreadsBraiding: function (controller) {
      var self = this;
      controller.get('threads').forEach(function (thread) {
        self.updateThreadBraiding(thread);
      });
    }
  });

  return BraidingController;
});
