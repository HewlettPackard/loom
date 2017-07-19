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
define(function() {
  "use strict";

  var BraidingController = require('weft/models/Tapestry/BraidingController');
  var FeatureSwitcherService = require('weaver/services/FeatureSwitcherService');
  var FiberSelectionService = require('weaver/services/FiberSelectionService');
  var ThreadSelectionService = require('weaver/services/ThreadSelectionService');
  var FiberTooltipService = require('weaver/services/FiberTooltipService');
  var FurtherRelationshipFilterService = require('weaver/services/FurtherRelationshipFilterService');
  var LockingManager = require('weft/services/LockingManager');
  var NotificationService = require('weaver/services/NotificationService');
  var PatternInTapestryMarker = require('weft/services/PatternInTapestryMarker');
  var PrimaryFilterService = require('weaver/services/PrimaryFilterService');
  var ProviderHighlightService = require('weft/services/ProviderHighlightService');
  var ProvidersLegendService = require('weft/services/ProvidersLegendService');
  var RelationshipHighlightFilterService = require('weaver/services/RelationshipHighlightFilterService');
  var RelationshipSelectedFilterService = require('weaver/services/RelationshipSelectedFilterService');
  var RelationTypeUpdater = require('weft/services/RelationTypeUpdater');
  var StatusLoader = require('weft/services/StatusLoader');
  var ThreadAvailabilityMonitor = require('weft/services/ThreadAvailabilityMonitor');
  var ThreadMonitorService = require('weft/services/ThreadMonitorService');


  /**
   * The tapestry services controller is responsible for bringing up and registering the required services. It also
   * safely destroys them and unregisters them when appropriate
   * @class TapestryServicesController
   */
  var TapestryServicesController = {

    /**
     * Start all of the services required by the tapestry screen
     * @param serviceManager
     * @param tapestry
     */
    start: function(serviceManager, tapestry) {
      serviceManager.register('PrimaryFilterService', this._createPrimaryFilterService());
      serviceManager.register('FeatureSwitcherService', FeatureSwitcherService); // singleton, no new required..
      serviceManager.register('FiberSelectionService', this._createFiberSelectionService());
      serviceManager.register('ThreadSelectionService', this._createThreadSelectionService());
      serviceManager.register('FiberTooltipService', this._createFiberTooltipService(tapestry));
      serviceManager.register('RelationshipSelectedFilterService', this._createRelationshipSelectedFilterService());
      serviceManager.register('RelationshipHighlightFilterService', this._createRelationshipHighlightFilterService());
      serviceManager.register('NotificationService', this._createNotificationService(tapestry));
      serviceManager.register('ProviderHighlightService', this._createProviderHighlightService(tapestry));
      serviceManager.register('ProvidersLegendService', this._createProvidersLegendService(serviceManager));
      serviceManager.register('StatusLoader', this._createStatusLoader(serviceManager));
      serviceManager.register('PatternInTapestryMarker', this._createPatternInTapestryMarker(serviceManager, tapestry));
      serviceManager.register('ThreadMonitorService', this._createThreadMonitorService(tapestry));
      serviceManager.register('LockingManager', this._createLockingManager(tapestry, serviceManager));
      serviceManager.register('BraidingController', this._createBraidingController());
      serviceManager.register('RelationTypeUpdater', this._createRelationTypeUpdater(tapestry));
      serviceManager.register('ThreadAvailabilityMonitor', this._createThreadAvailabilityMonitor(tapestry, serviceManager));
      serviceManager.register('FurtherRelationshipFilterService', this._createFurtherRelationshipFilterService(serviceManager));
    },

    /**
     * Stop the services required by the tapestry screen
     * @param serviceManager
     */
    stop: function(serviceManager) {
      serviceManager.stopListening('FiberSelectionService');
      serviceManager.stopListening('FiberTooltipService');
      serviceManager.stopListening('FurtherRelationshipFilterService');
      serviceManager.stopListening('NotificationService');
      serviceManager.stopListening('PrimaryFilterService');
      serviceManager.stopListening('RelationshipSelectedFilterService');
      serviceManager.stopListening('RelationshipHighlightFilterService');
      serviceManager.stopListening('ProviderHighlightService');
      serviceManager.stopListening('StatusLoader');
      serviceManager.stopListening('RelationTypeUpdater');
      serviceManager.get('ThreadMonitorService').get('queryResults').set([]);
      serviceManager.stopListening('ThreadAvailabilityMonitor');
      serviceManager.stopListening('ThreadMonitorService');
      serviceManager.stopListening('ThreadSelectionService');
    },

    /**
     * Deregister the services required by the tapestry screen
     * @param serviceManager
     */
    deregister: function(serviceManager) {
      serviceManager.deregister('PrimaryFilterService');
      serviceManager.deregister('FeatureSwitcherService');
      serviceManager.deregister('FiberSelectionService');
      serviceManager.deregister('FiberTooltipService');
      serviceManager.deregister('FurtherRelationshipFilterService');
      serviceManager.deregister('RelationshipSelectedFilterService');
      serviceManager.deregister('RelationshipHighlightFilterService');
      serviceManager.deregister('NotificationService');
      serviceManager.deregister('ProviderHighlightService');
      serviceManager.deregister('ProvidersLegendService');
      serviceManager.deregister('StatusLoader');
      serviceManager.deregister('ThreadSelectionService');
      serviceManager.deregister('PatternInTapestryMarker');
      serviceManager.deregister('LockingManager');
      serviceManager.deregister('ThreadMonitorService');
      serviceManager.deregister('BraidingController');
      serviceManager.deregister('RelationTypeUpdater');
      serviceManager.deregister('ThreadAvailabilityMonitor');
    },

    _createFurtherRelationshipFilterService: function (serviceManager) {
      return new FurtherRelationshipFilterService({
        'AggregatorClient': serviceManager.get('AggregatorClient'),
        'RelationshipSelectedFilterService': serviceManager.get('RelationshipSelectedFilterService'),
        'RelationshipHighlightFilterService': serviceManager.get('RelationshipHighlightFilterService')
      });
    },

    _createNotificationService: function (tapestry) {
      return new NotificationService(tapestry);
    },



    _createFiberSelectionService: function () {
      return new FiberSelectionService();
    },

    _createThreadSelectionService: function () {
      return new ThreadSelectionService();
    },

    _createFiberTooltipService: function (tapestry) {
      return new FiberTooltipService(tapestry);
    },

    _createProviderHighlightService: function (tapestry) {
      return new ProviderHighlightService(
        tapestry.model.get('fibersIndex').get('index')
      );
    },

    _createProvidersLegendService: function (serviceManager) {
      return new ProvidersLegendService({
        providers: serviceManager.get('AggregatorClient').get('loggedInProviders')
      });
    },

    _createStatusLoader: function (serviceManager) {
      return new StatusLoader({
        aggregator: serviceManager.get('AggregatorClient')
      });
    },

    _createPatternInTapestryMarker: function (serviceManager, tapestry) {
      return new PatternInTapestryMarker({
        patterns: serviceManager.get('AggregatorClient').get('availablePatterns'),
        tapestry: tapestry.model
      });
    },

    _createThreadMonitorService: function (tapestry) {
      return new ThreadMonitorService({
        queryResults: tapestry.model.get('sharedQueryResults').get('results'),
        fibersIndex: tapestry.model.get('fibersIndex').get('index')
      });
    },

    _createLockingManager: function (tapestry, serviceManager) {
      return new LockingManager({
        tapestry: tapestry.model,
        aggregator: serviceManager.get('AggregatorClient'),
        threadMonitor: serviceManager.get('ThreadMonitorService')
      });
    },

    _createPrimaryFilterService: function () {
      return new PrimaryFilterService();
    },

    _createRelationshipSelectedFilterService: function (serviceManager) {
      return new RelationshipSelectedFilterService(serviceManager);
    },

    _createRelationshipHighlightFilterService: function () {
      return new RelationshipHighlightFilterService();
    },

    _createBraidingController: function () {
      return new BraidingController();
    },

    _createRelationTypeUpdater: function (tapestry) {
      return new RelationTypeUpdater(tapestry.model);
    },

    _createThreadAvailabilityMonitor: function (tapestry, serviceManager) {
      return new ThreadAvailabilityMonitor(
        tapestry.model.get('threads'),
        serviceManager.get('AggregatorClient').get('availableItemTypes')
      );
    }

  };

  return TapestryServicesController;
});
