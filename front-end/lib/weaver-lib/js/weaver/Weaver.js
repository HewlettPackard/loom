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
/**
 *
 */
define(function (require) {

  "use strict";
  var _ = require('lodash');
  var $ = require('jquery');
  var Backbone = require('backbone');
  var EventBus = require('weaver/utils/EventBus');
  require('handjs');
  require('weaver/utils/lodash.inflection');

  var TapestryScreen = require('weaver/screens/TapestryScreen');
  var LoginScreen = require('weaver/screens/LoginScreen');
  var offlineMessage = require('weaver/screens/OfflineScreen.html');
  var connectionScreen = require('weaver/screens/ConnectionScreen.html');
  var AggregatorClient = require('weft/services/AggregatorClient');
  // Loaded for configuration purposes, might be worth encapsulating
  var ThreadMonitorService = require('weft/services/ThreadMonitorService');
  var BraidingUpdater = require('weaver/screens/TapestryScreen/BraidingUpdater');
  var ServiceManager = require('weaver/utils/ServiceManager');
  var TapestryServicesController = require('weaver/screens/TapestryScreen/TapestryServicesController');

  /**
   * A Weaver is an application that loads and displays the information of the system being managed.
   * It delegates its work to a WeaverDisplay, handling the screen updates, and a WeaverWorker, responsible for
   * loading and processing the data.
   * @class Weaver
   * @module weaver
   * @namespace weaver
   * @param configuration {Object} The configuration of the Weaver, which can take the following properties:
   *
   *  - `loom-url`: a String defining the URL of the loom server
   *  - `polling-interval`: the number of milliseconds between each poll
   *
   * @constructor
   * @extends Backbone.Router
   */
  var Weaver = Backbone.Router.extend({

    constructor: function (configuration) {
      Backbone.Router.prototype.constructor.apply(this);
      this._applyConfiguration(configuration);
      this._createServices();
    },

    routes: {
      "": function () {
        this.navigate('login');
      },
      login: "login",
      tapestry: "tapestry"
    },

    eventBus: EventBus,

    enableEventBusLogging: false,

    ServiceManager: ServiceManager,

    /**
     * Debug tool to view the events happening in the system
     * @param event
     * @param payload
     */
    eventBusLogger: function(event, payload) {
      if (this.enableEventBusLogging) {
        console.log('Weaver->'+event, payload);
      }
    },

    /**
     * @method navigate
     * @param route
     */
    navigate: function (route) {
      // Redefine default behaviour of navigate
      // to trigger method calls and use pushState 'replaceState'
      // so that history is consistent
      Backbone.Router.prototype.navigate.call(this, route, {
        trigger: true,
        replace: true
      });
    },

    /**
     * @method login
     */
    login: function () {
      if (this.screen) {
        this.screen.remove();
      }
      this.screen = new LoginScreen({
        model: this.aggregatorClient.get('providers')
      });
      this.screen.once('didLogin', function (patterns) {
        setImmediate(_.bind(function () {
          this._loadInitialPattern(patterns);
          this.navigate('tapestry');
        }, this));
      }, this);
      document.body.appendChild(this.screen.el);
    },

    /**
     * @method connecting
     */
    connecting: function () {
      if (this.screen) {
        this.screen.remove();
      }
      this.screen = $(connectionScreen).appendTo(document.body);
    },

    /**
     * @method tapestry
     */
    tapestry: function () {
      if (this.screen) {
        this.screen.remove();
      }
      if (!this.aggregatorClient.get('loggedIn')) {
        this.navigate('login');
        return;
      }
      this.screen = new TapestryScreen({
        model: this.tapestry,
        ServiceManager: this.ServiceManager,
        TapestryServicesController: TapestryServicesController
      });
      // Append to the body rather than using the body
      // as its element so removal doesn't break anything
      document.body.appendChild(this.screen.el);
    },

    /**
     * @method offline
     */
    offline: function () {
      if (this.screen) {
        this.screen.remove();
      }
      this.screen = $(offlineMessage).appendTo(document.body);
    },

    /**
     * Starts the Weaver
     * @method start
     * @param {Function} callback A callback that will get run once the Weaver has started
     * @todo is this true?
     */ 
    start: function (displayOfflineMessage) {
      $('.mas-splashScreen').remove();
      this.aggregatorClient.expireAuthenticationCookie();
      this.listenTo(EventBus, 'all', this.eventBusLogger);
      $(window).on('unload', _.bind(function () {
        // If Backbone.history is not stopped, Chrome has issues registering
        // the `submit` listeners on the login form, making it being submitted
        // the server rather than handled on the client
        Backbone.history.stop();
        // Make the request synchronous
        if (this.aggregatorClient.get('loggedIn')) {
          this.aggregatorClient.logout(true);
        }
      }, this));

      var promise = this.aggregatorClient.getProviders();
      promise.then(function () {
          Backbone.history.start({
            // Hmm... feels a bit too easy
            root: document.location.pathname
          });
        })
        .done();

      if (displayOfflineMessage) {
        // Backbone history is not started at this point
        // so call the method directly
        promise.fail(_.bind(this.offline, this)).done();
        this.connecting();
      }
      return promise;
    },

    /**
     * @method _loadInitialPattern
     * @param patterns
     * @private
     */
    _loadInitialPattern: function (patterns) {
      this.tapestry = this.aggregatorClient.getEmptyTapestry();
      var defaultPattern = this._getDefaultPattern(patterns);
      if (defaultPattern) {
        this.tapestry.load(defaultPattern);
      }
    },

    /**
     * @method _getDefaultPattern
     * @param patterns
     * @returns {T|*}
     * @private
     */
    _getDefaultPattern: function (patterns) {
      if (patterns && patterns.length) {
        return _.find(patterns, function (pattern) {
          return pattern.get('defaultPattern');
        }) || patterns[0];
      }
    },

    /**
     * @method _createServices
     * @private
     */
    _createServices: function () {
      this.aggregatorClient = new AggregatorClient();
      this.aggregatorClient.on('change:loggedIn', this._maybeDisplayLogin, this);
      this.ServiceManager.register('AggregatorClient', this.aggregatorClient);
    },

    /**
     * @method _maybeDisplayLogin
     * @param aggregatorClient
     * @param loggedIn
     * @private
     */
    _maybeDisplayLogin: function (aggregatorClient, loggedIn) {
      if (!loggedIn) {
        this.navigate('login');
      }
    },

    /**
     * @method _applyConfiguration
     * @param configuration
     * @private
     */
    _applyConfiguration: function (configuration) {
      configuration = configuration || {};
      var mapping = {
        'loom-url': [AggregatorClient, 'url'],
        'polling-interval': [ThreadMonitorService, 'polling-interval'],
        'minimum-fibers-width': [BraidingUpdater, 'minimumFibersWidth'],
        'maximum-number-of-fibers': [BraidingUpdater, 'maximumNumberOfFibers']
      };
      _.forEach(mapping, function (mapping, configurationKey) {
        if (configuration[configurationKey]) {
          mapping[0].prototype.config[mapping[1]] = configuration[configurationKey];
        }
      });
    }
  });

  // Version number gets injected by webpack
  /* jshint -W117 */
  Weaver.VERSION = VERSION;
  /* jshint +W117 */

  return Weaver;
});
