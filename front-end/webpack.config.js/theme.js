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

module.exports = function(projectRoot, appRoot) {
  var path = require('path');
  var webpack = require('webpack');

  var ExtractTextPlugin = require('extract-text-webpack-plugin');
  var themeExtractPlugin = new ExtractTextPlugin('css/theme.css');

  return {
    entry: path.join(projectRoot, 'themes/weaver-theme-hpe/less/theme.less'),
    output: {
      path: path.join(appRoot, 'dist/theme'),
      filename: 'ignore-me.js',
    },
    module: {
      loaders: [
        { test: /\.less$/, loader: themeExtractPlugin.extract('css?sourceMap!autoprefixer!less', {
          publicPath: '../'
        })},
        { test: /\.woff(\?.*)*$/, loader: "file?name=fonts/[name].[ext]"},
        { test: /\.woff2(\?.*)*$/, loader: "file?name=fonts/[name].[ext]"},
        { test: /\.eot(\?.*)*$/, loader: "file?name=fonts/[name].[ext]"},
        { test: /\.ttf(\?.*)*$/, loader: "file?name=fonts/[name].[ext]"},
        { test: /\.otf(\?.*)*$/, loader: "file?name=fonts/[name].[ext]"},
        { test: /\webfont.svg(\?.*)*$/, loader: "file?name=fonts/[name].[ext]"},
        { test: /\.svg$/, loader: "file?name=images/[name].[ext]"}
      ]
    },
    resolve: {
      modulesDirectories: [
        path.join(projectRoot, 'node_modules'),
        path.join(projectRoot, 'themes'),
      ]
    },
    devtool: 'source-map',
    plugins: [
      themeExtractPlugin
    ]
  };
};
