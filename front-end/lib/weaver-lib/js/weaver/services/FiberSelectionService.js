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
   * FiberSelectionService is the main service that handles fiber selection.
   * @class FiberSelectionService
   * @namespace services
   * @module weaver
   * @submodule services
   * @extends Backbone.Model
   */
  var FiberSelectionService = FilterService.extend({

    constructorName: 'LOOM_FiberSelectionService',

    /**
     * @property selectedFiber
     */
    selectedFiber: null,

    selectedFiberEvent: {
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
      this.listenTo(this.EventBus, 'fiber:click', this.onFiberClick);
      this.listenTo(this.EventBus, 'element:will:remove', function(event) {
        if (this.selectedFiberEvent && (event.view === this.selectedFiberEvent.fiberView)) {
          this._unselectFiber(this.selectedFiberEvent);
        }
      });
    },

    onFiberClick: function (event) {
      if (event.fiberView === this.selectedFiber) {
        this._unselectFiber(event);
      } else {
        // unselect the current item before selecting the new one
        if (this.selectedFiber !== null) {
          this._unselectFiber(this.selectedFiberEvent);
        }
        this.selectedFiber = event.fiberView;
        this._enableWatchModelAlertChanges();
        this.selectedFiberEvent = event;
        this.EventBus.trigger('fiber:selected', event);
      }
    },

    _unselectFiber: function(event) {
      this._disableWatchModelAlertChanges();
      this.selectedFiber = null;
      this.EventBus.trigger('fiber:unselected', event);
    },

    /**
     * Utility function to send out alert change notifications
     * @private
     */
    _notifyFiberAlertChange: function() {
      this.EventBus.trigger('fiber:selected:alert:change', this.selectedFiberEvent);
    },

    /**
     * Utility function to enable watching model alerts
     * @private
     */
    _enableWatchModelAlertChanges: function() {
      if (this.selectedFiber && this.selectedFiber.model && this.selectedFiber.model.alert) {
        this.listenTo(this.selectedFiber.model.alert, 'change:count', this._notifyFiberAlertChange);
        this.listenTo(this.selectedFiber.model.alert, 'change:description', this._notifyFiberAlertChange);
        this.listenTo(this.selectedFiber.model.alert, 'change:level', this._notifyFiberAlertChange);
      }
    },

    /**
     * Utility function to disable watching model alerts
     * @private
     */
    _disableWatchModelAlertChanges: function() {
      if (this.selectedFiber && this.selectedFiber.model && this.selectedFiber.model.alert) {
        this.stopListening(this.selectedFiber.model.alert);
      }
    }
  });

  return FiberSelectionService;
});
