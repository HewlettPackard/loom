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

  var FilterService = require('weft/services/FilterService');
  var EventBus = require('weaver/utils/EventBus');

  /**
   * ThreadSelectionService is the main service that handles thread selection.
   * @class ThreadSelectionService
   * @namespace services
   * @module weaver
   * @submodule services
   * @extends Backbone.Model
   */
  var ThreadSelectionService = FilterService.extend({

    constructorName: 'LOOM_ThreadSelectionService',

    /**
     * @property selectedThread
     */
    selectedThread: null,

    selectedThreadEvent: {
      fiber: null,
      fiberView: null,
      thread: null
    },

    /**
     * Global event bus for inter module communication
     * @type Backbone.Events
     */
    EventBus: EventBus,

    initialize: function () {
      FilterService.prototype.initialize.apply(this, arguments);
      this.listenTo(this.EventBus, 'thread:click', this.onThreadClick);
      this.listenTo(this.EventBus, 'thread:close thread:list:remove', function() {
        this._unselectThread(this.selectedThreadEvent);
      });
    },

    onThreadClick: function (event) {
      if (event.threadView === this.selectedThread) {
        this._unselectThread(event);
      } else {
        // unselect the current item before selecting the new one
        if (this.selectedThread !== null) {
          this._unselectThread(this.selectedThreadEvent);
        }
        this.selectedThread = event.threadView;
        this.selectedThreadEvent = event;  
        this.EventBus.trigger('thread:selected', event);
      }
    },

    _unselectThread: function(event) {
      this.selectedThread = null;
      this.EventBus.trigger('thread:unselected', event);
    }

  });

  return ThreadSelectionService;
});
