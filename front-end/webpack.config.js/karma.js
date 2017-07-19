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
module.exports = function (projectRoot) {

  var webpack = require('webpack');
  var _ = require('lodash');
  var path = require('path');



  var config = require('./app-base')(projectRoot);

  config.devtool = 'inline-source-map';

  config.module.preLoaders = [{
    test: /\.js$/, // include .js files
    exclude: /(node_modules|bower_components|vendor)/, // Exclude files that aren't part of our codebase
    loader: "jshint-loader"
  }];

  var minimist = require('minimist');
  var cliArgs = minimist(process.argv.slice(2));
  if (cliArgs.coverage !== false) {
    config.module.postLoaders = [{
      test: /\.js$/,
      exclude: /(test|node_modules|bower_components|vendor)/,
      loader: 'istanbul-instrumenter'
    }];
  }

  config.module.loaders.unshift({
    test: /sinon.js$/,
    loader: 'script'
  });

  config.plugins.push(new webpack.NormalModuleReplacementPlugin(/^sinon$/, path.join(projectRoot, "node_modules/sinon/pkg/sinon.js")));
  config.node = {
    setImmediate: false
  };

  config.jshint = {
    // emitErrors: true,
    failOnHint: true
  };

  return config;
};
