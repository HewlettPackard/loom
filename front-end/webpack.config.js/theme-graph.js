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
module.exports = function (projectRoot, appRoot) {

  var path = require('path');
  var webpack = require('webpack');
  var themeConfig = require('./theme')(projectRoot, appRoot);

  var ExtractTextPlugin = require('extract-text-webpack-plugin');
  var themeExtractPlugin = new ExtractTextPlugin('css/theme-graph.css');

  // Replace the entry point to match the graph theme file
  themeConfig.entry = path.join(projectRoot, 'themes/weaver-theme-hpe/less/theme-graph.less');
  themeConfig.output.filename = 'ignore-me-too.js';

  // And replace the loaders to extract it in the appropriate place
  themeConfig.module.loaders.splice(0, 1, { test: /\.less$/, loader: themeExtractPlugin.extract('css?sourceMap!autoprefixer!less', {
    publicPath: '../'
  })});

  themeConfig.plugins.splice(0, 1, themeExtractPlugin);

  return themeConfig;
};
