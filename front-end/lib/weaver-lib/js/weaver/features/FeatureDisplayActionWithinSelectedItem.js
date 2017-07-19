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
define(['lodash', 'weaver/features/Feature', 'weaver/utils/EventBus'], function (_, Feature, EventBus) {
  "use strict";

  /**
   * Enables the action view in the side menu
   * @class FeatureDisplayActionWithinSelectedItem
   * @namespace features
   * @module  weaver
   * @submodule features
   * @extends Feature
   */
  return _.extend(new Feature(), {
    name: 'display-action-within-selected-item',
    onEnable: function () {
      this.listenTo(EventBus, 'fiber:action:display', function (event) {
        EventBus.trigger(this.name, event);
      });
    }
  });
});
