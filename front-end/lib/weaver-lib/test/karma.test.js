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
"use strict";

var Backbone = require('backbone');
Backbone.$ = require('jquery');

require('sinon');
var chai = require('chai');
var sinonChai = require('sinon-chai');
var chaiThings = require('chai-things');
var chaiJquery = require('chai-jquery');
chai.use(sinonChai);
chai.use(chaiThings);
chai.use(chaiJquery);

window.expect = chai.expect;

var MAX_TESTS = 0;
var INCLUDE_TESTS = [
  //'./views/FiberOverview/TooltipController.test.js'
  // './views/QueryEditor.test.js'
];
var EXCLUDE_TESTS = [];

function hasSpaceForTests(index) {

  return !(MAX_TESTS && index > MAX_TESTS);
}

function isIncludedTest(file) {
  return INCLUDE_TESTS.indexOf(file) !== -1;
}

function isExcludedTest(file) {
  return EXCLUDE_TESTS.indexOf(file) !== -1;
}

var requireTest = require.context('./weaver', true, /\.test\.js$/);
requireTest.keys().filter(function(file, index) {
  return !isExcludedTest(file) && (hasSpaceForTests(index) || isIncludedTest(file));

}).forEach(requireTest);
