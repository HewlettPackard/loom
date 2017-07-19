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

  var BaseView = require('../../views/BaseView');

  /**
   * ThreadMonitorController is responsible for maintaining the Tapestry it manages synced with the state of the screen
   *
   * todo: possibly move this (and other) controllers into a controller namespace. I don't know if it makes sense to be here
   * @backbone no-initialize
   * @class ThreadMonitorController
   * @module weaver
   * @submodule screens.TapestryScreen
   * @namespace  screens.TapestryScreen
   * @constructor
   */
  var ThreadMonitorController = BaseView.extend({

    constructorName: 'LOOM_ThreadMonitorController',

    events: {
      'didDisplayThread': 'addThread',
      'didRemoveThread': 'removeThread'
    },

    /**
     * Called when a thread has been added to the {{#crossLink "models.Tapestry"}}{{/crossLink}}
     * @method addThread
     * @param event
     */
    addThread: function (event) {
      this.model.add(event.originalEvent.thread);
    },

    /**
     * Called when a thread has been removed from the {{#crossLink "models.Tapestry"}}{{/crossLink}}
     * @method removeThread
     * @param event
     */
    removeThread: function (event) {
      this.model.remove(event.originalEvent.thread);
    }
  });

  return ThreadMonitorController;
});
