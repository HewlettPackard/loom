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
define(function () {
configuration = {
  "paths": {
    "weaver": "../../bower_components/weaver/js/weaver",
    "requirejs-config": "../../js/test/require-config",
    "plugin-test": "../../js/test",
    "plugins": "../../js",
    "weft": "../../../bower_components/weft/js/weft",
    "chai": "../../bower_components/chai/chai",
    "sinon": "../../bower_components/sinonjs/sinon",
    "sinon-chai": "../../bower_components/sinon-chai/lib/sinon-chai",
    "chai-jquery": "../../bower_components/chai-jquery/chai-jquery",
    "animation-frame": "../../bower_components/animation-frame/AnimationFrame",
    "jquery": "../../bower_components/jquery/jquery",
    "setImmediate": "../../bower_components/setimmediate/setImmediate",
    "mustache": "../../bower_components/mustache/mustache",
    "lodash.extensions": "../../bower_components/weaver/js/weaver/utils/lodash.extensions",
    "json": "../../bower_components/requirejs-plugins/src/json",
    "d3": "../../bower_components/d3/index",
    "cartogram": "../../bower_components/cartogram/cartogram-d3",
    "cartogram-standalone": "../../bower_components/cartogram/cartogram-standalone",
    "task": "../../bower_components/plugins/plugin-map/js/weaver-map/taskutils/taskPlugin",
    "assert": "../../bower_components/weaver/js/weaver/utils/assert",
    "fastdom": "../../../bower_components/fastdom/index",
    "underscore.string": "../../bower_components/underscore.string/dist/underscore.string.min",
    "jquery-ui": "../../../bower_components/jquery-ui/ui",
    "backbone.stickit": "../../../bower_components/backbone.stickit/backbone.stickit",
    "text": "../../../bower_components/requirejs-text/text",
    "numeral": "../../../bower_components/numeral/numeral",
    "handjs": "../../../bower_components/handjs/index",
    "jgestures": "../../../bower_components/jgestures/index",
    "q": "../../bower_components/q/q",
    "lodash": "../../bower_components/lodash/dist/lodash",
    "backbone": "../../bower_components/backbone/backbone",
    "backbone.uniquemodel": "../../bower_components/backbone.uniquemodel/backbone.uniquemodel",
    "cookies-js": "../../bower_components/cookies-js/src/cookies",
    "backbone-filtered-collection": "../../bower_components/backbone-filtered-collection/backbone-filtered-collection"
  },
  "shim": {
    "cartogram": {
      "deps": [
        "d3",
        "q",
        "setImmediate"
      ]
    },
    "backbone": {
      "deps": [
        "lodash",
        "jquery"
      ],
      "exports": "Backbone"
    }
  },
  "map": {
    "*": {
      "underscore": "lodash"
    }
  },
  "baseUrl": "js"
};
configuration.paths['weaver-map'] = '../../plugin-map/js/weaver-map';
configuration.paths['weaver-table'] = '../../plugin-table/js/weaver-table';
configuration.paths['weaver-treemap'] = '../../plugin-treemap/js/weaver-treemap';
return configuration;});