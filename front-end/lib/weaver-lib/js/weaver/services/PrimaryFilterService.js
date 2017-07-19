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
  var RelationshipHighlightFilterService = require('weaver/services/RelationshipHighlightFilterService');
  var EventBus = require('weaver/utils/EventBus');

  /**
   * PrimaryFilterService is the main filter service in weaver. Its purpose is to expose an EventBus in FilterService
   * and to react to relationType changes
   * @class PrimaryFilterService
   * @namespace services
   * @module weaver
   * @submodule services
   * @extends Backbone.Model
   */
  var PrimaryFilterService = FilterService.extend({

    constructorName: 'LOOM_PrimaryFilterService',

    /**
     * Global event bus for inter module communication
     * @type Backbone.Events
     */
    EventBus: EventBus,

    initialize: function () {
      FilterService.prototype.initialize.apply(this, arguments);
      this.listenTo(this.EventBus, RelationshipHighlightFilterService.RelationTypeChangeEvent, function (relationType) {
        this.set('relationType', relationType);
      });
    }
  });

  return PrimaryFilterService;
});
