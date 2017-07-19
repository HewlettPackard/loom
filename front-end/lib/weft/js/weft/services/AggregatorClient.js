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
  var FilteredCollection = require('backbone-filtered-collection');
  var LoomClient = require('weft/io/LoomClient');
  var Tapestry = require('weft/models/Tapestry');
  var Pattern = require('weft/models/Pattern');
  var Operation = require('weft/models/Operation');
  var Provider = require('weft/models/Provider');
  var ProvidersCollection = require('weft/models/AggregatorClient/ProvidersCollection');
  var AvailableItemTypesCollection = require('weft/models/AvailableItemTypesCollection');

  /**
   * AggregatorClient centralises the interactions with a remote aggregator,
   * providing methods for querying it and interacting with it
   * @class AggregatorClient
   * @namespace services
   * @module weft
   * @submodule services
   * @extends Backbone.Model
   */
  var AggregatorClient = Backbone.Model.extend({

    constructorName: 'LOOM_AggregatorClient',

    /**
     * A map allowing some configuration of the class
     * via AggregatorClient.prototype.config
     * @property {Object} config
     */
    config: {
      url: '/loom'
    },

    // PROPERTIES
    defaults: function () {

      var providers = new ProvidersCollection();
      var lockedProviders = new FilteredCollection(providers);
      lockedProviders.filterBy({
        locked: true
      });
      var loggedInProviders = new FilteredCollection(providers);
      loggedInProviders.filterBy({
        loggedIn: true
      });
      return {

        /**
         * @property selectedFiber
         */
        selectedFiber: undefined,

        /**
         * @todo HUI - please explain what this means
         * @property bogyFiber
         */
        bogyFiber: undefined,

        /**
         * @todo HUI - please explain what this means
         * @property bogyFiberRelations
         */
        bogyFiberRelations: undefined,

        /**
         * The list of patterns currently available on this Aggregator
         * @attribute  availablePatterns
         * @type {Backbone.Collection}
         */
        availablePatterns: new Backbone.Collection(),

        /**
         * The list of providers available on the remote aggregator
         * @type {Backbone.Collection}
         */
        providers: providers,

        /**
         * The list of itemTypes currently available on this Aggregator
         * @attribute  availableItemTypes
         * @type {AvailableItemTypesCollection}
         */
        availableItemTypes: new AvailableItemTypesCollection(providers),

        /**
         * A flag marking if the user is logged in to the aggregator
         * @attribute  loggedId
         * @type {Boolean}
         */
        loggedIn: false,

        /**
         * The URL of the aggregator
         * @property url
         * @type {String}
         * @default '/loom'
         */
        url: AggregatorClient.prototype.config.url,

        /**
         * The list of providers currently locking the Loom server
         * because their session has expired.
         * @type {FilteredCollection}
         */
        lockedProviders: lockedProviders,

        /**
         * The list of providers user have currently logged in to
         * @type {FilteredCollection}
         */
        loggedInProviders: loggedInProviders,

        /**
         * The number of locked providers
         * @type {Number}
         */
        hasLockedProviders: 0,

        relationTypes: {
          equivalence: 'Equivalent',
          default: 'Not equivalent',
          'dir:file:ancestor': 'Ancestor directory',
          'dir:file:contains': 'Parent directory'
        }
      };
    },

    initialize: function () {
      this._initializeLoomClient();

      // Listen to the filtered collection and not the underlying list of all providers
      // so that the collections are updated when accessing their data
      this.get('loggedInProviders').on('add remove', this._updateAggregatorLoggedIn, this);
      this.get('loggedInProviders').on('add remove', this._updateAvailablePatterns, this);
      this.get('lockedProviders').on('add remove', this._updateAggregatorLocked, this);
    },

    /**
     * @method _updateAggregatorLocked
     * @private
     */
    _updateAggregatorLocked: function () {
      this.set('hasLockedProviders', this.get('lockedProviders').size());
    },

    /**
     * @method _updateAggregatorLoggedIn
     * @private
     */
    _updateAggregatorLoggedIn: function () {
      this.set('loggedIn', !!this.hasOneProviderLoggedIn());
    },

    /**
     * @method _updateAvailablePatterns
     * @private
     */
    _updateAvailablePatterns: function () {
      this.get('availablePatterns').set(this._listAvailablePatterns());
    },

    /**
     * @method _listAvailablePatterns
     * @returns {*}
     * @private
     */
    _listAvailablePatterns: function () {
      return this.get('loggedInProviders').reduce(function (result, provider) {
        var newPatterns = _.filter(provider.get('patterns'), function (pattern) {
          return !_.find(result, function (listedPattern) {
            return listedPattern.id === pattern.id;
          });
        });
        return result.concat(newPatterns);
      }, []);
    },

    /**
     * @method _initializeLoomClient
     * @private
     */
    _initializeLoomClient: function () {
      this.loomClient = new LoomClient(this.get('url'));
      this.loomClient.on('error', _.bind(this._handleClientError, this));
      this.on('change:url', function (model, url) {
        this.loomClient.url = url;
      }, this);
    },

    /**
     * Logout of the aggregator
     * @method logout
     * @param {Boolean} synchronous Will send synchronous requests to the server if true
     * @return {jqXHR} The jqXHR corresponding to the request
     */
    logout: function (synchronous) {
      this.get('providers').forEach(function (provider) {
        // Don't send requests to the server and use bulk disconnection
        provider.logout(true);
      });
      // Expect the server to clear the cookie in the response to the logout
      return this.loomClient
        .logout(synchronous)
        .always(_.bind(this.set, this, 'loggedIn', false));
    },

    /**
     * @method expireAuthenticationCookie
     */
    expireAuthenticationCookie: function () {
      this.loomClient.expireAuthenticationCookie();
    },

    /**
     * Fetches the list of providers available on this aggregator
     * @method getProviders
     * @return {Promise} A promise for the list of Providers available on this aggregator
     */
    getProviders: function () {
      return this.loomClient.getProviders()
        .then(_.bind(this._parseProviders, this))
        .then(_.bind(this.setProviders, this));
    },

    /**
     * Returns the list of providers the user is currently logged in
     * @method getLoggedInProviders
     * @return {Array} [description]
     */
    getLoggedInProviders: function () {
      return this.get('loggedInProviders').models;
    },

    /**
     * @method findProviders
     * @param providersDescription
     * @returns {TResult[]}
     */
    findProviders: function (providersDescription) {
      return _.map(providersDescription, function (providerDescription) {
        return this.get('providers').findWhere(providerDescription);
      }, this);
    },

    /**
     * Checks if the user is currently logged in to at least one provider
     * on this aggregator
     * @method hasOneProviderLoggedIn
     * @return {Boolean}
     */
    hasOneProviderLoggedIn: function () {
      return this.get('loggedInProviders').size();
    },

    /**
     * Updates the list of providers available on this aggregator
     * @method setProviders
     * @param {[type]} providers [description]
     */
    setProviders: function (providers) {
      this.get('providers').set(providers);
      return providers;
    },

    /**
     * @method _parseProviders
     * @param json
     * @returns {any[]|TResult[]}
     * @private
     */
    _parseProviders: function (json) {
      return _.map(json.providers, _.bind(function (providerJson) {
        var existingProvider = this.getProvider(providerJson.id);
        if (existingProvider) {
          existingProvider.set(existingProvider.parse(providerJson));
        } else {
          return new Provider(providerJson, {
            parse: true,
            aggregator: this
          });
        }
      }, this));
    },

    /**
     * @method getProvider
     * @param providerId
     * @returns {*}
     */
    getProvider: function (providerId) {
      return this.get('providers').get(providerId);
    },

    /**
     * @method getSchema
     * @returns {*|jqXHR}
     */
    getSchema: function () {
      return this.loomClient.getSchema();
    },

    /**
     * Creates an empty Tapestry, associated with this aggregator
     * and that will save itself when its content changes
     * @method getEmptyTapestry
     * @return {Tapestry} Created tapestry
     */
    getEmptyTapestry: function () {
      return new Tapestry([], {
        aggregator: this
      });
    },

    /**
     * It should updateTapestry on the server
     * @method syncTapestry
     * @param  {Tapestry} tapestry The tapestry to update
     * @return {Promise} A promise for the updated Tapestry
     */
    syncTapestry: function (tapestry) {
      // Tapestry is considered new as long as it doesn't have an ID
      if (!tapestry.id) {
        // As tapestry implements toJSON, it'll be automatically serialized
        // when the query actually gets sent
        return this.loomClient.createTapestry(tapestry)
          .then(function (response) {
            tapestry.set('id', response.id);
            return tapestry;
          });
      } else {
        return this.loomClient.updateTapestry(tapestry)
          .then(function () {
            return tapestry;
          });
      }
    },

    /**
     * Sends a request to the aggregator to check its status
     * @method  getStatus
     * @return {jqXHR} The jqXHR corresponding to the request
     */
    getStatus: function () {
      return this.loomClient.getStatus();
    },

    /**
     * Sends a request for the patterns offered by the aggregator
     * @method  getPatterns
     * @return {Promise} A promise for the list of patterns
     */
    getPatterns: function () {
      return this.loomClient.getPatterns().then(_.bind(this._parsePatterns, this));
    },

    /**
     * @method _parsePatterns
     * @param response
     * @returns {any[]|TResult[]}
     * @private
     */
    _parsePatterns: function (response) {
      return _.map(response.patterns || [], _.bind(this._parsePattern, this));
    },

    /**
     * @method _parsePattern
     * @param json
     * @returns {Pattern}
     * @private
     */
    _parsePattern: function (json) {
      return new Pattern(json, {
        parse: true
      });
    },

    /**
     * Adds one or several Pattern(s) to the list of available Patterns
     * @method addAvailablePatterns
     * @param {Pattern|Array} patterns One or multiple Pattern to add
     */
    addAvailablePatterns: function (patterns) {
      this.get('availablePatterns').add(patterns);
    },

    /**
     * Sends a request for pattern with given ID to the aggregator
     * @method  getPattern
     * @param  {String}  id The ID of requested pattern
     * @return {Promise} A promise for the pattern
     */
    getPattern: function (id) {
      return this.loomClient.getPattern(id).then(_.bind(this._parsePattern, this));
    },

    /**
     * todo: investigate this function.. has the definition changed? if so.. why? when? 4 params?
     *
     * Queries given thread with provided query parameters
     *
     * @method  query
     * @param  {models.Thread}  thread         The thread to query
     * @param  {Backbone.Model} query          The parameters of the query
     * @param  {Array}          relatedThreads A list of threads to compute relations against
     * @param  {Array}          metrics        A list of metrics to return in addition to the properties of the Thread's elements
     * @return {jqXHR} The jqXHR corresponding to the request
     */
    query: function (thread) {
      return this.loomClient.query(thread.url() + '/results');
    },

    /**
     * @method getItemTypes
     * @param options
     * @returns {*|type[]}
     */
    getItemTypes: function (options) {
      return this.loomClient.getItemTypes(options);
    },

    /**
     * Returns the list of operations available to query
     * the ItemType with provided ID
     * @method getOperationsForItemType
     * @param  {String} itemTypeId The ID of the ItemType you want the operations for
     * @return {Promise}             The list of operations
     */
    getOperationsForItemType: function (itemTypeId) {
      return this.loomClient.getOperations({
        itemType: itemTypeId
      })
      .then(this._removeDuplicateOperations);
    },

    /**
     * Remove duplicate operations that would have been registered
     * by multiple adapters
     * @method _removeDuplicateOperations
     * @param  {Object} response The JSON response from the server
     * @return {Array}           The list of operations, free from duplicate operations
     */
    _removeDuplicateOperations: function (response) {
      return _.reduce(response.operations, function (result, operation) {
        operation = new Operation(operation);
        if (!operation.isInList(result)) {
          result.push(operation);
        }
        return result;
      }, [], this);
    },

    /**
     * @method _handleClientError
     * @param xhr
     * @private
     */
    _handleClientError: function (xhr) {
      if (xhr.status === 423) {
        this._handleLockedError(xhr);
      } else if (xhr.status === 401) {
        this._handle401Unauthorized(xhr);
      }
    },

    /**
     * @method _handleLockedError
     * @param xhr
     * @private
     */
    _handleLockedError: function (xhr) {
      var content = JSON.parse(xhr.responseText);
      content.message = JSON.parse(content.message);
      var providers = this.findProviders(content.message.providers);
      _.forEach(providers, function (provider) {
        provider.lock();
      });
    },

    /**
     * @method _handle401Unauthorized
     * @private
     */
    _handle401Unauthorized: function () {
      /**
       * Fired when the AggregatorClient loses its session with Loom. Fired on the object not on the
       * global Backbone Bus
       * @event loomSessionLost
       */
      this.trigger('loomSessionLost');
    },

    /**
     * Sends given request to the aggregator
     * @method send
     * @param request {Object} The content of the request
     * (see [jQuery.ajax() available options](http://api.jquery.com/jQuery.ajax/#jQuery-ajax-settings))
     * @return {jqXHR}
     */
    send: function (request) {
      return this.loomClient.send(request);
    },

    /**
     * Get the url for this Aggregator
     * @method url
     * @returns {any|*}
     */
    url: function () {
      return this.get('url');
    }
  });

  return AggregatorClient;
});
