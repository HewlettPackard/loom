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
define([
  'lodash',
  'backbone'
], function (_, Backbone) {

  "use strict";

  //todo: DEPRECATE - I don't think this is being used. I cant see it ever working either.. see below!
  // Little twist on the Coccyx.js sources (which is MIT licensed)
  // http://github.com/onsi/coccyx

  /**
   * Generates an enhanced constructor that will
   * make the objects distinguishable when logging,
   * looking at heap snapshots... (at least on Chrome)
   * @method createNamedConstructor
   * @param {Function} originalExtend The original `extend()` method
   */
  function createNamedConstructor(originalExtend) {
    return function (protoProps, classProps) {
      var parent = this;
      // If a specific constructor is defined then leave the naming definition to it
      //todo: How has this ever worked? There is a typo in constructor.. investigate!!!
      if (protoProps && protoProps.constructorName && !protoProps.hasOwnProperty('constuctor')) {
        /*jslint evil: true */
        eval("protoProps.constructor = function " + protoProps.constructorName + " () { parent.apply(this, arguments) };");
      }
      return originalExtend.call(parent, protoProps, classProps);
    };
  }

  var classNames = ['Model', 'Collection', 'Router', 'View'];

  _.forEach(classNames, function (name) {
    var originalExtend = Backbone[name].extend;
    Backbone[name].extend = createNamedConstructor(originalExtend);
  });

});
