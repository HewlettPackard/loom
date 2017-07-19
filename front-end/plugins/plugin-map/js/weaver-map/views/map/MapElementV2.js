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
  var d3 = require('d3');
  var CustomEventDispatcher = require('weaver/views/CustomEventDispatcher');

  var MapElement = CustomEventDispatcher.extend({

    constructorName: "LOOM_MapElement",

    events: {
      'tapone': function (event, ev) {
        ev.originalEvent.preventDefault();
        d3.event = event;
        if (!_.isUndefined(this.model)) {
          if (!this.model.get('selected')) {
            this.selectElement(event);
          } else {
            this.unselectElement(event);
          }
        }
      },
    },

    initialize: function (options) {

      this.d3map = options.d3map;
      this.thread = options.thread;
    },

    selectElement: function (event) {
      // if (!this.model.get('selected')) {
      this.__dispatchEvent(event, 'willSelectElement', this.model);
      this.__dispatchEvent(event, 'didSelectElement', this.model);
      this.__dispatchEvent(event, 'action:selectElement', this.model);

      this.isSelected = true;
      this.thread.set('focus', true);
      this.__dispatchEvent(event, 'element:show-details', true, this.thread);
      // }
    },

    unselectElement: function (event) {
      this.__dispatchEvent(event, 'action:unselectElement');
      this.__dispatchEvent(event, 'didUnselectElement');

      this.isSelected = false;
      this.thread.set('focus', true);
      this.__dispatchEvent(event, 'element:show-details', false, this.thread);
    },

    _updateRelatedState: function (related) {
      if (related) {
        this._addClass('is-related');
      } else {
        this._removeClass('is-related');
      }
    },

    _updateFilterState: function (match) {

      if (match) {
        this._addClass('is-matchingFilter');
      } else {
        this._removeClass('is-matchingFilter');
      }
    },

    _updateSelectedState: function (selected) {
      if (selected) {
        this._addClass('is-related');
      } else {
        this._removeClass('is-related');
      }
    },

    _updateAlertState: function (level) {
      this._setClass('alertLevel', 'mas-alertNotification-' + level);
    },
  });

  return MapElement;
});
