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

  /**
   * The definition of an action that can be executed on an element
   * @class ActionDefinition
   * @module weft
   * @submodule models
   * @namespace models
   */
  var ActionDefinition = Backbone.Model.extend({

    constructorName: 'LOOM_ActionDefinition',

    urlRoot: '/actions',

    // PROPERTIES
    /**
     * @property aggregator
     * @type services.AggregatorClient
     */

    /**
     * Execute the action on given element with given parameters
     * @method  execute
     * @param  {models.Element} element     The element affected by the action
     * @param  {Object}  parameters  A map of parameters for the action
     * @return {jqXHR} [description]
     */
    execute: function (element, parameters) {
      return element.getAggregator().send(this.buildRequest(element, parameters));
    },

    /**
     * Builds a request object to be sent to execute the action
     * @param  {models.Element} element    The element affected by the action
     * @param  {Object}         parameters The parameters of the action
     * @return {Object}         The request to be sent
     */
    buildRequest: function (element, parameters) {

      return {
        url: this.urlRoot,
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
          id: this.id,
          targets: [element.getActionInputsId()],
          params: this.encodeParameters(parameters)
        })
      };
    },

    encodeParameters: function (parameters) {

      return _.reduce(parameters, function (result, value, id) {

        result.push({
          id: id,
          value: value
        });
        return result;
      }, []);
    }
  });

  return ActionDefinition;
});
