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

  var ClusterOperation = Backbone.Model.extend({

    initialize: function () {
      this.setRawParameters();
    },

    setRawParameters: function (parameters) {

      parameters = parameters || {};

      // Set the attributes for parameters.
      parameters.attributes = _.keys(this.get('attributes'));

      this.set('parameters', parameters);
      return this;
    },

    addRawParameter: function (name, value) {
      this.get('parameters')[name] = value;
      return this;
    },

    createArrayFromAttributes: function (parameterName, callback, thisArg) {
      var parameters = this.get('parameters');
      var newParameter = parameters[parameterName] = [];
      thisArg = thisArg || {};

      _.forEach(_.keys(this.get('attributes')), function (attribute) {
        newParameter.push(callback.call(thisArg, attribute));
      });
      return this;
    },

    convertAttributes: function (attributes) {
      var res = _.reduce(_.keys(attributes), function (res, attribute) {
        res += attribute + ",";
        return res;
      }, "");
      return res.substr(0, res.length - 1);
    },

    toJSON: function () {
      var result = {
        operator: this.get('operator'),
        parameters: this.get('parameters')
      };

      return result;
    },

  });

  return ClusterOperation;
});
