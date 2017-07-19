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

var loadedConfiguration = {};

require('./weaver-config.json');
require('weaver-lib/css/style.less');

var Backbone = require('backbone');
var $ = Backbone.$ = require('jquery');
var Cocktail = require('backbone.cocktail');
Cocktail.patch(Backbone);

var Weaver = require('weaver/Weaver');
var loadConfiguration = require('web/loadConfiguration');

loadConfiguration().then(function (configuration) {
  if (configuration.theme) {
    $('<link rel="stylesheet">').attr('href', configuration.theme).appendTo(document.head);
  }
  loadedConfiguration = configuration;
}).done(function () {
  window.weaver = new Weaver(loadedConfiguration);
  window.weaver.start(true);
});
