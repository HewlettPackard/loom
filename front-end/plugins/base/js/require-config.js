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
define(['bower_components/weaver/js/require-config'], function (configuration) {
configuration.paths['weaver'] = '../bower_components/weaver/js/weaver';
configuration.paths['requirejs-config'] = '../bower_components/plugins/js/require-config';
configuration.paths['mustache'] = '../bower_components/mustache/mustache';
configuration.paths['lodash.extensions'] = '../bower_components/weaver/js/weaver/utils/lodash.extensions';
configuration.paths['json'] = '../bower_components/requirejs-plugins/src/json';
configuration.paths['d3'] = '../bower_components/d3/index';
configuration.paths['cartogram'] = '../bower_components/cartogram/cartogram-d3';
configuration.paths['cartogram-standalone'] = '../bower_components/cartogram/cartogram-standalone';
configuration.paths['task'] = '../bower_components/plugins/plugin-map/js/weaver-map/taskutils/taskPlugin';
configuration.paths['assert'] = '../bower_components/weaver/js/weaver/utils/assert';
configuration.shim['cartogram'] = {"deps":["d3","q","setImmediate"]};
configuration.paths['weaver-map'] = '../bower_components/plugins/plugin-map/js/weaver-map';
configuration.paths['weaver-table'] = '../bower_components/plugins/plugin-table/js/weaver-table';
configuration.paths['weaver-treemap'] = '../bower_components/plugins/plugin-treemap/js/weaver-treemap';
return configuration;});