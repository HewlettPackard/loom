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
  var Cookies = require('cookies-js');

  /**
   * LoomClient provides a low level client API for interactions with Loom
   * @class LoomClient
   * @namespace io
   * @module weft
   * @submodule io
   * @constructor
   * @param {String} url The URL of the Loom server to interact with
   * @extends Backbone.Events
   */
  function LoomClient(url) {
    this.url = url || '/loom';
  }

  _.extend(LoomClient, {
    STATUS_PATH: '/status',
    PATTERNS_PATH: '/patterns',
    TAPESTRIES_PATH: '/tapestries',
    PROVIDERS_PATH: '/providers',
    OPERATIONS_PATH: '/operations',
    ITEM_TYPES_PATH: '/itemTypes',
    SCHEMA_PATH: '/providers/schema'
  });

  _.extend(LoomClient.prototype, Backbone.Events);

  _.extend(LoomClient.prototype, {

    /**
     * Log a user into a provider
     * @method login
     * @param username
     * @param password
     * @returns {*|type[]}
     */
    login: function (username, password) {
      return this.send({
        url: LoomClient.PROVIDERS_PATH + '/os/private?operation=login',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
          username: username,
          password: password
        })
      });
    },

    /**
     * Logout of the aggregator
     * @method logout
     * @return {jqXHR} The jqXHR corresponding to the request
     */
    logout: function (synchronous) {
      return this.send({
        url: LoomClient.PROVIDERS_PATH + '?operation=logout',
        method: 'POST',
        async: !synchronous
      });
    },

    /**
     * Expire the loom cookie
     * @method expireAuthenticationCookie
     */
    expireAuthenticationCookie: function () {
      Cookies.expire('loom');
    },

    /**
     * Requests the list of providers available on the Aggregator
     * @method getProviders
     * @return {jqXHR} The jqXHR corresponding to the request
     */
    getProviders: function () {
      return this.send({
        url: LoomClient.PROVIDERS_PATH,
        method: 'GET'
      });
    },

    /**
     * Creates a tapestry on the server
     * @method createTapestry
     * @param  {Object} tapestry The tapestry description to be sent as data
     * @return {jqXHR} The jqXHR corresponding to the request
     */
    createTapestry: function (tapestry) {
      return this.send({
        url: LoomClient.TAPESTRIES_PATH,
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(tapestry)
      });
    },

    /**
     * Updates the tapestry on the server
     * @method updateTapestry
     * @param tapestry
     * @returns {*|type[]}
     */
    updateTapestry: function (tapestry) {
      return this.send({
        url: LoomClient.TAPESTRIES_PATH + '/' + tapestry.id,
        method: 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(tapestry)
      });
    },

    /**
     * Sends a request to check the status of the server
     * @method  getStatus
     * @return {jqXHR} The jqXHR corresponding to the request
     */
    getStatus: function () {
      return this.send({
        url: LoomClient.STATUS_PATH,
        method: 'GET'
      });
    },

    /**
     * Sends a request to list the patterns available on the server
     * @method  getPatterns
     * @return {jqXHR} The jqXHR corresponding to the request
     */
    getPatterns: function () {
      return this.send({
        url: LoomClient.PATTERNS_PATH,
        method: 'GET'
      });
    },

    /**
     * Sends a request for the pattern with given id
     * @method getPattern
     * @param  {String} id The ID of the patterns
     * @return {jqXHR}  The jqXHR corresponding to the request
     */
    getPattern: function (id) {
      return this.send({
        url: LoomClient.PATTERNS_PATH + '/' + id,
        method: 'GET'
      });
    },

    /**
     * Sends a request for the list of operations available
     * to set up the queries
     * @method getOperations
     * @param  {String} [options.providerType] ID of the provider type to use for filtering the list
     * @return {jqXHR}  The jqXHR corresponding to the request
     */
    getOperations: function (options) {
      options = options || {};
      return this.send({
        url: LoomClient.OPERATIONS_PATH,
        type: 'GET',
        contentType: 'application/json',
        data: options
      });
    },

    /**
     * Sends a request for the schema currently available from the different
     * providers the user is logged in
     * @method getSchema
     * @return {jqXHR} The jqXHR corresponding to the request
     */
    getSchema: function () {
      return this.send({
        url: LoomClient.SCHEMA_PATH,
        dataType: 'json'
      });
    },

    /**
     * Gets the item types from the server
     * @method getItemTypes
     * @param options
     * @returns {*|type[]}
     */
    getItemTypes: function (options) {
      return this.send({
        url: LoomClient.ITEM_TYPES_PATH,
        data: options
      });
    },

    /**
     * Make a query to the server at the passed url
     * @method query
     * @param threadPath
     * @returns {*|type[]}
     */
    query: function (threadPath) {
      return this.send({
        url: threadPath,
        type: 'GET',
        contentType: 'application/json'
      });
    },

    /**
     * Sends given request to the server
     * @method send
     * @param  {[type]} request [description]
     * @return {[type]}         [description]
     */
    send: function (request) {
      var url = request.url || '';
      if (url.indexOf('http') !== 0) {
        request.url = (this.url || '') + url;
      }
      request.xhrFields = {
        withCredentials: true
      };
      var xhr = Backbone.ajax(request);
      /**
       * Fired when an error occurs sending the payload to the server
       * The event is triggered directly on the LoomClient object
       *
       * @event error
       * @param {LoomClient} object
       */
      xhr.fail(_.bind(this.trigger, this, 'error'));

      return xhr;
    }
  });

  return LoomClient;
});
