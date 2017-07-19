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
define(['lodash'], function(_) {
  "use strict";

  /**
   * @class ServiceManager
   * @module weaver
   * @submodule utils
   */
  return (function() {  
    /**
     * Array of services
     * @property {Object} service
     * @private
     */
    var services =  {};

    return {

      ERROR_SERVICE_EXISTS: "Service already exists in manager: ",
      ERROR_SERVICE_UNKNOWN: 'Service unavailable in manager: ',
      ERROR_STOP_LISTENING_NOT_SUPPORTED: 'Service does not support "stop listening" method: ',

      /**
       * Get a service by name
       * @method get
       * @param serviceName
       * @throws When service unknown
       * @returns {*}
       */
      get: function(serviceName) {
        if (_.has(services, serviceName)) {
          return _.get(services, serviceName);
        }
        throw new Error(this.ERROR_SERVICE_UNKNOWN+serviceName);
      },

      /**
       * Register a service by name
       * @method register
       * @param serviceName
       * @param service
       * @returns {boolean}
       */
      register: function(serviceName, service) {
        if (_.has(services, serviceName)) {
          throw new Error(this.ERROR_SERVICE_EXISTS+serviceName);
        }
        _.set(services, serviceName, service);
        return true;
      },

      /**
       * Deregister a service by name
       * @method deregister
       * @param serviceName
       */
      deregister: function(serviceName) {
        if (_.has(services, serviceName)) {
          delete services[serviceName];
          return true;
        }
        throw new Error(this.ERROR_SERVICE_UNKNOWN+serviceName);
      },

      /**
       * Stops listening to a service
       * @method stopListening
       * @param serviceName
       */
      stopListening: function(serviceName) {
        if (!_.has(services, serviceName)) {
          throw new Error(this.ERROR_SERVICE_UNKNOWN+serviceName);
        }
        if (typeof services[serviceName].stopListening !== 'function') {
          throw new Error(this.ERROR_STOP_LISTENING_NOT_SUPPORTED+serviceName);
        }
        services[serviceName].stopListening();
        return true;
      },

      /**
       * Push out the registered services
       * @returns {*|string|LoDashWrapper<string>|String}
       */
      toString: function() {
        return _.keys(services).join('\n').trim();
      }
    };
  })();
});
