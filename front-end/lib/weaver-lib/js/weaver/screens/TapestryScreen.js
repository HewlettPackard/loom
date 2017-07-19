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

  /** @type BaseView */
  var BaseView = require('../views/BaseView');
  var ThreadListView = require('weaver/views/ThreadList/ThreadListView');
  var ToolbarView = require('weaver/views/Toolbar/ToolbarView');
  var NotificationView = require('weaver/views/Notifications/NotificationView');
  var PatternController = require('./../views/SideMenu/Tapestry/Patterns/PatternController');
  var ProvidersReloader = require('weft/services/ProvidersReloader');
  var ActionsAndMenuDisablingController = require('./TapestryScreen/ActionsAndMenuDisablingController');
  var ThreadMonitorController = require('./TapestryScreen/ThreadMonitorController');
  var BraidingUpdater = require('./TapestryScreen/BraidingUpdater');
  var DefaultPatternLoader = require('./TapestryScreen/DefaultPatternLoader');
  var OperationEditionController = require('./TapestryScreen/OperationEditionController');
  var SideMenuController = require('./../views/SideMenu/SideMenuController');
  var SideMenuLayoutView = require('weaver/views/SideMenuLayoutView');
  var MassRelationFilterController = require('weaver/views/ThreadList/ThreadListView/MassRelationFilterController');
  var ThreadActionMenuController = require('weaver/views/actions/ThreadActionMenuController');
  
  /**
   * TapestryScreen brings the whole application together, it is the primary view into loom.
   * @class TapestryScreen
   * @namespace  screens.TapestryScreen
   * @module weaver
   * @submodule screens.TapestryScreen
   * @constructor
   * @extends BaseView
   */
  var TapestryScreen = BaseView.extend({

    className: 'mas mas-tapestryScreen is-hidingLegends',

    events: {
      'click .mas-logout': 'logout',
      'click .mas-action--clearTapestry': function () {
        this.threadListView.clear();
      },
      'change:height .mas-toolbar': function (event) {
        this.threadListView.$el.css('height', 'calc(100% - ' + event.originalEvent.toolbarHeight + 'px)');
      },
      'didExpand .mas-providerSelectionMenu': function () {
        this.$el.removeClass('is-hidingLegends');
      },
      'didCollapse .mas-providerSelectionMenu': function () {
        this.$el.addClass('is-hidingLegends');
      }
    },

    createSideMenuController: function () {
      this.sideMenuController = new SideMenuController({
        el: this.el,
        model: this.sideMenuLayoutView,
        serviceManager: this.serviceManager,
        aggregatorClient: this.serviceManager.get('AggregatorClient'),
        relationshipService: this.serviceManager.get('RelationshipSelectedFilterService'),
        relationshipHighlightService: this.serviceManager.get('RelationshipHighlightFilterService'),
        relationTypeList: this.model.get('relationTypeList'),
        displayedThreads: this.threadListView.model
      });
    },

    /**
     * Create and initialize the side menu view
     * todo :: WARNING! THIS IS NOT TRUE! The side menu layout also renders the main thread list (via content option..)
     */
    createSideMenuLayoutView: function () {
      this.sideMenuLayoutView = new SideMenuLayoutView({
        serviceManager: this.serviceManager,
        content: this.threadListView
      });
    },

    /**
     * Create the thread list view
     */
    createThreadListView: function () {
      this.threadListView = new ThreadListView({
        filterService: this.serviceManager.get('PrimaryFilterService'),
        highlightService: this.serviceManager.get('RelationshipHighlightFilterService'),
        providerLegendService: this.serviceManager.get('ProvidersLegendService'),
        braidingController: this.serviceManager.get('BraidingController')
      });
    },

    /**
     * Create the thread action menu controller
     */
    createThreadActionMenuController: function () {
      this.threadActionMenuController = new ThreadActionMenuController({
        el: this.el,
        model: this.sideMenuLayoutView
      });
      this.listenTo(this.EventBus, 'display-action-in-side-menu', function(event) {
        this.threadActionMenuController.showMenu(event.action, event.element, true);
      });
    },

    /**
     * create the thread monitor controller
     */
    createThreadMonitorController: function () {
      this.ThreadMonitorController = new ThreadMonitorController({
        el: this.el,
        model: this.model
      });
    },

    /**
     * create the pattern controller
     */
    createPatternController: function () {
      this.patternController = new PatternController({
        el: this.el,
        model: this.threadListView.model,
        braidingController: this.serviceManager.get('BraidingController')
      });
    },

    /**
     * create the actions and menu disabling controller
     */
    createActionsAndMenuDisablingController: function () {
      this.actionsAndMenuDisablingController = new ActionsAndMenuDisablingController({
        el: this.el,
        model: this.serviceManager.get('StatusLoader')
      });
    },

    /**
     * create the mass relation filter controller
     */
    createMassRelationFilterController: function () {
      this.massRelationFilterController = new MassRelationFilterController({
        el: this.el
      });
    },

    /**
     * //todo: remove services passed
     * create the toolbar view
     */
    createToolbarView: function () {
      this.toolbarView = new ToolbarView({
        serviceManager: this.serviceManager,
        services: {
          relationshipHighlightService: this.serviceManager.get('RelationshipHighlightFilterService'),
          providerHighlightService: this.serviceManager.get('ProviderHighlightService')
        },
        sideMenu: this.sideMenuLayoutView
      });
      this.toolbarView.$el.addClass('mas-bottomToolbarLayout--toolbar');
    },

    createNotificationView: function() {
      this.notificationView = new NotificationView();
    },

    /**
     * create the braiding updater
     */
    createBraidingUpdater: function () {
      this.braidingUpdater = new BraidingUpdater({
        model: this.serviceManager.get('BraidingController'),
        toolbar: this.toolbarView
      });
    },

    /**
     * create the operation edition controller
     */
    createOperationEditionController: function () {
      this.operationEditionController = new OperationEditionController({
        el: this.el
      });
    },

    /**
     * create the default pattern loader
     */
    createDefaultPatternLoader: function () {
      this.defaultPatternLoader = new DefaultPatternLoader(
        this.serviceManager.get('AggregatorClient').get('providers'),
        this.threadListView.model,
        this.serviceManager.get('BraidingController'));
    },

    /**
     * create the providers reloader
     */
    createProvidersReloader: function () {
      this.providersReloader = new ProvidersReloader(this.serviceManager.get('StatusLoader').get('adapters'), this.serviceManager.get('AggregatorClient'));
    },

    /**
     * Hookup event listeners on services
     */
    hookupServiceEventListeners: function () {
      //todo: refactor this, the thread monitor should request the aggregator and monitor it
      this.serviceManager.get('AggregatorClient').get('providers').on(
        'change:loggedIn',
        this.serviceManager.get('ThreadMonitorService').pollAll,
        this.serviceManager.get('ThreadMonitorService')
      );
      this.listenTo(this.serviceManager.get('AggregatorClient'), 'change:hasLockedProviders', this._updateLockedState);
      this.listenTo(this.model, 'request', function () {
        this.serviceManager.get('ThreadMonitorService').abortAll();
      });
      this.listenTo(this.serviceManager.get('RelationshipHighlightFilterService'), 'change:active', function (service, active) {
        this.$el.toggleClass('is-highlightingRelationships', active);
      });
      this.listenTo(this.EventBus, 'willShowMenu', function (event) {
        if (event.controller === this.sideMenuController) {
          this.threadActionMenuController.hideMenu();
          return;
        }
        if (event.controller === this.threadActionMenuController) {
          this.sideMenuController.hideMenu();
        }
      });
    },

    /**
     * todo: Now that we have extracted the meaning into individual functions.. its obvious this has too many responsibilities
     * create a service manager that is responsible for bringing up the various services and safely tearing them down when required.
     * each service should become EventBus aware so they can communicate independently of the tapestry screen.
     */
    initialize: function (options) {
      this.serviceManager = options.ServiceManager;
      /** @type TapestryServicesController */
      this.servicesController = options.TapestryServicesController;
      BaseView.prototype.initialize.apply(this, arguments);
      this.servicesController.start(this.serviceManager, this);
      this.serviceManager.get('BraidingController').get('threads').set(this.model.get('threads').models);
      this.serviceManager.get('FeatureSwitcherService').initializeFeatures();

      this.createThreadListView();
      this.createSideMenuLayoutView();
      this.createSideMenuController();
      this.createThreadActionMenuController();
      this._displayInitialThreads();
      this.createThreadMonitorController();
      this.createPatternController();
      this.createActionsAndMenuDisablingController();
      this.createMassRelationFilterController();
      this.createToolbarView();
      this.createNotificationView();
      this.createBraidingUpdater();
      this.createOperationEditionController();
      this.createDefaultPatternLoader();
      this.createProvidersReloader();
      this.hookupServiceEventListeners();
      this.render();

      //@todo remove this once we have merged in the weaver.enableModernMode function
      this.serviceManager.get('FeatureSwitcherService').get('display-action-in-side-menu').enable();
      this.serviceManager.get('FeatureSwitcherService').get('display-action-within-selected-item').disable();
    },

    /**
     * Updates the 'locked' state of the tapestry, responds to events on the aggregator
     * @method _updateLockedState
     * @private
     */
    _updateLockedState: function () {
      if (this.serviceManager.get('AggregatorClient').get('hasLockedProviders')) {
        this.lock();
      } else {
        this.unlock();
      }
    },

    /**
     * Reacts to lock events
     * todo: Should this be private? Does anyone else call it? or does it just react to events?
     * @method lock
     */
    lock: function () {
      this.$el.addClass('is-locked');
      this.sideMenuController.showMenu('providers');
    },

    /**
     * Reacts to unlock events
     * todo: Should this be private? Does anyone else call it? or does it just react to events?
     * @method unlock
     */
    unlock: function () {
      this.$el.removeClass('is-locked');
    },

    /**
     * @method _displayInitialThreads
     * @private
     */
    _displayInitialThreads: function () {
      var i = 0;
      var threads = this.model.get('threads').models;
      var numberOfThreads = threads.length;
      var target = this.threadListView.model;
      var model = this.model;

      function displayThread() {
        if (i < numberOfThreads) {
          var thread = threads[i];
          if (!thread.get('hidden')) {
            target.add(thread);
          }
          i++;
          setImmediate(displayThread);
        } else {
          model.attachAutoSyncOnEvents();
          model.save();
        }
      }

      setImmediate(displayThread);
    },

    /**
     * Safely destroy all services, avoiding memory leaks
     * @method _destroyServices
     * @private
     */
    _destroyServices: function () {
      this.servicesController.stop(this.serviceManager);
      this.servicesController.deregister(this.serviceManager);
      this.defaultPatternLoader.stopListening();
      this.providersReloader.stopListening();
      this.operationEditionController.stopListening();
      this.massRelationFilterController.stopListening();
    },

    /**
     * @method remove
     */
    remove: function () {
      BaseView.prototype.remove.apply(this, arguments);
      this.threadListView.remove();
      this.toolbarView.remove();
      this._destroyServices();
    },

    /**
     * @method logout
     */
    logout: function () {
      this.serviceManager.get('AggregatorClient').logout();
    },

    /**
     * @method render
     */
    render: function () {
      this.sideMenuLayoutView.$el.addClass('mas-bottomToolbarLayout--content');
      this.$el
        .append(this.toolbarView.el)
        .append(this.sideMenuLayoutView.el)
        .append(this.notificationView.el);
      this._updateLockedState();
    }
  });

  return TapestryScreen;
});
