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
var defaults = {
  pom: 'pom.xml',
  src: ['package.json']
};

var task = module.exports = function (grunt) {

  grunt.registerMultiTask('pom-version-to-json', 'Pulls version number from a Maven POM file to insert it in JSON files', function () {

    var options = this.options(defaults);
    var done = this.async();
    var pomVersion = require('../lib/pom-version');

    var updateVersion = require('../lib/update-version');
    pomVersion(options.pom, function (err, version) {

      if (err) {
        done(err);
      }

      // pomVersion might be in the Maven X.Y.Z-SNAPSHOT format
      // This is not compatible with NPM (bower seems fine),
      // so we need to strip the 'SNAPSHOT part'
      var indexOfDash = version.indexOf('-');
      if (indexOfDash !== -1) {
        version = version.substr(0, indexOfDash);
      }

      // Semver (used by NPM) works on a 3 number system
      // and NPM breaks if there is only two... so add trailing '.0'
      var parts = version.split('.');
      if (parts.length < 3) {
        var missing = 3 - parts.length;
        for (var i = 0; i < missing; i++) {
          parts.push(0);
        }
        version = parts.join('.');
      }

      options.src.forEach(function (path) {
          updateVersion(path, version);
        });
      done();
    });
  });
};
