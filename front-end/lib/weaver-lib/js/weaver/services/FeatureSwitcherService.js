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
  'weaver/utils/EventBus',
  'lodash',
  'weaver/features/Feature',
  'weaver/features/FeatureDisplayActionInSideMenu',
  'weaver/features/FeatureDisplayActionWithinSelectedItem'
], function (
  EventBus,
  _,
  Feature,
  FeatureDisplayActionInSideMenu,
  FeatureDisplayActionWithinSelectedItem
) {

  "use strict";

  /**
   *
   * FeatureSwitcherService.add(FeatureX.enable());
   * FeatureSwitcherService.add(FeatureY.enable());
   * FeatureSwitcherService.add(FeatureZ.disable());
   * FeatureSwitcherService.get(FeatureZ).enable();
   * FeatureSwitcherService.ping(FeatureY);
   *
   * @class FeatureSwitcherService
   * @namespace services
   * @module weaver
   * @submodule services
   */
  return (function() {

    var features = {};

    return {

      /**
       * Initialize the features in their default state
       */
      initializeFeatures: function() {
        this.add(FeatureDisplayActionInSideMenu);
        this.add(FeatureDisplayActionWithinSelectedItem.enable());
      },

      /**
       * Add a feature
       * @param feature
       */
      add: function (feature) {
        _.set(features, feature.name, feature);
      },

      /**
       * Get a feature by its name
       * @param name
       * @returns {Feature|undefined}
       */
      get: function (name) {
        if(_.has(features, name)) {
          return _.get(features, name);
        }
        return undefined;
      },

      /**
       * Ping the current state of a feature out onto the EventBus
       * @param feature
       */
      ping: function(feature) {
        if(_.has(features, feature.name)) {
          EventBus.trigger(EventBus.createEventName(['feature', 'ping', feature.name]), {enabled: feature.isEnabled()});
        }
      },

      /**
       * A string representation of all the registered features and their current state
       * @method toString
       * @returns {string}
       */
      toString: function() {
        return _.values(features)
          .map(function(feature) {return [
            feature.name,
            'is',
            feature.isEnabled() ? 'enabled' : 'disabled'
          ].join(' ');}, this)
          .join('\n')
          .trim();
      }
    };
  })();
});
  