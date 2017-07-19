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
  var FilterService = require('weft/services/FilterService');
  var EventBus = require('weaver/utils/EventBus');

  /**
   * FurtherRelationshipFilterService handles which further related elements are currently highlighted.
   * @class FurtherRelationshipFilterService
   * @namespace services
   * @module weaver
   * @submodule services
   * @extends Backbone.Model
   */
  var FurtherRelationshipFilterService = FilterService.extend({

    constructorName: 'LOOM_FurtherRelationshipFilterService',

    /**
     * Global event bus for inter module communication
     * @type Backbone.Events
     */
    EventBus: EventBus,

    selectedElement: null,

    defaults: function () {
      return _.defaults({

        /**
         * The flag that will be set on elements part of the filter.
         * Set to undefined if you don't want these elements to be flagged.
         * @property filterFlag
         * @type {string}
         */
        filterFlag: 'selected',

        /**
         * The flag that will be set on elements matching the filter
         * @property filteredElementsFlag
         * @type {string}
         */
        filteredElementsFlag: 'furtherRelated',

        /**
         * A flag setting if the service is active or not
         * @property active
         * @type {boolean}
         */
        active: true
      }, FilterService.prototype.defaults());
    },

    initialize: function (options) {
      FilterService.prototype.initialize.apply(this, arguments);
      this.options = options;

//
// TEMPORARILY DISABLED WHILE WE FIND A SOLUTION TO THE MEMORY LEAK
//

      // this._fiberCustomReselect = _.throttle(_.bind(this._fiberCustomReselect, this), 250, {leading: true});
      // this.listenTo(this.EventBus, 'willRemoveElement', function (event) {
      //   if (event.view.model.get('selected')) {
      //     this._unselectElement(event.fiberView);
      //   }
      // });
      // this.listenTo(this.EventBus, 'fiber:selected', function (event) {
      //   if (this.selectedElement) {
      //     this._unselectElement();
      //   }
      //   this._selectElement(event.fiberView);
      // });
      // this.listenTo(this.EventBus, 'fiber:unselected', function () {
      //   this._unselectElement();
      // });
      // this.listenTo(this.EventBus, 'fiber:custom:reselect', function (event) {
      //   this._fiberCustomReselect(event);
      // });
    },

    /**
     * Utility function to provide an _.debounce wrapper for 'fiber:custom:reselect'
     * @param event
     * @private
     */
    _fiberCustomReselect: function(event) {
      this._unselectElement();
      this._selectElement(event.fiberView);
    },

    /**
     * Unselect the selected element
     * @private
     */
    _unselectElement: function() {
      this.setFilter([]);
      var aggregatorClient = this.options.AggregatorClient;
      aggregatorClient.set('selectedFiber', undefined);
      if (aggregatorClient.get('bogyFiber')) {
        aggregatorClient.get('bogyFiber').set('l.relations', aggregatorClient.get('bogyFiberRelations'));
      }
      aggregatorClient.set('bogyFiber', undefined);
      aggregatorClient.set('bogyFiberRelations', undefined);
      this.selectedElement = null;
    },

    /**
     * Associate the selected element
     *
     * @param fiberView
     * @private
     */
    _selectElement: function(fiberView) {
      var fiber = fiberView.model;
      var furtherRelations = this.calcFurtherRelations(fiberView);
      var furtherRelation = furtherRelations.length > 0 ? furtherRelations[0] : null;
      var aggregatorClient = this.options.AggregatorClient;

      if (furtherRelation && furtherRelations.length > 1) {
        aggregatorClient.set('bogyFiber', furtherRelation);
        aggregatorClient.set('bogyFiberRelations', furtherRelation.get('l.relations'));
        for (var j = 1; j < furtherRelations.length; j++) {
          furtherRelation.set('l.relations', _.union(furtherRelation.get('l.relations'), furtherRelations[j].get('l.relations')));
        }
        aggregatorClient.set('bogyFiber', furtherRelation);
      }

      this.options.RelationshipSelectedFilterService.setFilter([ fiber ]);
      aggregatorClient.set('selectedFiber', fiberView);
      var furtherRelationFilter = furtherRelation ? [furtherRelation] : [];
      this.setFilter(furtherRelationFilter);
      this.options.RelationshipHighlightFilterService.setFilter([ fiber ]);
      this.selectedElement = fiberView;
    },

    calcFurtherRelations: function (fiberView) {
      var fiber = fiberView.model;
      var lr = fiber.get('l.relations');
      var r = fiber.get('relations');

      var furtherRelations = [];

      for (var i = 0; i < lr.length; i++) {
        var R = r[lr[i]];

        if (R === undefined) {
          // todo: hui, we should see why we sometimes get an undefined item in the list.
          console.log("Misshaped R in calcFurtherRelations: ");
          console.log(fiber);
          continue;
        }

        switch (fiber.itemType.id) {
          case 'tm-shelf':
            switch (R.itemType.id) {
              case 'tm-soc':
                furtherRelations.push(R);
                break;
              case 'tm-memoryboard':
                furtherRelations.push(R);
                break;
            }
            break;
          // TODO: for books, we cannot do furthe relations - possibly to .push(R) for shelves?
          // case 'tm-interleave_group':
          //   switch (R.itemType.id) {
          //     case 'tm-memoryboard':
          //       furtherRelations.push(R);
          //       break;
          //   }
          //   break;
          case 'tm-instance':
          case 'tm-rack':
          case 'tm-enclosure':
          case 'tm-node':
          case 'tm-fabric_switch':
            switch (R.itemType.id) {
              case 'tm-soc':
              case 'tm-memoryboard':
                furtherRelations.push(R);
                break;
            }
            break;
        }
      }
      return furtherRelations;
    }

  });

  return FurtherRelationshipFilterService;
});
