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
module.exports = function (grunt) {

  var defaults = {
    port: parseInt(grunt.option('dye-server.port'), 10) || 11111,
    themePath: grunt.option('dye-server.themePath') || 'css/theme.css',
    proxiedHost: grunt.option('dye-server.proxiedHost') || 'localhost',
    proxiedPort: grunt.option('dye-server.proxiedPort') || 10000,
    proxiedContext: grunt.option('dye-server.proxiedContext') || '/'
  };

  // Struggled too much with grunt-contrib-connect so writting a task with connect
  grunt.registerMultiTask('dye-server', 'Starts a proxy server to help development', function () {

    var options = this.options(defaults);

    var loomURL = 'http://' + options.proxiedHost + ':' + options.proxiedPort + '/loom';
    var weaverURL = 'http://' + options.proxiedHost + ':' + options.proxiedPort + options.proxiedContext;

    var connect = require('connect');
    var serveStatic = require('serve-static');
    var inject = require('connect-inject');
    var http = require('http');
    var path = require('path');

    var app = connect();

    app.use(serveStatic(process.cwd()));

    var proxy = require('proxy-middleware');
    var url = require('url');

    var request = require('superagent');

    app.use('/js/weaver-config.json', function (req, res, next) {

      var weaverConfig = {};
      request(weaverURL + '/js/weaver-config.json').end(function (response) {

        if (response.status === 200) {
          weaverConfig = JSON.parse(response.text);
        }

        weaverConfig.theme = options.themePath;

        res.statusCode = 200;
        res.setHeader('Content-Type', 'application/json');
        res.end(JSON.stringify(weaverConfig));
      });
    });

    var styleguidePath = path.join(__dirname, '../node_modules/weaver/docs/css');
    console.log(styleguidePath);

    app.use('/dye/styleguide', inject({
      snippet: '<link rel="stylesheet" href="/' + options.themePath + '">'
    }));
    app.use('/dye/styleguide', serveStatic(styleguidePath));

    app.use('/loom', proxy(url.parse(loomURL)));
    app.use('/', proxy(url.parse(weaverURL)));

    http.createServer(app).listen(options.port);
  });
};