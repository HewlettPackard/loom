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
define([
  'backbone'
], function (Backbone) {

  "use strict";

  /**
   * Base class for experimental features.
   * @class ExperimentalFeature
   * @namespace models.experimentalFeatures
   * @module weaver
   * @submodule models.experimentalFeatures
   * @constructor
   * @extends Backbone.Model
   */
  var ExperimentalFeature = Backbone.Model.extend({

    constructorName: 'LOOM_ExperimentalFeature',

    defaults: {

      /**
       * Unique identifier for the feature
       * @property id
       * @type {String}
       */

      /**
       * Human readable name for the feature
       * @property name
       * @type {String}
       */

      /**
       * Flag showing if the feature is enabled
       * @property enabled
       * @type {Boolean}
       * @default false
       */
      enabled: false
    },

    /**
     * Enables the feature
     * @method enable
     */
    enable: function () {
      this.set('enabled', true);
    },

    /**
     * Disables the feature
     * @method disable
     */
    disable: function () {
      this.set('enabled', false);
    },

    /**
     * Enables the feature if it is disabled, and vice-versa
     * @method toggle
     */
    toggle: function () {
      if (this.get('enabled')) {
        this.disable();
      } else {
        this.enable();
      }
    }
  });

  return ExperimentalFeature;
});
