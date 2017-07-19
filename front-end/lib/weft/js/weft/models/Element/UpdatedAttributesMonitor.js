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
   * UpdatedAttributesMonitor is a helper keeping track of the attributes
   * that have changed on the element it monitors
   *
   * @class UpdatedAttributesMonitor
   * @param {models.Element} element The monitored element
   * @namespace models.element
   * @module weft
   * @submodule models.element
   */
  var UpdatedAttributesMonitor = Backbone.Model.extend({

    defaults: function () {
      return {
        /**
         * The element monitored by this UpdatedAttributesMonitor
         * @property {models.Element} element
         */

        /**
         * The list of attributes that have been updated
         * @property {Array} updatedAttributes
         */
        updatedAttributes: []
      };
    },

    initialize: function () {
      // monitor the element change events
      this.get('element').on('change', this._updateAttributesList, this);

      /**
       * didSetState is triggered in Element._doSetState() when a state changes.
       * State represents
       */
      this.get('element').on('didSetState', this._clearAttributesList, this);
    },

    _updateAttributesList: function () {

      var currentAttributes = this.get('updatedAttributes');
      var newAttributes = this.get('element').getDisplayablePropertiesThatHasChanged();

      // Avoid unnecessary event triggers as union will create a new array every time
      if (newAttributes.length) {
        this.set('updatedAttributes', _.union(currentAttributes, newAttributes));
      }
    },

    _clearAttributesList: function (state) {
      if (state !== 'updated' && state !== 'nestedStateChanges') {
        this.set('updatedAttributes', []);
      }
    }
  });

  return UpdatedAttributesMonitor;
});
