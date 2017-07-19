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
define(['bower_components/weft/js/require-config'], function (configuration) {

  "use strict";

  // Prevent JSHint from complaining about not using the dot notation
  // to add to the configuration
  /*jshint -W069 */
  configuration.paths['fastdom'] = '../bower_components/fastdom/index';
  configuration.paths['underscore.string'] = '../bower_components/underscore.string/dist/underscore.string.min';
  configuration.paths['jquery-ui'] = '../bower_components/jquery-ui/ui';
  configuration.paths['backbone.stickit'] = '../bower_components/backbone.stickit/backbone.stickit';
  configuration.paths['text'] = '../bower_components/requirejs-text/text';
  configuration.paths['numeral'] = '../bower_components/numeral/numeral';
  configuration.paths['handjs'] = '../bower_components/handjs/index';
  configuration.paths['jgestures'] = '../bower_components/jgestures/index';
  configuration.paths['weft'] = '../bower_components/weft/js/weft';
  /*jshint +W069 */

  return configuration;
});
