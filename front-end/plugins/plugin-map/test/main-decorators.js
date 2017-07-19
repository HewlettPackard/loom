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
/* global describe, it, sinon, expect, beforeEach, after, afterEach */
/* jshint expr: true */
define(function (require) {
  "use strict";

  require('weaver-map/views/ThreadViewDecorator');
  require('weaver-map/views/ThreadListViewDecorator');
  require('weaver-map/views/ThreadSettingsMenuDecorator');
  require('weaver-map/models/Tapestry/BraidingControllerDecorator');
  require('weaver-map/models/ThreadDecorator');
  require('weaver-map/models/ElementDecorator');
  require('weaver-map/models/AggregationDecorator');
  // TODO: require this line at some point to test it.
  //require('weaver-map/screens/TapestryScreenDecorator');
  //require('./weaver-map/models/QueryResultDecorator');
  //require('./weaver-map/services/FibersLinkerDecorator');
  require('weaver-map/views/QueryEditorDecorator');
});