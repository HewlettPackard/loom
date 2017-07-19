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
// Common configuration for Webpack builds
module.exports = function (projectRoot){

  var webpack = require('webpack');
  var path = require('path');

  var xml2js = require('xml2js');

  function pomVersion(path) {

    var parseString = require('xml2js').parseString;
    var fs = require('fs');
    var xml = fs.readFileSync(path);

    var version;
    parseString(xml, function (err, result) {

      if (err) {
        callback(err);
        return;
      }
      // TODO : Refactor so it's the same code as the Grunt task
      version = result.project.parent[0].version[0];
    });

    return version;
  }

  return {
    module: {
      loaders: [
        { test: /\.html$/, loader: "html"},
        { test: /\.css$/, loader: "raw"},

        // Cytosape needs jQuery but crashes when parsed by webpack
        // for require statements. So we need to expose jQuery as a global
        //        v---- Leading slash to avoid exposing chai-jquery (or any other xyz-jquery)
        { test: /\/jquery\.js/, loader: 'expose?$!expose?jQuery'},
        // Makes sure jQuery is loaded properly when loading other jQuery features
        { test: /jquery.*/, loader: 'imports?jQuery=jquery&$=jquery'},
        { test: /\.woff(\?.*)*$/, loader: "file?name=fonts/[name].[ext]"},
        { test: /\.woff2(\?.*)*$/, loader: "file?name=fonts/[name].[ext]"},
        { test: /\.eot(\?.*)*$/, loader: "file?name=fonts/[name].[ext]"},
        { test: /\.ttf(\?.*)*$/, loader: "file?name=fonts/[name].[ext]"},
        { test: /\.otf(\?.*)*$/, loader: "file?name=fonts/[name].[ext]"},
        // Unlike standard SVG files, SVG fonts need to go in the fonts folder
        { test: /webfont.svg(\?.*)*$/, loader: "file?name=fonts/[name].[ext]"},
        { test: /\.svg$/, loader: "file?name=images/[name].[ext]"},
        { test: /\.json$/, loader: 'json'},
      ],
      // Cytoscape is provided as a "compiled" library, webpack doesn't need to parse
      // `require` calls inside it
      noParse: [ /cytoscape/ ]
    },
    resolve: {
      extensions: ['', '.webpack.js', '.web.js', '.ts', '.js'],
      modulesDirectories: [
        'node_modules',
        path.join(projectRoot, 'node_modules'),
        path.join(projectRoot, 'vendor')
      ],
      alias: {
        // Alias for resolving other modules in the project ...
        'weft': path.join(projectRoot, 'lib/weft/js/weft'),
        'weaver': path.join(projectRoot, 'lib/weaver-lib/js/weaver'),
        'weaver-lib/css': path.join(projectRoot, 'lib/weaver-lib/css'),
        'windows8': path.join(projectRoot, 'apps/windows8/js/windows8'),
        'dye': path.join(projectRoot, 'themes/dye'),
        'plugins': path.join(projectRoot, 'plugins/base/js'),
        'weaver-dma': path.join(projectRoot, 'plugins/plugin-dma/js/weaver-dma'),
        'weaver-table': path.join(projectRoot, 'plugins/plugin-table/js/weaver-table'),
        'weaver-treemap': path.join(projectRoot, 'plugins/plugin-treemap/js/weaver-treemap'),
        'weaver-map': path.join(projectRoot, 'plugins/plugin-map/js/weaver-map'),

        // ... or from 3rd party libraries
        'jquery-ui': path.join(projectRoot,'node_modules/jquery-ui/ui'),
        'd3': path.join(projectRoot, 'vendor/d3.js'),
        'cartogram': path.join(projectRoot, 'vendor/cartogram/cartogram-d3.js'),
        'position-calculator': path.join(projectRoot, 'vendor/position-calculator.min.js'),

        // Project uses lodash instead of underscore, so there's no need to
        // bundle underscore with it.
        'underscore': 'lodash'
      },


    },
    resolveLoader: { root: path.join(projectRoot, "node_modules") },
    plugins: [
        // Because of the way NPM install packages, different versions of jQuery and Backbone
        // get loaded in the project. These replacements ensure only one is loaded
        new webpack.NormalModuleReplacementPlugin(/^jquery$/, path.join(projectRoot, 'node_modules/jquery/dist/jquery.js')),
        new webpack.NormalModuleReplacementPlugin(/^backbone$/, path.join(projectRoot, 'node_modules/backbone/backbone.js')),

        // Pulls the version number from the pom.xml so it can be displayed in the app
        new webpack.DefinePlugin({
          VERSION: JSON.stringify(pomVersion(path.join(projectRoot, 'pom.xml')))
        })
    ]
  };
};
