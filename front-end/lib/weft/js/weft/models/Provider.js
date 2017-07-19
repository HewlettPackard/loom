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
  var Pattern = require('weft/models/Pattern');

  /**
   * Providers are entities providing sets of Aggregations and Patterns
   * to query on an aggregator
   * @class Provider
   * @module weft
   * @submodule models
   * @namespace models
   * @constructor
   * @extends Backbone.Model
   * @type {[type]}
   */
  var Provider = Backbone.Model.extend({

    /**
     * @attribute {services.AggregatorClient} aggregator
     */

    /**
     * @attribute {String} providerType
     */

    /**
     * A flag marking if the user has logged in to this Provider
     * @attribute {Boolean} loggedIn
     */

    /**
     * A flag marking if the user session with the Provider has been locked
     * @attribute {Boolean} locked
     */

    /**
     * A flag marking if Fibers coming from this provider are highlighted
     * @attribute {Boolean} highlighted
     */

    /**
     * Identifier of the provider among the providers with the same providerType
     * @attribute {String} providerId
     */

    /**
     * The list of patterns offered by this provider
     * @attribute {Array} patterns
     */

    /**
     * A human readable name for the provider
     * @attribute {String} name
     */

    urlRoot: '/providers',

    /**
     * Login to the provider
     * @method login
     * @param  {String} username
     * @param  {String} password
     * @return {Promise} A promise for the Patterns available on this
     */
    // IMPROVE: Parse the list of patterns
    login: function (username, password) {
      var xhr = this.get('aggregator').send({
        url: this.url() + '?operation=login',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
          username: username,
          password: password
        })
      });
      var promise = xhr.then(_.bind(this._parsePatterns, this));
      promise.then(_.bind(function () {
        this.set('loggedIn', true);
        this.unlock();
      }, this));
      return promise;
    },

    /**
     * Get the default pattern from all of the patterns, get the first found default pattern if there are
     * ever more than one
     * @method getDefaultPattern
     * @return {models.Pattern} The default pattern for this provider
     */
    getDefaultPattern: function () {
      return _.find(this.get('patterns'), function (pattern) {
        return pattern.get('defaultPattern');
      });
    },

    /**
     *
     * @method _parsePatterns
     * @param response
     * @returns {TResult[]}
     * @private
     */
    _parsePatterns: function (response) {
      var patterns = _.map(response.patterns || [], function (json) {
        return new Pattern(Pattern.prototype.parse(json));
      });
      this.set('patterns', patterns);
      return patterns;
    },

    /**
     * Checks if this Provider offers given pattern
     * @method  isOffering
     * @param  {models.Pattern}  pattern
     * @return {Boolean}
     */
    isOffering: function (pattern) {
      return _.find(this.get('patterns'), function (p) {
        return pattern === p;
      });
    },

    /**
     * @method getAvailableItemTypes
     * @return {Array} The list of item types offered by this provider
     */
    getAvailableItemTypes: function () {
      var itemTypes = _.map(this.get('patterns'), function (pattern) {
        return pattern.getThreadsItemTypes();
      });
      return _.union.apply(_, itemTypes);
    },

    /**
     * @method offersItemTypeOfThread
     * @param thread
     * @returns {boolean}
     */
    offersItemTypeOfThread: function (thread) {
      return this.getAvailableItemTypes().indexOf(thread.get('itemType').id) !== -1;
    },

    /**
     * Logs the user out of this provider
     * @method logout
     * @param {Boolean} clientOnly Flag avoiding notifying the server of the logout
     *                             (eg. when logging out from all the providers
     *                             or a subset of providers)
     * @return {jqXHR}
     */
    logout: function (clientOnly) {
      if (clientOnly) {
        this.set('loggedIn', false);
      } else {
        var xhr = this.get('aggregator').send({
          url: this.url() + '?operation=logout',
          method: 'POST',
          contentType: 'application/json'
        });
        xhr.then(_.bind(function () {
          this.set('loggedIn', false);
          this.unlock();
        }, this));
        return xhr;
      }
    },

    /**
     * Lock the current provider
     * @method lock
     */
    lock: function () {
      this.set('locked', true);
    },

    /**
     * Unlock the current provider
     * @method unlock
     */
    unlock: function () {
      this.set('locked', false);
    },

    /**
     * Get the URL for the current provider
     * @method url
     * @returns {String}
     */
    url: function () {
      return [this.urlRoot, this.get('providerType'), this.get('providerId')].join('/');
    },

    /**
     * Parse the JSON response using options and return the JSON result
     * @method parse
     * @param json
     * @param options
     * @returns {*}
     */
    parse: function (json, options) {
      if (options.aggregator) {
        json.aggregator = options.aggregator;
      }
      json.id = json.providerType + '/' + json.providerId;
      json.name = json.name || json.providerType + ' - ' + json.providerId;
      return json;
    }
  });

  return Provider;
});
