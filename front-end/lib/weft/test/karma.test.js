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
require('sinon');
var chai = require('chai');
var sinonChai = require('sinon-chai');
var chaiThings = require('chai-things');
chai.use(sinonChai);
chai.use(chaiThings);

var Backbone = require('backbone');
Backbone.$ = require('jquery');

window.expect = chai.expect;

var requireTest = require.context('./weft', true, /\.test\.js$/);
requireTest.keys().forEach(requireTest);
