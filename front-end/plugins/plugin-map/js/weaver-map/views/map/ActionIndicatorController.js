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

  var MapEvent = require('./MapEventV2');
  var ActionIndicatorView = require('./ActionIndicatorView');

  var ActionIndicatorController = MapEvent.extend({

    // We need to fetch 6 state
    //    * Can we zoom in
    //    * Can we zoom out
    //    * Can we pan left
    //    * Can we pan right
    //    * Can we pan up
    //    * Can we pan down
    // 
    // Those event need to be trigger by the ZoomPanController.
    // Or we can store the scale and compare it with the maximum/minimum scale possible
    // and we can store the translation and look if something has changed in one direction.
    // (Actually this wouldn't work as you can pan only in one direction if you want)
    events: {
      'change:panup' : '_forwardToView',
      'change:pandown': '_forwardToView',
      'change:panleft': '_forwardToView',
      'change:panright': '_forwardToView',
      'change:zoomminus': '_forwardToView',
      'change:zoomplus': '_forwardToView',
    },

    initializeWhenAttached: function () {
      this.view = new ActionIndicatorView();
      this.map.$el.append(this.view.$el);
    },

    removeFromMap: function () {
      this.view.remove();
    },

    _forwardToView: function (event) {
      this.view.events[event.type].call(this.view, event.originalEvent.args);
    },
  });

  return ActionIndicatorController;
});