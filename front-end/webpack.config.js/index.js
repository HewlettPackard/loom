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
var path = require('path');
var minimist = require('minimist');
var cliArgs = minimist(process.argv.slice(2));

if (!cliArgs.app) {
  cliArgs.app = 'web';
}

var projectRoot = path.resolve(__dirname, '..');
var appRoot = path.join(projectRoot, 'apps', cliArgs.app);

module.exports = [

  // Build is split into two parts:
  // 1. The app in itself, assembling JS, LESS and other resources into the app
  require('./app')(projectRoot, appRoot, cliArgs),
  // 2. The theme to be used
  require('./theme')(projectRoot, appRoot),
  // 3. The theme for the relationship graph
  require('./theme-graph')(projectRoot, appRoot)
];
