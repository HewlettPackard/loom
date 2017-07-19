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

module.exports = function (projectRoot, appRoot, cliArgs) {

  var path = require('path');
  var fs = require('fs');
  var _ = require('lodash');
  var webpack = require('webpack');

  var ExtractTextPlugin = require('extract-text-webpack-plugin');
  var HTMLPlugin = require('html-webpack-plugin');

  var configExtractPlugin = new ExtractTextPlugin('js/weaver-config.json');
  var styleExtractPlugin = new ExtractTextPlugin('css/style.css');

  var entries = [
    path.join(appRoot, './js/main.js')
  ];

  var PLUGIN_ENTRY_FILES = [
    // TODO: Lookup TypeScript files once TypeScript compilation works OK
    //'ts/main.ts',
    'js/main.js'
  ];

  function getPluginEntry(pluginName) {
    var pluginPath = path.join(projectRoot, 'plugins/plugin-' + pluginName);
    return _(PLUGIN_ENTRY_FILES).map(function (entryFile) {
      return path.join(pluginPath, entryFile);
    }).find(function (entryPath) {
      return fs.existsSync(entryPath);
    });
  }

  function getPluginEntries (pluginNameList) {
    return pluginNameList.map(getPluginEntry);
  }

  function getPluginNameList() {
    if (cliArgs['with-plugins']) {
      return cliArgs['with-plugins'].split(',');
    }
  }

  function validateEntries(pluginEntries, pluginNameList) {

    return _.reduce(pluginEntries, function validateEntry(result, pluginEntry, index) {
      if (!pluginEntry) {
        result.ok = false;
        result.errors.push({
          pluginName: pluginNameList[index]
        });
      }
      return result;
    }, {
      ok: true,
      errors: []
    });
  }

  var pluginNameList = getPluginNameList();
  if (pluginNameList) {
    var pluginEntries = getPluginEntries(pluginNameList);
    var validationResult = validateEntries(pluginEntries, pluginNameList);
    if (validationResult.ok) {
      entries = entries.concat(pluginEntries);
    } else {
      console.log('Could not find entries for plugins', validationResult.errors);
    }
  }

  var config = require('./app-base')(projectRoot);

  _.merge(config, {
    entry: entries,
    output: {
      path: path.join(appRoot, 'dist'),
      filename: 'js/app.js',
      pathinfo: true
    },
    devtool: 'source-map'
  });

  config.resolve.root = path.join(appRoot, 'js');

  Array.prototype.push.apply(config.plugins, [
    // Extracts style.css into a separate file
    styleExtractPlugin,
    // Extracts weaver-config.json into its separate file
    configExtractPlugin,
    // Generates the index.html file that loads Weaver
    new HTMLPlugin({
      template: path.join(appRoot, 'index.html')
    }),

    // TODO: Fix imports of jQuery where needed
    new webpack.ProvidePlugin({
      $: "jquery",
      jQuery: "jquery",
      "window.jQuery": "jquery"
    })
  ]);

  Array.prototype.unshift.apply(config.module.loaders, [
    { test: /\.less$/, loader: styleExtractPlugin.extract('css?sourceMap!autoprefixer!less', {
      publicPath: '../'
    })},
    { test: /weaver-config.json$/, loader: configExtractPlugin.extract('raw')},
  ]);

  if (cliArgs.app === 'windows8') {
    config.plugins.push(new webpack.NormalModuleReplacementPlugin(/^weaver\/utils\/confirm$/, path.join(appRoot, 'js/windows8/utils/confirm.js')));
  }

  return config;

};
