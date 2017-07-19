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

  /**
   * AttributesOperationBuilder is a helper class to build a new cluster operation
   * from an existing one. It is safe regarding query immutable state requirements.
   * 
   * @param {Operation} operation is the operation to clone from.
   */
  var AttributesOperationBuilder = function AttributesOperationBuilder(operation) {

    return new Helper(operation);
  };

  var Helper = function Helper(operation) {

    this.operator = _.cloneDeep(operation.operator);
    this.parameters = _.cloneDeep(operation.parameters);
  };

  /**
   * Set a raw value for a parameter of the operation.
   * @param {String} name  is the name of the operation parameter
   * @param {Object} value is the value for that operation (can be whatever you want)
   */
  Helper.prototype.setRawParameter = function (name, value) {
    this.parameters[name] = value;
    return this;
  };

  /**
   * Allow to change a parameter based on an implicit parameter assumed to be there:
   *   - attributes
   *
   * This parameter contains the name of the attributes that are to be used for this cluster
   * operation. The callback given will be used to fill an array of the same length as attributes
   * where each value will match the result of:
   *                     callback(<attributeName>)
   *
   * The expected result of calling this function is the same as doing conceptually:
   *     
   *     var attributes = parameters.attributes;
   *     parameters[parameterName] = [callback(attributes[0]), callback(attributes[1]), ...]
   *
   * 
   * @param  {String}   parameterName is the name of the parameter
   * @param  {Function} callback      is the function that will be called on each attribute
   * @param  {Object}   thisArg       is an optional this argument for the callback
   * @return {Object}                 Returns this.
   */
  Helper.prototype.createArrayFromAttributes = function (parameterName, callback, thisArg) {
    
    var parameters = this.parameters;
    var newParameter = parameters[parameterName] = [];
    thisArg = thisArg || {};

    _.forEach(parameters.attributes, function (attribute) {
      newParameter.push(callback.call(thisArg, attribute));
    });
    return this;
  };

  /**
   * Build the operation resulting from the previous changes.
   * @return {Operation} Returns the resulting operation.
   */
  Helper.prototype.build = function () {
    var result = {
      operator: this.operator,
      parameters: this.parameters,
    };

    return result;
  };

  return AttributesOperationBuilder;
});
