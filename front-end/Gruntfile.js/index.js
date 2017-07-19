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
  "use strict";

  require('load-grunt-tasks')(grunt);
  require('./tasks/pom-version-to-appxmanifest')(grunt);

  grunt.initConfig({

    msbuild: {
      all: {
        src: ['apps/windows8/windows8.1.jsproj'],
        options: {
          targets: ['Clean', 'Rebuild'],
          buildParameters: {
            projectConfiguration: 'Debug',
            'BuildDir': 'apps/windows8/AppPackages'
          }
        }
      }
    },

    'pom-version-to-appxmanifest': {
      all: {
        options: {
          pom: './pom.xml',
          src: ['apps/windows8/package.1.appxmanifest']
        }
      }
    }
  });

  grunt.registerTask('default', ['msbuild']);
};
