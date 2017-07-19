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
define({
  'baseUrl': 'js',
  'paths': {
    'q': '../bower_components/q/q',
    'jquery': '../bower_components/jquery/jquery',
    'lodash': '../bower_components/lodash/dist/lodash',
    'backbone': '../bower_components/backbone/backbone',
    'cookies-js': '../bower_components/cookies-js/src/cookies',
    'underscore.string': '../bower_components/underscore.string/dist/underscore.string.min',
    'backbone-filtered-collection': '../bower_components/backbone-filtered-collection/backbone-filtered-collection',
    'setImmediate': '../bower_components/setimmediate/setImmediate'
  },
  'shim': { // Shim for libraries that do not make requireJS exports natively
    backbone: {
      "deps": ['lodash', 'jquery'],
      "exports": "Backbone"
    }
  },
  'map': {
    '*': {
      'underscore': 'lodash'
    }
  }
});
